package ireader.domain.js.update

/**
 * iOS implementation of JSPluginUpdateScheduler
 * 
 * TODO: Implement using BGTaskScheduler
 */
actual class JSPluginUpdateScheduler {
    actual fun scheduleUpdateCheck(intervalHours: Int) {
        // TODO: Schedule using BGTaskScheduler
    }
    
    actual fun cancelScheduledUpdates() {
        // TODO: Cancel scheduled tasks
    }
    
    actual fun checkForUpdatesNow() {
        // TODO: Trigger immediate check
    }
}
