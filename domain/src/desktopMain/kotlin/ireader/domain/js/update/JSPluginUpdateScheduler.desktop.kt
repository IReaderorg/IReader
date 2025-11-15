package ireader.domain.js.update

import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

/**
 * Desktop implementation of plugin update scheduler using ScheduledExecutorService.
 */
actual class JSPluginUpdateScheduler {
    
    private var scheduler: ScheduledExecutorService? = null
    private var scheduledFuture: ScheduledFuture<*>? = null
    
    actual fun schedulePeriodicCheck(intervalHours: Int) {
        cancelPeriodicCheck()
        
        scheduler = Executors.newSingleThreadScheduledExecutor()
        scheduledFuture = scheduler?.scheduleAtFixedRate(
            {
                // TODO: Perform update check
                // This would require injecting the update checker
            },
            intervalHours.toLong(),
            intervalHours.toLong(),
            TimeUnit.HOURS
        )
    }
    
    actual fun cancelPeriodicCheck() {
        scheduledFuture?.cancel(false)
        scheduledFuture = null
        scheduler?.shutdown()
        scheduler = null
    }
    
    actual fun isScheduled(): Boolean {
        return scheduledFuture != null && !(scheduledFuture?.isCancelled ?: true)
    }
}
