package ireader.data.sync.datasource

import ireader.domain.services.sync.CertificateService
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Tests for Desktop TLS configuration.
 * 
 * Following TDD methodology:
 * 1. Write test first (RED)
 * 2. Implement minimal code (GREEN)
 * 3. Refactor (REFACTOR)
 */
class DesktopTlsConfigTest {
    
    // Test certificate data (self-signed for testing)
    private val testCertificatePem = """
        -----BEGIN CERTIFICATE-----
        MIICljCCAX4CCQCKz8Qr8vN8pDANBgkqhkiG9w0BAQsFADANMQswCQYDVQQGEwJV
        UzAeFw0yNDAxMDEwMDAwMDBaFw0yNTAxMDEwMDAwMDBaMA0xCzAJBgNVBAYTAlVT
        MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAtest
        -----END CERTIFICATE-----
    """.trimIndent().toByteArray()
    
    private val testPrivateKeyPem = """
        -----BEGIN PRIVATE KEY-----
        MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCtest
        -----END PRIVATE KEY-----
    """.trimIndent().toByteArray()
    
    private val testCertificateData = CertificateService.CertificateData(
        certificate = testCertificatePem,
        privateKey = testPrivateKeyPem,
        publicKey = ByteArray(0),
        fingerprint = "AA:BB:CC:DD:EE:FF:00:11:22:33:44:55:66:77:88:99:AA:BB:CC:DD:EE:FF:00:11:22:33:44:55:66:77:88:99"
    )
    
    // ========== Test: KeyStore Creation ==========
    
    @Test
    fun `createKeyStore should throw exception for empty certificate`() {
        // Arrange
        val invalidData = testCertificateData.copy(certificate = ByteArray(0))
        
        // Act & Assert
        val exception = assertFailsWith<IllegalArgumentException> {
            DesktopTlsConfig.createKeyStore(invalidData)
        }
        assertTrue(exception.message?.contains("Certificate cannot be empty") == true)
    }
    
    @Test
    fun `createKeyStore should throw exception for empty private key`() {
        // Arrange
        val invalidData = testCertificateData.copy(privateKey = ByteArray(0))
        
        // Act & Assert
        val exception = assertFailsWith<IllegalArgumentException> {
            DesktopTlsConfig.createKeyStore(invalidData)
        }
        assertTrue(exception.message?.contains("Private key cannot be empty") == true)
    }
    
    // ========== Test: SSLContext Creation ==========
    
    @Test
    fun `getSupportedTlsProtocols should return TLS 1_2 and 1_3`() {
        // Act
        val protocols = DesktopTlsConfig.getSupportedTlsProtocols()
        
        // Assert
        assertNotNull(protocols)
        assertEquals(2, protocols.size)
        assertTrue(protocols.contains("TLSv1.2"))
        assertTrue(protocols.contains("TLSv1.3"))
    }
    
    @Test
    fun `getSupportedTlsProtocols should not include deprecated protocols`() {
        // Act
        val protocols = DesktopTlsConfig.getSupportedTlsProtocols()
        
        // Assert
        assertTrue(!protocols.contains("TLSv1.0"))
        assertTrue(!protocols.contains("TLSv1.1"))
        assertTrue(!protocols.contains("SSLv3"))
    }
    
    // ========== Test: Certificate Pinning ==========
    
    @Test
    fun `createPinningTrustManager should throw exception for empty fingerprint`() {
        // Act & Assert
        val exception = assertFailsWith<IllegalArgumentException> {
            DesktopTlsConfig.createPinningTrustManager("")
        }
        assertTrue(exception.message?.contains("Expected fingerprint cannot be empty") == true)
    }
    
    @Test
    fun `createPinningTrustManager should create valid TrustManager`() {
        // Arrange
        val fingerprint = "AA:BB:CC:DD:EE:FF:00:11:22:33:44:55:66:77:88:99:AA:BB:CC:DD:EE:FF:00:11:22:33:44:55:66:77:88:99"
        
        // Act
        val trustManager = DesktopTlsConfig.createPinningTrustManager(fingerprint)
        
        // Assert
        assertNotNull(trustManager)
        assertNotNull(trustManager.acceptedIssuers)
        assertEquals(0, trustManager.acceptedIssuers.size)
    }
    
    // ========== Test: SSLContext with Pinning ==========
    
    @Test
    fun `createSslContextWithPinning should throw exception for empty fingerprint`() {
        // Act & Assert
        val exception = assertFailsWith<IllegalArgumentException> {
            DesktopTlsConfig.createSslContextWithPinning("")
        }
        assertTrue(exception.message?.contains("Expected fingerprint cannot be empty") == true)
    }
    
    @Test
    fun `createSslContextWithPinning should create valid SSLContext`() {
        // Arrange
        val fingerprint = "AA:BB:CC:DD:EE:FF:00:11:22:33:44:55:66:77:88:99:AA:BB:CC:DD:EE:FF:00:11:22:33:44:55:66:77:88:99"
        
        // Act
        val sslContext = DesktopTlsConfig.createSslContextWithPinning(fingerprint)
        
        // Assert
        assertNotNull(sslContext)
        assertEquals("TLS", sslContext.protocol)
    }
}
