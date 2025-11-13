package ireader.domain.usecases.review

import ireader.domain.data.repository.ReviewRepository
import ireader.domain.models.remote.ChapterReview

class SubmitChapterReviewUseCase(
    private val reviewRepository: ReviewRepository
) {
    suspend operator fun invoke(
        bookTitle: String, 
        chapterName: String, 
        rating: Int, 
        reviewText: String
    ): Result<ChapterReview> {
        require(rating in 1..5) { "Rating must be between 1 and 5" }
        require(reviewText.isNotBlank()) { "Review text cannot be empty" }
        
        return reviewRepository.submitChapterReview(bookTitle, chapterName, rating, reviewText)
    }
}
