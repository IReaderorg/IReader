package ireader.domain.usecases.sync

import ireader.domain.models.sync.TrustedDevice
import ireader.domain.repositories.FakeTrustedDeviceRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Test suite for CheckDeviceTrustUseCase.
 * 
 * Tests device trust expiration logic following TDD principles.
 */
class CheckDeviceTrustUseCaseTest {
    
    private val THIRTY_DAYS_MS = 30L * 24 * 60 * 60 * 1000
    
    @Test
    fun `invoke should return true for active non-expired device`() = runTest {
        // Arrange
        val repository = FakeTrustedDeviceRepository()
        val useCase = CheckDeviceTrustUseCase(repository)
        
        val now = System.currentTimeMillis()
        val device = TrustedDevice(
            deviceId = "device-123",
            deviceName = "Test Device",
            pairedAt = now - (10L * 24 * 60 * 60 * 1000), // 10 days ago
            expiresAt = now + (20L * 24 * 60 * 60 * 1000), // 20 days from now
            isActive = true
        )
        repository.upsertTrustedDevice(device)
        
        // Act
        val result = useCase("device-123")
        
        // Assert
        assertTrue(result, "Should return true for active non-expired device")
    }
    
    @Test
    fun `invoke should return false for expired device`() = runTest {
        // Arrange
        val repository = FakeTrustedDeviceRepository()
        val useCase = CheckDeviceTrustUseCase(repository)
        
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
        assertFalse(result, "Should return false for expired device")
    }
    
    @Test
    fun `invoke should return false for inactive device`() = runTest {
        // Arrange
        val repository = FakeTrustedDeviceRepository()
        val useCase = CheckDeviceTrustUseCase(repository)
        
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
        assertFalse(result, "Should return false for inactive device")
    }
    
    @Test
    fun `invoke should return false for non-existent device`() = runTest {
        // Arrange
        val repository = FakeTrustedDeviceRepository()
        val useCase = CheckDeviceTrustUseCase(repository)
        
        // Act
        val result = useCase("non-existent")
        
        // Assert
        assertFalse(result, "Should return false for non-existent device")
    }
    
    @Test
    fun `invoke should return false for device expiring exactly now`() = runTest {
        // Arrange
        val repository = FakeTrustedDeviceRepository()
        val useCase = CheckDeviceTrustUseCase(repository)
        
        val now = System.currentTimeMillis()
        val device = TrustedDevice(
            deviceId = "device-123",
            deviceName = "Test Device",
            pairedAt = now - THIRTY_DAYS_MS,
            expiresAt = now, // Expires exactly now
            isActive = true
        )
        repository.upsertTrustedDevice(device)
        
        // Act
        val result = useCase("device-123")
        
        // Assert
        assertFalse(result, "Should return false for device expiring exactly now")
    }
    
    @Test
    fun `invoke should return true for device expiring in 1 millisecond`() = runTest {
        // Arrange
        val repository = FakeTrustedDeviceRepository()
        val useCase = CheckDeviceTrustUseCase(repository)
        
        val now = System.currentTimeMillis()
        val device = TrustedDevice(
            deviceId = "device-123",
            deviceName = "Test Device",
            pairedAt = now - THIRTY_DAYS_MS,
            expiresAt = now + 1, // Expires in 1ms
            isActive = true
        )
        repository.upsertTrustedDevice(device)
        
        // Act
        val result = useCase("device-123")
        
        // Assert
        assertTrue(result, "Should return true for device expiring in the future")
    }
    
    @Test
    fun `invoke should return false for inactive expired device`() = runTest {
        // Arrange
        val repository = FakeTrustedDeviceRepository()
        val useCase = CheckDeviceTrustUseCase(repository)
        
        val now = System.currentTimeMillis()
        val device = TrustedDevice(
            deviceId = "device-123",
            deviceName = "Test Device",
            pairedAt = now - (40L * 24 * 60 * 60 * 1000),
            expiresAt = now - (10L * 24 * 60 * 60 * 1000),
            isActive = false
        )
        repository.upsertTrustedDevice(device)
        
        // Act
        val result = useCase("device-123")
        
        // Assert
        assertFalse(result, "Should return false for inactive expired device")
    }
}
