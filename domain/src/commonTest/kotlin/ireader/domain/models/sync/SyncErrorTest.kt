package ireader.domain.models.sync

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class SyncErrorTest {

    @Test
    fun `SyncError NetworkUnavailable should be created`() {
        // Arrange & Act
        val error = SyncError.NetworkUnavailable

        // Assert
        assertIs<SyncError.NetworkUnavailable>(error)
    }

    @Test
    fun `SyncError ConnectionFailed should be created with message`() {
        // Arrange
        val message = "Connection timeout"

        // Act
        val error = SyncError.ConnectionFailed(message)

        // Assert
        assertIs<SyncError.ConnectionFailed>(error)
        assertEquals(message, error.message)
    }

    @Test
    fun `SyncError AuthenticationFailed should be created with message`() {
        // Arrange
        val message = "Invalid PIN"

        // Act
        val error = SyncError.AuthenticationFailed(message)

        // Assert
        assertIs<SyncError.AuthenticationFailed>(error)
        assertEquals(message, error.message)
    }

    @Test
    fun `SyncError IncompatibleVersion should be created with versions`() {
        // Arrange
        val localVersion = 1
        val remoteVersion = 2

        // Act
        val error = SyncError.IncompatibleVersion(localVersion, remoteVersion)

        // Assert
        assertIs<SyncError.IncompatibleVersion>(error)
        assertEquals(localVersion, error.localVersion)
        assertEquals(remoteVersion, error.remoteVersion)
    }

    @Test
    fun `SyncError TransferFailed should be created with message`() {
        // Arrange
        val message = "File transfer interrupted"

        // Act
        val error = SyncError.TransferFailed(message)

        // Assert
        assertIs<SyncError.TransferFailed>(error)
        assertEquals(message, error.message)
    }

    @Test
    fun `SyncError ConflictResolutionFailed should be created with message`() {
        // Arrange
        val message = "Unable to resolve conflict"

        // Act
        val error = SyncError.ConflictResolutionFailed(message)

        // Assert
        assertIs<SyncError.ConflictResolutionFailed>(error)
        assertEquals(message, error.message)
    }

    @Test
    fun `SyncError InsufficientStorage should be created with required and available`() {
        // Arrange
        val required = 1000000L
        val available = 500000L

        // Act
        val error = SyncError.InsufficientStorage(required, available)

        // Assert
        assertIs<SyncError.InsufficientStorage>(error)
        assertEquals(required, error.required)
        assertEquals(available, error.available)
    }

    @Test
    fun `SyncError DeviceNotFound should be created with deviceId`() {
        // Arrange
        val deviceId = "test-device-123"

        // Act
        val error = SyncError.DeviceNotFound(deviceId)

        // Assert
        assertIs<SyncError.DeviceNotFound>(error)
        assertEquals(deviceId, error.deviceId)
    }

    @Test
    fun `SyncError Cancelled should be created`() {
        // Arrange & Act
        val error = SyncError.Cancelled

        // Assert
        assertIs<SyncError.Cancelled>(error)
    }

    @Test
    fun `SyncError Unknown should be created with message`() {
        // Arrange
        val message = "Unexpected error occurred"

        // Act
        val error = SyncError.Unknown(message)

        // Assert
        assertIs<SyncError.Unknown>(error)
        assertEquals(message, error.message)
    }
}
