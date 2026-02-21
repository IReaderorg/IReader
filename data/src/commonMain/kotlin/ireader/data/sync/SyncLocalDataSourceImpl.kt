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
                    isActive = it.is_active
                )
            }
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
                    isActive = it.is_active
                )
            }
        }
    }

    override suspend fun deactivateTrustedDevice(deviceId: String) {
        handler.await {
            trusted_devicesQueries.updateDeviceActiveStatus(
                is_active = false,
                device_id = deviceId
            )
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
        }
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
            historyQueries.findHistories().executeAsList().mapNotNull { history ->
                // Get the chapter for this history entry
                val chapter = chapterQueries.getChapterById(history.chapter_id).executeAsOneOrNull()
                    ?: return@mapNotNull null
                
                ReadingProgressData(
                    bookId = chapter.book_id,
                    chapterId = history.chapter_id,
                    chapterIndex = chapter.source_order.toInt(),
                    offset = chapter.last_page_read.toInt(),
                    progress = history.progress?.toFloat() ?: 0f,
                    lastReadAt = history.last_read ?: 0L
                )
            }
        }
    }

    override suspend fun getBookmarks(): List<BookmarkData> {
        return handler.await {
            chapterQueries.findAll().executeAsList().filter { it.bookmark }.map { chapter ->
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
}
