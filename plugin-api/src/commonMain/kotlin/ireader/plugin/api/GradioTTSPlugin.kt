package ireader.plugin.api

import kotlinx.serialization.Serializable

/**
 * Plugin interface for Gradio-based TTS engines.
 * Supports Coqui TTS, XTTS, and other Gradio endpoints.
 * 
 * Example:
 * ```kotlin
 * class CoquiTTSPlugin : GradioTTSPlugin {
 *     override val manifest = PluginManifest(
 *         id = "com.example.coqui-tts",
 *         name = "Coqui TTS",
 *         type = PluginType.GRADIO_TTS,
 *         permissions = listOf(PluginPermission.NETWORK, PluginPermission.GRADIO_ACCESS),
 *         // ... other manifest fields
 *     )
 *     
 *     override val gradioConfig = GradioConfig(
 *         defaultEndpoint = "http://localhost:7860",
 *         supportsCustomEndpoint = true
 *     )
 *     
 *     override suspend fun synthesize(request: GradioTTSRequest): GradioResult<GradioAudioResponse> {
 *         // Call Gradio API
 *     }
 * }
 * ```
 */
interface GradioTTSPlugin : Plugin {
    /**
     * Gradio endpoint configuration.
     */
    val gradioConfig: GradioConfig
    
    /**
     * Available TTS models.
     */
    val availableModels: List<GradioTTSModel>
    
    /**
     * Check if Gradio endpoint is reachable.
     */
    suspend fun checkConnection(endpoint: String = gradioConfig.defaultEndpoint): GradioResult<GradioStatus>
    
    /**
     * Get available voices/speakers for a model.
     */
    suspend fun getVoices(modelId: String): GradioResult<List<GradioVoice>>
    
    /**
     * Synthesize speech from text.
     */
    suspend fun synthesize(request: GradioTTSRequest): GradioResult<GradioAudioResponse>
    
    /**
     * Synthesize with voice cloning (if supported).
     */
    suspend fun synthesizeWithCloning(
        request: GradioTTSRequest,
        referenceAudio: ByteArray
    ): GradioResult<GradioAudioResponse>
    
    /**
     * Stream audio generation (for real-time playback).
     */
    suspend fun streamSynthesize(
        request: GradioTTSRequest,
        onChunk: (ByteArray) -> Unit
    ): GradioResult<Unit>
    
    /**
     * Cancel ongoing synthesis.
     */
    fun cancelSynthesis()
    
    /**
     * Get supported languages for a model.
     */
    fun getSupportedLanguages(modelId: String): List<String>
    
    /**
     * Configure custom endpoint.
     */
    fun setEndpoint(endpoint: String)
    
    /**
     * Get current endpoint.
     */
    fun getEndpoint(): String
}

/**
 * Gradio endpoint configuration.
 */
@Serializable
data class GradioConfig(
    /** Default Gradio endpoint URL */
    val defaultEndpoint: String,
    /** Whether custom endpoints are supported */
    val supportsCustomEndpoint: Boolean = true,
    /** API path for TTS */
    val apiPath: String = "/api/predict",
    /** Connection timeout in milliseconds */
    val connectionTimeoutMs: Long = 30000,
    /** Read timeout in milliseconds */
    val readTimeoutMs: Long = 120000,
    /** Whether streaming is supported */
    val supportsStreaming: Boolean = false,
    /** Whether voice cloning is supported */
    val supportsVoiceCloning: Boolean = false,
    /** Maximum text length per request */
    val maxTextLength: Int = 5000
)

/**
 * Gradio TTS model information.
 */
@Serializable
data class GradioTTSModel(
    /** Model identifier */
    val id: String,
    /** Model display name */
    val name: String,
    /** Model description */
    val description: String? = null,
    /** Supported languages */
    val languages: List<String>,
    /** Whether model supports voice cloning */
    val supportsCloning: Boolean = false,
    /** Whether model supports streaming */
    val supportsStreaming: Boolean = false,
    /** Model quality (1-5) */
    val quality: Int = 3,
    /** Average latency in milliseconds */
    val avgLatencyMs: Long? = null
)

/**
 * Voice/speaker information for Gradio TTS.
 */
@Serializable
data class GradioVoice(
    /** Voice identifier */
    val id: String,
    /** Voice display name */
    val name: String,
    /** Voice language */
    val language: String,
    /** Voice gender */
    val gender: VoiceGender = VoiceGender.NEUTRAL,
    /** Voice style/emotion */
    val style: String? = null,
    /** Sample audio URL */
    val sampleUrl: String? = null
)

/**
 * TTS synthesis request.
 */
@Serializable
data class GradioTTSRequest(
    /** Text to synthesize */
    val text: String,
    /** Model to use */
    val modelId: String,
    /** Voice/speaker ID */
    val voiceId: String? = null,
    /** Target language */
    val language: String = "en",
    /** Speech speed (0.5 - 2.0) */
    val speed: Float = 1.0f,
    /** Speech pitch (0.5 - 2.0) */
    val pitch: Float = 1.0f,
    /** Output audio format */
    val outputFormat: GradioAudioFormat = GradioAudioFormat.WAV,
    /** Custom endpoint (overrides default) */
    val endpoint: String? = null,
    /** Additional model-specific parameters */
    val extraParams: Map<String, String> = emptyMap()
)

/**
 * Audio format for Gradio TTS output.
 */
@Serializable
enum class GradioAudioFormat {
    WAV,
    MP3,
    OGG,
    FLAC
}

/**
 * Audio response from Gradio TTS.
 */
@Serializable
data class GradioAudioResponse(
    /** Audio data as bytes */
    val audioData: ByteArray,
    /** Audio format */
    val format: GradioAudioFormat,
    /** Sample rate */
    val sampleRate: Int,
    /** Duration in milliseconds */
    val durationMs: Long,
    /** Generation time in milliseconds */
    val generationTimeMs: Long
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as GradioAudioResponse
        return audioData.contentEquals(other.audioData) &&
                format == other.format &&
                sampleRate == other.sampleRate
    }
    
    override fun hashCode(): Int {
        var result = audioData.contentHashCode()
        result = 31 * result + format.hashCode()
        result = 31 * result + sampleRate
        return result
    }
}

/**
 * Gradio endpoint status.
 */
@Serializable
data class GradioStatus(
    /** Whether endpoint is reachable */
    val isOnline: Boolean,
    /** Endpoint URL */
    val endpoint: String,
    /** Available models */
    val availableModels: List<String> = emptyList(),
    /** Server version */
    val serverVersion: String? = null,
    /** GPU available */
    val gpuAvailable: Boolean = false,
    /** Current queue size */
    val queueSize: Int = 0,
    /** Latency in milliseconds */
    val latencyMs: Long? = null
)

/**
 * Result wrapper for Gradio operations.
 */
sealed class GradioResult<out T> {
    data class Success<T>(val data: T) : GradioResult<T>()
    data class Error(val error: GradioError) : GradioResult<Nothing>()
    
    fun getOrNull(): T? = when (this) {
        is Success -> data
        is Error -> null
    }
    
    inline fun <R> map(transform: (T) -> R): GradioResult<R> = when (this) {
        is Success -> Success(transform(data))
        is Error -> this
    }
    
    inline fun onSuccess(action: (T) -> Unit): GradioResult<T> {
        if (this is Success) action(data)
        return this
    }
    
    inline fun onError(action: (GradioError) -> Unit): GradioResult<T> {
        if (this is Error) action(error)
        return this
    }
}

/**
 * Gradio operation errors.
 */
@Serializable
sealed class GradioError {
    /** Endpoint not reachable */
    data class ConnectionFailed(val endpoint: String, val reason: String) : GradioError()
    /** Request timed out */
    data class Timeout(val timeoutMs: Long) : GradioError()
    /** Model not found */
    data class ModelNotFound(val modelId: String) : GradioError()
    /** Voice not found */
    data class VoiceNotFound(val voiceId: String) : GradioError()
    /** Text too long */
    data class TextTooLong(val maxLength: Int, val actualLength: Int) : GradioError()
    /** Server error */
    data class ServerError(val statusCode: Int, val message: String) : GradioError()
    /** Queue full */
    data class QueueFull(val queueSize: Int) : GradioError()
    /** Operation cancelled */
    data object Cancelled : GradioError()
    /** Unknown error */
    data class Unknown(val message: String) : GradioError()
}
