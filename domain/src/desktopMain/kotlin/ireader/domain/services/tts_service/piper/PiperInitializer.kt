package ireader.domain.services.tts_service.piper

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Initializer for Piper TTS native libraries.
 * 
 * This class provides a high-level API for initializing Piper TTS with comprehensive
 * error handling, status reporting, and graceful degradation support.
 * 
 * Features:
 * - Thread-safe initialization
 * - Comprehensive error handling and reporting
 * - Detailed diagnostic information
 * - Graceful degradation to simulation mode
 * - Status tracking and querying
 * 
 * Usage:
 * ```kotlin
 * val result = PiperInitializer.initialize()
 * if (result.isSuccess) {
 *     // Piper TTS is ready to use
 * } else {
 *     // Fall back to simulation mode
 *     println(PiperInitializer.getStatusInfo())
 * }
 * ```
 * 
 * @see PiperNative
 * @see NativeLibraryLoader
 */
object PiperInitializer {
    
    private var initializationAttempted = false
    private var initializationSuccessful = false
    private var initializationError: Throwable? = null
    private var initializationTimeMs: Long = 0
    private val lock = Any()
    
    /**
     * Initialization status enum for detailed status reporting.
     */
    enum class InitializationStatus {
        /** Initialization has not been attempted yet */
        NOT_STARTED,
        
        /** Initialization is currently in progress */
        IN_PROGRESS,
        
        /** Initialization completed successfully */
        SUCCESS,
        
        /** Initialization failed - platform not supported */
        UNSUPPORTED_PLATFORM,
        
        /** Initialization failed - libraries not found */
        LIBRARIES_NOT_FOUND,
        
        /** Initialization failed - library loading error */
        LOAD_ERROR,
        
        /** Initialization failed - verification error */
        VERIFICATION_ERROR,
        
        /** Initialization failed - unknown error */
        UNKNOWN_ERROR
    }
    
    /**
     * Initialize Piper TTS by loading native libraries.
     * 
     * This method is safe to call multiple times - it will only attempt initialization once.
     * Subsequent calls will return the cached result.
     * 
     * The initialization process:
     * 1. Detects the current platform
     * 2. Loads native libraries (ONNX Runtime and Piper JNI)
     * 3. Verifies library functionality
     * 4. Performs basic health checks
     * 
     * @return Result indicating success or failure with error details
     */
    suspend fun initialize(): Result<Unit> = withContext(Dispatchers.IO) {
        synchronized(lock) {
            // Return cached result if already attempted
            if (initializationAttempted) {
                return@withContext if (initializationSuccessful) {
                    Result.success(Unit)
                } else {
                    Result.failure(
                        initializationError ?: IllegalStateException("Initialization failed for unknown reason")
                    )
                }
            }
            
            initializationAttempted = true
        }
        
        val startTime = System.currentTimeMillis()
        
        try {
            println("=== Initializing Piper TTS ===")
            println()
            
            // Step 1: Load the native libraries
            println("Step 1: Loading native libraries...")
            NativeLibraryLoader.loadLibraries()
            println()
            
            // Step 2: Verify the library is functional
            println("Step 2: Verifying library functionality...")
            if (!PiperNative.isLoaded()) {
                throw IllegalStateException(
                    "Native library loaded but not functional. " +
                    "This may indicate a version mismatch or missing dependencies."
                )
            }
            println("  ✓ Library is functional")
            println()
            
            // Step 3: Perform basic health checks
            println("Step 3: Performing health checks...")
            performHealthChecks()
            println("  ✓ All health checks passed")
            println()
            
            initializationTimeMs = System.currentTimeMillis() - startTime
            initializationSuccessful = true
            
            println("✓ Piper TTS initialized successfully in ${initializationTimeMs}ms")
            println()
            
            Result.success(Unit)
            
        } catch (e: UnsupportedOperationException) {
            // Platform not supported
            initializationError = e
            initializationTimeMs = System.currentTimeMillis() - startTime
            
            println("✗ Piper TTS initialization failed: Platform not supported")
            println("  ${e.message}")
            println()
            println("Piper TTS will not be available. The application will use simulation mode.")
            println()
            
            Result.failure(PiperException.InitializationException(
                "Platform not supported: ${e.message}",
                e
            ))
            
        } catch (e: UnsatisfiedLinkError) {
            // Library loading error
            initializationError = e
            initializationTimeMs = System.currentTimeMillis() - startTime
            
            println("✗ Piper TTS initialization failed: Could not load native libraries")
            println("  ${e.message}")
            println()
            println("Piper TTS will not be available. The application will use simulation mode.")
            println()
            println("For troubleshooting, see:")
            println("  ${NativeLibraryLoader.getPlatformInfo()}")
            println()
            
            Result.failure(PiperException.InitializationException(
                "Failed to load native libraries: ${e.message}",
                e
            ))
            
        } catch (e: SecurityException) {
            // Verification error
            initializationError = e
            initializationTimeMs = System.currentTimeMillis() - startTime
            
            println("✗ Piper TTS initialization failed: Library verification failed")
            println("  ${e.message}")
            println()
            
            Result.failure(PiperException.InitializationException(
                "Library verification failed: ${e.message}",
                e
            ))
            
        } catch (e: Exception) {
            // Unknown error
            initializationError = e
            initializationTimeMs = System.currentTimeMillis() - startTime
            
            println("✗ Piper TTS initialization failed: ${e.message}")
            println()
            println("Piper TTS will not be available. The application will use simulation mode.")
            println()
            
            Result.failure(PiperException.InitializationException(
                "Initialization failed: ${e.message}",
                e
            ))
        }
    }
    
    /**
     * Perform basic health checks to verify Piper functionality.
     * 
     * @throws Exception if any health check fails
     */
    private fun performHealthChecks() {
        try {
            // Check 1: Verify we can get the version
            val version = PiperNative.getVersion()
            println("  ✓ Version check passed: $version")
            
            // Check 2: Verify we can create and destroy an instance (if test model available)
            // This is optional and only runs if a test model is available
            // performInstanceHealthCheck()
            
        } catch (e: Exception) {
            throw IllegalStateException("Health check failed: ${e.message}", e)
        }
    }
    
    /**
     * Perform an instance creation health check (optional).
     * Only runs if a test model is available.
     */
    private fun performInstanceHealthCheck() {
        // This would require a small test model to be bundled
        // For now, we skip this check
        // TODO: Add instance creation test with bundled test model
    }
    
    /**
     * Check if Piper TTS is available and ready to use.
     * 
     * @return true if initialized successfully and functional, false otherwise
     */
    fun isAvailable(): Boolean {
        return initializationSuccessful && PiperNative.isLoaded()
    }
    
    /**
     * Get the current initialization status.
     * 
     * @return Current initialization status
     */
    fun getStatus(): InitializationStatus {
        return when {
            !initializationAttempted -> InitializationStatus.NOT_STARTED
            initializationSuccessful -> InitializationStatus.SUCCESS
            initializationError is UnsupportedOperationException -> InitializationStatus.UNSUPPORTED_PLATFORM
            initializationError is IllegalStateException && 
                initializationError?.message?.contains("not found") == true -> InitializationStatus.LIBRARIES_NOT_FOUND
            initializationError is UnsatisfiedLinkError -> InitializationStatus.LOAD_ERROR
            initializationError is SecurityException -> InitializationStatus.VERIFICATION_ERROR
            initializationError != null -> InitializationStatus.UNKNOWN_ERROR
            else -> InitializationStatus.NOT_STARTED
        }
    }
    
    /**
     * Get the initialization error if initialization failed.
     * 
     * @return The error that occurred during initialization, or null if successful
     */
    fun getInitializationError(): Throwable? {
        return initializationError
    }
    
    /**
     * Get the time taken for initialization in milliseconds.
     * 
     * @return Initialization time in milliseconds, or 0 if not yet attempted
     */
    fun getInitializationTimeMs(): Long {
        return initializationTimeMs
    }
    
    /**
     * Get detailed platform and initialization status information.
     * 
     * Provides comprehensive diagnostic information useful for debugging and support.
     * 
     * @return String containing platform and status information
     */
    fun getStatusInfo(): String {
        return buildString {
            appendLine("=== Piper TTS Status ===")
            appendLine()
            appendLine("Initialization:")
            appendLine("  Attempted: $initializationAttempted")
            appendLine("  Successful: $initializationSuccessful")
            appendLine("  Status: ${getStatus()}")
            appendLine("  Time: ${initializationTimeMs}ms")
            appendLine("  Available: ${isAvailable()}")
            appendLine()
            
            if (initializationError != null) {
                appendLine("Error Information:")
                appendLine("  Type: ${initializationError!!::class.simpleName}")
                appendLine("  Message: ${initializationError?.message}")
                appendLine()
            }
            
            appendLine(NativeLibraryLoader.getPlatformInfo())
            
            if (!initializationSuccessful && initializationAttempted) {
                appendLine()
                appendLine("=== Troubleshooting ===")
                appendLine()
                appendLine("Piper TTS is not available. The application is running in simulation mode.")
                appendLine()
                appendLine("To enable Piper TTS:")
                appendLine("1. Ensure your platform is supported (Windows x64, macOS x64/ARM64, Linux x64)")
                appendLine("2. Verify native libraries are included in the application")
                appendLine("3. Check that all system dependencies are installed")
                appendLine("4. Review the error message above for specific issues")
                appendLine()
                appendLine("For more help, visit:")
                appendLine("https://github.com/yourusername/ireader/wiki/TTS-Troubleshooting")
            }
        }
    }
    
    /**
     * Get a concise user-friendly status message.
     * 
     * @return User-friendly status message
     */
    fun getStatusMessage(): String {
        return when (getStatus()) {
            InitializationStatus.NOT_STARTED -> "Piper TTS not initialized"
            InitializationStatus.IN_PROGRESS -> "Initializing Piper TTS..."
            InitializationStatus.SUCCESS -> "Piper TTS ready (initialized in ${initializationTimeMs}ms)"
            InitializationStatus.UNSUPPORTED_PLATFORM -> "Piper TTS not available: Platform not supported"
            InitializationStatus.LIBRARIES_NOT_FOUND -> "Piper TTS not available: Libraries not found"
            InitializationStatus.LOAD_ERROR -> "Piper TTS not available: Failed to load libraries"
            InitializationStatus.VERIFICATION_ERROR -> "Piper TTS not available: Library verification failed"
            InitializationStatus.UNKNOWN_ERROR -> "Piper TTS not available: ${initializationError?.message}"
        }
    }
    
    /**
     * Check if the application should fall back to simulation mode.
     * 
     * @return true if simulation mode should be used, false if Piper is available
     */
    fun shouldUseSimulationMode(): Boolean {
        return !isAvailable()
    }
    
    /**
     * Get recommendations for the user based on initialization status.
     * 
     * @return List of user-friendly recommendations
     */
    fun getRecommendations(): List<String> {
        return when (getStatus()) {
            InitializationStatus.SUCCESS -> listOf(
                "Piper TTS is working correctly",
                "You can download voice models from the settings"
            )
            InitializationStatus.UNSUPPORTED_PLATFORM -> listOf(
                "Your platform is not currently supported",
                "Supported platforms: Windows x64, macOS x64/ARM64, Linux x64",
                "The application will use simulation mode for TTS"
            )
            InitializationStatus.LIBRARIES_NOT_FOUND -> listOf(
                "Native libraries are missing from the application",
                "Try reinstalling the application",
                "If building from source, ensure native libraries are built and packaged"
            )
            InitializationStatus.LOAD_ERROR -> listOf(
                "Failed to load native libraries",
                "Ensure all system dependencies are installed",
                "On Windows: Install Visual C++ Redistributable 2015-2022",
                "On Linux: Install libasound2 and ensure glibc 2.27+",
                "On macOS: Ensure macOS 10.15 or later"
            )
            InitializationStatus.VERIFICATION_ERROR -> listOf(
                "Library verification failed",
                "The libraries may be corrupted",
                "Try reinstalling the application"
            )
            else -> listOf(
                "Piper TTS initialization failed",
                "Check the error message for details",
                "The application will use simulation mode"
            )
        }
    }
    
    /**
     * Reset initialization state. FOR TESTING ONLY.
     * 
     * This method is intended for unit tests and should not be called in production code.
     * It does not unload libraries, only resets the state flags.
     */
    internal fun resetForTesting() {
        synchronized(lock) {
            initializationAttempted = false
            initializationSuccessful = false
            initializationError = null
            initializationTimeMs = 0
            NativeLibraryLoader.resetForTesting()
        }
    }
}
