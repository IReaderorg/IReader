package ireader.data.sync

import android.content.Context
import android.net.wifi.WifiManager
import android.os.PowerManager
import ireader.core.log.Log

/**
 * Android implementation of SyncWakeLock.
 * Manages wake locks to keep WiFi active during sync operations.
 * Prevents Android from putting WiFi to sleep during data transfer.
 */
actual class SyncWakeLock(private val context: Context) {
    
    private var wifiLock: WifiManager.WifiLock? = null
    private var wakeLock: PowerManager.WakeLock? = null
    
    /**
     * Acquire locks to keep WiFi and CPU active.
     * Call this before starting sync.
     */
    actual fun acquire() {
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
            
            Log.debug { "[SyncWakeLock] Locks acquired - WiFi will stay active during sync" }
        } catch (e: Exception) {
            Log.error(e, "[SyncWakeLock] Failed to acquire locks: ${e.message}")
        }
    }
    
    /**
     * Release locks to allow normal power management.
     * Call this after sync completes or fails.
     */
    actual fun release() {
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
            
            Log.debug { "[SyncWakeLock] Locks released - normal power management resumed" }
        } catch (e: Exception) {
            Log.error(e, "[SyncWakeLock] Failed to release locks: ${e.message}")
        }
    }
}
