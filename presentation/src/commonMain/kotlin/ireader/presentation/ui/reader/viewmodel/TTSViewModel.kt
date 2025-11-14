package ireader.presentation.ui.reader.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

/**
 * ViewModel for TTS playback controls
 * Requirements: 10.1, 10.2, 10.3, 10.4
 */
class TTSViewModel : ViewModel() {
    
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()
    
    private val _currentPosition = MutableStateFlow(0.milliseconds)
    val currentPosition: StateFlow<Duration> = _currentPosition.asStateFlow()
    
    private val _duration = MutableStateFlow(0.milliseconds)
    val duration: StateFlow<Duration> = _duration.asStateFlow()
    
    private val _speechRate = MutableStateFlow(1.0f)
    val speechRate: StateFlow<Float> = _speechRate.asStateFlow()
    
    private val _currentTextIndex = MutableStateFlow(0)
    val currentTextIndex: StateFlow<Int> = _currentTextIndex.asStateFlow()
    
    private var playbackJob: Job? = null
    
    /**
     * Toggle play/pause
     */
    fun togglePlayPause() {
        if (_isPlaying.value) {
            pause()
        } else {
            play()
        }
    }
    
    /**
     * Start playback
     */
    fun play() {
        _isPlaying.value = true
        startPlaybackTimer()
    }
    
    /**
     * Pause playback
     */
    fun pause() {
        _isPlaying.value = false
        playbackJob?.cancel()
        playbackJob = null
    }
    
    /**
     * Stop playback and reset position
     */
    fun stop() {
        pause()
        _currentPosition.value = 0.milliseconds
        _currentTextIndex.value = 0
    }
    
    /**
     * Skip backward by 10 seconds
     */
    fun skipBackward() {
        val newPosition = (_currentPosition.value - 10.seconds).coerceAtLeast(0.milliseconds)
        _currentPosition.value = newPosition
        updateTextIndexForPosition(newPosition)
    }
    
    /**
     * Skip forward by 10 seconds
     */
    fun skipForward() {
        val newPosition = (_currentPosition.value + 10.seconds).coerceAtMost(_duration.value)
        _currentPosition.value = newPosition
        updateTextIndexForPosition(newPosition)
    }
    
    /**
     * Set speech rate (0.5 - 2.0)
     */
    fun setSpeechRate(rate: Float) {
        val clampedRate = rate.coerceIn(0.5f, 2.0f)
        _speechRate.value = clampedRate
        
        // TODO: Apply to actual TTS engine when integrated
    }
    
    /**
     * Seek to specific position
     */
    fun seekTo(position: Duration) {
        _currentPosition.value = position.coerceIn(0.milliseconds, _duration.value)
        updateTextIndexForPosition(position)
    }
    
    /**
     * Set total duration
     */
    fun setDuration(duration: Duration) {
        _duration.value = duration
    }
    
    private val _state = MutableStateFlow(TTSState())
    val state: StateFlow<TTSState> = _state.asStateFlow()
    
    /**
     * Set current text being read
     */
    fun setCurrentText(text: String, index: Int) {
        _currentTextIndex.value = index
        _state.update { it.copy(currentText = text) }
    }
    
    /**
     * Start playback timer
     */
    private fun startPlaybackTimer() {
        playbackJob?.cancel()
        playbackJob = viewModelScope.launch {
            while (_isPlaying.value && _currentPosition.value < _duration.value) {
                delay(100) // Update every 100ms
                _currentPosition.update { 
                    (it + 100.milliseconds).coerceAtMost(_duration.value)
                }
                
                // Check if we've reached the end
                if (_currentPosition.value >= _duration.value) {
                    stop()
                }
            }
        }
    }
    
    /**
     * Update text index based on playback position
     */
    private fun updateTextIndexForPosition(position: Duration) {
        // TODO: Calculate which text segment corresponds to this position
        // This will be implemented when integrated with actual TTS service
    }
    
    override fun onCleared() {
        super.onCleared()
        playbackJob?.cancel()
    }
}

/**
 * State for TTS playback
 */
data class TTSState(
    val currentText: String = "",
    val error: String? = null
)
