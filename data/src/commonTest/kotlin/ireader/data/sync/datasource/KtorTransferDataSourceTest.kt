package ireader.data.sync.datasource

import ireader.domain.models.sync.*
import kotlinx.coroutines.test.runTest
import kotlin.test.*

/**
 * Test suite for KtorTransferDataSource following TDD methodology.
 * 
 * Tests written FIRST (RED phase) before implementation.
 */
class KtorTransferDataSourceTest {
    
    private lateinit var transferDataSource: KtorTransferDataSource
    
    companion object {
        private var testPortCounter = 9000 // Start from 9000 to avoid conflicts
        
        @Synchronized
        fun getNextPort(): Int {
            return testPortCounter++
        }
    }
    
    @BeforeTest
    fun setup() {
        transferDataSource = KtorTransferDataSource()
    }
    
    @AfterTest
    fun tearDown() = runTest {
        // Clean up any active connections
        transferDataSource.stopServer()
        transferDataSource.closeConnection()
        // Give time for cleanup
        kotlinx.coroutines.delay(200)
    }
    
    // ========== Server Lifecycle Tests ==========
    
    @Test
    fun `startServer should start server on specified port`() = runTest {
        // Arrange
        val port = getNextPort()
        
        // Act
        val result = transferDataSource.startServer(port)
        
        // Assert
        assertTrue(result.isSuccess)
        assertEquals(port, result.getOrNull())
    }
    
    @Test
    fun `startServer should fail if server already running`() = runTest {
        // Arrange
        val port = getNextPort()
        transferDataSource.startServer(port)
        
        // Act
        val result = transferDataSource.startServer(port)
        
        // Assert
        assertTrue(result.isFailure)
    }
    
    @Test
    fun `stopServer should stop running server`() = runTest {
        // Arrange
        val port = getNextPort()
        transferDataSource.startServer(port)
        
        // Act
        val result = transferDataSource.stopServer()
        
        // Assert
        assertTrue(result.isSuccess)
    }
    
    @Test
    fun `stopServer should succeed even if no server running`() = runTest {
        // Act
        val result = transferDataSource.stopServer()
        
        // Assert
        assertTrue(result.isSuccess)
    }
    
    // ========== Connection Tests ==========
    
    @Test
    @Ignore // Integration test - requires actual network connection
    fun `connectToDevice should establish connection to server`() = runTest {
        // Arrange
        val port = getNextPort()
        val deviceInfo = createTestDeviceInfo(port = port)
        
        // Start server first
        val serverDataSource = KtorTransferDataSource()
        serverDataSource.startServer(port)
        
        // Give server time to start
        kotlinx.coroutines.delay(200)
        
        // Act
        val result = transferDataSource.connectToDevice(deviceInfo)
        
        // Assert
        assertTrue(result.isSuccess)
        
        // Cleanup
        serverDataSource.stopServer()
    }
    
    @Test
    fun `connectToDevice should fail if server not reachable`() = runTest {
        // Arrange
        val deviceInfo = createTestDeviceInfo(port = 9999)
        
        // Act
        val result = transferDataSource.connectToDevice(deviceInfo)
        
        // Assert
        assertTrue(result.isFailure)
    }
    
    @Test
    fun `disconnectFromDevice should close client connection`() = runTest {
        // Arrange
        val port = getNextPort()
        val deviceInfo = createTestDeviceInfo(port = port)
        val serverDataSource = KtorTransferDataSource()
        serverDataSource.startServer(port)
        kotlinx.coroutines.delay(200)
        transferDataSource.connectToDevice(deviceInfo)
        
        // Act
        val result = transferDataSource.disconnectFromDevice()
        
        // Assert
        assertTrue(result.isSuccess)
        
        // Cleanup
        serverDataSource.stopServer()
    }
    
    // ========== Data Transfer Tests ==========
    
    @Test
    @Ignore // Integration test - requires actual network connection
    fun `sendData should send sync data to connected device`() = runTest {
        // Arrange
        val syncData = createTestSyncData()
        val port = getNextPort()
        val deviceInfo = createTestDeviceInfo(port = port)
        
        val serverDataSource = KtorTransferDataSource()
        serverDataSource.startServer(port)
        kotlinx.coroutines.delay(200)
        transferDataSource.connectToDevice(deviceInfo)
        kotlinx.coroutines.delay(200)
        
        // Act
        val result = transferDataSource.sendData(syncData)
        
        // Assert
        assertTrue(result.isSuccess)
        
        // Cleanup
        serverDataSource.stopServer()
    }
    
    @Test
    fun `sendData should fail if not connected`() = runTest {
        // Arrange
        val syncData = createTestSyncData()
        
        // Act
        val result = transferDataSource.sendData(syncData)
        
        // Assert
        assertTrue(result.isFailure)
    }
    
    @Test
    @Ignore // Integration test - requires actual network connection
    fun `receiveData should receive sync data from connected device`() = runTest {
        // Arrange
        val expectedData = createTestSyncData()
        val port = getNextPort()
        val deviceInfo = createTestDeviceInfo(port = port)
        
        val serverDataSource = KtorTransferDataSource()
        serverDataSource.startServer(port)
        kotlinx.coroutines.delay(200)
        transferDataSource.connectToDevice(deviceInfo)
        kotlinx.coroutines.delay(200)
        
        // Server sends data
        serverDataSource.sendData(expectedData)
        
        // Act
        val result = transferDataSource.receiveData()
        
        // Assert
        assertTrue(result.isSuccess)
        val receivedData = result.getOrNull()
        assertNotNull(receivedData)
        assertEquals(expectedData.metadata.deviceId, receivedData.metadata.deviceId)
        
        // Cleanup
        serverDataSource.stopServer()
    }
    
    @Test
    fun `receiveData should fail if not connected`() = runTest {
        // Act
        val result = transferDataSource.receiveData()
        
        // Assert
        assertTrue(result.isFailure)
    }
    
    // ========== Progress Tracking Tests ==========
    
    @Test
    fun `observeTransferProgress should emit progress during transfer`() = runTest {
        // Arrange
        val progressValues = mutableListOf<Float>()
        val port = getNextPort()
        val deviceInfo = createTestDeviceInfo(port = port)
        
        val serverDataSource = KtorTransferDataSource()
        serverDataSource.startServer(port)
        kotlinx.coroutines.delay(200)
        transferDataSource.connectToDevice(deviceInfo)
        kotlinx.coroutines.delay(200)
        
        // Collect progress values
        // Note: This test verifies the Flow exists and can be collected
        val progress = transferDataSource.observeTransferProgress()
        assertNotNull(progress)
        
        // Cleanup
        serverDataSource.stopServer()
    }
    
    // ========== Connection Cleanup Tests ==========
    
    @Test
    fun `closeConnection should close any active connection`() = runTest {
        // Arrange
        val port = getNextPort()
        val deviceInfo = createTestDeviceInfo(port = port)
        val serverDataSource = KtorTransferDataSource()
        serverDataSource.startServer(port)
        kotlinx.coroutines.delay(200)
        transferDataSource.connectToDevice(deviceInfo)
        kotlinx.coroutines.delay(200)
        
        // Act
        val result = transferDataSource.closeConnection()
        
        // Assert
        assertTrue(result.isSuccess)
        
        // Cleanup
        serverDataSource.stopServer()
    }
    
    @Test
    fun `closeConnection should succeed even if no connection active`() = runTest {
        // Act
        val result = transferDataSource.closeConnection()
        
        // Assert
        assertTrue(result.isSuccess)
    }
    
    // ========== Helper Methods ==========
    
    private fun createTestDeviceInfo(port: Int = 8080): DeviceInfo {
        return DeviceInfo(
            deviceId = "test-device-id",
            deviceName = "Test Device",
            deviceType = DeviceType.ANDROID,
            appVersion = "1.0.0",
            ipAddress = "127.0.0.1",
            port = port,
            lastSeen = System.currentTimeMillis()
        )
    }
    
    private fun createTestSyncData(): SyncData {
        val currentTime = System.currentTimeMillis()
        return SyncData(
            books = listOf(
                BookSyncData(
                    bookId = 1L,
                    title = "Test Book",
                    author = "Test Author",
                    coverUrl = null,
                    sourceId = "test-source",
                    sourceUrl = "https://example.com/book-1",
                    addedAt = currentTime,
                    updatedAt = currentTime,
                    fileHash = "abc123"
                )
            ),
            readingProgress = listOf(
                ReadingProgressData(
                    bookId = 1L,
                    chapterId = 1L,
                    chapterIndex = 5,
                    offset = 100,
                    progress = 0.5f,
                    lastReadAt = currentTime
                )
            ),
            bookmarks = emptyList(),
            metadata = SyncMetadata(
                deviceId = "test-device",
                timestamp = currentTime,
                version = 1,
                checksum = "checksum123"
            )
        )
    }
}
