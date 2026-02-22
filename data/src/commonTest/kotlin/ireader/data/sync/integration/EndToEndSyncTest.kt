package ireader.data.sync.integration

import ireader.data.sync.SyncRepository
import ireader.data.sync.fake.FakeDiscoveryDataSource
import ireader.data.sync.fake.FakeTransferDataSource
import ireader.data.sync.fake.FakeSyncLocalDataSource
import ireader.domain.models.sync.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.*

/**
 * End-to-end integration tests for complete sync flows.
 * Tests the entire sync lifecycle from discovery to completion.
 */
class EndToEndSyncTest {
    
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
    
    // RED: Test complete sync flow
    @Test
    fun `complete sync flow should discover pair sync and disconnect successfully`() = runTest {
        // Arrange
        val remoteDevice = DeviceInfo(
            deviceId = "device-2",
            deviceName = "Test Device",
            deviceType = DeviceType.ANDROID,
            appVersion = "1.0.0",
            ipAddress = "192.168.1.100",
            port = 8080,
            lastSeen = System.currentTimeMillis()
        )
        discoveryDataSource.addDiscoverableDevice(remoteDevice)
        
        // Act - Discovery
        repository.startDiscovery()
        val discoveredDevices = repository.getDiscoveredDevices().first()
        
        // Assert - Discovery
        assertEquals(1, discoveredDevices.size)
        assertEquals(remoteDevice.id, discoveredDevices[0].id)
        
        // Act - Pairing
        val pairingResult = repository.initiatePairing(remoteDevice.id, "123456")
        
        // Assert - Pairing
        assertTrue(pairingResult.isSuccess)
        val pairedDevice = pairingResult.getOrNull()
        assertNotNull(pairedDevice)
        assertEquals(PairingStatus.PAIRED, pairedDevice.status)
        
        // Act - Sync
        val syncResult = repository.syncWithDevice(remoteDevice.id)
        
        // Assert - Sync
        assertTrue(syncResult.isSuccess)
        val syncSession = syncResult.getOrNull()
        assertNotNull(syncSession)
        assertEquals(SyncStatus.COMPLETED, syncSession.status)
        
        // Act - Disconnect
        repository.disconnect(remoteDevice.id)
        
        // Assert - Disconnect
        val finalDevices = repository.getDiscoveredDevices().first()
        assertTrue(finalDevices.isEmpty())
    }
    
    // RED: Test manifest exchange
    @Test
    fun `manifest exchange should transfer and compare data correctly`() = runTest {
        // Arrange
        val localBooks = listOf(
            createTestBook(id = 1L, title = "Book 1", lastUpdate = 1000L),
            createTestBook(id = 2L, title = "Book 2", lastUpdate = 2000L)
        )
        localDataSource.setBooks(localBooks)
        
        val remoteBooks = listOf(
            createTestBook(id = 1L, title = "Book 1", lastUpdate = 1500L), // Newer
            createTestBook(id = 3L, title = "Book 3", lastUpdate = 3000L)  // New book
        )
        transferDataSource.setRemoteManifest(remoteBooks)
        
        val deviceId = "device-2"
        discoveryDataSource.addDiscoverableDevice(createTestDevice(deviceId))
        
        // Act
        repository.startDiscovery()
        repository.initiatePairing(deviceId, "123456")
        val syncResult = repository.syncWithDevice(deviceId)
        
        // Assert
        assertTrue(syncResult.isSuccess)
        val session = syncResult.getOrNull()
        assertNotNull(session)
        
        // Should detect 1 conflict (Book 1) and 1 new item (Book 3)
        assertEquals(1, session.conflicts.size)
        assertEquals(1, session.itemsToReceive)
        assertEquals(1, session.itemsToSend) // Book 2 is local-only
    }
    
    // RED: Test conflict detection and resolution
    @Test
    fun `conflict detection should identify all conflicting items`() = runTest {
        // Arrange
        val conflictingBooks = listOf(
            createTestBook(id = 1L, title = "Book 1", lastUpdate = 1000L),
            createTestBook(id = 2L, title = "Book 2", lastUpdate = 2000L)
        )
        localDataSource.setBooks(conflictingBooks)
        
        val remoteConflicts = listOf(
            createTestBook(id = 1L, title = "Book 1 Remote", lastUpdate = 1500L),
            createTestBook(id = 2L, title = "Book 2 Remote", lastUpdate = 2500L)
        )
        transferDataSource.setRemoteManifest(remoteConflicts)
        
        val deviceId = "device-2"
        discoveryDataSource.addDiscoverableDevice(createTestDevice(deviceId))
        
        // Act
        repository.startDiscovery()
        repository.initiatePairing(deviceId, "123456")
        val syncResult = repository.syncWithDevice(deviceId)
        
        // Assert
        assertTrue(syncResult.isSuccess)
        val session = syncResult.getOrNull()
        assertNotNull(session)
        assertEquals(2, session.conflicts.size)
        
        // Verify conflict details
        val conflict1 = session.conflicts.find { it.itemId == 1L }
        assertNotNull(conflict1)
        assertEquals("Book 1", conflict1.localVersion.title)
        assertEquals("Book 1 Remote", conflict1.remoteVersion.title)
    }
    
    // RED: Test data transfer with progress tracking
    @Test
    fun `data transfer should report progress correctly`() = runTest {
        // Arrange
        val books = (1..10).map { createTestBook(id = it.toLong(), title = "Book $it") }
        transferDataSource.setRemoteManifest(books)
        
        val deviceId = "device-2"
        discoveryDataSource.addDiscoverableDevice(createTestDevice(deviceId))
        
        val progressUpdates = mutableListOf<SyncProgress>()
        
        // Act
        repository.startDiscovery()
        repository.initiatePairing(deviceId, "123456")
        
        repository.observeSyncProgress(deviceId).collect { progress ->
            progressUpdates.add(progress)
            if (progress.status == SyncStatus.COMPLETED) {
                return@collect
            }
        }
        
        repository.syncWithDevice(deviceId)
        
        // Assert
        assertTrue(progressUpdates.isNotEmpty())
        assertTrue(progressUpdates.any { it.status == SyncStatus.IN_PROGRESS })
        assertTrue(progressUpdates.any { it.status == SyncStatus.COMPLETED })
        
        val finalProgress = progressUpdates.last()
        assertEquals(10, finalProgress.totalItems)
        assertEquals(10, finalProgress.completedItems)
        assertEquals(100, finalProgress.progressPercentage)
    }
    
    // RED: Test error recovery
    @Test
    fun `sync should recover from transient errors`() = runTest {
        // Arrange
        val deviceId = "device-2"
        discoveryDataSource.addDiscoverableDevice(createTestDevice(deviceId))
        transferDataSource.setFailureMode(FailureMode.TRANSIENT, maxFailures = 2)
        
        // Act
        repository.startDiscovery()
        repository.initiatePairing(deviceId, "123456")
        val syncResult = repository.syncWithDevice(deviceId)
        
        // Assert - Should succeed after retries
        assertTrue(syncResult.isSuccess)
        val session = syncResult.getOrNull()
        assertNotNull(session)
        assertEquals(SyncStatus.COMPLETED, session.status)
        assertTrue(session.retryCount > 0)
    }
    
    @Test
    fun `sync should fail after max retries on persistent errors`() = runTest {
        // Arrange
        val deviceId = "device-2"
        discoveryDataSource.addDiscoverableDevice(createTestDevice(deviceId))
        transferDataSource.setFailureMode(FailureMode.PERSISTENT)
        
        // Act
        repository.startDiscovery()
        repository.initiatePairing(deviceId, "123456")
        val syncResult = repository.syncWithDevice(deviceId)
        
        // Assert
        assertTrue(syncResult.isFailure)
        val error = syncResult.exceptionOrNull()
        assertNotNull(error)
        assertTrue(error is SyncException)
        assertEquals(SyncErrorType.TRANSFER_FAILED, (error as SyncException).errorType)
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
    
    private fun createTestBook(
        id: Long,
        title: String,
        lastUpdate: Long = System.currentTimeMillis()
    ) = SyncableBook(
        id = id,
        title = title,
        author = "Test Author",
        lastModified = lastUpdate,
        coverUrl = null,
        chapters = emptyList()
    )
}
