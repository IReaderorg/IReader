package ireader.domain.usecases.statistics

import ireader.domain.data.repository.LibraryInsightsRepository
import ireader.domain.models.entities.ReadingAnalytics
import kotlinx.coroutines.flow.Flow

/**
 * Use case to get reading analytics with time tracking
 */
class GetReadingAnalyticsUseCase(
    private val repository: LibraryInsightsRepository
) {
    suspend operator fun invoke(): ReadingAnalytics {
        return repository.getReadingAnalytics()
    }
    
    fun asFlow(): Flow<ReadingAnalytics> {
        return repository.getReadingAnalyticsFlow()
    }
}
