package ireader.presentation.ui.settings.tracking

import ireader.core.log.Log
import java.util.Timer
import java.util.TimerTask
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Desktop implementation of TrackingSyncScheduler using Java Timer.
 * Note: This is a basic implementation - sync only runs while app is open.
 */
class DesktopTrackingSyncScheduler(
    private val onSync: () -> Unit = {}
) : TrackingSyncScheduler {
    
    private var timer: Timer? = null
    private val scheduled = AtomicBoolean(false)
    
    override fun schedule(intervalMinutes: Int, requireWifi: Boolean) {
        cancel() // Cancel any existing schedule
        
        try {
            timer = Timer("TrackingSyncTimer", true).apply {
                val intervalMs = intervalMinutes * 60 * 1000L
                scheduleAtFixedRate(object : TimerTask() {
                    override fun run() {
                        Log.info { "Desktop tracking sync triggered" }
                        try {
                            onSync()
                        } catch (e: Exception) {
                            Log.error(e, "Desktop tracking sync failed")
                        }
                    }
                }, intervalMs, intervalMs)
            }
            scheduled.set(true)
            Log.info { "Desktop tracking sync scheduled every $intervalMinutes minutes (WiFi requirement ignored on desktop)" }
        } catch (e: Exception) {
            Log.error(e, "Failed to schedule desktop tracking sync")
        }
    }
    
    override fun cancel() {
        try {
            timer?.cancel()
            timer = null
            scheduled.set(false)
            Log.info { "Desktop tracking sync cancelled" }
        } catch (e: Exception) {
            Log.error(e, "Failed to cancel desktop tracking sync")
        }
    }
    
    override fun isScheduled(): Boolean = scheduled.get()
}
