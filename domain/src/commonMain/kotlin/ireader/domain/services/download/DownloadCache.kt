package ireader.domain.services.download

/**
 * Interface for caching downloaded chapter status.
 * Based on Mihon's DownloadCache for fast "is downloaded" checks.
 */
interface DownloadCache {
    
    /**
     * Returns true if the chapter is downloaded.
     * This should be a fast operation using in-memory cache.
     */
    fun isChapterDownloaded(bookId: Long, chapterId: Long): Boolean
    
    /**
     * Returns all downloaded chapter IDs for a book.
     */
    fun getDownloadedChapterIds(bookId: Long): Set<Long>
    
    /**
     * Adds a chapter to the downloaded cache.
     * Called when a download completes.
     */
    fun addDownloadedChapter(bookId: Long, chapterId: Long)
    
    /**
     * Removes a chapter from the downloaded cache.
     * Called when a downloaded chapter is deleted.
     */
    fun removeDownloadedChapter(bookId: Long, chapterId: Long)
    
    /**
     * Invalidates the entire cache.
     * The cache will be rebuilt on next access.
     */
    fun invalidate()
    
    /**
     * Invalidates the cache for a specific book.
     */
    fun invalidateBook(bookId: Long)
    
    /**
     * Refreshes the cache from the filesystem.
     * This is an expensive operation and should be called sparingly.
     */
    suspend fun refresh()
    
    /**
     * Returns the number of downloaded chapters in the cache.
     */
    fun getDownloadedCount(): Int
    
    /**
     * Returns the number of downloaded chapters for a specific book.
     */
    fun getDownloadedCount(bookId: Long): Int
    
    /**
     * Returns true if the cache has been initialized.
     */
    fun isInitialized(): Boolean
    
    companion object {
        /**
         * Default TTL for cache refresh (60 minutes).
         */
        const val DEFAULT_TTL_MS = 60L * 60 * 1000
    }
}
