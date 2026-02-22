package ireader.data.sync.datasource

import ireader.domain.models.sync.DeviceInfo
import ireader.domain.services.sync.CertificateService
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Integration tests for TLS functionality in KtorTransferDataSource.
 * 
 * Tests the integration of platform-specific TLS configuration with
 * the WebSocket transfer implementation.
 * 
 * Following TDD methodology:
 * 1. Write test first (RED)
 * 2. Implement minimal code (GREEN)
 * 3. Refactor (REFACTOR)
 */
class KtorTransferDataSourceTlsTest {
    
    private val testCertificateData = CertificateService.CertificateData(
        certificate = "-----BEGIN CERTIFICATE-----\nMIICTest\n-----END CERTIFICATE-----".toByteArray(),
        privateKey = "-----BEGIN PRIVATE KEY-----\nMIIETest\n-----END PRIVATE KEY-----".toByteArray(),
        publicKey = "-----BEGIN PUBLIC KEY-----\nMIIBTest\n-----END PUBLIC KEY-----".toByteArray(),
        fingerprint = "AA:BB:CC:DD:EE:FF:00:11:22:33:44:55:66:77:88:99:AA:BB:CC:DD:EE:FF:00:11:22:33:44:55:66:77:88:99"
    )
    
    private val testDeviceInfo = DeviceInfo(
        deviceId = "test-device",
        deviceName = "Test Device",
        ipAddress = "192.168.1.100",
        port = 8443,
        platform = "Android"
    )
    
    // ========== Test: TLS Server Configuration ==========
    
    @Test
    fun `startServerWithTls should fail with empty certificate`() = runTest {
        // Arrange
        val dataSource = KtorTransferDataSource()
        val invalidCertData = testCertificateData.copy(certificate = ByteArray(0))
        
        // Act
        val result = dataSource.startServerWithTls(8443, invalidCertData)
        
        // Assert
        assertTrue(result.isFailure, "Should fail with empty certificate")
        val exception = result.exceptionOrNull()
        assertNotNull(exception)
        assertTrue(
            exception is IllegalArgumentException,
            "Should throw IllegalArgumentException"
        )
    }
    
    @Test
    fun `startServerWithTls should fail with empty private key`() = runTest {
        // Arrange
        val dataSource = KtorTransferDataSource()
        val invalidCertData = testCertificateData.copy(privateKey = ByteArray(0))
        
        // Act
        val result = dataSource.startServerWithTls(8443, invalidCertData)
        
        // Assert
        assertTrue(result.isFailure, "Should fail with empty private key")
        val exception = result.exceptionOrNull()
        assertNotNull(exception)
        assertTrue(
            exception is IllegalArgumentException,
            "Should throw IllegalArgumentException"
        )
    }
    
    @Test
    fun `startServerWithTls should fail if server already running`() = runTest {
        // Arrange
        val dataSource = KtorTransferDataSource()
        val port = 8443
        
        // Start server first time
        dataSource.startServer(port)
        
        // Act - Try to start TLS server while regular server is running
        val result = dataSource.startServerWithTls(port, testCertificateData)
        
        // Assert
        assertTrue(result.isFailure, "Should fail when server already running")
        val exception = result.exceptionOrNull()
        assertNotNull(exception)
        assertTrue(
            exception is IllegalStateException,
            "Should throw IllegalStateException"
        )
        
        // Cleanup
        dataSource.stopServer()
    }
    
    // ========== Test: TLS Client Configuration ==========
    
    @Test
    fun `connectToDeviceWithTls should fail with empty fingerprint`() = runTest {
        // Arrange
        val dataSource = KtorTransferDataSource()
        
        // Act
        val result = dataSource.connectToDeviceWithTls(testDeviceInfo, "")
        
        // Assert
        assertTrue(result.isFailure, "Should fail with empty fingerprint")
        val exception = result.exceptionOrNull()
        assertNotNull(exception)
        assertTrue(
            exception is IllegalArgumentException,
            "Should throw IllegalArgumentException"
        )
    }
    
    @Test
    fun `connectToDeviceWithTls should fail if already connected`() = runTest {
        // Arrange
        val dataSource = KtorTransferDataSource()
        val fingerprint = "AA:BB:CC:DD:EE:FF:00:11:22:33:44:55:66:77:88:99:AA:BB:CC:DD:EE:FF:00:11:22:33:44:55:66:77:88:99"
        
        // Connect first time (will fail to connect but will set client state)
        dataSource.connectToDevice(testDeviceInfo)
        
        // Act - Try to connect with TLS while already connected
        val result = dataSource.connectToDeviceWithTls(testDeviceInfo, fingerprint)
        
        // Assert
        assertTrue(result.isFailure, "Should fail when already connected")
        
        // Cleanup
        dataSource.disconnectFromDevice()
    }
    
    // ========== Test: Certificate Validation ==========
    
    @Test
    fun `validateCertificateData should return true for valid data`() {
        // Arrange
        val validData = testCertificateData
        
        // Act
        val result = validateCertificateData(validData)
        
        // Assert
        assertTrue(result, "Valid certificate data should pass validation")
    }
    
    @Test
    fun `validateCertificateData should return false for invalid data`() {
        // Arrange
        val invalidData = testCertificateData.copy(certificate = ByteArray(0))
        
        // Act
        val result = validateCertificateData(invalidData)
        
        // Assert
        assertFalse(result, "Invalid certificate data should fail validation")
    }
    
    // ========== Test: Fingerprint Validation ==========
    
    @Test
    fun `validateFingerprint should return true for valid SHA-256 fingerprint`() {
        // Arrange
        val validFingerprint = "AA:BB:CC:DD:EE:FF:00:11:22:33:44:55:66:77:88:99:AA:BB:CC:DD:EE:FF:00:11:22:33:44:55:66:77:88:99"
        
        // Act
        val result = validateFingerprint(validFingerprint)
        
        // Assert
        assertTrue(result, "Valid SHA-256 fingerprint should pass validation")
    }
    
    @Test
    fun `validateFingerprint should return false for invalid fingerprint`() {
        // Arrange
        val invalidFingerprint = "INVALID"
        
        // Act
        val result = validateFingerprint(invalidFingerprint)
        
        // Assert
        assertFalse(result, "Invalid fingerprint should fail validation")
    }
    
    // ========== Test: TLS Protocol Support ==========
    
    @Test
    fun `getSupportedTlsProtocols should only include TLS 1_2 and higher`() {
        // Act
        val protocols = getSupportedTlsProtocols()
        
        // Assert
        assertNotNull(protocols)
        assertTrue(protocols.isNotEmpty(), "Should have supported protocols")
        assertTrue(
            protocols.all { it.contains("1.2") || it.contains("1.3") },
            "Should only support TLS 1.2 and 1.3"
        )
        assertFalse(
            protocols.any { it.contains("1.0") || it.contains("1.1") },
            "Should not support deprecated TLS versions"
        )
    }
}
