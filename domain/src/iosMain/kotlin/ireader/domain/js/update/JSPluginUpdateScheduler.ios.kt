package ireader.domain.js.update

/**
 * iOS implementation of JSPluginUpdateScheduler
 * 
 * TODO: Implement using BGTaskScheduler
 */
actual class JSPluginUpdateScheduler {
    private var scheduled = false
    
    actual fun schedulePeriodicCheck(intervalHours: Int) {
        // TODO: Schedule using BGTaskScheduler
        scheduled = true
    }
    
    actual fun cancelPeriodicCheck() {
        // TODO: Cancel scheduled tasks
        scheduled = false
    }
    
    actual fun isScheduled(): Boolean {
        return scheduled
    }
}
