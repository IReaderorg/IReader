package ireader.domain.models.sync

import kotlinx.serialization.Serializable

/**
 * Represents reading progress data for synchronization.
 *
 * @property bookId ID of the book this progress belongs to
 * @property chapterId ID of the current chapter being read
 * @property chapterIndex Index of the current chapter (0-based)
 * @property offset Character/byte offset within the chapter
 * @property progress Reading progress as a percentage (0.0 to 1.0)
 * @property lastReadAt Timestamp when this book was last read
 *
 * @throws IllegalArgumentException if validation fails
 */
@Serializable
data class ReadingProgressData(
    val bookId: Long,
    val chapterId: Long,
    val chapterIndex: Int,
    val offset: Int,
    val progress: Float,
    val lastReadAt: Long
) {
    init {
        require(bookId >= 0) { "Book ID cannot be negative, got: $bookId" }
        require(chapterId >= 0) { "Chapter ID cannot be negative, got: $chapterId" }
        require(chapterIndex >= 0) { "Chapter index cannot be negative, got: $chapterIndex" }
        require(offset >= 0) { "Offset cannot be negative, got: $offset" }
        require(progress in 0.0f..1.0f) { "Progress must be between 0.0 and 1.0, got: $progress" }
        require(lastReadAt >= 0) { "Last read timestamp cannot be negative, got: $lastReadAt" }
    }
}
