package ireader.data.remote

import ireader.domain.models.remote.ReadingProgress
import kotlinx.coroutines.CoroutineScope
import ireader.domain.utils.extensions.ioDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Debounces reading progress sync operations to batch updates
 * Prevents excessive network requests when user is rapidly scrolling
 * 
 * Requirements: 8.1, 10.1
 */
class DebouncedProgressSync(
    private val syncOperation: suspend (ReadingProgress) -> Result<Unit>,
    private val delayMs: Long = 2000
) {
    private val lock = Any()
    @kotlin.concurrent.Volatile
    private var pendingProgress: ReadingProgress? = null
    private var syncJob: Job? = null
    private val scope = CoroutineScope(kotlinx.coroutines.SupervisorJob() + ioDispatcher)
    
    /**
     * Schedules a sync operation with debouncing
     * Cancels any pending sync and schedules a new one
     * 
     * @param progress The reading progress to sync
     */
    fun scheduleSync(progress: ReadingProgress) {
        synchronized(lock) {
            pendingProgress = progress
            syncJob?.cancel()
            syncJob = scope.launch {
                delay(delayMs)
                val current = synchronized(lock) {
                    val p = pendingProgress
                    pendingProgress = null
                    p
                }
                if (current != null) {
                    syncOperation(current)
                }
            }
        }
    }
    
    /**
     * Immediately executes any pending sync operation
     */
    suspend fun flushPending() {
        val toFlush = synchronized(lock) {
            syncJob?.cancel()
            val p = pendingProgress
            pendingProgress = null
            p
        }
        if (toFlush != null) {
            syncOperation(toFlush)
        }
    }
    
    /**
     * Cancels any pending sync operation
     */
    fun cancel() {
        synchronized(lock) {
            pendingProgress = null
            syncJob?.cancel()
            syncJob = null
        }
    }
}
