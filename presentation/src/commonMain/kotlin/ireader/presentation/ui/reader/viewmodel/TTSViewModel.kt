package ireader.presentation.ui.reader.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import ireader.core.log.Log
import ireader.core.source.model.Text
import ireader.domain.models.entities.Chapter
import ireader.domain.preferences.prefs.ReaderPreferences
import ireader.domain.services.common.ServiceResult
import ireader.domain.services.common.TTSChapterInfo
import ireader.domain.services.common.TTSPlaybackState
import ireader.domain.services.common.TTSService
import ireader.domain.services.common.VoiceQuality
import ireader.i18n.UiText
import ireader.presentation.ui.core.viewmodel.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for TTS playback using sealed state pattern.
 * 
 * Uses a single immutable StateFlow<TTSState> instead of multiple mutable states.
 * This provides:
 * - Single source of truth for TTS state
 * - Atomic state updates
 * - Better Compose performance with @Immutable state
 * - Clear Idle/Ready/Error states
 */
class TTSViewModel(
    private val ttsService: TTSService,
    private val readerPreferences: ReaderPreferences,
) : BaseViewModel() {

    // ==================== State Management ====================
    
    private val _state = MutableStateFlow<TTSState>(TTSState.Idle)
    val state: StateFlow<TTSState> = _state.asStateFlow()
    
    // Dialog state (separate for simpler updates)
    var currentDialog by mutableStateOf<TTSDialog>(TTSDialog.None)
        private set
    
    // Helper to update Ready state only
    private inline fun updateReadyState(crossinline update: (TTSState.Ready) -> TTSState.Ready) {
        _state.update { current ->
            when (current) {
                is TTSState.Ready -> update(current)
                else -> current
            }
        }
    }
    
    // ==================== Initialization ====================
    
    init {
        initializeState()
        observeTTSService()
        loadAvailableVoices()
    }
    
    private fun initializeState() {
        // Initialize with Ready state and load preferences
        _state.value = TTSState.Ready(
            speed = readerPreferences.speechRate().get(),
            pitch = readerPreferences.speechPitch().get(),
            selectedVoiceId = readerPreferences.speechLanguage().get(),
            autoPlay = readerPreferences.readerAutoNext().get(),
            highlightText = readerPreferences.followTTSSpeaker().get(),
            skipEmptyLines = readerPreferences.selectableText().get(),
        )
    }
    
    private fun observeTTSService() {
        // Observe TTS playback state
        ttsService.playbackState.onEach { playbackState ->
            updateReadyState { it.copy(playbackState = playbackState) }
        }.launchIn(scope)
        
        // Observe progress
        ttsService.progress.onEach { progress ->
            updateReadyState { state ->
                val sentenceIndex = (progress * state.totalSentences).toInt()
                    .coerceIn(0, maxOf(0, state.totalSentences - 1))
                state.copy(
                    playbackProgress = progress,
                    currentSentenceIndex = sentenceIndex
                )
            }
        }.launchIn(scope)
    }
    
    // ==================== Playback Control ====================
    
    /**
     * Start or resume TTS playback
     */
    fun play(chapter: Chapter? = null) {
        scope.launch {
            try {
                val currentState = _state.value
                
                if (chapter != null) {
                    // Start new playback
                    val text = extractTextFromChapter(chapter, currentState)
                    
                    if (text.isEmpty()) {
                        showSnackBar(UiText.DynamicString("No text to read"))
                        return@launch
                    }
                    
                    updateReadyState { state ->
                        state.copy(
                            totalSentences = text.size,
                            currentSentenceIndex = 0,
                            chapterId = chapter.id,
                            chapterName = chapter.name,
                            bookId = chapter.bookId,
                        )
                    }
                    
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
                    
                    handleServiceResult(result)
                } else {
                    // Resume playback
                    ttsService.resume()
                }
            } catch (e: Exception) {
                Log.error("TTS play failed", e)
                handleError(e)
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
                handleError(e)
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
                updateReadyState { state ->
                    state.copy(
                        currentSentenceIndex = 0,
                        playbackProgress = 0f,
                        currentText = ""
                    )
                }
            } catch (e: Exception) {
                Log.error("TTS stop failed", e)
                handleError(e)
            }
        }
    }
    
    /**
     * Toggle play/pause
     */
    fun togglePlayPause(chapter: Chapter? = null) {
        val currentState = _state.value
        if (currentState !is TTSState.Ready) return
        
        when {
            currentState.isPlaying -> pause()
            currentState.isPaused -> play()
            else -> play(chapter)
        }
    }
    
    /**
     * Skip to next sentence
     */
    fun skipNext() {
        scope.launch {
            try {
                val currentState = _state.value
                if (currentState !is TTSState.Ready) return@launch
                
                if (currentState.canSkipNext) {
                    updateReadyState { it.copy(currentSentenceIndex = it.currentSentenceIndex + 1) }
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
                val currentState = _state.value
                if (currentState !is TTSState.Ready) return@launch
                
                if (currentState.canSkipPrevious) {
                    updateReadyState { it.copy(currentSentenceIndex = it.currentSentenceIndex - 1) }
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
                val currentState = _state.value
                if (currentState !is TTSState.Ready) return@launch
                
                val targetIndex = (position * currentState.totalSentences).toInt()
                    .coerceIn(0, maxOf(0, currentState.totalSentences - 1))
                
                val diff = targetIndex - currentState.currentSentenceIndex
                updateReadyState { it.copy(currentSentenceIndex = targetIndex) }
                
                // Use skip methods since TTSService doesn't have seekTo
                if (diff > 0) {
                    repeat(diff) { ttsService.skipNext() }
                } else if (diff < 0) {
                    repeat(-diff) { ttsService.skipPrevious() }
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
            updateReadyState { it.copy(speed = clampedSpeed) }
            
            val currentState = _state.value
            if (currentState is TTSState.Ready && currentState.isPlaying) {
                ttsService.setSpeed(clampedSpeed)
            }
        }
    }
    
    /**
     * Increase speed by 0.1
     */
    fun increaseSpeed() {
        val currentState = _state.value
        if (currentState is TTSState.Ready) {
            setSpeed((currentState.speed + 0.1f).coerceAtMost(2.0f))
        }
    }
    
    /**
     * Decrease speed by 0.1
     */
    fun decreaseSpeed() {
        val currentState = _state.value
        if (currentState is TTSState.Ready) {
            setSpeed((currentState.speed - 0.1f).coerceAtLeast(0.5f))
        }
    }
    
    /**
     * Set TTS pitch (0.5 to 2.0)
     */
    fun setPitch(pitch: Float) {
        scope.launch {
            val clampedPitch = pitch.coerceIn(0.5f, 2.0f)
            readerPreferences.speechPitch().set(clampedPitch)
            updateReadyState { it.copy(pitch = clampedPitch) }
            
            val currentState = _state.value
            if (currentState is TTSState.Ready && currentState.isPlaying) {
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
            updateReadyState { it.copy(selectedVoiceId = voiceId) }
            
            val currentState = _state.value
            if (currentState is TTSState.Ready && currentState.isPlaying) {
                ttsService.setVoice(voiceId)
            }
        }
    }
    
    /**
     * Toggle auto-play next chapter
     */
    fun toggleAutoPlay(enabled: Boolean) {
        scope.launch {
            readerPreferences.readerAutoNext().set(enabled)
            updateReadyState { it.copy(autoPlay = enabled) }
        }
    }
    
    /**
     * Toggle text highlighting
     */
    fun toggleTextHighlight(enabled: Boolean) {
        scope.launch {
            readerPreferences.followTTSSpeaker().set(enabled)
            updateReadyState { it.copy(highlightText = enabled) }
        }
    }
    
    /**
     * Toggle skip empty lines
     */
    fun toggleSkipEmptyLines(enabled: Boolean) {
        scope.launch {
            readerPreferences.selectableText().set(enabled)
            updateReadyState { it.copy(skipEmptyLines = enabled) }
        }
    }
    
    // ==================== Voice Management ====================
    
    /**
     * Load available TTS voices
     */
    private fun loadAvailableVoices() {
        scope.launch {
            updateReadyState { it.copy(isLoadingVoices = true) }
            
            try {
                val result = ttsService.fetchAvailableVoices()
                
                when (result) {
                    is ServiceResult.Success -> {
                        val voices = result.data.map { voice ->
                            TTSVoiceInfo(
                                id = voice.id,
                                name = voice.name,
                                language = voice.language,
                                quality = voice.quality,
                                isDownloaded = voice.isDownloaded
                            )
                        }
                        updateReadyState { it.copy(
                            availableVoices = voices,
                            isLoadingVoices = false
                        )}
                        Log.debug("Loaded ${voices.size} TTS voices")
                    }
                    is ServiceResult.Error -> {
                        Log.error("Failed to load TTS voices: ${result.message}")
                        updateReadyState { it.copy(
                            availableVoices = emptyList(),
                            isLoadingVoices = false
                        )}
                    }
                    is ServiceResult.Loading -> {
                        // Loading state handled above
                    }
                }
            } catch (e: Exception) {
                Log.error("Failed to load TTS voices", e)
                updateReadyState { it.copy(
                    availableVoices = emptyList(),
                    isLoadingVoices = false
                )}
            }
        }
    }
    
    /**
     * Refresh available voices
     */
    fun refreshVoices() {
        loadAvailableVoices()
    }
    
    /**
     * Download a TTS voice
     */
    fun downloadVoice(voiceId: String) {
        scope.launch {
            try {
                updateReadyState { it.copy(downloadingVoiceId = voiceId, downloadProgress = 0f) }
                showSnackBar(UiText.DynamicString("Downloading voice..."))
                
                val result = ttsService.downloadVoice(voiceId, showNotification = true)
                
                when (result) {
                    is ServiceResult.Success -> {
                        loadAvailableVoices()
                        showSnackBar(UiText.DynamicString("Voice downloaded"))
                    }
                    is ServiceResult.Error -> {
                        showSnackBar(UiText.DynamicString("Download failed: ${result.message}"))
                    }
                    is ServiceResult.Loading -> {
                        // Loading state
                    }
                }
            } catch (e: Exception) {
                Log.error("Failed to download voice", e)
                showSnackBar(UiText.DynamicString("Download failed: ${e.message}"))
            } finally {
                updateReadyState { it.copy(downloadingVoiceId = null, downloadProgress = 0f) }
            }
        }
    }
    
    // ==================== Dialog Management ====================
    
    fun showDialog(dialog: TTSDialog) {
        currentDialog = dialog
    }
    
    fun dismissDialog() {
        currentDialog = TTSDialog.None
    }
    
    // ==================== Utility ====================
    
    private fun extractTextFromChapter(chapter: Chapter, state: TTSState): List<String> {
        val skipEmpty = when (state) {
            is TTSState.Ready -> state.skipEmptyLines
            else -> true
        }
        
        return chapter.content
            .filterIsInstance<Text>()
            .map { it.text }
            .filter { text -> if (skipEmpty) text.isNotBlank() else true }
    }
    
    private fun handleServiceResult(result: ServiceResult<Unit>) {
        when (result) {
            is ServiceResult.Error -> {
                showSnackBar(UiText.DynamicString("TTS error: ${result.message ?: "Unknown error"}"))
            }
            is ServiceResult.Loading, is ServiceResult.Success -> {
                // No action needed
            }
        }
    }
    
    private fun handleError(e: Exception) {
        showSnackBar(UiText.DynamicString("TTS error: ${e.message ?: "Unknown error"}"))
    }
    
    /**
     * Check if TTS is available
     */
    fun isTTSAvailable(): Boolean {
        val currentState = _state.value
        return currentState is TTSState.Ready && currentState.hasVoices
    }
    
    /**
     * Get current sentence being read
     */
    fun getCurrentSentence(): String? {
        val currentState = _state.value
        return if (currentState is TTSState.Ready && currentState.currentText.isNotEmpty()) {
            currentState.currentText
        } else {
            null
        }
    }
    
    // ==================== Cleanup ====================
    
    override fun onCleared() {
        super.onCleared()
        scope.launch {
            ttsService.stop()
        }
    }
}
