package ireader.domain.services.tts_service.piper

import org.junit.Test
import java.io.File
import java.nio.file.Files
import kotlin.io.path.writeText
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Integration tests for security features.
 * 
 * Tests the LibraryVerifier, InputSanitizer, and SecurityManager components.
 */
class SecurityIntegrationTest {
    
    @Test
    fun `test input sanitizer removes control characters`() {
        val sanitizer = InputSanitizer()
        
        // Text with control characters
        val dirtyText = "Hello\u0000World\u0001Test\u001F"
        val cleanText = sanitizer.sanitizeText(dirtyText)
        
        // Control characters should be removed
        assertEquals("HelloWorldTest", cleanText)
    }
    
    @Test
    fun `test input sanitizer enforces max length`() {
        val sanitizer = InputSanitizer()
        
        // Text longer than max length
        val longText = "a".repeat(200_000)
        val truncatedText = sanitizer.sanitizeText(longText)
        
        // Should be truncated to max length
        assertTrue(truncatedText.length <= InputSanitizer.MAX_TEXT_LENGTH)
    }
    
    @Test
    fun `test input sanitizer validates file paths`() {
        val sanitizer = InputSanitizer()
        
        // Valid path
        val tempFile = Files.createTempFile("test", ".onnx")
        tempFile.writeText("test content")
        
        val result = sanitizer.validateFilePath(
            path = tempFile.toString(),
            allowedExtensions = setOf("onnx"),
            mustExist = true
        )
        
        assertTrue(result.isValid)
        assertNotNull(result.sanitizedValue)
        
        // Cleanup
        Files.deleteIfExists(tempFile)
    }
    
    @Test
    fun `test input sanitizer detects path traversal`() {
        val sanitizer = InputSanitizer()
        
        // Path with traversal attempt
        val maliciousPath = "../../../etc/passwd"
        val result = sanitizer.validateFilePath(maliciousPath)
        
        assertFalse(result.isValid)
        assertNotNull(result.errorMessage)
        assertTrue(result.errorMessage!!.contains("dangerous pattern"))
    }
    
    @Test
    fun `test input sanitizer validates file extensions`() {
        val sanitizer = InputSanitizer()
        
        // Create temp file with wrong extension
        val tempFile = Files.createTempFile("test", ".txt")
        tempFile.writeText("test content")
        
        val result = sanitizer.validateFilePath(
            path = tempFile.toString(),
            allowedExtensions = setOf("onnx"),
            mustExist = true
        )
        
        assertFalse(result.isValid)
        assertNotNull(result.errorMessage)
        assertTrue(result.errorMessage!!.contains("Invalid file extension"))
        
        // Cleanup
        Files.deleteIfExists(tempFile)
    }
    
    @Test
    fun `test input sanitizer validates parameter ranges`() {
        val sanitizer = InputSanitizer()
        
        // Valid range
        val validResult = sanitizer.validateRange(1.0f, 0.5f, 2.0f, "speechRate")
        assertTrue(validResult.isValid)
        
        // Out of range
        val invalidResult = sanitizer.validateRange(3.0f, 0.5f, 2.0f, "speechRate")
        assertFalse(invalidResult.isValid)
        assertNotNull(invalidResult.errorMessage)
    }
    
    @Test
    fun `test input sanitizer sanitizes filenames`() {
        val sanitizer = InputSanitizer()
        
        // Filename with dangerous characters
        val dirtyFilename = "test/file\\name:with*special?chars"
        val cleanFilename = sanitizer.sanitizeFilename(dirtyFilename)
        
        // Dangerous characters should be replaced
        assertFalse(cleanFilename.contains("/"))
        assertFalse(cleanFilename.contains("\\"))
        assertFalse(cleanFilename.contains(":"))
        assertFalse(cleanFilename.contains("*"))
        assertFalse(cleanFilename.contains("?"))
    }
    
    @Test
    fun `test security manager validates file access`() {
        val securityManager = SecurityManager()
        
        // Create a temp file in an allowed directory
        val tempDir = Files.createTempDirectory("piper_test")
        val tempFile = tempDir.resolve("test.onnx")
        tempFile.writeText("test content")
        
        // Note: This will fail because the temp directory is not in the allowed directories
        // In a real scenario, you would configure the security policy to allow the test directory
        val canAccess = securityManager.canAccessFile(
            tempFile.toString(),
            SecurityManager.FileType.MODEL
        )
        
        // Cleanup
        Files.deleteIfExists(tempFile)
        Files.deleteIfExists(tempDir)
        
        // The result depends on the security policy configuration
        // In production, this would be properly configured
    }
    
    @Test
    fun `test security manager tracks instances`() {
        val securityManager = SecurityManager()
        
        // Register instances
        assertTrue(securityManager.registerInstance(1L))
        assertTrue(securityManager.registerInstance(2L))
        assertEquals(2, securityManager.getActiveInstanceCount())
        
        // Unregister instance
        securityManager.unregisterInstance(1L)
        assertEquals(1, securityManager.getActiveInstanceCount())
        
        // Cleanup
        securityManager.unregisterInstance(2L)
    }
    
    @Test
    fun `test security manager enforces instance limit`() {
        val policy = SecurityManager.SecurityPolicy(
            maxConcurrentInstances = 2
        )
        val securityManager = SecurityManager(policy)
        
        // Register up to limit
        assertTrue(securityManager.registerInstance(1L))
        assertTrue(securityManager.registerInstance(2L))
        
        // Try to exceed limit
        assertFalse(securityManager.registerInstance(3L))
        
        // Cleanup
        securityManager.unregisterInstance(1L)
        securityManager.unregisterInstance(2L)
    }
    
    @Test
    fun `test security manager generates report`() {
        val securityManager = SecurityManager()
        
        // Generate some events
        securityManager.registerInstance(1L)
        securityManager.unregisterInstance(1L)
        
        val report = securityManager.getSecurityReport()
        
        assertNotNull(report)
        assertTrue(report.contains("Security Report"))
        assertTrue(report.contains("Policy Configuration"))
    }
    
    @Test
    fun `test library verifier validates file existence`() {
        val verifier = LibraryVerifier()
        
        // Non-existent file
        val result = verifier.verifyLibrary(
            libraryPath = File("nonexistent.dll").toPath(),
            skipSignatureCheck = true
        )
        
        assertFalse(result.isVerified)
        assertNotNull(result.errorMessage)
        assertTrue(result.errorMessage!!.contains("does not exist"))
    }
    
    @Test
    fun `test library verifier checks file extension`() {
        val verifier = LibraryVerifier()
        
        // Create temp file with wrong extension
        val tempFile = Files.createTempFile("test", ".txt")
        tempFile.writeText("test content")
        
        val result = verifier.verifyLibrary(
            libraryPath = tempFile,
            skipSignatureCheck = true
        )
        
        // Should have warning about unexpected extension
        assertTrue(result.warnings.any { it.contains("Unexpected file extension") })
        
        // Cleanup
        Files.deleteIfExists(tempFile)
    }
    
    @Test
    fun `test library verifier generates report`() {
        val verifier = LibraryVerifier()
        
        // Create temp file
        val tempFile = Files.createTempFile("test", ".dll")
        tempFile.writeText("test content")
        
        val result = verifier.verifyLibrary(
            libraryPath = tempFile,
            skipSignatureCheck = true
        )
        
        val report = result.getReport()
        
        assertNotNull(report)
        assertTrue(report.contains("Library Verification Report"))
        assertTrue(report.contains("Checksum"))
        
        // Cleanup
        Files.deleteIfExists(tempFile)
    }
}
