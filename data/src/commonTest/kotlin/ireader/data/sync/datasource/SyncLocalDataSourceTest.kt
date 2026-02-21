package ireader.data.sync.datasource

import ireader.domain.models.sync.DeviceType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests for SyncLocalDataSource interface.
 * 
 * Following TDD: These tests define the contract for local database operations.
 */
class SyncLocalDataSourceTest {

    @Test
    fun `getSyncMetadata should return metadata for device`() = runTest {
        // Arrange
        val dataSource = FakeSyncLocalDataSource()
        val deviceId = "device-123"
        val metadata = SyncMetadataEntity(
            deviceId = deviceId,
            deviceName = "Test Device",
            deviceType = "ANDROID",
            lastSyncTime = System.currentTimeMillis(),
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        dataSource.upsertSyncMetadata(metadata)

        // Act
        val result = dataSource.getSyncMetadata(deviceId)

        // Assert
        assertNotNull(result)
        assertEquals(deviceId, result.deviceId)
        assertEquals("Test Device", result.deviceName)
    }

    @Test
    fun `getSyncMetadata should return null for non-existent device`() = runTest {
        // Arrange
        val dataSource = FakeSyncLocalDataSource()

        // Act
        val result = dataSource.getSyncMetadata("non-existent")

        // Assert
        assertNull(result)
    }

    @Test
    fun `upsertSyncMetadata should insert new metadata`() = runTest {
        // Arrange
        val dataSource = FakeSyncLocalDataSource()
        val metadata = SyncMetadataEntity(
            deviceId = "device-123",
            deviceName = "Test Device",
            deviceType = "ANDROID",
            lastSyncTime = System.currentTimeMillis(),
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        // Act
        dataSource.upsertSyncMetadata(metadata)
        val result = dataSource.getSyncMetadata("device-123")

        // Assert
        assertNotNull(result)
        assertEquals("device-123", result.deviceId)
    }

    @Test
    fun `upsertSyncMetadata should update existing metadata`() = runTest {
        // Arrange
        val dataSource = FakeSyncLocalDataSource()
        val metadata1 = SyncMetadataEntity(
            deviceId = "device-123",
            deviceName = "Old Name",
            deviceType = "ANDROID",
            lastSyncTime = 1000L,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        val metadata2 = metadata1.copy(
            deviceName = "New Name",
            lastSyncTime = 2000L
        )
        dataSource.upsertSyncMetadata(metadata1)

        // Act
        dataSource.upsertSyncMetadata(metadata2)
        val result = dataSource.getSyncMetadata("device-123")

        // Assert
        assertNotNull(result)
        assertEquals("New Name", result.deviceName)
        assertEquals(2000L, result.lastSyncTime)
    }

    @Test
    fun `getTrustedDevice should return trusted device`() = runTest {
        // Arrange
        val dataSource = FakeSyncLocalDataSource()
        val deviceId = "device-123"
        val trustedDevice = TrustedDeviceEntity(
            deviceId = deviceId,
            deviceName = "Trusted Device",
            pairedAt = System.currentTimeMillis(),
            expiresAt = System.currentTimeMillis() + 86400000,
            isActive = true
        )
        dataSource.addTrustedDevice(trustedDevice)

        // Act
        val result = dataSource.getTrustedDevice(deviceId)

        // Assert
        assertNotNull(result)
        assertEquals(deviceId, result.deviceId)
        assertTrue(result.isActive)
    }

    @Test
    fun `getActiveTrustedDevices should return only active devices`() = runTest {
        // Arrange
        val dataSource = FakeSyncLocalDataSource()
        val active = TrustedDeviceEntity(
            deviceId = "active-1",
            deviceName = "Active Device",
            pairedAt = System.currentTimeMillis(),
            expiresAt = System.currentTimeMillis() + 86400000,
            isActive = true
        )
        val inactive = TrustedDeviceEntity(
            deviceId = "inactive-1",
            deviceName = "Inactive Device",
            pairedAt = System.currentTimeMillis(),
            expiresAt = System.currentTimeMillis() + 86400000,
            isActive = false
        )
        dataSource.addTrustedDevice(active)
        dataSource.addTrustedDevice(inactive)

        // Act
        val result = dataSource.getActiveTrustedDevices().first()

        // Assert
        assertEquals(1, result.size)
        assertEquals("active-1", result[0].deviceId)
    }

    @Test
    fun `insertSyncLog should add log entry`() = runTest {
        // Arrange
        val dataSource = FakeSyncLocalDataSource()
        val log = SyncLogEntity(
            id = 1,
            syncId = "sync-123",
            deviceId = "device-123",
            status = "SUCCESS",
            itemsSynced = 10,
            duration = 5000,
            errorMessage = null,
            timestamp = System.currentTimeMillis()
        )

        // Act
        dataSource.insertSyncLog(log)
        val result = dataSource.getSyncLogById(1)

        // Assert
        assertNotNull(result)
        assertEquals("sync-123", result.syncId)
        assertEquals("SUCCESS", result.status)
        assertEquals(10, result.itemsSynced)
    }

    @Test
    fun `getSyncLogsByDevice should return logs for specific device`() = runTest {
        // Arrange
        val dataSource = FakeSyncLocalDataSource()
        val log1 = SyncLogEntity(
            id = 1,
            syncId = "sync-1",
            deviceId = "device-123",
            status = "SUCCESS",
            itemsSynced = 10,
            duration = 5000,
            errorMessage = null,
            timestamp = System.currentTimeMillis()
        )
        val log2 = SyncLogEntity(
            id = 2,
            syncId = "sync-2",
            deviceId = "device-456",
            status = "SUCCESS",
            itemsSynced = 5,
            duration = 3000,
            errorMessage = null,
            timestamp = System.currentTimeMillis()
        )
        dataSource.insertSyncLog(log1)
        dataSource.insertSyncLog(log2)

        // Act
        val result = dataSource.getSyncLogsByDevice("device-123").first()

        // Assert
        assertEquals(1, result.size)
        assertEquals("device-123", result[0].deviceId)
    }

    @Test
    fun `deleteSyncMetadata should remove metadata`() = runTest {
        // Arrange
        val dataSource = FakeSyncLocalDataSource()
        val metadata = SyncMetadataEntity(
            deviceId = "device-123",
            deviceName = "Test Device",
            deviceType = "ANDROID",
            lastSyncTime = System.currentTimeMillis(),
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        dataSource.upsertSyncMetadata(metadata)

        // Act
        dataSource.deleteSyncMetadata("device-123")
        val result = dataSource.getSyncMetadata("device-123")

        // Assert
        assertNull(result)
    }

    @Test
    fun `deactivateTrustedDevice should set isActive to false`() = runTest {
        // Arrange
        val dataSource = FakeSyncLocalDataSource()
        val trustedDevice = TrustedDeviceEntity(
            deviceId = "device-123",
            deviceName = "Trusted Device",
            pairedAt = System.currentTimeMillis(),
            expiresAt = System.currentTimeMillis() + 86400000,
            isActive = true
        )
        dataSource.addTrustedDevice(trustedDevice)

        // Act
        dataSource.deactivateTrustedDevice("device-123")
        val result = dataSource.getTrustedDevice("device-123")

        // Assert
        assertNotNull(result)
        assertEquals(false, result.isActive)
    }
}
