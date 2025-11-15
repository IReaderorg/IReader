package ireader.domain.services.tts

/**
 * Error handler for TTS operations with fallback support
 * Requirements: 5.4
 */
class TTSErrorHandler {
    
    /**
     * Handle TTS error and determine fallback strategy
     * Requirements: 5.4
     */
    fun handleError(error: TTSError): TTSErrorResult {
        return when (error) {
            is TTSError.PluginNotFound -> {
                TTSErrorResult.Fallback(
                    message = "TTS plugin not found. Using built-in TTS.",
                    shouldFallback = true
                )
            }
            is TTSError.PluginInitializationFailed -> {
                TTSErrorResult.Fallback(
                    message = "Failed to initialize TTS plugin: ${error.reason}. Using built-in TTS.",
                    shouldFallback = true
                )
            }
            is TTSError.VoiceNotFound -> {
                TTSErrorResult.Retry(
                    message = "Voice not found: ${error.voiceId}. Please select a different voice.",
                    shouldRetry = false
                )
            }
            is TTSError.AudioStreamError -> {
                TTSErrorResult.Fallback(
                    message = "Audio stream error: ${error.reason}. Trying built-in TTS.",
                    shouldFallback = true
                )
            }
            is TTSError.NetworkError -> {
                TTSErrorResult.Retry(
                    message = "Network error. Please check your connection and try again.",
                    shouldRetry = true
                )
            }
            is TTSError.PermissionDenied -> {
                TTSErrorResult.Fatal(
                    message = "Permission denied: ${error.permission}. Please grant the required permission."
                )
            }
            is TTSError.UnsupportedFormat -> {
                TTSErrorResult.Fallback(
                    message = "Unsupported audio format: ${error.format}. Using built-in TTS.",
                    shouldFallback = true
                )
            }
            is TTSError.PluginDisabled -> {
                TTSErrorResult.Fallback(
                    message = "TTS plugin is disabled. Using built-in TTS.",
                    shouldFallback = true
                )
            }
            is TTSError.ResourceExhausted -> {
                TTSErrorResult.Retry(
                    message = "Plugin resources exhausted. Please try again later.",
                    shouldRetry = true
                )
            }
            is TTSError.Unknown -> {
                TTSErrorResult.Fallback(
                    message = "Unknown error: ${error.message}. Trying built-in TTS.",
                    shouldFallback = true
                )
            }
        }
    }
    
    /**
     * Convert exception to TTS error
     */
    fun exceptionToError(exception: Exception, context: String = ""): TTSError {
        return when {
            exception.message?.contains("not found", ignoreCase = true) == true -> {
                TTSError.PluginNotFound(context)
            }
            exception.message?.contains("network", ignoreCase = true) == true -> {
                TTSError.NetworkError(exception.message ?: "Network error")
            }
            exception.message?.contains("permission", ignoreCase = true) == true -> {
                TTSError.PermissionDenied(exception.message ?: "Permission denied")
            }
            exception.message?.contains("stream", ignoreCase = true) == true -> {
                TTSError.AudioStreamError(exception.message ?: "Stream error")
            }
            else -> {
                TTSError.Unknown(exception.message ?: "Unknown error")
            }
        }
    }
    
    /**
     * Get user-friendly error message
     */
    fun getUserMessage(error: TTSError): String {
        return when (error) {
            is TTSError.PluginNotFound -> 
                "The selected TTS plugin could not be found. Please check if it's installed."
            is TTSError.PluginInitializationFailed -> 
                "Failed to start TTS plugin. ${error.reason}"
            is TTSError.VoiceNotFound -> 
                "The selected voice is not available. Please choose another voice."
            is TTSError.AudioStreamError -> 
                "Audio playback error. Please try again."
            is TTSError.NetworkError -> 
                "Network connection error. Please check your internet connection."
            is TTSError.PermissionDenied -> 
                "Permission required: ${error.permission}. Please grant access in settings."
            is TTSError.UnsupportedFormat -> 
                "This audio format is not supported on your device."
            is TTSError.PluginDisabled -> 
                "The TTS plugin is currently disabled. Enable it in plugin settings."
            is TTSError.ResourceExhausted -> 
                "The TTS plugin is currently overloaded. Please try again in a moment."
            is TTSError.Unknown -> 
                "An unexpected error occurred: ${error.message}"
        }
    }
}

/**
 * TTS error types
 * Requirements: 5.4
 */
sealed class TTSError {
    data class PluginNotFound(val pluginId: String) : TTSError()
    data class PluginInitializationFailed(val reason: String) : TTSError()
    data class VoiceNotFound(val voiceId: String) : TTSError()
    data class AudioStreamError(val reason: String) : TTSError()
    data class NetworkError(val reason: String) : TTSError()
    data class PermissionDenied(val permission: String) : TTSError()
    data class UnsupportedFormat(val format: String) : TTSError()
    data class PluginDisabled(val pluginId: String) : TTSError()
    data class ResourceExhausted(val pluginId: String) : TTSError()
    data class Unknown(val message: String) : TTSError()
}

/**
 * TTS error result with fallback strategy
 * Requirements: 5.4
 */
sealed class TTSErrorResult {
    data class Fallback(
        val message: String,
        val shouldFallback: Boolean
    ) : TTSErrorResult()
    
    data class Retry(
        val message: String,
        val shouldRetry: Boolean
    ) : TTSErrorResult()
    
    data class Fatal(
        val message: String
    ) : TTSErrorResult()
}
