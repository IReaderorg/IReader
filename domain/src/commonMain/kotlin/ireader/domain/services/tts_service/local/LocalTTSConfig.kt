package ireader.domain.services.tts_service.local

import kotlinx.serialization.Serializable

/**
 * Supported API formats for Local TTS servers
 */
@Serializable
enum class LocalTTSApiFormat {
    /**
     * Standard OpenAI audio speech API (POST /v1/audio/speech)
     * Payload: {"input": "...", "model": "...", "voice": "...", "speed": 1.0, "response_format": "wav"}
     */
    OPENAI_SPEECH,

    /**
     * Simple REST API (POST /api/tts)
     * Payload: {"text": "...", "voice": "...", "speed": 1.0}
     */
    SIMPLE_REST
}

/**
 * Information about a voice exposed by the local TTS server
 */
@Serializable
data class LocalTTSVoice(
    val id: String,
    val name: String,
    val language: String = "fa",
    val gender: String = "neutral"
)

/**
 * Server health check response
 */
@Serializable
data class LocalTTSHealthResponse(
    val status: String = "ok",
    val model: String = "",
    val device: String = "cpu",
    val loaded: Boolean = true,
    val sample_rate: Int = 24000,
    val error: String? = null
)

/**
 * Configuration for connecting to a local or remote Chatterbox TTS server
 */
@Serializable
data class LocalTTSConfig(
    val serverUrl: String = "http://127.0.0.1:8000",
    val voice: String = "default",
    val speed: Float = 1.0f,
    val pitch: Float = 1.0f,
    val apiKey: String? = null,
    val apiFormat: LocalTTSApiFormat = LocalTTSApiFormat.SIMPLE_REST,
    val enabled: Boolean = true,
    val name: String = "Chatterbox Persian TTS (Local)",
    val timeoutMs: Long = 60000L
) {
    /**
     * Returns sanitized base URL without trailing slashes
     */
    val normalizedUrl: String
        get() {
            var url = serverUrl.trim().trimEnd('/')
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                url = "http://$url"
            }
            return url
        }
}
