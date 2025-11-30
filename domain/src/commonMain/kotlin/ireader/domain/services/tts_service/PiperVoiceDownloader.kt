package ireader.domain.services.tts_service

import ireader.domain.models.tts.PiperVoice

/**
 * Interface for platform-specific voice downloading.
 * Implemented on Desktop only.
 */
interface PiperVoiceDownloader {
    /**
     * Download a voice model.
     * @param voice The voice to download
     * @param onProgress Progress callback (0.0 to 1.0)
     */
    suspend fun downloadVoice(voice: PiperVoice, onProgress: (Float) -> Unit)
    
    /**
     * Delete a downloaded voice model.
     */
    suspend fun deleteVoice(voiceId: String)
    
    /**
     * Check if a voice is downloaded.
     */
    suspend fun isVoiceDownloaded(voiceId: String): Boolean
    
    /**
     * Get the file paths for a downloaded voice.
     */
    fun getVoicePaths(voiceId: String): VoicePaths?
}

/**
 * Paths to downloaded voice model files.
 */
data class VoicePaths(
    val modelPath: String,
    val configPath: String
)
