package ireader.data.sync.integration

import ireader.data.sync.SyncRepository
import ireader.data.sync.fake.FakeDiscoveryDataSource
import ireader.data.sync.fake.FakeTransferDataSource
import ireader.data.sync.fake.FakeSyncLocalDataSource
import ireader.domain.models.sync.*
import kotlinx.coroutines.test.runTest
import kotlin.test.*

/**
 * Integration tests for network resilience and error handling.
 * Tests connection interruptions, timeouts, and retry logic.
 */
class NetworkResilienceTest {
    
    private lateinit var repository: SyncRepository
    private lateinit var discoveryDataSource: FakeDiscoveryDataSource
    private lateinit var transferDataSource: FakeTransferDataSource
    private lateinit var localDataSource: FakeSyncLocalDataSource
    
    @BeforeTest
    fun setup() {
        discoveryDataSource = FakeDiscoveryDataSource()
        transferDataSource = FakeTransferDataSource()
        localDataSource = FakeSyncLocalDataSource()
        
        repository = SyncRepository(
            discoveryDataSource = discoveryDataSource,
            transferDataSource = transferDataSource,
            localDataSource = localDataSource
        )
    }
    
    @AfterTest
    fun tearDown() {
        discoveryDataSource.cleanup()
        transferDataSource.cleanup()
    }
    
    // RED: Test network interruption handling
    @Test
    fun `sync should handle network interruption and resume`() = runTest {
        // Arrange
        val deviceId = "device-2"
        val books = (1..10).map { createTestBook(it.toLong(), "Book $it") }
        transferDataSource.setRemoteManifest(books)
        
        discoveryDataSource.addDiscoverableDevice(createTestDevice(deviceId))
        transferDataSource.setExpectedPin("123456")
        
        // Simulate interruption after 5 items
        transferDataSource.setInterruptionPoint(5)
        
        // Act
        repository.startDiscovery()
        repository.initiatePairing(deviceId, "123456")
        val syncResult = repository.syncWithDevice(deviceId)
        
        // Assert - Should resume and complete
        assertTrue(syncResult.isSuccess)
        val session = syncResult.getOrNull()
        assertNotNull(session)
        assertEquals(SyncStatus.COMPLETED, session.status)
        assertEquals(10, session.completedItems)
        assertTrue(session.wasResumed)
    }
    
    @Test
    fun `sync should fail after max interruptions`() = runTest {
        // Arrange
        val deviceId = "device-2"
        discoveryDataSource.addDiscoverableDevice(createTestDevice(deviceId))
        transferDataSource.setExpectedPin("123456")
        
        // Simulate persistent interruptions
        transferDataSource.setFailureMode(FailureMode.NETWORK_INTERRUPTION, maxFailures = 10)
        
        // Act
        repository.startDiscovery()
        repository.initiatePairing(deviceId, "123456")
        val syncResult = repository.syncWithDevice(deviceId)
        
        // Assert
        assertTrue(syncResult.isFailure)
        val error = syncResult.exceptionOrNull()
        assertNotNull(error)
        assertTrue(error is SyncException)
        assertEquals(SyncErrorType.NETWORK_ERROR, (error as SyncException).errorType)
    }
    
    // RED: Test connection timeout handling
    @Test
    fun `discovery should timeout if no devices found`() = runTest {
        // Arrange
        discoveryDataSource.setDiscoveryTimeout(1000L) // 1 second
        
        // Act
        repository.startDiscovery()
        kotlinx.coroutines.delay(1100L)
        
        val devices = repository.getDiscoveredDevices().first()
        
        // Assert
        assertTrue(devices.isEmpty())
        assertFalse(repository.isDiscovering())
    }
    
    @Test
    fun `pairing should timeout if device does not respond`() = runTest {
        // Arrange
        val deviceId = "device-2"
        discoveryDataSource.addDiscoverableDevice(createTestDevice(deviceId))
        transferDataSource.setResponseDelay(5000L) // 5 seconds
        transferDataSource.setPairingTimeout(1000L) // 1 second timeout
        
        // Act
        repository.startDiscovery()
        val pairingResult = repository.initiatePairing(deviceId, "123456")
        
        // Assert
        assertTrue(pairingResult.isFailure)
        val error = pairingResult.exceptionOrNull()
        assertNotNull(error)
        assertTrue(error is SyncException)
        assertEquals(SyncErrorType.TIMEOUT, (error as SyncException).errorType)
    }
    
    @Test
    fun `sync should timeout if transfer takes too long`() = runTest {
        // Arrange
        val deviceId = "device-2"
        val books = (1..100).map { createTestBook(it.toLong(), "Book $it") }
        transferDataSource.setRemoteManifest(books)
        
        discoveryDataSource.addDiscoverableDevice(createTestDevice(deviceId))
        transferDataSource.setExpectedPin("123456")
        transferDataSource.setTransferDelay(100L) // 100ms per item = 10 seconds total
        transferDataSource.setSyncTimeout(2000L) // 2 second timeout
        
        // Act
        repository.startDiscovery()
        repository.initiatePairing(deviceId, "123456")
        val syncResult = repository.syncWithDevice(deviceId)
        
        // Assert
        assertTrue(syncResult.isFailure)
        val error = syncResult.exceptionOrNull()
        assertNotNull(error)
        assertTrue(error is SyncException)
        assertEquals(SyncErrorType.TIMEOUT, (error as SyncException).errorType)
    }
    
    // RED: Test retry logic
    @Test
    fun `discovery should retry on transient failures`() = runTest {
        // Arrange
        discoveryDataSource.setFailureMode(FailureMode.TRANSIENT, maxFailures = 2)
        discoveryDataSource.addDiscoverableDevice(createTestDevice("device-2"))
        
        // Act
        repository.startDiscovery()
        kotlinx.coroutines.delay(500L) // Allow retries
        
        val devices = repository.getDiscoveredDevices().first()
        
        // Assert - Should succeed after retries
        assertEquals(1, devices.size)
        assertTrue(discoveryDataSource.getRetryCount() > 0)
    }
    
    @Test
    fun `pairing should retry on transient failures`() = runTest {
        // Arrange
        val deviceId = "device-2"
        discoveryDataSource.addDiscoverableDevice(createTestDevice(deviceId))
        transferDataSource.setExpectedPin("123456")
        transferDataSource.setFailureMode(FailureMode.TRANSIENT, maxFailures = 2)
        
        // Act
        repository.startDiscovery()
        val pairingResult = repository.initiatePairing(deviceId, "123456")
        
        // Assert
        assertTrue(pairingResult.isSuccess)
        assertTrue(transferDataSource.getRetryCount() > 0)
    }
    
    @Test
    fun `sync should retry individual item transfers on failure`() = runTest {
        // Arrange
        val deviceId = "device-2"
        val books = (1..5).map { createTestBook(it.toLong(), "Book $it") }
        transferDataSource.setRemoteManifest(books)
        
        discoveryDataSource.addDiscoverableDevice(createTestDevice(deviceId))
        transferDataSource.setExpectedPin("123456")
        
        // Fail item 3 twice, then succeed
        transferDataSource.setItemFailurePattern(mapOf(3L to 2))
        
        // Act
        repository.startDiscovery()
        repository.initiatePairing(deviceId, "123456")
        val syncResult = repository.syncWithDevice(deviceId)
        
        // Assert
        assertTrue(syncResult.isSuccess)
        val session = syncResult.getOrNull()
        assertNotNull(session)
        assertEquals(5, session.completedItems)
        assertTrue(session.retryCount > 0)
    }
    
    @Test
    fun `retry should use exponential backoff`() = runTest {
        // Arrange
        val deviceId = "device-2"
        discoveryDataSource.addDiscoverableDevice(createTestDevice(deviceId))
        transferDataSource.setExpectedPin("123456")
        transferDataSource.setFailureMode(FailureMode.TRANSIENT, maxFailures = 3)
        
        val startTime = System.currentTimeMillis()
        
        // Act
        repository.startDiscovery()
        repository.initiatePairing(deviceId, "123456")
        
        val endTime = System.currentTimeMillis()
        val retryDelays = transferDataSource.getRetryDelays()
        
        // Assert
        assertTrue(retryDelays.size >= 2)
        // Each retry should take longer than the previous
        for (i in 1 until retryDelays.size) {
            assertTrue(retryDelays[i] > retryDelays[i - 1])
        }
    }
    
    // RED: Test graceful degradation
    @Test
    fun `sync should continue with available items if some fail`() = runTest {
        // Arrange
        val deviceId = "device-2"
        val books = (1..10).map { createTestBook(it.toLong(), "Book $it") }
        transferDataSource.setRemoteManifest(books)
        
        discoveryDataSource.addDiscoverableDevice(createTestDevice(deviceId))
        transferDataSource.setExpectedPin("123456")
        
        // Make items 3, 5, 7 fail permanently
        transferDataSource.setItemFailurePattern(mapOf(
            3L to Int.MAX_VALUE,
            5L to Int.MAX_VALUE,
            7L to Int.MAX_VALUE
        ))
        
        // Act
        repository.startDiscovery()
        repository.initiatePairing(deviceId, "123456")
        val syncResult = repository.syncWithDevice(deviceId)
        
        // Assert
        assertTrue(syncResult.isSuccess)
        val session = syncResult.getOrNull()
        assertNotNull(session)
        assertEquals(SyncStatus.COMPLETED_WITH_ERRORS, session.status)
        assertEquals(7, session.completedItems) // 10 - 3 failed
        assertEquals(3, session.failedItems)
        assertEquals(10, session.totalItems)
    }
    
    @Test
    fun `partial sync should be resumable`() = runTest {
        // Arrange
        val deviceId = "device-2"
        val books = (1..10).map { createTestBook(it.toLong(), "Book $it") }
        transferDataSource.setRemoteManifest(books)
        
        discoveryDataSource.addDiscoverableDevice(createTestDevice(deviceId))
        transferDataSource.setExpectedPin("123456")
        
        // Interrupt after 5 items
        transferDataSource.setInterruptionPoint(5)
        
        // Act - First sync (interrupted)
        repository.startDiscovery()
        repository.initiatePairing(deviceId, "123456")
        val firstSync = repository.syncWithDevice(deviceId)
        
        // Resume sync
        val resumeSync = repository.resumeSync(deviceId)
        
        // Assert
        assertTrue(resumeSync.isSuccess)
        val session = resumeSync.getOrNull()
        assertNotNull(session)
        assertEquals(10, session.completedItems)
        assertEquals(5, session.resumedFromItem)
    }
    
    // Helper functions
    private fun createTestDevice(id: String) = DeviceInfo(
        deviceId = id,
        deviceName = "Test Device",
        deviceType = DeviceType.ANDROID,
        appVersion = "1.0.0",
        ipAddress = "192.168.1.100",
        port = 8080,
        lastSeen = System.currentTimeMillis()
    )
    
    private fun createTestBook(id: Long, title: String) = SyncableBook(
        id = id,
        title = title,
        author = "Test Author",
        lastModified = System.currentTimeMillis(),
        coverUrl = null,
        chapters = emptyList()
    )
}
