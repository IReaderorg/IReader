package org.ireader.domain.use_cases.history

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import org.ireader.domain.feature_services.io.HistoryWithRelations
import org.ireader.domain.models.entities.History
import org.ireader.domain.repository.HistoryRepository
import javax.inject.Inject

class HistoryUseCase @Inject constructor(private val historyRepository: HistoryRepository) :
    HistoryRepository {

    override suspend fun findHistory(id: Long): History {
        return historyRepository.findHistory(id)
    }

    override fun findHistoriesPaging(): Flow<PagingData<HistoryWithRelations>> {
        return historyRepository.findHistoriesPaging()
    }

    override suspend fun findHistories(): List<History> {
        return historyRepository.findHistories()
    }

    override suspend fun insertHistory(history: History): Long {
        return historyRepository.insertHistory(history)

    }

    override suspend fun insertHistories(histories: List<History>): List<Long> {
        return historyRepository.insertHistories(histories)
    }


    override suspend fun deleteAllHistories(histories: List<History>) {
        return historyRepository.deleteAllHistories(histories)

    }

    override suspend fun deleteHistory(history: History) {
        return historyRepository.deleteHistory(history)

    }
}