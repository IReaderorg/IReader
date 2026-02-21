package ireader.domain.repositories

import ireader.domain.models.sync.*
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for sync operations.
 * Defines all operations needed for device discovery, connection, and data synchronization.
 */
interface SyncRepository {
    
    // ========== Discovery Operations ==========
    
    /**
     * Start discovering devices on the local network.
     * Begins broadcasting this device's presence and listening for other devices.
     *
     * @return Result indicating success or failure
     */
    suspend fun startDiscovery(): Result<Unit>
    
    /**
     * Stop discovering devices and stop broadcasting this device's presence.
     *
     * @return Result indicating success or failure
     */
    suspend fun stopDiscovery(): Result<Unit>
    
    /**
     * Observe the list of discovered devices in real-time.
     * The flow emits a new list whenever devices are added, removed, or updated.
     *
     * @return Flow of discovered device lists
     */
    fun observeDiscoveredDevices(): Flow<List<DiscoveredDevice>>
    
    /**
     * Get information about a specific device.
     *
     * @param deviceId ID of the device to get information about
     * @return Result containing DeviceInfo or error
     */
    suspend fun getDeviceInfo(deviceId: String): Result<DeviceInfo>
    
    // ========== Connection Operations ==========
    
    /**
     * Establish a connection with a remote device.
     *
     * @param device Information about the device to connect to
     * @return Result containing Connection object or error
     */
    suspend fun connectToDevice(device: DeviceInfo): Result<Connection>
    
    /**
     * Disconnect from a remote device and clean up resources.
     *
     * @param connection The connection to close
     * @return Result indicating success or failure
     */
    suspend fun disconnectFromDevice(connection: Connection): Result<Unit>
    
    // ========== Sync Operations ==========
    
    /**
     * Exchange sync manifests with the remote device.
     * This determines what data needs to be synced.
     *
     * @param connection Active connection to the remote device
     * @return Result containing pair of (local manifest, remote manifest) or error
     */
    suspend fun exchangeManifests(connection: Connection): Result<Pair<SyncManifest, SyncManifest>>
    
    /**
     * Perform the actual sync operation, transferring data between devices.
     *
     * @param connection Active connection to the remote device
     * @param localManifest Manifest of local data
     * @param remoteManifest Manifest of remote data
     * @return Result containing SyncResult or error
     */
    suspend fun performSync(
        connection: Connection,
        localManifest: SyncManifest,
        remoteManifest: SyncManifest
    ): Result<SyncResult>
    
    // ========== Status Operations ==========
    
    /**
     * Observe the current sync status in real-time.
     *
     * @return Flow of sync status updates
     */
    fun observeSyncStatus(): Flow<SyncStatus>
    
    /**
     * Cancel an ongoing sync operation.
     *
     * @return Result indicating success or failure
     */
    suspend fun cancelSync(): Result<Unit>
    
    // ========== Local Data Operations ==========
    
    /**
     * Get all books that should be included in sync.
     *
     * @return Result containing list of BookSyncData or error
     */
    suspend fun getBooksToSync(): Result<List<BookSyncData>>
    
    /**
     * Get all reading progress records.
     *
     * @return Result containing list of ReadingProgressData or error
     */
    suspend fun getReadingProgress(): Result<List<ReadingProgressData>>
    
    /**
     * Get all bookmarks.
     *
     * @return Result containing list of BookmarkData or error
     */
    suspend fun getBookmarks(): Result<List<BookmarkData>>
    
    /**
     * Apply synced data to the local database.
     * This merges the received data with local data.
     *
     * @param data The sync data to apply
     * @return Result indicating success or failure
     */
    suspend fun applySync(data: SyncData): Result<Unit>
    
    // ========== Metadata Operations ==========
    
    /**
     * Get the timestamp of the last successful sync with a specific device.
     *
     * @param deviceId ID of the device
     * @return Result containing timestamp (null if never synced) or error
     */
    suspend fun getLastSyncTime(deviceId: String): Result<Long?>
    
    /**
     * Update the last sync timestamp for a device.
     *
     * @param deviceId ID of the device
     * @param timestamp Timestamp of the sync
     * @return Result indicating success or failure
     */
    suspend fun updateLastSyncTime(deviceId: String, timestamp: Long): Result<Unit>
}

/**
 * Represents an active connection to a remote device.
 *
 * @property deviceId ID of the connected device
 * @property deviceName Name of the connected device
 */
data class Connection(
    val deviceId: String,
    val deviceName: String
)

/**
 * Result of a sync operation.
 *
 * @property deviceId ID of the device that was synced with
 * @property itemsSynced Number of items that were synced
 * @property duration Duration of the sync in milliseconds
 */
data class SyncResult(
    val deviceId: String,
    val itemsSynced: Int,
    val duration: Long
)
