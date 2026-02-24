package ireader.data.sync

/**
 * Desktop implementation of SyncWakeLock.
 * Desktop doesn't need wake locks - no-op implementation.
 */
actual class SyncWakeLock {
    /**
     * No-op on desktop - power management not needed.
     */
    actual fun acquire() {
        // No-op on desktop
    }
    
    /**
     * No-op on desktop - power management not needed.
     */
    actual fun release() {
        // No-op on desktop
    }
}
