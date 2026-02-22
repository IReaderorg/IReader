package ireader.data.sync.integration

import ireader.data.sync.SyncRepository
import ireader.data.sync.fake.FakeDiscoveryDataSource
import ireader.data.sync.fake.FakeTransferDataSource
import ireader.data.sync.fake.FakeSyncLocalDataSource
import ireader.domain.models.sync.*
import kotlinx.coroutines.test.runTest
import kotlin.test.*

/**
 * Integration tests for security features in sync functionality.
 * Tests PIN-based pairing, encryption, and certificate validation.
 */
class SecurityIntegrationTest {
    
    private lateinit var repository: SyncRepository
    private lateinit var discoveryDataSource: FakeDiscoveryDataSource
    private lateinit var transferDataSource: FakeTransferDataSource
    private lateinit var localDataSource: FakeSyncLocalDataSource
    
    @BeforeTest
    fun setup() {
        discoveryDataSource = FakeDiscoveryDataSource()
        transferDataSource = FakeTransferDataSource()
        localDataSource = FakeSyncLocalDataSource()
        
        repository = SyncRepository(
            discoveryDataSource = discoveryDataSource,
            transferDataSource = transferDataSource,
            localDataSource = localDataSource
        )
    }
    
    @AfterTest
    fun tearDown() {
        discoveryDataSource.cleanup()
        transferDataSource.cleanup()
    }
    
    // RED: Test PIN-based pairing flow end-to-end
    @Test
    fun `PIN-based pairing should complete successfully with correct PIN`() = runTest {
        // Arrange
        val deviceId = "device-2"
        val correctPin = "123456"
        discoveryDataSource.addDiscoverableDevice(createTestDevice(deviceId))
        transferDataSource.setExpectedPin(correctPin)
        
        // Act
        repository.startDiscovery()
        val pairingResult = repository.initiatePairing(deviceId, correctPin)
        
        // Assert
        assertTrue(pairingResult.isSuccess)
        val pairedDevice = pairingResult.getOrNull()
        assertNotNull(pairedDevice)
        assertEquals(PairingStatus.PAIRED, pairedDevice.status)
        assertNotNull(pairedDevice.certificate)
        assertTrue(pairedDevice.isTrusted)
    }
    
    @Test
    fun `PIN-based pairing should fail with incorrect PIN`() = runTest {
        // Arrange
        val deviceId = "device-2"
        val correctPin = "123456"
        val wrongPin = "654321"
        discoveryDataSource.addDiscoverableDevice(createTestDevice(deviceId))
        transferDataSource.setExpectedPin(correctPin)
        
        // Act
        repository.startDiscovery()
        val pairingResult = repository.initiatePairing(deviceId, wrongPin)
        
        // Assert
        assertTrue(pairingResult.isFailure)
        val error = pairingResult.exceptionOrNull()
        assertNotNull(error)
        assertTrue(error is SyncException)
        assertEquals(SyncErrorType.AUTHENTICATION_FAILED, (error as SyncException).errorType)
    }
    
    @Test
    fun `PIN-based pairing should lock after max attempts`() = runTest {
        // Arrange
        val deviceId = "device-2"
        val correctPin = "123456"
        val wrongPin = "000000"
        discoveryDataSource.addDiscoverableDevice(createTestDevice(deviceId))
        transferDataSource.setExpectedPin(correctPin)
        transferDataSource.setMaxPinAttempts(3)
        
        // Act - Try wrong PIN 3 times
        repository.startDiscovery()
        repeat(3) {
            repository.initiatePairing(deviceId, wrongPin)
        }
        
        // Try with correct PIN after lockout
        val finalResult = repository.initiatePairing(deviceId, correctPin)
        
        // Assert
        assertTrue(finalResult.isFailure)
        val error = finalResult.exceptionOrNull()
        assertNotNull(error)
        assertTrue(error is SyncException)
        assertEquals(SyncErrorType.TOO_MANY_ATTEMPTS, (error as SyncException).errorType)
    }
    
    // RED: Test certificate pinning validation
    @Test
    fun `certificate pinning should validate trusted certificates`() = runTest {
        // Arrange
        val deviceId = "device-2"
        val pin = "123456"
        discoveryDataSource.addDiscoverableDevice(createTestDevice(deviceId))
        transferDataSource.setExpectedPin(pin)
        
        // Act - Initial pairing establishes trust
        repository.startDiscovery()
        val pairingResult = repository.initiatePairing(deviceId, pin)
        assertTrue(pairingResult.isSuccess)
        
        // Simulate reconnection with same certificate
        repository.disconnect(deviceId)
        discoveryDataSource.addDiscoverableDevice(createTestDevice(deviceId))
        repository.startDiscovery()
        
        val reconnectResult = repository.initiatePairing(deviceId, pin)
        
        // Assert
        assertTrue(reconnectResult.isSuccess)
        val device = reconnectResult.getOrNull()
        assertNotNull(device)
        assertTrue(device.isTrusted)
    }
    
    @Test
    fun `certificate pinning should reject changed certificates`() = runTest {
        // Arrange
        val deviceId = "device-2"
        val pin = "123456"
        discoveryDataSource.addDiscoverableDevice(createTestDevice(deviceId))
        transferDataSource.setExpectedPin(pin)
        
        // Act - Initial pairing
        repository.startDiscovery()
        repository.initiatePairing(deviceId, pin)
        
        // Simulate MITM attack - different certificate
        repository.disconnect(deviceId)
        transferDataSource.rotateCertificate(deviceId)
        discoveryDataSource.addDiscoverableDevice(createTestDevice(deviceId))
        repository.startDiscovery()
        
        val reconnectResult = repository.initiatePairing(deviceId, pin)
        
        // Assert
        assertTrue(reconnectResult.isFailure)
        val error = reconnectResult.exceptionOrNull()
        assertNotNull(error)
        assertTrue(error is SyncException)
        assertEquals(SyncErrorType.CERTIFICATE_MISMATCH, (error as SyncException).errorType)
    }
    
    // RED: Test encrypted data transfer
    @Test
    fun `data transfer should encrypt all transmitted data`() = runTest {
        // Arrange
        val deviceId = "device-2"
        val books = listOf(createTestBook(1L, "Secret Book"))
        localDataSource.setBooks(books)
        
        discoveryDataSource.addDiscoverableDevice(createTestDevice(deviceId))
        transferDataSource.setExpectedPin("123456")
        transferDataSource.enableEncryptionValidation()
        
        // Act
        repository.startDiscovery()
        repository.initiatePairing(deviceId, "123456")
        val syncResult = repository.syncWithDevice(deviceId)
        
        // Assert
        assertTrue(syncResult.isSuccess)
        assertTrue(transferDataSource.wasDataEncrypted())
        
        val transmittedData = transferDataSource.getTransmittedData()
        assertFalse(transmittedData.contains("Secret Book")) // Should be encrypted
    }
    
    // RED: Test trust expiration and re-authentication
    @Test
    fun `trust should expire after configured duration`() = runTest {
        // Arrange
        val deviceId = "device-2"
        val pin = "123456"
        discoveryDataSource.addDiscoverableDevice(createTestDevice(deviceId))
        transferDataSource.setExpectedPin(pin)
        transferDataSource.setTrustDuration(1000L) // 1 second
        
        // Act - Initial pairing
        repository.startDiscovery()
        repository.initiatePairing(deviceId, pin)
        
        // Wait for trust to expire
        kotlinx.coroutines.delay(1100L)
        
        // Try to sync without re-authentication
        val syncResult = repository.syncWithDevice(deviceId)
        
        // Assert
        assertTrue(syncResult.isFailure)
        val error = syncResult.exceptionOrNull()
        assertNotNull(error)
        assertTrue(error is SyncException)
        assertEquals(SyncErrorType.TRUST_EXPIRED, (error as SyncException).errorType)
    }
    
    @Test
    fun `re-authentication should restore trust after expiration`() = runTest {
        // Arrange
        val deviceId = "device-2"
        val pin = "123456"
        discoveryDataSource.addDiscoverableDevice(createTestDevice(deviceId))
        transferDataSource.setExpectedPin(pin)
        transferDataSource.setTrustDuration(1000L)
        
        // Act - Initial pairing
        repository.startDiscovery()
        repository.initiatePairing(deviceId, pin)
        
        // Wait for trust to expire
        kotlinx.coroutines.delay(1100L)
        
        // Re-authenticate
        val reAuthResult = repository.initiatePairing(deviceId, pin)
        
        // Assert
        assertTrue(reAuthResult.isSuccess)
        val device = reAuthResult.getOrNull()
        assertNotNull(device)
        assertTrue(device.isTrusted)
        
        // Should be able to sync now
        val syncResult = repository.syncWithDevice(deviceId)
        assertTrue(syncResult.isSuccess)
    }
    
    // RED: Test MITM attack prevention
    @Test
    fun `MITM attack should be detected and prevented`() = runTest {
        // Arrange
        val deviceId = "device-2"
        val pin = "123456"
        discoveryDataSource.addDiscoverableDevice(createTestDevice(deviceId))
        transferDataSource.setExpectedPin(pin)
        
        // Act - Initial pairing
        repository.startDiscovery()
        repository.initiatePairing(deviceId, pin)
        
        // Simulate MITM attack during sync
        transferDataSource.simulateMITMAttack()
        val syncResult = repository.syncWithDevice(deviceId)
        
        // Assert
        assertTrue(syncResult.isFailure)
        val error = syncResult.exceptionOrNull()
        assertNotNull(error)
        assertTrue(error is SyncException)
        assertEquals(SyncErrorType.SECURITY_VIOLATION, (error as SyncException).errorType)
    }
    
    // RED: Test certificate rotation
    @Test
    fun `certificate rotation should require re-pairing`() = runTest {
        // Arrange
        val deviceId = "device-2"
        val pin = "123456"
        discoveryDataSource.addDiscoverableDevice(createTestDevice(deviceId))
        transferDataSource.setExpectedPin(pin)
        
        // Act - Initial pairing
        repository.startDiscovery()
        repository.initiatePairing(deviceId, pin)
        
        // Rotate certificate on remote device
        transferDataSource.rotateCertificate(deviceId)
        
        // Try to sync with old trust
        val syncResult = repository.syncWithDevice(deviceId)
        
        // Assert - Should fail
        assertTrue(syncResult.isFailure)
        
        // Re-pair with new certificate
        val rePairResult = repository.initiatePairing(deviceId, pin)
        assertTrue(rePairResult.isSuccess)
        
        // Should work now
        val newSyncResult = repository.syncWithDevice(deviceId)
        assertTrue(newSyncResult.isSuccess)
    }
    
    // Helper functions
    private fun createTestDevice(id: String) = DeviceInfo(
        deviceId = id,
        deviceName = "Test Device",
        deviceType = DeviceType.ANDROID,
        appVersion = "1.0.0",
        ipAddress = "192.168.1.100",
        port = 8080,
        lastSeen = System.currentTimeMillis()
    )
    
    private fun createTestBook(id: Long, title: String) = SyncableBook(
        id = id,
        title = title,
        author = "Test Author",
        lastModified = System.currentTimeMillis(),
        coverUrl = null,
        chapters = emptyList()
    )
}
