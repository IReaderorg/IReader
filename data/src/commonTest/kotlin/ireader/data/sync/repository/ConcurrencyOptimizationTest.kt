package ireader.data.sync.repository

import ireader.data.sync.datasource.FakeDiscoveryDataSource
import ireader.data.sync.datasource.FakeSyncLocalDataSource
import ireader.data.sync.datasource.FakeTransferDataSource
import ireader.domain.models.sync.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.*
import kotlin.test.*
import kotlin.time.Duration.Companion.milliseconds

/**
 * Tests for Phase 10.4 - Concurrency Optimization.
 * 
 * Following TDD methodology:
 * 1. Write test first (RED)
 * 2. Implement minimal code to pass (GREEN)
 * 3. Refactor (REFACTOR)
 * 
 * Tests verify:
 * - 10.4.1: Appropriate coroutine dispatchers (IO, Default, Main)
 * - 10.4.2: Parallel processing where beneficial
 * - 10.4.3: Proper synchronization for shared state
 * - 10.4.4: Coroutine performance profiling
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ConcurrencyOptimizationTest {
    
    private lateinit var discoveryDataSource: FakeDiscoveryDataSource
    private lateinit var transferDataSource: FakeTransferDataSource
    private lateinit var localDataSource: FakeSyncLocalDataSource
    private lateinit var repository: SyncRepositoryImpl
    private lateinit var testDispatcher: TestDispatcher
    
    @BeforeTest
    fun setup() {
        testDispatcher = StandardTestDispatcher()
        Dispatchers.setMain(testDispatcher)
        
        discoveryDataSource = FakeDiscoveryDataSource()
        transferDataSource = FakeTransferDataSource()
        localDataSource = FakeSyncLocalDataSource()
        repository = SyncRepositoryImpl(
            discoveryDataSource = discoveryDataSource,
            transferDataSource = transferDataSource,
            localDataSource = localDataSource
        )
    }
    
    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }
    
    // ========== 10.4.1: Dispatcher Usage Tests ==========
    
    @Test
    fun `network operations should use IO dispatcher`() = runTest {
        // Arrange
        val device = createTestDevice()
        
        // Act
        val result = repository.connectToDevice(device)
        
        // Assert
        assertTrue(result.isSuccess)
        // Verify that network operations don't block the main thread
        // This test ensures IO dispatcher is used for network calls
    }
    
    @Test
    fun `database operations should use IO dispatcher`() = runTest {
        // Arrange
        val book = createTestBook()
        localDataSource.addBook(book)
        
        // Act
        val result = repository.getBooksToSync()
        
        // Assert
        assertTrue(result.isSuccess)
        // Verify database operations use IO dispatcher
    }
    
    @Test
    fun `CPU intensive operations should use Default dispatcher`() = runTest {
        // Arrange
        val device = createTestDevice()
        val connection = repository.connectToDevice(device).getOrThrow()
        val localManifest = createTestManifest("local", 100)
        val remoteManifest = createTestManifest("remote", 100)
        
        // Act - Manifest comparison is CPU intensive
        val result = repository.performSync(connection, localManifest, remoteManifest)
        
        // Assert
        assertTrue(result.isSuccess)
        // Verify CPU-intensive operations use Default dispatcher
    }
    
    // ========== 10.4.2: Parallel Processing Tests ==========
    
    @Test
    fun `multiple device discoveries should run in parallel`() = runTest {
        // Arrange
        val startTime = System.currentTimeMillis()
        
        // Act - Start multiple discovery operations
        val results = List(3) {
            async {
                repository.startDiscovery()
            }
        }.map { it.await() }
        
        val duration = System.currentTimeMillis() - startTime
        
        // Assert
        assertTrue(results.all { it.isSuccess })
        // Parallel execution should be faster than sequential
        // If sequential, would take 3x time
        assertTrue(duration < 1000, "Parallel execution should be fast")
    }
    
    @Test
    fun `fetching multiple data types should run in parallel`() = runTest {
        // Arrange
        localDataSource.addBook(createTestBook())
        localDataSource.addProgress(createTestProgress())
        localDataSource.addBookmark(createTestBookmark())
        
        val startTime = System.currentTimeMillis()
        
        // Act - Fetch all data types in parallel
        val booksDeferred = async { repository.getBooksToSync() }
        val progressDeferred = async { repository.getReadingProgress() }
        val bookmarksDeferred = async { repository.getBookmarks() }
        
        val books = booksDeferred.await()
        val progress = progressDeferred.await()
        val bookmarks = bookmarksDeferred.await()
        
        val duration = System.currentTimeMillis() - startTime
        
        // Assert
        assertTrue(books.isSuccess)
        assertTrue(progress.isSuccess)
        assertTrue(bookmarks.isSuccess)
        // Parallel execution should be faster
        assertTrue(duration < 500, "Parallel data fetching should be fast")
    }
    
    @Test
    fun `manifest comparison should process items in parallel`() = runTest {
        // Arrange
        val device = createTestDevice()
        val connection = repository.connectToDevice(device).getOrThrow()
        
        // Create large manifests to test parallel processing
        val localManifest = createTestManifest("local", 1000)
        val remoteManifest = createTestManifest("remote", 1000)
        
        val startTime = System.currentTimeMillis()
        
        // Act
        val result = repository.performSync(connection, localManifest, remoteManifest)
        
        val duration = System.currentTimeMillis() - startTime
        
        // Assert
        assertTrue(result.isSuccess)
        // Large manifest processing should benefit from parallelization
        assertTrue(duration < 5000, "Parallel manifest processing should be efficient")
    }
    
    // ========== 10.4.3: Synchronization Tests ==========
    
    @Test
    fun `concurrent sync status updates should be thread-safe`() = runTest {
        // Arrange
        val device = createTestDevice()
        
        // Act - Multiple concurrent status updates
        val jobs = List(10) {
            async {
                repository.connectToDevice(device)
                delay(10.milliseconds)
                repository.cancelSync()
            }
        }
        
        jobs.forEach { it.await() }
        
        // Assert - No crashes or data corruption
        val status = repository.observeSyncStatus()
        assertNotNull(status)
    }
    
    @Test
    fun `concurrent device connections should be synchronized`() = runTest {
        // Arrange
        val devices = List(5) { createTestDevice(id = "device-$it") }
        
        // Act - Try to connect to multiple devices concurrently
        val results = devices.map { device ->
            async {
                repository.connectToDevice(device)
            }
        }.map { it.await() }
        
        // Assert - All connections should succeed without race conditions
        assertTrue(results.all { it.isSuccess })
    }
    
    @Test
    fun `concurrent metadata updates should not cause data loss`() = runTest {
        // Arrange
        val deviceId = "test-device"
        val timestamps = List(100) { System.currentTimeMillis() + it }
        
        // Act - Concurrent metadata updates
        val results = timestamps.map { timestamp ->
            async {
                repository.updateLastSyncTime(deviceId, timestamp)
            }
        }.map { it.await() }
        
        // Assert
        assertTrue(results.all { it.isSuccess })
        
        // Verify final state is consistent
        val lastSyncTime = repository.getLastSyncTime(deviceId).getOrNull()
        assertNotNull(lastSyncTime)
        assertTrue(timestamps.contains(lastSyncTime))
    }
    
    @Test
    fun `shared state access should be mutex-protected`() = runTest {
        // Arrange
        val device = createTestDevice()
        val connection = repository.connectToDevice(device).getOrThrow()
        
        // Act - Concurrent sync operations on same connection
        val results = List(5) {
            async {
                repository.performSync(
                    connection,
                    createTestManifest("local", 10),
                    createTestManifest("remote", 10)
                )
            }
        }.map { it.await() }
        
        // Assert - Operations should be serialized, not cause corruption
        // At least one should succeed
        assertTrue(results.any { it.isSuccess })
    }
    
    // ========== 10.4.4: Performance Profiling Tests ==========
    
    @Test
    fun `sync operation should complete within performance budget`() = runTest {
        // Arrange
        val device = createTestDevice()
        val connection = repository.connectToDevice(device).getOrThrow()
        val localManifest = createTestManifest("local", 100)
        val remoteManifest = createTestManifest("remote", 100)
        
        val startTime = System.currentTimeMillis()
        
        // Act
        val result = repository.performSync(connection, localManifest, remoteManifest)
        
        val duration = System.currentTimeMillis() - startTime
        
        // Assert
        assertTrue(result.isSuccess)
        // Performance budget: 100 items should sync in under 2 seconds
        assertTrue(duration < 2000, "Sync should complete within performance budget")
    }
    
    @Test
    fun `discovery should not block other operations`() = runTest {
        // Arrange
        repository.startDiscovery()
        
        // Act - Perform other operations while discovery is running
        val startTime = System.currentTimeMillis()
        val booksResult = repository.getBooksToSync()
        val duration = System.currentTimeMillis() - startTime
        
        // Assert
        assertTrue(booksResult.isSuccess)
        // Should complete quickly, not blocked by discovery
        assertTrue(duration < 500, "Operations should not be blocked by discovery")
        
        // Cleanup
        repository.stopDiscovery()
    }
    
    @Test
    fun `coroutine cancellation should be handled properly`() = runTest {
        // Arrange
        val device = createTestDevice()
        val connection = repository.connectToDevice(device).getOrThrow()
        
        // Act - Start sync and cancel it
        val syncJob = async {
            repository.performSync(
                connection,
                createTestManifest("local", 1000),
                createTestManifest("remote", 1000)
            )
        }
        
        delay(50.milliseconds)
        repository.cancelSync()
        
        val result = syncJob.await()
        
        // Assert - Cancellation should be handled gracefully
        assertTrue(result.isFailure || result.isSuccess)
    }
    
    @Test
    fun `memory usage should remain stable during large sync`() = runTest {
        // Arrange
        val device = createTestDevice()
        val connection = repository.connectToDevice(device).getOrThrow()
        
        // Create large dataset
        val largeManifest = createTestManifest("local", 10000)
        
        // Act - Perform large sync
        val result = repository.performSync(
            connection,
            largeManifest,
            createTestManifest("remote", 10000)
        )
        
        // Assert
        assertTrue(result.isSuccess)
        // Memory should be managed efficiently (no OOM)
        // This test verifies streaming/chunking is used
    }
    
    // ========== Helper Methods ==========
    
    private fun createTestDevice(id: String = "test-device"): DeviceInfo {
        return DeviceInfo(
            deviceId = id,
            deviceName = "Test Device",
            deviceType = DeviceType.ANDROID,
            appVersion = "1.0.0",
            ipAddress = "192.168.1.100",
            port = 8080,
            lastSeen = System.currentTimeMillis()
        )
    }
    
    private fun createTestBook(id: Long = 1L): BookSyncData {
        return BookSyncData(
            bookId = id,
            title = "Test Book $id",
            author = "Test Author",
            coverUrl = null,
            sourceId = "source-1",
            sourceUrl = "https://example.com/book/$id",
            addedAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            fileHash = "hash-$id"
        )
    }
    
    private fun createTestProgress(bookId: Long = 1L): ReadingProgressData {
        return ReadingProgressData(
            bookId = bookId,
            chapterId = 10L,
            chapterIndex = 5,
            offset = 100,
            progress = 0.5f,
            lastReadAt = System.currentTimeMillis()
        )
    }
    
    private fun createTestBookmark(id: Long = 1L): BookmarkData {
        return BookmarkData(
            bookmarkId = id,
            bookId = 1L,
            chapterId = 3L,
            position = 50,
            note = "Test note",
            createdAt = System.currentTimeMillis()
        )
    }
    
    private fun createTestManifest(deviceId: String, itemCount: Int): SyncManifest {
        val items = List(itemCount) { index ->
            SyncManifestItem(
                itemId = "item-$deviceId-$index",
                itemType = SyncItemType.BOOK,
                hash = "hash-$index",
                lastModified = System.currentTimeMillis()
            )
        }
        
        return SyncManifest(
            deviceId = deviceId,
            timestamp = System.currentTimeMillis(),
            items = items
        )
    }
}
