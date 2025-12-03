package ireader.domain.data.repository

import ireader.domain.models.tts.VoiceModel
import okio.Path

/**
 * Repository interface for managing voice models.
 * Uses Okio Path for KMP compatibility instead of java.io.File.
 * Requirements: 4.1, 4.2, 4.3, 4.4
 */
interface VoiceModelRepository {
    
    /**
     * Get all available voices from the catalog
     * @return List of all available voice models
     */
    suspend fun getAvailableVoices(): Result<List<VoiceModel>>
    
    /**
     * Get voices filtered by language
     * @param language ISO 639-1 language code (e.g., "en", "es")
     * @return List of voice models for the specified language
     */
    suspend fun getVoicesByLanguage(language: String): Result<List<VoiceModel>>
    
    /**
     * Download a voice model
     * @param voiceId Unique identifier of the voice to download
     * @param onProgress Callback for download progress (0.0 to 1.0)
     * @return Result containing the downloaded model path
     */
    suspend fun downloadVoice(
        voiceId: String, 
        onProgress: (Float) -> Unit
    ): Result<Path>
    
    /**
     * Delete a downloaded voice model
     * @param voiceId Unique identifier of the voice to delete
     * @return Result indicating success or failure
     */
    suspend fun deleteVoice(voiceId: String): Result<Unit>
    
    /**
     * Get list of installed (downloaded) voices
     * @return List of installed voice models
     */
    suspend fun getInstalledVoices(): Result<List<VoiceModel>>
    
    /**
     * Verify the integrity of a downloaded voice model
     * @param voiceId Unique identifier of the voice to verify
     * @return true if the voice model is valid and uncorrupted
     */
    suspend fun verifyVoiceIntegrity(voiceId: String): Boolean
    
    /**
     * Get total storage used by downloaded voice models
     * @return Storage usage in bytes
     */
    suspend fun getStorageUsage(): Long
    
    /**
     * Get a specific voice model by ID
     * @param voiceId Unique identifier of the voice
     * @return Voice model if found, null otherwise
     */
    suspend fun getVoiceById(voiceId: String): Result<VoiceModel?>
    
    /**
     * Check if a voice is downloaded
     * @param voiceId Unique identifier of the voice
     * @return true if the voice is downloaded and available locally
     */
    suspend fun isVoiceDownloaded(voiceId: String): Boolean
}
