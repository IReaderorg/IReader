package ireader.domain.models.sync

import kotlinx.serialization.Serializable

/**
 * Represents a book that can be synced between devices.
 *
 * @property id Unique identifier for the book
 * @property title Title of the book
 * @property author Author of the book
 * @property lastModified Timestamp when the book was last modified
 * @property coverUrl URL to the book's cover image (nullable)
 * @property chapters List of chapters in the book
 *
 * @throws IllegalArgumentException if validation fails
 */
@Serializable
data class SyncableBook(
    val id: Long,
    val title: String,
    val author: String,
    val lastModified: Long,
    val coverUrl: String?,
    val chapters: List<SyncableChapter>
) {
    init {
        require(id >= 0) { "Book ID cannot be negative, got: $id" }
        require(title.isNotBlank()) { "Title cannot be empty or blank" }
        require(lastModified >= 0) { "Last modified timestamp cannot be negative, got: $lastModified" }
    }
}

/**
 * Represents a chapter within a syncable book.
 *
 * @property id Unique identifier for the chapter
 * @property bookId ID of the book this chapter belongs to
 * @property title Title of the chapter
 * @property content Content of the chapter
 * @property index Index/order of the chapter in the book (0-based)
 *
 * @throws IllegalArgumentException if validation fails
 */
@Serializable
data class SyncableChapter(
    val id: Long,
    val bookId: Long,
    val title: String,
    val content: String,
    val index: Int
) {
    init {
        require(id >= 0) { "Chapter ID cannot be negative, got: $id" }
        require(bookId >= 0) { "Book ID cannot be negative, got: $bookId" }
        require(title.isNotBlank()) { "Title cannot be empty or blank" }
        require(index >= 0) { "Index cannot be negative, got: $index" }
    }
}
