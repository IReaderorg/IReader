package ireader.domain.models.tts

import kotlinx.serialization.Serializable

/**
 * Voice model metadata representing a TTS voice
 * Requirements: 4.1, 4.2
 */
@Serializable
data class VoiceModel(
    val id: String,
    val name: String,
    val language: String,              // ISO 639-1 code (e.g., "en", "es")
    val locale: String,                // Full locale (e.g., "en-US", "es-MX")
    val gender: VoiceGender,
    val quality: VoiceQuality,
    val sampleRate: Int,               // Audio sample rate (22050, 44100)
    val modelSize: Long,               // File size in bytes
    val downloadUrl: String,           // CDN URL for model file
    val configUrl: String,             // CDN URL for config file
    val checksum: String,              // SHA-256 checksum
    val license: String,               // License type
    val description: String,           // User-friendly description
    val tags: List<String> = emptyList() // Searchable tags
)

/**
 * Voice gender classification
 */
@Serializable
enum class VoiceGender {
    MALE,
    FEMALE,
    NEUTRAL
}

/**
 * Voice quality levels
 */
@Serializable
enum class VoiceQuality {
    LOW,
    MEDIUM,
    HIGH,
    PREMIUM
}
