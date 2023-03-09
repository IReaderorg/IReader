package ireader.domain.usecases.history

import ireader.domain.data.repository.HistoryRepository
import ireader.domain.models.entities.History
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow



class HistoryUseCase(private val historyRepository: HistoryRepository) {

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
