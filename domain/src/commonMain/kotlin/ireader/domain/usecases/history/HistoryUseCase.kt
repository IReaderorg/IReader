package ireader.domain.usecases.history

import ireader.domain.data.repository.HistoryRepository
import ireader.domain.models.entities.History
import ireader.domain.models.entities.HistoryWithRelations
import ireader.domain.utils.extensions.toLocalDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDateTime


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

    fun findHistoriesByFlow(query:String = ""): Flow<Map<LocalDateTime, List<HistoryWithRelations>>> {
        return historyRepository.findHistoriesByFlow(query).map { list ->
            list.groupBy { it.readAt?.toLocalDate() ?: (0L).toLocalDate() }
        }
    }
    fun findHistoriesByFlowLongType(query:String = ""): Flow<Map<Long, List<HistoryWithRelations>>> {
        return historyRepository.findHistoriesByFlow(query).map { list ->
            list.groupBy { it.readAt ?: 0L }
        }
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
