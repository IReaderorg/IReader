package org.ireader.domain.repository

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import org.ireader.domain.feature_services.io.HistoryWithRelations
import org.ireader.domain.models.entities.History

interface HistoryRepository {
    suspend fun findHistory(id: Long): History


    fun findHistoriesPaging(): Flow<PagingData<HistoryWithRelations>>


    suspend fun findHistories(): List<History>

    suspend fun insertHistory(history: History): Long
    suspend fun insertHistories(histories: List<History>): List<Long>

    suspend fun deleteAllHistories(histories: List<History>)
    suspend fun deleteHistory(history: History)
}