package ireader.domain.models.sync

import kotlinx.serialization.Serializable

/**
 * Represents book data for synchronization.
 *
 * @property bookId Unique identifier for the book
 * @property title Title of the book
 * @property author Author of the book
 * @property coverUrl URL to the book's cover image (nullable)
 * @property sourceId Identifier of the source where the book came from
 * @property sourceUrl URL to the book on the source website
 * @property addedAt Timestamp when the book was added to the library
 * @property updatedAt Timestamp when the book was last updated
 * @property fileHash SHA-256 hash of the book file (nullable if no file exists)
 *
 * @throws IllegalArgumentException if validation fails
 */
@Serializable
data class BookSyncData(
    val bookId: Long,
    val title: String,
    val author: String,
    val coverUrl: String?,
    val sourceId: String,
    val sourceUrl: String,
    val addedAt: Long,
    val updatedAt: Long,
    val fileHash: String?
) {
    init {
        require(bookId >= 0) { "Book ID cannot be negative, got: $bookId" }
        require(title.isNotBlank()) { "Title cannot be empty or blank" }
        require(addedAt >= 0) { "Added timestamp cannot be negative, got: $addedAt" }
        require(updatedAt >= 0) { "Updated timestamp cannot be negative, got: $updatedAt" }
    }
}
