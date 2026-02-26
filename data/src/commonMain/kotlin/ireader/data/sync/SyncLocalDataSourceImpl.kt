package ireader.data.sync

import ireader.core.log.Log
import ireader.core.source.model.Page
import ireader.core.source.model.decode
import ireader.core.source.model.encode
import ireader.data.core.DatabaseHandler
import ireader.data.sync.datasource.SyncLocalDataSource
import ireader.data.sync.datasource.SyncLogEntity
import ireader.data.sync.datasource.SyncMetadataEntity
import ireader.domain.models.sync.BookSyncData
import ireader.domain.models.sync.ChapterSyncData
import ireader.domain.models.sync.HistorySyncData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.map

/**
 * SQLDelight implementation of SyncLocalDataSource.
 * 
 * This class provides access to sync-related database operations using SQLDelight queries.
 * It wraps the DatabaseHandler to execute queries and map results to entity objects.
 * 
 * @param handler DatabaseHandler for executing queries
 */
class SyncLocalDataSourceImpl(
    private val handler: DatabaseHandler
) : SyncLocalDataSource {

    // ========== Sync Metadata Operations ==========

    override suspend fun getSyncMetadata(deviceId: String): SyncMetadataEntity? {
        return handler.await {
            sync_metadataQueries.getSyncMetadata(deviceId).executeAsOneOrNull()?.let {
                SyncMetadataEntity(
                    deviceId = it.device_id,
                    deviceName = it.device_name,
                    deviceType = it.device_type,
                    lastSyncTime = it.last_sync_time,
                    createdAt = it.created_at,
                    updatedAt = it.updated_at
                )
            }
        }
    }

    override suspend fun upsertSyncMetadata(metadata: SyncMetadataEntity) {
        handler.await {
            sync_metadataQueries.upsertSyncMetadata(
                device_id = metadata.deviceId,
                device_name = metadata.deviceName,
                device_type = metadata.deviceType,
                last_sync_time = metadata.lastSyncTime,
                created_at = metadata.createdAt,
                updated_at = metadata.updatedAt
            )
        }
    }

    override suspend fun deleteSyncMetadata(deviceId: String) {
        handler.await {
            sync_metadataQueries.deleteSyncMetadata(deviceId)
        }
    }

    // ========== Sync Log Operations ==========

    override suspend fun insertSyncLog(log: SyncLogEntity) {
        handler.await {
            sync_logQueries.insertSyncLog(
                sync_id = log.syncId,
                device_id = log.deviceId,
                status = log.status,
                items_synced = log.itemsSynced.toLong(),
                duration = log.duration,
                error_message = log.errorMessage,
                timestamp = log.timestamp
            )
        }
    }

    override suspend fun getSyncLogById(id: Long): SyncLogEntity? {
        return handler.await {
            sync_logQueries.getSyncLogById(id).executeAsOneOrNull()?.let {
                SyncLogEntity(
                    id = it.id,
                    syncId = it.sync_id,
                    deviceId = it.device_id,
                    status = it.status,
                    itemsSynced = it.items_synced.toInt(),
                    duration = it.duration,
                    errorMessage = it.error_message,
                    timestamp = it.timestamp
                )
            }
        }
    }

    override fun getSyncLogsByDevice(deviceId: String): Flow<List<SyncLogEntity>> {
        return handler.subscribeToList {
            sync_logQueries.getSyncLogsByDevice(deviceId)
        }.map { list ->
            list.map {
                SyncLogEntity(
                    id = it.id,
                    syncId = it.sync_id,
                    deviceId = it.device_id,
                    status = it.status,
                    itemsSynced = it.items_synced.toInt(),
                    duration = it.duration,
                    errorMessage = it.error_message,
                    timestamp = it.timestamp
                )
            }
        }.conflate() // Drop intermediate values if collector is slow (Task 10.1.3)
    }

    // ========== Sync Data Operations ==========

    override suspend fun getBooks(): List<BookSyncData> {
        return handler.await {
            // Get ALL books (not just favorites)
            bookQueries.findAllBooks().executeAsList().map { book ->
                val globalId = "${book.source}|${book.url}"
                BookSyncData(
                    globalId = globalId,
                    title = book.title,
                    author = book.author ?: "",
                    coverUrl = book.thumbnail_url,
                    sourceId = book.source.toString(),
                    key = book.url,
                    favorite = book.favorite,
                    addedAt = book.date_added,
                    updatedAt = book.last_update ?: book.date_added,
                    description = book.description ?: "",
                    genres = book.genre ?: emptyList(),
                    status = book.status
                )
            }
        }
    }

    override suspend fun getChapters(): List<ChapterSyncData> {
        return handler.await {
            // Get all chapters for all books
            chapterQueries.getAllChapters().executeAsList().map { chapter ->
                // Get book to construct book global ID
                val book = bookQueries.findBookById(chapter.book_id).executeAsOneOrNull()
                val bookGlobalId = if (book != null) "${book.source}|${book.url}" else ""
                val chapterGlobalId = if (book != null) "${book.source}|${chapter.url}" else ""
                
                // Encode content to JSON string using extension function
                val contentJson = try {
                    chapter.content.encode()
                } catch (e: Exception) {
                    Log.error("Failed to encode chapter content for chapter ${chapter.name} (${chapter.url})", e)
                    "[]" // Empty content if encoding fails
                }
                
                ChapterSyncData(
                    globalId = chapterGlobalId,
                    bookGlobalId = bookGlobalId,
                    key = chapter.url,
                    name = chapter.name,
                    read = chapter.read,
                    bookmark = chapter.bookmark,
                    lastPageRead = chapter.last_page_read,
                    sourceOrder = chapter.source_order,
                    number = chapter.chapter_number,
                    dateUpload = chapter.date_upload,
                    dateFetch = chapter.date_fetch,
                    translator = chapter.scanlator ?: "",
                    content = contentJson
                )
            }
        }
    }

    override suspend fun getHistory(): List<HistorySyncData> {
        return handler.await {
            // Get all history records with chapter info
            historyQueries.getProgressWithChapters().executeAsList().mapNotNull { row ->
                // Get chapter to construct global ID
                val chapter = chapterQueries.getChapterById(row.chapter_id).executeAsOneOrNull()
                val book = chapter?.let { bookQueries.findBookById(it.book_id).executeAsOneOrNull() }
                
                if (chapter != null && book != null) {
                    val chapterGlobalId = "${book.source}|${chapter.url}"
                    HistorySyncData(
                        chapterGlobalId = chapterGlobalId,
                        lastRead = row.last_read ?: 0L,
                        timeRead = row.time_read,
                        readingProgress = row.progress ?: 0.0
                    )
                } else {
                    null // Skip if chapter or book not found
                }
            }
        }
    }
    
    override suspend fun applyBooks(books: List<BookSyncData>) {
        handler.await {
            books.forEach { book ->
                // Find book by sourceId + key (global ID)
                val existingBook = bookQueries.findBookBySourceAndUrl(
                    source = book.sourceId.toLongOrNull() ?: 0L,
                    url = book.key
                ).executeAsOneOrNull()
                
                if (existingBook == null) {
                    // Book doesn't exist - insert it
                    Log.debug { "[SyncLocalDataSource] Inserting new book: ${book.title} (${book.globalId})" }
                    bookQueries.upsert(
                        id = 0L, // Let database auto-generate ID
                        source = book.sourceId.toLongOrNull() ?: 0L,
                        url = book.key,
                        artist = null,
                        author = book.author.ifEmpty { null },
                        description = book.description.ifEmpty { null },
                        genre = book.genres,
                        title = book.title,
                        status = book.status,
                        thumbnailUrl = book.coverUrl ?: "",
                        customCover = "",
                        favorite = book.favorite,
                        lastUpdate = book.updatedAt,
                        nextUpdate = 0L,
                        initialized = true,
                        viewerFlags = 0L,
                        chapterFlags = 0L,
                        coverLastModified = 0L,
                        dateAdded = book.addedAt,
                        isPinned = false,
                        pinnedOrder = 0L,
                        isArchived = false
                    )
                } else {
                    // Book exists - update only if remote is newer
                    val remoteUpdatedAt = book.updatedAt
                    val localUpdatedAt = existingBook.last_update ?: existingBook.date_added
                    
                    if (remoteUpdatedAt > localUpdatedAt) {
                        Log.debug { "[SyncLocalDataSource] Updating book: ${book.title} (${book.globalId}) - remote is newer" }
                        bookQueries.update(
                            id = existingBook._id,
                            source = book.sourceId.toLongOrNull(),
                            url = book.key,
                            author = book.author.ifEmpty { null },
                            description = book.description.ifEmpty { null },
                            genre = null, // Keep existing genre
                            title = book.title,
                            status = book.status,
                            thumbnailUrl = book.coverUrl,
                            customCover = null,
                            favorite = book.favorite,
                            lastUpdate = book.updatedAt,
                            initialized = true,
                            viewer = null,
                            chapterFlags = null,
                            coverLastModified = null,
                            dateAdded = null,
                            isPinned = null,
                            pinnedOrder = null,
                            isArchived = null
                        )
                    } else {
                        Log.debug { "[SyncLocalDataSource] Skipping book: ${book.title} (${book.globalId}) - local is newer or same" }
                    }
                }
            }
        }
    }
    
    override suspend fun applyChapters(chapters: List<ChapterSyncData>) {
        handler.await {
            chapters.forEach { chapter ->
                // Extract sourceId from bookGlobalId
                val sourceId = chapter.bookGlobalId.substringBefore("|").toLongOrNull() ?: 0L
                val bookKey = chapter.bookGlobalId.substringAfter("|")
                
                // Find the local book by sourceId + key
                val book = bookQueries.findBookBySourceAndUrl(
                    source = sourceId,
                    url = bookKey
                ).executeAsOneOrNull()
                
                if (book == null) {
                    Log.warn { "[SyncLocalDataSource] WARNING: Book not found for chapter: ${chapter.name} (${chapter.bookGlobalId})" }
                    return@forEach
                }
                
                // Find existing chapter by bookId + key
                val existingChapter = chapterQueries.findChapterByBookAndUrl(
                    bookId = book._id,
                    url = chapter.key
                ).executeAsOneOrNull()
                
                // Use upsertForSync to handle both insert and update cases
                // This query doesn't include _id in INSERT, letting database auto-generate it
                // The query uses (book_id, url) unique constraint to detect conflicts
                val shouldUpdate = existingChapter == null || chapter.dateFetch > existingChapter.date_fetch
                
                if (shouldUpdate) {
                    if (existingChapter == null) {
                        Log.debug { "[SyncLocalDataSource] Inserting new chapter: ${chapter.name} (${chapter.globalId})" }
                    } else {
                        Log.debug { "[SyncLocalDataSource] Updating chapter: ${chapter.name} (${chapter.globalId}) - remote is newer" }
                    }
                    
                    // Decode content from JSON string to List<Page> using extension function
                    val contentPages: List<Page> = try {
                        chapter.content.decode()
                    } catch (e: Exception) {
                        emptyList() // Empty content if decoding fails
                    }
                    
                    chapterQueries.upsertForSync(
                        bookId = book._id,
                        key = chapter.key,
                        name = chapter.name,
                        translator = chapter.translator.ifEmpty { null },
                        read = chapter.read,
                        bookmark = chapter.bookmark,
                        last_page_read = chapter.lastPageRead,
                        chapter_number = chapter.number,
                        source_order = chapter.sourceOrder,
                        date_fetch = chapter.dateFetch,
                        date_upload = chapter.dateUpload,
                        content = contentPages, // Decoded content from remote device
                        type = 0L // Default type
                    )
                } else {
                    Log.debug { "[SyncLocalDataSource] Skipping chapter: ${chapter.name} (${chapter.globalId}) - local is newer or same" }
                }
            }
        }
    }
    
    override suspend fun applyHistory(history: List<HistorySyncData>) {
        handler.await {
            history.forEach { historyData ->
                // Extract sourceId and chapterKey from chapterGlobalId
                val sourceId = historyData.chapterGlobalId.substringBefore("|").toLongOrNull() ?: 0L
                val chapterKey = historyData.chapterGlobalId.substringAfter("|")
                
                // Find the chapter by sourceId + key
                // First find all chapters with this key
                val matchingChapter = chapterQueries.findChapterByUrl(chapterKey).executeAsList()
                    .firstOrNull { chapter ->
                        // Verify the book's source matches
                        val book = bookQueries.findBookById(chapter.book_id).executeAsOneOrNull()
                        book?.source == sourceId
                    }
                
                if (matchingChapter == null) {
                    Log.warn { "[SyncLocalDataSource] WARNING: Chapter not found for history: ${historyData.chapterGlobalId}" }
                    return@forEach
                }
                
                // Check if history exists
                val existingHistory = historyQueries.findHistoryByChapterId(matchingChapter._id).executeAsOneOrNull()
                
                if (existingHistory == null) {
                    // History doesn't exist - insert it
                    Log.debug { "[SyncLocalDataSource] Inserting new history for chapter: ${matchingChapter._id}" }
                    historyQueries.upsert(
                        chapterId = matchingChapter._id,
                        readAt = historyData.lastRead,
                        time_read = historyData.timeRead,
                        reading_progress = historyData.readingProgress
                    )
                } else {
                    // History exists - update only if remote is newer
                    val remoteLastRead = historyData.lastRead
                    val localLastRead = existingHistory.last_read ?: 0L
                    
                    if (remoteLastRead > localLastRead) {
                        Log.debug { "[SyncLocalDataSource] Updating history for chapter: ${matchingChapter._id} - remote is newer" }
                        historyQueries.upsert(
                            chapterId = matchingChapter._id,
                            readAt = historyData.lastRead,
                            time_read = historyData.timeRead,
                            reading_progress = historyData.readingProgress
                        )
                    } else {
                        Log.debug { "[SyncLocalDataSource] Skipping history for chapter: ${matchingChapter._id} - local is newer or same" }
                    }
                }
            }
        }
    }
}
