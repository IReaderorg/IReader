package org.ireader.domain.use_cases.history

import kotlinx.coroutines.flow.Flow
import org.ireader.common_models.entities.History
import org.ireader.common_models.entities.HistoryWithRelations
import javax.inject.Inject

class HistoryUseCase @Inject constructor(private val historyRepository: org.ireader.common_data.repository.HistoryRepository) {

     suspend fun findHistory(chapterId: Long): History? {
        return historyRepository.findHistory(chapterId)
    }

     suspend fun findHistoryByBookId(bookId: Long): History? {
        return historyRepository.findHistoryByBookId(bookId)
    }

     fun findHistoriesPaging(query: String): Flow<Map<String, List<HistoryWithRelations>>> {
        return historyRepository.findHistoriesPaging(query)
    }

     suspend fun findHistories(): List<History> {
        return historyRepository.findHistories()
    }

     suspend fun insertHistory(history: History): Long {
        return historyRepository.insertHistory(history)

    }

     suspend fun insertHistories(histories: List<History>): List<Long> {
        return historyRepository.insertHistories(histories)
    }


     suspend fun deleteHistories(histories: List<History>) {
        return historyRepository.deleteHistories(histories)

    }

     suspend fun deleteHistory(id: Long) {
        return historyRepository.deleteHistory(id)

    }

     suspend fun deleteAllHistories() {
        return historyRepository.deleteAllHistories()
    }
}