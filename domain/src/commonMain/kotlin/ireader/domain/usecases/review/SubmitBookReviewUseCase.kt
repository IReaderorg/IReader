package ireader.domain.usecases.review

import ireader.domain.data.repository.ReviewRepository
import ireader.domain.models.remote.BookReview

class SubmitBookReviewUseCase(
    private val reviewRepository: ReviewRepository
) {
    suspend operator fun invoke(bookTitle: String, rating: Int, reviewText: String): Result<BookReview> {
        require(rating in 1..5) { "Rating must be between 1 and 5" }
        require(reviewText.isNotBlank()) { "Review text cannot be empty" }
        
        return reviewRepository.submitBookReview(bookTitle, rating, reviewText)
    }
}
