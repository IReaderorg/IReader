package ireader.domain.models.sync

import kotlinx.serialization.Serializable

/**
 * Represents bookmark data for synchronization.
 *
 * @property bookmarkId Unique identifier for the bookmark
 * @property bookId ID of the book this bookmark belongs to
 * @property chapterId ID of the chapter this bookmark is in
 * @property position Character/byte position within the chapter
 * @property note Optional note attached to the bookmark
 * @property createdAt Timestamp when the bookmark was created
 *
 * @throws IllegalArgumentException if validation fails
 */
@Serializable
data class BookmarkData(
    val bookmarkId: Long,
    val bookId: Long,
    val chapterId: Long,
    val position: Int,
    val note: String?,
    val createdAt: Long
) {
    init {
        require(bookmarkId >= 0) { "Bookmark ID cannot be negative, got: $bookmarkId" }
        require(bookId >= 0) { "Book ID cannot be negative, got: $bookId" }
        require(chapterId >= 0) { "Chapter ID cannot be negative, got: $chapterId" }
        require(position >= 0) { "Position cannot be negative, got: $position" }
        require(createdAt >= 0) { "Created timestamp cannot be negative, got: $createdAt" }
    }
}
