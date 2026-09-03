package ireader.domain.repositories

import ireader.domain.models.sync.BookSyncData
import ireader.domain.models.sync.ChapterSyncData
import ireader.domain.models.sync.HistorySyncData

/**
 * Domain repository interface for local database sync operations.
 */
interface SyncLocalRepository {
    /**
     * Get all books for synchronization.
     */
    suspend fun getBooks(): List<BookSyncData>

    /**
     * Get all chapters for synchronization.
     *
     * @param includeDownloadedContent Whether to serialize and include full chapter page content
     */
    suspend fun getChapters(includeDownloadedContent: Boolean = false): List<ChapterSyncData>

    /**
     * Get all history records for synchronization.
     */
    suspend fun getHistory(): List<HistorySyncData>

    /**
     * Apply synced books to local database.
     */
    suspend fun applyBooks(books: List<BookSyncData>)

    /**
     * Apply synced chapters to local database.
     */
    suspend fun applyChapters(chapters: List<ChapterSyncData>)

    /**
     * Apply synced history to local database.
     */
    suspend fun applyHistory(history: List<HistorySyncData>)

    /**
     * Delete books by global IDs (sourceId + key) from local database.
     */
    suspend fun deleteBooksByGlobalIds(globalIds: List<String>)
}
