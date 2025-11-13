package ireader.domain.models.remote

import kotlinx.serialization.Serializable

/**
 * Domain model for chapter reviews
 * Reviews are based on book title + chapter name (normalized) - shared across all sources
 */
@Serializable
data class ChapterReview(
    val id: String? = null,
    val userId: String,
    val bookTitle: String,
    val chapterName: String,
    val rating: Int, // 1-5 stars
    val reviewText: String,
    val createdAt: Long,
    val updatedAt: Long
)
