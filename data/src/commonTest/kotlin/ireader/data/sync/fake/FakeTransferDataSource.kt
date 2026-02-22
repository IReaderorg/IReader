package ireader.data.sync.fake

import ireader.domain.models.sync.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Fake implementation of transfer data source for testing.
 * Simulates data transfer, pairing, and security features.
 */
class FakeTransferDataSource {
    
    private val _syncProgress = MutableStateFlow<Map<String, SyncProgress>>(emptyMap())
    
    private var expectedPin: String? = null
    private var maxPinAttempts: Int = Int.MAX_VALUE
    private val pinAttempts = mutableMapOf<String, Int>()
    
    private val deviceCertificates = mutableMapOf<String, String>()
    private val trustedCertificates = mutableMapOf<String, String>()
    private val trustExpiration = mutableMapOf<String, Long>()
    
    private val remoteManifests = mutableMapOf<String, List<SyncableBook>>()
    private var defaultRemoteManifest: List<SyncableBook> = emptyList()
    
    private var failureMode: FailureMode? = null
    private var maxFailures: Int = 0
    private var currentFailures: Int = 0
    private var retryCount: Int = 0
    private val retryDelays = mutableListOf<Long>()
    
    private var responseDelay: Long = 0
    private var pairingTimeout: Long = Long.MAX_VALUE
    private var syncTimeout: Long = Long.MAX_VALUE
    private var transferDelay: Long = 0
    private val transferDelayPerDevice = mutableMapOf<String, Long>()
    
    private var interruptionPoint: Int = Int.MAX_VALUE
    private val itemFailurePattern = mutableMapOf<Long, Int>()
    private val itemFailureCount = mutableMapOf<Long, Int>()
    
    private var encryptionEnabled: Boolean = false
    private var dataEncrypted: Boolean = false
    private val transmittedData = StringBuilder()
    
    private var trustDuration: Long = Long.MAX_VALUE
    private var mitmAttackSimulated: Boolean = false
    
    private var batchingEnabled: Boolean = false
    private var batchSize: Int = 100
    private var batchCount: Int = 0
    
    private var streamingEnabled: Boolean = false
    private var peakMemoryUsage: Long = 0
    private var memoryTrackingEnabled: Boolean = false
    private val memorySnapshots = mutableListOf<Long>()
    
    private val progressUpdates = mutableListOf<SyncProgress>()
    
    fun observeSyncProgress(deviceId: String): Flow<SyncProgress> {
        return MutableStateFlow(
            _syncProgress.value[deviceId] ?: SyncProgress(
                deviceId = deviceId,
                status = SyncStatus.IDLE,
                totalItems = 0,
                completedItems = 0,
                progressPercentage = 0
            )
        ).asStateFlow()
    }
    
    suspend fun initiatePairing(deviceId: String, pin: String): Result<PairedDevice> {
        // Simulate response delay
        if (responseDelay > 0) {
            kotlinx.coroutines.delay(responseDelay)
            if (responseDelay > pairingTimeout) {
                return Result.failure(SyncException(SyncErrorType.TIMEOUT, "Pairing timeout"))
            }
        }
        
        // Check PIN attempts
        val attempts = pinAttempts.getOrDefault(deviceId, 0)
        if (attempts >= maxPinAttempts) {
            return Result.failure(SyncException(SyncErrorType.TOO_MANY_ATTEMPTS, "Too many attempts"))
        }
        
        // Simulate failures
        if (shouldFail()) {
            currentFailures++
            retryCount++
            retryDelays.add(getBackoffDelay(retryCount))
            kotlinx.coroutines.delay(retryDelays.last())
            
            if (currentFailures < maxFailures) {
                return initiatePairing(deviceId, pin)
            }
        }
        
        // Validate PIN
        if (expectedPin != null && pin != expectedPin) {
            pinAttempts[deviceId] = attempts + 1
            return Result.failure(SyncException(SyncErrorType.AUTHENTICATION_FAILED, "Invalid PIN"))
        }
        
        // Check certificate pinning
        val existingCert = trustedCertificates[deviceId]
        val currentCert = deviceCertificates.getOrPut(deviceId) { generateCertificate(deviceId) }
        
        if (existingCert != null && existingCert != currentCert) {
            return Result.failure(SyncException(SyncErrorType.CERTIFICATE_MISMATCH, "Certificate mismatch"))
        }
        
        // Check trust expiration
        val expiration = trustExpiration[deviceId]
        if (expiration != null && System.currentTimeMillis() > expiration) {
            trustedCertificates.remove(deviceId)
            return Result.failure(SyncException(SyncErrorType.TRUST_EXPIRED, "Trust expired"))
        }
        
        // Establish trust
        trustedCertificates[deviceId] = currentCert
        if (trustDuration < Long.MAX_VALUE) {
            trustExpiration[deviceId] = System.currentTimeMillis() + trustDuration
        }
        
        return Result.success(
            PairedDevice(
                device = DeviceInfo(
                    deviceId = deviceId,
                    deviceName = "Test Device",
                    deviceType = DeviceType.ANDROID,
                    appVersion = "1.0.0",
                    ipAddress = "192.168.1.100",
                    port = 8080,
                    lastSeen = System.currentTimeMillis()
                ),
                status = PairingStatus.PAIRED,
                certificate = currentCert,
                isTrusted = true
            )
        )
    }
    
    suspend fun syncWithDevice(deviceId: String): Result<SyncSession> {
        // Check trust
        if (!trustedCertificates.containsKey(deviceId)) {
            return Result.failure(SyncException(SyncErrorType.NOT_PAIRED, "Device not paired"))
        }
        
        // Check trust expiration
        val expiration = trustExpiration[deviceId]
        if (expiration != null && System.currentTimeMillis() > expiration) {
            return Result.failure(SyncException(SyncErrorType.TRUST_EXPIRED, "Trust expired"))
        }
        
        // Check MITM attack
        if (mitmAttackSimulated) {
            return Result.failure(SyncException(SyncErrorType.SECURITY_VIOLATION, "MITM attack detected"))
        }
        
        val manifest = remoteManifests[deviceId] ?: defaultRemoteManifest
        val totalItems = manifest.size
        
        var completedItems = 0
        var failedItems = 0
        var retries = 0
        var wasResumed = false
        var resumedFrom = 0
        
        val delay = transferDelayPerDevice[deviceId] ?: transferDelay
        
        // Simulate transfer
        for ((index, book) in manifest.withIndex()) {
            // Check timeout
            val elapsedTime = (index + 1) * delay
            if (elapsedTime > syncTimeout) {
                return Result.failure(SyncException(SyncErrorType.TIMEOUT, "Sync timeout"))
            }
            
            // Check interruption
            if (index == interruptionPoint) {
                wasResumed = true
                resumedFrom = index
                kotlinx.coroutines.delay(100) // Simulate reconnection
            }
            
            // Check item failure
            val itemFailures = itemFailurePattern[book.id] ?: 0
            val currentItemFailures = itemFailureCount.getOrDefault(book.id, 0)
            
            if (currentItemFailures < itemFailures) {
                itemFailureCount[book.id] = currentItemFailures + 1
                retries++
                kotlinx.coroutines.delay(50) // Retry delay
                // Retry this item
                continue
            }
            
            if (itemFailures == Int.MAX_VALUE) {
                // Permanent failure
                failedItems++
                continue
            }
            
            // Simulate transfer delay
            if (delay > 0) {
                kotlinx.coroutines.delay(delay)
            }
            
            // Track memory if enabled
            if (memoryTrackingEnabled) {
                val currentMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()
                memorySnapshots.add(currentMemory)
                if (currentMemory > peakMemoryUsage) {
                    peakMemoryUsage = currentMemory
                }
            }
            
            // Encrypt data if enabled
            if (encryptionEnabled) {
                dataEncrypted = true
                transmittedData.append(encryptData(book.title))
            } else {
                transmittedData.append(book.title)
            }
            
            completedItems++
            
            // Update progress
            val progress = SyncProgress(
                deviceId = deviceId,
                status = SyncStatus.IN_PROGRESS,
                totalItems = totalItems,
                completedItems = completedItems,
                progressPercentage = (completedItems * 100) / totalItems
            )
            progressUpdates.add(progress)
            _syncProgress.value = _syncProgress.value + (deviceId to progress)
            
            // Track batching
            if (batchingEnabled && completedItems % batchSize == 0) {
                batchCount++
            }
        }
        
        // Check for failures
        if (failureMode == FailureMode.PERSISTENT || 
            (failureMode == FailureMode.NETWORK_INTERRUPTION && currentFailures >= maxFailures)) {
            return Result.failure(SyncException(SyncErrorType.NETWORK_ERROR, "Network error"))
        }
        
        val finalStatus = when {
            failedItems > 0 -> SyncStatus.COMPLETED_WITH_ERRORS
            else -> SyncStatus.COMPLETED
        }
        
        val session = SyncSession(
            id = "session-$deviceId",
            deviceId = deviceId,
            status = finalStatus,
            totalItems = totalItems,
            completedItems = completedItems,
            failedItems = failedItems,
            conflicts = emptyList(),
            itemsToSend = 0,
            itemsToReceive = totalItems,
            retryCount = retries,
            wasResumed = wasResumed,
            resumedFromItem = resumedFrom,
            completionTime = System.currentTimeMillis(),
            startTime = System.currentTimeMillis() - (totalItems * delay)
        )
        
        // Final progress
        val finalProgress = SyncProgress(
            deviceId = deviceId,
            status = finalStatus,
            totalItems = totalItems,
            completedItems = completedItems,
            progressPercentage = 100
        )
        progressUpdates.add(finalProgress)
        _syncProgress.value = _syncProgress.value + (deviceId to finalProgress)
        
        return Result.success(session)
    }
    
    // Configuration methods
    fun setExpectedPin(pin: String) {
        expectedPin = pin
    }
    
    fun setMaxPinAttempts(max: Int) {
        maxPinAttempts = max
    }
    
    fun setRemoteManifest(books: List<SyncableBook>) {
        defaultRemoteManifest = books
    }
    
    fun setRemoteManifestForDevice(deviceId: String, books: List<SyncableBook>) {
        remoteManifests[deviceId] = books
    }
    
    fun setFailureMode(mode: FailureMode, maxFailures: Int = 3) {
        this.failureMode = mode
        this.maxFailures = maxFailures
        this.currentFailures = 0
    }
    
    fun setResponseDelay(delayMs: Long) {
        responseDelay = delayMs
    }
    
    fun setPairingTimeout(timeoutMs: Long) {
        pairingTimeout = timeoutMs
    }
    
    fun setSyncTimeout(timeoutMs: Long) {
        syncTimeout = timeoutMs
    }
    
    fun setTransferDelay(delayMs: Long) {
        transferDelay = delayMs
    }
    
    fun setTransferDelayForDevice(deviceId: String, delayMs: Long) {
        transferDelayPerDevice[deviceId] = delayMs
    }
    
    fun setInterruptionPoint(itemIndex: Int) {
        interruptionPoint = itemIndex
    }
    
    fun setItemFailurePattern(pattern: Map<Long, Int>) {
        itemFailurePattern.clear()
        itemFailurePattern.putAll(pattern)
    }
    
    fun enableEncryptionValidation() {
        encryptionEnabled = true
    }
    
    fun setTrustDuration(durationMs: Long) {
        trustDuration = durationMs
    }
    
    fun simulateMITMAttack() {
        mitmAttackSimulated = true
    }
    
    fun rotateCertificate(deviceId: String) {
        deviceCertificates[deviceId] = generateCertificate("$deviceId-rotated")
    }
    
    fun enableBatching(batchSize: Int) {
        batchingEnabled = true
        this.batchSize = batchSize
    }
    
    fun enableStreaming() {
        streamingEnabled = true
    }
    
    fun enableMemoryTracking() {
        memoryTrackingEnabled = true
    }
    
    // Query methods
    fun wasDataEncrypted(): Boolean = dataEncrypted
    
    fun getTransmittedData(): String = transmittedData.toString()
    
    fun getRetryCount(): Int = retryCount
    
    fun getRetryDelays(): List<Long> = retryDelays.toList()
    
    fun getBatchCount(): Int = batchCount
    
    fun getPeakMemoryUsage(): Long = peakMemoryUsage
    
    fun getMemoryStats(): MemoryStats {
        return MemoryStats(
            peakUsage = peakMemoryUsage,
            averageUsage = if (memorySnapshots.isEmpty()) 0 else memorySnapshots.average().toLong()
        )
    }
    
    fun getProgressUpdates(): List<SyncProgress> = progressUpdates.toList()
    
    fun cleanup() {
        pinAttempts.clear()
        deviceCertificates.clear()
        trustedCertificates.clear()
        trustExpiration.clear()
        remoteManifests.clear()
        defaultRemoteManifest = emptyList()
        currentFailures = 0
        retryCount = 0
        retryDelays.clear()
        itemFailureCount.clear()
        transmittedData.clear()
        dataEncrypted = false
        mitmAttackSimulated = false
        batchCount = 0
        peakMemoryUsage = 0
        memorySnapshots.clear()
        progressUpdates.clear()
    }
    
    private fun shouldFail(): Boolean {
        return when (failureMode) {
            FailureMode.TRANSIENT -> currentFailures < maxFailures
            FailureMode.PERSISTENT -> true
            else -> false
        }
    }
    
    private fun getBackoffDelay(attempt: Int): Long {
        return (100L * (1 shl (attempt - 1))).coerceAtMost(5000L)
    }
    
    private fun generateCertificate(seed: String): String {
        return "CERT-${seed.hashCode()}"
    }
    
    private fun encryptData(data: String): String {
        return data.reversed() // Simple "encryption" for testing
    }
}

data class MemoryStats(
    val peakUsage: Long,
    val averageUsage: Long
)
