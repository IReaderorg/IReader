package org.ireader.domain.repository

import kotlinx.coroutines.flow.Flow
import org.ireader.domain.feature_service.io.HistoryWithRelations
import org.ireader.domain.models.entities.History

interface HistoryRepository {
    suspend fun findHistory(id: Long): History?

    suspend fun findHistoryByBookId(bookId: Long): History?

    fun findHistoriesPaging(query: String): Flow<Map<String, List<HistoryWithRelations>>>


    suspend fun findHistories(): List<History>

    suspend fun insertHistory(history: History): Long
    suspend fun insertHistories(histories: List<History>): List<Long>

    suspend fun deleteHistories(histories: List<History>)
    suspend fun deleteHistory(chapterId: Long)
    suspend fun deleteAllHistories()
}