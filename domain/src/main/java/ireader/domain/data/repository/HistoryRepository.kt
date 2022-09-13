package ireader.domain.data.repository

import androidx.paging.PagingSource
import kotlinx.coroutines.flow.Flow
import ireader.common.models.entities.History
import ireader.common.models.entities.HistoryWithRelations
import kotlinx.datetime.LocalDate

interface HistoryRepository {
    suspend fun findHistory(id: Long): History?

    suspend fun findHistoryByBookId(bookId: Long): History?
    suspend fun findHistoriesByBookId(bookId: Long): List<History>
    fun subscribeHistoryByBookId(bookId: Long): Flow<History?>

    fun findHistoriesPaging(query: String): PagingSource<Long, HistoryWithRelations>

    suspend fun findHistories(): List<History>

    suspend fun insertHistory(history: History)
    suspend fun insertHistories(histories: List<History>)

    suspend fun deleteHistories(histories: List<History>)
    suspend fun deleteHistory(chapterId: Long)
    suspend fun deleteHistoryByBookId(bookId: Long)
    suspend fun deleteAllHistories()
}
