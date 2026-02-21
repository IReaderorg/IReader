package ireader.data.sync.datasource

import ireader.domain.models.sync.DeviceInfo
import ireader.domain.models.sync.DeviceType
import ireader.domain.models.sync.SyncData
import ireader.domain.models.sync.SyncMetadata
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Tests for TransferDataSource interface.
 * 
 * Following TDD: These tests define the contract for data transfer implementations.
 */
class TransferDataSourceTest {

    @Test
    fun `startServer should return success and port number`() = runTest {
        // Arrange
        val dataSource = FakeTransferDataSource()
        val port = 8080

        // Act
        val result = dataSource.startServer(port)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(port, result.getOrNull())
        assertTrue(dataSource.isServerRunning)
    }

    @Test
    fun `stopServer should return success when server stops`() = runTest {
        // Arrange
        val dataSource = FakeTransferDataSource()
        dataSource.startServer(8080)

        // Act
        val result = dataSource.stopServer()

        // Assert
        assertTrue(result.isSuccess)
        assertFalse(dataSource.isServerRunning)
    }

    @Test
    fun `connectToDevice should return success when connection established`() = runTest {
        // Arrange
        val dataSource = FakeTransferDataSource()
        val deviceInfo = createTestDeviceInfo()

        // Act
        val result = dataSource.connectToDevice(deviceInfo)

        // Assert
        assertTrue(result.isSuccess)
        assertTrue(dataSource.isConnected)
    }

    @Test
    fun `disconnectFromDevice should return success when disconnected`() = runTest {
        // Arrange
        val dataSource = FakeTransferDataSource()
        val deviceInfo = createTestDeviceInfo()
        dataSource.connectToDevice(deviceInfo)

        // Act
        val result = dataSource.disconnectFromDevice()

        // Assert
        assertTrue(result.isSuccess)
        assertFalse(dataSource.isConnected)
    }

    @Test
    fun `sendData should return success when data sent`() = runTest {
        // Arrange
        val dataSource = FakeTransferDataSource()
        val syncData = createTestSyncData()

        // Act
        val result = dataSource.sendData(syncData)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(syncData, dataSource.lastSentData)
    }

    @Test
    fun `receiveData should return success with received data`() = runTest {
        // Arrange
        val dataSource = FakeTransferDataSource()
        val expectedData = createTestSyncData()
        dataSource.setDataToReceive(expectedData)

        // Act
        val result = dataSource.receiveData()

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(expectedData, result.getOrNull())
    }

    @Test
    fun `observeTransferProgress should emit progress updates`() = runTest {
        // Arrange
        val dataSource = FakeTransferDataSource()

        // Act
        val initialProgress = dataSource.observeTransferProgress().first()
        dataSource.updateProgress(0.5f)
        val updatedProgress = dataSource.observeTransferProgress().first()

        // Assert
        assertEquals(0.0f, initialProgress)
        assertEquals(0.5f, updatedProgress)
    }

    @Test
    fun `closeConnection should return success`() = runTest {
        // Arrange
        val dataSource = FakeTransferDataSource()
        val deviceInfo = createTestDeviceInfo()
        dataSource.connectToDevice(deviceInfo)

        // Act
        val result = dataSource.closeConnection()

        // Assert
        assertTrue(result.isSuccess)
        assertFalse(dataSource.isConnected)
    }

    @Test
    fun `stopServer should succeed even if server not running`() = runTest {
        // Arrange
        val dataSource = FakeTransferDataSource()

        // Act
        val result = dataSource.stopServer()

        // Assert
        assertTrue(result.isSuccess)
    }

    @Test
    fun `disconnectFromDevice should succeed even if not connected`() = runTest {
        // Arrange
        val dataSource = FakeTransferDataSource()

        // Act
        val result = dataSource.disconnectFromDevice()

        // Assert
        assertTrue(result.isSuccess)
    }

    @Test
    fun `sendData should handle large data sets`() = runTest {
        // Arrange
        val dataSource = FakeTransferDataSource()
        val largeData = createTestSyncData(bookCount = 100)

        // Act
        val result = dataSource.sendData(largeData)

        // Assert
        assertTrue(result.isSuccess)
        assertEquals(100, dataSource.lastSentData?.books?.size)
    }

    @Test
    fun `receiveData should handle timeout gracefully`() = runTest {
        // Arrange
        val dataSource = FakeTransferDataSource()
        dataSource.simulateTimeout(true)

        // Act
        val result = dataSource.receiveData()

        // Assert
        assertTrue(result.isFailure)
        assertNotNull(result.exceptionOrNull())
    }

    // Helper functions
    private fun createTestDeviceInfo(): DeviceInfo {
        return DeviceInfo(
            deviceId = "test-device-123",
            deviceName = "Test Device",
            deviceType = DeviceType.ANDROID,
            appVersion = "1.0.0",
            ipAddress = "192.168.1.100",
            port = 8080,
            lastSeen = System.currentTimeMillis()
        )
    }

    private fun createTestSyncData(bookCount: Int = 1): SyncData {
        val currentTime = System.currentTimeMillis()
        val metadata = SyncMetadata(
            deviceId = "device-123",
            timestamp = currentTime,
            version = 1,
            checksum = "abc123"
        )
        
        val books = List(bookCount) { index ->
            ireader.domain.models.sync.BookSyncData(
                bookId = index.toLong(),
                title = "Test Book $index",
                author = "Test Author",
                coverUrl = null,
                sourceId = "source-1",
                sourceUrl = "https://example.com/book-$index",
                addedAt = currentTime,
                updatedAt = currentTime,
                fileHash = null
            )
        }
        
        return SyncData(
            metadata = metadata,
            books = books,
            readingProgress = emptyList(),
            bookmarks = emptyList()
        )
    }
}
