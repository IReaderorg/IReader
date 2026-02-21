package ireader.domain.models.sync

import kotlinx.serialization.Serializable

/**
 * Container for all data to be synchronized between devices.
 *
 * @property books List of books to sync
 * @property readingProgress List of reading progress records to sync
 * @property bookmarks List of bookmarks to sync
 * @property metadata Metadata about this sync operation
 */
@Serializable
data class SyncData(
    val books: List<BookSyncData>,
    val readingProgress: List<ReadingProgressData>,
    val bookmarks: List<BookmarkData>,
    val metadata: SyncMetadata
)
