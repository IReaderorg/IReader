package ireader.domain.models.sync

import kotlinx.serialization.Serializable

/**
 * Container for all data to be synchronized between devices.
 *
 * @property books List of books to sync
 * @property chapters List of chapters to sync
 * @property history List of history records to sync
 * @property metadata Metadata about this sync operation
 */
@Serializable
data class SyncData(
    val books: List<BookSyncData>,
    val chapters: List<ChapterSyncData>,
    val history: List<HistorySyncData>,
    val metadata: SyncMetadata
)
