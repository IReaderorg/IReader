package ireader.domain.models.remote

import kotlinx.serialization.Serializable

/**
 * Domain model for book reviews
 * Reviews are based on book title (normalized) - shared across all sources
 */
@Serializable
data class BookReview(
    val id: String? = null,
    val userId: String,
    val bookTitle: String,
    val rating: Int, // 1-5 stars
    val reviewText: String,
    val createdAt: Long,
    val updatedAt: Long
)
