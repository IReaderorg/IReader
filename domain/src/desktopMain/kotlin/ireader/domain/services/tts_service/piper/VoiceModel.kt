package ireader.domain.services.tts_service.piper

import kotlinx.serialization.Serializable

/**
 * Represents a Piper voice model
 * 
 * @property id Unique identifier for the voice model
 * @property name Display name of the voice
 * @property language Language code (e.g., "en-US", "es-ES")
 * @property quality Voice quality level
 * @property gender Voice gender
 * @property sizeBytes Size of the model files in bytes
 * @property modelUrl URL to download the ONNX model file
 * @property configUrl URL to download the config JSON file
 * @property modelChecksum Optional MD5 checksum for model file verification
 * @property configChecksum Optional MD5 checksum for config file verification
 * @property isDownloaded Whether the model is downloaded locally
 */
@Serializable
data class VoiceModel(
    val id: String,
    val name: String,
    val language: String,
    val quality: Quality,
    val gender: Gender,
    val sizeBytes: Long,
    val modelUrl: String,
    val configUrl: String,
    val modelChecksum: String? = null,
    val configChecksum: String? = null,
    val isDownloaded: Boolean = false
) {
    @Serializable
    enum class Quality { LOW, MEDIUM, HIGH }
    
    @Serializable
    enum class Gender { MALE, FEMALE, NEUTRAL }
}
