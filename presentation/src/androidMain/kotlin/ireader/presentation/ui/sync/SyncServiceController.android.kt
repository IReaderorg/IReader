package ireader.presentation.ui.sync

import android.content.Context
import android.net.wifi.WifiManager
import android.os.PowerManager

/**
 * Android implementation of SyncServiceController.
 * 
 * Controls the SyncForegroundService to manage sync operations in the background.
 * Also manages wake locks to keep WiFi active during sync.
 */
actual class SyncServiceController(private val context: Context) {
    
    private var wifiLock: WifiManager.WifiLock? = null
    private var wakeLock: PowerManager.WakeLock? = null
    
    actual fun startService(deviceName: String) {
        // Acquire wake locks to keep WiFi active
        acquireWakeLocks()
        
        SyncForegroundService.startSync(context, deviceName)
    }

    actual fun updateProgress(progress: Int, currentItem: String, currentIndex: Int, totalItems: Int) {
        SyncForegroundService.updateProgress(context, progress, currentItem, currentIndex, totalItems)
    }

    actual fun stopService() {
        SyncForegroundService.stopSync(context)
        
        // Release wake locks when sync completes
        releaseWakeLocks()
    }

    actual fun cancelSync() {
        SyncForegroundService.cancelSync(context)
        
        // Release wake locks when sync is cancelled
        releaseWakeLocks()
    }
    
    actual fun setCancelCallback(callback: () -> Unit) {
        SyncForegroundService.onCancelCallback = callback
    }
    
    actual fun showCompletionNotification(deviceName: String, syncedItems: Int, durationMs: Long) {
        SyncForegroundService.showCompletionNotification(context, deviceName, syncedItems, durationMs)
    }
    
    actual fun showErrorNotification(deviceName: String?, errorMessage: String, suggestion: String?) {
        SyncForegroundService.showErrorNotification(context, deviceName, errorMessage, suggestion)
        
        // Release wake locks on error
        releaseWakeLocks()
    }
    
    /**
     * Acquire wake locks to keep WiFi and CPU active during sync.
     * Prevents Android power saving from interrupting the connection.
     */
    private fun acquireWakeLocks() {
        try {
            // Acquire WiFi lock to prevent WiFi from going to sleep
            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            wifiLock = wifiManager.createWifiLock(
                WifiManager.WIFI_MODE_FULL_HIGH_PERF,
                "IReader:SyncWifiLock"
            ).apply {
                setReferenceCounted(false)
                acquire()
            }
            
            // Acquire partial wake lock to keep CPU active
            val powerManager = context.applicationContext.getSystemService(Context.POWER_SERVICE) as PowerManager
            wakeLock = powerManager.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                "IReader:SyncWakeLock"
            ).apply {
                setReferenceCounted(false)
                acquire(10 * 60 * 1000L) // 10 minutes max
            }
            
            println("[SyncServiceController] ✓ Wake locks acquired - WiFi will stay active during sync")
        } catch (e: Exception) {
            println("[SyncServiceController] ✗ Failed to acquire wake locks: ${e.message}")
            e.printStackTrace()
        }
    }
    
    /**
     * Release wake locks to allow normal power management.
     */
    private fun releaseWakeLocks() {
        try {
            wifiLock?.let {
                if (it.isHeld) {
                    it.release()
                }
            }
            wifiLock = null
            
            wakeLock?.let {
                if (it.isHeld) {
                    it.release()
                }
            }
            wakeLock = null
            
            println("[SyncServiceController] ✓ Wake locks released - normal power management resumed")
        } catch (e: Exception) {
            println("[SyncServiceController] ✗ Failed to release wake locks: ${e.message}")
            e.printStackTrace()
        }
    }
}
