package ireader.domain.services.tts_service

import ireader.core.log.Log
import ireader.domain.utils.extensions.currentTimeToLong
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath

/**
 * TTS Chapter Audio Cache - Caches entire chapter audio for remote TTS engines
 * 
 * Features:
 * - Download and cache whole chapter audio
 * - Configurable cache duration (auto-cleanup after X days)
 * - Manual cache clearing
 * - Progress tracking for downloads
 * 
 * This is specifically for remote engines (Gradio) where downloading
 * the entire chapter audio upfront provides a better reading experience.
 */
class TTSChapterCache(
    private val fileSystem: FileSystem,
    private val cacheDir: Path
) {
    companion object {
        private const val TAG = "TTSChapterCache"
        private const val CACHE_INDEX_FILE = "chapter_cache_index.json"
        private const val MILLIS_PER_DAY = 24 * 60 * 60 * 1000L
    }
    
    /**
     * Cache entry metadata
     */
    data class CacheEntry(
        val chapterId: Long,
        val bookId: Long,
        val filename: String,
        val sizeBytes: Long,
        val createdAt: Long,
        val expiresAt: Long,
        val engineId: String
    ) {
        fun isExpired(): Boolean = currentTimeToLong() > expiresAt
    }
    
    /**
     * Download progress state
     */
    data class DownloadProgress(
        val chapterId: Long,
        val currentParagraph: Int,
        val totalParagraphs: Int,
        val isComplete: Boolean = false,
        val error: String? = null
    ) {
        val progress: Float get() = if (totalParagraphs > 0) 
            currentParagraph.toFloat() / totalParagraphs else 0f
    }
    
    // Cache index - maps chapterId to cache entry
    private val cacheIndex = mutableMapOf<Long, CacheEntry>()
    
    // Download progress tracking
    private val _downloadProgress = MutableStateFlow<DownloadProgress?>(null)
    val downloadProgress: StateFlow<DownloadProgress?> = _downloadProgress.asStateFlow()
    
    // Currently downloading chapter
    private val _isDownloading = MutableStateFlow(false)
    val isDownloading: StateFlow<Boolean> = _isDownloading.asStateFlow()
    
    init {
        ensureCacheDir()
        loadCacheIndex()
    }
    
    /**
     * Check if chapter audio is cached and not expired
     */
    fun isCached(chapterId: Long): Boolean {
        val entry = cacheIndex[chapterId] ?: return false
        if (entry.isExpired()) {
            // Remove expired entry
            removeEntry(chapterId)
            return false
        }
        // Verify file exists
        val filePath = cacheDir / entry.filename
        return fileSystem.exists(filePath)
    }
    
    /**
     * Get cached audio file path
     */
    fun getCachedAudioPath(chapterId: Long): Path? {
        val entry = cacheIndex[chapterId] ?: return null
        if (entry.isExpired()) {
            removeEntry(chapterId)
            return null
        }
        val filePath = cacheDir / entry.filename
        return if (fileSystem.exists(filePath)) filePath else null
    }
    
    /**
     * Store chapter audio in cache
     * 
     * @param chapterId Chapter ID
     * @param bookId Book ID
     * @param audioData Combined audio data for the chapter
     * @param engineId TTS engine identifier
     * @param cacheDays Number of days to keep the cache
     */
    suspend fun cacheChapterAudio(
        chapterId: Long,
        bookId: Long,
        audioData: ByteArray,
        engineId: String,
        cacheDays: Int
    ): Boolean {
        return try {
            val filename = "chapter_${bookId}_${chapterId}_${currentTimeToLong()}.wav"
            val filePath = cacheDir / filename
            
            // Write audio file
            fileSystem.write(filePath) {
                write(audioData)
            }
            
            // Create cache entry
            val entry = CacheEntry(
                chapterId = chapterId,
                bookId = bookId,
                filename = filename,
                sizeBytes = audioData.size.toLong(),
                createdAt = currentTimeToLong(),
                expiresAt = currentTimeToLong() + (cacheDays * MILLIS_PER_DAY),
                engineId = engineId
            )
            
            cacheIndex[chapterId] = entry
            saveCacheIndex()
            
            Log.info { "$TAG: Cached chapter $chapterId (${audioData.size} bytes, expires in $cacheDays days)" }
            true
        } catch (e: Exception) {
            Log.error { "$TAG: Failed to cache chapter audio: ${e.message}" }
            false
        }
    }
    
    /**
     * Update download progress
     */
    fun updateProgress(chapterId: Long, current: Int, total: Int) {
        _downloadProgress.value = DownloadProgress(
            chapterId = chapterId,
            currentParagraph = current,
            totalParagraphs = total
        )
    }
    
    /**
     * Mark download as complete
     */
    fun completeDownload(chapterId: Long, total: Int) {
        _downloadProgress.value = DownloadProgress(
            chapterId = chapterId,
            currentParagraph = total,
            totalParagraphs = total,
            isComplete = true
        )
        _isDownloading.value = false
    }
    
    /**
     * Mark download as failed
     */
    fun failDownload(chapterId: Long, error: String) {
        _downloadProgress.value = _downloadProgress.value?.copy(error = error)
        _isDownloading.value = false
    }
    
    /**
     * Start download tracking
     */
    fun startDownload(chapterId: Long, totalParagraphs: Int) {
        _isDownloading.value = true
        _downloadProgress.value = DownloadProgress(
            chapterId = chapterId,
            currentParagraph = 0,
            totalParagraphs = totalParagraphs
        )
    }
    
    /**
     * Clear download progress
     */
    fun clearProgress() {
        _downloadProgress.value = null
        _isDownloading.value = false
    }
    
    /**
     * Remove a specific chapter from cache
     */
    fun removeEntry(chapterId: Long) {
        val entry = cacheIndex.remove(chapterId) ?: return
        try {
            val filePath = cacheDir / entry.filename
            if (fileSystem.exists(filePath)) {
                fileSystem.delete(filePath)
            }
            saveCacheIndex()
            Log.info { "$TAG: Removed cached chapter $chapterId" }
        } catch (e: Exception) {
            Log.error { "$TAG: Failed to remove cache entry: ${e.message}" }
        }
    }
    
    /**
     * Clear all cached chapter audio
     */
    fun clearAll() {
        try {
            cacheIndex.values.forEach { entry ->
                val filePath = cacheDir / entry.filename
                if (fileSystem.exists(filePath)) {
                    fileSystem.delete(filePath)
                }
            }
            cacheIndex.clear()
            saveCacheIndex()
            Log.info { "$TAG: Cleared all cached chapter audio" }
        } catch (e: Exception) {
            Log.error { "$TAG: Failed to clear cache: ${e.message}" }
        }
    }
    
    /**
     * Clean up expired entries
     */
    fun cleanupExpired() {
        val expiredIds = cacheIndex.filter { it.value.isExpired() }.keys.toList()
        expiredIds.forEach { removeEntry(it) }
        if (expiredIds.isNotEmpty()) {
            Log.info { "$TAG: Cleaned up ${expiredIds.size} expired cache entries" }
        }
    }
    
    /**
     * Get cache statistics
     */
    fun getCacheStats(): CacheStats {
        val totalSize = cacheIndex.values.sumOf { it.sizeBytes }
        val entryCount = cacheIndex.size
        val expiredCount = cacheIndex.values.count { it.isExpired() }
        return CacheStats(
            entryCount = entryCount,
            totalSizeBytes = totalSize,
            expiredCount = expiredCount
        )
    }
    
    data class CacheStats(
        val entryCount: Int,
        val totalSizeBytes: Long,
        val expiredCount: Int
    ) {
        val totalSizeMB: Float get() = totalSizeBytes / (1024f * 1024f)
    }
    
    // ========== Private Methods ==========
    
    private fun ensureCacheDir() {
        try {
            if (!fileSystem.exists(cacheDir)) {
                fileSystem.createDirectories(cacheDir)
            }
        } catch (e: Exception) {
            Log.error { "$TAG: Failed to create cache directory: ${e.message}" }
        }
    }
    
    private fun loadCacheIndex() {
        try {
            val indexPath = cacheDir / CACHE_INDEX_FILE
            if (!fileSystem.exists(indexPath)) return
            
            val content = fileSystem.read(indexPath) {
                readUtf8()
            }
            
            // Simple JSON parsing (avoiding external dependencies)
            parseIndexJson(content)
            
            // Clean up expired entries on load
            cleanupExpired()
            
            Log.info { "$TAG: Loaded ${cacheIndex.size} cache entries" }
        } catch (e: Exception) {
            Log.error { "$TAG: Failed to load cache index: ${e.message}" }
        }
    }
    
    private fun saveCacheIndex() {
        try {
            val indexPath = cacheDir / CACHE_INDEX_FILE
            val json = buildIndexJson()
            
            fileSystem.write(indexPath) {
                writeUtf8(json)
            }
        } catch (e: Exception) {
            Log.error { "$TAG: Failed to save cache index: ${e.message}" }
        }
    }
    
    private fun parseIndexJson(json: String) {
        // Simple JSON parsing for cache entries
        // Format: {"entries":[{...},{...}]}
        try {
            val entriesMatch = Regex(""""entries"\s*:\s*\[(.*?)\]""", RegexOption.DOT_MATCHES_ALL)
                .find(json) ?: return
            
            val entriesContent = entriesMatch.groupValues[1]
            val entryPattern = Regex("""\{[^}]+\}""")
            
            entryPattern.findAll(entriesContent).forEach { match ->
                val entryJson = match.value
                parseEntry(entryJson)?.let { entry ->
                    cacheIndex[entry.chapterId] = entry
                }
            }
        } catch (e: Exception) {
            Log.error { "$TAG: Failed to parse cache index: ${e.message}" }
        }
    }
    
    private fun parseEntry(json: String): CacheEntry? {
        return try {
            fun extractLong(key: String): Long = 
                Regex(""""$key"\s*:\s*(\d+)""").find(json)?.groupValues?.get(1)?.toLongOrNull() ?: 0L
            fun extractString(key: String): String = 
                Regex(""""$key"\s*:\s*"([^"]+)"""").find(json)?.groupValues?.get(1) ?: ""
            
            CacheEntry(
                chapterId = extractLong("chapterId"),
                bookId = extractLong("bookId"),
                filename = extractString("filename"),
                sizeBytes = extractLong("sizeBytes"),
                createdAt = extractLong("createdAt"),
                expiresAt = extractLong("expiresAt"),
                engineId = extractString("engineId")
            )
        } catch (e: Exception) {
            null
        }
    }
    
    private fun buildIndexJson(): String {
        val entries = cacheIndex.values.joinToString(",\n") { entry ->
            """{"chapterId":${entry.chapterId},"bookId":${entry.bookId},"filename":"${entry.filename}","sizeBytes":${entry.sizeBytes},"createdAt":${entry.createdAt},"expiresAt":${entry.expiresAt},"engineId":"${entry.engineId}"}"""
        }
        return """{"entries":[$entries]}"""
    }
}
