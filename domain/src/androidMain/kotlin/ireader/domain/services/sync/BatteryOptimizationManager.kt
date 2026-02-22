package ireader.domain.services.sync

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.os.PowerManager
import androidx.annotation.RequiresApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Manages battery optimization for sync operations on Android.
 * 
 * Responsibilities:
 * - Wake lock management to keep CPU running during sync
 * - Battery saver mode detection and handling
 * - Battery level monitoring and adaptive sync
 * - CPU usage monitoring and throttling
 * 
 * Following TDD methodology - tests written first, implementation follows.
 * 
 * Requirements:
 * - 10.3.1: Wake lock management (partial wake lock, proper cleanup)
 * - 10.3.2: Battery saver mode detection and handling
 * - 10.3.3: Adaptive sync based on battery level
 * - 10.3.4: Battery usage tracking and metrics
 * - 10.3.5: CPU usage monitoring and throttling
 * 
 * @param context Android context for accessing system services
 */
class BatteryOptimizationManager(private val context: Context) {

    private val powerManager: PowerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
    private val batteryManager: BatteryManager? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
    } else {
        null
    }

    // Wake lock for keeping CPU active during sync
    private var wakeLock: PowerManager.WakeLock? = null

    // Battery state flows
    private val _batteryLevel = MutableStateFlow(100)
    val batteryLevel: StateFlow<Int> = _batteryLevel.asStateFlow()

    private val _isBatterySaverActive = MutableStateFlow(false)
    val isBatterySaverActive: StateFlow<Boolean> = _isBatterySaverActive.asStateFlow()

    private val _shouldThrottleSync = MutableStateFlow(false)
    val shouldThrottleSync: StateFlow<Boolean> = _shouldThrottleSync.asStateFlow()

    companion object {
        private const val WAKE_LOCK_TAG = "IReader:SyncWakeLock"
        private const val WAKE_LOCK_TIMEOUT_MS = 10 * 60 * 1000L // 10 minutes
        
        // Battery thresholds
        private const val BATTERY_LOW_THRESHOLD = 20 // Reduce sync frequency below 20%
        private const val BATTERY_CRITICAL_THRESHOLD = 10 // Pause sync below 10%
    }

    // ========== 10.3.1: WAKE LOCK MANAGEMENT ==========

    /**
     * Acquire a partial wake lock to keep CPU running during sync.
     * Partial wake lock allows screen to turn off while keeping CPU active.
     * 
     * This is idempotent - calling multiple times won't create multiple wake locks.
     * Wake lock has a timeout to prevent battery drain if app crashes.
     */
    fun acquireWakeLock() {
        synchronized(this) {
            // If wake lock already held, don't acquire again
            if (wakeLock?.isHeld == true) {
                return
            }

            // Create and acquire partial wake lock
            wakeLock = powerManager.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                WAKE_LOCK_TAG
            ).apply {
                // Set timeout to prevent battery drain if app crashes
                acquire(WAKE_LOCK_TIMEOUT_MS)
            }
        }
    }

    /**
     * Release the wake lock after sync completes or is cancelled.
     * 
     * This is idempotent - calling multiple times is safe.
     */
    fun releaseWakeLock() {
        synchronized(this) {
            wakeLock?.let { lock ->
                if (lock.isHeld) {
                    lock.release()
                }
            }
            wakeLock = null
        }
    }

    /**
     * Check if wake lock is currently held.
     * 
     * @return true if wake lock is held, false otherwise
     */
    fun isWakeLockHeld(): Boolean {
        return wakeLock?.isHeld == true
    }

    // ========== 10.3.2: BATTERY SAVER MODE DETECTION ==========

    /**
     * Check if device is in battery saver mode.
     * 
     * @return true if battery saver is active, false otherwise
     */
    fun isBatterySaverMode(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            powerManager.isPowerSaveMode
        } else {
            false
        }
    }

    /**
     * Update battery saver state and notify observers.
     * Should be called periodically during sync.
     */
    fun updateBatterySaverState() {
        _isBatterySaverActive.value = isBatterySaverMode()
    }

    /**
     * Check if sync should be paused due to battery saver mode.
     * 
     * @return true if sync should be paused, false otherwise
     */
    fun shouldPauseSyncForBatterySaver(): Boolean {
        return isBatterySaverMode()
    }

    // ========== 10.3.3: BATTERY LEVEL MONITORING ==========

    /**
     * Get current battery level as a percentage (0-100).
     * 
     * @return battery level percentage, or 100 if unable to determine
     */
    fun getBatteryLevel(): Int {
        val batteryStatus: Intent? = context.registerReceiver(
            null,
            IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        )

        return batteryStatus?.let { intent ->
            val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            
            if (level >= 0 && scale > 0) {
                (level * 100 / scale)
            } else {
                100 // Default to full battery if unable to determine
            }
        } ?: 100
    }

    /**
     * Update battery level state and determine if sync should be throttled.
     * Should be called periodically during sync.
     */
    fun updateBatteryLevel() {
        val level = getBatteryLevel()
        _batteryLevel.value = level

        // Determine if sync should be throttled based on battery level
        _shouldThrottleSync.value = when {
            level < BATTERY_CRITICAL_THRESHOLD -> true // Pause sync
            level < BATTERY_LOW_THRESHOLD -> true // Reduce frequency
            else -> false
        }
    }

    /**
     * Check if sync should be paused due to critical battery level.
     * 
     * @return true if battery is below 10%, false otherwise
     */
    fun shouldPauseSyncForBattery(): Boolean {
        return getBatteryLevel() < BATTERY_CRITICAL_THRESHOLD
    }

    /**
     * Check if sync should be throttled due to low battery level.
     * 
     * @return true if battery is below 20%, false otherwise
     */
    fun shouldThrottleSyncForBattery(): Boolean {
        return getBatteryLevel() < BATTERY_LOW_THRESHOLD
    }

    /**
     * Check if device is charging.
     * 
     * @return true if device is charging, false otherwise
     */
    fun isCharging(): Boolean {
        val batteryStatus: Intent? = context.registerReceiver(
            null,
            IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        )

        return batteryStatus?.let { intent ->
            val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
            status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL
        } ?: false
    }

    // ========== 10.3.4: BATTERY USAGE TRACKING ==========

    /**
     * Get estimated battery usage for sync operation.
     * This is a rough estimate based on sync duration and data transferred.
     * 
     * @param durationMs sync duration in milliseconds
     * @param bytesTransferred number of bytes transferred
     * @return estimated battery usage in percentage points
     */
    fun estimateBatteryUsage(durationMs: Long, bytesTransferred: Long): Double {
        // Rough estimation:
        // - WiFi transfer: ~0.5% per minute of active transfer
        // - CPU processing: ~0.3% per minute
        // - Total: ~0.8% per minute
        
        val minutes = durationMs / 60000.0
        val baseUsage = minutes * 0.8
        
        // Adjust for data volume (more data = more processing)
        val dataMB = bytesTransferred / (1024.0 * 1024.0)
        val dataAdjustment = dataMB * 0.01 // 0.01% per MB
        
        return baseUsage + dataAdjustment
    }

    /**
     * Log battery usage metrics for monitoring.
     * 
     * @param startLevel battery level at start of sync
     * @param endLevel battery level at end of sync
     * @param durationMs sync duration in milliseconds
     * @param bytesTransferred number of bytes transferred
     */
    fun logBatteryUsage(
        startLevel: Int,
        endLevel: Int,
        durationMs: Long,
        bytesTransferred: Long
    ) {
        val actualUsage = startLevel - endLevel
        val estimatedUsage = estimateBatteryUsage(durationMs, bytesTransferred)
        
        // Log for debugging and optimization
        android.util.Log.d(
            "BatteryOptimization",
            "Sync battery usage: actual=$actualUsage%, estimated=${"%.2f".format(estimatedUsage)}%, " +
                    "duration=${durationMs}ms, data=${bytesTransferred / 1024}KB"
        )
    }

    // ========== 10.3.5: CPU USAGE MONITORING ==========

    /**
     * Get recommended delay between sync operations based on battery state.
     * 
     * @return delay in milliseconds
     */
    fun getRecommendedSyncDelay(): Long {
        return when {
            shouldPauseSyncForBattery() -> Long.MAX_VALUE // Effectively pause
            shouldThrottleSyncForBattery() -> 5000L // 5 second delay
            isBatterySaverMode() -> 3000L // 3 second delay
            isCharging() -> 100L // Minimal delay when charging
            else -> 1000L // Normal 1 second delay
        }
    }

    /**
     * Check if sync should yield CPU to other processes.
     * This helps keep CPU usage under 30%.
     * 
     * @return true if sync should yield, false otherwise
     */
    fun shouldYieldCpu(): Boolean {
        // Yield more frequently when battery is low or battery saver is active
        return shouldThrottleSyncForBattery() || isBatterySaverMode()
    }

    /**
     * Get recommended thread priority for sync operations.
     * Lower priority when battery is low to reduce CPU usage.
     * 
     * @return Android thread priority constant
     */
    fun getRecommendedThreadPriority(): Int {
        return when {
            shouldPauseSyncForBattery() -> android.os.Process.THREAD_PRIORITY_LOWEST
            shouldThrottleSyncForBattery() -> android.os.Process.THREAD_PRIORITY_BACKGROUND
            isBatterySaverMode() -> android.os.Process.THREAD_PRIORITY_BACKGROUND
            else -> android.os.Process.THREAD_PRIORITY_DEFAULT
        }
    }

    /**
     * Cleanup resources when manager is no longer needed.
     * Ensures wake lock is released.
     */
    fun cleanup() {
        releaseWakeLock()
    }
}
