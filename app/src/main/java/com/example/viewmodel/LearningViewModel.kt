package com.example.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.random.Random

sealed interface AppScreen {
    object WelcomeNameInput : AppScreen     // Initial screen if name is default or if child wants to change it
    object Home : AppScreen                  // Shows colorful Class blocks, Subject blocks, Chapter blocks
    object GamesList : AppScreen             // For selected chapter, displays the available games: Quiz, Match, Motion Runner
    object QuizGame : AppScreen              // Interactive quiz game
    object MatchingGame : AppScreen          // Matching card game
    object MotionGame : AppScreen            // Mario platformer-style side scroller
    object Leaderboard : AppScreen
    object SkinsShop : AppScreen
    object ParentConsole : AppScreen
}

class LearningViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application, viewModelScope)
    private val repository = AppRepository(database.appDao())

    // --- State flows from Database ---
    val studentProfile: StateFlow<StudentProfile> = repository.studentProfile
        .map { it ?: StudentProfile() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), StudentProfile())

    val activityLogs: StateFlow<List<ActivityLog>> = repository.activityLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val leaderboard: StateFlow<List<LeaderboardEntry>> = repository.leaderboard
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allProgress: StateFlow<List<ChapterProgress>> = repository.allProgress
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Curricular Navigation State ---
    var currentScreen by mutableStateOf<AppScreen>(AppScreen.Home)
    var selectedClass by mutableStateOf<GameClass?>(null)
    var selectedSubject by mutableStateOf<Subject?>(null)
    var selectedChapter by mutableStateOf<Chapter?>(null)

    // --- UI/UX Notifications ---
    var bannerMessage by mutableStateOf<String?>(null)
    var rewardEarnedBanner by mutableStateOf<String?>(null) // e.g. "+50 PTS, +20 COINS!"

    // --- 1. Quiz Game Play States ---
    var quizCurrentQuestionIndex by mutableStateOf(0)
    var quizSelectedOptionIndex by mutableStateOf<Int?>(null)
    var quizIsAnswered by mutableStateOf(false)
    var quizScore by mutableStateOf(0)
    var quizCompleted by mutableStateOf(false)

    // --- 2. Matching Cards Game States ---
    data class CardItem(val index: Int, val text: String, val isKey: Boolean, var isMatched: Boolean = false)
    val matchCards = mutableStateListOf<CardItem>()
    var selectedFirstCardIndex by mutableStateOf<Int?>(null)
    var selectedSecondCardIndex by mutableStateOf<Int?>(null)
    var matchingTries by mutableStateOf(0)
    var matchingSuccessCount by mutableStateOf(0)
    var matchingCompleted by mutableStateOf(false)

    // --- 3. Mario-Style Side-Scroller Motion Game States ---
    var runnerScore by mutableStateOf(0)
    var runnerCoinsCollected by mutableStateOf(0)
    var runnerDistance by mutableStateOf(0f)
    var playerYPercent by mutableStateOf(0.75f) // floor position
    var isJumping by mutableStateOf(false)
    var isGameOver by mutableStateOf(false)
    var isGameWon by mutableStateOf(false)
    
    // Simple obstacles for side-scroller (X percent from 0f to 1.1f, Y position, speed, type)
    data class MarioObstacle(val id: Int, var xPercent: Float, val heightPercent: Float, val type: String)
    val obstacles = mutableStateListOf<MarioObstacle>()
    private var gameLoopJob: Job? = null

    init {
        // Automatically check if standard profile configuration setup is required
        viewModelScope.launch {
            studentProfile.collect { profile ->
                if (profile.name == "Young Learner" && currentScreen == AppScreen.Home) {
                    currentScreen = AppScreen.WelcomeNameInput
                }
            }
        }
    }

    // --- Actions ---
    fun updatePlayerName(newName: String) {
        if (newName.isBlank()) return
        viewModelScope.launch {
            repository.updateProfileName(newName.trim())
            currentScreen = AppScreen.Home
            showBanner("Welcome to your fun classroom, ${newName.trim()}! 🤠")
        }
    }

    fun selectDifficulty(difficulty: String) {
        viewModelScope.launch {
            repository.updateDifficulty(difficulty)
            showBanner("Difficulty updated to $difficulty! Prepare for the quest! ⚡")
        }
    }

    fun equipSkin(skinId: String) {
        viewModelScope.launch {
            repository.updateSelectedSkin(skinId)
            showBanner("Skins updated! Looking awesome! 😎")
        }
    }

    fun buySkin(skin: CustomSkin) {
        viewModelScope.launch {
            val success = repository.buyAndUnlockSkin(skin.id, skin.cost)
            if (success) {
                showBanner("Unlocked custom skin ${skin.name}! Yay! 🎉")
            } else {
                showBanner("Oh no! You need more coins to unlock this style! Keep playing! 💎")
            }
        }
    }

    fun selectClassBlock(cl: GameClass) {
        selectedClass = cl
        selectedSubject = null
        selectedChapter = null
    }

    fun selectSubjectBlock(sub: Subject) {
        selectedSubject = sub
        selectedChapter = null
    }

    fun selectChapterBlock(chap: Chapter) {
        selectedChapter = chap
        currentScreen = AppScreen.GamesList
    }

    // Go back in the hierarchy
    fun handleBackNavigation() {
        when (currentScreen) {
            AppScreen.WelcomeNameInput -> currentScreen = AppScreen.Home
            AppScreen.GamesList -> {
                selectedChapter = null
                currentScreen = AppScreen.Home
            }
            AppScreen.QuizGame, AppScreen.MatchingGame, AppScreen.MotionGame -> {
                stopMotionGameLoop()
                currentScreen = AppScreen.GamesList
            }
            AppScreen.Leaderboard -> currentScreen = AppScreen.Home
            AppScreen.SkinsShop -> currentScreen = AppScreen.Home
            AppScreen.ParentConsole -> currentScreen = AppScreen.Home
            AppScreen.Home -> {
                if (selectedSubject != null) {
                    selectedSubject = null
                } else if (selectedClass != null) {
                    selectedClass = null
                }
            }
        }
    }

    private fun showBanner(msg: String) {
        bannerMessage = msg
        viewModelScope.launch {
            delay(3500)
            if (bannerMessage == msg) {
                bannerMessage = null
            }
        }
    }

    // ==========================================
    // --- 1. QUIZ GAME CONTROLLER ---
    // ==========================================
    fun startQuizGame() {
        val chapter = selectedChapter ?: return
        quizCurrentQuestionIndex = 0
        quizSelectedOptionIndex = null
        quizIsAnswered = false
        quizScore = 0
        quizCompleted = false
        currentScreen = AppScreen.QuizGame
    }

    fun submitQuizAnswer(selectedIdx: Int) {
        if (quizIsAnswered) return
        quizSelectedOptionIndex = selectedIdx
        quizIsAnswered = true
        val chapter = selectedChapter ?: return
        val question = chapter.quizQuestions[quizCurrentQuestionIndex]
        if (selectedIdx == question.correctIndex) {
            quizScore++
        }
    }

    fun nextQuizQuestion() {
        val chapter = selectedChapter ?: return
        if (quizCurrentQuestionIndex < chapter.quizQuestions.size - 1) {
            quizCurrentQuestionIndex++
            quizSelectedOptionIndex = null
            quizIsAnswered = false
        } else {
            quizCompleted = true
            // Save Progress and Earn Rewards
            viewModelScope.launch {
                val totalQ = chapter.quizQuestions.size
                repository.saveGameProgressAndEarnRewards(
                    classId = selectedClass?.id ?: "class_1",
                    subjectId = selectedSubject?.id ?: "math",
                    chapterId = chapter.id,
                    gameType = "Quiz",
                    score = quizScore,
                    maxScore = totalQ
                )
                rewardEarnedBanner = "+${quizScore * 50} score, +${(quizScore.toFloat()/totalQ * 20).toInt()} coins!"
                delay(3000)
                rewardEarnedBanner = null
            }
        }
    }

    // ==========================================
    // --- 2. MATCHING CARDS GAME CONTROLLER ---
    // ==========================================
    fun startMatchingGame() {
        val chapter = selectedChapter ?: return
        matchingTries = 0
        matchingSuccessCount = 0
        matchingCompleted = false
        selectedFirstCardIndex = null
        selectedSecondCardIndex = null

        // Prep combinations of Keys and Values
        val itemsList = mutableListOf<CardItem>()
        chapter.matchPairs.forEachIndexed { num, pair ->
            itemsList.add(CardItem(num, pair.key, isKey = true))
            itemsList.add(CardItem(num, pair.value, isKey = false))
        }
        itemsList.shuffle()

        matchCards.clear()
        matchCards.addAll(itemsList)
        currentScreen = AppScreen.MatchingGame
    }

    fun selectMatchingCard(index: Int) {
        if (matchCards[index].isMatched) return
        if (selectedFirstCardIndex == index || selectedSecondCardIndex == index) return

        if (selectedFirstCardIndex == null) {
            selectedFirstCardIndex = index
        } else if (selectedSecondCardIndex == null) {
            selectedSecondCardIndex = index
            matchingTries++
            
            // Check Match
            val card1 = matchCards[selectedFirstCardIndex!!]
            val card2 = matchCards[selectedSecondCardIndex!!]

            if (card1.index == card2.index && card1.isKey != card2.isKey) {
                // Correct match!
                card1.isMatched = true
                card2.isMatched = true
                matchingSuccessCount++
                selectedFirstCardIndex = null
                selectedSecondCardIndex = null

                if (matchingSuccessCount == chapterPairsCount()) {
                    // Game won!
                    matchingCompleted = true
                    viewModelScope.launch {
                        // Calculate score: less tries = more rewards!
                        val totalPairs = chapterPairsCount()
                        val calculatedScore = (totalPairs * 10 - (matchingTries - totalPairs).coerceAtLeast(0) * 2).coerceIn(10, totalPairs * 10)
                        repository.saveGameProgressAndEarnRewards(
                            classId = selectedClass?.id ?: "class_1",
                            subjectId = selectedSubject?.id ?: "math",
                            chapterId = selectedChapter?.id ?: "chap_1",
                            gameType = "Matching",
                            score = calculatedScore,
                            maxScore = totalPairs * 10
                        )
                        rewardEarnedBanner = "+${calculatedScore * 50} pts, +25 coins!"
                        delay(3000)
                        rewardEarnedBanner = null
                    }
                }
            } else {
                // Wrong Match, wait a bit then reset selection
                viewModelScope.launch {
                    delay(1200)
                    selectedFirstCardIndex = null
                    selectedSecondCardIndex = null
                }
            }
        }
    }

    private fun chapterPairsCount(): Int {
        return selectedChapter?.matchPairs?.size ?: 4
    }

    // ==========================================
    // --- 3. MARIO RUNNER PLATFORMER CONTROLLER ---
    // ==========================================
    fun startMotionGame() {
        val chapter = selectedChapter ?: return
        runnerScore = 0
        runnerCoinsCollected = 0
        runnerDistance = 0f
        playerYPercent = 0.72f
        isJumping = false
        isGameOver = false
        isGameWon = false
        
        obstacles.clear()
        // Generate obstacles spaced out nicely
        val count = when(studentProfile.value.gameDifficulty) {
            "Easy" -> 7
            "Medium" -> 11
            "Hard" -> 15
            else -> 8
        }
        for (i in 0 until count) {
            val type = if (Random.nextBoolean()) "BLOCK" else "COIN"
            val height = if (type == "COIN") 0.45f else 0.72f // coins can spawn high up
            val xDistance = 1.0f + (i * 0.45f) + Random.nextFloat() * 0.25f
            obstacles.add(MarioObstacle(i, xDistance, height, type))
        }

        currentScreen = AppScreen.MotionGame
        startMotionGameLoop()
    }

    private fun startMotionGameLoop() {
        gameLoopJob?.cancel()
        gameLoopJob = viewModelScope.launch(Dispatchers.Main) {
            val speedFactor = when(studentProfile.value.gameDifficulty) {
                "Easy" -> 0.015f
                "Medium" -> 0.024f
                "Hard" -> 0.035f
                else -> 0.015f
            }
            while (!isGameOver && !isGameWon) {
                delay(30) // ~30fps loop
                
                // Jump physics simulation
                if (isJumping) {
                    playerYPercent -= 0.065f
                    if (playerYPercent <= 0.30f) {
                        isJumping = false // reached top peak, begin falling
                    }
                } else {
                    if (playerYPercent < 0.72f) {
                        playerYPercent += 0.05f
                        if (playerYPercent > 0.72f) {
                            playerYPercent = 0.72f // bounded to floor level
                        }
                    }
                }

                // Obstacle movement
                val iterator = obstacles.listIterator()
                while (iterator.hasNext()) {
                    val obs = iterator.next()
                    obs.xPercent -= speedFactor

                    // Collisions check (Player is around xPercent approx 0.20f)
                    val distanceX = kotlin.math.abs(obs.xPercent - 0.22f)
                    val distanceY = kotlin.math.abs(obs.heightPercent - playerYPercent)
                    
                    if (distanceX < 0.08f && distanceY < 0.15f) {
                        // HIT!
                        if (obs.type == "COIN") {
                            // Collect coin reward
                            runnerCoinsCollected++
                            runnerScore += 10
                            iterator.remove()
                        } else {
                            // Hit hard block (mushrooms/spikes)
                            isGameOver = true
                        }
                    }
                }

                runnerDistance += speedFactor * 10
                
                // If we ran far enough and obstacles are cleared, we win!
                val remainingThreats = obstacles.count { it.type == "BLOCK" && it.xPercent > 0f }
                if (remainingThreats == 0 && runnerDistance > 12f) {
                    isGameWon = true
                    val rewardCoins = runnerCoinsCollected + 15
                    val rewardScore = runnerScore + 100
                    viewModelScope.launch {
                        repository.saveGameProgressAndEarnRewards(
                            classId = selectedClass?.id ?: "class_1",
                            subjectId = selectedSubject?.id ?: "math",
                            chapterId = selectedChapter?.id ?: "chap_1",
                            gameType = "Motion",
                            score = rewardScore,
                            maxScore = 250
                        )
                        rewardEarnedBanner = "Super run! +$rewardScore score, +$rewardCoins coins!"
                        delay(3500)
                        rewardEarnedBanner = null
                    }
                }
            }
        }
    }

    private fun stopMotionGameLoop() {
        gameLoopJob?.cancel()
        gameLoopJob = null
    }

    fun makePlayerJump() {
        if (playerYPercent >= 0.70f) { // Can jump only from near floor level
            isJumping = true
        }
    }

    // --- Parent View Admin Actions ---
    fun clearLogs() {
        viewModelScope.launch {
            repository.clearHistory()
            showBanner("Admin activity log history cleared cleanly!")
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopMotionGameLoop()
    }
}
