package ireader.domain.usecases.backup

import ireader.domain.models.prefs.PreferenceValues
import kotlinx.coroutines.*
import java.util.concurrent.TimeUnit

/**
 * Desktop implementation of automatic backup scheduling
 * Uses a coroutine-based timer approach for desktop platforms
 */
class ScheduleAutomaticBackupImpl() : ScheduleAutomaticBackup {
    
    private var scheduledJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    
    override fun schedule(frequency: PreferenceValues.AutomaticBackup) {
        // Cancel any existing scheduled backup
        cancel()
        
        if (frequency == PreferenceValues.AutomaticBackup.Off) {
            return
        }
        
        val intervalMillis = when (frequency) {
            PreferenceValues.AutomaticBackup.Every6Hours -> TimeUnit.HOURS.toMillis(6)
            PreferenceValues.AutomaticBackup.Every12Hours -> TimeUnit.HOURS.toMillis(12)
            PreferenceValues.AutomaticBackup.Daily -> TimeUnit.DAYS.toMillis(1)
            PreferenceValues.AutomaticBackup.Every2Days -> TimeUnit.DAYS.toMillis(2)
            PreferenceValues.AutomaticBackup.Weekly -> TimeUnit.DAYS.toMillis(7)
            else -> return
        }
        
        scheduledJob = scope.launch {
            while (isActive) {
                delay(intervalMillis)
                try {
                    performBackup()
                } catch (e: Exception) {
                    println("Desktop automatic backup failed: ${e.message}")
                }
            }
        }
        
        println("Desktop automatic backup scheduled: $frequency (every ${intervalMillis}ms)")
    }
    
    override fun cancel() {
        scheduledJob?.cancel()
        scheduledJob = null
        println("Desktop automatic backup cancelled")
    }
    
    override fun isScheduled(): Boolean {
        return scheduledJob?.isActive == true
    }
    
    private suspend fun performBackup() {
        // This would be called by the scheduled job
        // The actual backup logic should be injected via dependency injection
        println("Performing scheduled desktop backup at ${System.currentTimeMillis()}")
        // Note: In a full implementation, this would call CreateBackup use case
    }
}
