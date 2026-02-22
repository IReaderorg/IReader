package ireader.domain.usecases.sync

import ireader.domain.models.sync.TrustedDevice
import ireader.domain.repositories.FakeTrustedDeviceRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Test suite for ReauthenticateDeviceUseCase.
 * 
 * Tests device re-authentication logic following TDD principles.
 */
class ReauthenticateDeviceUseCaseTest {
    
    private val THIRTY_DAYS_MS = 30L * 24 * 60 * 60 * 1000
    
    @Test
    fun `invoke should extend expiration by 30 days for existing device`() = runTest {
        // Arrange
        val repository = FakeTrustedDeviceRepository()
        val useCase = ReauthenticateDeviceUseCase(repository)
        
        val now = System.currentTimeMillis()
        val originalExpiration = now + (5L * 24 * 60 * 60 * 1000) // 5 days from now
        val device = TrustedDevice(
            deviceId = "device-123",
            deviceName = "Test Device",
            pairedAt = now - (25L * 24 * 60 * 60 * 1000), // 25 days ago
            expiresAt = originalExpiration,
            isActive = true
        )
        repository.upsertTrustedDevice(device)
        
        // Act
        val result = useCase("device-123")
        
        // Assert
        assertTrue(result, "Re-authentication should succeed")
        
        val updatedDevice = repository.getTrustedDevice("device-123")
        assertNotNull(updatedDevice)
        
        // New expiration should be approximately 30 days from now
        val expectedExpiration = now + THIRTY_DAYS_MS
        val tolerance = 1000L // 1 second tolerance for test execution time
        assertTrue(
            updatedDevice.expiresAt >= expectedExpiration - tolerance &&
            updatedDevice.expiresAt <= expectedExpiration + tolerance,
            "Expiration should be extended to ~30 days from now. " +
            "Expected: $expectedExpiration, Got: ${updatedDevice.expiresAt}"
        )
    }
    
    @Test
    fun `invoke should reactivate inactive device`() = runTest {
        // Arrange
        val repository = FakeTrustedDeviceRepository()
        val useCase = ReauthenticateDeviceUseCase(repository)
        
        val now = System.currentTimeMillis()
        val device = TrustedDevice(
            deviceId = "device-123",
            deviceName = "Test Device",
            pairedAt = now - (10L * 24 * 60 * 60 * 1000),
            expiresAt = now + (20L * 24 * 60 * 60 * 1000),
            isActive = false // Inactive
        )
        repository.upsertTrustedDevice(device)
        
        // Act
        val result = useCase("device-123")
        
        // Assert
        assertTrue(result, "Re-authentication should succeed")
        
        val updatedDevice = repository.getTrustedDevice("device-123")
        assertNotNull(updatedDevice)
        assertTrue(updatedDevice.isActive, "Device should be reactivated")
    }
    
    @Test
    fun `invoke should extend expiration for expired device`() = runTest {
        // Arrange
        val repository = FakeTrustedDeviceRepository()
        val useCase = ReauthenticateDeviceUseCase(repository)
        
        val now = System.currentTimeMillis()
        val device = TrustedDevice(
            deviceId = "device-123",
            deviceName = "Test Device",
            pairedAt = now - (40L * 24 * 60 * 60 * 1000), // 40 days ago
            expiresAt = now - (10L * 24 * 60 * 60 * 1000), // Expired 10 days ago
            isActive = true
        )
        repository.upsertTrustedDevice(device)
        
        // Act
        val result = useCase("device-123")
        
        // Assert
        assertTrue(result, "Re-authentication should succeed for expired device")
        
        val updatedDevice = repository.getTrustedDevice("device-123")
        assertNotNull(updatedDevice)
        
        // Should be extended to 30 days from now
        val expectedExpiration = now + THIRTY_DAYS_MS
        val tolerance = 1000L
        assertTrue(
            updatedDevice.expiresAt >= expectedExpiration - tolerance &&
            updatedDevice.expiresAt <= expectedExpiration + tolerance,
            "Expiration should be extended to ~30 days from now"
        )
    }
    
    @Test
    fun `invoke should return false for non-existent device`() = runTest {
        // Arrange
        val repository = FakeTrustedDeviceRepository()
        val useCase = ReauthenticateDeviceUseCase(repository)
        
        // Act
        val result = useCase("non-existent")
        
        // Assert
        assertFalse(result, "Should return false for non-existent device")
    }
    
    @Test
    fun `invoke should preserve device name and pairedAt`() = runTest {
        // Arrange
        val repository = FakeTrustedDeviceRepository()
        val useCase = ReauthenticateDeviceUseCase(repository)
        
        val now = System.currentTimeMillis()
        val originalPairedAt = now - (25L * 24 * 60 * 60 * 1000)
        val device = TrustedDevice(
            deviceId = "device-123",
            deviceName = "My Test Device",
            pairedAt = originalPairedAt,
            expiresAt = now + (5L * 24 * 60 * 60 * 1000),
            isActive = true
        )
        repository.upsertTrustedDevice(device)
        
        // Act
        useCase("device-123")
        
        // Assert
        val updatedDevice = repository.getTrustedDevice("device-123")
        assertNotNull(updatedDevice)
        assertEquals("My Test Device", updatedDevice.deviceName, "Device name should be preserved")
        assertEquals(originalPairedAt, updatedDevice.pairedAt, "Original pairedAt should be preserved")
    }
    
    @Test
    fun `invoke should handle multiple re-authentications`() = runTest {
        // Arrange
        val repository = FakeTrustedDeviceRepository()
        val useCase = ReauthenticateDeviceUseCase(repository)
        
        val now = System.currentTimeMillis()
        val device = TrustedDevice(
            deviceId = "device-123",
            deviceName = "Test Device",
            pairedAt = now - (25L * 24 * 60 * 60 * 1000),
            expiresAt = now + (5L * 24 * 60 * 60 * 1000),
            isActive = true
        )
        repository.upsertTrustedDevice(device)
        
        // Act - First re-authentication
        val result1 = useCase("device-123")
        val device1 = repository.getTrustedDevice("device-123")
        
        // Act - Second re-authentication
        val result2 = useCase("device-123")
        val device2 = repository.getTrustedDevice("device-123")
        
        // Assert
        assertTrue(result1, "First re-authentication should succeed")
        assertTrue(result2, "Second re-authentication should succeed")
        assertNotNull(device1)
        assertNotNull(device2)
        
        // Second expiration should be later than first
        assertTrue(
            device2.expiresAt >= device1.expiresAt,
            "Second re-authentication should extend or maintain expiration"
        )
    }
}
