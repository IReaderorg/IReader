package org.ireader.domain.use_cases.history

import kotlinx.coroutines.flow.Flow
import org.ireader.domain.feature_service.io.HistoryWithRelations
import org.ireader.domain.models.entities.History
import org.ireader.domain.repository.HistoryRepository
import javax.inject.Inject

class HistoryUseCase @Inject constructor(private val historyRepository: HistoryRepository) :
    HistoryRepository {

    override suspend fun findHistory(chapterId: Long): History? {
        return historyRepository.findHistory(chapterId)
    }

    override suspend fun findHistoryByBookId(bookId: Long): History? {
        return historyRepository.findHistoryByBookId(bookId)
    }

    override fun findHistoriesPaging(query: String): Flow<Map<String, List<HistoryWithRelations>>> {
        return historyRepository.findHistoriesPaging(query)
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


    override suspend fun deleteHistories(histories: List<History>) {
        return historyRepository.deleteHistories(histories)

    }

    override suspend fun deleteHistory(id: Long) {
        return historyRepository.deleteHistory(id)

    }

    override suspend fun deleteAllHistories() {
        return historyRepository.deleteAllHistories()
    }
}