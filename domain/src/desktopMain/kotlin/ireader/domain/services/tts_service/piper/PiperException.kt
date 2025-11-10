package ireader.domain.services.tts_service.piper

/**
 * Base exception class for all Piper TTS related errors.
 * 
 * This sealed class hierarchy provides specific exception types for different
 * error scenarios, making error handling more precise and informative.
 * 
 * All exceptions include:
 * - Descriptive error messages
 * - Optional cause (underlying exception)
 * - Diagnostic information where applicable
 * 
 * @param message Descriptive error message
 * @param cause Optional underlying cause of the error
 */
sealed class PiperException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause) {
    
    /**
     * Exception thrown when Piper initialization fails.
     * 
     * This can occur due to:
     * - Platform not supported
     * - Native libraries not found
     * - Library loading failures
     * - Dependency issues
     * 
     * Example:
     * ```kotlin
     * try {
     *     PiperInitializer.initialize()
     * } catch (e: PiperException.InitializationException) {
     *     println("Failed to initialize: ${e.message}")
     *     // Fall back to simulation mode
     * }
     * ```
     */
    class InitializationException(
        message: String,
        cause: Throwable? = null
    ) : PiperException("Initialization failed: $message", cause) {
        
        /**
         * Get user-friendly error message suitable for display.
         */
        override fun getUserMessage(): String {
            return when {
                message?.contains("Platform not supported") == true ->
                    "Your operating system is not supported for offline TTS. Using simulation mode."
                message?.contains("not found") == true ->
                    "TTS libraries are missing. Please reinstall the application."
                message?.contains("load") == true ->
                    "Failed to load TTS libraries. Some system dependencies may be missing."
                else ->
                    "TTS initialization failed. Using simulation mode."
            }
        }
    }
    
    /**
     * Exception thrown when a voice model fails to load.
     * 
     * This can occur due to:
     * - Model file not found
     * - Corrupted model file
     * - Invalid model format
     * - Incompatible model version
     * - Configuration file missing or invalid
     * 
     * Example:
     * ```kotlin
     * try {
     *     val instance = PiperNative.initialize(modelPath, configPath)
     * } catch (e: PiperException.ModelLoadException) {
     *     println("Failed to load model: ${e.modelPath}")
     *     println("Reason: ${e.message}")
     * }
     * ```
     */
    class ModelLoadException(
        val modelPath: String,
        message: String? = null,
        cause: Throwable? = null
    ) : PiperException(
        "Failed to load voice model '$modelPath'" + 
        (message?.let { ": $it" } ?: ""),
        cause
    ) {
        
        /**
         * Get user-friendly error message suitable for display.
         */
        override fun getUserMessage(): String {
            return when {
                message?.contains("not found") == true ->
                    "Voice model not found. Please download the model again."
                message?.contains("corrupt") == true ->
                    "Voice model is corrupted. Please re-download the model."
                message?.contains("format") == true ->
                    "Voice model format is invalid or incompatible."
                else ->
                    "Failed to load voice model. The model may be corrupted or incompatible."
            }
        }
    }
    
    /**
     * Exception thrown when text synthesis fails.
     * 
     * This can occur due to:
     * - Invalid instance handle
     * - Text too long
     * - Memory allocation failure
     * - Synthesis engine error
     * - Invalid characters in text
     * 
     * Example:
     * ```kotlin
     * try {
     *     val audio = PiperNative.synthesize(instance, text)
     * } catch (e: PiperException.SynthesisException) {
     *     println("Synthesis failed for text: ${e.textPreview}")
     *     println("Reason: ${e.message}")
     * }
     * ```
     */
    class SynthesisException(
        val text: String,
        message: String? = null,
        cause: Throwable? = null
    ) : PiperException(
        "Synthesis failed" + (message?.let { ": $it" } ?: ""),
        cause
    ) {
        
        /**
         * Get a preview of the text that failed (first 50 characters).
         */
        val textPreview: String
            get() = if (text.length > 50) {
                text.take(50) + "..."
            } else {
                text
            }
        
        /**
         * Get user-friendly error message suitable for display.
         */
        override fun getUserMessage(): String {
            return when {
                message?.contains("too long") == true ->
                    "Text is too long for synthesis. Try breaking it into smaller chunks."
                message?.contains("memory") == true ->
                    "Not enough memory for synthesis. Try closing other applications."
                message?.contains("invalid") == true ->
                    "Text contains invalid characters or formatting."
                else ->
                    "Failed to synthesize speech. Please try again."
            }
        }
    }
    
    /**
     * Exception thrown when an invalid parameter is provided.
     * 
     * This can occur due to:
     * - Parameter value out of valid range
     * - Invalid instance handle
     * - Null or empty required parameter
     * 
     * Example:
     * ```kotlin
     * try {
     *     PiperNative.setSpeechRate(instance, 5.0f) // Out of range
     * } catch (e: PiperException.InvalidParameterException) {
     *     println("Invalid ${e.parameterName}: ${e.parameterValue}")
     *     println("Valid range: ${e.validRange}")
     * }
     * ```
     */
    class InvalidParameterException(
        val parameterName: String,
        val parameterValue: Any?,
        val validRange: String? = null,
        message: String? = null
    ) : PiperException(
        "Invalid parameter '$parameterName' = $parameterValue" +
        (validRange?.let { " (valid range: $it)" } ?: "") +
        (message?.let { ": $it" } ?: "")
    ) {
        
        /**
         * Get user-friendly error message suitable for display.
         */
        override fun getUserMessage(): String {
            return "Invalid $parameterName: $parameterValue" +
                (validRange?.let { ". Valid range: $it" } ?: "")
        }
    }
    
    /**
     * Exception thrown when a resource error occurs.
     * 
     * This can occur due to:
     * - Out of memory
     * - File system errors
     * - Resource exhaustion
     * - Disk space issues
     * 
     * Example:
     * ```kotlin
     * try {
     *     val audio = PiperNative.synthesize(instance, veryLongText)
     * } catch (e: PiperException.ResourceException) {
     *     println("Resource error: ${e.resourceType}")
     *     println("Reason: ${e.message}")
     * }
     * ```
     */
    class ResourceException(
        val resourceType: String,
        message: String,
        cause: Throwable? = null
    ) : PiperException("$resourceType error: $message", cause) {
        
        /**
         * Get user-friendly error message suitable for display.
         */
        override fun getUserMessage(): String {
            return when (resourceType.lowercase()) {
                "memory" -> "Not enough memory available. Try closing other applications."
                "disk" -> "Not enough disk space available."
                "file" -> "File system error occurred."
                else -> "System resource error: $message"
            }
        }
    }
    
    /**
     * Exception thrown when an invalid instance handle is used.
     * 
     * This can occur due to:
     * - Using an instance after shutdown
     * - Using an invalid/zero instance handle
     * - Instance was never created successfully
     * 
     * Example:
     * ```kotlin
     * try {
     *     PiperNative.synthesize(0, text) // Invalid instance
     * } catch (e: PiperException.InvalidInstanceException) {
     *     println("Invalid instance: ${e.instanceId}")
     * }
     * ```
     */
    class InvalidInstanceException(
        val instanceId: Long,
        message: String? = null
    ) : PiperException(
        "Invalid instance handle: $instanceId" +
        (message?.let { " ($it)" } ?: "")
    ) {
        
        /**
         * Get user-friendly error message suitable for display.
         */
        override fun getUserMessage(): String {
            return "Voice instance is no longer valid. Please reload the voice model."
        }
    }
    
    /**
     * Exception thrown when a configuration error occurs.
     * 
     * This can occur due to:
     * - Invalid configuration file
     * - Missing required configuration
     * - Incompatible configuration version
     * 
     * Example:
     * ```kotlin
     * try {
     *     val instance = PiperNative.initialize(modelPath, configPath)
     * } catch (e: PiperException.ConfigurationException) {
     *     println("Configuration error: ${e.configPath}")
     * }
     * ```
     */
    class ConfigurationException(
        val configPath: String,
        message: String,
        cause: Throwable? = null
    ) : PiperException("Configuration error in '$configPath': $message", cause) {
        
        /**
         * Get user-friendly error message suitable for display.
         */
        override fun getUserMessage(): String {
            return "Voice configuration is invalid. Please re-download the voice model."
        }
    }
    
    /**
     * Get a user-friendly error message suitable for display in UI.
     * 
     * This method provides a generic fallback for exception types that don't
     * override getUserMessage().
     */
    open fun getUserMessage(): String {
        return message ?: "An error occurred with Piper TTS"
    }
    
    /**
     * Get diagnostic information for debugging and support.
     * 
     * @return Detailed diagnostic information
     */
    fun getDiagnosticInfo(): String {
        return buildString {
            appendLine("Exception Type: ${this@PiperException::class.simpleName}")
            appendLine("Message: ${message}")
            
            when (this@PiperException) {
                is ModelLoadException -> {
                    appendLine("Model Path: $modelPath")
                }
                is SynthesisException -> {
                    appendLine("Text Preview: $textPreview")
                    appendLine("Text Length: ${text.length}")
                }
                is InvalidParameterException -> {
                    appendLine("Parameter: $parameterName")
                    appendLine("Value: $parameterValue")
                    validRange?.let { appendLine("Valid Range: $it") }
                }
                is ResourceException -> {
                    appendLine("Resource Type: $resourceType")
                }
                is InvalidInstanceException -> {
                    appendLine("Instance ID: $instanceId")
                }
                is ConfigurationException -> {
                    appendLine("Config Path: $configPath")
                }
                else -> {
                    // Generic exception info
                }
            }
            
            cause?.let {
                appendLine("Caused by: ${it::class.simpleName}: ${it.message}")
            }
        }
    }
}
