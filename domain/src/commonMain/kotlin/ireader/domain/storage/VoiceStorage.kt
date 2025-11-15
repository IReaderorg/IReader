package ireader.domain.storage

/**
 * Interface for managing voice model storage
 * Handles saving, loading, and deleting voice model files
 * Requirements: 3.1, 3.2
 */
interface VoiceStorage {
    /**
     * Save a voice model to storage
     * @param voiceId Unique identifier for the voice
     * @param data Voice model binary data
     * @return Result indicating success or failure
     */
    suspend fun saveVoiceModel(voiceId: String, data: ByteArray): Result<Unit>
    
    /**
     * Load a voice model from storage
     * @param voiceId Unique identifier for the voice
     * @return Result containing the voice model data or error
     */
    suspend fun loadVoiceModel(voiceId: String): Result<ByteArray>
    
    /**
     * Delete a voice model from storage
     * @param voiceId Unique identifier for the voice
     * @return Result indicating success or failure
     */
    suspend fun deleteVoiceModel(voiceId: String): Result<Unit>
    
    /**
     * Get list of all installed voice IDs
     * @return List of voice IDs that are currently installed
     */
    suspend fun getInstalledVoices(): List<String>
    
    /**
     * Get the file path for a voice model
     * @param voiceId Unique identifier for the voice
     * @return File path if voice exists, null otherwise
     */
    suspend fun getVoiceModelPath(voiceId: String): String?
    
    /**
     * Get the size of a voice model file
     * @param voiceId Unique identifier for the voice
     * @return Size in bytes, or 0 if voice doesn't exist
     */
    suspend fun getVoiceModelSize(voiceId: String): Long
}
