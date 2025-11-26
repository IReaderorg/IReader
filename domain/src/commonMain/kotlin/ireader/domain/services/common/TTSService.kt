package ireader.domain.services.common

import kotlinx.coroutines.flow.StateFlow

/**
 * Common Text-to-Speech service
 */
interface TTSService : PlatformService {
    /**
     * Current playback state
     */
    val playbackState: StateFlow<TTSPlaybackState>
    
    /**
     * Available voices
     */
    val availableVoices: StateFlow<List<TTSVoice>>
    
    /**
     * Current voice
     */
    val currentVoice: StateFlow<TTSVoice?>
    
    /**
     * Playback progress (0.0 to 1.0)
     */
    val progress: StateFlow<Float>
    
    /**
     * Current chapter being read
     */
    val currentChapter: StateFlow<TTSChapterInfo?>
    
    /**
     * Initialize TTS engine
     */
    suspend fun initializeEngine(
        engineType: TTSEngineType = TTSEngineType.SYSTEM
    ): ServiceResult<Unit>
    
    /**
     * Start reading text
     */
    suspend fun speak(
        text: String,
        chapterInfo: TTSChapterInfo? = null
    ): ServiceResult<Unit>
    
    /**
     * Start reading chapter
     */
    suspend fun speakChapter(
        chapterId: Long,
        startPosition: Int = 0
    ): ServiceResult<Unit>
    
    /**
     * Pause playback
     */
    suspend fun pause()
    
    /**
     * Resume playback
     */
    suspend fun resume()
    
    /**
     * Stop playback
     * Overrides PlatformService.stop()
     */
    override suspend fun stop()
    
    /**
     * Skip to next paragraph
     */
    suspend fun skipNext()
    
    /**
     * Skip to previous paragraph
     */
    suspend fun skipPrevious()
    
    /**
     * Set playback speed
     */
    suspend fun setSpeed(speed: Float): ServiceResult<Unit>
    
    /**
     * Set pitch
     */
    suspend fun setPitch(pitch: Float): ServiceResult<Unit>
    
    /**
     * Set voice
     */
    suspend fun setVoice(voiceId: String): ServiceResult<Unit>
    
    /**
     * Get available voices
     */
    suspend fun fetchAvailableVoices(): ServiceResult<List<TTSVoice>>
    
    /**
     * Download voice (for offline TTS)
     */
    suspend fun downloadVoice(
        voiceId: String,
        showNotification: Boolean = true
    ): ServiceResult<Unit>
}

/**
 * TTS playback state
 */
enum class TTSPlaybackState {
    IDLE,
    INITIALIZING,
    PLAYING,
    PAUSED,
    STOPPED,
    ERROR
}

/**
 * TTS engine type
 */
enum class TTSEngineType {
    SYSTEM,          // Android TTS / Desktop TTS
    PIPER,           // Offline neural TTS
    COQUI,           // Coqui TTS
    KOKORO,          // Kokoro TTS
    CLOUD,           // Cloud-based TTS
    PLUGIN           // Plugin-based TTS
}

/**
 * TTS voice information
 */
data class TTSVoice(
    val id: String,
    val name: String,
    val language: String,
    val locale: String,
    val gender: VoiceGender = VoiceGender.NEUTRAL,
    val engineType: TTSEngineType,
    val isDownloaded: Boolean = false,
    val quality: VoiceQuality = VoiceQuality.NORMAL,
    val sampleUrl: String? = null
)

/**
 * Voice gender
 */
enum class VoiceGender {
    MALE,
    FEMALE,
    NEUTRAL
}

/**
 * Voice quality
 */
enum class VoiceQuality {
    LOW,
    NORMAL,
    HIGH,
    VERY_HIGH
}

/**
 * Chapter information for TTS
 */
data class TTSChapterInfo(
    val chapterId: Long,
    val chapterName: String,
    val bookId: Long,
    val bookName: String,
    val totalParagraphs: Int = 0,
    val currentParagraph: Int = 0
)
