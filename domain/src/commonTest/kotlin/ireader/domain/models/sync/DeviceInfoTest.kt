package ireader.domain.models.sync

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class DeviceInfoTest {

    @Test
    fun `DeviceInfo should be created with valid data`() {
        // Arrange
        val deviceId = "test-device-123"
        val deviceName = "My Phone"
        val deviceType = DeviceType.ANDROID
        val appVersion = "1.0.0"
        val ipAddress = "192.168.1.100"
        val port = 8080
        val lastSeen = System.currentTimeMillis()

        // Act
        val deviceInfo = DeviceInfo(
            deviceId = deviceId,
            deviceName = deviceName,
            deviceType = deviceType,
            appVersion = appVersion,
            ipAddress = ipAddress,
            port = port,
            lastSeen = lastSeen
        )

        // Assert
        assertEquals(deviceId, deviceInfo.deviceId)
        assertEquals(deviceName, deviceInfo.deviceName)
        assertEquals(deviceType, deviceInfo.deviceType)
        assertEquals(appVersion, deviceInfo.appVersion)
        assertEquals(ipAddress, deviceInfo.ipAddress)
        assertEquals(port, deviceInfo.port)
        assertEquals(lastSeen, deviceInfo.lastSeen)
    }

    @Test
    fun `DeviceInfo should reject empty deviceId`() {
        // Arrange & Act & Assert
        assertFailsWith<IllegalArgumentException> {
            DeviceInfo(
                deviceId = "",
                deviceName = "My Phone",
                deviceType = DeviceType.ANDROID,
                appVersion = "1.0.0",
                ipAddress = "192.168.1.100",
                port = 8080,
                lastSeen = System.currentTimeMillis()
            )
        }
    }

    @Test
    fun `DeviceInfo should reject blank deviceId`() {
        // Arrange & Act & Assert
        assertFailsWith<IllegalArgumentException> {
            DeviceInfo(
                deviceId = "   ",
                deviceName = "My Phone",
                deviceType = DeviceType.ANDROID,
                appVersion = "1.0.0",
                ipAddress = "192.168.1.100",
                port = 8080,
                lastSeen = System.currentTimeMillis()
            )
        }
    }

    @Test
    fun `DeviceInfo should reject empty deviceName`() {
        // Arrange & Act & Assert
        assertFailsWith<IllegalArgumentException> {
            DeviceInfo(
                deviceId = "test-device-123",
                deviceName = "",
                deviceType = DeviceType.ANDROID,
                appVersion = "1.0.0",
                ipAddress = "192.168.1.100",
                port = 8080,
                lastSeen = System.currentTimeMillis()
            )
        }
    }

    @Test
    fun `DeviceInfo should reject invalid port number below range`() {
        // Arrange & Act & Assert
        assertFailsWith<IllegalArgumentException> {
            DeviceInfo(
                deviceId = "test-device-123",
                deviceName = "My Phone",
                deviceType = DeviceType.ANDROID,
                appVersion = "1.0.0",
                ipAddress = "192.168.1.100",
                port = 0,
                lastSeen = System.currentTimeMillis()
            )
        }
    }

    @Test
    fun `DeviceInfo should reject invalid port number above range`() {
        // Arrange & Act & Assert
        assertFailsWith<IllegalArgumentException> {
            DeviceInfo(
                deviceId = "test-device-123",
                deviceName = "My Phone",
                deviceType = DeviceType.ANDROID,
                appVersion = "1.0.0",
                ipAddress = "192.168.1.100",
                port = 65536,
                lastSeen = System.currentTimeMillis()
            )
        }
    }

    @Test
    fun `DeviceInfo should accept minimum valid port`() {
        // Arrange & Act
        val deviceInfo = DeviceInfo(
            deviceId = "test-device-123",
            deviceName = "My Phone",
            deviceType = DeviceType.ANDROID,
            appVersion = "1.0.0",
            ipAddress = "192.168.1.100",
            port = 1,
            lastSeen = System.currentTimeMillis()
        )

        // Assert
        assertEquals(1, deviceInfo.port)
    }

    @Test
    fun `DeviceInfo should accept maximum valid port`() {
        // Arrange & Act
        val deviceInfo = DeviceInfo(
            deviceId = "test-device-123",
            deviceName = "My Phone",
            deviceType = DeviceType.ANDROID,
            appVersion = "1.0.0",
            ipAddress = "192.168.1.100",
            port = 65535,
            lastSeen = System.currentTimeMillis()
        )

        // Assert
        assertEquals(65535, deviceInfo.port)
    }

    @Test
    fun `DeviceInfo should reject negative lastSeen timestamp`() {
        // Arrange & Act & Assert
        assertFailsWith<IllegalArgumentException> {
            DeviceInfo(
                deviceId = "test-device-123",
                deviceName = "My Phone",
                deviceType = DeviceType.ANDROID,
                appVersion = "1.0.0",
                ipAddress = "192.168.1.100",
                port = 8080,
                lastSeen = -1L
            )
        }
    }

    @Test
    fun `DeviceType should have ANDROID and DESKTOP values`() {
        // Arrange & Act
        val androidType = DeviceType.ANDROID
        val desktopType = DeviceType.DESKTOP

        // Assert
        assertNotNull(androidType)
        assertNotNull(desktopType)
        assertTrue(DeviceType.values().contains(DeviceType.ANDROID))
        assertTrue(DeviceType.values().contains(DeviceType.DESKTOP))
    }
}
