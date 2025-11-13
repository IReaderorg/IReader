package ireader.domain.models.remote

/**
 * Domain model representing reading progress for a book
 */
data class ReadingProgress(
    val id: String? = null,
    val userId: String,
    val bookId: String,
    val lastChapterSlug: String,
    val lastScrollPosition: Float,
    val updatedAt: Long
)
