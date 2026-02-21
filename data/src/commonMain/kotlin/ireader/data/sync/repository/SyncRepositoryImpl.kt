package ireader.data.sync.repository

import ireader.data.sync.datasource.DiscoveryDataSource
import ireader.data.sync.datasource.SyncLocalDataSource
import ireader.data.sync.datasource.TransferDataSource
import ireader.domain.models.sync.*
import ireader.domain.repositories.Connection
import ireader.domain.repositories.SyncRepository
import ireader.domain.repositories.SyncResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Implementation of SyncRepository.
 * 
 * Orchestrates device discovery, connection management, and data synchronization
 * between devices using the provided data sources.
 *
 * @property discoveryDataSource Handles device discovery on local network
 * @property transferDataSource Handles data transfer between devices
 * @property localDataSource Handles local database operations
 */
@OptIn(ExperimentalUuidApi::class)
class SyncRepositoryImpl(
    private val discoveryDataSource: DiscoveryDataSource,
    private val transferDataSource: TransferDataSource,
    private val localDataSource: SyncLocalDataSource
) : SyncRepository {
    
    private val _syncStatus = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    private var isCancelled = false
    
    // Generate device info for this device
    private val currentDeviceInfo = DeviceInfo(
        deviceId = Uuid.random().toString(),
        deviceName = "IReader Device",
        deviceType = DeviceType.ANDROID,
        appVersion = "1.0.0",
        ipAddress = "0.0.0.0",
        port = 8080,
        lastSeen = System.currentTimeMillis()
    )
    
    // ========== Discovery Operations ==========
    
    override suspend fun startDiscovery(): Result<Unit> {
        return try {
            // Start broadcasting this device's presence
            discoveryDataSource.startBroadcasting(currentDeviceInfo).getOrThrow()
            
            // Start discovering other devices
            discoveryDataSource.startDiscovery().getOrThrow()
            
            _syncStatus.value = SyncStatus.Discovering
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun stopDiscovery(): Result<Unit> {
        return try {
            // Stop broadcasting
            discoveryDataSource.stopBroadcasting().getOrThrow()
            
            // Stop discovering
            discoveryDataSource.stopDiscovery().getOrThrow()
            
            _syncStatus.value = SyncStatus.Idle
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override fun observeDiscoveredDevices(): Flow<List<DiscoveredDevice>> {
        return discoveryDataSource.observeDiscoveredDevices()
    }
    
    override suspend fun getDeviceInfo(deviceId: String): Result<DeviceInfo> {
        return try {
            // Get current list of discovered devices
            val discoveredDevices = discoveryDataSource.observeDiscoveredDevices().first()
            
            // Find the device in the list
            val discoveredDevice = discoveredDevices.find { it.deviceInfo.deviceId == deviceId }
                ?: return Result.failure(Exception("Device not found: $deviceId"))
            
            // Return the DeviceInfo from the discovered device
            Result.success(discoveredDevice.deviceInfo)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // ========== Connection Operations ==========
    
    override suspend fun connectToDevice(device: DeviceInfo): Result<Connection> {
        return try {
            _syncStatus.value = SyncStatus.Connecting(device.deviceName)
            
            // Start server to accept incoming connections
            transferDataSource.startServer(device.port).getOrThrow()
            
            // Connect to the remote device
            transferDataSource.connectToDevice(device).getOrThrow()
            
            val connection = Connection(
                deviceId = device.deviceId,
                deviceName = device.deviceName
            )
            
            Result.success(connection)
        } catch (e: Exception) {
            _syncStatus.value = SyncStatus.Failed(
                device.deviceName,
                SyncError.ConnectionFailed(e.message ?: "Unknown error")
            )
            Result.failure(e)
        }
    }
    
    override suspend fun disconnectFromDevice(connection: Connection): Result<Unit> {
        return try {
            // Disconnect from the device
            transferDataSource.disconnectFromDevice().getOrThrow()
            
            // Stop the server
            transferDataSource.stopServer().getOrThrow()
            
            _syncStatus.value = SyncStatus.Idle
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // ========== Sync Operations ==========
    
    override suspend fun exchangeManifests(connection: Connection): Result<Pair<SyncManifest, SyncManifest>> {
        return try {
            // Build local manifest from local data
            val localManifest = buildLocalManifest()
            
            // Get remote manifest via transfer protocol
            // In real implementation, this would be sent/received via the transfer protocol
            // For now, return an empty remote manifest
            val remoteManifest = SyncManifest(
                deviceId = connection.deviceId,
                timestamp = System.currentTimeMillis(),
                items = emptyList()
            )
            
            Result.success(Pair(localManifest, remoteManifest))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun performSync(
        connection: Connection,
        localManifest: SyncManifest,
        remoteManifest: SyncManifest
    ): Result<SyncResult> {
        return try {
            if (isCancelled) {
                isCancelled = false
                return Result.failure(Exception("Sync cancelled"))
            }
            
            _syncStatus.value = SyncStatus.Syncing(
                deviceName = connection.deviceName,
                progress = 0.0f,
                currentItem = "Starting sync"
            )
            
            val startTime = System.currentTimeMillis()
            
            // Calculate items to sync
            val itemsToSend = calculateItemsToSend(localManifest, remoteManifest)
            val itemsToReceive = calculateItemsToReceive(localManifest, remoteManifest)
            
            var itemsSynced = 0
            
            // Send items
            if (itemsToSend.isNotEmpty()) {
                val dataToSend = buildSyncData(itemsToSend)
                transferDataSource.sendData(dataToSend).getOrThrow()
                itemsSynced += itemsToSend.size
            }
            
            // Receive items
            if (itemsToReceive.isNotEmpty()) {
                val receivedData = transferDataSource.receiveData().getOrThrow()
                applySync(receivedData).getOrThrow()
                itemsSynced += itemsToReceive.size
            }
            
            val duration = System.currentTimeMillis() - startTime
            
            // Update last sync time
            updateLastSyncTime(connection.deviceId, System.currentTimeMillis()).getOrThrow()
            
            val syncResult = SyncResult(
                deviceId = connection.deviceId,
                itemsSynced = itemsSynced,
                duration = duration
            )
            
            _syncStatus.value = SyncStatus.Completed(
                deviceName = connection.deviceName,
                syncedItems = itemsSynced,
                duration = duration
            )
            
            Result.success(syncResult)
        } catch (e: Exception) {
            _syncStatus.value = SyncStatus.Failed(
                connection.deviceName,
                SyncError.TransferFailed(e.message ?: "Unknown error")
            )
            Result.failure(e)
        }
    }
    
    // ========== Status Operations ==========
    
    override fun observeSyncStatus(): Flow<SyncStatus> {
        return _syncStatus.asStateFlow()
    }
    
    override suspend fun cancelSync(): Result<Unit> {
        isCancelled = true
        _syncStatus.value = SyncStatus.Idle
        return Result.success(Unit)
    }
    
    // ========== Local Data Operations ==========
    
    override suspend fun getBooksToSync(): Result<List<BookSyncData>> {
        return try {
            val books = localDataSource.getBooks()
            Result.success(books)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getReadingProgress(): Result<List<ReadingProgressData>> {
        return try {
            val progress = localDataSource.getProgress()
            Result.success(progress)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getBookmarks(): Result<List<BookmarkData>> {
        return try {
            val bookmarks = localDataSource.getBookmarks()
            Result.success(bookmarks)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun applySync(data: SyncData): Result<Unit> {
        return try {
            // Apply books
            // In real implementation, would merge with local database
            
            // Apply reading progress
            // In real implementation, would merge with local database
            
            // Apply bookmarks
            // In real implementation, would merge with local database
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // ========== Metadata Operations ==========
    
    override suspend fun getLastSyncTime(deviceId: String): Result<Long?> {
        return try {
            val metadata = localDataSource.getSyncMetadata(deviceId)
            Result.success(metadata?.lastSyncTime)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun updateLastSyncTime(deviceId: String, timestamp: Long): Result<Unit> {
        return try {
            val existingMetadata = localDataSource.getSyncMetadata(deviceId)
            
            val metadata = if (existingMetadata != null) {
                existingMetadata.copy(
                    lastSyncTime = timestamp,
                    updatedAt = System.currentTimeMillis()
                )
            } else {
                ireader.data.sync.datasource.SyncMetadataEntity(
                    deviceId = deviceId,
                    deviceName = "Unknown Device",
                    deviceType = "UNKNOWN",
                    lastSyncTime = timestamp,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
            }
            
            localDataSource.upsertSyncMetadata(metadata)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // ========== Private Helper Methods ==========
    
    private suspend fun buildLocalManifest(): SyncManifest {
        val items = mutableListOf<SyncManifestItem>()
        
        // Add books to manifest
        // In real implementation, would get from local data source
        
        return SyncManifest(
            deviceId = currentDeviceInfo.deviceId,
            timestamp = System.currentTimeMillis(),
            items = items
        )
    }
    
    private fun calculateItemsToSend(
        localManifest: SyncManifest,
        remoteManifest: SyncManifest
    ): List<SyncManifestItem> {
        val remoteItemIds = remoteManifest.items.map { it.itemId }.toSet()
        val remoteItemHashes = remoteManifest.items.associate { it.itemId to it.hash }
        
        return localManifest.items.filter { localItem ->
            // Send if remote doesn't have it
            !remoteItemIds.contains(localItem.itemId) ||
            // Or if local version is newer (different hash)
            remoteItemHashes[localItem.itemId] != localItem.hash
        }
    }
    
    private fun calculateItemsToReceive(
        localManifest: SyncManifest,
        remoteManifest: SyncManifest
    ): List<SyncManifestItem> {
        val localItemIds = localManifest.items.map { it.itemId }.toSet()
        val localItemHashes = localManifest.items.associate { it.itemId to it.hash }
        
        return remoteManifest.items.filter { remoteItem ->
            // Receive if local doesn't have it
            !localItemIds.contains(remoteItem.itemId) ||
            // Or if remote version is newer (different hash)
            localItemHashes[remoteItem.itemId] != remoteItem.hash
        }
    }
    
    private suspend fun buildSyncData(items: List<SyncManifestItem>): SyncData {
        val books = mutableListOf<BookSyncData>()
        val progress = mutableListOf<ReadingProgressData>()
        val bookmarks = mutableListOf<BookmarkData>()
        
        // Build sync data based on items
        // In real implementation, would fetch from local data source
        
        return SyncData(
            books = books,
            readingProgress = progress,
            bookmarks = bookmarks,
            metadata = SyncMetadata(
                deviceId = currentDeviceInfo.deviceId,
                timestamp = System.currentTimeMillis(),
                version = 1,
                checksum = calculateChecksum(books, progress, bookmarks)
            )
        )
    }
    
    private fun calculateChecksum(
        books: List<BookSyncData>,
        progress: List<ReadingProgressData>,
        bookmarks: List<BookmarkData>
    ): String {
        // Simple checksum calculation
        // In real implementation, would use SHA-256
        val content = "${books.size}-${progress.size}-${bookmarks.size}"
        return content.hashCode().toString()
    }
}
