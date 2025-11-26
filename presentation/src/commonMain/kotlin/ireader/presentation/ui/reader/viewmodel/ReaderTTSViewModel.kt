package ireader.presentation.ui.reader.viewmodel

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import ireader.core.log.Log
import ireader.domain.models.entities.Chapter
import ireader.domain.preferences.prefs.ReaderPreferences
import ireader.domain.services.common.TTSService
import ireader.domain.services.common.TTSPlaybackState
import ireader.domain.services.common.ServiceResult
import ireader.domain.services.common.TTSChapterInfo
import ireader.domain.services.common.VoiceQuality
import ireader.i18n.UiText
import ireader.presentation.ui.core.viewmodel.BaseViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

/**
 * ViewModel responsible for Text-to-Speech (TTS) control
 * 
 * Handles:
 * - TTS playback control (play, pause, stop)
 * - Voice selection
 * - Speed and pitch control
 * - TTS state management
 * - Chapter navigation via TTS
 */
class ReaderTTSViewModel(
    private val ttsService: TTSService,
    private val readerPreferences: ReaderPreferences,
) : BaseViewModel() {
    
    // TTS state from service
    private val ttsServiceState = ttsService.playbackState
    
    var currentTTSState by mutableStateOf<TTSPlaybackState>(TTSPlaybackState.IDLE)
        private set
    
    // Derived states
    val isPlaying by derivedStateOf { currentTTSState == TTSPlaybackState.PLAYING }
    val isPaused by derivedStateOf { currentTTSState == TTSPlaybackState.PAUSED }
    val isStopped by derivedStateOf { currentTTSState == TTSPlaybackState.IDLE }
    val isLoading by derivedStateOf { currentTTSState == TTSPlaybackState.INITIALIZING }
    
    // TTS preferences
    val ttsSpeed = readerPreferences.speechRate().asState()
    val ttsPitch = readerPreferences.speechPitch().asState()
    val ttsVoice = readerPreferences.speechLanguage().asState()
    val ttsAutoPlay = readerPreferences.readerAutoNext().asState()
    val ttsHighlightText = readerPreferences.followTTSSpeaker().asState()
    val ttsSkipEmptyLines = readerPreferences.selectableText().asState()
    
    // Current playback info
    var currentSentenceIndex by mutableStateOf(0)
        private set
    
    var totalSentences by mutableStateOf(0)
        private set
    
    var playbackProgress by mutableStateOf(0f)
        private set
    
    // Available voices
    var availableVoices by mutableStateOf<List<TTSVoiceInfo>>(emptyList())
        private set
    
    var isLoadingVoices by mutableStateOf(false)
        private set
    
    init {
        // Observe TTS state changes
        ttsServiceState.onEach { state ->
            currentTTSState = state
            updatePlaybackProgress()
        }.launchIn(scope)
        
        // Observe progress changes
        ttsService.progress.onEach { progress ->
            playbackProgress = progress
        }.launchIn(scope)
        
        // Load available voices
        loadAvailableVoices()
    }
    
    // ==================== Playback Control ====================
    
    /**
     * Start or resume TTS playback
     */
    fun play(chapter: Chapter? = null) {
        scope.launch {
            try {
                if (chapter != null) {
                    // Start new playback
                    val text = extractTextFromChapter(chapter)
                    
                    if (text.isEmpty()) {
                        showSnackBar(UiText.DynamicString("No text to read"))
                        return@launch
                    }
                    
                    totalSentences = text.size
                    currentSentenceIndex = 0
                    
                    val chapterInfo = TTSChapterInfo(
                        chapterId = chapter.id,
                        chapterName = chapter.name,
                        bookId = chapter.bookId,
                        bookName = "",
                        totalParagraphs = text.size,
                        currentParagraph = 0
                    )
                    
                    val result = ttsService.speak(
                        text = text.joinToString(" "),
                        chapterInfo = chapterInfo
                    )
                    
                    when (result) {
                        is ServiceResult.Error -> {
                            showSnackBar(UiText.DynamicString("TTS error: ${result.message}"))
                        }
                        is ServiceResult.Loading -> {
                            // Loading state - do nothing
                        }
                        is ServiceResult.Success -> {
                            // Success - do nothing
                        }
                    }
                } else {
                    // Resume playback
                    ttsService.resume()
                }
                
            } catch (e: Exception) {
                Log.error("TTS play failed", e)
                showSnackBar(UiText.DynamicString("TTS error: ${e.message}"))
            }
        }
    }
    
    /**
     * Pause TTS playback
     */
    fun pause() {
        scope.launch {
            try {
                ttsService.pause()
            } catch (e: Exception) {
                Log.error("TTS pause failed", e)
                showSnackBar(UiText.DynamicString("TTS error: ${e.message}"))
            }
        }
    }
    
    /**
     * Stop TTS playback
     */
    fun stop() {
        scope.launch {
            try {
                ttsService.stop()
                currentSentenceIndex = 0
                playbackProgress = 0f
            } catch (e: Exception) {
                Log.error("TTS stop failed", e)
                showSnackBar(UiText.DynamicString("TTS error: ${e.message}"))
            }
        }
    }
    
    /**
     * Toggle play/pause
     */
    fun togglePlayPause(chapter: Chapter? = null) {
        when {
            isPlaying -> pause()
            isPaused -> play()
            else -> play(chapter)
        }
    }
    
    /**
     * Skip to next sentence
     */
    fun skipNext() {
        scope.launch {
            try {
                if (currentSentenceIndex < totalSentences - 1) {
                    currentSentenceIndex++
                    ttsService.skipNext()
                }
            } catch (e: Exception) {
                Log.error("TTS skip next failed", e)
            }
        }
    }
    
    /**
     * Skip to previous sentence
     */
    fun skipPrevious() {
        scope.launch {
            try {
                if (currentSentenceIndex > 0) {
                    currentSentenceIndex--
                    ttsService.skipPrevious()
                }
            } catch (e: Exception) {
                Log.error("TTS skip previous failed", e)
            }
        }
    }
    
    /**
     * Seek to specific position (0.0 to 1.0)
     */
    fun seekTo(position: Float) {
        scope.launch {
            try {
                val targetIndex = (position * totalSentences).toInt()
                    .coerceIn(0, totalSentences - 1)
                
                val diff = targetIndex - currentSentenceIndex
                currentSentenceIndex = targetIndex
                
                // Note: TTSService doesn't have seekTo, using skipNext/skipPrevious instead
                if (diff > 0) {
                    repeat(diff) { skipNext() }
                } else if (diff < 0) {
                    repeat(-diff) { skipPrevious() }
                }
            } catch (e: Exception) {
                Log.error("TTS seek failed", e)
            }
        }
    }
    
    // ==================== Settings ====================
    
    /**
     * Set TTS speed (0.5 to 2.0)
     */
    fun setSpeed(speed: Float) {
        scope.launch {
            val clampedSpeed = speed.coerceIn(0.5f, 2.0f)
            readerPreferences.speechRate().set(clampedSpeed)
            
            // Update current playback if active
            if (isPlaying) {
                ttsService.setSpeed(clampedSpeed)
            }
        }
    }
    
    /**
     * Increase speed
     */
    fun increaseSpeed() {
        val newSpeed = (ttsSpeed.value + 0.1f).coerceAtMost(2.0f)
        setSpeed(newSpeed)
    }
    
    /**
     * Decrease speed
     */
    fun decreaseSpeed() {
        val newSpeed = (ttsSpeed.value - 0.1f).coerceAtLeast(0.5f)
        setSpeed(newSpeed)
    }
    
    /**
     * Set TTS pitch (0.5 to 2.0)
     */
    fun setPitch(pitch: Float) {
        scope.launch {
            val clampedPitch = pitch.coerceIn(0.5f, 2.0f)
            readerPreferences.speechPitch().set(clampedPitch)
            
            // Update current playback if active
            if (isPlaying) {
                ttsService.setPitch(clampedPitch)
            }
        }
    }
    
    /**
     * Set TTS voice
     */
    fun setVoice(voiceId: String) {
        scope.launch {
            readerPreferences.speechLanguage().set(voiceId)
            
            // Update current playback if active
            if (isPlaying) {
                ttsService.setVoice(voiceId)
            }
        }
    }
    
    /**
     * Toggle auto-play
     */
    fun toggleAutoPlay(enabled: Boolean) {
        scope.launch {
            readerPreferences.readerAutoNext().set(enabled)
        }
    }
    
    /**
     * Toggle text highlighting
     */
    fun toggleTextHighlight(enabled: Boolean) {
        scope.launch {
            readerPreferences.followTTSSpeaker().set(enabled)
        }
    }
    
    /**
     * Toggle skip empty lines
     */
    fun toggleSkipEmptyLines(enabled: Boolean) {
        scope.launch {
            readerPreferences.selectableText().set(enabled)
        }
    }
    
    // ==================== Voice Management ====================
    
    /**
     * Load available TTS voices
     */
    private fun loadAvailableVoices() {
        scope.launch {
            isLoadingVoices = true
            
            try {
                // Get voices from TTS service
                val result = ttsService.fetchAvailableVoices()
                
                when (result) {
                    is ServiceResult.Success -> {
                        availableVoices = result.data.map { voice ->
                            TTSVoiceInfo(
                                id = voice.id,
                                name = voice.name,
                                language = voice.language,
                                quality = voice.quality,
                                isDownloaded = voice.isDownloaded
                            )
                        }
                        Log.debug("Loaded ${availableVoices.size} TTS voices")
                    }
                    is ServiceResult.Error -> {
                        Log.error("Failed to load TTS voices: ${result.message}")
                        availableVoices = emptyList()
                    }
                    is ServiceResult.Loading -> {
                        // Loading state - do nothing
                    }
                }
                
            } catch (e: Exception) {
                Log.error("Failed to load TTS voices", e)
                availableVoices = emptyList()
            } finally {
                isLoadingVoices = false
            }
        }
    }
    
    /**
     * Download a TTS voice
     */
    fun downloadVoice(voiceId: String) {
        scope.launch {
            try {
                showSnackBar(UiText.DynamicString("Downloading voice..."))
                
                val result = ttsService.downloadVoice(voiceId, showNotification = true)
                
                when (result) {
                    is ServiceResult.Success -> {
                        // Reload voices
                        loadAvailableVoices()
                        showSnackBar(UiText.DynamicString("Voice downloaded"))
                    }
                    is ServiceResult.Error -> {
                        showSnackBar(UiText.DynamicString("Download failed: ${result.message}"))
                    }
                    is ServiceResult.Loading -> {
                        // Loading state - do nothing
                    }
                }
                
            } catch (e: Exception) {
                Log.error("Failed to download voice", e)
                showSnackBar(UiText.DynamicString("Download failed: ${e.message}"))
            }
        }
    }
    
    /**
     * Delete a TTS voice
     */
    fun deleteVoice(voiceId: String) {
        scope.launch {
            try {
                // Note: TTSService doesn't have deleteVoice method
                // This functionality may need to be implemented in the service
                showSnackBar(UiText.DynamicString("Delete voice not supported yet"))
                
            } catch (e: Exception) {
                Log.error("Failed to delete voice", e)
                showSnackBar(UiText.DynamicString("Delete failed: ${e.message}"))
            }
        }
    }
    
    // ==================== Utility ====================
    
    /**
     * Extract text from chapter for TTS
     */
    private fun extractTextFromChapter(chapter: Chapter): List<String> {
        val content = chapter.content
        
        if (content.isEmpty()) return emptyList()
        
        return content
            .filterIsInstance<ireader.core.source.model.Text>()
            .map { it.text }
            .filter { text ->
                // Skip empty lines if enabled
                if (ttsSkipEmptyLines.value) {
                    text.isNotBlank()
                } else {
                    true
                }
            }
    }
    
    /**
     * Update playback progress
     */
    private fun updatePlaybackProgress() {
        if (totalSentences > 0) {
            playbackProgress = currentSentenceIndex.toFloat() / totalSentences.toFloat()
        } else {
            playbackProgress = 0f
        }
    }
    
    /**
     * Get current sentence being read
     */
    fun getCurrentSentence(): String? {
        // This would be provided by the TTS service
        return null
    }
    
    /**
     * Check if TTS is available
     */
    fun isTTSAvailable(): Boolean {
        return availableVoices.isNotEmpty()
    }
    
    override fun onCleared() {
        super.onCleared()
        // Stop TTS when ViewModel is cleared
        scope.launch {
            ttsService.stop()
        }
    }
}

/**
 * TTS voice information
 */
data class TTSVoiceInfo(
    val id: String,
    val name: String,
    val language: String,
    val quality: VoiceQuality = VoiceQuality.NORMAL,
    val isDownloaded: Boolean = false
)

/**
 * Voice quality enumeration
 */
enum class VoiceQuality {
    LOW,
    NORMAL,
    HIGH,
    VERY_HIGH
}
