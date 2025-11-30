package ireader.domain.models.tts

import kotlinx.serialization.Serializable

/**
 * Piper voice model entity for database storage.
 * Represents a voice from the Piper TTS catalog.
 */
@Serializable
data class PiperVoice(
    val id: String,
    val name: String,
    val language: String,
    val locale: String,
    val gender: VoiceGender,
    val quality: VoiceQuality,
    val sampleRate: Int = 22050,
    val modelSize: Long = 0,
    val downloadUrl: String,
    val configUrl: String,
    val checksum: String = "",
    val license: String = "MIT",
    val description: String = "",
    val tags: List<String> = emptyList(),
    val isDownloaded: Boolean = false,
    val lastUpdated: Long = 0
) {
    companion object {
        const val VOICES_JSON_URL = "https://rhasspy.github.io/piper-samples/voices.json"
        const val BASE_DOWNLOAD_URL = "https://huggingface.co/rhasspy/piper-voices/resolve/main/"
    }
}
