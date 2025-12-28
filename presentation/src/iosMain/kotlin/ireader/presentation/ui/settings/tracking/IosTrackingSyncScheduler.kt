package ireader.presentation.ui.settings.tracking

import ireader.core.log.Log
import platform.Foundation.NSTimer
import kotlin.concurrent.AtomicReference

/**
 * iOS implementation of TrackingSyncScheduler using NSTimer.
 * Note: This is a basic implementation - sync only runs while app is in foreground.
 * For background sync, BGTaskScheduler would need to be configured in the app delegate.
 */
class IosTrackingSyncScheduler(
    private val onSync: () -> Unit = {}
) : TrackingSyncScheduler {
    
    private val timerRef = AtomicReference<NSTimer?>(null)
    private val scheduledRef = AtomicReference(false)
    
    override fun schedule(intervalMinutes: Int, requireWifi: Boolean) {
        cancel() // Cancel any existing schedule
        
        try {
            val intervalSeconds = intervalMinutes * 60.0
            val timer = NSTimer.scheduledTimerWithTimeInterval(
                interval = intervalSeconds,
                repeats = true
            ) { _ ->
                Log.info { "iOS tracking sync triggered" }
                try {
                    onSync()
                } catch (e: Exception) {
                    Log.error(e, "iOS tracking sync failed")
                }
            }
            timerRef.value = timer
            scheduledRef.value = true
            Log.info { "iOS tracking sync scheduled every $intervalMinutes minutes (WiFi requirement: $requireWifi - note: only runs in foreground)" }
        } catch (e: Exception) {
            Log.error(e, "Failed to schedule iOS tracking sync")
        }
    }
    
    override fun cancel() {
        try {
            timerRef.value?.invalidate()
            timerRef.value = null
            scheduledRef.value = false
            Log.info { "iOS tracking sync cancelled" }
        } catch (e: Exception) {
            Log.error(e, "Failed to cancel iOS tracking sync")
        }
    }
    
    override fun isScheduled(): Boolean = scheduledRef.value
}
