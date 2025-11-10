package ireader.domain.services.tts_service.piper

import java.io.File
import java.nio.file.Path
import java.security.MessageDigest
import kotlin.io.path.exists
import kotlin.io.path.extension
import kotlin.io.path.fileSize
import kotlin.io.path.inputStream

/**
 * Verifies the integrity and authenticity of native libraries.
 * 
 * This class provides comprehensive verification of native libraries including:
 * - Checksum verification (SHA-256)
 * - Code signature verification (Windows/macOS)
 * - File integrity checks
 * - Security logging
 * 
 * The verifier helps ensure that:
 * - Libraries haven't been tampered with
 * - Libraries are authentic and from trusted sources
 * - Libraries meet security requirements
 * 
 * Usage:
 * ```kotlin
 * val verifier = LibraryVerifier()
 * val result = verifier.verifyLibrary(libraryPath)
 * if (result.isSuccess) {
 *     // Library is verified and safe to load
 * } else {
 *     // Handle verification failure
 *     println(result.errorMessage)
 * }
 * ```
 * 
 * @see NativeLibraryLoader
 * @see SecurityManager
 */
class LibraryVerifier {
    
    /**
     * Result of library verification.
     */
    data class VerificationResult(
        val isVerified: Boolean,
        val libraryPath: Path,
        val checksumMatch: Boolean = false,
        val signatureValid: Boolean = false,
        val errorMessage: String? = null,
        val warnings: List<String> = emptyList()
    ) {
        val isSuccess: Boolean get() = isVerified && errorMessage == null
        
        /**
         * Get a detailed verification report.
         */
        fun getReport(): String = buildString {
            appendLine("=== Library Verification Report ===")
            appendLine()
            appendLine("Library: ${libraryPath.fileName}")
            appendLine("Path: $libraryPath")
            appendLine("Status: ${if (isVerified) "VERIFIED" else "FAILED"}")
            appendLine()
            appendLine("Checks:")
            appendLine("  Checksum: ${if (checksumMatch) "✓ PASS" else "✗ FAIL"}")
            appendLine("  Signature: ${if (signatureValid) "✓ PASS" else "- SKIP"}")
            appendLine()
            
            if (warnings.isNotEmpty()) {
                appendLine("Warnings:")
                warnings.forEach { appendLine("  ⚠ $it") }
                appendLine()
            }
            
            if (errorMessage != null) {
                appendLine("Error: $errorMessage")
            }
        }
    }
    
    /**
     * Expected checksums for known library versions.
     * 
     * In production, these should be loaded from a secure configuration file
     * or embedded in the application with code signing.
     * 
     * Format: libraryName -> SHA-256 checksum
     */
    private val knownChecksums = mapOf(
        // Windows libraries
        "piper_jni.dll" to "BA2CE5E17DC4579F04445DDC824030F8237D02915DDA626C8E7BF9CAAF0128A1",
        "onnxruntime.dll" to "PLACEHOLDER_CHECKSUM_WINDOWS_ONNXRUNTIME",
        
        // macOS x64 libraries
        "libpiper_jni.dylib" to "PLACEHOLDER_CHECKSUM_MACOS_X64_PIPER_JNI",
        "libonnxruntime.dylib" to "PLACEHOLDER_CHECKSUM_MACOS_X64_ONNXRUNTIME",
        
        // macOS ARM64 libraries (different checksums for different architectures)
        "libpiper_jni_arm64.dylib" to "PLACEHOLDER_CHECKSUM_MACOS_ARM64_PIPER_JNI",
        "libonnxruntime_arm64.dylib" to "PLACEHOLDER_CHECKSUM_MACOS_ARM64_ONNXRUNTIME",
        
        // Linux libraries
        "libpiper_jni.so" to "PLACEHOLDER_CHECKSUM_LINUX_PIPER_JNI",
        "libonnxruntime.so" to "PLACEHOLDER_CHECKSUM_LINUX_ONNXRUNTIME"
    )
    
    /**
     * Verify a native library's integrity and authenticity.
     * 
     * Performs comprehensive verification including:
     * 1. Basic file checks (exists, readable, non-empty)
     * 2. Checksum verification (SHA-256)
     * 3. Platform-specific signature verification (Windows/macOS)
     * 4. Security logging
     * 
     * @param libraryPath Path to the library file to verify
     * @param skipSignatureCheck If true, skip code signature verification (for testing)
     * @return VerificationResult containing verification status and details
     */
    fun verifyLibrary(
        libraryPath: Path,
        skipSignatureCheck: Boolean = false
    ): VerificationResult {
        val warnings = mutableListOf<String>()
        
        try {
            // Step 1: Basic file checks
            if (!libraryPath.exists()) {
                return VerificationResult(
                    isVerified = false,
                    libraryPath = libraryPath,
                    errorMessage = "Library file does not exist"
                )
            }
            
            if (!libraryPath.toFile().canRead()) {
                return VerificationResult(
                    isVerified = false,
                    libraryPath = libraryPath,
                    errorMessage = "Library file is not readable"
                )
            }
            
            if (libraryPath.fileSize() == 0L) {
                return VerificationResult(
                    isVerified = false,
                    libraryPath = libraryPath,
                    errorMessage = "Library file is empty"
                )
            }
            
            // Step 2: Verify file extension
            val validExtensions = listOf("dll", "dylib", "so")
            if (libraryPath.extension !in validExtensions) {
                warnings.add("Unexpected file extension: ${libraryPath.extension}")
            }
            
            // Step 3: Checksum verification
            val checksumMatch = verifyChecksum(libraryPath, warnings)
            
            // Step 4: Platform-specific signature verification
            val signatureValid = if (!skipSignatureCheck) {
                verifySignature(libraryPath, warnings)
            } else {
                warnings.add("Signature verification skipped")
                false
            }
            
            // Step 5: Determine overall verification status
            // Library is considered verified if checksum matches OR signature is valid
            // (Some platforms may not support signature verification)
            val isVerified = checksumMatch || signatureValid
            
            val result = VerificationResult(
                isVerified = isVerified,
                libraryPath = libraryPath,
                checksumMatch = checksumMatch,
                signatureValid = signatureValid,
                errorMessage = if (!isVerified) "Verification failed: checksum mismatch and no valid signature" else null,
                warnings = warnings
            )
            
            // Log verification result
            logVerificationResult(result)
            
            return result
            
        } catch (e: Exception) {
            val result = VerificationResult(
                isVerified = false,
                libraryPath = libraryPath,
                errorMessage = "Verification error: ${e.message}",
                warnings = warnings
            )
            logVerificationResult(result)
            return result
        }
    }
    
    /**
     * Verify the SHA-256 checksum of a library file.
     * 
     * @param libraryPath Path to the library file
     * @param warnings Mutable list to add warnings to
     * @return true if checksum matches, false otherwise
     */
    private fun verifyChecksum(libraryPath: Path, warnings: MutableList<String>): Boolean {
        try {
            val libraryName = libraryPath.fileName.toString()
            val expectedChecksum = knownChecksums[libraryName]
            
            if (expectedChecksum == null) {
                warnings.add("No known checksum for library: $libraryName")
                return false
            }
            
            // Check if this is a placeholder checksum (not yet configured)
            if (expectedChecksum.startsWith("PLACEHOLDER_")) {
                warnings.add("Checksum verification not configured for: $libraryName")
                return false
            }
            
            val actualChecksum = calculateSHA256(libraryPath)
            
            if (actualChecksum.equals(expectedChecksum, ignoreCase = true)) {
                return true
            } else {
                warnings.add("Checksum mismatch for $libraryName")
                warnings.add("  Expected: $expectedChecksum")
                warnings.add("  Actual:   $actualChecksum")
                return false
            }
            
        } catch (e: Exception) {
            warnings.add("Checksum calculation failed: ${e.message}")
            return false
        }
    }
    
    /**
     * Calculate SHA-256 checksum of a file.
     * 
     * @param file Path to the file
     * @return Hex-encoded SHA-256 checksum
     */
    private fun calculateSHA256(file: Path): String {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input ->
            val buffer = ByteArray(8192)
            var bytesRead: Int
            while (input.read(buffer).also { bytesRead = it } != -1) {
                digest.update(buffer, 0, bytesRead)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }
    
    /**
     * Verify the code signature of a library (platform-specific).
     * 
     * @param libraryPath Path to the library file
     * @param warnings Mutable list to add warnings to
     * @return true if signature is valid, false otherwise or if not supported
     */
    private fun verifySignature(libraryPath: Path, warnings: MutableList<String>): Boolean {
        val osName = System.getProperty("os.name").lowercase()
        
        return when {
            osName.contains("win") -> verifyWindowsSignature(libraryPath, warnings)
            osName.contains("mac") || osName.contains("darwin") -> verifyMacOSSignature(libraryPath, warnings)
            else -> {
                warnings.add("Code signature verification not supported on this platform")
                false
            }
        }
    }
    
    /**
     * Verify Windows Authenticode signature.
     * 
     * Uses PowerShell's Get-AuthenticodeSignature cmdlet to verify the signature.
     * 
     * @param libraryPath Path to the library file
     * @param warnings Mutable list to add warnings to
     * @return true if signature is valid, false otherwise
     */
    private fun verifyWindowsSignature(libraryPath: Path, warnings: MutableList<String>): Boolean {
        try {
            val command = listOf(
                "powershell.exe",
                "-NoProfile",
                "-Command",
                "Get-AuthenticodeSignature -FilePath '${libraryPath.toAbsolutePath()}' | Select-Object -ExpandProperty Status"
            )
            
            val process = ProcessBuilder(command)
                .redirectErrorStream(true)
                .start()
            
            val output = process.inputStream.bufferedReader().readText().trim()
            val exitCode = process.waitFor()
            
            if (exitCode != 0) {
                warnings.add("Failed to verify Windows signature: exit code $exitCode")
                return false
            }
            
            return when (output) {
                "Valid" -> true
                "NotSigned" -> {
                    warnings.add("Library is not signed")
                    false
                }
                else -> {
                    warnings.add("Invalid signature status: $output")
                    false
                }
            }
            
        } catch (e: Exception) {
            warnings.add("Windows signature verification failed: ${e.message}")
            return false
        }
    }
    
    /**
     * Verify macOS code signature.
     * 
     * Uses the codesign utility to verify the signature.
     * 
     * @param libraryPath Path to the library file
     * @param warnings Mutable list to add warnings to
     * @return true if signature is valid, false otherwise
     */
    private fun verifyMacOSSignature(libraryPath: Path, warnings: MutableList<String>): Boolean {
        try {
            val command = listOf(
                "codesign",
                "--verify",
                "--verbose",
                libraryPath.toAbsolutePath().toString()
            )
            
            val process = ProcessBuilder(command)
                .redirectErrorStream(true)
                .start()
            
            val output = process.inputStream.bufferedReader().readText()
            val exitCode = process.waitFor()
            
            if (exitCode == 0) {
                return true
            } else {
                warnings.add("macOS signature verification failed")
                if (output.isNotBlank()) {
                    warnings.add("  $output")
                }
                return false
            }
            
        } catch (e: Exception) {
            warnings.add("macOS signature verification failed: ${e.message}")
            return false
        }
    }
    
    /**
     * Log verification result for security auditing.
     * 
     * @param result The verification result to log
     */
    private fun logVerificationResult(result: VerificationResult) {
        val status = if (result.isVerified) "VERIFIED" else "FAILED"
        val libraryName = result.libraryPath.fileName
        
        println("Library Verification [$status]: $libraryName")
        
        if (!result.isVerified) {
            println("  Error: ${result.errorMessage}")
        }
        
        if (result.warnings.isNotEmpty()) {
            println("  Warnings:")
            result.warnings.forEach { println("    - $it") }
        }
        
        // In production, this should also log to a security audit log
        // SecurityAuditLogger.log("LIBRARY_VERIFICATION", result)
    }
    
    /**
     * Verify multiple libraries at once.
     * 
     * @param libraryPaths List of library paths to verify
     * @param skipSignatureCheck If true, skip code signature verification
     * @return Map of library path to verification result
     */
    fun verifyLibraries(
        libraryPaths: List<Path>,
        skipSignatureCheck: Boolean = false
    ): Map<Path, VerificationResult> {
        return libraryPaths.associateWith { path ->
            verifyLibrary(path, skipSignatureCheck)
        }
    }
    
    /**
     * Check if all libraries in a list are verified.
     * 
     * @param libraryPaths List of library paths to verify
     * @param skipSignatureCheck If true, skip code signature verification
     * @return true if all libraries are verified, false otherwise
     */
    fun verifyAllLibraries(
        libraryPaths: List<Path>,
        skipSignatureCheck: Boolean = false
    ): Boolean {
        val results = verifyLibraries(libraryPaths, skipSignatureCheck)
        return results.values.all { it.isVerified }
    }
    
    /**
     * Get the expected checksum for a library.
     * 
     * This is useful for generating checksums for new library versions.
     * 
     * @param libraryName Name of the library file
     * @return Expected checksum, or null if not known
     */
    fun getExpectedChecksum(libraryName: String): String? {
        return knownChecksums[libraryName]
    }
    
    /**
     * Calculate and print checksums for libraries (for configuration).
     * 
     * This utility method helps generate checksums for new library versions.
     * The output can be used to update the knownChecksums map.
     * 
     * @param libraryPaths List of library paths to calculate checksums for
     */
    fun generateChecksums(libraryPaths: List<Path>) {
        println("=== Library Checksums (SHA-256) ===")
        println()
        
        libraryPaths.forEach { path ->
            try {
                val checksum = calculateSHA256(path)
                val libraryName = path.fileName.toString()
                println("\"$libraryName\" to \"$checksum\",")
            } catch (e: Exception) {
                println("// Error calculating checksum for ${path.fileName}: ${e.message}")
            }
        }
        
        println()
    }
}
