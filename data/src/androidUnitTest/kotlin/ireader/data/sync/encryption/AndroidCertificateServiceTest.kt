package ireader.data.sync.encryption

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

/**
 * Android-specific tests for AndroidCertificateService.
 * 
 * Tests the complete certificate generation and storage flow using BouncyCastle.
 * Task 9.2.2: Self-signed certificate generation (Android)
 */
@RunWith(AndroidJUnit4::class)
class AndroidCertificateServiceTest {
    
    private lateinit var certificateService: AndroidCertificateService
    private lateinit var context: Context
    
    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        certificateService = AndroidCertificateService(context)
    }
    
    @After
    fun cleanup() = runTest {
        // Clean up any test certificates
        certificateService.deleteCertificate("test_cert_1")
        certificateService.deleteCertificate("test_cert_2")
        certificateService.deleteCertificate("test_cert_3")
        certificateService.deleteCertificate("test_cert_4")
    }
    
    @Test
    fun generateSelfSignedCertificate_shouldCreateValidCertificate() = runTest {
        // Arrange
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
    fun generateSelfSignedCertificate_shouldCreateUniqueCertificates() = runTest {
        // Arrange
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
    fun storeCertificate_shouldSaveCertificateData() = runTest {
        // Arrange
        val alias = "test_cert_1"
        val certResult = certificateService.generateSelfSignedCertificate("test-device")
        val certData = certResult.getOrNull()!!
        
        // Act
        val storeResult = certificateService.storeCertificate(alias, certData)
        
        // Assert
        assertTrue(storeResult.isSuccess, "Certificate storage should succeed")
    }
    
    @Test
    fun retrieveCertificate_shouldReturnStoredCertificate() = runTest {
        // Arrange
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
    fun retrieveCertificate_shouldFailForNonExistentCertificate() = runTest {
        // Arrange
        val alias = "non_existent_cert"
        
        // Act
        val result = certificateService.retrieveCertificate(alias)
        
        // Assert
        assertTrue(result.isFailure, "Retrieving non-existent certificate should fail")
    }
    
    @Test
    fun calculateFingerprint_shouldReturnConsistentHash() = runTest {
        // Arrange
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
    fun verifyCertificateFingerprint_shouldReturnTrueForMatchingFingerprint() = runTest {
        // Arrange
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
    fun verifyCertificateFingerprint_shouldReturnFalseForWrongFingerprint() = runTest {
        // Arrange
        val certResult = certificateService.generateSelfSignedCertificate("test-device")
        val certData = certResult.getOrNull()!!
        val wrongFingerprint = "00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00:00"
        
        // Act
        val isValid = certificateService.verifyCertificateFingerprint(
            certData.certificate,
            wrongFingerprint
        )
        
        // Assert
        assertFalse(isValid, "Fingerprint verification should fail for wrong fingerprint")
    }
    
    @Test
    fun deleteCertificate_shouldRemoveStoredCertificate() = runTest {
        // Arrange
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
    fun certificateExists_shouldReturnTrueForStoredCertificate() = runTest {
        // Arrange
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
    fun certificateExists_shouldReturnFalseForNonExistentCertificate() = runTest {
        // Arrange
        val alias = "non_existent_cert_2"
        
        // Act
        val exists = certificateService.certificateExists(alias)
        
        // Assert
        assertFalse(exists, "Non-existent certificate should not exist")
    }
    
    @Test
    fun fingerprint_shouldBe64CharacterHexStringWithColons() = runTest {
        // Arrange
        val certResult = certificateService.generateSelfSignedCertificate("test-device")
        val certData = certResult.getOrNull()!!
        
        // Assert
        // SHA-256 fingerprint format: XX:XX:XX:...:XX (32 bytes = 64 hex chars + 31 colons = 95 chars)
        assertEquals(95, certData.fingerprint.length, "SHA-256 fingerprint should be 95 characters with colons")
        
        // Verify format: hex pairs separated by colons
        val parts = certData.fingerprint.split(":")
        assertEquals(32, parts.size, "Should have 32 hex pairs")
        parts.forEach { part ->
            assertEquals(2, part.length, "Each part should be 2 hex characters")
            assertTrue(
                part.all { it in '0'..'9' || it in 'A'..'F' },
                "Each part should only contain uppercase hex characters"
            )
        }
    }
}
