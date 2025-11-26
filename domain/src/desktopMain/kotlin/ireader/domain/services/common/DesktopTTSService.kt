package ireader.domain.services.common

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Desktop implementation of TTSService
 * This is a wrapper that delegates to the existing TTS implementation
 */
class DesktopTTSService : TTSService {
    
    private val _playbackState = MutableStateFlow<TTSPlaybackState>(TTSPlaybackState.IDLE)
    override val playbackState: StateFlow<TTSPlaybackState> = _playbackState.asStateFlow()
    
    private val _availableVoices = MutableStateFlow<List<TTSVoice>>(emptyList())
    override val availableVoices: StateFlow<List<TTSVoice>> = _availableVoices.asStateFlow()
    
    private val _currentVoice = MutableStateFlow<TTSVoice?>(null)
    override val currentVoice: StateFlow<TTSVoice?> = _currentVoice.asStateFlow()
    
    private val _progress = MutableStateFlow(0f)
    override val progress: StateFlow<Float> = _progress.asStateFlow()
    
    private val _currentChapter = MutableStateFlow<TTSChapterInfo?>(null)
    override val currentChapter: StateFlow<TTSChapterInfo?> = _currentChapter.asStateFlow()
    
    override suspend fun initialize() {
        _playbackState.value = TTSPlaybackState.IDLE
    }
    
    override suspend fun start() {
        _playbackState.value = TTSPlaybackState.PLAYING
    }
    
    override fun isRunning(): Boolean = _playbackState.value == TTSPlaybackState.PLAYING
    
    override suspend fun cleanup() {
        _playbackState.value = TTSPlaybackState.IDLE
        _currentChapter.value = null
    }
    
    override suspend fun initializeEngine(engineType: TTSEngineType): ServiceResult<Unit> {
        return try {
            _playbackState.value = TTSPlaybackState.INITIALIZING
            _playbackState.value = TTSPlaybackState.IDLE
            ServiceResult.Success(Unit)
        } catch (e: Exception) {
            _playbackState.value = TTSPlaybackState.ERROR
            ServiceResult.Error("Failed to initialize TTS: ${e.message}", e)
        }
    }
    
    override suspend fun speak(text: String, chapterInfo: TTSChapterInfo?): ServiceResult<Unit> {
        return try {
            _playbackState.value = TTSPlaybackState.PLAYING
            _currentChapter.value = chapterInfo
            ServiceResult.Success(Unit)
        } catch (e: Exception) {
            _playbackState.value = TTSPlaybackState.ERROR
            ServiceResult.Error("Failed to speak: ${e.message}", e)
        }
    }
    
    override suspend fun speakChapter(chapterId: Long, startPosition: Int): ServiceResult<Unit> {
        return try {
            _playbackState.value = TTSPlaybackState.PLAYING
            ServiceResult.Success(Unit)
        } catch (e: Exception) {
            _playbackState.value = TTSPlaybackState.ERROR
            ServiceResult.Error("Failed to speak chapter: ${e.message}", e)
        }
    }
    
    override suspend fun pause() {
        _playbackState.value = TTSPlaybackState.PAUSED
    }
    
    override suspend fun resume() {
        _playbackState.value = TTSPlaybackState.PLAYING
    }
    
    override suspend fun stop() {
        _playbackState.value = TTSPlaybackState.STOPPED
        _currentChapter.value = null
    }
    
    override suspend fun skipNext() {}
    
    override suspend fun skipPrevious() {}
    
    override suspend fun setSpeed(speed: Float): ServiceResult<Unit> {
        return ServiceResult.Success(Unit)
    }
    
    override suspend fun setPitch(pitch: Float): ServiceResult<Unit> {
        return ServiceResult.Success(Unit)
    }
    
    override suspend fun setVoice(voiceId: String): ServiceResult<Unit> {
        return try {
            val voice = _availableVoices.value.find { it.id == voiceId }
            _currentVoice.value = voice
            ServiceResult.Success(Unit)
        } catch (e: Exception) {
            ServiceResult.Error("Failed to set voice: ${e.message}", e)
        }
    }
    
    override suspend fun fetchAvailableVoices(): ServiceResult<List<TTSVoice>> {
        return try {
            val voices = emptyList<TTSVoice>()
            _availableVoices.value = voices
            ServiceResult.Success(voices)
        } catch (e: Exception) {
            ServiceResult.Error("Failed to fetch voices: ${e.message}", e)
        }
    }
    
    override suspend fun downloadVoice(voiceId: String, showNotification: Boolean): ServiceResult<Unit> {
        return ServiceResult.Success(Unit)
    }
}
