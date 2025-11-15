package ireader.domain.preferences

/**
 * Interface for managing voice selection preferences
 * Persists user's voice and language selections across app sessions
 * Requirements: 6.1, 6.2, 6.3, 6.4
 */
interface VoicePreferences {
    /**
     * Get the selected voice ID for a specific TTS engine
     * @param engine TTS engine type (PIPER, KOKORO, MAYA, SIMULATION)
     * @return Voice ID if set, null otherwise
     */
    fun getSelectedVoiceId(engine: String): String?
    
    /**
     * Set the selected voice ID for a specific TTS engine
     * @param engine TTS engine type
     * @param voiceId Voice ID to set
     */
    fun setSelectedVoiceId(engine: String, voiceId: String)
    
    /**
     * Get the selected language for a specific TTS engine
     * @param engine TTS engine type
     * @return Language code if set, null otherwise
     */
    fun getSelectedLanguage(engine: String): String?
    
    /**
     * Set the selected language for a specific TTS engine
     * @param engine TTS engine type
     * @param language Language code to set
     */
    fun setSelectedLanguage(engine: String, language: String)
    
    /**
     * Get the selected Kokoro voice ID
     * @return Voice ID if set, null otherwise
     */
    fun getSelectedKokoroVoice(): String?
    
    /**
     * Set the selected Kokoro voice ID
     * @param voiceId Voice ID to set
     */
    fun setSelectedKokoroVoice(voiceId: String)
    
    /**
     * Get the selected Maya language
     * @return Language code if set, defaults to "en"
     */
    fun getSelectedMayaLanguage(): String?
    
    /**
     * Set the selected Maya language
     * @param language Language code to set
     */
    fun setSelectedMayaLanguage(language: String)
    
    /**
     * Get set of installed voice IDs
     * @return Set of voice IDs that are installed
     */
    fun getInstalledVoices(): Set<String>
    
    /**
     * Set the installed voice IDs
     * @param voices Set of voice IDs to mark as installed
     */
    fun setInstalledVoices(voices: Set<String>)
}
