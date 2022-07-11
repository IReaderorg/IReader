package org.ireader.common_data.repository

import kotlinx.coroutines.flow.Flow
import org.ireader.common_models.entities.History
import org.ireader.common_models.entities.HistoryWithRelations

interface HistoryRepository {
    suspend fun findHistory(id: Long): History?

    suspend fun findHistoryByBookId(bookId: Long): History?
    suspend fun findHistoriesByBookId(bookId: Long): List<History>
    fun subscribeHistoryByBookId(bookId: Long): Flow<History?>

    fun findHistoriesPaging(query: String): Flow<Map<kotlinx.datetime.LocalDate, List<HistoryWithRelations>>>

    suspend fun findHistories(): List<History>

    suspend fun insertHistory(history: History)
    suspend fun insertHistories(histories: List<History>)

    suspend fun deleteHistories(histories: List<History>)
    suspend fun deleteHistory(chapterId: Long)
    suspend fun deleteHistoryByBookId(bookId: Long)
    suspend fun deleteAllHistories()
}
