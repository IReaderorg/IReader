package ireader.domain.models.sync

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DiscoveredDeviceTest {

    @Test
    fun `DiscoveredDevice should be created with valid data`() {
        // Arrange
        val deviceInfo = createTestDeviceInfo()
        val isReachable = true
        val discoveredAt = System.currentTimeMillis()

        // Act
        val discoveredDevice = DiscoveredDevice(
            deviceInfo = deviceInfo,
            isReachable = isReachable,
            discoveredAt = discoveredAt
        )

        // Assert
        assertEquals(deviceInfo, discoveredDevice.deviceInfo)
        assertEquals(isReachable, discoveredDevice.isReachable)
        assertEquals(discoveredAt, discoveredDevice.discoveredAt)
    }

    @Test
    fun `DiscoveredDevice should calculate if device is stale after 5 minutes`() {
        // Arrange
        val deviceInfo = createTestDeviceInfo()
        val fiveMinutesAgo = System.currentTimeMillis() - (5 * 60 * 1000)
        val sixMinutesAgo = System.currentTimeMillis() - (6 * 60 * 1000)

        // Act
        val recentDevice = DiscoveredDevice(
            deviceInfo = deviceInfo,
            isReachable = true,
            discoveredAt = fiveMinutesAgo
        )
        val staleDevice = DiscoveredDevice(
            deviceInfo = deviceInfo,
            isReachable = true,
            discoveredAt = sixMinutesAgo
        )

        // Assert
        assertFalse(recentDevice.isStale())
        assertTrue(staleDevice.isStale())
    }

    @Test
    fun `DiscoveredDevice should not be stale if discovered recently`() {
        // Arrange
        val deviceInfo = createTestDeviceInfo()
        val now = System.currentTimeMillis()

        // Act
        val device = DiscoveredDevice(
            deviceInfo = deviceInfo,
            isReachable = true,
            discoveredAt = now
        )

        // Assert
        assertFalse(device.isStale())
    }

    @Test
    fun `DiscoveredDevice should be stale exactly at 5 minute threshold`() {
        // Arrange
        val deviceInfo = createTestDeviceInfo()
        val exactlyFiveMinutesAgo = System.currentTimeMillis() - (5 * 60 * 1000)

        // Act
        val device = DiscoveredDevice(
            deviceInfo = deviceInfo,
            isReachable = true,
            discoveredAt = exactlyFiveMinutesAgo
        )

        // Assert - At exactly 5 minutes, should not be stale yet (> not >=)
        assertFalse(device.isStale())
    }

    @Test
    fun `DiscoveredDevice should be stale just after 5 minute threshold`() {
        // Arrange
        val deviceInfo = createTestDeviceInfo()
        val justOverFiveMinutesAgo = System.currentTimeMillis() - (5 * 60 * 1000 + 1)

        // Act
        val device = DiscoveredDevice(
            deviceInfo = deviceInfo,
            isReachable = true,
            discoveredAt = justOverFiveMinutesAgo
        )

        // Assert
        assertTrue(device.isStale())
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
}
