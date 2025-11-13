package ireader.data.remote

import ireader.domain.models.remote.ReadingProgress
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Queue for failed sync operations to retry later
 */
class SyncQueue {
    private val queue = mutableListOf<ReadingProgress>()
    private val mutex = Mutex()
    
    suspend fun enqueue(progress: ReadingProgress) {
        mutex.withLock {
            queue.add(progress)
        }
    }
    
    suspend fun processQueue(processor: suspend (ReadingProgress) -> Result<Unit>): Int {
        val itemsToProcess = mutex.withLock {
            queue.toList().also { queue.clear() }
        }
        
        var successCount = 0
        val failedItems = mutableListOf<ReadingProgress>()
        
        itemsToProcess.forEach { item ->
            val result = processor(item)
            if (result.isSuccess) {
                successCount++
            } else {
                failedItems.add(item)
            }
        }
        
        // Re-add failed items
        mutex.withLock {
            queue.addAll(failedItems)
        }
        
        return successCount
    }
    
    suspend fun size(): Int = mutex.withLock { queue.size }
}
