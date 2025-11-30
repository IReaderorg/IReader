package ireader.domain.data.repository

import ireader.domain.models.tts.PiperVoice
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for managing Piper voice models.
 * Handles CRUD operations and synchronization with remote catalog.
 */
interface PiperVoiceRepository {
    
    /**
     * Get all voices as a Flow for reactive updates
     */
    fun subscribeAll(): Flow<List<PiperVoice>>
    
    /**
     * Get all voices
     */
    suspend fun getAll(): List<PiperVoice>
    
    /**
     * Get voice by ID
     */
    suspend fun getById(id: String): PiperVoice?
    
    /**
     * Get voices by language
     */
    suspend fun getByLanguage(language: String): List<PiperVoice>
    
    /**
     * Get downloaded voices
     */
    suspend fun getDownloaded(): List<PiperVoice>
    
    /**
     * Subscribe to downloaded voices
     */
    fun subscribeDownloaded(): Flow<List<PiperVoice>>
    
    /**
     * Get all distinct languages
     */
    suspend fun getLanguages(): List<String>
    
    /**
     * Insert or update a voice
     */
    suspend fun upsert(voice: PiperVoice)
    
    /**
     * Insert or update multiple voices
     */
    suspend fun upsertAll(voices: List<PiperVoice>)
    
    /**
     * Update download status for a voice
     */
    suspend fun updateDownloadStatus(id: String, isDownloaded: Boolean)
    
    /**
     * Delete all voices (for refresh)
     */
    suspend fun deleteAll()
    
    /**
     * Get total voice count
     */
    suspend fun count(): Long
    
    /**
     * Get downloaded voice count
     */
    suspend fun countDownloaded(): Long
    
    /**
     * Search voices by name, language, or tags
     */
    suspend fun search(query: String): List<PiperVoice>
}
