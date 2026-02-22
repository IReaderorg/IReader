package ireader.data.sync.encryption

import ireader.data.sync.datasource.SyncLocalDataSource
import ireader.data.sync.datasource.TrustedDeviceEntity
import ireader.domain.services.sync.CertificateService
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.*

/**
 * Test suite for CertificatePinningManager.
 * 
 * Tests certificate pinning functionality including:
 * - Pinning certificates for devices
 * - Validating pinned certificates
 * - Updating pinned certificates
 * - Removing pinned certificates
 * - Handling edge cases
 * 
 * Following TDD methodology: Tests written first (RED phase).
 */
class CertificatePinningManagerTest {
    
    private lateinit var certificateService: FakeCertificateService
    private lateinit var localStorage: FakeSyncLocalDataSource
    private lateinit var pinningManager: CertificatePinningManager
    
    @BeforeTest
    fun setup() {
        certificateService = FakeCertificateService()
        localStorage = FakeSyncLocalDataSource()
        pinningManager = CertificatePinningManager(certificateService, localStorage)
    }
    
    // ========== Pin Certificate Tests ==========
    
    @Test
    fun `pinCertificate should store fingerprint for device`() = runTest {
        // Arrange
        val deviceId = "device-123"
        val fingerprint = "SHA256:abcd1234"
        
        // Act
        val result = pinningManager.pinCertificate(deviceId, fingerprint)
        
        // Assert
        assertTrue(result.isSuccess, "Should successfully pin certificate")
        
        // Verify by retrieving the fingerprint
        val retrievedResult = pinningManager.getPinnedFingerprint(deviceId)
        assertTrue(retrievedResult.isSuccess, "Should successfully retrieve fingerprint")
        assertEquals(fingerprint, retrievedResult.getOrNull(), "Fingerprint should be stored")
    }
    
    @Test
    fun `pinCertificate should update existing fingerprint`() = runTest {
        // Arrange
        val deviceId = "device-123"
        val oldFingerprint = "SHA256:old1234"
        val newFingerprint = "SHA256:new5678"
        
        // Pin old fingerprint first
        pinningManager.pinCertificate(deviceId, oldFingerprint)
        
        // Act
        val result = pinningManager.pinCertificate(deviceId, newFingerprint)
        
        // Assert
        assertTrue(result.isSuccess, "Should successfully update fingerprint")
        
        // Verify by retrieving the fingerprint
        val retrievedResult = pinningManager.getPinnedFingerprint(deviceId)
        assertTrue(retrievedResult.isSuccess, "Should successfully retrieve fingerprint")
        assertEquals(newFingerprint, retrievedResult.getOrNull(), "Fingerprint should be updated")
    }
    
    @Test
    fun `pinCertificate should reject empty fingerprint`() = runTest {
        // Arrange
        val deviceId = "device-123"
        val emptyFingerprint = ""
        
        // Act
        val result = pinningManager.pinCertificate(deviceId, emptyFingerprint)
        
        // Assert
        assertTrue(result.isFailure, "Should fail with empty fingerprint")
    }
    
    @Test
    fun `pinCertificate should reject empty deviceId`() = runTest {
        // Arrange
        val emptyDeviceId = ""
        val fingerprint = "SHA256:abcd1234"
        
        // Act
        val result = pinningManager.pinCertificate(emptyDeviceId, fingerprint)
        
        // Assert
        assertTrue(result.isFailure, "Should fail with empty deviceId")
    }
    
    // ========== Validate Certificate Tests ==========
    
    @Test
    fun `validatePinnedCertificate should pass for matching fingerprint`() = runTest {
        // Arrange
        val deviceId = "device-123"
        val fingerprint = "SHA256:abcd1234"
        val certificate = byteArrayOf(1, 2, 3, 4)
        
        certificateService.setFingerprintForCertificate(certificate, fingerprint)
        pinningManager.pinCertificate(deviceId, fingerprint)
        
        // Act
        val result = pinningManager.validatePinnedCertificate(deviceId, certificate)
        
        // Assert
        assertTrue(result.isSuccess, "Validation should succeed")
        assertTrue(result.getOrNull() == true, "Certificate should be valid")
    }
    
    @Test
    fun `validatePinnedCertificate should fail for mismatched fingerprint`() = runTest {
        // Arrange
        val deviceId = "device-123"
        val pinnedFingerprint = "SHA256:abcd1234"
        val actualFingerprint = "SHA256:different"
        val certificate = byteArrayOf(1, 2, 3, 4)
        
        certificateService.setFingerprintForCertificate(certificate, actualFingerprint)
        pinningManager.pinCertificate(deviceId, pinnedFingerprint)
        
        // Act
        val result = pinningManager.validatePinnedCertificate(deviceId, certificate)
        
        // Assert
        assertTrue(result.isSuccess, "Validation should succeed")
        assertFalse(result.getOrNull() == true, "Certificate should be invalid")
    }
    
    @Test
    fun `validatePinnedCertificate should fail for non-existent device`() = runTest {
        // Arrange
        val deviceId = "non-existent-device"
        val certificate = byteArrayOf(1, 2, 3, 4)
        
        // Act
        val result = pinningManager.validatePinnedCertificate(deviceId, certificate)
        
        // Assert
        assertTrue(result.isFailure, "Should fail for non-existent device")
    }
    
    @Test
    fun `validatePinnedCertificate should handle empty certificate`() = runTest {
        // Arrange
        val deviceId = "device-123"
        val fingerprint = "SHA256:abcd1234"
        val emptyCertificate = byteArrayOf()
        
        pinningManager.pinCertificate(deviceId, fingerprint)
        
        // Act
        val result = pinningManager.validatePinnedCertificate(deviceId, emptyCertificate)
        
        // Assert
        assertTrue(result.isFailure, "Should fail with empty certificate")
    }
    
    // ========== Update Certificate Tests ==========
    
    @Test
    fun `updatePinnedCertificate should update fingerprint`() = runTest {
        // Arrange
        val deviceId = "device-123"
        val oldFingerprint = "SHA256:old1234"
        val newFingerprint = "SHA256:new5678"
        
        pinningManager.pinCertificate(deviceId, oldFingerprint)
        
        // Act
        val result = pinningManager.updatePinnedCertificate(deviceId, newFingerprint)
        
        // Assert
        assertTrue(result.isSuccess, "Should successfully update")
        
        // Verify by retrieving the fingerprint
        val retrievedResult = pinningManager.getPinnedFingerprint(deviceId)
        assertTrue(retrievedResult.isSuccess, "Should successfully retrieve fingerprint")
        assertEquals(newFingerprint, retrievedResult.getOrNull(), "Fingerprint should be updated")
    }
    
    @Test
    fun `updatePinnedCertificate should fail for non-existent device`() = runTest {
        // Arrange
        val deviceId = "non-existent-device"
        val newFingerprint = "SHA256:new5678"
        
        // Act
        val result = pinningManager.updatePinnedCertificate(deviceId, newFingerprint)
        
        // Assert
        assertTrue(result.isFailure, "Should fail for non-existent device")
    }
    
    // ========== Remove Certificate Tests ==========
    
    @Test
    fun `removePinnedCertificate should remove fingerprint`() = runTest {
        // Arrange
        val deviceId = "device-123"
        val fingerprint = "SHA256:abcd1234"
        
        pinningManager.pinCertificate(deviceId, fingerprint)
        
        // Act
        val result = pinningManager.removePinnedCertificate(deviceId)
        
        // Assert
        assertTrue(result.isSuccess, "Should successfully remove")
        
        // Verify by retrieving the fingerprint
        val retrievedResult = pinningManager.getPinnedFingerprint(deviceId)
        assertTrue(retrievedResult.isSuccess, "Should successfully retrieve")
        assertNull(retrievedResult.getOrNull(), "Fingerprint should be removed")
    }
    
    @Test
    fun `removePinnedCertificate should succeed for non-existent device`() = runTest {
        // Arrange
        val deviceId = "non-existent-device"
        
        // Act
        val result = pinningManager.removePinnedCertificate(deviceId)
        
        // Assert
        assertTrue(result.isSuccess, "Should succeed even if device doesn't exist")
    }
    
    // ========== Get Fingerprint Tests ==========
    
    @Test
    fun `getPinnedFingerprint should return stored fingerprint`() = runTest {
        // Arrange
        val deviceId = "device-123"
        val fingerprint = "SHA256:abcd1234"
        
        pinningManager.pinCertificate(deviceId, fingerprint)
        
        // Act
        val result = pinningManager.getPinnedFingerprint(deviceId)
        
        // Assert
        assertTrue(result.isSuccess, "Should successfully retrieve")
        assertEquals(fingerprint, result.getOrNull(), "Should return correct fingerprint")
    }
    
    @Test
    fun `getPinnedFingerprint should return null for non-existent device`() = runTest {
        // Arrange
        val deviceId = "non-existent-device"
        
        // Act
        val result = pinningManager.getPinnedFingerprint(deviceId)
        
        // Assert
        assertTrue(result.isSuccess, "Should succeed")
        assertNull(result.getOrNull(), "Should return null for non-existent device")
    }
    
    // ========== Database Integration Tests ==========
    
    @Test
    fun `pinCertificate should store fingerprint in database`() = runTest {
        // Arrange
        val deviceId = "device-123"
        val fingerprint = "SHA256:abcd1234"
        
        // Create trusted device first
        localStorage.upsertTrustedDevice(
            TrustedDeviceEntity(
                deviceId = deviceId,
                deviceName = "Test Device",
                pairedAt = System.currentTimeMillis(),
                expiresAt = System.currentTimeMillis() + 86400000,
                isActive = true
            )
        )
        
        // Act
        val result = pinningManager.pinCertificate(deviceId, fingerprint)
        
        // Assert
        assertTrue(result.isSuccess, "Should successfully pin certificate")
        
        // Verify fingerprint is stored in database
        val storedFingerprint = localStorage.getCertificateFingerprint(deviceId)
        assertEquals(fingerprint, storedFingerprint, "Fingerprint should be stored in database")
    }
    
    @Test
    fun `validatePinnedCertificate should retrieve fingerprint from database`() = runTest {
        // Arrange
        val deviceId = "device-123"
        val fingerprint = "SHA256:abcd1234"
        val certificate = byteArrayOf(1, 2, 3, 4)
        
        // Create trusted device with fingerprint
        localStorage.upsertTrustedDevice(
            TrustedDeviceEntity(
                deviceId = deviceId,
                deviceName = "Test Device",
                pairedAt = System.currentTimeMillis(),
                expiresAt = System.currentTimeMillis() + 86400000,
                isActive = true,
                certificateFingerprint = fingerprint
            )
        )
        
        certificateService.setFingerprintForCertificate(certificate, fingerprint)
        
        // Act
        val result = pinningManager.validatePinnedCertificate(deviceId, certificate)
        
        // Assert
        assertTrue(result.isSuccess, "Validation should succeed")
        assertTrue(result.getOrNull() == true, "Certificate should be valid")
    }
    
    @Test
    fun `removePinnedCertificate should remove fingerprint from database`() = runTest {
        // Arrange
        val deviceId = "device-123"
        val fingerprint = "SHA256:abcd1234"
        
        // Create trusted device with fingerprint
        localStorage.upsertTrustedDevice(
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
        val result = pinningManager.removePinnedCertificate(deviceId)
        
        // Assert
        assertTrue(result.isSuccess, "Should successfully remove")
        
        // Verify fingerprint is removed from database
        val storedFingerprint = localStorage.getCertificateFingerprint(deviceId)
        assertNull(storedFingerprint, "Fingerprint should be removed from database")
    }
    
    @Test
    fun `updatePinnedCertificate should update fingerprint in database`() = runTest {
        // Arrange
        val deviceId = "device-123"
        val oldFingerprint = "SHA256:old1234"
        val newFingerprint = "SHA256:new5678"
        
        // Create trusted device with old fingerprint
        localStorage.upsertTrustedDevice(
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
        val result = pinningManager.updatePinnedCertificate(deviceId, newFingerprint)
        
        // Assert
        assertTrue(result.isSuccess, "Should successfully update")
        
        // Verify fingerprint is updated in database
        val storedFingerprint = localStorage.getCertificateFingerprint(deviceId)
        assertEquals(newFingerprint, storedFingerprint, "Fingerprint should be updated in database")
    }
}

// ========== Fake Implementations for Testing ==========

/**
 * Fake CertificateService for testing.
 */
private class FakeCertificateService : CertificateService {
    private val fingerprintMap = mutableMapOf<String, String>()
    
    fun setFingerprintForCertificate(certificate: ByteArray, fingerprint: String) {
        fingerprintMap[certificate.contentToString()] = fingerprint
    }
    
    override suspend fun generateSelfSignedCertificate(
        commonName: String,
        validityDays: Int
    ): Result<CertificateService.CertificateData> {
        return Result.success(
            CertificateService.CertificateData(
                certificate = byteArrayOf(1, 2, 3),
                privateKey = byteArrayOf(4, 5, 6),
                publicKey = byteArrayOf(7, 8, 9),
                fingerprint = "SHA256:fake"
            )
        )
    }
    
    override suspend fun storeCertificate(
        alias: String,
        certificateData: CertificateService.CertificateData
    ): Result<Unit> = Result.success(Unit)
    
    override suspend fun retrieveCertificate(alias: String): Result<CertificateService.CertificateData> {
        return Result.failure(Exception("Not implemented"))
    }
    
    override fun verifyCertificateFingerprint(
        certificate: ByteArray,
        expectedFingerprint: String
    ): Boolean {
        val actualFingerprint = calculateFingerprint(certificate)
        return actualFingerprint == expectedFingerprint
    }
    
    override fun calculateFingerprint(certificate: ByteArray): String {
        return fingerprintMap[certificate.contentToString()] ?: "SHA256:unknown"
    }
    
    override suspend fun deleteCertificate(alias: String): Result<Unit> = Result.success(Unit)
    
    override suspend fun certificateExists(alias: String): Boolean = false
}

/**
 * Fake SyncLocalDataSource for testing.
 */
private class FakeSyncLocalDataSource : SyncLocalDataSource {
    private val trustedDevices = mutableMapOf<String, TrustedDeviceEntity>()
    
    override suspend fun getSyncMetadata(deviceId: String) = null
    override suspend fun upsertSyncMetadata(metadata: ireader.data.sync.datasource.SyncMetadataEntity) {}
    override suspend fun deleteSyncMetadata(deviceId: String) {}
    
    override suspend fun getTrustedDevice(deviceId: String): TrustedDeviceEntity? {
        return trustedDevices[deviceId]
    }
    
    override suspend fun upsertTrustedDevice(device: TrustedDeviceEntity) {
        trustedDevices[device.deviceId] = device
    }
    
    override fun getActiveTrustedDevices() = flowOf(trustedDevices.values.filter { it.isActive })
    
    override suspend fun deactivateTrustedDevice(deviceId: String) {
        trustedDevices[deviceId]?.let {
            trustedDevices[deviceId] = it.copy(isActive = false)
        }
    }
    
    override suspend fun updateDeviceExpiration(deviceId: String, expiresAt: Long) {
        trustedDevices[deviceId]?.let {
            trustedDevices[deviceId] = it.copy(expiresAt = expiresAt)
        }
    }
    
    override suspend fun deleteTrustedDevice(deviceId: String) {
        trustedDevices.remove(deviceId)
    }
    
    override suspend fun updateCertificateFingerprint(deviceId: String, fingerprint: String?) {
        trustedDevices[deviceId]?.let {
            trustedDevices[deviceId] = it.copy(certificateFingerprint = fingerprint)
        }
    }
    
    override suspend fun getCertificateFingerprint(deviceId: String): String? {
        return trustedDevices[deviceId]?.certificateFingerprint
    }
    
    override suspend fun insertSyncLog(log: ireader.data.sync.datasource.SyncLogEntity) {}
    override suspend fun getSyncLogById(id: Long) = null
    override fun getSyncLogsByDevice(deviceId: String) = flowOf(emptyList())
    
    override suspend fun getBooks() = emptyList<ireader.domain.models.sync.BookSyncData>()
    override suspend fun getProgress() = emptyList<ireader.domain.models.sync.ReadingProgressData>()
    override suspend fun getBookmarks() = emptyList<ireader.domain.models.sync.BookmarkData>()
}
