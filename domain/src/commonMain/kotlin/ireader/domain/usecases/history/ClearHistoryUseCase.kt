package ireader.domain.usecases.history

import ireader.domain.data.repository.HistoryRepository

/**
 * Use case for clearing all history
 */
class ClearHistoryUseCase(
    private val historyRepository: HistoryRepository
) {
    /**
     * Clear all reading history
     */
    suspend operator fun invoke() {
        historyRepository.deleteAllHistories()
    }
    
    /**
     * Clear history older than specified timestamp
     */
    suspend fun clearOlderThan(timestamp: Long) {
        val allHistory = historyRepository.findHistories()
        val oldHistory = allHistory.filter { (it.readAt ?: 0L) < timestamp }
        oldHistory.forEach { history ->
            historyRepository.deleteHistory(history.id)
        }
    }
}
