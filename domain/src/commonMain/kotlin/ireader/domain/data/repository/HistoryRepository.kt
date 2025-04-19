package ireader.domain.data.repository

import ireader.domain.models.entities.HistoryWithRelations
import ireader.domain.models.entities.History
import kotlinx.coroutines.flow.Flow

interface HistoryRepository {
    suspend fun findHistory(id: Long): History?

    suspend fun findHistoryByChapterId(chapterId: Long): History?
    suspend fun findHistoryByBookId(bookId: Long): History?
    suspend fun findHistoriesByBookId(bookId: Long): List<History>
    fun subscribeHistoryByBookId(bookId: Long): Flow<History?>
    suspend fun findHistoryByChapterUrl(chapterUrl: String): History?

    suspend fun findHistories(): List<History>
    fun findHistoriesByFlow(query:String): Flow<List<HistoryWithRelations>>

    suspend fun upsert(chapterId: Long,
                       readAt: Long,
                       readDuration: Long,
                       progress: Float)
    suspend fun insertHistory(history: History)
    suspend fun insertHistories(histories: List<History>)

    suspend fun deleteHistories(histories: List<History>)
    suspend fun deleteHistory(chapterId: Long)
    suspend fun deleteHistoryByBookId(bookId: Long)
    suspend fun deleteAllHistories()
    suspend fun updateHistory(
        chapterId: Long,
        readAt: Long?,
        readDuration: Long?,
        progress: Float?
    )
    suspend fun resetHistoryById(historyId: Long)
    suspend fun resetHistoryByBookId(historyId: Long)
}
