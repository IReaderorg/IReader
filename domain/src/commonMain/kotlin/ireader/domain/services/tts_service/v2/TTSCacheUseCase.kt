package ireader.domain.services.tts_service.v2

import ireader.core.log.Log
import ireader.domain.services.tts_service.TTSChapterCache
import kotlinx.coroutines.flow.StateFlow

/**
 * TTS Cache Use Case - Manages chunk-based audio caching for offline playback
 * 
 * This use case provides a clean interface for:
 * - Checking if chunks are cached
 * - Retrieving cached audio data
 * - Caching new audio chunks
 * - Tracking download progress
 * 
 * Works with the existing TTSChapterCache for storage.
 */
class TTSCacheUseCase(
    private val chapterCache: TTSChapterCache
) {
    companion object {
        private const val TAG = "TTSCacheUseCase"
        private const val DEFAULT_CACHE_DAYS = 7
    }
    
    /**
     * Download progress state
     */
    val downloadProgress: StateFlow<TTSChapterCache.DownloadProgress?> = chapterCache.downloadProgress
    
    /**
     * Whether a download is in progress
     */
    val isDownloading: StateFlow<Boolean> = chapterCache.isDownloading
    
    /**
     * Check if a specific chunk is cached for a chapter
     */
    fun isChunkCached(chapterId: Long, chunkIndex: Int): Boolean {
        val cached = chapterCache.isChunkCached(chapterId, chunkIndex)
        Log.warn { "$TAG: isChunkCached(chapterId=$chapterId, chunkIndex=$chunkIndex) = $cached" }
        return cached
    }
    
    /**
     * Check if all chunks for a chapter are cached
     */
    fun areAllChunksCached(chapterId: Long, totalChunks: Int): Boolean {
        val allCached = chapterCache.areAllChunksCached(chapterId, totalChunks)
        Log.warn { "$TAG: areAllChunksCached(chapterId=$chapterId, totalChunks=$totalChunks) = $allCached" }
        return allCached
    }
    
    /**
     * Get cached audio data for a chunk
     * @return ByteArray of audio data or null if not cached
     */
    fun getChunkAudio(chapterId: Long, chunkIndex: Int): ByteArray? {
        val audio = chapterCache.getChunkAudio(chapterId, chunkIndex)
        Log.warn { "$TAG: getChunkAudio(chapterId=$chapterId, chunkIndex=$chunkIndex) = ${audio?.size ?: 0} bytes" }
        return audio
    }
    
    /**
     * Cache audio data for a chunk
     */
    fun cacheChunkAudio(
        chapterId: Long,
        chunkIndex: Int,
        audioData: ByteArray,
        engineId: String,
        paragraphIndices: List<Int>,
        cacheDays: Int = DEFAULT_CACHE_DAYS
    ): Boolean {
        Log.warn { "$TAG: cacheChunkAudio(chapterId=$chapterId, chunkIndex=$chunkIndex, size=${audioData.size})" }
        return chapterCache.cacheChunkAudio(
            chapterId = chapterId,
            chunkIndex = chunkIndex,
            audioData = audioData,
            engineId = engineId,
            cacheDays = cacheDays,
            paragraphIndices = paragraphIndices
        )
    }
    
    /**
     * Get all cached chunk indices for a chapter
     */
    fun getCachedChunkIndices(chapterId: Long): Set<Int> {
        return chapterCache.getCachedChunkIndices(chapterId)
    }
    
    /**
     * Start tracking download progress
     */
    fun startDownload(chapterId: Long, totalChunks: Int) {
        Log.warn { "$TAG: startDownload(chapterId=$chapterId, totalChunks=$totalChunks)" }
        chapterCache.startDownload(chapterId, totalChunks)
    }
    
    /**
     * Update download progress
     */
    fun updateProgress(chapterId: Long, currentChunk: Int, totalChunks: Int) {
        chapterCache.updateProgress(chapterId, currentChunk, totalChunks)
    }
    
    /**
     * Mark download as complete
     */
    fun completeDownload(chapterId: Long, totalChunks: Int) {
        Log.warn { "$TAG: completeDownload(chapterId=$chapterId, totalChunks=$totalChunks)" }
        chapterCache.completeDownload(chapterId, totalChunks)
    }
    
    /**
     * Mark download as failed
     */
    fun failDownload(chapterId: Long, error: String) {
        Log.warn { "$TAG: failDownload(chapterId=$chapterId, error=$error)" }
        chapterCache.failDownload(chapterId, error)
    }
    
    /**
     * Clear download progress
     */
    fun clearProgress() {
        chapterCache.clearProgress()
    }
    
    /**
     * Remove all cached chunks for a chapter
     */
    fun clearChapterCache(chapterId: Long) {
        Log.warn { "$TAG: clearChapterCache(chapterId=$chapterId)" }
        chapterCache.removeAllChunksForChapter(chapterId)
    }
    
    /**
     * Clear all cached audio
     */
    fun clearAllCache() {
        Log.warn { "$TAG: clearAllCache()" }
        chapterCache.clearAllChunks()
    }
    
    /**
     * Get cache statistics
     */
    fun getCacheStats(): TTSChapterCache.ChunkCacheStats {
        return chapterCache.getChunkCacheStats()
    }
}
