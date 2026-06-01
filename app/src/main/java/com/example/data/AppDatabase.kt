package com.example.data

import android.content.Context
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

@Dao
interface AppDao {
    // Student Profile
    @Query("SELECT * FROM student_profile WHERE id = 1")
    fun getStudentProfile(): Flow<StudentProfile?>

    @Query("SELECT * FROM student_profile WHERE id = 1")
    suspend fun getStudentProfileDirect(): StudentProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveStudentProfile(profile: StudentProfile)

    // Activity Logs
    @Query("SELECT * FROM activity_logs ORDER BY timestamp DESC")
    fun getAllActivityLogs(): Flow<List<ActivityLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: ActivityLog)

    @Query("DELETE FROM activity_logs")
    suspend fun clearAllLogs()

    // Leaderboard
    @Query("SELECT * FROM leaderboard ORDER BY score DESC")
    fun getLeaderboard(): Flow<List<LeaderboardEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLeaderboardEntry(entry: LeaderboardEntry)

    @Query("UPDATE leaderboard SET score = :score WHERE isRealPlayer = 1")
    suspend fun updateRealPlayerScore(score: Int)

    // Chapter Progress
    @Query("SELECT * FROM chapter_progress")
    fun getAllChapterProgress(): Flow<List<ChapterProgress>>

    @Query("SELECT * FROM chapter_progress WHERE progressId = :progressId")
    suspend fun getChapterProgressDirect(progressId: String): ChapterProgress?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveChapterProgress(progress: ChapterProgress)
}

@Database(
    entities = [
        StudentProfile::class,
        ActivityLog::class,
        LeaderboardEntry::class,
        ChapterProgress::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "educational_game_db"
                )
                .addCallback(DatabasePrepopulationCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabasePrepopulationCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    val dao = database.appDao()
                    
                    // 1. Initial Profile
                    dao.saveStudentProfile(
                        StudentProfile(
                            id = 1,
                            name = "Super Kid Scholar",
                            selectedSkin = "classic_hero",
                            coins = 50, // Let's give them a head start!
                            totalScore = 40,
                            currentLevel = 1,
                            unlockedSkins = "classic_hero",
                            gameDifficulty = "Easy"
                        )
                    )

                    // 2. Simulated classmates for Leaderboard competition
                    dao.insertLeaderboardEntry(LeaderboardEntry(name = "Aanya Sharma", score = 380, avatarSkin = "fire_knight"))
                    dao.insertLeaderboardEntry(LeaderboardEntry(name = "Vivaan Roy", score = 210, avatarSkin = "cosmic_ranger"))
                    dao.insertLeaderboardEntry(LeaderboardEntry(name = "You (Super Kid)", score = 40, isRealPlayer = true, avatarSkin = "classic_hero"))
                    dao.insertLeaderboardEntry(LeaderboardEntry(name = "Aarav Mishra", score = 520, avatarSkin = "rainbow_unicorn"))
                    dao.insertLeaderboardEntry(LeaderboardEntry(name = "Saira Verma", score = 160, avatarSkin = "classic_hero"))
                    dao.insertLeaderboardEntry(LeaderboardEntry(name = "Rishi Sen", score = 90, avatarSkin = "cosmic_ranger"))

                    // 3. Demo Activity Logs for immediate Parent View
                    dao.insertLog(ActivityLog(
                        action = "Welcome onboard",
                        details = "Classwise Learning Games platform initialized",
                        pointsEarned = 10,
                        coinsEarned = 10,
                        timestamp = System.currentTimeMillis() - 7200000 // 2 hrs ago
                    ))
                    dao.insertLog(ActivityLog(
                        action = "Installed Application",
                        details = "Created primary student profile 'Super Kid Scholar'",
                        pointsEarned = 30,
                        coinsEarned = 40,
                        timestamp = System.currentTimeMillis() - 3600000 // 1 hr ago
                    ))
                }
            }
        }
    }
}
