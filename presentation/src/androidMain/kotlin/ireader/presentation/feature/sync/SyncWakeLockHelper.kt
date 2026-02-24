package ireader.presentation.feature.sync

import android.content.Context
import android.net.wifi.WifiManager
import android.os.PowerManager

/**
 * Helper class to manage wake locks during WiFi sync operations.
 * Prevents Android from putting WiFi to sleep during data transfer.
 * 
 * Usage:
 * ```
 * val helper = SyncWakeLockHelper(context)
 * helper.acquire()  // Before starting sync
 * // ... perform sync ...
 * helper.release()  // After sync completes or fails
 * ```
 */
class SyncWakeLockHelper(private val context: Context) {
    
    private var wifiLock: WifiManager.WifiLock? = null
    private var wakeLock: PowerManager.WakeLock? = null
    
    /**
     * Acquire locks to keep WiFi and CPU active during sync.
     * Call this before starting sync operations.
     */
    fun acquire() {
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
            
            println("[SyncWakeLockHelper] ✓ Locks acquired - WiFi will stay active during sync")
        } catch (e: Exception) {
            println("[SyncWakeLockHelper] ✗ Failed to acquire locks: ${e.message}")
            e.printStackTrace()
        }
    }
    
    /**
     * Release locks to allow normal power management.
     * Call this after sync completes or fails.
     */
    fun release() {
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
            
            println("[SyncWakeLockHelper] ✓ Locks released - normal power management resumed")
        } catch (e: Exception) {
            println("[SyncWakeLockHelper] ✗ Failed to release locks: ${e.message}")
            e.printStackTrace()
        }
    }
    
    /**
     * Check if locks are currently held.
     */
    fun isHeld(): Boolean {
        return (wifiLock?.isHeld == true) || (wakeLock?.isHeld == true)
    }
}
