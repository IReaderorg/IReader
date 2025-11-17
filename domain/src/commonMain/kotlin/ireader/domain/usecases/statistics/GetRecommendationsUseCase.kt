package ireader.domain.usecases.statistics

import ireader.domain.data.repository.LibraryInsightsRepository
import ireader.domain.models.entities.BookRecommendation

/**
 * Use case to get book recommendations based on reading history
 */
class GetRecommendationsUseCase(
    private val repository: LibraryInsightsRepository
) {
    suspend operator fun invoke(limit: Int = 20): List<BookRecommendation> {
        return repository.getRecommendations(limit)
    }
}
