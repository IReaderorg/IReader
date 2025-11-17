package ireader.domain.usecases.statistics

import ireader.domain.data.repository.LibraryInsightsRepository
import ireader.domain.models.entities.UpcomingRelease
import kotlinx.coroutines.flow.Flow

/**
 * Use case to get upcoming releases with calendar integration
 */
class GetUpcomingReleasesUseCase(
    private val repository: LibraryInsightsRepository
) {
    suspend operator fun invoke(): List<UpcomingRelease> {
        return repository.getUpcomingReleases()
    }
    
    fun asFlow(): Flow<List<UpcomingRelease>> {
        return repository.getUpcomingReleasesFlow()
    }
}
