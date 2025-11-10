package ireader.domain.services

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Manages reading timer for break reminders
 * Tracks continuous reading time and triggers reminders at configured intervals
 */
class ReadingTimerManager(
    private val scope: CoroutineScope,
    private val onIntervalReached: () -> Unit
) {
    private var startTime: Long = 0
    private var pausedTime: Long = 0
    private var accumulatedTime: Long = 0
    private var intervalMillis: Long = 0
    private var timerJob: Job? = null
    private var isRunning: Boolean = false
    private var isPaused: Boolean = false
    
    /**
     * Start the timer with the specified interval in minutes
     */
    fun startTimer(intervalMinutes: Int) {
        if (isRunning && !isPaused) {
            // Timer already running, don't restart
            return
        }
        
        intervalMillis = intervalMinutes * 60 * 1000L
        
        if (isPaused) {
            // Resume from paused state
            resumeTimer()
        } else {
            // Start fresh
            startTime = System.currentTimeMillis()
            accumulatedTime = 0
            isRunning = true
            isPaused = false
            
            startTimerJob()
        }
    }
    
    /**
     * Pause the timer (preserves accumulated time)
     */
    fun pauseTimer() {
        if (!isRunning || isPaused) return
        
        pausedTime = System.currentTimeMillis()
        accumulatedTime += (pausedTime - startTime)
        isPaused = true
        timerJob?.cancel()
    }
    
    /**
     * Resume the timer from paused state
     */
    fun resumeTimer() {
        if (!isRunning || !isPaused) return
        
        startTime = System.currentTimeMillis()
        isPaused = false
        startTimerJob()
    }
    
    /**
     * Reset the timer completely
     */
    fun resetTimer() {
        timerJob?.cancel()
        startTime = 0
        pausedTime = 0
        accumulatedTime = 0
        isRunning = false
        isPaused = false
    }
    
    /**
     * Stop the timer (same as reset)
     */
    fun stopTimer() {
        resetTimer()
    }
    
    /**
     * Get the current elapsed time in milliseconds
     */
    fun getElapsedTime(): Long {
        if (!isRunning) return 0
        
        return if (isPaused) {
            accumulatedTime
        } else {
            accumulatedTime + (System.currentTimeMillis() - startTime)
        }
    }
    
    /**
     * Get the remaining time until next break in milliseconds
     */
    fun getRemainingTime(): Long {
        if (!isRunning || intervalMillis == 0L) return 0
        
        val elapsed = getElapsedTime()
        val remaining = intervalMillis - elapsed
        return if (remaining > 0) remaining else 0
    }
    
    /**
     * Check if timer is currently running
     */
    fun isTimerRunning(): Boolean = isRunning && !isPaused
    
    /**
     * Check if timer is paused
     */
    fun isTimerPaused(): Boolean = isPaused
    
    /**
     * Internal method to start the timer coroutine
     */
    private fun startTimerJob() {
        timerJob?.cancel()
        timerJob = scope.launch {
            while (isActive && isRunning && !isPaused) {
                delay(1000) // Check every second
                
                val elapsed = accumulatedTime + (System.currentTimeMillis() - startTime)
                if (elapsed >= intervalMillis) {
                    onIntervalReached()
                    resetTimer()
                    break
                }
            }
        }
    }
}
