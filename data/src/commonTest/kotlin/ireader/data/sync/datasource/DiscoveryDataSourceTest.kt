package ireader.data.sync.datasource

import ireader.domain.models.sync.DeviceInfo
import ireader.domain.models.sync.DeviceType
import ireader.domain.models.sync.DiscoveredDevice
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tests for DiscoveryDataSource interface.
 * 
 * Following TDD: These tests define the contract that implementations must follow.
 */
class DiscoveryDataSourceTest {

    @Test
    fun `startBroadcasting should return success when service starts`() = runTest {
        // Arrange
        val dataSource = FakeDiscoveryDataSource()
        val deviceInfo = createTestDeviceInfo()

        // Act
        val result = dataSource.startBroadcasting(deviceInfo)

        // Assert
        assertTrue(result.isSuccess)
        assertTrue(dataSource.isBroadcasting)
    }

    @Test
    fun `stopBroadcasting should return success when service stops`() = runTest {
        // Arrange
        val dataSource = FakeDiscoveryDataSource()
        val deviceInfo = createTestDeviceInfo()
        dataSource.startBroadcasting(deviceInfo)

        // Act
        val result = dataSource.stopBroadcasting()

        // Assert
        assertTrue(result.isSuccess)
        assertFalse(dataSource.isBroadcasting)
    }

    @Test
    fun `startDiscovery should return success when discovery starts`() = runTest {
        // Arrange
        val dataSource = FakeDiscoveryDataSource()

        // Act
        val result = dataSource.startDiscovery()

        // Assert
        assertTrue(result.isSuccess)
        assertTrue(dataSource.isDiscovering)
    }

    @Test
    fun `stopDiscovery should return success when discovery stops`() = runTest {
        // Arrange
        val dataSource = FakeDiscoveryDataSource()
        dataSource.startDiscovery()

        // Act
        val result = dataSource.stopDiscovery()

        // Assert
        assertTrue(result.isSuccess)
        assertFalse(dataSource.isDiscovering)
    }

    @Test
    fun `observeDiscoveredDevices should emit discovered devices`() = runTest {
        // Arrange
        val dataSource = FakeDiscoveryDataSource()
        val device = createTestDiscoveredDevice()

        // Act
        val initialDevices = dataSource.observeDiscoveredDevices().first()
        dataSource.addDiscoveredDevice(device)
        val devicesAfterAdd = dataSource.observeDiscoveredDevices().first()

        // Assert
        assertEquals(emptyList(), initialDevices)
        assertEquals(listOf(device), devicesAfterAdd)
    }

    @Test
    fun `verifyDevice should return true for reachable device`() = runTest {
        // Arrange
        val dataSource = FakeDiscoveryDataSource()
        val deviceInfo = createTestDeviceInfo()
        dataSource.setDeviceReachable(deviceInfo.deviceId, true)

        // Act
        val result = dataSource.verifyDevice(deviceInfo)

        // Assert
        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull() == true)
    }

    @Test
    fun `verifyDevice should return false for unreachable device`() = runTest {
        // Arrange
        val dataSource = FakeDiscoveryDataSource()
        val deviceInfo = createTestDeviceInfo()
        dataSource.setDeviceReachable(deviceInfo.deviceId, false)

        // Act
        val result = dataSource.verifyDevice(deviceInfo)

        // Assert
        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull() == false)
    }

    @Test
    fun `stopBroadcasting should succeed even if not broadcasting`() = runTest {
        // Arrange
        val dataSource = FakeDiscoveryDataSource()

        // Act
        val result = dataSource.stopBroadcasting()

        // Assert
        assertTrue(result.isSuccess)
    }

    @Test
    fun `stopDiscovery should succeed even if not discovering`() = runTest {
        // Arrange
        val dataSource = FakeDiscoveryDataSource()

        // Act
        val result = dataSource.stopDiscovery()

        // Assert
        assertTrue(result.isSuccess)
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

    private fun createTestDiscoveredDevice(): DiscoveredDevice {
        return DiscoveredDevice(
            deviceInfo = createTestDeviceInfo(),
            isReachable = true,
            discoveredAt = System.currentTimeMillis()
        )
    }
}
