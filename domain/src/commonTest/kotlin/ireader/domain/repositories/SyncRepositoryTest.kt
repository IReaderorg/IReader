package ireader.domain.repositories

import ireader.domain.models.sync.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests for SyncRepository interface contract.
 * These tests use a fake implementation to verify the interface design.
 */
class SyncRepositoryTest {

    @Test
    fun `Connection should be created with valid data`() {
        // Arrange
        val deviceId = "test-device-123"
        val deviceName = "My Phone"

        // Act
        val connection = Connection(deviceId, deviceName)

        // Assert
        assertEquals(deviceId, connection.deviceId)
        assertEquals(deviceName, connection.deviceName)
    }

    @Test
    fun `SyncResult should be created with valid data`() {
        // Arrange
        val deviceId = "test-device-123"
        val itemsSynced = 42
        val duration = 5000L

        // Act
        val result = SyncResult(deviceId, itemsSynced, duration)

        // Assert
        assertEquals(deviceId, result.deviceId)
        assertEquals(itemsSynced, result.itemsSynced)
        assertEquals(duration, result.duration)
    }

    @Test
    fun `FakeSyncRepository should implement all interface methods`() = runTest {
        // Arrange
        val repository = FakeSyncRepository()

        // Act & Assert - Verify all methods can be called
        assertTrue(repository.startDiscovery().isSuccess)
        assertTrue(repository.stopDiscovery().isSuccess)
        assertTrue(repository.observeDiscoveredDevices() is Flow)
        assertTrue(repository.getDeviceInfo("test").isSuccess)
        assertTrue(repository.connectToDevice(createTestDeviceInfo()).isSuccess)
        assertTrue(repository.disconnectFromDevice(createTestConnection()).isSuccess)
        assertTrue(repository.exchangeManifests(createTestConnection()).isSuccess)
        assertTrue(repository.performSync(createTestConnection(), createTestManifest(), createTestManifest()).isSuccess)
        assertTrue(repository.observeSyncStatus() is Flow)
        assertTrue(repository.cancelSync().isSuccess)
        assertTrue(repository.getBooksToSync().isSuccess)
        assertTrue(repository.getReadingProgress().isSuccess)
        assertTrue(repository.getBookmarks().isSuccess)
        assertTrue(repository.applySync(createTestSyncData()).isSuccess)
        assertTrue(repository.getLastSyncTime("test").isSuccess)
        assertTrue(repository.updateLastSyncTime("test", 1000L).isSuccess)
    }

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

    private fun createTestConnection(): Connection {
        return Connection("test-device-123", "Test Device")
    }

    private fun createTestManifest(): SyncManifest {
        return SyncManifest(
            deviceId = "test-device-123",
            timestamp = System.currentTimeMillis(),
            items = emptyList()
        )
    }

    private fun createTestSyncData(): SyncData {
        return SyncData(
            books = emptyList(),
            readingProgress = emptyList(),
            bookmarks = emptyList(),
            metadata = SyncMetadata(
                deviceId = "test-device-123",
                timestamp = System.currentTimeMillis(),
                version = 1,
                checksum = "abc123"
            )
        )
    }
}

/**
 * Fake implementation of SyncRepository for testing.
 */
class FakeSyncRepository(
    private val shouldFail: Boolean = false,
    private val shouldFailConnection: Boolean = false,
    private val hasConflicts: Boolean = false
) : SyncRepository {
    override suspend fun startDiscovery(): Result<Unit> = if (shouldFail) {
        Result.failure(Exception("Start discovery failed"))
    } else {
        Result.success(Unit)
    }
    override suspend fun stopDiscovery(): Result<Unit> = if (shouldFail) {
        Result.failure(Exception("Stop discovery failed"))
    } else {
        Result.success(Unit)
    }
    override fun observeDiscoveredDevices(): Flow<List<DiscoveredDevice>> = flowOf(emptyList())
    override suspend fun getDeviceInfo(deviceId: String): Result<DeviceInfo> = if (shouldFail) {
        Result.failure(Exception("Device not found"))
    } else {
        Result.success(
            DeviceInfo("test-device-123", "Test", DeviceType.ANDROID, "1.0.0", "192.168.1.1", 8080, System.currentTimeMillis())
        )
    }
    override suspend fun connectToDevice(device: DeviceInfo): Result<Connection> = if (shouldFailConnection) {
        Result.failure(Exception("Connection failed"))
    } else {
        Result.success(Connection(device.deviceId, device.deviceName))
    }
    override suspend fun disconnectFromDevice(connection: Connection): Result<Unit> = Result.success(Unit)
    override suspend fun exchangeManifests(connection: Connection): Result<Pair<SyncManifest, SyncManifest>> {
        val manifest = SyncManifest("test", System.currentTimeMillis(), emptyList())
        return Result.success(Pair(manifest, manifest))
    }
    override suspend fun performSync(
        connection: Connection,
        localManifest: SyncManifest,
        remoteManifest: SyncManifest
    ): Result<SyncResult> = Result.success(SyncResult(connection.deviceId, 0, 0L))
    override fun observeSyncStatus(): Flow<SyncStatus> = flowOf(SyncStatus.Idle)
    override suspend fun cancelSync(): Result<Unit> = Result.success(Unit)
    override suspend fun getBooksToSync(): Result<List<BookSyncData>> = Result.success(
        if (hasConflicts) {
            listOf(
                BookSyncData(123L, "Book", "Author", null, "src", "url", 1000L, 1000L, "hash1")
            )
        } else {
            emptyList()
        }
    )
    override suspend fun getReadingProgress(): Result<List<ReadingProgressData>> = Result.success(
        if (hasConflicts) {
            listOf(
                ReadingProgressData(123L, 456L, 5, 1024, 0.5f, 1000L)
            )
        } else {
            emptyList()
        }
    )
    override suspend fun getBookmarks(): Result<List<BookmarkData>> = Result.success(emptyList())
    override suspend fun applySync(data: SyncData): Result<Unit> = Result.success(Unit)
    override suspend fun getLastSyncTime(deviceId: String): Result<Long?> = Result.success(null)
    override suspend fun updateLastSyncTime(deviceId: String, timestamp: Long): Result<Unit> = Result.success(Unit)
}
