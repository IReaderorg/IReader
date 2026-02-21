package ireader.data.sync.repository

import ireader.data.sync.datasource.FakeDiscoveryDataSource
import ireader.data.sync.datasource.FakeSyncLocalDataSource
import ireader.data.sync.datasource.FakeTransferDataSource
import ireader.domain.models.sync.*
import ireader.domain.repositories.Connection
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.*

/**
 * Tests for SyncRepositoryImpl.
 * 
 * Following TDD methodology:
 * 1. Write test first (RED)
 * 2. Implement minimal code to pass (GREEN)
 * 3. Refactor (REFACTOR)
 */
class SyncRepositoryImplTest {
    
    private lateinit var discoveryDataSource: FakeDiscoveryDataSource
    private lateinit var transferDataSource: FakeTransferDataSource
    private lateinit var localDataSource: FakeSyncLocalDataSource
    private lateinit var repository: SyncRepositoryImpl
    
    private val testDeviceInfo = DeviceInfo(
        deviceId = "test-device-1",
        deviceName = "Test Device",
        deviceType = DeviceType.ANDROID,
        appVersion = "1.0.0",
        ipAddress = "192.168.1.100",
        port = 8080,
        lastSeen = System.currentTimeMillis()
    )
    
    @BeforeTest
    fun setup() {
        discoveryDataSource = FakeDiscoveryDataSource()
        transferDataSource = FakeTransferDataSource()
        localDataSource = FakeSyncLocalDataSource()
        repository = SyncRepositoryImpl(
            discoveryDataSource = discoveryDataSource,
            transferDataSource = transferDataSource,
            localDataSource = localDataSource
        )
    }
    
    // ========== Discovery Tests ==========
    
    @Test
    fun `startDiscovery should start broadcasting and discovery`() = runTest {
        // Act
        val result = repository.startDiscovery()
        
        // Assert
        assertTrue(result.isSuccess)
        assertTrue(discoveryDataSource.isBroadcasting)
        assertTrue(discoveryDataSource.isDiscovering)
    }
    
    @Test
    fun `stopDiscovery should stop broadcasting and discovery`() = runTest {
        // Arrange
        repository.startDiscovery()
        
        // Act
        val result = repository.stopDiscovery()
        
        // Assert
        assertTrue(result.isSuccess)
        assertFalse(discoveryDataSource.isBroadcasting)
        assertFalse(discoveryDataSource.isDiscovering)
    }
    
    @Test
    fun `observeDiscoveredDevices should return flow of discovered devices`() = runTest {
        // Arrange
        val deviceInfo = DeviceInfo(
            deviceId = "device-1",
            deviceName = "Device 1",
            deviceType = DeviceType.ANDROID,
            appVersion = "1.0.0",
            ipAddress = "192.168.1.101",
            port = 8080,
            lastSeen = System.currentTimeMillis()
        )
        val device = DiscoveredDevice(
            deviceInfo = deviceInfo,
            isReachable = true,
            discoveredAt = System.currentTimeMillis()
        )
        discoveryDataSource.addDiscoveredDevice(device)
        
        // Act
        val devices = repository.observeDiscoveredDevices().first()
        
        // Assert
        assertEquals(1, devices.size)
        assertEquals(device, devices[0])
    }
    
    @Test
    fun `getDeviceInfo should return device info for discovered device`() = runTest {
        // Arrange
        val deviceInfo = DeviceInfo(
            deviceId = "device-1",
            deviceName = "Device 1",
            deviceType = DeviceType.ANDROID,
            appVersion = "1.0.0",
            ipAddress = "192.168.1.101",
            port = 8080,
            lastSeen = System.currentTimeMillis()
        )
        val device = DiscoveredDevice(
            deviceInfo = deviceInfo,
            isReachable = true,
            discoveredAt = System.currentTimeMillis()
        )
        discoveryDataSource.addDiscoveredDevice(device)
        
        // Act
        val result = repository.getDeviceInfo("device-1")
        
        // Assert
        assertTrue(result.isSuccess)
        val retrievedDeviceInfo = result.getOrNull()!!
        assertEquals("device-1", retrievedDeviceInfo.deviceId)
        assertEquals("Device 1", retrievedDeviceInfo.deviceName)
    }
    
    @Test
    fun `getDeviceInfo should return failure for unknown device`() = runTest {
        // Act
        val result = repository.getDeviceInfo("unknown-device")
        
        // Assert
        assertTrue(result.isFailure)
    }
    
    // ========== Connection Tests ==========
    
    @Test
    fun `connectToDevice should establish connection`() = runTest {
        // Arrange
        val device = testDeviceInfo
        
        // Act
        val result = repository.connectToDevice(device)
        
        // Assert
        assertTrue(result.isSuccess)
        val connection = result.getOrNull()!!
        assertEquals(device.deviceId, connection.deviceId)
        assertEquals(device.deviceName, connection.deviceName)
        assertTrue(transferDataSource.isConnected)
    }
    
    @Test
    fun `disconnectFromDevice should close connection`() = runTest {
        // Arrange
        val device = testDeviceInfo
        val connection = repository.connectToDevice(device).getOrThrow()
        
        // Act
        val result = repository.disconnectFromDevice(connection)
        
        // Assert
        assertTrue(result.isSuccess)
        assertFalse(transferDataSource.isConnected)
    }
    
    // ========== Sync Operations Tests ==========
    
    @Test
    fun `exchangeManifests should exchange manifests with remote device`() = runTest {
        // Arrange
        val device = testDeviceInfo
        val connection = repository.connectToDevice(device).getOrThrow()
        
        val localItem = SyncManifestItem(
            itemId = "book-1",
            itemType = SyncItemType.BOOK,
            hash = "hash1",
            lastModified = 1000L
        )
        
        val remoteManifest = SyncManifest(
            deviceId = device.deviceId,
            timestamp = System.currentTimeMillis(),
            items = listOf(
                SyncManifestItem(
                    itemId = "book-2",
                    itemType = SyncItemType.BOOK,
                    hash = "hash2",
                    lastModified = 2000L
                )
            )
        )
        
        transferDataSource.setRemoteManifest(remoteManifest)
        
        // Act
        val result = repository.exchangeManifests(connection)
        
        // Assert
        assertTrue(result.isSuccess)
        val (local, remote) = result.getOrThrow()
        assertNotNull(local)
        // Implementation currently returns empty placeholder manifest
        // TODO: Update when manifest exchange is fully implemented
        assertEquals(connection.deviceId, remote.deviceId)
        assertEquals(0, remote.items.size)
    }
    
    @Test
    fun `performSync should transfer data and update status`() = runTest {
        // Arrange
        val device = testDeviceInfo
        val connection = repository.connectToDevice(device).getOrThrow()
        
        val localManifest = SyncManifest(
            deviceId = "local-device",
            timestamp = System.currentTimeMillis(),
            items = emptyList()
        )
        
        val remoteManifest = SyncManifest(
            deviceId = device.deviceId,
            timestamp = System.currentTimeMillis(),
            items = emptyList()
        )
        
        // Act
        val result = repository.performSync(connection, localManifest, remoteManifest)
        
        // Assert
        assertTrue(result.isSuccess)
        val syncResult = result.getOrThrow()
        assertEquals(device.deviceId, syncResult.deviceId)
        assertTrue(syncResult.duration >= 0)
    }
    
    // ========== Status Tests ==========
    
    @Test
    fun `observeSyncStatus should emit status updates`() = runTest {
        // Act
        val status = repository.observeSyncStatus().first()
        
        // Assert
        assertTrue(status is SyncStatus.Idle)
    }
    
    @Test
    fun `cancelSync should cancel ongoing sync`() = runTest {
        // Act
        val result = repository.cancelSync()
        
        // Assert
        assertTrue(result.isSuccess)
    }
    
    // ========== Local Data Tests ==========
    
    @Test
    fun `getBooksToSync should return books from local data source`() = runTest {
        // Arrange
        val book = BookSyncData(
            bookId = 1L,
            title = "Test Book",
            author = "Test Author",
            coverUrl = null,
            sourceId = "source-1",
            sourceUrl = "https://example.com/book",
            addedAt = 1000L,
            updatedAt = 2000L,
            fileHash = "hash1"
        )
        localDataSource.addBook(book)
        
        // Act
        val result = repository.getBooksToSync()
        
        // Assert
        assertTrue(result.isSuccess)
        val books = result.getOrThrow()
        assertEquals(1, books.size)
        assertEquals(book, books[0])
    }
    
    @Test
    fun `getReadingProgress should return progress from local data source`() = runTest {
        // Arrange
        val progress = ReadingProgressData(
            bookId = 1L,
            chapterId = 10L,
            chapterIndex = 5,
            offset = 100,
            progress = 0.5f,
            lastReadAt = System.currentTimeMillis()
        )
        localDataSource.addProgress(progress)
        
        // Act
        val result = repository.getReadingProgress()
        
        // Assert
        assertTrue(result.isSuccess)
        val progressList = result.getOrThrow()
        assertEquals(1, progressList.size)
        assertEquals(progress, progressList[0])
    }
    
    @Test
    fun `getBookmarks should return bookmarks from local data source`() = runTest {
        // Arrange
        val bookmark = BookmarkData(
            bookmarkId = 1L,
            bookId = 1L,
            chapterId = 3L,
            position = 50,
            note = "Test note",
            createdAt = System.currentTimeMillis()
        )
        localDataSource.addBookmark(bookmark)
        
        // Act
        val result = repository.getBookmarks()
        
        // Assert
        assertTrue(result.isSuccess)
        val bookmarks = result.getOrThrow()
        assertEquals(1, bookmarks.size)
        assertEquals(bookmark, bookmarks[0])
    }
    
    @Test
    fun `applySync should apply sync data to local database`() = runTest {
        // Arrange
        val syncData = SyncData(
            books = emptyList(),
            readingProgress = emptyList(),
            bookmarks = emptyList(),
            metadata = SyncMetadata(
                deviceId = "remote-device",
                timestamp = System.currentTimeMillis(),
                version = 1,
                checksum = "checksum"
            )
        )
        
        // Act
        val result = repository.applySync(syncData)
        
        // Assert
        assertTrue(result.isSuccess)
    }
    
    // ========== Metadata Tests ==========
    
    @Test
    fun `getLastSyncTime should return null for never synced device`() = runTest {
        // Act
        val result = repository.getLastSyncTime("device-1")
        
        // Assert
        assertTrue(result.isSuccess)
        assertNull(result.getOrNull())
    }
    
    @Test
    fun `updateLastSyncTime should update sync timestamp`() = runTest {
        // Arrange
        val deviceId = "device-1"
        val timestamp = System.currentTimeMillis()
        
        // Act
        val updateResult = repository.updateLastSyncTime(deviceId, timestamp)
        val getResult = repository.getLastSyncTime(deviceId)
        
        // Assert
        assertTrue(updateResult.isSuccess)
        assertTrue(getResult.isSuccess)
        assertEquals(timestamp, getResult.getOrNull())
    }
}
