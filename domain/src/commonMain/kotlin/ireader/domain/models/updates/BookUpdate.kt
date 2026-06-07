package ireader.domain.models.updates

/**
 * Data class for partial book updates following Mihon's pattern.
 * Allows updating specific fields without affecting others.
 */
data class BookUpdate(
    val id: Long,
    val sourceId: Long? = null,
    val title: String? = null,
    val key: String? = null,
    val author: String? = null,
    val description: String? = null,
    val genres: List<String>? = null,
    val status: Long? = null,
    val cover: String? = null,
    val customCover: String? = null,
    val favorite: Boolean? = null,
    val lastUpdate: Long? = null,
    val initialized: Boolean? = null,
    val dateAdded: Long? = null,
    val viewer: Long? = null,
    val flags: Long? = null,
    val isPinned: Boolean? = null,
    val pinnedOrder: Int? = null,
    val isArchived: Boolean? = null,
)