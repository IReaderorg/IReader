package ireader.domain.data.repository

import ireader.domain.models.entities.ReadingStatistics
import kotlinx.coroutines.flow.Flow

interface ReadingStatisticsRepository {
    fun getStatisticsFlow(): Flow<ReadingStatistics>
    suspend fun getStatistics(): ReadingStatistics
    suspend fun incrementChaptersRead()
    suspend fun addReadingTime(minutes: Long)
    suspend fun updateStreak(streak: Int, lastReadDate: Long)
    suspend fun addWordsRead(words: Int)
    suspend fun getBooksCompleted(): Int
    suspend fun getCurrentlyReading(): Int
}
