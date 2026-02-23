package ireader.data.sync.datasource

import ireader.domain.models.sync.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlin.test.*
import kotlin.time.Duration.Companion.seconds

/**
 * Unit tests for KtorTransferDataSource WebSocket implementation.
 * 
 * These tests verify that the WebSocket pattern (suspending iteration)
 * works correctly for both client and server roles.
 */
class KtorTransferDataSourceTest {
    
    private lateinit var dataSource: KtorTransferDataSource
    
    @BeforeTest
    fun setup() {
        dataSource = KtorTransferDataSource()
    }
    
    @AfterTest
    fun teardown() = runTest {
        dataSource.closeConnection()
        dataSource.stopServer()
    }
    
    // ========== Server Tests ==========
    
    @Test
    fun `startServer should bind to port successfully`() = runTest(timeout = 10.seconds) {
        // Act
        val result = dataSource.startServer(8081)
        
        // Assert
        assertTrue(result.isSuccess, "Server should start successfully")
        assertEquals(8081, result.getOrNull())
    }
    
    @Test
    fun `startServer should allow restart on same port`() = runTest(timeout = 10.seconds) {
        // Arrange - Start server first time
        val firstResult = dataSource.startServer(8082)
        assertTrue(firstResult.isSuccess, "First server start should succeed")
        
        // Act - Start server again on same port (should restart)
        val secondResult = dataSource.startServer(8082)
        
        // Assert - Should succeed because implementation allows restart
        assertTrue(secondResult.isSuccess, "Should allow restart on same port")
        assertEquals(8082, secondResult.getOrNull())
    }
    
    @Test
    fun `stopServer should close server successfully`() = runTest(timeout = 10.seconds) {
        // Arrange
        dataSource.startServer(8083).getOrThrow()
        
        // Act
        val result = dataSource.stopServer()
        
        // Assert
        assertTrue(result.isSuccess, "Server should stop successfully")
    }
    
    // ========== Client Tests ==========
    
    @Test
    fun `connectToDevice should fail when server not running`() = runTest(timeout = 10.seconds) {
        // Arrange
        val deviceInfo = DeviceInfo(
            deviceId = "test-device",
            deviceName = "Test Device",
            deviceType = DeviceType.DESKTOP,
            appVersion = "1.0.0",
            ipAddress = "127.0.0.1",
            port = 9999, // Port with no server
            lastSeen = System.currentTimeMillis()
        )
        
        // Act
        val result = dataSource.connectToDevice(deviceInfo)
        
        // Assert
        assertTrue(result.isFailure, "Should fail when server is not running")
    }
    
    @Test
    fun `connectToDevice should succeed when server is running`() = runTest(timeout = 15.seconds) {
        // Arrange - Start server
        val port = 8084
        dataSource.startServer(port).getOrThrow()
        
        // Create client data source
        val clientDataSource = KtorTransferDataSource()
        
        val deviceInfo = DeviceInfo(
            deviceId = "test-server",
            deviceName = "Test Server",
            deviceType = DeviceType.DESKTOP,
            appVersion = "1.0.0",
            ipAddress = "127.0.0.1",
            port = port,
            lastSeen = System.currentTimeMillis()
        )
        
        try {
            // Act
            val result = clientDataSource.connectToDevice(deviceInfo)
            
            // Assert
            assertTrue(result.isSuccess, "Client should connect successfully")
            
            // Verify connection is active
            delay(1000) // Give time for session to establish
            assertTrue(clientDataSource.hasActiveConnection(), "Client should have active connection")
            assertTrue(dataSource.hasActiveConnection(), "Server should have active connection")
        } finally {
            clientDataSource.closeConnection()
        }
    }
    
    // ========== Data Transfer Tests ==========
    
    @Test
    fun `sendData and receiveData should transfer data successfully`() = runTest(timeout = 20.seconds) {
        // Arrange - Start server
        val port = 8085
        dataSource.startServer(port).getOrThrow()
        
        // Create client
        val clientDataSource = KtorTransferDataSource()
        
        val deviceInfo = DeviceInfo(
            deviceId = "test-server",
            deviceName = "Test Server",
            deviceType = DeviceType.DESKTOP,
            appVersion = "1.0.0",
            ipAddress = "127.0.0.1",
            port = port,
            lastSeen = System.currentTimeMillis()
        )
        
        clientDataSource.connectToDevice(deviceInfo).getOrThrow()
        delay(1000) // Wait for connection to establish
        
        try {
            // Create test data
            val testBook = BookSyncData(
                bookId = 1L,
                title = "Test Book",
                author = "Test Author",
                coverUrl = "https://example.com/cover.jpg",
                sourceId = "test-source",
                sourceUrl = "https://example.com/book",
                addedAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
                fileHash = null
            )
            
            val metadata = SyncMetadata(
                deviceId = "test-device",
                timestamp = System.currentTimeMillis(),
                version = 1,
                checksum = "test-checksum"
            )
            
            val syncData = SyncData(
                books = listOf(testBook),
                readingProgress = emptyList(),
                bookmarks = emptyList(),
                metadata = metadata
            )
            
            // Act - Send from client, receive on server
            val sendJob = launch {
                val sendResult = clientDataSource.sendData(syncData)
                assertTrue(sendResult.isSuccess, "Send should succeed")
            }
            
            val receiveJob = launch {
                val receiveResult = dataSource.receiveData()
                assertTrue(receiveResult.isSuccess, "Receive should succeed")
                
                val receivedData = receiveResult.getOrThrow()
                assertEquals(1, receivedData.books.size, "Should receive 1 book")
                assertEquals("Test Book", receivedData.books[0].title, "Book title should match")
                assertEquals("Test Author", receivedData.books[0].author, "Book author should match")
            }
            
            // Wait for both operations to complete
            sendJob.join()
            receiveJob.join()
        } finally {
            clientDataSource.closeConnection()
        }
    }
    
    @Test
    fun `sendData should fail when no connection`() = runTest(timeout = 10.seconds) {
        // Arrange
        val metadata = SyncMetadata(
            deviceId = "test-device",
            timestamp = System.currentTimeMillis(),
            version = 1,
            checksum = "test-checksum"
        )
        
        val syncData = SyncData(
            books = emptyList(),
            readingProgress = emptyList(),
            bookmarks = emptyList(),
            metadata = metadata
        )
        
        // Act
        val result = dataSource.sendData(syncData)
        
        // Assert
        assertTrue(result.isFailure, "Send should fail when no connection")
    }
    
    @Test
    fun `receiveData should timeout when no data sent`() = runTest(timeout = 35.seconds) {
        // Arrange - Start server
        val port = 8086
        dataSource.startServer(port).getOrThrow()
        
        // Create client
        val clientDataSource = KtorTransferDataSource()
        
        val deviceInfo = DeviceInfo(
            deviceId = "test-server",
            deviceName = "Test Server",
            deviceType = DeviceType.DESKTOP,
            appVersion = "1.0.0",
            ipAddress = "127.0.0.1",
            port = port,
            lastSeen = System.currentTimeMillis()
        )
        
        clientDataSource.connectToDevice(deviceInfo).getOrThrow()
        delay(1000) // Wait for connection to establish
        
        try {
            // Act - Try to receive without sending
            val result = dataSource.receiveData()
            
            // Assert
            assertTrue(result.isFailure, "Receive should timeout when no data sent")
        } finally {
            clientDataSource.closeConnection()
        }
    }
    
    // ========== Connection State Tests ==========
    
    @Test
    fun `hasActiveConnection should return false initially`() = runTest {
        // Act & Assert
        assertFalse(dataSource.hasActiveConnection(), "Should not have active connection initially")
    }
    
    @Test
    fun `hasActiveConnection should return true after server starts and client connects`() = runTest(timeout = 15.seconds) {
        // Arrange
        val port = 8087
        dataSource.startServer(port).getOrThrow()
        
        val clientDataSource = KtorTransferDataSource()
        val deviceInfo = DeviceInfo(
            deviceId = "test-server",
            deviceName = "Test Server",
            deviceType = DeviceType.DESKTOP,
            appVersion = "1.0.0",
            ipAddress = "127.0.0.1",
            port = port,
            lastSeen = System.currentTimeMillis()
        )
        
        clientDataSource.connectToDevice(deviceInfo).getOrThrow()
        delay(1000) // Wait for connection to establish
        
        try {
            // Act & Assert
            assertTrue(dataSource.hasActiveConnection(), "Server should have active connection")
            assertTrue(clientDataSource.hasActiveConnection(), "Client should have active connection")
        } finally {
            clientDataSource.closeConnection()
        }
    }
    
    @Test
    fun `hasActiveConnection should return false after closeConnection`() = runTest(timeout = 15.seconds) {
        // Arrange
        val port = 8088
        dataSource.startServer(port).getOrThrow()
        
        val clientDataSource = KtorTransferDataSource()
        val deviceInfo = DeviceInfo(
            deviceId = "test-server",
            deviceName = "Test Server",
            deviceType = DeviceType.DESKTOP,
            appVersion = "1.0.0",
            ipAddress = "127.0.0.1",
            port = port,
            lastSeen = System.currentTimeMillis()
        )
        
        clientDataSource.connectToDevice(deviceInfo).getOrThrow()
        delay(1000) // Wait for connection to establish
        
        // Act
        clientDataSource.closeConnection()
        delay(500) // Give time for cleanup
        
        // Assert
        assertFalse(clientDataSource.hasActiveConnection(), "Client should not have active connection after close")
    }
}
