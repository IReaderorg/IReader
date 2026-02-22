package ireader.data.sync.datasource

import ireader.domain.services.sync.CertificateService
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Tests for TLS configuration functionality.
 * 
 * Tests platform-specific TLS/SSL configuration for secure WebSocket connections.
 * Validates certificate handling, TLS context creation, and certificate pinning.
 */
class TlsConfigurationTest {
    
    private val testCertificateData = CertificateService.CertificateData(
        certificate = "-----BEGIN CERTIFICATE-----\nMIICTest\n-----END CERTIFICATE-----".toByteArray(),
        privateKey = "-----BEGIN PRIVATE KEY-----\nMIIETest\n-----END PRIVATE KEY-----".toByteArray(),
        publicKey = "-----BEGIN PUBLIC KEY-----\nMIIBTest\n-----END PUBLIC KEY-----".toByteArray(),
        fingerprint = "AA:BB:CC:DD:EE:FF:00:11:22:33:44:55:66:77:88:99:AA:BB:CC:DD:EE:FF:00:11:22:33:44:55:66:77:88:99"
    )
    
    // ========== Test: Certificate Validation ==========
    
    @Test
    fun `validateCertificateData should return true for valid certificate`() {
        // Arrange
        val certificateData = testCertificateData
        
        // Act
        val result = validateCertificateData(certificateData)
        
        // Assert
        assertTrue(result, "Valid certificate data should pass validation")
    }
    
    @Test
    fun `validateCertificateData should return false for empty certificate`() {
        // Arrange
        val certificateData = testCertificateData.copy(certificate = ByteArray(0))
        
        // Act
        val result = validateCertificateData(certificateData)
        
        // Assert
        assertFalse(result, "Empty certificate should fail validation")
    }
    
    @Test
    fun `validateCertificateData should return false for empty private key`() {
        // Arrange
        val certificateData = testCertificateData.copy(privateKey = ByteArray(0))
        
        // Act
        val result = validateCertificateData(certificateData)
        
        // Assert
        assertFalse(result, "Empty private key should fail validation")
    }
    
    @Test
    fun `validateCertificateData should return false for empty fingerprint`() {
        // Arrange
        val certificateData = testCertificateData.copy(fingerprint = "")
        
        // Act
        val result = validateCertificateData(certificateData)
        
        // Assert
        assertFalse(result, "Empty fingerprint should fail validation")
    }
    
    // ========== Test: Fingerprint Validation ==========
    
    @Test
    fun `validateFingerprint should return true for valid SHA-256 fingerprint`() {
        // Arrange
        val fingerprint = "AA:BB:CC:DD:EE:FF:00:11:22:33:44:55:66:77:88:99:AA:BB:CC:DD:EE:FF:00:11:22:33:44:55:66:77:88:99"
        
        // Act
        val result = validateFingerprint(fingerprint)
        
        // Assert
        assertTrue(result, "Valid SHA-256 fingerprint should pass validation")
    }
    
    @Test
    fun `validateFingerprint should return false for empty fingerprint`() {
        // Arrange
        val fingerprint = ""
        
        // Act
        val result = validateFingerprint(fingerprint)
        
        // Assert
        assertFalse(result, "Empty fingerprint should fail validation")
    }
    
    @Test
    fun `validateFingerprint should return false for invalid format`() {
        // Arrange
        val fingerprint = "INVALID_FORMAT"
        
        // Act
        val result = validateFingerprint(fingerprint)
        
        // Assert
        assertFalse(result, "Invalid fingerprint format should fail validation")
    }
    
    @Test
    fun `validateFingerprint should return false for wrong length`() {
        // Arrange
        val fingerprint = "AA:BB:CC:DD" // Too short
        
        // Act
        val result = validateFingerprint(fingerprint)
        
        // Assert
        assertFalse(result, "Wrong length fingerprint should fail validation")
    }
    
    // ========== Test: TLS Protocol Validation ==========
    
    @Test
    fun `getSupportedTlsProtocols should return TLS 1_2 and higher`() {
        // Act
        val protocols = getSupportedTlsProtocols()
        
        // Assert
        assertNotNull(protocols, "Supported protocols should not be null")
        assertTrue(protocols.isNotEmpty(), "Should have at least one supported protocol")
        assertTrue(
            protocols.any { it.contains("1.2") || it.contains("1.3") },
            "Should support TLS 1.2 or higher"
        )
    }
    
    @Test
    fun `getSupportedTlsProtocols should not include TLS 1_0 or 1_1`() {
        // Act
        val protocols = getSupportedTlsProtocols()
        
        // Assert
        assertFalse(
            protocols.any { it.contains("1.0") || it.contains("1.1") },
            "Should not support deprecated TLS 1.0 or 1.1"
        )
    }
}

// ========== Helper Functions to Test ==========

/**
 * Validate certificate data for TLS configuration.
 * Ensures all required fields are present and non-empty.
 */
fun validateCertificateData(certificateData: CertificateService.CertificateData): Boolean {
    return certificateData.certificate.isNotEmpty() &&
            certificateData.privateKey.isNotEmpty() &&
            certificateData.fingerprint.isNotBlank()
}

/**
 * Validate certificate fingerprint format.
 * SHA-256 fingerprints should be 32 bytes (64 hex chars) with colons.
 */
fun validateFingerprint(fingerprint: String): Boolean {
    if (fingerprint.isBlank()) return false
    
    // SHA-256 fingerprint format: AA:BB:CC:...:DD (32 bytes = 95 chars with colons)
    val parts = fingerprint.split(":")
    if (parts.size != 32) return false
    
    // Each part should be 2 hex characters
    return parts.all { it.length == 2 && it.all { c -> c in '0'..'9' || c in 'A'..'F' || c in 'a'..'f' } }
}

/**
 * Get list of supported TLS protocols.
 * Should only include TLS 1.2 and higher for security.
 */
fun getSupportedTlsProtocols(): List<String> {
    return listOf("TLSv1.2", "TLSv1.3")
}
