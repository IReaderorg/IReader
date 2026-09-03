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
    fun `DeviceType should have refined device values and display names`() {
        // Assert
        assertTrue(DeviceType.values().contains(DeviceType.PHONE))
        assertTrue(DeviceType.values().contains(DeviceType.TABLET))
        assertTrue(DeviceType.values().contains(DeviceType.DESKTOP))
        assertTrue(DeviceType.values().contains(DeviceType.TV))
        assertTrue(DeviceType.values().contains(DeviceType.ANDROID))

        assertEquals("Phone", DeviceType.PHONE.displayName)
        assertEquals("Tablet", DeviceType.TABLET.displayName)
        assertEquals("PC / Desktop", DeviceType.DESKTOP.displayName)
        assertEquals("TV", DeviceType.TV.displayName)

        assertEquals(DeviceType.PHONE, DeviceType.fromString("phone"))
        assertEquals(DeviceType.TABLET, DeviceType.fromString("tablet"))
        assertEquals(DeviceType.DESKTOP, DeviceType.fromString("pc"))
        assertEquals(DeviceType.DESKTOP, DeviceType.fromString("desktop"))
        assertEquals(DeviceType.TV, DeviceType.fromString("tv"))
        assertEquals(DeviceType.UNKNOWN, DeviceType.fromString("invalid"))
    }

    @Test
    fun `SavedDevice should display custom alias when provided`() {
        val device = SavedDevice(
            deviceId = "dev-123",
            deviceName = "Pixel 8 Pro",
            customAlias = "My Daily Driver",
            deviceType = DeviceType.PHONE,
            lastKnownIp = "192.168.1.50"
        )
        assertEquals("My Daily Driver", device.displayName)

        val deviceWithoutAlias = device.copy(customAlias = null)
        assertEquals("Pixel 8 Pro", deviceWithoutAlias.displayName)
    }

    @Test
    fun `SyncTransferScope presets should configure correct flags`() {
        val everything = SyncTransferScope.Everything
        assertTrue(everything.transferLibrary)
        assertTrue(everything.transferReadingProgress)
        assertTrue(everything.transferDownloadedChapters)
        assertTrue(everything.transferSettings)
        assertEquals(TransferPreset.EVERYTHING, everything.preset)

        val progressOnly = SyncTransferScope.ProgressOnly
        assertTrue(!progressOnly.transferLibrary)
        assertTrue(progressOnly.transferReadingProgress)
        assertTrue(!progressOnly.transferDownloadedChapters)
        assertEquals(TransferPreset.PROGRESS_ONLY, progressOnly.preset)
    }
}
