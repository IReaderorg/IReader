package ireader.domain.models.sync

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class SyncMetadataTest {

    @Test
    fun `SyncMetadata should be created with valid data`() {
        // Arrange
        val deviceId = "test-device-123"
        val timestamp = System.currentTimeMillis()
        val version = 1
        val checksum = "abc123def456"

        // Act
        val metadata = SyncMetadata(
            deviceId = deviceId,
            timestamp = timestamp,
            version = version,
            checksum = checksum
        )

        // Assert
        assertEquals(deviceId, metadata.deviceId)
        assertEquals(timestamp, metadata.timestamp)
        assertEquals(version, metadata.version)
        assertEquals(checksum, metadata.checksum)
    }

    @Test
    fun `SyncMetadata should reject empty deviceId`() {
        // Arrange & Act & Assert
        assertFailsWith<IllegalArgumentException> {
            SyncMetadata(
                deviceId = "",
                timestamp = System.currentTimeMillis(),
                version = 1,
                checksum = "abc123"
            )
        }
    }

    @Test
    fun `SyncMetadata should reject blank deviceId`() {
        // Arrange & Act & Assert
        assertFailsWith<IllegalArgumentException> {
            SyncMetadata(
                deviceId = "   ",
                timestamp = System.currentTimeMillis(),
                version = 1,
                checksum = "abc123"
            )
        }
    }

    @Test
    fun `SyncMetadata should reject negative timestamp`() {
        // Arrange & Act & Assert
        assertFailsWith<IllegalArgumentException> {
            SyncMetadata(
                deviceId = "test-device-123",
                timestamp = -1L,
                version = 1,
                checksum = "abc123"
            )
        }
    }

    @Test
    fun `SyncMetadata should reject zero version`() {
        // Arrange & Act & Assert
        assertFailsWith<IllegalArgumentException> {
            SyncMetadata(
                deviceId = "test-device-123",
                timestamp = System.currentTimeMillis(),
                version = 0,
                checksum = "abc123"
            )
        }
    }

    @Test
    fun `SyncMetadata should reject negative version`() {
        // Arrange & Act & Assert
        assertFailsWith<IllegalArgumentException> {
            SyncMetadata(
                deviceId = "test-device-123",
                timestamp = System.currentTimeMillis(),
                version = -1,
                checksum = "abc123"
            )
        }
    }

    @Test
    fun `SyncMetadata should accept version 1`() {
        // Arrange & Act
        val metadata = SyncMetadata(
            deviceId = "test-device-123",
            timestamp = System.currentTimeMillis(),
            version = 1,
            checksum = "abc123"
        )

        // Assert
        assertEquals(1, metadata.version)
    }

    @Test
    fun `SyncMetadata should reject empty checksum`() {
        // Arrange & Act & Assert
        assertFailsWith<IllegalArgumentException> {
            SyncMetadata(
                deviceId = "test-device-123",
                timestamp = System.currentTimeMillis(),
                version = 1,
                checksum = ""
            )
        }
    }

    @Test
    fun `SyncMetadata should reject blank checksum`() {
        // Arrange & Act & Assert
        assertFailsWith<IllegalArgumentException> {
            SyncMetadata(
                deviceId = "test-device-123",
                timestamp = System.currentTimeMillis(),
                version = 1,
                checksum = "   "
            )
        }
    }
}
