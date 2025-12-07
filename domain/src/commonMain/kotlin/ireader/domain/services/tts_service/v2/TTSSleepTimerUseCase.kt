package ireader.domain.services.tts_service.v2

import ireader.core.log.Log
import ireader.domain.utils.extensions.currentTimeToLong
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * TTS Sleep Timer Use Case - Manages automatic playback stop after a set duration
 * 
 * Features:
 * - Set timer in minutes
 * - Track remaining time
 * - Auto-stop playback when timer expires
 * - Cancel timer
 */
class TTSSleepTimerUseCase {
    companion object {
        private const val TAG = "TTSSleepTimerUseCase"
        private const val UPDATE_INTERVAL_MS = 1000L // Update every second
    }
    
    /**
     * Sleep timer state
     */
    data class SleepTimerState(
        val isEnabled: Boolean = false,
        val remainingTimeMs: Long = 0L,
        val totalTimeMs: Long = 0L
    ) {
        val remainingMinutes: Int get() = (remainingTimeMs / 60000).toInt()
        val remainingSeconds: Int get() = ((remainingTimeMs % 60000) / 1000).toInt()
        val progress: Float get() = if (totalTimeMs > 0) 
            remainingTimeMs.toFloat() / totalTimeMs else 0f
        
        fun formatRemaining(): String {
            val mins = remainingMinutes
            val secs = remainingSeconds
            return if (mins > 0) "${mins}m ${secs}s" else "${secs}s"
        }
    }
    
    private val _state = MutableStateFlow(SleepTimerState())
    val state: StateFlow<SleepTimerState> = _state.asStateFlow()
    
    private var timerJob: Job? = null
    private var controller: TTSController? = null
    private var scope: CoroutineScope? = null
    
    /**
     * Initialize with controller and scope
     */
    fun initialize(controller: TTSController, scope: CoroutineScope) {
        this.controller = controller
        this.scope = scope
    }
    
    /**
     * Start sleep timer
     * 
     * @param minutes Duration in minutes
     */
    fun start(minutes: Int) {
        if (minutes <= 0) {
            Log.warn { "$TAG: Invalid timer duration: $minutes" }
            return
        }
        
        Log.warn { "$TAG: start($minutes minutes)" }
        
        // Cancel existing timer
        cancel()
        
        val totalMs = minutes * 60 * 1000L
        val startTime = currentTimeToLong()
        val endTime = startTime + totalMs
        
        _state.value = SleepTimerState(
            isEnabled = true,
            remainingTimeMs = totalMs,
            totalTimeMs = totalMs
        )
        
        // Start countdown
        timerJob = scope?.launch {
            while (true) {
                val now = currentTimeToLong()
                val remaining = endTime - now
                
                if (remaining <= 0) {
                    // Timer expired - stop playback
                    Log.warn { "$TAG: Timer expired, stopping playback" }
                    controller?.dispatch(TTSCommand.Pause)
                    _state.value = SleepTimerState(isEnabled = false)
                    break
                }
                
                _state.value = _state.value.copy(remainingTimeMs = remaining)
                delay(UPDATE_INTERVAL_MS)
            }
        }
    }
    
    /**
     * Add time to existing timer
     * 
     * @param minutes Additional minutes to add
     */
    fun addTime(minutes: Int) {
        if (!_state.value.isEnabled) {
            // No active timer, start new one
            start(minutes)
            return
        }
        
        Log.warn { "$TAG: addTime($minutes minutes)" }
        
        val additionalMs = minutes * 60 * 1000L
        val currentRemaining = _state.value.remainingTimeMs
        val newRemaining = currentRemaining + additionalMs
        val newTotal = _state.value.totalTimeMs + additionalMs
        
        _state.value = _state.value.copy(
            remainingTimeMs = newRemaining,
            totalTimeMs = newTotal
        )
        
        // Restart timer with new end time
        timerJob?.cancel()
        val endTime = currentTimeToLong() + newRemaining
        
        timerJob = scope?.launch {
            while (true) {
                val now = currentTimeToLong()
                val remaining = endTime - now
                
                if (remaining <= 0) {
                    Log.warn { "$TAG: Timer expired, stopping playback" }
                    controller?.dispatch(TTSCommand.Pause)
                    _state.value = SleepTimerState(isEnabled = false)
                    break
                }
                
                _state.value = _state.value.copy(remainingTimeMs = remaining)
                delay(UPDATE_INTERVAL_MS)
            }
        }
    }
    
    /**
     * Cancel the sleep timer
     */
    fun cancel() {
        Log.warn { "$TAG: cancel()" }
        
        timerJob?.cancel()
        timerJob = null
        _state.value = SleepTimerState(isEnabled = false)
    }
    
    /**
     * Check if timer is active
     */
    fun isActive(): Boolean = _state.value.isEnabled
    
    /**
     * Get remaining time in milliseconds
     */
    fun getRemainingTimeMs(): Long = _state.value.remainingTimeMs
    
    /**
     * Clean up resources
     */
    fun cleanup() {
        Log.warn { "$TAG: cleanup()" }
        cancel()
        controller = null
        scope = null
    }
}
