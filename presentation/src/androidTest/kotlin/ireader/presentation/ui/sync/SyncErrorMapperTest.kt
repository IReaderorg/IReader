package ireader.presentation.ui.sync

import androidx.test.ext.junit.runners.AndroidJUnit4
import ireader.domain.models.sync.SyncError
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Tests for SyncErrorMapper.
 * 
 * Following TDD methodology - these tests are written BEFORE the mapper implementation.
 * 
 * Tests verify:
 * - Each SyncError type maps to appropriate user-friendly message
 * - Suggestions are provided where applicable
 * - Messages are clear and actionable
 */
@RunWith(AndroidJUnit4::class)
class SyncErrorMapperTest {

    @Test
    fun testNetworkUnavailableError() {
        // Arrange
        val error = SyncError.NetworkUnavailable
        
        // Act
        val result = SyncErrorMapper.mapError(error)
        
        // Assert
        assertEquals("WiFi connection lost", result.message)
        assertEquals("Check your WiFi connection and try again", result.suggestion)
    }

    @Test
    fun testConnectionFailedError() {
        // Arrange
        val error = SyncError.ConnectionFailed("Connection timeout")
        
        // Act
        val result = SyncErrorMapper.mapError(error)
        
        // Assert
        assertEquals("Failed to connect to device", result.message)
        assertEquals("Ensure both devices are on the same WiFi network", result.suggestion)
    }

    @Test
    fun testAuthenticationFailedError() {
        // Arrange
        val error = SyncError.AuthenticationFailed("Invalid PIN")
        
        // Act
        val result = SyncErrorMapper.mapError(error)
        
        // Assert
        assertEquals("Device authentication failed", result.message)
        assertEquals("Try pairing with the device again", result.suggestion)
    }

    @Test
    fun testIncompatibleVersionError() {
        // Arrange
        val error = SyncError.IncompatibleVersion(localVersion = 1, remoteVersion = 2)
        
        // Act
        val result = SyncErrorMapper.mapError(error)
        
        // Assert
        assertTrue(result.message.contains("incompatible"))
        assertTrue(result.message.contains("1"))
        assertTrue(result.message.contains("2"))
        assertEquals("Update the app on both devices to the latest version", result.suggestion)
    }

    @Test
    fun testTransferFailedError() {
        // Arrange
        val errorMessage = "Network interrupted"
        val error = SyncError.TransferFailed(errorMessage)
        
        // Act
        val result = SyncErrorMapper.mapError(error)
        
        // Assert
        assertTrue(result.message.contains("Data transfer failed"))
        assertTrue(result.message.contains(errorMessage))
        assertEquals("Check your WiFi connection and try again", result.suggestion)
    }

    @Test
    fun testConflictResolutionFailedError() {
        // Arrange
        val error = SyncError.ConflictResolutionFailed("Unable to merge")
        
        // Act
        val result = SyncErrorMapper.mapError(error)
        
        // Assert
        assertEquals("Failed to resolve data conflicts", result.message)
        assertEquals("Try resolving conflicts manually in settings", result.suggestion)
    }

    @Test
    fun testInsufficientStorageError() {
        // Arrange
        val required = 500L * 1024 * 1024 // 500 MB
        val available = 100L * 1024 * 1024 // 100 MB
        val error = SyncError.InsufficientStorage(required = required, available = available)
        
        // Act
        val result = SyncErrorMapper.mapError(error)
        
        // Assert
        assertTrue(result.message.contains("Insufficient storage"))
        assertTrue(result.message.contains("500"))
        assertTrue(result.message.contains("100"))
        assertEquals("Free up storage space and try again", result.suggestion)
    }

    @Test
    fun testDeviceNotFoundError() {
        // Arrange
        val error = SyncError.DeviceNotFound("device-123")
        
        // Act
        val result = SyncErrorMapper.mapError(error)
        
        // Assert
        assertEquals("Device not found or no longer available", result.message)
        assertEquals("Ensure the device is still on the network and try again", result.suggestion)
    }

    @Test
    fun testCancelledError() {
        // Arrange
        val error = SyncError.Cancelled
        
        // Act
        val result = SyncErrorMapper.mapError(error)
        
        // Assert
        assertEquals("Sync operation was cancelled", result.message)
        assertNull(result.suggestion)
    }

    @Test
    fun testUnknownError() {
        // Arrange
        val errorMessage = "Something went wrong"
        val error = SyncError.Unknown(errorMessage)
        
        // Act
        val result = SyncErrorMapper.mapError(error)
        
        // Assert
        assertEquals(errorMessage, result.message)
        assertEquals("Please try again or contact support if the problem persists", result.suggestion)
    }
}
