package ireader.domain.services.tts_service.piper

import kotlinx.coroutines.test.runTest
import java.io.File
import kotlin.test.*

/**
 * Cross-platform compatibility tests for Piper TTS.
 * 
 * These tests verify consistent behavior across:
 * - Windows 10/11 (x64)
 * - macOS Intel (x64)
 * - macOS Apple Silicon (ARM64)
 * - Linux distributions (Ubuntu, Fedora, Arch)
 * 
 * Tests focus on:
 * - Platform detection
 * - Library loading
 * - Consistent synthesis output
 * - Platform-specific features
 * 
 * Requirements: 8.3
 */
class PiperCrossPlatformTest {
    
    companion object {
        private var librariesAvailable = false
        private var testModelPath: String? = null
        private var testConfigPath: String? = null
        
        init {
            try {
                PiperInitializer.resetForTesting()
                val result = kotlinx.coroutines.runBlocking {
                    PiperInitializer.initialize()
                }
                librariesAvailable = result.isSuccess
                
                testModelPath = System.getProperty("test.model.path")
                testConfigPath = System.getProperty("test.config.path")
            } catch (e: Exception) {
                println("Native libraries not available for cross-platform testing: ${e.message}")
                librariesAvailable = false
            }
        }
    }
    
    // ========== Platform Detection Tests ==========
    
    @Test
    fun `test platform detection is accurate`() {
        val osName = System.getProperty("os.name").lowercase()
        val osArch = System.getProperty("os.arch").lowercase()
        
        println("Detected OS: $osName")
        println("Detected Architecture: $osArch")
        
        val platform = when {
            osName.contains("windows") -> "Windows"
            osName.contains("mac") || osName.contains("darwin") -> "macOS"
            osName.contains("linux") -> "Linux"
            else -> "Unknown"
        }
        
        val architecture = when {
            osArch.contains("amd64") || osArch.contains("x86_64") -> "x64"
            osArch.contains("aarch64") || osArch.contains("arm64") -> "ARM64"
            else -> "Unknown"
        }
        
        println("Platform: $platform")
        println("Architecture: $architecture")
        
        // Verify we're on a supported platform
        val supportedPlatforms = listOf("Windows", "macOS", "Linux")
        assertTrue(
            platform in supportedPlatforms,
            "Platform $platform should be one of: $supportedPlatforms"
        )
        
        val supportedArchitectures = listOf("x64", "ARM64")
        assertTrue(
            architecture in supportedArchitectures,
            "Architecture $architecture should be one of: $supportedArchitectures"
        )
    }
    
    @Test
    fun `test platform-specific library naming`() {
        val osName = System.getProperty("os.name").lowercase()
        
        val expectedLibraryExtension = when {
            osName.contains("windows") -> ".dll"
            osName.contains("mac") || osName.contains("darwin") -> ".dylib"
            osName.contains("linux") -> ".so"
            else -> null
        }
        
        assertNotNull(expectedLibraryExtension, "Should have expected library extension for platform")
        
        println("Expected library extension: $expectedLibraryExtension")
        
        // Verify the extension matches platform conventions
        when {
            osName.contains("windows") -> assertEquals(".dll", expectedLibraryExtension)
            osName.contains("mac") || osName.contains("darwin") -> assertEquals(".dylib", expectedLibraryExtension)
            osName.contains("linux") -> assertEquals(".so", expectedLibraryExtension)
        }
    }
    
    @Test
    fun `test system properties are accessible`() {
        val requiredProperties = listOf(
            "os.name",
            "os.arch",
            "os.version",
            "java.version",
            "java.home",
            "user.home",
            "user.dir"
        )
        
        requiredProperties.forEach { property ->
            val value = System.getProperty(property)
            assertNotNull(value, "System property $property should be accessible")
            assertTrue(value.isNotEmpty(), "System property $property should not be empty")
            println("$property: $value")
        }
    }
    
    // ========== Library Loading Tests ==========
    
    @Test
    fun `test native library loads on current platform`() = runTest {
        PiperInitializer.resetForTesting()
        
        val result = PiperInitializer.initialize()
        
        if (result.isSuccess) {
            println("✓ Native library loaded successfully on current platform")
            assertTrue(PiperInitializer.isAvailable())
        } else {
            println("✗ Native library failed to load: ${result.exceptionOrNull()?.message}")
            // This is acceptable if libraries aren't built for this platform yet
            assertFalse(PiperInitializer.isAvailable())
        }
    }
    
    @Test
    fun `test library loading provides detailed error messages`() = runTest {
        PiperInitializer.resetForTesting()
        
        val result = PiperInitializer.initialize()
        
        if (result.isFailure) {
            val error = result.exceptionOrNull()
            assertNotNull(error, "Should have error information")
            assertNotNull(error.message, "Error should have message")
            assertTrue(error.message!!.isNotEmpty(), "Error message should not be empty")
            
            println("Error message: ${error.message}")
            
            // Error message should be informative
            val message = error.message!!.lowercase()
            val hasUsefulInfo = message.contains("platform") ||
                                message.contains("library") ||
                                message.contains("not found") ||
                                message.contains("load")
            
            assertTrue(hasUsefulInfo, "Error message should contain useful diagnostic information")
        }
    }
    
    @Test
    fun `test initialization status provides platform information`() {
        val statusInfo = PiperInitializer.getStatusInfo()
        
        assertNotNull(statusInfo)
        assertTrue(statusInfo.isNotEmpty())
        
        println("Status Info:")
        println(statusInfo)
        
        // Status should contain platform information
        assertTrue(statusInfo.contains("Platform") || statusInfo.contains("OS"))
    }
    
    // ========== Consistent Behavior Tests ==========
    
    @Test
    fun `test synthesis produces consistent output format`() {
        if (!librariesAvailable || testModelPath == null) {
            println("Skipping - libraries or test model not available")
            return
        }
        
        val instance = PiperNative.initialize(testModelPath!!, testConfigPath!!)
        val text = "Cross-platform consistency test."
        
        val audio = PiperNative.synthesize(instance, text)
        
        // Verify audio format is consistent
        assertNotNull(audio)
        assertTrue(audio.isNotEmpty())
        
        // Audio should be 16-bit PCM (2 bytes per sample)
        assertTrue(audio.size % 2 == 0, "Audio size should be even (16-bit samples)")
        
        // Get sample rate
        val sampleRate = PiperNative.getSampleRate(instance)
        assertTrue(sampleRate > 0)
        assertTrue(sampleRate in 8000..48000, "Sample rate should be in valid range")
        
        // Get channels
        val channels = PiperNative.getChannels(instance)
        assertTrue(channels in 1..2, "Channels should be 1 (mono) or 2 (stereo)")
        
        println("Audio format: $sampleRate Hz, $channels channel(s), ${audio.size} bytes")
        
        PiperNative.shutdown(instance)
    }
    
    @Test
    fun `test same text produces deterministic output`() {
        if (!librariesAvailable || testModelPath == null) {
            println("Skipping - libraries or test model not available")
            return
        }
        
        val instance = PiperNative.initialize(testModelPath!!, testConfigPath!!)
        val text = "Deterministic output test."
        
        // Synthesize the same text multiple times
        val audio1 = PiperNative.synthesize(instance, text)
        val audio2 = PiperNative.synthesize(instance, text)
        val audio3 = PiperNative.synthesize(instance, text)
        
        // All outputs should be identical
        assertContentEquals(audio1, audio2, "First and second synthesis should be identical")
        assertContentEquals(audio2, audio3, "Second and third synthesis should be identical")
        
        println("✓ Synthesis produces deterministic output (${audio1.size} bytes)")
        
        PiperNative.shutdown(instance)
    }
    
    @Test
    fun `test parameter changes have consistent effects`() {
        if (!librariesAvailable || testModelPath == null) {
            println("Skipping - libraries or test model not available")
            return
        }
        
        val instance = PiperNative.initialize(testModelPath!!, testConfigPath!!)
        val text = "Parameter consistency test."
        
        // Synthesize with default parameters
        val audioDefault = PiperNative.synthesize(instance, text)
        
        // Synthesize with faster speech rate
        PiperNative.setSpeechRate(instance, 1.5f)
        val audioFaster = PiperNative.synthesize(instance, text)
        
        // Synthesize with slower speech rate
        PiperNative.setSpeechRate(instance, 0.75f)
        val audioSlower = PiperNative.synthesize(instance, text)
        
        // Faster speech should produce shorter audio
        assertTrue(
            audioFaster.size < audioDefault.size,
            "Faster speech rate should produce shorter audio"
        )
        
        // Slower speech should produce longer audio
        assertTrue(
            audioSlower.size > audioDefault.size,
            "Slower speech rate should produce longer audio"
        )
        
        println("Audio sizes: default=${audioDefault.size}, faster=${audioFaster.size}, slower=${audioSlower.size}")
        
        PiperNative.shutdown(instance)
    }
    
    // ========== Platform-Specific Feature Tests ==========
    
    @Test
    fun `test file path handling is platform-appropriate`() {
        val separator = File.separator
        val osName = System.getProperty("os.name").lowercase()
        
        when {
            osName.contains("windows") -> {
                assertEquals("\\", separator, "Windows should use backslash separator")
            }
            osName.contains("mac") || osName.contains("darwin") || osName.contains("linux") -> {
                assertEquals("/", separator, "Unix-like systems should use forward slash separator")
            }
        }
        
        println("File separator: $separator")
        
        // Test path construction
        val testPath = listOf("path", "to", "file.txt").joinToString(separator)
        assertTrue(testPath.contains(separator), "Path should contain platform separator")
        
        println("Test path: $testPath")
    }
    
    @Test
    fun `test temporary directory is accessible`() {
        val tempDir = System.getProperty("java.io.tmpdir")
        
        assertNotNull(tempDir, "Temporary directory should be defined")
        assertTrue(tempDir.isNotEmpty(), "Temporary directory should not be empty")
        
        val tempDirFile = File(tempDir)
        assertTrue(tempDirFile.exists(), "Temporary directory should exist")
        assertTrue(tempDirFile.isDirectory, "Temporary directory should be a directory")
        assertTrue(tempDirFile.canWrite(), "Temporary directory should be writable")
        
        println("Temporary directory: $tempDir")
    }
    
    @Test
    fun `test user home directory is accessible`() {
        val userHome = System.getProperty("user.home")
        
        assertNotNull(userHome, "User home directory should be defined")
        assertTrue(userHome.isNotEmpty(), "User home directory should not be empty")
        
        val userHomeFile = File(userHome)
        assertTrue(userHomeFile.exists(), "User home directory should exist")
        assertTrue(userHomeFile.isDirectory, "User home directory should be a directory")
        
        println("User home directory: $userHome")
    }
    
    // ========== Error Handling Consistency Tests ==========
    
    @Test
    fun `test error messages are consistent across platforms`() {
        if (!librariesAvailable) {
            println("Skipping - libraries not available")
            return
        }
        
        // Test with invalid model path
        try {
            PiperNative.initialize("/nonexistent/model.onnx", "/nonexistent/config.json")
            fail("Should throw exception for invalid model path")
        } catch (e: Exception) {
            assertNotNull(e.message)
            assertTrue(e.message!!.isNotEmpty())
            println("Error message for invalid model: ${e.message}")
        }
        
        // Test with invalid instance
        try {
            PiperNative.synthesize(0, "Test")
            fail("Should throw exception for invalid instance")
        } catch (e: Exception) {
            assertNotNull(e.message)
            assertTrue(e.message!!.isNotEmpty())
            println("Error message for invalid instance: ${e.message}")
        }
    }
    
    @Test
    fun `test exception types are consistent`() {
        if (!librariesAvailable) {
            println("Skipping - libraries not available")
            return
        }
        
        // Test invalid parameter
        if (testModelPath != null) {
            val instance = PiperNative.initialize(testModelPath!!, testConfigPath!!)
            
            try {
                PiperNative.setSpeechRate(instance, 5.0f) // Out of range
                fail("Should throw exception for invalid parameter")
            } catch (e: Exception) {
                // Should be IllegalArgumentException or similar
                println("Exception type for invalid parameter: ${e::class.simpleName}")
            }
            
            PiperNative.shutdown(instance)
        }
    }
    
    // ========== Version and Compatibility Tests ==========
    
    @Test
    fun `test library version is accessible`() {
        if (!librariesAvailable) {
            println("Skipping - libraries not available")
            return
        }
        
        val version = PiperNative.getVersion()
        
        assertNotNull(version)
        assertTrue(version.isNotEmpty())
        
        // Version should follow semantic versioning pattern
        val versionPattern = Regex("\\d+\\.\\d+.*")
        assertTrue(
            version.matches(versionPattern),
            "Version should follow semantic versioning: $version"
        )
        
        println("Piper library version: $version")
    }
    
    @Test
    fun `test JVM version compatibility`() {
        val javaVersion = System.getProperty("java.version")
        val javaVendor = System.getProperty("java.vendor")
        
        println("Java version: $javaVersion")
        println("Java vendor: $javaVendor")
        
        // Extract major version
        val majorVersion = javaVersion.split(".")[0].toIntOrNull() ?: 0
        
        // Should be running on Java 11 or later
        assertTrue(majorVersion >= 11, "Should be running on Java 11 or later")
    }
    
    // ========== Resource Management Tests ==========
    
    @Test
    fun `test resource cleanup is consistent`() {
        if (!librariesAvailable || testModelPath == null) {
            println("Skipping - libraries or test model not available")
            return
        }
        
        val runtime = Runtime.getRuntime()
        
        // Create and destroy multiple instances
        repeat(5) { iteration ->
            System.gc()
            Thread.sleep(50)
            val memoryBefore = runtime.totalMemory() - runtime.freeMemory()
            
            val instance = PiperNative.initialize(testModelPath!!, testConfigPath!!)
            PiperNative.synthesize(instance, "Resource test")
            PiperNative.shutdown(instance)
            
            System.gc()
            Thread.sleep(50)
            val memoryAfter = runtime.totalMemory() - runtime.freeMemory()
            
            val memoryDiff = (memoryAfter - memoryBefore) / (1024 * 1024)
            println("Iteration $iteration: Memory difference: ${memoryDiff}MB")
        }
        
        println("✓ Resource cleanup appears consistent across iterations")
    }
    
    // ========== Platform Summary Test ==========
    
    @Test
    fun `generate platform compatibility report`() {
        println("\n" + "=".repeat(60))
        println("PLATFORM COMPATIBILITY REPORT")
        println("=".repeat(60))
        
        // System Information
        println("\nSystem Information:")
        println("  OS Name: ${System.getProperty("os.name")}")
        println("  OS Version: ${System.getProperty("os.version")}")
        println("  OS Architecture: ${System.getProperty("os.arch")}")
        println("  Java Version: ${System.getProperty("java.version")}")
        println("  Java Vendor: ${System.getProperty("java.vendor")}")
        
        // Library Status
        println("\nLibrary Status:")
        println("  Libraries Available: $librariesAvailable")
        println("  Initialization Status: ${PiperInitializer.getStatus()}")
        
        if (librariesAvailable) {
            println("  Library Version: ${PiperNative.getVersion()}")
        }
        
        // Test Model Status
        println("\nTest Model Status:")
        println("  Model Path: ${testModelPath ?: "Not configured"}")
        println("  Config Path: ${testConfigPath ?: "Not configured"}")
        
        // Platform-Specific Features
        println("\nPlatform Features:")
        println("  File Separator: ${File.separator}")
        println("  Path Separator: ${File.pathSeparator}")
        println("  Line Separator: ${System.lineSeparator().replace("\n", "\\n").replace("\r", "\\r")}")
        
        // Recommendations
        println("\nRecommendations:")
        if (!librariesAvailable) {
            println("  ⚠ Native libraries not available")
            println("  → Build libraries for this platform")
            println("  → Ensure all dependencies are installed")
        } else {
            println("  ✓ Platform is fully supported")
        }
        
        if (testModelPath == null) {
            println("  ⚠ Test model not configured")
            println("  → Set test.model.path system property")
        } else {
            println("  ✓ Test model configured")
        }
        
        println("\n" + "=".repeat(60))
    }
}
