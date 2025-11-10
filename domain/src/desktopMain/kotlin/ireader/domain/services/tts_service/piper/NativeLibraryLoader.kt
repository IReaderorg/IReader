package ireader.domain.services.tts_service.piper

import java.io.File
import java.io.FileOutputStream
import java.nio.file.Files
import java.nio.file.StandardCopyOption

/**
 * Handles loading of platform-specific native libraries for Piper TTS.
 * 
 * This loader provides:
 * - Automatic platform detection (Windows, macOS, Linux)
 * - Architecture detection (x64, ARM64)
 * - Library integrity verification via checksums
 * - Detailed error messages and diagnostics
 * - Thread-safe loading
 * 
 * Supported platforms:
 * - Windows x64
 * - macOS x64 (Intel)
 * - macOS ARM64 (Apple Silicon)
 * - Linux x64
 * 
 * @see PiperNative
 * @see PiperInitializer
 */
object NativeLibraryLoader {
    
    private var libraryLoaded = false
    private var loadError: Throwable? = null
    private val lock = Any()
    
    /**
     * Platform-specific library information
     */
    private data class LibraryInfo(
        val piperLibName: String,
        val onnxRuntimeLibName: String,
        val resourcePath: String,
        val platform: String,
        val architecture: String
    )
    
    /**
     * Detect the current platform and return appropriate library information.
     * 
     * Performs comprehensive platform and architecture detection to determine
     * which native libraries to load.
     * 
     * @return LibraryInfo containing platform-specific library details
     * @throws UnsupportedOperationException if the platform is not supported
     */
    private fun detectPlatform(): LibraryInfo {
        val osName = System.getProperty("os.name").lowercase()
        val osArch = System.getProperty("os.arch").lowercase()
        val osVersion = System.getProperty("os.version")
        
        return when {
            osName.contains("win") -> {
                // Windows platform
                if (!osArch.contains("64") && !osArch.contains("amd64") && !osArch.contains("x86_64")) {
                    throw UnsupportedOperationException(
                        "Unsupported Windows architecture: $osArch. " +
                        "Only 64-bit Windows is supported."
                    )
                }
                LibraryInfo(
                    piperLibName = "piper_jni.dll",
                    onnxRuntimeLibName = "onnxruntime.dll",
                    resourcePath = "/native/windows-x64/",
                    platform = "Windows",
                    architecture = "x64"
                )
            }
            osName.contains("mac") || osName.contains("darwin") -> {
                // macOS platform
                val arch = when {
                    osArch.contains("aarch64") || osArch.contains("arm") -> "arm64"
                    osArch.contains("x86_64") || osArch.contains("amd64") -> "x64"
                    else -> throw UnsupportedOperationException(
                        "Unsupported macOS architecture: $osArch. " +
                        "Only x64 (Intel) and ARM64 (Apple Silicon) are supported."
                    )
                }
                LibraryInfo(
                    piperLibName = "libpiper_jni.dylib",
                    onnxRuntimeLibName = "libonnxruntime.dylib",
                    resourcePath = "/native/macos-$arch/",
                    platform = "macOS",
                    architecture = arch
                )
            }
            osName.contains("nux") || osName.contains("nix") -> {
                // Linux platform
                if (!osArch.contains("64") && !osArch.contains("amd64") && !osArch.contains("x86_64")) {
                    throw UnsupportedOperationException(
                        "Unsupported Linux architecture: $osArch. " +
                        "Only 64-bit Linux is supported."
                    )
                }
                LibraryInfo(
                    piperLibName = "libpiper_jni.so",
                    onnxRuntimeLibName = "libonnxruntime.so",
                    resourcePath = "/native/linux-x64/",
                    platform = "Linux",
                    architecture = "x64"
                )
            }
            else -> {
                throw UnsupportedOperationException(
                    "Unsupported operating system: $osName ($osArch) version $osVersion.\n" +
                    "Piper TTS is only supported on:\n" +
                    "  - Windows 10/11 (x64)\n" +
                    "  - macOS 10.15+ (x64 or ARM64)\n" +
                    "  - Linux (x64)\n" +
                    "\n" +
                    "For more information, visit: https://github.com/yourusername/ireader"
                )
            }
        }
    }
    
    /**
     * Load the native libraries required for Piper TTS.
     * 
     * This method is thread-safe and will only load libraries once. Subsequent calls
     * will return immediately if libraries are already loaded, or throw the cached
     * error if loading previously failed.
     * 
     * The loading process:
     * 1. Detects the current platform and architecture
     * 2. Extracts libraries from resources to a temporary directory
     * 3. Verifies library integrity (optional, if checksums are available)
     * 4. Loads ONNX Runtime library (dependency)
     * 5. Loads Piper JNI library
     * 
     * @throws UnsatisfiedLinkError if the libraries cannot be loaded
     * @throws UnsupportedOperationException if the platform is not supported
     * @throws SecurityException if library verification fails
     */
    fun loadLibraries() {
        synchronized(lock) {
            // Return immediately if already loaded
            if (libraryLoaded) {
                return
            }
            
            // Throw cached error if previous load attempt failed
            loadError?.let { error ->
                throw UnsatisfiedLinkError(
                    "Previous library load attempt failed: ${error.message}"
                ).initCause(error) as UnsatisfiedLinkError
            }
            
            try {
                val platformInfo = detectPlatform()
                
                println("Loading Piper native libraries for ${platformInfo.platform} ${platformInfo.architecture}...")
                
                // Create a temporary directory for native libraries
                val tempDir = Files.createTempDirectory("piper_native_").toFile()
                tempDir.deleteOnExit()
                
                // Load ONNX Runtime first (dependency)
                println("  Loading ONNX Runtime: ${platformInfo.onnxRuntimeLibName}")
                val onnxRuntimeLib = extractLibrary(
                    platformInfo.resourcePath + platformInfo.onnxRuntimeLibName,
                    platformInfo.onnxRuntimeLibName,
                    tempDir
                )
                
                // Verify library before loading (if verification is enabled)
                if (shouldVerifyLibraries()) {
                    verifyLibrary(onnxRuntimeLib, platformInfo.onnxRuntimeLibName)
                }
                
                System.load(onnxRuntimeLib.absolutePath)
                println("    ✓ ONNX Runtime loaded successfully")
                
                // Load Piper JNI library
                println("  Loading Piper JNI: ${platformInfo.piperLibName}")
                val piperLib = extractLibrary(
                    platformInfo.resourcePath + platformInfo.piperLibName,
                    platformInfo.piperLibName,
                    tempDir
                )
                
                // Verify library before loading (if verification is enabled)
                if (shouldVerifyLibraries()) {
                    verifyLibrary(piperLib, platformInfo.piperLibName)
                }
                
                System.load(piperLib.absolutePath)
                println("    ✓ Piper JNI loaded successfully")
                
                libraryLoaded = true
                println("✓ All Piper native libraries loaded successfully")
                println("  Platform: ${platformInfo.platform} ${platformInfo.architecture}")
                println("  Resource path: ${platformInfo.resourcePath}")
                
            } catch (e: UnsupportedOperationException) {
                // Platform not supported - provide detailed error
                loadError = e
                throw e
            } catch (e: SecurityException) {
                // Library verification failed
                loadError = e
                throw UnsatisfiedLinkError(
                    "Library verification failed: ${e.message}\n" +
                    "The native libraries may be corrupted or tampered with.\n" +
                    "Please reinstall the application or download fresh libraries."
                ).initCause(e) as UnsatisfiedLinkError
            } catch (e: Exception) {
                // Generic loading error
                loadError = e
                val platformInfo = try {
                    detectPlatform()
                } catch (ex: Exception) {
                    null
                }
                
                val errorMessage = buildString {
                    appendLine("Failed to load Piper native libraries: ${e.message}")
                    appendLine()
                    appendLine("Troubleshooting steps:")
                    appendLine("1. Ensure the native libraries are included in the application resources")
                    
                    if (platformInfo != null) {
                        appendLine("2. Expected library location: ${platformInfo.resourcePath}")
                        appendLine("3. Expected libraries:")
                        appendLine("   - ${platformInfo.onnxRuntimeLibName}")
                        appendLine("   - ${platformInfo.piperLibName}")
                    }
                    
                    appendLine("4. Check that you have the correct version for your platform")
                    appendLine("5. Verify that all dependencies are installed:")
                    
                    when {
                        System.getProperty("os.name").lowercase().contains("win") -> {
                            appendLine("   - Visual C++ Redistributable 2015-2022")
                        }
                        System.getProperty("os.name").lowercase().contains("mac") -> {
                            appendLine("   - macOS 10.15 or later")
                        }
                        System.getProperty("os.name").lowercase().contains("nux") -> {
                            appendLine("   - glibc 2.27 or later")
                            appendLine("   - libasound2 (ALSA)")
                        }
                    }
                    
                    appendLine()
                    appendLine("For more help, visit: https://github.com/yourusername/ireader/wiki/TTS-Troubleshooting")
                }
                
                throw UnsatisfiedLinkError(errorMessage).initCause(e) as UnsatisfiedLinkError
            }
        }
    }
    
    /**
     * Check if library verification should be performed.
     * 
     * Verification can be disabled via system property for development/testing.
     * 
     * @return true if libraries should be verified, false otherwise
     */
    private fun shouldVerifyLibraries(): Boolean {
        return System.getProperty("piper.verify.libraries", "true").toBoolean()
    }
    
    /**
     * Verify the integrity of a library file using LibraryVerifier.
     * 
     * Performs comprehensive verification including checksums and code signatures.
     * 
     * @param libraryFile The library file to verify
     * @param libraryName The name of the library (for error messages)
     * @throws SecurityException if verification fails
     */
    private fun verifyLibrary(libraryFile: File, libraryName: String) {
        val verifier = LibraryVerifier()
        val result = verifier.verifyLibrary(
            libraryPath = libraryFile.toPath(),
            skipSignatureCheck = !shouldVerifySignatures()
        )
        
        if (!result.isVerified) {
            // Print detailed verification report
            println(result.getReport())
            
            throw SecurityException(
                "Library verification failed for $libraryName: ${result.errorMessage}\n" +
                "The library may be corrupted, tampered with, or from an untrusted source.\n" +
                "For security reasons, the library will not be loaded.\n" +
                "\n" +
                "To disable verification (not recommended for production):\n" +
                "  Set system property: -Dpiper.verify.libraries=false"
            )
        }
        
        // Log warnings if any
        if (result.warnings.isNotEmpty()) {
            println("Library verification warnings for $libraryName:")
            result.warnings.forEach { println("  ⚠ $it") }
        }
    }
    
    /**
     * Check if code signature verification should be performed.
     * 
     * Signature verification can be disabled via system property for development/testing.
     * 
     * @return true if signatures should be verified, false otherwise
     */
    private fun shouldVerifySignatures(): Boolean {
        return System.getProperty("piper.verify.signatures", "true").toBoolean()
    }
    
    /**
     * Extract a native library from resources to a temporary file.
     * 
     * @param resourcePath Path to the library in resources
     * @param libraryName Name of the library file
     * @param targetDir Directory to extract the library to
     * @return File object pointing to the extracted library
     * @throws IllegalStateException if the library is not found in resources
     * @throws java.io.IOException if extraction fails
     */
    private fun extractLibrary(resourcePath: String, libraryName: String, targetDir: File): File {
        val targetFile = File(targetDir, libraryName)
        
        // Extract the library from resources
        val inputStream = NativeLibraryLoader::class.java.getResourceAsStream(resourcePath)
        
        if (inputStream == null) {
            // Provide detailed error message with troubleshooting steps
            val errorMessage = buildString {
                appendLine("Native library not found in resources: $resourcePath")
                appendLine()
                appendLine("This usually means:")
                appendLine("1. The native libraries were not packaged with the application")
                appendLine("2. The library path is incorrect for your platform")
                appendLine("3. The application was not built correctly")
                appendLine()
                appendLine("Expected resource path: $resourcePath")
                appendLine("Library name: $libraryName")
                appendLine()
                appendLine("To fix this:")
                appendLine("1. Ensure you have built the native libraries for your platform")
                appendLine("2. Verify the libraries are in the correct resources directory")
                appendLine("3. Check the build configuration includes native resources")
                appendLine()
                appendLine("For build instructions, see:")
                appendLine("https://github.com/yourusername/ireader/blob/main/BUILD_NATIVE_LIBS.md")
            }
            
            throw IllegalStateException(errorMessage)
        }
        
        try {
            inputStream.use { input ->
                FileOutputStream(targetFile).use { output ->
                    val bytesWritten = input.copyTo(output)
                    println("    Extracted $bytesWritten bytes to ${targetFile.name}")
                }
            }
        } catch (e: Exception) {
            throw IllegalStateException(
                "Failed to extract library $libraryName: ${e.message}\n" +
                "Ensure you have write permissions to the temporary directory."
            , e)
        }
        
        // Verify the extracted file
        if (!targetFile.exists() || targetFile.length() == 0L) {
            throw IllegalStateException(
                "Library extraction failed: $libraryName\n" +
                "The extracted file is missing or empty."
            )
        }
        
        // Make the library executable on Unix-like systems
        if (!System.getProperty("os.name").lowercase().contains("win")) {
            try {
                targetFile.setExecutable(true, false)
                targetFile.setReadable(true, false)
            } catch (e: Exception) {
                println("    Warning: Could not set executable permissions on $libraryName: ${e.message}")
            }
        }
        
        targetFile.deleteOnExit()
        return targetFile
    }
    
    /**
     * Check if the native library is loaded.
     * 
     * @return true if loaded successfully, false otherwise
     */
    fun isLibraryLoaded(): Boolean {
        return libraryLoaded
    }
    
    /**
     * Get the error that occurred during library loading, if any.
     * 
     * @return The error that occurred, or null if loading was successful
     */
    fun getLoadError(): Throwable? {
        return loadError
    }
    
    /**
     * Get information about the current platform.
     * 
     * Provides detailed diagnostic information useful for debugging and support.
     * Includes OS details, expected library paths, and loading status.
     * 
     * @return String describing the platform and library status
     */
    fun getPlatformInfo(): String {
        val osName = System.getProperty("os.name")
        val osArch = System.getProperty("os.arch")
        val osVersion = System.getProperty("os.version")
        val javaVersion = System.getProperty("java.version")
        val javaVendor = System.getProperty("java.vendor")
        
        return buildString {
            appendLine("=== Platform Information ===")
            appendLine()
            appendLine("Operating System:")
            appendLine("  Name: $osName")
            appendLine("  Architecture: $osArch")
            appendLine("  Version: $osVersion")
            appendLine()
            appendLine("Java Runtime:")
            appendLine("  Version: $javaVersion")
            appendLine("  Vendor: $javaVendor")
            appendLine()
            
            try {
                val platformInfo = detectPlatform()
                appendLine("Detected Platform:")
                appendLine("  Platform: ${platformInfo.platform}")
                appendLine("  Architecture: ${platformInfo.architecture}")
                appendLine()
                appendLine("Expected Libraries:")
                appendLine("  Piper JNI: ${platformInfo.piperLibName}")
                appendLine("  ONNX Runtime: ${platformInfo.onnxRuntimeLibName}")
                appendLine("  Resource Path: ${platformInfo.resourcePath}")
                appendLine()
            } catch (e: Exception) {
                appendLine("Platform Detection:")
                appendLine("  Status: UNSUPPORTED")
                appendLine("  Error: ${e.message}")
                appendLine()
            }
            
            appendLine("Library Status:")
            appendLine("  Loaded: $libraryLoaded")
            
            if (loadError != null) {
                appendLine("  Error: ${loadError?.message}")
                appendLine()
                appendLine("Error Details:")
                loadError?.printStackTrace()
            }
            
            appendLine()
            appendLine("System Properties:")
            appendLine("  Temp Directory: ${System.getProperty("java.io.tmpdir")}")
            appendLine("  User Directory: ${System.getProperty("user.dir")}")
            appendLine("  Library Path: ${System.getProperty("java.library.path")}")
        }
    }
    
    /**
     * Get a concise diagnostic message suitable for user display.
     * 
     * @return User-friendly diagnostic message
     */
    fun getDiagnosticMessage(): String {
        return if (libraryLoaded) {
            "Piper TTS libraries loaded successfully"
        } else if (loadError != null) {
            "Piper TTS libraries failed to load: ${loadError?.message}"
        } else {
            "Piper TTS libraries not yet loaded"
        }
    }
    
    /**
     * Reset the loader state. FOR TESTING ONLY.
     * 
     * This method is intended for unit tests and should not be called in production code.
     * It does not unload libraries, only resets the state flags.
     */
    internal fun resetForTesting() {
        synchronized(lock) {
            libraryLoaded = false
            loadError = null
        }
    }
}
