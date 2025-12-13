package ireader.domain.data.repository

import ireader.domain.models.entities.ReadingStatisticsType1
import kotlinx.coroutines.flow.Flow

interface ReadingStatisticsRepository {
    fun getStatisticsFlow(): Flow<ReadingStatisticsType1>
    suspend fun getStatistics(): ReadingStatisticsType1
    suspend fun getLastReadDate(): Long?
    suspend fun getCurrentStreak(): Int
    suspend fun getLongestStreak(): Int
    suspend fun incrementChaptersRead()
    suspend fun addReadingTime(minutes: Long)
    suspend fun updateStreak(streak: Int, lastReadDate: Long)
    suspend fun updateStreakWithLongest(streak: Int, lastReadDate: Long)
    suspend fun addWordsRead(words: Int)
    suspend fun getBooksCompleted(): Int
    suspend fun getCurrentlyReading(): Int
    suspend fun incrementBooksCompleted()
    
    // Reading Buddy methods - unified with statistics
    suspend fun getBuddyLevel(): Int
    suspend fun getBuddyExperience(): Int
    suspend fun updateBuddyProgress(level: Int, experience: Int)
    suspend fun getUnlockedAchievements(): String
    suspend fun updateUnlockedAchievements(achievements: String)
    suspend fun getLastInteractionTime(): Long
    suspend fun updateLastInteractionTime(time: Long)
}
