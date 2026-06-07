package ireader.domain.models.sync

import kotlinx.serialization.Serializable

/**
 * Represents book data for synchronization.
 *
 * @property globalId Globally unique identifier (sourceId + key)
 * @property title Title of the book
 * @property author Author of the book
 * @property coverUrl URL to the book's cover image (nullable)
 * @property sourceId Identifier of the source where the book came from
 * @property key Unique key/URL for the book on the source
 * @property favorite Whether the book is marked as favorite
 * @property addedAt Timestamp when the book was added to the library
 * @property updatedAt Timestamp when the book was last updated
 * @property description Book description
 * @property genres List of genres
 * @property status Book status (ongoing, completed, etc.)
 *
 * @throws IllegalArgumentException if validation fails
 */
@Serializable
data class BookSyncData(
    val globalId: String, // sourceId + "|" + key
    val title: String,
    val author: String,
    val coverUrl: String?,
    val sourceId: String,
    val key: String,
    val favorite: Boolean,
    val addedAt: Long,
    val updatedAt: Long,
    val description: String,
    val genres: List<String>,
    val status: Long
) {
    init {
        require(globalId.isNotBlank()) { "Global ID cannot be empty or blank" }
        require(title.isNotBlank()) { "Title cannot be empty or blank" }
        require(sourceId.isNotBlank()) { "Source ID cannot be empty or blank" }
        require(key.isNotBlank()) { "Key cannot be empty or blank" }
        require(addedAt >= 0) { "Added timestamp cannot be negative, got: $addedAt" }
        require(updatedAt >= 0) { "Updated timestamp cannot be negative, got: $updatedAt" }
    }
}
