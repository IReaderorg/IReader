package ireader.data.remote

import ireader.data.core.DatabaseHandler
import ireader.domain.models.remote.ReadingProgress
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString

/**
 * Queue for managing offline reading progress updates
 * Stores updates locally when network is unavailable and syncs when connection is restored
 */
class SyncQueue(
    private val handler: DatabaseHandler,
    private val json: Json = Json { ignoreUnknownKeys = true }
) {
    
    /**
     * Adds a reading progress update to the sync queue
     * 
     * @param progress The reading progress to queue for syncing
     */
    suspend fun enqueue(progress: ReadingProgress) {
        handler.await {
            sync_queueQueries.insert(
                book_id = progress.bookId,
                data_ = json.encodeToString(progress),
                timestamp = System.currentTimeMillis()
            )
        }
    }
    
    /**
     * Processes all queued items by attempting to sync them
     * 
     * @param syncOperation Function that performs the actual sync operation
     * @return Number of successfully synced items
     */
    suspend fun processQueue(
        syncOperation: suspend (ReadingProgress) -> Result<Unit>
    ): Int {
        val items = handler.awaitList {
            sync_queueQueries.selectAll()
        }
        
        var successCount = 0
        
        items.forEach { item ->
            try {
                val progress = json.decodeFromString<ReadingProgress>(item.data_)
                val result = syncOperation(progress)
                
                if (result.isSuccess) {
                    // Remove from queue on success
                    handler.await {
                        sync_queueQueries.delete(item.id)
                    }
                    successCount++
                } else {
                    // Increment retry count on failure
                    handler.await {
                        sync_queueQueries.incrementRetry(item.id)
                    }
                }
            } catch (e: Exception) {
                // Increment retry count on exception
                handler.await {
                    sync_queueQueries.incrementRetry(item.id)
                }
            }
        }
        
        return successCount
    }
    
    /**
     * Gets the number of items currently in the queue
     * 
     * @return Count of queued items
     */
    suspend fun getQueueSize(): Int {
        return handler.awaitList {
            sync_queueQueries.selectAll()
        }.size
    }
    
    /**
     * Clears all items from the queue
     * Use with caution - this will delete all pending sync operations
     */
    suspend fun clearQueue() {
        handler.await {
            sync_queueQueries.deleteAll()
        }
    }
}
