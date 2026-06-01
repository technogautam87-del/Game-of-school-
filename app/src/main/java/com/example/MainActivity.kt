package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.*
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.AppScreen
import com.example.viewmodel.LearningViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    MainScreenContainer(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun MainScreenContainer(
    modifier: Modifier = Modifier,
    viewModel: LearningViewModel = viewModel()
) {
    val studentProfile by viewModel.studentProfile.collectAsStateWithLifecycle()
    val activityLogs by viewModel.activityLogs.collectAsStateWithLifecycle()
    val leaderboardEntries by viewModel.leaderboard.collectAsStateWithLifecycle()
    val allProgress by viewModel.allProgress.collectAsStateWithLifecycle()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFDFCFB))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            
            // Curved Whimsical Header Group
            if (viewModel.currentScreen != AppScreen.WelcomeNameInput) {
                TopCurvedHeaderSection(
                    profile = studentProfile,
                    viewModel = viewModel
                )
            }

            // Screen Switcher Layout
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (viewModel.currentScreen) {
                    AppScreen.WelcomeNameInput -> {
                        NameSetupScreen(
                            profile = studentProfile,
                            onSubmit = { viewModel.updatePlayerName(it) }
                        )
                    }
                    AppScreen.Home -> {
                        CurriculumSelectorScreen(
                            viewModel = viewModel,
                            allProgress = allProgress
                        )
                    }
                    AppScreen.GamesList -> {
                        GamesSelectorScreen(
                            viewModel = viewModel
                        )
                    }
                    AppScreen.QuizGame -> {
                        QuizPlaygroundScreen(
                            viewModel = viewModel
                        )
                    }
                    AppScreen.MatchingGame -> {
                        MatchingPairsPlaygroundScreen(
                            viewModel = viewModel
                        )
                    }
                    AppScreen.MotionGame -> {
                        MarioRunnerPlaygroundScreen(
                            viewModel = viewModel
                        )
                    }
                    AppScreen.Leaderboard -> {
                        LeaderboardListScreen(
                            entries = leaderboardEntries,
                            currentStudent = studentProfile
                        )
                    }
                    AppScreen.SkinsShop -> {
                        WardrobeShopScreen(
                            profile = studentProfile,
                            viewModel = viewModel
                        )
                    }
                    AppScreen.ParentConsole -> {
                        ParentSupervisorLogsScreen(
                            logs = activityLogs,
                            viewModel = viewModel
                        )
                    }
                }
            }

            // Bottom Nav panel
            if (viewModel.currentScreen != AppScreen.WelcomeNameInput) {
                ArtisticBottomNavigationBar(
                    currentScreen = viewModel.currentScreen,
                    onNavigate = { screen ->
                        viewModel.currentScreen = screen
                    }
                )
            }
        }

        // Drop-down banner messages
        viewModel.bannerMessage?.let { banner ->
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp)
                    .padding(horizontal = 24.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFF673AB7))
                    .border(2.dp, Color.White, RoundedCornerShape(24.dp))
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .testTag("app_banner")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = "Alert",
                        tint = Color(0xFFFFEB3B),
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = banner,
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Reward level up pops
        viewModel.rewardEarnedBanner?.let { reward ->
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .clip(RoundedCornerShape(32.dp))
                    .background(Color(0xFFFF9800))
                    .border(4.dp, Color(0xFFFFE082), RoundedCornerShape(32.dp))
                    .padding(horizontal = 24.dp, vertical = 16.dp)
                    .testTag("reward_banner")
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "GREAT EFFORT! 🏆",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = reward,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "🌟 Keep Learning 🌟",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

@Composable
fun TopCurvedHeaderSection(
    profile: StudentProfile,
    viewModel: LearningViewModel
) {
    val activeSkin = CurriculumData.skins.find { it.id == profile.selectedSkin } ?: CurriculumData.skins.first()
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp))
            .background(Color(0xFFF3E7FF))
            .padding(start = 20.dp, end = 20.dp, top = 40.dp, bottom = 20.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(Color(activeSkin.colorHex))
                            .border(2.dp, Color.White, CircleShape)
                            .clickable { viewModel.currentScreen = AppScreen.WelcomeNameInput }
                            .testTag("profile_avatar"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = activeSkin.avatarEmoji, fontSize = 26.sp)
                    }

                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Lvl ${profile.currentLevel}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF673AB7),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFD1C4E9))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                            Text(
                                text = "Class ${viewModel.selectedClass?.name?.substringAfter(" ") ?: "Explorer"}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE91E63),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFF8BBD0))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                        Text(
                            text = "Hi, ${profile.name}!",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF263238),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Coins
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.White)
                            .border(1.dp, Color(0xFFFFF9C4), RoundedCornerShape(20.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(text = "🪙", fontSize = 14.sp)
                        Text(text = "${profile.coins}", fontSize = 13.sp, fontWeight = FontWeight.Black, color = Color(0xFFE65100))
                    }

                    // Score
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.White)
                            .border(1.dp, Color(0xFFFFCCBC), RoundedCornerShape(20.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(text = "⭐", fontSize = 14.sp)
                        Text(text = "${profile.totalScore}", fontSize = 13.sp, fontWeight = FontWeight.Black, color = Color(0xFFD84315))
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                val nextLevelXp = 200
                val progressLeft = profile.totalScore % nextLevelXp
                val progressFraction = progressLeft.toFloat() / nextLevelXp.toFloat()
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "LEVEL UP PROGRESS",
                        fontSize = 9.sp,
                        color = Color(0xFF7E57C2),
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${nextLevelXp - progressLeft} XP TO LEVEL UP",
                        fontSize = 9.sp,
                        color = Color(0xFF7E57C2),
                        fontWeight = FontWeight.Bold
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.6f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(progressFraction.coerceIn(0.05f, 1f))
                            .clip(CircleShape)
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color(0xFF9C27B0), Color(0xFF3F51B5))
                                )
                            )
                    )
                }
            }
        }
    }
}

@Composable
fun NameSetupScreen(
    profile: StudentProfile,
    onSubmit: (String) -> Unit
) {
    var nameInput by remember { mutableStateOf(profile.name) }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(32.dp))
                .background(Color(0xFFF3E7FF))
                .padding(24.dp)
        ) {
            Text(
                text = "🎒 My Avatar Name",
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF4A148C),
                textAlign = TextAlign.Center
            )

            Text(
                text = "Type your custom student hero name so that classmates on the global leaderboard can view your rank!",
                fontSize = 13.sp,
                color = Color(0xFF6A1B9A),
                textAlign = TextAlign.Center
            )

            OutlinedTextField(
                value = nameInput,
                onValueChange = { nameInput = it },
                label = { Text("Hero Name") },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("name_edit_input")
            )

            Button(
                onClick = { onSubmit(nameInput) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF673AB7)),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("submit_name_btn")
            ) {
                Text(text = "LET'S STUDY & PLAY! 🚀", fontWeight = FontWeight.Black, fontSize = 15.sp, color = Color.White)
            }
        }
    }
}

@Composable
fun CurriculumSelectorScreen(
    viewModel: LearningViewModel,
    allProgress: List<ChapterProgress>
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Classes level Grid blocks
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(text = "🎨", fontSize = 24.sp)
                    Text(
                        text = "CHOOSE YOUR CLASS",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF37474F),
                        letterSpacing = 1.sp
                    )
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CurriculumData.classes.forEach { cls ->
                        val isSelected = (viewModel.selectedClass?.id == cls.id)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(95.dp)
                                .clip(RoundedCornerShape(24.dp))
                                .background(Color(cls.rawColor))
                                .border(
                                    width = if (isSelected) 4.dp else 0.dp,
                                    color = if (isSelected) Color(0xFF673AB7) else Color.Transparent,
                                    shape = RoundedCornerShape(24.dp)
                                )
                                .clickable { viewModel.selectClassBlock(cls) }
                                .padding(12.dp)
                                .testTag("class_block_${cls.id}"),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = cls.icon, fontSize = 26.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = cls.name,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF37474F)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Subjects level colorful Grid blocks
        item {
            val currentClass = viewModel.selectedClass
            if (currentClass != null) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(text = "📐", fontSize = 24.sp)
                            Text(
                                text = "SUBJECTS FOR ${currentClass.name.uppercase()}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF37474F),
                                letterSpacing = 0.5.sp
                            )
                        }
                    }

                    val subjects = CurriculumData.subjects[currentClass.id] ?: emptyList()
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 240.dp)
                    ) {
                        items(subjects) { subject ->
                            val isSelected = (viewModel.selectedSubject?.id == subject.id)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(115.dp)
                                    .clip(RoundedCornerShape(28.dp))
                                    .background(Color(subject.rawColor))
                                    .border(
                                        width = if (isSelected) 4.dp else 1.dp,
                                        color = if (isSelected) Color(0xFF7B1FA2) else Color.White.copy(alpha = 0.5f),
                                        shape = RoundedCornerShape(28.dp)
                                    )
                                    .clickable { viewModel.selectSubjectBlock(subject) }
                                    .padding(14.dp)
                                    .testTag("subject_block_${subject.id}"),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(text = subject.icon, fontSize = 26.sp)
                                    Text(
                                        text = subject.name,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFF263238),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = subject.description,
                                        fontSize = 10.sp,
                                        color = Color(0xFF455A64),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0xFFE1F5FE))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "⭐ Select any colorful Class Block above to explore topics! ⭐",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color(0xFF0288D1),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Chapters level colorful Grid blocks
        item {
            val currentClass = viewModel.selectedClass
            val currentSubject = viewModel.selectedSubject
            
            if (currentClass != null && currentSubject != null) {
                val chapters = CurriculumData.getChapterList(currentClass.id, currentSubject.id)
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(text = "📚", fontSize = 24.sp)
                        Text(
                            text = "SELECT CHAPTER TO PLAY",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF37474F)
                        )
                    }

                    if (chapters.isEmpty()) {
                        Text(text = "No chapters found for this selection.", fontSize = 12.sp, color = Color.Gray)
                    }

                    chapters.forEach { chapter ->
                        val progressAndStats = allProgress.find { it.progressId == "${currentClass.id}_${currentSubject.id}_${chapter.id}" }
                        val isQuizDone = progressAndStats?.quizCompleted == true
                        val isMatchDone = progressAndStats?.matchingCompleted == true
                        val isMotionDone = progressAndStats?.motionCompleted == true

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(32.dp))
                                .background(Color(chapter.rawColor))
                                .clickable { viewModel.selectChapterBlock(chapter) }
                                .padding(16.dp)
                                .testTag("chapter_block_${chapter.id}"),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "CHAPTER ${chapter.orderId}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF455A64)
                                )
                                Text(
                                    text = chapter.name,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF212121)
                                )
                                
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(top = 4.dp)
                                ) {
                                    BadgeIndicator(title = "Quiz", isCompleted = isQuizDone)
                                    BadgeIndicator(title = "Match", isCompleted = isMatchDone)
                                    BadgeIndicator(title = "Run", isCompleted = isMotionDone)
                                }
                            }
                            
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                                    .clickable { viewModel.selectChapterBlock(chapter) },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Play",
                                    tint = Color(0xFFE91E63)
                                )
                            }
                        }
                    }
                }
            } else if (currentClass != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0xFFFFF3E0))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🎒 Click any Subject Block above to reveal dynamic study chapters!",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color(0xFFE65100),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun GamesSelectorScreen(
    viewModel: LearningViewModel
) {
    val chapter = viewModel.selectedChapter ?: return
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { viewModel.handleBackNavigation() }
        ) {
            Icon(Icons.Filled.ArrowBack, contentDescription = "Go Back")
            Text(text = "Back to Chapter Library", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }

        Text(
            text = "Game Quest Room",
            fontSize = 24.sp,
            fontWeight = FontWeight.Black,
            color = Color(0xFF3F51B5)
        )
        Text(
            text = "Chapter: ${chapter.name}",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFE91E63)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFEDE7F6)),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "CHALLENGE DIFFICULTY",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF5E35B1)
                )
                
                val currentDiff = viewModel.studentProfile.value.gameDifficulty
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Easy", "Medium", "Hard").forEach { diff ->
                        val active = currentDiff == diff
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (active) Color(0xFF673AB7) else Color.White)
                                .clickable { viewModel.selectDifficulty(diff) }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = diff,
                                color = if (active) Color.White else Color(0xFF5E35B1),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            InteractiveGameModeBlock(
                title = "IQ Brainy Quiz",
                category = "TEST KNOWLEDGE",
                details = "${chapter.quizQuestions.size} Multiple choice puzzles with colorful animations & fun facts!",
                icon = "📝",
                backgroundColor = 0xFFFFEBEE,
                accentColor = 0xFFEF5350,
                tag = "quiz_mode",
                onClick = { viewModel.startQuizGame() }
            )

            InteractiveGameModeBlock(
                title = "Flipping Match Pairs",
                category = "MEMORY MASTER",
                details = "Flip and pair keys to their corresponding definitions instantly!",
                icon = "🧩",
                backgroundColor = 0xFFE3F2FD,
                accentColor = 0xFF42A5F5,
                tag = "match_mode",
                onClick = { viewModel.startMatchingGame() }
            )

            InteractiveGameModeBlock(
                title = "Super Study Mario Run",
                category = "ACTION MOTION RUNNER",
                details = "Jump over blocks and grab yellow coins with your equipped avatar skin!",
                icon = "🏃",
                backgroundColor = 0xFFFFF8E1,
                accentColor = 0xFFFFB300,
                tag = "runner_mode",
                onClick = { viewModel.startMotionGame() }
            )
        }
    }
}

@Composable
fun QuizPlaygroundScreen(
    viewModel: LearningViewModel
) {
    val chapter = viewModel.selectedChapter ?: return
    val currentIdx = viewModel.quizCurrentQuestionIndex
    val totalQ = chapter.quizQuestions.size
    
    if (viewModel.quizCompleted) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(32.dp))
                    .background(Color(0xFFE8F5E9))
                    .padding(24.dp)
            ) {
                Text(text = "👑", fontSize = 60.sp)
                Text(
                    text = "QUIZ COMPLETED!",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF2E7D32)
                )
                Text(
                    text = "You scored ${viewModel.quizScore} out of $totalQ questions!",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF33691E)
                )
                
                Button(
                    onClick = { viewModel.handleBackNavigation() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text(text = "Back to Game selector", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    } else {
        val questionObj = chapter.quizQuestions[currentIdx]
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.handleBackNavigation() }) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Exit Quiz")
                }
                Text(
                    text = "Question ${currentIdx + 1} of $totalQ",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Color(0xFF5E35B1)
                )
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 120.dp),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E7FF))
            ) {
                Box(modifier = Modifier.padding(20.dp), contentAlignment = Alignment.Center) {
                    Text(
                        text = questionObj.question,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF263238),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                questionObj.options.forEachIndexed { optIdx, title ->
                    val isSelected = viewModel.quizSelectedOptionIndex == optIdx
                    val isCorrectIdx = optIdx == questionObj.correctIndex
                    
                    val cardBg = when {
                        viewModel.quizIsAnswered && isCorrectIdx -> Color(0xFFC8E6C9)
                        viewModel.quizIsAnswered && isSelected -> Color(0xFFFFCDD2)
                        isSelected -> Color(0xFFD1C4E9)
                        else -> Color.White
                    }

                    val borderColor = when {
                        viewModel.quizIsAnswered && isCorrectIdx -> Color(0xFF4CAF50)
                        viewModel.quizIsAnswered && isSelected -> Color(0xFFF44336)
                        isSelected -> Color(0xFF673AB7)
                        else -> Color(0xFFB0BEC5)
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .background(cardBg)
                            .border(2.dp, borderColor, RoundedCornerShape(20.dp))
                            .clickable { viewModel.submitQuizAnswer(optIdx) }
                            .padding(16.dp)
                            .testTag("quiz_option_$optIdx"),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "${'A' + optIdx}", fontWeight = FontWeight.Bold, color = Color(0xFF673AB7))
                            Text(text = title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }
            }

            if (viewModel.quizIsAnswered) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "💡 FUN REINFORCING FACT!",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF2E7D32)
                        )
                        Text(
                            text = questionObj.funFact,
                            fontSize = 12.sp,
                            color = Color(0xFF1B5E20)
                        )
                    }
                }

                Button(
                    onClick = { viewModel.nextQuizQuestion() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF673AB7)),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("quiz_next_button")
                ) {
                    Text(
                        text = if (currentIdx == totalQ - 1) "FINISH CHAPTER 🎉" else "CONTINUE QUEST 🚀",
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun MatchingPairsPlaygroundScreen(
    viewModel: LearningViewModel
) {
    if (viewModel.matchingCompleted) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(32.dp))
                    .background(Color(0xFFE3F2FD))
                    .padding(24.dp)
            ) {
                Text(text = "⭐🧩⭐", fontSize = 48.sp)
                Text(
                    text = "PAIRING MASTER ACHIEVED!",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF0D47A1)
                )
                Text(
                    text = "Correctly matched all blocks in ${viewModel.matchingTries} attempts!",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1565C0),
                    textAlign = TextAlign.Center
                )

                Button(
                    onClick = { viewModel.handleBackNavigation() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2)),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text(text = "Continue Journey", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.handleBackNavigation() }) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Exit Pairs")
                }
                
                Text(
                    text = "Tries: ${viewModel.matchingTries}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color(0xFF1565C0)
                )

                Text(
                    text = "Matches: ${viewModel.matchingSuccessCount} / ${viewModel.matchCards.size / 2}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color(0xFF2E7D32)
                )
            }

            Text(
                text = "Tap on a term from the left or definition on the right to build complete matching pairs!",
                fontSize = 11.sp,
                color = Color(0xFF546E7A),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(viewModel.matchCards.size) { index ->
                    val card = viewModel.matchCards[index]
                    val isFirstSelected = viewModel.selectedFirstCardIndex == index
                    val isSecondSelected = viewModel.selectedSecondCardIndex == index
                    val isSelectedMarked = isFirstSelected || isSecondSelected

                    val bgBase = when {
                        card.isMatched -> Color(0xFFC8E6C9)
                        isSelectedMarked -> Color(0xFFBBDEFB)
                        card.isKey -> Color(0xFFFFF3E0)
                        else -> Color(0xFFF1F8E9)
                    }

                    val activeBorder = when {
                        card.isMatched -> Color(0xFF4CAF50)
                        isSelectedMarked -> Color(0xFF1E88E5)
                        else -> Color(0xFFCFD8DC)
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(bgBase)
                            .border(2.dp, activeBorder, RoundedCornerShape(24.dp))
                            .clickable { viewModel.selectMatchingCard(index) }
                            .padding(10.dp)
                            .testTag("match_card_$index"),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = if (card.isKey) "❓ TERM" else "💡 KEYANSWER",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Black,
                                color = if (card.isKey) Color(0xFFE65100) else Color(0xFF33691E)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = card.text,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF263238),
                                textAlign = TextAlign.Center,
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MarioRunnerPlaygroundScreen(
    viewModel: LearningViewModel
) {
    val activeSkin = CurriculumData.skins.find { it.id == viewModel.studentProfile.value.selectedSkin } ?: CurriculumData.skins.first()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { viewModel.handleBackNavigation() }) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Exit Motion")
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = "🪙 ${viewModel.runnerCoinsCollected}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFF9800))
                Text(text = "⭐ ${viewModel.runnerScore}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF3F51B5))
                Text(text = "Dist: ${viewModel.runnerDistance.toInt()}m", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(32.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF80DEEA), Color(0xFFE0F7FA))
                    )
                )
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { viewModel.makePlayerJump() }
                    )
                }
                .testTag("motion_canvas_area")
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val canvasWidth = size.width
                val canvasHeight = size.height

                val floorY = canvasHeight * 0.85f
                drawRect(
                    color = Color(0xFF8D6E63),
                    topLeft = androidx.compose.ui.geometry.Offset(0f, floorY),
                    size = androidx.compose.ui.geometry.Size(canvasWidth, canvasHeight - floorY)
                )
                drawRect(
                    color = Color(0xFF4CAF50),
                    topLeft = androidx.compose.ui.geometry.Offset(0f, floorY),
                    size = androidx.compose.ui.geometry.Size(canvasWidth, 15f)
                )

                drawCircle(
                    color = Color.White.copy(alpha = 0.5f),
                    radius = 35f,
                    center = androidx.compose.ui.geometry.Offset(canvasWidth * 0.25f, canvasHeight * 0.20f)
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.5f),
                    radius = 50f,
                    center = androidx.compose.ui.geometry.Offset(canvasWidth * 0.28f, canvasHeight * 0.22f)
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.6f),
                    radius = 45f,
                    center = androidx.compose.ui.geometry.Offset(canvasWidth * 0.65f, canvasHeight * 0.15f)
                )
            }

            val playerYFraction = viewModel.playerYPercent
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(
                        start = 55.dp, 
                        top = (playerYFraction * 180).dp
                    )
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(Color(activeSkin.colorHex)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = activeSkin.avatarEmoji, fontSize = 28.sp)
            }

            viewModel.obstacles.forEach { obs ->
                if (obs.xPercent in 0.0f..1.1f) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(
                                start = (obs.xPercent * 280).dp,
                                top = (obs.heightPercent * 185).dp
                            )
                            .size(if (obs.type == "COIN") 32.dp else 40.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (obs.type == "COIN") Color(0xFFFFD54F) else Color(0xFFE57373)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (obs.type == "COIN") "⭐" else "🍄",
                            fontSize = 18.sp
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 12.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black.copy(alpha = 0.6f))
                    .padding(horizontal = 14.dp, vertical = 4.dp)
            ) {
                Text(text = "TAP SCREEN TO JUMP!", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }

        if (viewModel.isGameOver) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFFFFEBEE))
                    .border(2.dp, Color(0xFFEF5350), RoundedCornerShape(24.dp))
                    .padding(14.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "Mushroom blast! Reset jump energy! 🥀", fontWeight = FontWeight.Black, color = Color(0xFFC62828))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { viewModel.startMotionGame() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF5350))
                        ) {
                            Text(text = "Try Again", color = Color.White)
                        }
                        Button(
                            onClick = { viewModel.handleBackNavigation() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF90A4AE))
                        ) {
                            Text(text = "Exit", color = Color.White)
                        }
                    }
                }
            }
        }

        if (viewModel.isGameWon) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFFE8F5E9))
                    .border(2.dp, Color(0xFF4CAF50), RoundedCornerShape(24.dp))
                    .padding(14.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(text = "Victory! Run finished! 🏆", fontWeight = FontWeight.Black, color = Color(0xFF1B5E20))
                    Button(
                        onClick = { viewModel.handleBackNavigation() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                    ) {
                        Text(text = "Unlock Next Quest", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun LeaderboardListScreen(
    entries: List<LeaderboardEntry>,
    currentStudent: StudentProfile
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(text = "🏆", fontSize = 28.sp)
            Text(
                text = "GLOBAL STUDY LEADERBOARD",
                fontWeight = FontWeight.Black,
                fontSize = 15.sp,
                color = Color(0xFF3F51B5),
                letterSpacing = 0.5.sp
            )
        }

        Text(
            text = "Play games and score higher to rank top! Real-time ranking with simulated classmate profiles.",
            fontSize = 11.sp,
            color = Color(0xFF546E7A)
        )

        val sortedEntries = entries.sortedByDescending { it.score }
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(sortedEntries) { index, entry ->
                val designSkin = CurriculumData.skins.find { it.id == entry.avatarSkin } ?: CurriculumData.skins.first()
                val isSelf = entry.isRealPlayer || entry.name.contains(currentStudent.name, ignoreCase = true)
                
                val background = if (isSelf) Color(0xFFEDE7F6) else Color.White
                val border = if (isSelf) BorderStroke(2.dp, Color(0xFF9575CD)) else BorderStroke(1.dp, Color(0xFFECEFF1))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    border = border,
                    colors = CardDefaults.cardColors(containerColor = background)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "${index + 1}",
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp,
                                color = if (index < 3) Color(0xFFFF9800) else Color.Gray,
                                modifier = Modifier.width(20.dp)
                            )
                            
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(designSkin.colorHex)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = designSkin.avatarEmoji, fontSize = 20.sp)
                            }

                            Column {
                                Text(
                                    text = entry.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = if (isSelf) Color(0xFF4A148C) else Color(0xFF263238)
                                )
                                Text(
                                    text = "Skin: ${designSkin.name}",
                                    fontSize = 10.sp,
                                    color = Color.Gray
                                )
                            }
                        }

                        Text(
                            text = "${entry.score} PTS",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF3F51B5)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WardrobeShopScreen(
    profile: StudentProfile,
    viewModel: LearningViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = "👕", fontSize = 28.sp)
            Text(
                text = "CHARACTER WARDROBE SHOP",
                fontWeight = FontWeight.Black,
                fontSize = 15.sp,
                color = Color(0xFF4A148C),
                letterSpacing = 0.5.sp
            )
        }

        Text(
            text = "Unlock colorful custom runner skins with coins representing your study points!",
            fontSize = 11.sp,
            color = Color(0xFF546E7A)
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(CurriculumData.skins) { skin ->
                val isUnlocked = profile.isSkinUnlocked(skin.id)
                val isEquipped = profile.selectedSkin == skin.id

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(if (isEquipped) 3.dp else 1.dp, if (isEquipped) Color(0xFF9C27B0) else Color(0xFFEF9A9A))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(Color(skin.colorHex)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = skin.avatarEmoji, fontSize = 28.sp)
                            }

                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = skin.name,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = skin.abilityDescription,
                                    fontSize = 10.sp,
                                    color = Color.Gray
                                )
                            }
                        }

                        Box(modifier = Modifier.width(105.dp)) {
                            when {
                                isEquipped -> {
                                    Text(
                                        text = "EQUIPPED",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 12.sp,
                                        color = Color(0xFF9C27B0),
                                        modifier = Modifier
                                            .align(Alignment.Center)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color(0xFFE1BEE7))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                                isUnlocked -> {
                                    Button(
                                        onClick = { viewModel.equipSkin(skin.id) },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBA68C8)),
                                        shape = RoundedCornerShape(12.dp),
                                        contentPadding = PaddingValues(0.dp),
                                        modifier = Modifier.fillMaxWidth().height(36.dp)
                                    ) {
                                        Text(text = "EQUIP", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                }
                                else -> {
                                    Button(
                                        onClick = { viewModel.buySkin(skin) },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800)),
                                        shape = RoundedCornerShape(12.dp),
                                        contentPadding = PaddingValues(0.dp),
                                        modifier = Modifier.fillMaxWidth().height(36.dp)
                                    ) {
                                        Text(text = "🪙 ${skin.cost}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ParentSupervisorLogsScreen(
    logs: List<ActivityLog>,
    viewModel: LearningViewModel
) {
    val formatter = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = "🖥️", fontSize = 28.sp)
                Column {
                    Text(
                        text = "PARENT MONITOR PORTAL",
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp,
                        color = Color(0xFF263238)
                    )
                    Text(
                        text = "Live Student Monitoring Console",
                        fontSize = 10.sp,
                        color = Color(0xFF78909C)
                    )
                }
            }

            Button(
                onClick = { viewModel.clearLogs() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF5350)),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 10.dp),
                modifier = Modifier.height(32.dp).testTag("clear_logs_btn")
            ) {
                Text(text = "Clear Logs", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFF1E1E1E))
                .border(2.dp, Color(0xFF37474F), RoundedCornerShape(24.dp))
                .padding(14.dp)
        ) {
            if (logs.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "No recorded learner events yet. Start playing chapters!",
                        color = Color(0xFF81C784),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    item {
                        Text(
                            text = "== ADMIN LIVE ACTIVITY MONITOR RECORDING ==",
                            color = Color(0xFF81C784).copy(alpha = 0.5f),
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    }

                    items(logs) { log ->
                        val timeStr = formatter.format(Date(log.timestamp))
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp), modifier = Modifier.fillMaxWidth()) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = "[$timeStr]",
                                    color = Color(0xFF4CAF50),
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = log.action,
                                    color = Color(0xFF64B5F6),
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Black
                                )
                            }
                            Text(
                                text = "↳ ${log.details}",
                                color = Color(0xFFE0E0E0),
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.padding(start = 14.dp)
                            )
                            if (log.pointsEarned != 0 || log.coinsEarned != 0) {
                                Text(
                                    text = "  XP Earned: +${log.pointsEarned} • Coins: +${log.coinsEarned}",
                                    color = Color(0xFFFFD54F),
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier.padding(start = 14.dp)
                                )
                            }
                            Divider(color = Color.White.copy(alpha = 0.08f), thickness = 1.dp, modifier = Modifier.padding(top = 4.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ArtisticBottomNavigationBar(
    currentScreen: AppScreen,
    onNavigate: (AppScreen) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val homeSelected = currentScreen == AppScreen.Home || currentScreen == AppScreen.GamesList || currentScreen == AppScreen.QuizGame || currentScreen == AppScreen.MatchingGame || currentScreen == AppScreen.MotionGame
            BottomTabItem(
                icon = "🏠",
                isSelected = homeSelected,
                onClick = { onNavigate(AppScreen.Home) },
                tag = "nav_home"
            )

            BottomTabItem(
                icon = "🏆",
                isSelected = currentScreen == AppScreen.Leaderboard,
                onClick = { onNavigate(AppScreen.Leaderboard) },
                tag = "nav_leaderboard"
            )

            BottomTabItem(
                icon = "👕",
                badge = true,
                isSelected = currentScreen == AppScreen.SkinsShop,
                onClick = { onNavigate(AppScreen.SkinsShop) },
                tag = "nav_shop"
            )

            BottomTabItem(
                icon = "⚙️",
                isSelected = currentScreen == AppScreen.ParentConsole,
                onClick = { onNavigate(AppScreen.ParentConsole) },
                tag = "nav_parent"
            )
        }
    }
}

@Composable
fun BottomTabItem(
    icon: String,
    badge: Boolean = false,
    isSelected: Boolean,
    onClick: () -> Unit,
    tag: String
) {
    Box(
        modifier = Modifier
            .size(54.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) Color(0xFFF3E7FF) else Color.Transparent)
            .clickable { onClick() }
            .testTag(tag),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .width(14.dp)
                        .height(3.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF673AB7))
                )
                Spacer(modifier = Modifier.height(2.dp))
            }
            Box {
                Text(
                    text = icon,
                    fontSize = 25.sp,
                    modifier = Modifier.align(Alignment.Center)
                )
                if (badge) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(Color.Red)
                    )
                }
            }
        }
    }
}

@Composable
fun InteractiveGameModeBlock(
    title: String,
    category: String,
    details: String,
    icon: String,
    backgroundColor: Long,
    accentColor: Long,
    tag: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(Color(backgroundColor))
            .clickable { onClick() }
            .padding(16.dp)
            .testTag(tag),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(accentColor)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = icon, fontSize = 24.sp)
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = category,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(accentColor)
                )
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF263238)
                )
                Text(
                    text = details,
                    fontSize = 10.sp,
                    color = Color(0xFF455A64),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        Icon(Icons.Filled.KeyboardArrowRight, contentDescription = "Arrow", tint = Color(accentColor))
    }
}

@Composable
fun BadgeIndicator(title: String, isCompleted: Boolean) {
    Text(
        text = if (isCompleted) "✓ $title" else "○ $title",
        fontSize = 9.sp,
        fontWeight = FontWeight.Black,
        color = if (isCompleted) Color(0xFF1E88E5) else Color(0xFF757575),
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isCompleted) Color(0xFFE3F2FD) else Color(0xFFECEFF1))
            .padding(horizontal = 8.dp, vertical = 2.dp)
    )
}
