package org.ireader.domain.use_cases.history

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.datetime.LocalDate
import org.ireader.common_models.entities.History
import org.ireader.common_models.entities.HistoryWithRelations

class HistoryUseCase(private val historyRepository: org.ireader.common_data.repository.HistoryRepository) {

    suspend fun findHistory(chapterId: Long): History? {
        return historyRepository.findHistory(chapterId)
    }

    suspend fun findHistoryByBookId(bookId: Long): History? {
        return historyRepository.findHistoryByBookId(bookId)
    }
    fun subscribeHistoryByBookId(bookId: Long?): Flow<History?> {
        if (bookId == null) return emptyFlow()
        return historyRepository.subscribeHistoryByBookId(bookId)
    }

    fun findHistoriesPaging(query: String): Flow<Map<LocalDate, List<HistoryWithRelations>>> {
        return historyRepository.findHistoriesPaging(query)
    }

    suspend fun findHistories(): List<History> {
        return historyRepository.findHistories()
    }

    suspend fun insertHistory(history: History) {
        return historyRepository.insertHistory(history)
    }

    suspend fun insertHistories(histories: List<History>) {
        return historyRepository.insertHistories(histories)
    }

    suspend fun deleteHistories(histories: List<History>) {
        return historyRepository.deleteHistories(histories)
    }

    suspend fun deleteHistory(id: Long) {
        return historyRepository.deleteHistory(id)
    }
    suspend fun deleteHistoryByBookId(bookId: Long) {
        return historyRepository.deleteHistoryByBookId(bookId)
    }

    suspend fun deleteAllHistories() {
        return historyRepository.deleteAllHistories()
    }
}
