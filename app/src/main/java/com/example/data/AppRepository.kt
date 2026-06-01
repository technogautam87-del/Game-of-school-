package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class AppRepository(private val appDao: AppDao) {

    val studentProfile: Flow<StudentProfile?> = appDao.getStudentProfile()
    val activityLogs: Flow<List<ActivityLog>> = appDao.getAllActivityLogs()
    val leaderboard: Flow<List<LeaderboardEntry>> = appDao.getLeaderboard()
    val allProgress: Flow<List<ChapterProgress>> = appDao.getAllChapterProgress()

    suspend fun updateProfileName(newName: String) {
        val current = appDao.getStudentProfileDirect() ?: StudentProfile()
        val updated = current.copy(name = newName)
        appDao.saveStudentProfile(updated)

        // Update real player name in leaderboard
        appDao.insertLog(ActivityLog(
            action = "Profile Renamed",
            details = "Changed student name from '${current.name}' to '$newName'"
        ))
        
        // Let's look for real player in the leaderboard list or update it
        val entries = appDao.getLeaderboard()
        val list = entries.firstOrNull() ?: emptyList()
        val realEntry = list.find { it.isRealPlayer }
        if (realEntry != null) {
            appDao.insertLeaderboardEntry(realEntry.copy(name = "You ($newName)"))
        } else {
            appDao.insertLeaderboardEntry(LeaderboardEntry(
                name = "You ($newName)",
                score = current.totalScore,
                isRealPlayer = true,
                avatarSkin = current.selectedSkin
            ))
        }
    }

    suspend fun updateDifficulty(newDifficulty: String) {
        val current = appDao.getStudentProfileDirect() ?: StudentProfile()
        val updated = current.copy(gameDifficulty = newDifficulty)
        appDao.saveStudentProfile(updated)
        appDao.insertLog(ActivityLog(
            action = "Changed Difficulty",
            details = "Set game difficulty to $newDifficulty"
        ))
    }

    suspend fun buyAndUnlockSkin(skinId: String, cost: Int): Boolean {
        val current = appDao.getStudentProfileDirect() ?: StudentProfile()
        if (current.isSkinUnlocked(skinId)) {
            val updated = current.copy(selectedSkin = skinId)
            appDao.saveStudentProfile(updated)
            // Update leaderboard skin if real player exists
            val list = appDao.getLeaderboard().firstOrNull() ?: emptyList()
            list.find { it.isRealPlayer }?.let { realEntry ->
                appDao.insertLeaderboardEntry(realEntry.copy(avatarSkin = skinId))
            }
            return true
        }

        if (current.coins >= cost) {
            val newUnlocked = "${current.unlockedSkins},$skinId"
            val updated = current.copy(
                coins = current.coins - cost,
                unlockedSkins = newUnlocked,
                selectedSkin = skinId
            )
            appDao.saveStudentProfile(updated)
            appDao.insertLog(ActivityLog(
                action = "Skin Unlocked",
                details = "Purchased and customized skin: '$skinId' for $cost coins!",
                coinsEarned = -cost
            ))

            // Update leaderboard skin
            val list = appDao.getLeaderboard().firstOrNull() ?: emptyList()
            list.find { it.isRealPlayer }?.let { realEntry ->
                appDao.insertLeaderboardEntry(realEntry.copy(avatarSkin = skinId))
            }
            return true
        }
        return false
    }

    suspend fun updateSelectedSkin(skinId: String) {
        val current = appDao.getStudentProfileDirect() ?: StudentProfile()
        if (current.isSkinUnlocked(skinId)) {
            val updated = current.copy(selectedSkin = skinId)
            appDao.saveStudentProfile(updated)
            appDao.insertLog(ActivityLog(
                action = "Changed Character Skin",
                details = "Equipped skin: '$skinId'"
            ))

            // Update leaderboard
            val list = appDao.getLeaderboard().firstOrNull() ?: emptyList()
            list.find { it.isRealPlayer }?.let { realEntry ->
                appDao.insertLeaderboardEntry(realEntry.copy(avatarSkin = skinId))
            }
        }
    }

    suspend fun saveGameProgressAndEarnRewards(
        classId: String,
        subjectId: String,
        chapterId: String,
        gameType: String, // "Quiz", "Matching", "Motion"
        score: Int,
        maxScore: Int
    ) {
        val progressId = "${classId}_${subjectId}_$chapterId"
        val currentProgress = appDao.getChapterProgressDirect(progressId) ?: ChapterProgress(
            progressId = progressId,
            classId = classId,
            subjectId = subjectId,
            chapterId = chapterId
        )

        // Decide reward amounts based on performance
        val percentage = if (maxScore > 0) (score.toFloat() / maxScore) else 1.0f
        val coinReward = (percentage * 20).toInt().coerceIn(0, 30)
        val scoreReward = (percentage * 50).toInt().coerceIn(0, 50)

        // Check if this gives higher score than previously saved score
        val updatedProgress = when (gameType) {
            "Quiz" -> {
                if (score > currentProgress.quizScore) {
                    currentProgress.copy(quizCompleted = true, quizScore = score)
                } else currentProgress.copy(quizCompleted = true)
            }
            "Matching" -> {
                if (score > currentProgress.matchingScore) {
                    currentProgress.copy(matchingCompleted = true, matchingScore = score)
                } else currentProgress.copy(matchingCompleted = true)
            }
            "Motion" -> {
                if (score > currentProgress.motionScore) {
                    currentProgress.copy(motionCompleted = true, motionScore = score)
                } else currentProgress.copy(motionCompleted = true)
            }
            else -> currentProgress
        }

        appDao.saveChapterProgress(updatedProgress)

        // Give student rewards
        val currentProfile = appDao.getStudentProfileDirect() ?: StudentProfile()
        val newScore = currentProfile.totalScore + scoreReward
        val newCoins = currentProfile.coins + coinReward
        val newLevel = 1 + (newScore / 200) // 1 level per 200 points

        val updatedProfile = currentProfile.copy(
            totalScore = newScore,
            coins = newCoins,
            currentLevel = newLevel
        )
        appDao.saveStudentProfile(updatedProfile)

        // Log the achievement
        appDao.insertLog(ActivityLog(
            action = "Completed $gameType",
            details = "Class: ${classId.uppercase()}, Subject: ${subjectId.uppercase()}, Chapter: $chapterId. Score: $score/$maxScore",
            pointsEarned = scoreReward,
            coinsEarned = coinReward
        ))

        // Update leaderboard
        val list = appDao.getLeaderboard().firstOrNull() ?: emptyList()
        val realEntry = list.find { it.isRealPlayer }
        if (realEntry != null) {
            appDao.insertLeaderboardEntry(realEntry.copy(score = newScore, avatarSkin = currentProfile.selectedSkin))
        } else {
            appDao.insertLeaderboardEntry(LeaderboardEntry(
                name = "You (${currentProfile.name})",
                score = newScore,
                isRealPlayer = true,
                avatarSkin = currentProfile.selectedSkin
            ))
        }
    }

    suspend fun clearHistory() {
        appDao.clearAllLogs()
        appDao.insertLog(ActivityLog(
            action = "Logs Cleared",
            details = "All activity logs were reset by supervisor."
        ))
    }
}
