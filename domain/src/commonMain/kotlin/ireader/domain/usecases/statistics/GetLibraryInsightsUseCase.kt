package ireader.domain.usecases.statistics

import ireader.domain.data.repository.LibraryInsightsRepository
import ireader.domain.models.entities.LibraryInsights
import kotlinx.coroutines.flow.Flow

/**
 * Use case to get comprehensive library insights
 */
class GetLibraryInsightsUseCase(
    private val repository: LibraryInsightsRepository
) {
    suspend operator fun invoke(): LibraryInsights {
        return repository.getLibraryInsights()
    }
    
    fun asFlow(): Flow<LibraryInsights> {
        return repository.getLibraryInsightsFlow()
    }
}
