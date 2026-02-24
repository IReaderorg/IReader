package ireader.data.sync

/**
 * Platform-specific wake lock management for sync operations.
 * Keeps WiFi and CPU active during sync to prevent connection drops.
 * 
 * Android: Acquires WiFi lock and partial wake lock
 * Desktop/iOS: No-op (not needed)
 */
expect class SyncWakeLock {
    /**
     * Acquire locks to keep WiFi and CPU active.
     * Call this before starting sync.
     */
    fun acquire()
    
    /**
     * Release locks to allow normal power management.
     * Call this after sync completes or fails.
     */
    fun release()
}
