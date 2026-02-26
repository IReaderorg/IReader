package ireader.data.sync.repository

import ireader.core.log.Log
import ireader.data.sync.ConcurrencyManager
import ireader.data.sync.datasource.DiscoveryDataSource
import ireader.data.sync.datasource.SyncLocalDataSource
import ireader.data.sync.datasource.TransferDataSource
import ireader.domain.models.sync.*
import ireader.domain.repositories.Connection
import ireader.domain.repositories.SyncRepository
import ireader.domain.repositories.SyncResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.withContext
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
 * @property platformConfig Platform-specific configuration for device ID
 */
@OptIn(ExperimentalUuidApi::class)
class SyncRepositoryImpl(
    private val discoveryDataSource: DiscoveryDataSource,
    private val transferDataSource: TransferDataSource,
    private val localDataSource: SyncLocalDataSource,
    private val platformConfig: ireader.domain.config.PlatformConfig,
    private val syncPreferences: ireader.domain.preferences.prefs.SyncPreferences
) : SyncRepository {
    
    // Repository scope for Flow lifecycle management (Task 10.1.3)
    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    
    // Phase 10.4: Concurrency manager for parallel operations
    private val concurrencyManager = ConcurrencyManager(maxConcurrentTransfers = 3)
    
    private val _syncStatus = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    private var isCancelled = false
    
    // Shared Flow for discovered devices to prevent duplicate subscriptions (Task 10.1.3)
    private val _discoveredDevices = discoveryDataSource
        .observeDiscoveredDevices()
        .shareIn(
            scope = repositoryScope,
            started = SharingStarted.WhileSubscribed(5000), // Keep alive 5s after last subscriber
            replay = 1 // Replay last value to new subscribers
        )
    
    // Get persistent device info for this device
    private val currentDeviceInfo = run {
        val deviceId = platformConfig.getDeviceId()
        val deviceName = getDeviceName()
        val deviceType = getDeviceType()
        val appVersion = getAppVersion()
        
        // Debug logging
        Log.debug { "[SyncRepository] Device Info:" }
        Log.debug { "[SyncRepository]   ID: $deviceId" }
        Log.debug { "[SyncRepository]   Name: $deviceName" }
        Log.debug { "[SyncRepository]   Type: $deviceType" }
        Log.debug { "[SyncRepository]   Version: $appVersion" }
        
        DeviceInfo(
            deviceId = deviceId,
            deviceName = deviceName,
            deviceType = deviceType,
            appVersion = appVersion,
            ipAddress = "0.0.0.0",
            port = 8963,
            lastSeen = System.currentTimeMillis()
        )
    }
    
    /**
     * Get the device name based on platform.
     * Returns the actual device/computer name from the system.
     */
    private fun getDeviceName(): String {
        return try {
            val deviceType = getDeviceType()
            
            when (deviceType) {
                DeviceType.ANDROID -> {
                    // For Android, try to get device model from Build class
                    try {
                        // Use reflection to access android.os.Build
                        val buildClass = Class.forName("android.os.Build")
                        val model = buildClass.getField("MODEL").get(null) as? String
                        val manufacturer = buildClass.getField("MANUFACTURER").get(null) as? String
                        
                        when {
                            model != null && manufacturer != null -> "$manufacturer $model"
                            model != null -> model
                            manufacturer != null -> manufacturer
                            else -> "Android Device"
                        }
                    } catch (e: Exception) {
                        "Android Device"
                    }
                }
                
                DeviceType.DESKTOP -> {
                    // Try multiple methods to get computer name
                    System.getenv("COMPUTERNAME") // Windows
                        ?: System.getenv("HOSTNAME") // Linux/Mac
                        ?: System.getenv("HOST") // Alternative
                        ?: System.getProperty("user.name")?.let { "$it's Computer" } // Fallback to username
                        ?: "IReader Device"
                }
            }
        } catch (e: Exception) {
            // Fallback if all methods fail
            "IReader Device"
        }
    }
    
    /**
     * Detect the current platform/device type.
     */
    private fun getDeviceType(): DeviceType {
        return try {
            // Check if we're running on Android by trying to access Android-specific classes
            try {
                Class.forName("android.os.Build")
                return DeviceType.ANDROID
            } catch (e: ClassNotFoundException) {
                // Not Android, must be Desktop
                return DeviceType.DESKTOP
            }
        } catch (e: Exception) {
            // Fallback to checking system properties
            val osName = System.getProperty("os.name")?.lowercase() ?: ""
            if (osName.contains("android")) {
                DeviceType.ANDROID
            } else {
                DeviceType.DESKTOP
            }
        }
    }
    
    /**
     * Get the app version.
     * Uses BuildConfig if available, otherwise returns default.
     */
    private fun getAppVersion(): String {
        return try {
            // Try to get version from BuildConfig
            // This will be different for each platform
            "2.0.14" // From ProjectConfig
        } catch (e: Exception) {
            "1.0.0"
        }
    }
    
    // ========== Discovery Operations ==========
    
    override suspend fun startDiscovery(): Result<Unit> {
        return try {
            Log.info { "[SyncRepository] ========== START DISCOVERY ==========" }
            Log.debug { "[SyncRepository] Broadcasting device info:" }
            Log.debug { "[SyncRepository]   Device ID: ${currentDeviceInfo.deviceId}" }
            Log.debug { "[SyncRepository]   Device Name: ${currentDeviceInfo.deviceName}" }
            Log.debug { "[SyncRepository]   Device Type: ${currentDeviceInfo.deviceType}" }
            Log.debug { "[SyncRepository]   IP: ${currentDeviceInfo.ipAddress}:${currentDeviceInfo.port}" }
            
            // Start broadcasting this device's presence
            discoveryDataSource.startBroadcasting(currentDeviceInfo).getOrThrow()
            Log.info { "[SyncRepository] Broadcasting started successfully" }
            
            // Start discovering other devices
            discoveryDataSource.startDiscovery().getOrThrow()
            Log.info { "[SyncRepository] Discovery started successfully" }
            
            _syncStatus.value = SyncStatus.Discovering
            Result.success(Unit)
        } catch (e: Exception) {
            Log.error(e, "[SyncRepository] ERROR: Failed to start discovery: ${e.message}")
            Result.failure(e)
        }
    }
    
    override suspend fun stopDiscovery(): Result<Unit> {
        return try {
            Log.info { "[SyncRepository] ========== STOP DISCOVERY ==========" }
            
            // Stop broadcasting
            discoveryDataSource.stopBroadcasting().getOrThrow()
            Log.debug { "[SyncRepository] Broadcasting stopped" }
            
            // Stop discovering
            discoveryDataSource.stopDiscovery().getOrThrow()
            Log.debug { "[SyncRepository] Discovery stopped" }
            
            _syncStatus.value = SyncStatus.Idle
            Result.success(Unit)
        } catch (e: Exception) {
            Log.error(e, "[SyncRepository] ERROR: Failed to stop discovery: ${e.message}")
            Result.failure(e)
        }
    }
    
    override fun observeDiscoveredDevices(): Flow<List<DiscoveredDevice>> {
        // Return shared Flow to prevent duplicate subscriptions (Task 10.1.3)
        return _discoveredDevices
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
        // Phase 10.4.1: Use IO dispatcher for network operations
        return withContext(Dispatchers.IO) {
            try {
                Log.info { "[SyncRepository] ========== CONNECT TO DEVICE ==========" }
                _syncStatus.value = SyncStatus.Connecting(device.deviceName)
                
                // Determine role based on user manual selection
                val shouldBeServer = syncPreferences.isServer()
                
                // Debug logging
                Log.debug { "[SyncRepository] User selection: ${if (shouldBeServer) "SERVER" else "CLIENT"} mode" }
                Log.debug { "[SyncRepository] Current device ID: ${currentDeviceInfo.deviceId}" }
                Log.debug { "[SyncRepository] Remote device ID: ${device.deviceId}" }
                Log.debug { "[SyncRepository] Should be server: $shouldBeServer" }
                Log.debug { "[SyncRepository] Remote device: ${device.deviceName} at ${device.ipAddress}:${device.port}" }
                
                if (shouldBeServer) {
                    // This device acts as SERVER
                    Log.info { "[SyncRepository] ========== ROLE: SERVER ==========" }
                    Log.debug { "[SyncRepository] Starting server on port 8963" }
                    // Start server first and wait for client to connect
                    val serverPort = 8963 // Use fixed port for server
                    val startResult = transferDataSource.startServer(serverPort)
                    
                    if (startResult.isFailure) {
                        Log.error { "[SyncRepository] ERROR: Failed to start server: ${startResult.exceptionOrNull()?.message}" }
                        throw startResult.exceptionOrNull() ?: Exception("Failed to start server")
                    }
                    
                    Log.debug { "[SyncRepository] Server started successfully, waiting for client connection..." }
                    
                    // Wait for actual client connection with timeout
                    var retryCount = 0
                    val maxRetries = 30 // 30 seconds total timeout
                    var connected = false
                    
                    while (retryCount < maxRetries && !connected) {
                        delay(1000L) // Check every second
                        
                        // Check if client has actually connected
                        connected = transferDataSource.hasActiveConnection()
                        
                        if (!connected) {
                            retryCount++
                            if (retryCount % 5 == 0) {
                                Log.debug { "[SyncRepository] Still waiting for client... (${retryCount}s elapsed)" }
                            }
                        }
                    }
                    
                    if (!connected) {
                        // Clean up server if client didn't connect
                        Log.error { "[SyncRepository] ERROR: Client failed to connect after ${maxRetries} seconds" }
                        Log.debug { "[SyncRepository] Stopping server..." }
                        transferDataSource.stopServer()
                        throw Exception("Client failed to connect after ${maxRetries} seconds. Ensure both devices are on the same network and the other device has started sync.")
                    }
                    
                    Log.info { "[SyncRepository] ✓ Client connected successfully!" }
                } else {
                    // This device acts as CLIENT
                    Log.info { "[SyncRepository] ========== ROLE: CLIENT ==========" }
                    Log.debug { "[SyncRepository] Connecting to server at ${device.ipAddress}:${device.port}" }
                    // Connect to the server with retry logic
                    var retryCount = 0
                    val maxRetries = 10
                    var lastError: Exception? = null
                    var connected = false
                    
                    while (retryCount < maxRetries && !connected) {
                        try {
                            // Wait before attempting connection (give server time to start)
                            delay(2000L) // Wait 2 seconds between attempts
                            
                            Log.debug { "[SyncRepository] Connection attempt ${retryCount + 1}/$maxRetries to ${device.ipAddress}:${device.port}" }
                            val connectResult = transferDataSource.connectToDevice(device)
                            
                            if (connectResult.isFailure) {
                                throw connectResult.exceptionOrNull() ?: Exception("Connection failed")
                            }
                            
                            Log.debug { "[SyncRepository] WebSocket connection initiated, verifying session..." }
                            
                            // Verify connection is actually established
                            // Give WebSocket more time to establish and set session
                            delay(1000L) // Increased from 500ms to 1000ms
                            connected = transferDataSource.hasActiveConnection()
                            
                            if (connected) {
                                Log.info { "[SyncRepository] ✓ Connected successfully!" }
                                break // Connection successful
                            } else {
                                Log.warn { "[SyncRepository] WARNING: Connection established but no active session, retrying..." }
                                // Don't increment retry count for this case, just retry
                            }
                        } catch (e: Exception) {
                            Log.debug { "[SyncRepository] Connection attempt failed: ${e.message}" }
                            lastError = e
                            retryCount++
                            
                            if (retryCount >= maxRetries) {
                                Log.error { "[SyncRepository] ERROR: All connection attempts exhausted" }
                                throw Exception("Failed to connect after $maxRetries attempts: ${e.message}. Ensure the other device has started sync and is acting as server.", e)
                            }
                        }
                    }
                    
                    if (!connected) {
                        Log.error { "[SyncRepository] ERROR: Failed to establish connection after all retries" }
                        throw Exception("Failed to establish connection after $maxRetries attempts. ${lastError?.message ?: "Unknown error"}")
                    }
                }
                
                val connection = Connection(
                    deviceId = device.deviceId,
                    deviceName = device.deviceName
                )
                
                Log.info { "[SyncRepository] ✓ Connection established successfully" }
                Log.debug { "[SyncRepository] Connection details: ${device.deviceName} (${device.deviceId})" }
                
                _syncStatus.value = SyncStatus.Idle
                Result.success(connection)
            } catch (e: Exception) {
                Log.error(e, "[SyncRepository] ========== CONNECTION FAILED ==========")
                Log.error(e, "[SyncRepository] ERROR: ${e.message}")
                
                _syncStatus.value = SyncStatus.Failed(
                    device.deviceName,
                    SyncError.ConnectionFailed(e.message ?: "Unknown error")
                )
                Result.failure(e)
            }
        }
    }
    
    override suspend fun disconnectFromDevice(connection: Connection): Result<Unit> {
        // Phase 10.4.1: Use IO dispatcher for network operations
        return withContext(Dispatchers.IO) {
            try {
                // Determine role based on device ID comparison (same logic as connect)
                val shouldBeServer = currentDeviceInfo.deviceId < connection.deviceId  // Desktop will be server, Android will be client
                
                if (shouldBeServer) {
                    // This device was SERVER - stop the server
                    transferDataSource.stopServer().getOrThrow()
                } else {
                    // This device was CLIENT - disconnect from server
                    transferDataSource.disconnectFromDevice().getOrThrow()
                }
                
                _syncStatus.value = SyncStatus.Idle
                Result.success(Unit)
            } catch (e: Exception) {
                // Log error but still mark as disconnected
                _syncStatus.value = SyncStatus.Idle
                Result.failure(e)
            }
        }
    }
    
    // ========== Sync Operations ==========
    
    override suspend fun exchangeManifests(connection: Connection): Result<Pair<SyncManifest, SyncManifest>> {
        return try {
            Log.info { "[SyncRepository] ========== EXCHANGE MANIFESTS ==========" }
            
            // Build local manifest from local data
            val localManifest = buildLocalManifest()
            Log.debug { "[SyncRepository] Local manifest built: ${localManifest.items.size} items" }
            
            // Determine role based on user manual selection
            val shouldBeServer = syncPreferences.isServer()
            Log.debug { "[SyncRepository] User selection: ${if (shouldBeServer) "SERVER" else "CLIENT"} mode" }
            Log.debug { "[SyncRepository] Role determination: currentDeviceId=${currentDeviceInfo.deviceId}, remoteDeviceId=${connection.deviceId}, shouldBeServer=$shouldBeServer" }
            
            val remoteManifest = if (shouldBeServer) {
                // SERVER: Receive client manifest first, then send ours
                Log.debug { "[SyncRepository] Server: Waiting for client manifest..." }
                val clientManifest = transferDataSource.receiveManifest().getOrThrow()
                Log.debug { "[SyncRepository] Server: Received client manifest with ${clientManifest.items.size} items" }
                
                Log.debug { "[SyncRepository] Server: Sending our manifest..." }
                transferDataSource.sendManifest(localManifest).getOrThrow()
                Log.debug { "[SyncRepository] Server: Manifest sent" }
                
                clientManifest
            } else {
                // CLIENT: Send our manifest first, then receive server's
                Log.debug { "[SyncRepository] Client: Sending our manifest..." }
                transferDataSource.sendManifest(localManifest).getOrThrow()
                Log.debug { "[SyncRepository] Client: Manifest sent" }
                
                Log.debug { "[SyncRepository] Client: Waiting for server manifest..." }
                val serverManifest = transferDataSource.receiveManifest().getOrThrow()
                Log.debug { "[SyncRepository] Client: Received server manifest with ${serverManifest.items.size} items" }
                
                serverManifest
            }
            
            Log.info { "[SyncRepository] ========== MANIFESTS EXCHANGED ==========" }
            Log.debug { "[SyncRepository] Local: ${localManifest.items.size} items" }
            Log.debug { "[SyncRepository] Remote: ${remoteManifest.items.size} items" }
            
            Result.success(Pair(localManifest, remoteManifest))
        } catch (e: Exception) {
            Log.error(e) { "[SyncRepository] ERROR: Failed to exchange manifests: ${e.message}" }
            Result.failure(e)
        }
    }
    
    override suspend fun performSync(
        connection: Connection,
        localManifest: SyncManifest,
        remoteManifest: SyncManifest
    ): Result<SyncResult> {
        return try {
            Log.info { "[SyncRepository] ========== PERFORM SYNC ==========" }
            Log.debug { "[SyncRepository] Syncing with: ${connection.deviceName} (${connection.deviceId})" }
            Log.debug { "[SyncRepository] Local manifest: ${localManifest.items.size} items" }
            Log.debug { "[SyncRepository] Remote manifest: ${remoteManifest.items.size} items" }
            
            if (isCancelled) {
                isCancelled = false
                Log.warn { "[SyncRepository] Sync was cancelled" }
                return Result.failure(Exception("Sync cancelled"))
            }
            
            // Phase 10.4.3: Use mutex for thread-safe status updates
            concurrencyManager.withMutex {
                _syncStatus.value = SyncStatus.Syncing(
                    deviceName = connection.deviceName,
                    progress = 0.0f,
                    currentItem = "Starting sync"
                )
            }
            
            val startTime = System.currentTimeMillis()
            
            // Phase 10.4.1: Use Default dispatcher for CPU-intensive manifest comparison
            val (itemsToSend, itemsToReceive) = withContext(Dispatchers.Default) {
                Log.debug { "[SyncRepository] Calculating items to sync..." }
                // Phase 10.4.2: Calculate items in parallel
                val itemsToSendDeferred = async {
                    calculateItemsToSend(localManifest, remoteManifest)
                }
                val itemsToReceiveDeferred = async {
                    calculateItemsToReceive(localManifest, remoteManifest)
                }
                
                Pair(itemsToSendDeferred.await(), itemsToReceiveDeferred.await())
            }
            
            Log.debug { "[SyncRepository] Items to send: ${itemsToSend.size}" }
            Log.debug { "[SyncRepository] Items to receive: ${itemsToReceive.size}" }
            
            var itemsSynced = 0
            
            // Phase 10.4.2: Send and receive in parallel using coroutineScope
            itemsSynced = coroutineScope {
                val sendJob = async(Dispatchers.IO) {
                    if (itemsToSend.isNotEmpty()) {
                        Log.debug { "[SyncRepository] Sending ${itemsToSend.size} items..." }
                        concurrencyManager.withConcurrencyControl {
                            val dataToSend = buildSyncData(itemsToSend)
                            Log.debug { "[SyncRepository] Built sync data: ${dataToSend.books.size} books, ${dataToSend.chapters.size} chapters, ${dataToSend.history.size} history" }
                            
                            val sendResult = transferDataSource.sendData(dataToSend)
                            if (sendResult.isFailure) {
                                Log.error { "[SyncRepository] ERROR: Failed to send data: ${sendResult.exceptionOrNull()?.message}" }
                                throw sendResult.exceptionOrNull() ?: Exception("Failed to send data")
                            }
                            
                            Log.info { "[SyncRepository] ✓ Data sent successfully" }
                            itemsToSend.size
                        }
                    } else {
                        Log.debug { "[SyncRepository] No items to send" }
                        0
                    }
                }
                
                val receiveJob = async(Dispatchers.IO) {
                    if (itemsToReceive.isNotEmpty()) {
                        Log.debug { "[SyncRepository] Receiving ${itemsToReceive.size} items..." }
                        concurrencyManager.withConcurrencyControl {
                            val receiveResult = transferDataSource.receiveData()
                            if (receiveResult.isFailure) {
                                Log.error { "[SyncRepository] ERROR: Failed to receive data: ${receiveResult.exceptionOrNull()?.message}" }
                                throw receiveResult.exceptionOrNull() ?: Exception("Failed to receive data")
                            }
                            
                            val receivedData = receiveResult.getOrThrow()
                            Log.debug { "[SyncRepository] ✓ Data received: ${receivedData.books.size} books, ${receivedData.chapters.size} chapters, ${receivedData.history.size} history" }
                            
                            Log.debug { "[SyncRepository] Applying received data..." }
                            val applyResult = applySync(receivedData)
                            if (applyResult.isFailure) {
                                Log.error { "[SyncRepository] ERROR: Failed to apply sync: ${applyResult.exceptionOrNull()?.message}" }
                                throw applyResult.exceptionOrNull() ?: Exception("Failed to apply sync")
                            }
                            
                            Log.info { "[SyncRepository] ✓ Data applied successfully" }
                            itemsToReceive.size
                        }
                    } else {
                        Log.debug { "[SyncRepository] No items to receive" }
                        0
                    }
                }
                
                // Wait for both operations to complete
                val sent = sendJob.await()
                val received = receiveJob.await()
                Log.debug { "[SyncRepository] Sync operations completed: sent=$sent, received=$received" }
                sent + received
            }
            
            val duration = System.currentTimeMillis() - startTime
            
            // Phase 10.4.1: Use IO dispatcher for database update
            withContext(Dispatchers.IO) {
                Log.debug { "[SyncRepository] Updating last sync time..." }
                updateLastSyncTime(connection.deviceId, System.currentTimeMillis()).getOrThrow()
            }
            
            val syncResult = SyncResult(
                deviceId = connection.deviceId,
                itemsSynced = itemsSynced,
                duration = duration
            )
            
            Log.info { "[SyncRepository] ========== SYNC COMPLETED ==========" }
            Log.info { "[SyncRepository] Items synced: $itemsSynced" }
            Log.info { "[SyncRepository] Duration: ${duration}ms" }
            
            // Phase 10.4.3: Thread-safe status update
            concurrencyManager.withMutex {
                _syncStatus.value = SyncStatus.Completed(
                    deviceName = connection.deviceName,
                    syncedItems = itemsSynced,
                    duration = duration
                )
            }
            
            Result.success(syncResult)
        } catch (e: Exception) {
            Log.error(e) { "[SyncRepository] ========== SYNC FAILED ==========" }
            Log.error(e) { "[SyncRepository] ERROR: ${e.message}" }
            
            // Phase 10.4.3: Thread-safe status update
            concurrencyManager.withMutex {
                _syncStatus.value = SyncStatus.Failed(
                    connection.deviceName,
                    SyncError.TransferFailed(e.message ?: "Unknown error")
                )
            }
            Result.failure(e)
        }
    }
    
    // ========== Status Operations ==========
    
    override fun observeSyncStatus(): Flow<SyncStatus> {
        return _syncStatus.asStateFlow()
    }
    
    override suspend fun cancelSync(): Result<Unit> {
        // Phase 10.4.3: Thread-safe cancellation
        concurrencyManager.withMutex {
            isCancelled = true
            _syncStatus.value = SyncStatus.Idle
        }
        return Result.success(Unit)
    }
    
    // ========== Local Data Operations ==========
    
    override suspend fun getBooksToSync(): Result<List<BookSyncData>> {
        // Phase 10.4.1: Use IO dispatcher for database operations
        return withContext(Dispatchers.IO) {
            try {
                val books = localDataSource.getBooks()
                Result.success(books)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    override suspend fun getChaptersToSync(): Result<List<ChapterSyncData>> {
        // Phase 10.4.1: Use IO dispatcher for database operations
        return withContext(Dispatchers.IO) {
            try {
                val chapters = localDataSource.getChapters()
                Result.success(chapters)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    override suspend fun getHistoryToSync(): Result<List<HistorySyncData>> {
        // Phase 10.4.1: Use IO dispatcher for database operations
        return withContext(Dispatchers.IO) {
            try {
                val history = localDataSource.getHistory()
                Result.success(history)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    override suspend fun applySync(data: SyncData): Result<Unit> {
        return try {
            Log.debug { "[SyncRepository] Applying sync data: ${data.books.size} books, ${data.chapters.size} chapters, ${data.history.size} history" }
            
            // Apply books first
            if (data.books.isNotEmpty()) {
                try {
                    localDataSource.applyBooks(data.books)
                    Log.debug { "[SyncRepository] ✓ Books applied successfully" }
                } catch (e: Exception) {
                    Log.error(e) { "[SyncRepository] ERROR applying books: ${e.message}" }
                    throw e
                }
            }
            
            // Apply chapters second (after books exist)
            if (data.chapters.isNotEmpty()) {
                try {
                    localDataSource.applyChapters(data.chapters)
                    Log.debug { "[SyncRepository] ✓ Chapters applied successfully" }
                } catch (e: Exception) {
                    Log.error(e) { "[SyncRepository] ERROR applying chapters: ${e.message}" }
                    throw e
                }
            }
            
            // Apply history last (after chapters exist)
            if (data.history.isNotEmpty()) {
                try {
                    localDataSource.applyHistory(data.history)
                    Log.debug { "[SyncRepository] ✓ History applied successfully" }
                } catch (e: Exception) {
                    Log.error(e) { "[SyncRepository] ERROR applying history: ${e.message}" }
                    throw e
                }
            }
            
            Log.info { "[SyncRepository] ✓ All sync data applied successfully" }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.error(e) { "[SyncRepository] ERROR in applySync: ${e.message}" }
            Result.failure(e)
        }
    }
    
    // ========== Metadata Operations ==========
    
    override suspend fun getLastSyncTime(deviceId: String): Result<Long?> {
        // Phase 10.4.1: Use IO dispatcher for database operations
        return withContext(Dispatchers.IO) {
            try {
                val metadata = localDataSource.getSyncMetadata(deviceId)
                Result.success(metadata?.lastSyncTime)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    override suspend fun updateLastSyncTime(deviceId: String, timestamp: Long): Result<Unit> {
        // Phase 10.4.1: Use IO dispatcher for database operations
        return withContext(Dispatchers.IO) {
            try {
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
    }
    
    // ========== Private Helper Methods ==========
    
    private suspend fun buildLocalManifest(): SyncManifest {
        val items = mutableListOf<SyncManifestItem>()
        
        // Add books to manifest
        val books = localDataSource.getBooks()
        books.forEach { book ->
            items.add(
                SyncManifestItem(
                    itemId = book.globalId,
                    itemType = SyncItemType.BOOK,
                    hash = calculateItemHash(book),
                    lastModified = book.updatedAt
                )
            )
        }
        
        // Add chapters to manifest
        val chapters = localDataSource.getChapters()
        chapters.forEach { chapter ->
            items.add(
                SyncManifestItem(
                    itemId = chapter.globalId,
                    itemType = SyncItemType.CHAPTER,
                    hash = calculateItemHash(chapter),
                    lastModified = chapter.dateFetch
                )
            )
        }
        
        // Add history to manifest
        val history = localDataSource.getHistory()
        history.forEach { hist ->
            items.add(
                SyncManifestItem(
                    itemId = hist.chapterGlobalId,
                    itemType = SyncItemType.HISTORY,
                    hash = calculateItemHash(hist),
                    lastModified = hist.lastRead
                )
            )
        }
        
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
        val chapters = mutableListOf<ChapterSyncData>()
        val history = mutableListOf<HistorySyncData>()
        
        // Get all local data
        val allBooks = localDataSource.getBooks()
        val allChapters = localDataSource.getChapters()
        val allHistory = localDataSource.getHistory()
        
        // Filter based on items to send
        val itemIds = items.map { it.itemId }.toSet()
        
        // Add books that match the items list
        books.addAll(
            allBooks.filter { book ->
                itemIds.contains(book.globalId)
            }
        )
        
        // Add chapters that match the items list
        chapters.addAll(
            allChapters.filter { chapter ->
                itemIds.contains(chapter.globalId)
            }
        )
        
        // Add history that matches the items list
        history.addAll(
            allHistory.filter { hist ->
                itemIds.contains(hist.chapterGlobalId)
            }
        )
        
        return SyncData(
            books = books,
            chapters = chapters,
            history = history,
            metadata = SyncMetadata(
                deviceId = currentDeviceInfo.deviceId,
                timestamp = System.currentTimeMillis(),
                version = 1,
                checksum = calculateChecksum(books, chapters, history)
            )
        )
    }
    
    private fun calculateChecksum(
        books: List<BookSyncData>,
        chapters: List<ChapterSyncData>,
        history: List<HistorySyncData>
    ): String {
        // Simple checksum calculation
        // In real implementation, would use SHA-256
        val content = "${books.size}-${chapters.size}-${history.size}"
        return content.hashCode().toString()
    }
    
    private fun calculateItemHash(book: BookSyncData): String {
        return "${book.globalId}-${book.updatedAt}".hashCode().toString()
    }
    
    private fun calculateItemHash(chapter: ChapterSyncData): String {
        return "${chapter.globalId}-${chapter.dateFetch}".hashCode().toString()
    }
    
    private fun calculateItemHash(history: HistorySyncData): String {
        return "${history.chapterGlobalId}-${history.lastRead}".hashCode().toString()
    }
}
