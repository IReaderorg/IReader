package ireader.domain.usecases.review

import ireader.domain.data.repository.ReviewRepository
import ireader.domain.models.remote.BookReview

class GetBookReviewsUseCase(
    private val reviewRepository: ReviewRepository
) {
    suspend operator fun invoke(bookTitle: String): Result<List<BookReview>> {
        return reviewRepository.getBookReviews(bookTitle)
    }
}
