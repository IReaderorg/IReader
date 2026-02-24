package ireader.data.sync

/**
 * iOS implementation of SyncWakeLock.
 * iOS doesn't need explicit wake locks - no-op implementation.
 * iOS manages power automatically and keeps network active during active operations.
 */
actual class SyncWakeLock {
    /**
     * No-op on iOS - power management handled by system.
     */
    actual fun acquire() {
        // No-op on iOS
    }
    
    /**
     * No-op on iOS - power management handled by system.
     */
    actual fun release() {
        // No-op on iOS
    }
}
