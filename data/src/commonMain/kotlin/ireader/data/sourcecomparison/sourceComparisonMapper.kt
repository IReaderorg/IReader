package ireader.data.sourcecomparison

import data.SourceComparison as SourceComparisonEntity
import ireader.domain.models.entities.SourceComparison

val sourceComparisonMapper: (
    book_id: Long,
    current_source_id: Long,
    better_source_id: Long?,
    chapter_difference: Long,
    cached_at: Long,
    dismissed_until: Long?
) -> SourceComparison = { bookId, currentSourceId, betterSourceId, chapterDifference, cachedAt, dismissedUntil ->
    SourceComparison(
        bookId = bookId,
        currentSourceId = currentSourceId,
        betterSourceId = betterSourceId,
        chapterDifference = chapterDifference.toInt(),
        cachedAt = cachedAt,
        dismissedUntil = dismissedUntil
    )
}
