package ireader.domain.usecases.review

import ireader.domain.data.repository.ReviewRepository
import ireader.domain.models.remote.ChapterReview

class GetChapterReviewsUseCase(
    private val reviewRepository: ReviewRepository
) {
    suspend operator fun invoke(bookTitle: String): Result<List<ChapterReview>> {
        return reviewRepository.getChapterReviews(bookTitle)
    }
}
