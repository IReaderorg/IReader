package ireader.domain.usecases.history

import ireader.domain.data.repository.HistoryRepository

/**
 * Use case for deleting history entries
 */
class DeleteHistoryUseCase(
    private val historyRepository: HistoryRepository
) {
    /**
     * Delete history by ID
     */
    suspend operator fun invoke(historyId: Long) {
        historyRepository.deleteHistory(historyId)
    }
    
    /**
     * Delete history for a specific book
     */
    suspend fun deleteByBookId(bookId: Long) {
        historyRepository.deleteHistoryByBookId(bookId)
    }
    
    /**
     * Delete history for a specific chapter
     */
    suspend fun deleteByChapterId(chapterId: Long) {
        historyRepository.deleteHistory(chapterId)
    }
}
