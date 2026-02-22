package ireader.domain.services.sync

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

/**
 * TDD Tests for CertificateService (Phase 9.2.2 and 9.2.3)
 * 
 * Testing self-signed certificate generation and certificate pinning.
 */
class CertificateServiceTest {
    
    private lateinit var certificateService: CertificateService
    
    @Test
    fun `generateSelfSignedCertificate should create valid certificate`() = runTest {
        // Arrange
        certificateService = createCertificateService()
        val commonName = "test-device"
        
        // Act
        val result = certificateService.generateSelfSignedCertificate(commonName)
        
        // Assert
        assertTrue(result.isSuccess, "Certificate generation should succeed")
        val certData = result.getOrNull()!!
        assertTrue(certData.certificate.isNotEmpty(), "Certificate should not be empty")
        assertTrue(certData.privateKey.isNotEmpty(), "Private key should not be empty")
        assertTrue(certData.publicKey.isNotEmpty(), "Public key should not be empty")
        assertTrue(certData.fingerprint.isNotEmpty(), "Fingerprint should not be empty")
    }
    
    @Test
    fun `generateSelfSignedCertificate should create unique certificates`() = runTest {
        // Arrange
        certificateService = createCertificateService()
        val commonName = "test-device"
        
        // Act
        val result1 = certificateService.generateSelfSignedCertificate(commonName)
        val result2 = certificateService.generateSelfSignedCertificate(commonName)
        
        // Assert
        assertTrue(result1.isSuccess && result2.isSuccess)
        val cert1 = result1.getOrNull()!!
        val cert2 = result2.getOrNull()!!
        assertNotEquals(
            cert1.fingerprint,
            cert2.fingerprint,
            "Each certificate should have unique fingerprint"
        )
    }
    
    @Test
    fun `storeCertificate should save certificate data`() = runTest {
        // Arrange
        certificateService = createCertificateService()
        val alias = "test_cert_1"
        val certResult = certificateService.generateSelfSignedCertificate("test-device")
        val certData = certResult.getOrNull()!!
        
        // Act
        val storeResult = certificateService.storeCertificate(alias, certData)
        
        // Assert
        assertTrue(storeResult.isSuccess, "Certificate storage should succeed")
    }
    
    @Test
    fun `retrieveCertificate should return stored certificate`() = runTest {
        // Arrange
        certificateService = createCertificateService()
        val alias = "test_cert_2"
        val certResult = certificateService.generateSelfSignedCertificate("test-device")
        val originalCert = certResult.getOrNull()!!
        
        // Act
        certificateService.storeCertificate(alias, originalCert)
        val retrieveResult = certificateService.retrieveCertificate(alias)
        
        // Assert
        assertTrue(retrieveResult.isSuccess, "Certificate retrieval should succeed")
        val retrievedCert = retrieveResult.getOrNull()!!
        assertEquals(
            originalCert.fingerprint,
            retrievedCert.fingerprint,
            "Retrieved certificate should match stored certificate"
        )
    }
    
    @Test
    fun `retrieveCertificate should fail for non-existent certificate`() = runTest {
        // Arrange
        certificateService = createCertificateService()
        val alias = "non_existent_cert"
        
        // Act
        val result = certificateService.retrieveCertificate(alias)
        
        // Assert
        assertTrue(result.isFailure, "Retrieving non-existent certificate should fail")
    }
    
    @Test
    fun `calculateFingerprint should return consistent hash`() = runTest {
        // Arrange
        certificateService = createCertificateService()
        val certResult = certificateService.generateSelfSignedCertificate("test-device")
        val certData = certResult.getOrNull()!!
        
        // Act
        val fingerprint1 = certificateService.calculateFingerprint(certData.certificate)
        val fingerprint2 = certificateService.calculateFingerprint(certData.certificate)
        
        // Assert
        assertEquals(
            fingerprint1,
            fingerprint2,
            "Fingerprint should be consistent for same certificate"
        )
        assertEquals(
            certData.fingerprint,
            fingerprint1,
            "Calculated fingerprint should match stored fingerprint"
        )
    }
    
    @Test
    fun `verifyCertificateFingerprint should return true for matching fingerprint`() = runTest {
        // Arrange
        certificateService = createCertificateService()
        val certResult = certificateService.generateSelfSignedCertificate("test-device")
        val certData = certResult.getOrNull()!!
        
        // Act
        val isValid = certificateService.verifyCertificateFingerprint(
            certData.certificate,
            certData.fingerprint
        )
        
        // Assert
        assertTrue(isValid, "Fingerprint verification should succeed for matching fingerprint")
    }
    
    @Test
    fun `verifyCertificateFingerprint should return false for wrong fingerprint`() = runTest {
        // Arrange
        certificateService = createCertificateService()
        val certResult = certificateService.generateSelfSignedCertificate("test-device")
        val certData = certResult.getOrNull()!!
        val wrongFingerprint = "0000000000000000000000000000000000000000000000000000000000000000"
        
        // Act
        val isValid = certificateService.verifyCertificateFingerprint(
            certData.certificate,
            wrongFingerprint
        )
        
        // Assert
        assertFalse(isValid, "Fingerprint verification should fail for wrong fingerprint")
    }
    
    @Test
    fun `deleteCertificate should remove stored certificate`() = runTest {
        // Arrange
        certificateService = createCertificateService()
        val alias = "test_cert_3"
        val certResult = certificateService.generateSelfSignedCertificate("test-device")
        val certData = certResult.getOrNull()!!
        
        // Act
        certificateService.storeCertificate(alias, certData)
        val deleteResult = certificateService.deleteCertificate(alias)
        val retrieveResult = certificateService.retrieveCertificate(alias)
        
        // Assert
        assertTrue(deleteResult.isSuccess, "Certificate deletion should succeed")
        assertTrue(retrieveResult.isFailure, "Deleted certificate should not be retrievable")
    }
    
    @Test
    fun `certificateExists should return true for stored certificate`() = runTest {
        // Arrange
        certificateService = createCertificateService()
        val alias = "test_cert_4"
        val certResult = certificateService.generateSelfSignedCertificate("test-device")
        val certData = certResult.getOrNull()!!
        
        // Act
        certificateService.storeCertificate(alias, certData)
        val exists = certificateService.certificateExists(alias)
        
        // Assert
        assertTrue(exists, "Stored certificate should exist")
    }
    
    @Test
    fun `certificateExists should return false for non-existent certificate`() = runTest {
        // Arrange
        certificateService = createCertificateService()
        val alias = "non_existent_cert_2"
        
        // Act
        val exists = certificateService.certificateExists(alias)
        
        // Assert
        assertFalse(exists, "Non-existent certificate should not exist")
    }
    
    @Test
    fun `fingerprint should be 64 character hex string (SHA-256)`() = runTest {
        // Arrange
        certificateService = createCertificateService()
        val certResult = certificateService.generateSelfSignedCertificate("test-device")
        val certData = certResult.getOrNull()!!
        
        // Assert
        assertEquals(64, certData.fingerprint.length, "SHA-256 fingerprint should be 64 hex characters")
        assertTrue(
            certData.fingerprint.all { it in '0'..'9' || it in 'a'..'f' || it in 'A'..'F' },
            "Fingerprint should only contain hex characters"
        )
    }
}

/**
 * Platform-specific factory function for creating CertificateService.
 * Implemented in platform-specific test source sets.
 */
expect fun createCertificateService(): CertificateService
