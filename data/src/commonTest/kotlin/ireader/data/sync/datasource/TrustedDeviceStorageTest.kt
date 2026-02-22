package ireader.data.sync.datasource

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Test suite for trusted device storage operations in SyncLocalDataSource.
 * 
 * Tests device trust management following TDD principles.
 */
class TrustedDeviceStorageTest {
    
    @Test
    fun `upsertTrustedDevice should insert new device`() = runTest {
        // Arrange
        val dataSource = FakeSyncLocalDataSource()
        val device = TrustedDeviceEntity(
            deviceId = "device-123",
            deviceName = "Test Device",
            pairedAt = System.currentTimeMillis(),
            expiresAt = System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000, // 30 days
            isActive = true
        )
        
        // Act
        dataSource.upsertTrustedDevice(device)
        
        // Assert
        val retrieved = dataSource.getTrustedDevice("device-123")
        assertNotNull(retrieved, "Device should be inserted")
        assertEquals(device.deviceId, retrieved.deviceId)
        assertEquals(device.deviceName, retrieved.deviceName)
        assertEquals(device.pairedAt, retrieved.pairedAt)
        assertEquals(device.expiresAt, retrieved.expiresAt)
        assertEquals(device.isActive, retrieved.isActive)
    }
    
    @Test
    fun `upsertTrustedDevice should update existing device`() = runTest {
        // Arrange
        val dataSource = FakeSyncLocalDataSource()
        val originalDevice = TrustedDeviceEntity(
            deviceId = "device-123",
            deviceName = "Original Name",
            pairedAt = 1000L,
            expiresAt = 2000L,
            isActive = true
        )
        dataSource.upsertTrustedDevice(originalDevice)
        
        val updatedDevice = TrustedDeviceEntity(
            deviceId = "device-123",
            deviceName = "Updated Name",
            pairedAt = 1000L,
            expiresAt = 3000L,
            isActive = true
        )
        
        // Act
        dataSource.upsertTrustedDevice(updatedDevice)
        
        // Assert
        val retrieved = dataSource.getTrustedDevice("device-123")
        assertNotNull(retrieved)
        assertEquals("Updated Name", retrieved.deviceName)
        assertEquals(3000L, retrieved.expiresAt)
    }
    
    @Test
    fun `getTrustedDevice should return null for non-existent device`() = runTest {
        // Arrange
        val dataSource = FakeSyncLocalDataSource()
        
        // Act
        val result = dataSource.getTrustedDevice("non-existent")
        
        // Assert
        assertNull(result, "Should return null for non-existent device")
    }
    
    @Test
    fun `getActiveTrustedDevices should return only active devices`() = runTest {
        // Arrange
        val dataSource = FakeSyncLocalDataSource()
        val activeDevice = TrustedDeviceEntity(
            deviceId = "active-1",
            deviceName = "Active Device",
            pairedAt = System.currentTimeMillis(),
            expiresAt = System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000,
            isActive = true
        )
        val inactiveDevice = TrustedDeviceEntity(
            deviceId = "inactive-1",
            deviceName = "Inactive Device",
            pairedAt = System.currentTimeMillis(),
            expiresAt = System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000,
            isActive = false
        )
        
        dataSource.upsertTrustedDevice(activeDevice)
        dataSource.upsertTrustedDevice(inactiveDevice)
        
        // Act
        val activeDevices = dataSource.getActiveTrustedDevices().first()
        
        // Assert
        assertEquals(1, activeDevices.size, "Should return only active devices")
        assertEquals("active-1", activeDevices[0].deviceId)
    }
    
    @Test
    fun `deactivateTrustedDevice should set isActive to false`() = runTest {
        // Arrange
        val dataSource = FakeSyncLocalDataSource()
        val device = TrustedDeviceEntity(
            deviceId = "device-123",
            deviceName = "Test Device",
            pairedAt = System.currentTimeMillis(),
            expiresAt = System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000,
            isActive = true
        )
        dataSource.upsertTrustedDevice(device)
        
        // Act
        dataSource.deactivateTrustedDevice("device-123")
        
        // Assert
        val retrieved = dataSource.getTrustedDevice("device-123")
        assertNotNull(retrieved)
        assertEquals(false, retrieved.isActive, "Device should be deactivated")
    }
    
    @Test
    fun `updateDeviceExpiration should update expiresAt timestamp`() = runTest {
        // Arrange
        val dataSource = FakeSyncLocalDataSource()
        val device = TrustedDeviceEntity(
            deviceId = "device-123",
            deviceName = "Test Device",
            pairedAt = 1000L,
            expiresAt = 2000L,
            isActive = true
        )
        dataSource.upsertTrustedDevice(device)
        
        val newExpiration = 5000L
        
        // Act
        dataSource.updateDeviceExpiration("device-123", newExpiration)
        
        // Assert
        val retrieved = dataSource.getTrustedDevice("device-123")
        assertNotNull(retrieved)
        assertEquals(newExpiration, retrieved.expiresAt, "Expiration should be updated")
    }
    
    @Test
    fun `deleteTrustedDevice should remove device from storage`() = runTest {
        // Arrange
        val dataSource = FakeSyncLocalDataSource()
        val device = TrustedDeviceEntity(
            deviceId = "device-123",
            deviceName = "Test Device",
            pairedAt = System.currentTimeMillis(),
            expiresAt = System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000,
            isActive = true
        )
        dataSource.upsertTrustedDevice(device)
        
        // Act
        dataSource.deleteTrustedDevice("device-123")
        
        // Assert
        val retrieved = dataSource.getTrustedDevice("device-123")
        assertNull(retrieved, "Device should be deleted")
    }
}
