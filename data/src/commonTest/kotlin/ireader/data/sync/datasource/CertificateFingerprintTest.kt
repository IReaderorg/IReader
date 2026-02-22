package ireader.data.sync.datasource

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Tests for certificate fingerprint operations in SyncLocalDataSource.
 * 
 * Following TDD: These tests verify the database integration for certificate pinning.
 * 
 * RED Phase: Tests written first to define expected behavior.
 */
class CertificateFingerprintTest {
    
    @Test
    fun `updateCertificateFingerprint should store fingerprint for device`() = runTest {
        // Arrange
        val fakeDataSource = FakeSyncLocalDataSource()
        val deviceId = "device-123"
        val fingerprint = "SHA256:abcd1234"
        
        // Create trusted device first
        fakeDataSource.upsertTrustedDevice(
            TrustedDeviceEntity(
                deviceId = deviceId,
                deviceName = "Test Device",
                pairedAt = System.currentTimeMillis(),
                expiresAt = System.currentTimeMillis() + 86400000,
                isActive = true
            )
        )
        
        // Act
        fakeDataSource.updateCertificateFingerprint(deviceId, fingerprint)
        
        // Assert
        val storedFingerprint = fakeDataSource.getCertificateFingerprint(deviceId)
        assertEquals(fingerprint, storedFingerprint, "Fingerprint should be stored")
    }
    
    @Test
    fun `getCertificateFingerprint should return null for non-existent device`() = runTest {
        // Arrange
        val fakeDataSource = FakeSyncLocalDataSource()
        val deviceId = "non-existent-device"
        
        // Act
        val fingerprint = fakeDataSource.getCertificateFingerprint(deviceId)
        
        // Assert
        assertNull(fingerprint, "Should return null for non-existent device")
    }
    
    @Test
    fun `updateCertificateFingerprint should update existing fingerprint`() = runTest {
        // Arrange
        val fakeDataSource = FakeSyncLocalDataSource()
        val deviceId = "device-123"
        val oldFingerprint = "SHA256:old1234"
        val newFingerprint = "SHA256:new5678"
        
        // Create trusted device with old fingerprint
        fakeDataSource.upsertTrustedDevice(
            TrustedDeviceEntity(
                deviceId = deviceId,
                deviceName = "Test Device",
                pairedAt = System.currentTimeMillis(),
                expiresAt = System.currentTimeMillis() + 86400000,
                isActive = true,
                certificateFingerprint = oldFingerprint
            )
        )
        
        // Act
        fakeDataSource.updateCertificateFingerprint(deviceId, newFingerprint)
        
        // Assert
        val storedFingerprint = fakeDataSource.getCertificateFingerprint(deviceId)
        assertEquals(newFingerprint, storedFingerprint, "Fingerprint should be updated")
    }
    
    @Test
    fun `updateCertificateFingerprint should remove fingerprint when null`() = runTest {
        // Arrange
        val fakeDataSource = FakeSyncLocalDataSource()
        val deviceId = "device-123"
        val fingerprint = "SHA256:abcd1234"
        
        // Create trusted device with fingerprint
        fakeDataSource.upsertTrustedDevice(
            TrustedDeviceEntity(
                deviceId = deviceId,
                deviceName = "Test Device",
                pairedAt = System.currentTimeMillis(),
                expiresAt = System.currentTimeMillis() + 86400000,
                isActive = true,
                certificateFingerprint = fingerprint
            )
        )
        
        // Act
        fakeDataSource.updateCertificateFingerprint(deviceId, null)
        
        // Assert
        val storedFingerprint = fakeDataSource.getCertificateFingerprint(deviceId)
        assertNull(storedFingerprint, "Fingerprint should be removed")
    }
    
    @Test
    fun `upsertTrustedDevice should preserve certificate fingerprint`() = runTest {
        // Arrange
        val fakeDataSource = FakeSyncLocalDataSource()
        val deviceId = "device-123"
        val fingerprint = "SHA256:abcd1234"
        
        val device = TrustedDeviceEntity(
            deviceId = deviceId,
            deviceName = "Test Device",
            pairedAt = System.currentTimeMillis(),
            expiresAt = System.currentTimeMillis() + 86400000,
            isActive = true,
            certificateFingerprint = fingerprint
        )
        
        // Act
        fakeDataSource.upsertTrustedDevice(device)
        
        // Assert
        val storedFingerprint = fakeDataSource.getCertificateFingerprint(deviceId)
        assertEquals(fingerprint, storedFingerprint, "Fingerprint should be preserved")
    }
    
    @Test
    fun `getTrustedDevice should return device with certificate fingerprint`() = runTest {
        // Arrange
        val fakeDataSource = FakeSyncLocalDataSource()
        val deviceId = "device-123"
        val fingerprint = "SHA256:abcd1234"
        
        val device = TrustedDeviceEntity(
            deviceId = deviceId,
            deviceName = "Test Device",
            pairedAt = System.currentTimeMillis(),
            expiresAt = System.currentTimeMillis() + 86400000,
            isActive = true,
            certificateFingerprint = fingerprint
        )
        
        fakeDataSource.upsertTrustedDevice(device)
        
        // Act
        val retrievedDevice = fakeDataSource.getTrustedDevice(deviceId)
        
        // Assert
        assertEquals(fingerprint, retrievedDevice?.certificateFingerprint, "Device should have fingerprint")
    }
}
