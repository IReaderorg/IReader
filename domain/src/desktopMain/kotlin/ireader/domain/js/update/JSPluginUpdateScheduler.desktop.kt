package ireader.domain.js.update

import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

/**
 * Desktop implementation of plugin update scheduler using ScheduledExecutorService.
 */
actual class JSPluginUpdateScheduler(
    private val updateChecker: JSPluginUpdateChecker,
    private val updateNotifier: JSPluginUpdateNotifier
) {
    
    private var scheduler: ScheduledExecutorService? = null
    private var scheduledFuture: ScheduledFuture<*>? = null
    
    actual fun schedulePeriodicCheck(intervalHours: Int) {
        cancelPeriodicCheck()
        
        scheduler = Executors.newSingleThreadScheduledExecutor()
        scheduledFuture = scheduler?.scheduleAtFixedRate(
            {
                try {
                    // Perform update check in a coroutine
                    kotlinx.coroutines.runBlocking {
                        val updates = updateChecker.checkForUpdates()
                        if (updates.isNotEmpty()) {
                            updateNotifier.showUpdateNotification(updates)
                        }
                    }
                } catch (_: Exception) {
                    // Silently ignore update check errors
                }
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
