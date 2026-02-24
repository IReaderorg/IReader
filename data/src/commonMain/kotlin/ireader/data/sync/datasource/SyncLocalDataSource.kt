package ireader.data.sync.datasource

import kotlinx.coroutines.flow.Flow

/**
 * Data source interface for local database operations related to sync.
 * 
 * This interface defines the contract for accessing and manipulating sync-related
 * data in the local SQLDelight database. It provides methods for managing:
 * - Sync metadata (device sync history)
 * - Sync logs (operation history)
 * 
 * Implementation will use SQLDelight queries defined in:
 * - data/sync_metadata.sq
 * - data/sync_log.sq
 */
interface SyncLocalDataSource {
    
    // ========== Sync Metadata Operations ==========
    
    /**
     * Get sync metadata for a specific device.
     * 
     * @param deviceId Unique identifier of the device
     * @return Sync metadata entity or null if not found
     */
    suspend fun getSyncMetadata(deviceId: String): SyncMetadataEntity?
    
    /**
     * Insert or update sync metadata for a device.
     * 
     * If metadata for the device already exists, it will be updated.
     * Otherwise, a new record will be created.
     * 
     * @param metadata Sync metadata to upsert
     */
    suspend fun upsertSyncMetadata(metadata: SyncMetadataEntity)
    
    /**
     * Delete sync metadata for a device.
     * 
     * @param deviceId Unique identifier of the device
     */
    suspend fun deleteSyncMetadata(deviceId: String)
    
    // ========== Sync Log Operations ==========
    
    /**
     * Insert a new sync log entry.
     * 
     * @param log Sync log entity to insert
     */
    suspend fun insertSyncLog(log: SyncLogEntity)
    
    /**
     * Get a sync log entry by its ID.
     * 
     * @param id Unique identifier of the log entry
     * @return Sync log entity or null if not found
     */
    suspend fun getSyncLogById(id: Long): SyncLogEntity?
    
    /**
     * Observe sync logs for a specific device.
     * 
     * Returns a Flow that emits the current list of sync logs for the device
     * whenever new logs are added.
     * 
     * @param deviceId Unique identifier of the device
     * @return Flow emitting list of sync logs for the device
     */
    fun getSyncLogsByDevice(deviceId: String): Flow<List<SyncLogEntity>>
    
    // ========== Sync Data Operations ==========
    
    /**
     * Get all books for synchronization (all books, not just favorites).
     * 
     * @return List of book sync data
     */
    suspend fun getBooks(): List<ireader.domain.models.sync.BookSyncData>
    
    /**
     * Get all chapters for synchronization.
     * 
     * @return List of chapter sync data
     */
    suspend fun getChapters(): List<ireader.domain.models.sync.ChapterSyncData>
    
    /**
     * Get all history records for synchronization.
     * 
     * @return List of history sync data
     */
    suspend fun getHistory(): List<ireader.domain.models.sync.HistorySyncData>
    
    /**
     * Apply synced books to the local database.
     * Uses global ID (sourceId + key) to match books.
     * 
     * @param books List of books to apply
     */
    suspend fun applyBooks(books: List<ireader.domain.models.sync.BookSyncData>)
    
    /**
     * Apply synced chapters to the local database.
     * Uses global ID (sourceId + key) to match chapters.
     * 
     * @param chapters List of chapters to apply
     */
    suspend fun applyChapters(chapters: List<ireader.domain.models.sync.ChapterSyncData>)
    
    /**
     * Apply synced history to the local database.
     * Uses chapter global ID to match history records.
     * 
     * @param history List of history records to apply
     */
    suspend fun applyHistory(history: List<ireader.domain.models.sync.HistorySyncData>)
}

/**
 * Entity representing sync metadata in the database.
 * 
 * Maps to the sync_metadata table.
 */
data class SyncMetadataEntity(
    val deviceId: String,
    val deviceName: String,
    val deviceType: String,
    val lastSyncTime: Long,
    val createdAt: Long,
    val updatedAt: Long
)

/**
 * Entity representing a sync log entry in the database.
 * 
 * Maps to the sync_log table.
 */
data class SyncLogEntity(
    val id: Long,
    val syncId: String,
    val deviceId: String,
    val status: String,
    val itemsSynced: Int,
    val duration: Long,
    val errorMessage: String?,
    val timestamp: Long
)
