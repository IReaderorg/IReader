package ireader.domain.services

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import ireader.domain.utils.extensions.currentTimeToLong

/**
 * Manages reading time tracking and triggers rest reminders
 * Requirements: 17.1, 17.2, 17.3, 17.4, 17.5
 */
class ReadingTimerManager(
    private val scope: CoroutineScope,
    private val onIntervalReached: () -> Unit
) {
    private var timerJob: Job? = null
    private var startTime: Long = 0
    private var accumulatedTime: Long = 0
    private var intervalMinutes: Int = 30
    
    /**
     * Start the reading timer
     */
    fun startTimer(intervalMinutes: Int) {
        this.intervalMinutes = intervalMinutes
        
        if (timerJob?.isActive == true) {
            return // Already running
        }
        
        startTime = currentTimeToLong()
        launchTimerJob()
    }
    
    /**
     * Pause the reading timer
     */
    fun pauseTimer() {
        if (timerJob?.isActive == true) {
            val currentTime = currentTimeToLong()
            accumulatedTime += (currentTime - startTime)
            timerJob?.cancel()
            timerJob = null
        }
    }
    
    /**
     * Resume the reading timer
     */
    fun resumeTimer() {
        if (timerJob?.isActive != true && intervalMinutes > 0) {
            startTime = currentTimeToLong()
            launchTimerJob()
        }
    }
    
    /**
     * Stop and reset the reading timer
     */
    fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
        resetTimer()
    }
    
    /**
     * Reset accumulated time
     */
    fun resetTimer() {
        accumulatedTime = 0
        startTime = currentTimeToLong()
    }
    
    /**
     * Get total reading time in minutes
     */
    fun getTotalReadingTimeMinutes(): Int {
        if (timerJob?.isActive != true) {
            return (accumulatedTime / 60000).toInt()
        }
        val currentTime = currentTimeToLong()
        return ((currentTime - startTime + accumulatedTime) / 60000).toInt()
    }
    
    /**
     * Get remaining time until next break in milliseconds
     */
    fun getRemainingTime(): Long {
        if (timerJob?.isActive != true) {
            return 0L
        }
        val currentTime = currentTimeToLong()
        val elapsedMillis = currentTime - startTime + accumulatedTime
        val targetMillis = intervalMinutes * 60000L
        return (targetMillis - elapsedMillis).coerceAtLeast(0L)
    }
    
    /**
     * Check if the timer is currently running
     */
    fun isTimerRunning(): Boolean {
        return timerJob?.isActive == true
    }
    
    /**
     * Snooze the reminder for specified minutes
     */
    fun snoozeTimer(minutes: Int) {
        resetTimer()
        startTime = currentTimeToLong()
        // Adjust accumulated time to effectively snooze
        accumulatedTime = -(minutes * 60000L)
        // Resume the timer if it was running
        if (intervalMinutes > 0) {
            resumeTimer()
        }
    }
    
    /**
     * Internal method to launch the timer coroutine job
     * Extracted to eliminate code duplication between startTimer and resumeTimer
     */
    private fun launchTimerJob() {
        timerJob = scope.launch {
            while (isActive) {
                delay(1000) // Check every second
                
                val currentTime = currentTimeToLong()
                val elapsedMinutes = ((currentTime - startTime + accumulatedTime) / 60000).toInt()
                
                if (elapsedMinutes >= intervalMinutes) {
                    onIntervalReached()
                    // Reset timer after reminder
                    resetTimer()
                    startTime = currentTimeToLong()
                }
            }
        }
    }
}
