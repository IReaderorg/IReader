package ireader.domain.usecases.history

import ireader.domain.data.repository.HistoryRepository
import ireader.domain.models.entities.History
import ireader.domain.models.entities.HistoryWithRelations
import kotlinx.coroutines.flow.Flow

/**
 * Use case for retrieving reading history
 */
class GetHistoryUseCase(
    private val historyRepository: HistoryRepository
) {
    /**
     * Get all history as a one-time operation
     */
    suspend operator fun invoke(): List<History> {
        return historyRepository.findHistories()
    }
    
    /**
     * Subscribe to history changes with relations
     */
    fun subscribe(query: String = ""): Flow<List<HistoryWithRelations>> {
        return historyRepository.findHistoriesByFlow(query)
    }
    
    /**
     * Get history for a specific book
     */
    suspend fun getByBookId(bookId: Long): List<History> {
        return historyRepository.findHistoriesByBookId(bookId)
    }
}
