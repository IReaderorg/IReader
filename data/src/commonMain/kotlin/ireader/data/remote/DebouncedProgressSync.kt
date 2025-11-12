package ireader.data.remote

import ireader.domain.models.remote.ReadingProgress
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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
    private var syncJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO)
    
    /**
     * Schedules a sync operation with debouncing
     * Cancels any pending sync and schedules a new one
     * 
     * @param progress The reading progress to sync
     */
    fun scheduleSync(progress: ReadingProgress) {
        syncJob?.cancel()
        syncJob = scope.launch {
            delay(delayMs)
            syncOperation(progress)
        }
    }
    
    /**
     * Immediately executes any pending sync operation
     */
    suspend fun flushPending() {
        syncJob?.join()
    }
    
    /**
     * Cancels any pending sync operation
     */
    fun cancel() {
        syncJob?.cancel()
    }
}
