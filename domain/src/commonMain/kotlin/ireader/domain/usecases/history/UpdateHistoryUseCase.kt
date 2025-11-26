package ireader.domain.usecases.history

import ireader.domain.data.repository.HistoryRepository
import ireader.domain.models.entities.History

/**
 * Use case for updating reading history
 */
class UpdateHistoryUseCase(
    private val historyRepository: HistoryRepository
) {
    /**
     * Update or create a history entry
     */
    suspend operator fun invoke(history: History) {
        historyRepository.insertHistory(history)
    }
    
    /**
     * Record reading progress
     */
    suspend fun recordProgress(
        chapterId: Long,
        readAt: Long = System.currentTimeMillis()
    ) {
        val history = History(
            id = 0,
            chapterId = chapterId,
            readAt = readAt,
            readDuration = 0
        )
        invoke(history)
    }
}
