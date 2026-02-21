package ireader.data.sync.datasource

import ireader.domain.models.sync.DeviceInfo
import ireader.domain.models.sync.SyncData
import ireader.domain.models.sync.SyncMetadata
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Fake implementation of TransferDataSource for testing.
 */
class FakeTransferDataSource : TransferDataSource {
    
    var isConnected = false
        private set
    
    var isServerRunning = false
        private set
    
    private val _transferProgress = MutableStateFlow(0f)
    private var remoteManifest: ireader.domain.models.sync.SyncManifest? = null
    private var receivedData: SyncData? = null
    
    override suspend fun startServer(port: Int): Result<Int> {
        isServerRunning = true
        return Result.success(port)
    }
    
    override suspend fun stopServer(): Result<Unit> {
        isServerRunning = false
        return Result.success(Unit)
    }
    
    override suspend fun connectToDevice(deviceInfo: DeviceInfo): Result<Unit> {
        isConnected = true
        return Result.success(Unit)
    }
    
    override suspend fun disconnectFromDevice(): Result<Unit> {
        isConnected = false
        return Result.success(Unit)
    }
    
    override suspend fun sendData(data: SyncData): Result<Unit> {
        lastSentData = data
        receivedData = data
        _transferProgress.value = 1.0f
        return Result.success(Unit)
    }
    
    override suspend fun receiveData(): Result<SyncData> {
        if (shouldTimeout) {
            return Result.failure(Exception("Timeout waiting for data"))
        }
        
        // If a remote manifest was set, convert it to SyncData
        // The manifest items will be embedded in the metadata or we return the manifest separately
        return remoteManifest?.let {
            // For manifest exchange, we return empty SyncData with metadata
            // The actual manifest will be retrieved separately
            val syncData = SyncData(
                books = emptyList(),
                readingProgress = emptyList(),
                bookmarks = emptyList(),
                metadata = SyncMetadata(
                    deviceId = it.deviceId,
                    timestamp = it.timestamp,
                    version = 1,
                    checksum = "test-checksum"
                )
            )
            Result.success(syncData)
        } ?: dataToReceive?.let { Result.success(it) }
            ?: receivedData?.let { Result.success(it) }
            ?: Result.failure(Exception("No data available"))
    }
    
    override fun observeTransferProgress(): Flow<Float> {
        return _transferProgress.asStateFlow()
    }
    
    override suspend fun closeConnection(): Result<Unit> {
        isConnected = false
        isServerRunning = false
        return Result.success(Unit)
    }
    
    // Test helper methods
    fun setRemoteManifest(manifest: ireader.domain.models.sync.SyncManifest) {
        this.remoteManifest = manifest
    }
    
    fun getRemoteManifest(): ireader.domain.models.sync.SyncManifest? {
        return remoteManifest
    }
    
    var lastSentData: SyncData? = null
        private set
    
    private var dataToReceive: SyncData? = null
    private var shouldTimeout = false
    
    fun setDataToReceive(data: SyncData) {
        dataToReceive = data
        receivedData = data
    }
    
    fun updateProgress(progress: Float) {
        _transferProgress.value = progress
    }
    
    fun simulateTimeout(timeout: Boolean) {
        shouldTimeout = timeout
    }
}
