package ireader.domain.models.sync

/**
 * Represents a book that can be synced between devices.
 */
data class SyncableBook(
    val id: Long,
    val title: String,
    val author: String,
    val lastModified: Long,
    val coverUrl: String?,
    val chapters: List<SyncableChapter>
)

data class SyncableChapter(
    val id: Long,
    val bookId: Long,
    val title: String,
    val content: String,
    val index: Int
)
