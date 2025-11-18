package ireader.domain.data.repository

import ireader.domain.models.entities.ReadingStatisticsType1
import kotlinx.coroutines.flow.Flow

interface ReadingStatisticsRepository {
    fun getStatisticsFlow(): Flow<ReadingStatisticsType1>
    suspend fun getStatistics(): ReadingStatisticsType1
    suspend fun getLastReadDate(): Long?
    suspend fun getCurrentStreak(): Int
    suspend fun incrementChaptersRead()
    suspend fun addReadingTime(minutes: Long)
    suspend fun updateStreak(streak: Int, lastReadDate: Long)
    suspend fun addWordsRead(words: Int)
    suspend fun getBooksCompleted(): Int
    suspend fun getCurrentlyReading(): Int
    suspend fun incrementBooksCompleted()
}
