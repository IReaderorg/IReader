package ireader.data.sync

import ireader.data.core.DatabaseHandler
import ireader.data.sync.datasource.SyncLocalDataSource
import ireader.data.sync.datasource.SyncLogEntity
import ireader.data.sync.datasource.SyncMetadataEntity
import ireader.data.sync.datasource.TrustedDeviceEntity
import ireader.domain.models.sync.BookSyncData
import ireader.domain.models.sync.BookmarkData
import ireader.domain.models.sync.ReadingProgressData
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

    // ========== Trusted Devices Operations ==========

    override suspend fun getTrustedDevice(deviceId: String): TrustedDeviceEntity? {
        return handler.await {
            trusted_devicesQueries.getTrustedDevice(deviceId).executeAsOneOrNull()?.let {
                TrustedDeviceEntity(
                    deviceId = it.device_id,
                    deviceName = it.device_name,
                    pairedAt = it.paired_at,
                    expiresAt = it.expires_at,
                    isActive = it.is_active,
                    certificateFingerprint = it.certificate_fingerprint
                )
            }
        }
    }
    
    override suspend fun upsertTrustedDevice(device: TrustedDeviceEntity) {
        handler.await {
            trusted_devicesQueries.upsertTrustedDevice(
                device_id = device.deviceId,
                device_name = device.deviceName,
                paired_at = device.pairedAt,
                expires_at = device.expiresAt,
                is_active = device.isActive,
                certificate_fingerprint = device.certificateFingerprint
            )
        }
    }

    override fun getActiveTrustedDevices(): Flow<List<TrustedDeviceEntity>> {
        return handler.subscribeToList {
            trusted_devicesQueries.getActiveTrustedDevices()
        }.map { list ->
            list.map {
                TrustedDeviceEntity(
                    deviceId = it.device_id,
                    deviceName = it.device_name,
                    pairedAt = it.paired_at,
                    expiresAt = it.expires_at,
                    isActive = it.is_active,
                    certificateFingerprint = it.certificate_fingerprint
                )
            }
        }.conflate() // Drop intermediate values if collector is slow (Task 10.1.3)
    }

    override suspend fun deactivateTrustedDevice(deviceId: String) {
        handler.await {
            trusted_devicesQueries.updateDeviceActiveStatus(
                is_active = false,
                device_id = deviceId
            )
        }
    }
    
    override suspend fun updateDeviceExpiration(deviceId: String, expiresAt: Long) {
        handler.await {
            trusted_devicesQueries.updateDeviceExpiration(
                expires_at = expiresAt,
                device_id = deviceId
            )
        }
    }
    
    override suspend fun deleteTrustedDevice(deviceId: String) {
        handler.await {
            trusted_devicesQueries.deleteTrustedDevice(deviceId)
        }
    }
    
    override suspend fun updateCertificateFingerprint(deviceId: String, fingerprint: String?) {
        handler.await {
            trusted_devicesQueries.updateCertificateFingerprint(
                certificate_fingerprint = fingerprint,
                device_id = deviceId
            )
        }
    }
    
    override suspend fun getCertificateFingerprint(deviceId: String): String? {
        return handler.await {
            trusted_devicesQueries.getCertificateFingerprint(deviceId).executeAsOneOrNull()?.certificate_fingerprint
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
            bookQueries.findInLibraryBooks().executeAsList().map { book ->
                BookSyncData(
                    bookId = book._id,
                    title = book.title,
                    author = book.author ?: "",
                    coverUrl = book.thumbnail_url,
                    sourceId = book.source.toString(),
                    sourceUrl = book.url,
                    addedAt = book.date_added,
                    updatedAt = book.last_update ?: book.date_added,
                    fileHash = null
                )
            }
        }
    }

    override suspend fun getProgress(): List<ReadingProgressData> {
        return handler.await {
            // Optimized: Single JOIN query instead of N+1 queries (Task 10.1.2)
            historyQueries.getProgressWithChapters().executeAsList().map { row ->
                ReadingProgressData(
                    bookId = row.book_id,
                    chapterId = row.chapter_id,
                    chapterIndex = row.source_order.toInt(),
                    offset = row.last_page_read.toInt(),
                    progress = row.progress?.toFloat() ?: 0f,
                    lastReadAt = row.last_read ?: 0L
                )
            }
        }
    }

    override suspend fun getBookmarks(): List<BookmarkData> {
        return handler.await {
            // Optimized: SQL WHERE clause instead of fetching all + filtering (Task 10.1.2)
            chapterQueries.getBookmarkedChapters().executeAsList().map { chapter ->
                BookmarkData(
                    bookmarkId = chapter._id,
                    bookId = chapter.book_id,
                    chapterId = chapter._id,
                    position = chapter.last_page_read.toInt(),
                    note = null, // Chapter table doesn't have a note field
                    createdAt = chapter.date_fetch
                )
            }
        }
    }
    
    override suspend fun applyBooks(books: List<BookSyncData>) {
        handler.await {
            books.forEach { book ->
                // Check if book exists
                val existingBook = bookQueries.findBookById(book.bookId).executeAsOneOrNull()
                
                if (existingBook == null) {
                    // Book doesn't exist - insert it
                    println("[SyncLocalDataSource] Inserting new book: ${book.title} (${book.bookId})")
                    bookQueries.upsert(
                        id = book.bookId,
                        source = book.sourceId.toLongOrNull() ?: 0L,
                        url = book.sourceUrl,
                        artist = null,
                        author = book.author.ifEmpty { null },
                        description = null,
                        genre = emptyList(),
                        title = book.title,
                        status = 0L, // Unknown status
                        thumbnailUrl = book.coverUrl ?: "",
                        customCover = "",
                        favorite = true, // Mark as favorite since it's being synced
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
                        println("[SyncLocalDataSource] Updating book: ${book.title} (${book.bookId}) - remote is newer")
                        bookQueries.update(
                            id = book.bookId,
                            source = book.sourceId.toLongOrNull(),
                            url = book.sourceUrl,
                            author = book.author.ifEmpty { null },
                            description = null,
                            genre = null,
                            title = book.title,
                            status = null,
                            thumbnailUrl = book.coverUrl,
                            customCover = null,
                            favorite = true,
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
                        println("[SyncLocalDataSource] Skipping book: ${book.title} (${book.bookId}) - local is newer or same")
                    }
                }
            }
        }
    }
    
    override suspend fun applyProgress(progress: List<ReadingProgressData>) {
        handler.await {
            progress.forEach { progressData ->
                // Check if progress exists
                val existingProgress = historyQueries.findHistoryByChapterId(progressData.chapterId).executeAsOneOrNull()
                
                if (existingProgress == null) {
                    // Progress doesn't exist - insert it
                    println("[SyncLocalDataSource] Inserting new progress for chapter: ${progressData.chapterId}")
                    historyQueries.upsert(
                        chapterId = progressData.chapterId,
                        readAt = progressData.lastReadAt,
                        time_read = 0L, // We don't track time_read in sync data
                        reading_progress = progressData.progress.toDouble()
                    )
                    
                    // Update chapter's last_page_read
                    chapterQueries.update(
                        chapterId = progressData.chapterId,
                        mangaId = null,
                        url = null,
                        name = null,
                        scanlator = null,
                        read = null,
                        bookmark = null,
                        lastPageRead = progressData.offset.toLong(),
                        chapterNumber = null,
                        sourceOrder = null,
                        dateFetch = null,
                        dateUpload = null
                    )
                } else {
                    // Progress exists - update only if remote is newer
                    val remoteLastReadAt = progressData.lastReadAt
                    val localLastReadAt = existingProgress.last_read ?: 0L
                    
                    if (remoteLastReadAt > localLastReadAt) {
                        println("[SyncLocalDataSource] Updating progress for chapter: ${progressData.chapterId} - remote is newer")
                        historyQueries.upsert(
                            chapterId = progressData.chapterId,
                            readAt = progressData.lastReadAt,
                            time_read = existingProgress.time_read, // Preserve existing time_read
                            reading_progress = progressData.progress.toDouble()
                        )
                        
                        // Update chapter's last_page_read
                        chapterQueries.update(
                            chapterId = progressData.chapterId,
                            mangaId = null,
                            url = null,
                            name = null,
                            scanlator = null,
                            read = null,
                            bookmark = null,
                            lastPageRead = progressData.offset.toLong(),
                            chapterNumber = null,
                            sourceOrder = null,
                            dateFetch = null,
                            dateUpload = null
                        )
                    } else {
                        println("[SyncLocalDataSource] Skipping progress for chapter: ${progressData.chapterId} - local is newer or same")
                    }
                }
            }
        }
    }
    
    override suspend fun applyBookmarks(bookmarks: List<BookmarkData>) {
        handler.await {
            bookmarks.forEach { bookmark ->
                // Check if chapter exists
                val existingChapter = chapterQueries.getChapterById(bookmark.chapterId).executeAsOneOrNull()
                
                if (existingChapter != null) {
                    // Chapter exists - update bookmark flag using the update query
                    println("[SyncLocalDataSource] Setting bookmark for chapter: ${bookmark.chapterId}")
                    chapterQueries.update(
                        chapterId = bookmark.chapterId,
                        mangaId = null,
                        url = null,
                        name = null,
                        scanlator = null,
                        read = null,
                        bookmark = true,
                        lastPageRead = if (bookmark.position > 0) bookmark.position.toLong() else null,
                        chapterNumber = null,
                        sourceOrder = null,
                        dateFetch = null,
                        dateUpload = null
                    )
                } else {
                    println("[SyncLocalDataSource] WARNING: Chapter ${bookmark.chapterId} not found for bookmark")
                }
            }
        }
    }
}
