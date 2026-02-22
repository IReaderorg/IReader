package ireader.data.sync.datasource

import kotlinx.coroutines.flow.Flow

/**
 * Data source interface for local database operations related to sync.
 * 
 * This interface defines the contract for accessing and manipulating sync-related
 * data in the local SQLDelight database. It provides methods for managing:
 * - Sync metadata (device sync history)
 * - Trusted devices (paired devices)
 * - Sync logs (operation history)
 * 
 * Implementation will use SQLDelight queries defined in:
 * - data/sync_metadata.sq
 * - data/trusted_devices.sq
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
    
    // ========== Trusted Devices Operations ==========
    
    /**
     * Get a trusted device by its ID.
     * 
     * @param deviceId Unique identifier of the device
     * @return Trusted device entity or null if not found
     */
    suspend fun getTrustedDevice(deviceId: String): TrustedDeviceEntity?
    
    /**
     * Insert or update a trusted device.
     * 
     * If a device with the same deviceId already exists, it will be updated.
     * Otherwise, a new record will be created.
     * 
     * @param device Trusted device entity to upsert
     */
    suspend fun upsertTrustedDevice(device: TrustedDeviceEntity)
    
    /**
     * Observe all active trusted devices.
     * 
     * Returns a Flow that emits the current list of active (non-expired, isActive=true)
     * trusted devices whenever the list changes.
     * 
     * @return Flow emitting list of active trusted devices
     */
    fun getActiveTrustedDevices(): Flow<List<TrustedDeviceEntity>>
    
    /**
     * Deactivate a trusted device.
     * 
     * Sets the isActive flag to false, effectively removing the device from
     * the trusted list without deleting the record.
     * 
     * @param deviceId Unique identifier of the device
     */
    suspend fun deactivateTrustedDevice(deviceId: String)
    
    /**
     * Update the expiration timestamp of a trusted device.
     * 
     * This is used when re-authenticating a device to extend its trust period.
     * 
     * @param deviceId Unique identifier of the device
     * @param expiresAt New expiration timestamp (milliseconds since epoch)
     */
    suspend fun updateDeviceExpiration(deviceId: String, expiresAt: Long)
    
    /**
     * Delete a trusted device.
     * 
     * Permanently removes the device from the trusted devices list.
     * 
     * @param deviceId Unique identifier of the device
     */
    suspend fun deleteTrustedDevice(deviceId: String)
    
    /**
     * Update the certificate fingerprint for a trusted device.
     * 
     * This is used for certificate pinning to store the SHA-256 fingerprint
     * of the device's certificate.
     * 
     * @param deviceId Unique identifier of the device
     * @param fingerprint SHA-256 fingerprint of the certificate, or null to remove
     */
    suspend fun updateCertificateFingerprint(deviceId: String, fingerprint: String?)
    
    /**
     * Get the certificate fingerprint for a trusted device.
     * 
     * Returns the stored SHA-256 fingerprint for certificate pinning validation.
     * 
     * @param deviceId Unique identifier of the device
     * @return Certificate fingerprint or null if not set
     */
    suspend fun getCertificateFingerprint(deviceId: String): String?
    
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
     * Get all books for synchronization.
     * 
     * @return List of book sync data
     */
    suspend fun getBooks(): List<ireader.domain.models.sync.BookSyncData>
    
    /**
     * Get all reading progress for synchronization.
     * 
     * @return List of reading progress data
     */
    suspend fun getProgress(): List<ireader.domain.models.sync.ReadingProgressData>
    
    /**
     * Get all bookmarks for synchronization.
     * 
     * @return List of bookmark data
     */
    suspend fun getBookmarks(): List<ireader.domain.models.sync.BookmarkData>
    
    /**
     * Apply synced books to the local database.
     * 
     * @param books List of books to apply
     */
    suspend fun applyBooks(books: List<ireader.domain.models.sync.BookSyncData>)
    
    /**
     * Apply synced reading progress to the local database.
     * 
     * @param progress List of reading progress to apply
     */
    suspend fun applyProgress(progress: List<ireader.domain.models.sync.ReadingProgressData>)
    
    /**
     * Apply synced bookmarks to the local database.
     * 
     * @param bookmarks List of bookmarks to apply
     */
    suspend fun applyBookmarks(bookmarks: List<ireader.domain.models.sync.BookmarkData>)
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
 * Entity representing a trusted device in the database.
 * 
 * Maps to the trusted_devices table.
 */
data class TrustedDeviceEntity(
    val deviceId: String,
    val deviceName: String,
    val pairedAt: Long,
    val expiresAt: Long,
    val isActive: Boolean,
    val certificateFingerprint: String? = null
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
