package ireader.domain.models.entities

/**
 * Data model for comparing chapter counts across sources
 */
data class SourceComparison(
    val bookId: Long,
    val currentSourceId: Long,
    val betterSourceId: Long?,
    val chapterDifference: Int,
    val cachedAt: Long,
    val dismissedUntil: Long? = null
)
