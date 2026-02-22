package ireader.data.sync.integration

import ireader.data.sync.SyncRepository
import ireader.data.sync.fake.FakeDiscoveryDataSource
import ireader.data.sync.fake.FakeTransferDataSource
import ireader.data.sync.fake.FakeSyncLocalDataSource
import ireader.domain.models.sync.*
import kotlinx.coroutines.test.runTest
import kotlin.test.*
import kotlin.time.measureTime

/**
 * Integration tests for performance and scalability.
 * Tests sync with large datasets and concurrent operations.
 */
class PerformanceIntegrationTest {
    
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
    
    // RED: Test sync with 1000+ books
    @Test
    fun `sync should handle 1000 books efficiently`() = runTest {
        // Arrange
        val deviceId = "device-2"
        val bookCount = 1000
        val books = (1..bookCount).map { 
            createTestBook(it.toLong(), "Book $it", chapterCount = 10)
        }
        transferDataSource.setRemoteManifest(books)
        
        discoveryDataSource.addDiscoverableDevice(createTestDevice(deviceId))
        transferDataSource.setExpectedPin("123456")
        
        // Act
        repository.startDiscovery()
        repository.initiatePairing(deviceId, "123456")
        
        val duration = measureTime {
            val syncResult = repository.syncWithDevice(deviceId)
            assertTrue(syncResult.isSuccess)
        }
        
        // Assert
        val session = repository.getCurrentSyncSession(deviceId)
        assertNotNull(session)
        assertEquals(bookCount, session.completedItems)
        
        // Performance assertion - should complete in reasonable time
        // (This is a simulated test, real performance depends on implementation)
        assertTrue(duration.inWholeSeconds < 30, "Sync took too long: ${duration.inWholeSeconds}s")
    }
    
    @Test
    fun `sync should batch large datasets efficiently`() = runTest {
        // Arrange
        val deviceId = "device-2"
        val bookCount = 5000
        val books = (1..bookCount).map { 
            createTestBook(it.toLong(), "Book $it")
        }
        transferDataSource.setRemoteManifest(books)
        transferDataSource.enableBatching(batchSize = 100)
        
        discoveryDataSource.addDiscoverableDevice(createTestDevice(deviceId))
        transferDataSource.setExpectedPin("123456")
        
        // Act
        repository.startDiscovery()
        repository.initiatePairing(deviceId, "123456")
        val syncResult = repository.syncWithDevice(deviceId)
        
        // Assert
        assertTrue(syncResult.isSuccess)
        val session = syncResult.getOrNull()
        assertNotNull(session)
        assertEquals(bookCount, session.completedItems)
        
        // Verify batching was used
        val batchCount = transferDataSource.getBatchCount()
        assertEquals(50, batchCount) // 5000 / 100
    }
    
    // RED: Test large file transfer (simulated)
    @Test
    fun `sync should handle large book with many chapters`() = runTest {
        // Arrange
        val deviceId = "device-2"
        val largeBook = createTestBook(
            id = 1L,
            title = "Large Book",
            chapterCount = 1000,
            avgChapterSize = 50_000 // 50KB per chapter = ~50MB total
        )
        transferDataSource.setRemoteManifest(listOf(largeBook))
        
        discoveryDataSource.addDiscoverableDevice(createTestDevice(deviceId))
        transferDataSource.setExpectedPin("123456")
        
        // Act
        repository.startDiscovery()
        repository.initiatePairing(deviceId, "123456")
        val syncResult = repository.syncWithDevice(deviceId)
        
        // Assert
        assertTrue(syncResult.isSuccess)
        val session = syncResult.getOrNull()
        assertNotNull(session)
        assertEquals(1, session.completedItems)
        
        // Verify progress was reported during transfer
        val progressUpdates = transferDataSource.getProgressUpdates()
        assertTrue(progressUpdates.size > 10) // Should have multiple progress updates
    }
    
    @Test
    fun `sync should stream large files without loading into memory`() = runTest {
        // Arrange
        val deviceId = "device-2"
        val largeBook = createTestBook(
            id = 1L,
            title = "Huge Book",
            chapterCount = 5000,
            avgChapterSize = 100_000 // 100KB per chapter = ~500MB total
        )
        transferDataSource.setRemoteManifest(listOf(largeBook))
        transferDataSource.enableStreaming()
        
        discoveryDataSource.addDiscoverableDevice(createTestDevice(deviceId))
        transferDataSource.setExpectedPin("123456")
        
        val initialMemory = getMemoryUsage()
        
        // Act
        repository.startDiscovery()
        repository.initiatePairing(deviceId, "123456")
        val syncResult = repository.syncWithDevice(deviceId)
        
        val peakMemory = transferDataSource.getPeakMemoryUsage()
        
        // Assert
        assertTrue(syncResult.isSuccess)
        
        // Memory usage should not spike significantly (streaming)
        val memoryIncrease = peakMemory - initialMemory
        assertTrue(memoryIncrease < 50_000_000) // Less than 50MB increase
    }
    
    // RED: Test memory usage during sync
    @Test
    fun `sync should maintain reasonable memory usage with large dataset`() = runTest {
        // Arrange
        val deviceId = "device-2"
        val bookCount = 2000
        val books = (1..bookCount).map { 
            createTestBook(it.toLong(), "Book $it", chapterCount = 50)
        }
        transferDataSource.setRemoteManifest(books)
        transferDataSource.enableMemoryTracking()
        
        discoveryDataSource.addDiscoverableDevice(createTestDevice(deviceId))
        transferDataSource.setExpectedPin("123456")
        
        // Act
        repository.startDiscovery()
        repository.initiatePairing(deviceId, "123456")
        val syncResult = repository.syncWithDevice(deviceId)
        
        // Assert
        assertTrue(syncResult.isSuccess)
        
        val memoryStats = transferDataSource.getMemoryStats()
        assertTrue(memoryStats.peakUsage < 100_000_000) // Less than 100MB
        assertTrue(memoryStats.averageUsage < 50_000_000) // Less than 50MB average
    }
    
    // RED: Test concurrent operations
    @Test
    fun `repository should handle concurrent discovery and sync`() = runTest {
        // Arrange
        val device1 = createTestDevice("device-1")
        val device2 = createTestDevice("device-2")
        discoveryDataSource.addDiscoverableDevice(device1)
        discoveryDataSource.addDiscoverableDevice(device2)
        transferDataSource.setExpectedPin("123456")
        
        // Act - Start discovery and immediately try to pair
        repository.startDiscovery()
        
        val pairing1 = repository.initiatePairing(device1.id, "123456")
        val pairing2 = repository.initiatePairing(device2.id, "123456")
        
        // Assert
        assertTrue(pairing1.isSuccess)
        assertTrue(pairing2.isSuccess)
    }
    
    @Test
    fun `repository should handle multiple simultaneous syncs`() = runTest {
        // Arrange
        val device1 = createTestDevice("device-1")
        val device2 = createTestDevice("device-2")
        val device3 = createTestDevice("device-3")
        
        val books1 = (1..100).map { createTestBook(it.toLong(), "Device1-Book$it") }
        val books2 = (101..200).map { createTestBook(it.toLong(), "Device2-Book$it") }
        val books3 = (201..300).map { createTestBook(it.toLong(), "Device3-Book$it") }
        
        discoveryDataSource.addDiscoverableDevice(device1)
        discoveryDataSource.addDiscoverableDevice(device2)
        discoveryDataSource.addDiscoverableDevice(device3)
        
        transferDataSource.setExpectedPin("123456")
        transferDataSource.setRemoteManifestForDevice(device1.id, books1)
        transferDataSource.setRemoteManifestForDevice(device2.id, books2)
        transferDataSource.setRemoteManifestForDevice(device3.id, books3)
        
        // Act
        repository.startDiscovery()
        repository.initiatePairing(device1.id, "123456")
        repository.initiatePairing(device2.id, "123456")
        repository.initiatePairing(device3.id, "123456")
        
        // Start all syncs concurrently
        val sync1 = repository.syncWithDevice(device1.id)
        val sync2 = repository.syncWithDevice(device2.id)
        val sync3 = repository.syncWithDevice(device3.id)
        
        // Assert
        assertTrue(sync1.isSuccess)
        assertTrue(sync2.isSuccess)
        assertTrue(sync3.isSuccess)
        
        val session1 = sync1.getOrNull()
        val session2 = sync2.getOrNull()
        val session3 = sync3.getOrNull()
        
        assertNotNull(session1)
        assertNotNull(session2)
        assertNotNull(session3)
        
        assertEquals(100, session1.completedItems)
        assertEquals(100, session2.completedItems)
        assertEquals(100, session3.completedItems)
    }
    
    @Test
    fun `concurrent syncs should not interfere with each other`() = runTest {
        // Arrange
        val device1 = createTestDevice("device-1")
        val device2 = createTestDevice("device-2")
        
        discoveryDataSource.addDiscoverableDevice(device1)
        discoveryDataSource.addDiscoverableDevice(device2)
        transferDataSource.setExpectedPin("123456")
        
        // Device 1 has slow transfer
        transferDataSource.setTransferDelayForDevice(device1.id, 100L)
        // Device 2 has fast transfer
        transferDataSource.setTransferDelayForDevice(device2.id, 10L)
        
        val books = (1..50).map { createTestBook(it.toLong(), "Book $it") }
        transferDataSource.setRemoteManifestForDevice(device1.id, books)
        transferDataSource.setRemoteManifestForDevice(device2.id, books)
        
        // Act
        repository.startDiscovery()
        repository.initiatePairing(device1.id, "123456")
        repository.initiatePairing(device2.id, "123456")
        
        val startTime = System.currentTimeMillis()
        
        // Start both syncs
        val sync1 = repository.syncWithDevice(device1.id)
        val sync2 = repository.syncWithDevice(device2.id)
        
        val endTime = System.currentTimeMillis()
        
        // Assert
        assertTrue(sync1.isSuccess)
        assertTrue(sync2.isSuccess)
        
        // Device 2 should complete faster despite starting at same time
        val session1 = sync1.getOrNull()!!
        val session2 = sync2.getOrNull()!!
        
        assertTrue(session2.completionTime < session1.completionTime)
    }
    
    @Test
    fun `sync should handle rapid start stop cycles`() = runTest {
        // Arrange
        val deviceId = "device-2"
        discoveryDataSource.addDiscoverableDevice(createTestDevice(deviceId))
        transferDataSource.setExpectedPin("123456")
        
        // Act - Rapid start/stop
        repeat(10) {
            repository.startDiscovery()
            repository.stopDiscovery()
        }
        
        // Final sync should still work
        repository.startDiscovery()
        val pairingResult = repository.initiatePairing(deviceId, "123456")
        val syncResult = repository.syncWithDevice(deviceId)
        
        // Assert
        assertTrue(pairingResult.isSuccess)
        assertTrue(syncResult.isSuccess)
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
        chapterCount: Int = 0,
        avgChapterSize: Int = 10_000
    ) = SyncableBook(
        id = id,
        title = title,
        author = "Test Author",
        lastModified = System.currentTimeMillis(),
        coverUrl = null,
        chapters = (1..chapterCount).map {
            SyncableChapter(
                id = id * 1000 + it,
                bookId = id,
                title = "Chapter $it",
                content = "x".repeat(avgChapterSize),
                index = it
            )
        }
    )
    
    private fun getMemoryUsage(): Long {
        // Simulated memory usage
        return Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()
    }
}
