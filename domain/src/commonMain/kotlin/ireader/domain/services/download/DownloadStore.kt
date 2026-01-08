package ireader.domain.services.download

/**
 * Interface for persisting the download queue across app restarts.
 * Based on Mihon's DownloadStore for queue persistence.
 */
interface DownloadStore {
    
    /**
     * Saves the current download queue to persistent storage.
     * This should be called whenever the queue changes.
     */
    suspend fun saveQueue(downloads: List<DownloadQueueItem>)
    
    /**
     * Restores the download queue from persistent storage.
     * Returns an empty list if no queue is saved or if data is corrupted.
     */
    suspend fun restoreQueue(): List<DownloadQueueItem>
    
    /**
     * Clears the persisted queue.
     */
    suspend fun clear()
    
    /**
     * Returns true if there is a persisted queue.
     */
    suspend fun hasPersistedQueue(): Boolean
}
