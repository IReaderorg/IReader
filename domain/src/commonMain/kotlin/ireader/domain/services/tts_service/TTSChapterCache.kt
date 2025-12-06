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
 * TTS Chapter Audio Cache - Caches chapter audio chunks for remote TTS engines
 * 
 * Features:
 * - Download and cache audio per chunk (supports text merging)
 * - Configurable cache duration (auto-cleanup after X days)
 * - Manual cache clearing
 * - Progress tracking for downloads
 * - Chunk-based storage for efficient playback integration
 * 
 * This is specifically for remote engines (Gradio) where downloading
 * the chapter audio upfront provides a better reading experience.
 */
class TTSChapterCache(
    private val fileSystem: FileSystem,
    private val cacheDir: Path
) {
    companion object {
        private const val TAG = "TTSChapterCache"
        private const val CACHE_INDEX_FILE = "chapter_cache_index.json"
        private const val CHUNK_INDEX_FILE = "chunk_cache_index.json"
        private const val MILLIS_PER_DAY = 24 * 60 * 60 * 1000L
    }
    
    /**
     * Cache entry metadata for whole chapter (legacy)
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
     * Cache entry for individual chunks
     */
    data class ChunkCacheEntry(
        val chapterId: Long,
        val chunkIndex: Int,
        val filename: String,
        val sizeBytes: Long,
        val createdAt: Long,
        val expiresAt: Long,
        val engineId: String,
        val paragraphIndices: List<Int> // Original paragraph indices in this chunk
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
    
    // ========== Chunk-Based Caching (for text merging support) ==========
    
    // Chunk cache index - maps "chapterId_chunkIndex" to chunk entry
    private val chunkCacheIndex = mutableMapOf<String, ChunkCacheEntry>()
    
    init {
        loadChunkCacheIndex()
    }
    
    /**
     * Generate cache key for a chunk
     */
    private fun chunkKey(chapterId: Long, chunkIndex: Int): String = "${chapterId}_$chunkIndex"
    
    /**
     * Check if a specific chunk is cached
     */
    fun isChunkCached(chapterId: Long, chunkIndex: Int): Boolean {
        val key = chunkKey(chapterId, chunkIndex)
        val entry = chunkCacheIndex[key]
        if (entry == null) {
            Log.warn { "$TAG: isChunkCached($chapterId, $chunkIndex) = false (no entry)" }
            return false
        }
        if (entry.isExpired()) {
            Log.warn { "$TAG: isChunkCached($chapterId, $chunkIndex) = false (expired)" }
            removeChunkEntry(chapterId, chunkIndex)
            return false
        }
        val filePath = cacheDir / entry.filename
        val exists = fileSystem.exists(filePath)
        Log.warn { "$TAG: isChunkCached($chapterId, $chunkIndex) = $exists (file: $filePath)" }
        return exists
    }
    
    /**
     * Get cached audio data for a chunk
     */
    fun getChunkAudio(chapterId: Long, chunkIndex: Int): ByteArray? {
        val key = chunkKey(chapterId, chunkIndex)
        Log.warn { "$TAG: getChunkAudio - key=$key, chunkCacheIndex.size=${chunkCacheIndex.size}" }
        val entry = chunkCacheIndex[key]
        if (entry == null) {
            Log.warn { "$TAG: getChunkAudio - No entry found for key=$key" }
            return null
        }
        if (entry.isExpired()) {
            Log.warn { "$TAG: getChunkAudio - Entry expired for key=$key" }
            removeChunkEntry(chapterId, chunkIndex)
            return null
        }
        val filePath = cacheDir / entry.filename
        Log.warn { "$TAG: getChunkAudio - filePath=$filePath, exists=${fileSystem.exists(filePath)}" }
        return try {
            if (fileSystem.exists(filePath)) {
                val data = fileSystem.read(filePath) { readByteArray() }
                Log.warn { "$TAG: getChunkAudio - Read ${data.size} bytes from cache" }
                data
            } else {
                Log.warn { "$TAG: getChunkAudio - File does not exist: $filePath" }
                null
            }
        } catch (e: Exception) {
            Log.error { "$TAG: Failed to read chunk audio: ${e.message}" }
            null
        }
    }
    
    /**
     * Cache audio for a specific chunk
     */
    fun cacheChunkAudio(
        chapterId: Long,
        chunkIndex: Int,
        audioData: ByteArray,
        engineId: String,
        cacheDays: Int,
        paragraphIndices: List<Int>
    ): Boolean {
        return try {
            val filename = "chunk_${chapterId}_${chunkIndex}_${currentTimeToLong()}.wav"
            val filePath = cacheDir / filename
            
            fileSystem.write(filePath) {
                write(audioData)
            }
            
            val entry = ChunkCacheEntry(
                chapterId = chapterId,
                chunkIndex = chunkIndex,
                filename = filename,
                sizeBytes = audioData.size.toLong(),
                createdAt = currentTimeToLong(),
                expiresAt = currentTimeToLong() + (cacheDays * MILLIS_PER_DAY),
                engineId = engineId,
                paragraphIndices = paragraphIndices
            )
            
            chunkCacheIndex[chunkKey(chapterId, chunkIndex)] = entry
            saveChunkCacheIndex()
            
            Log.warn { "$TAG: CACHED chunk $chunkIndex for chapter $chapterId (${audioData.size} bytes) at $filePath" }
            Log.warn { "$TAG: Total cached chunks for chapter $chapterId: ${getCachedChunkIndices(chapterId)}" }
            true
        } catch (e: Exception) {
            Log.error { "$TAG: Failed to cache chunk audio: ${e.message}" }
            false
        }
    }
    
    /**
     * Get all cached chunk indices for a chapter
     */
    fun getCachedChunkIndices(chapterId: Long): Set<Int> {
        return chunkCacheIndex.values
            .filter { it.chapterId == chapterId && !it.isExpired() }
            .map { it.chunkIndex }
            .toSet()
    }
    
    /**
     * Check if all chunks for a chapter are cached
     */
    fun areAllChunksCached(chapterId: Long, totalChunks: Int): Boolean {
        val cachedIndices = getCachedChunkIndices(chapterId)
        return (0 until totalChunks).all { it in cachedIndices }
    }
    
    /**
     * Remove a specific chunk from cache
     */
    fun removeChunkEntry(chapterId: Long, chunkIndex: Int) {
        val key = chunkKey(chapterId, chunkIndex)
        val entry = chunkCacheIndex.remove(key) ?: return
        try {
            val filePath = cacheDir / entry.filename
            if (fileSystem.exists(filePath)) {
                fileSystem.delete(filePath)
            }
            saveChunkCacheIndex()
        } catch (e: Exception) {
            Log.error { "$TAG: Failed to remove chunk entry: ${e.message}" }
        }
    }
    
    /**
     * Remove all chunks for a chapter
     */
    fun removeAllChunksForChapter(chapterId: Long) {
        val keysToRemove = chunkCacheIndex.keys.filter { it.startsWith("${chapterId}_") }
        keysToRemove.forEach { key ->
            val entry = chunkCacheIndex.remove(key)
            entry?.let {
                try {
                    val filePath = cacheDir / it.filename
                    if (fileSystem.exists(filePath)) {
                        fileSystem.delete(filePath)
                    }
                } catch (e: Exception) {
                    Log.error { "$TAG: Failed to remove chunk file: ${e.message}" }
                }
            }
        }
        saveChunkCacheIndex()
        Log.info { "$TAG: Removed ${keysToRemove.size} chunks for chapter $chapterId" }
    }
    
    /**
     * Clear all chunk cache
     */
    fun clearAllChunks() {
        chunkCacheIndex.values.forEach { entry ->
            try {
                val filePath = cacheDir / entry.filename
                if (fileSystem.exists(filePath)) {
                    fileSystem.delete(filePath)
                }
            } catch (e: Exception) {
                Log.error { "$TAG: Failed to delete chunk file: ${e.message}" }
            }
        }
        chunkCacheIndex.clear()
        saveChunkCacheIndex()
        Log.info { "$TAG: Cleared all chunk cache" }
    }
    
    /**
     * Get chunk cache statistics
     */
    fun getChunkCacheStats(): ChunkCacheStats {
        val totalSize = chunkCacheIndex.values.sumOf { it.sizeBytes }
        val chunkCount = chunkCacheIndex.size
        val chapterCount = chunkCacheIndex.values.map { it.chapterId }.distinct().size
        return ChunkCacheStats(
            chunkCount = chunkCount,
            chapterCount = chapterCount,
            totalSizeBytes = totalSize
        )
    }
    
    data class ChunkCacheStats(
        val chunkCount: Int,
        val chapterCount: Int,
        val totalSizeBytes: Long
    ) {
        val totalSizeMB: Float get() = totalSizeBytes / (1024f * 1024f)
    }
    
    // ========== Chunk Cache Persistence ==========
    
    private fun loadChunkCacheIndex() {
        try {
            val indexPath = cacheDir / CHUNK_INDEX_FILE
            Log.warn { "$TAG: loadChunkCacheIndex - indexPath=$indexPath, exists=${fileSystem.exists(indexPath)}" }
            if (!fileSystem.exists(indexPath)) {
                Log.warn { "$TAG: loadChunkCacheIndex - No index file found" }
                return
            }
            
            val content = fileSystem.read(indexPath) { readUtf8() }
            Log.warn { "$TAG: loadChunkCacheIndex - Read ${content.length} chars from index" }
            parseChunkIndexJson(content)
            
            // Clean up expired chunk entries
            cleanupExpiredChunks()
            
            Log.warn { "$TAG: loadChunkCacheIndex - Loaded ${chunkCacheIndex.size} chunk cache entries" }
            // Log all loaded entries
            chunkCacheIndex.forEach { (key, entry) ->
                Log.warn { "$TAG: loadChunkCacheIndex - Entry: $key -> ${entry.filename}" }
            }
        } catch (e: Exception) {
            Log.error { "$TAG: Failed to load chunk cache index: ${e.message}" }
        }
    }
    
    private fun saveChunkCacheIndex() {
        try {
            val indexPath = cacheDir / CHUNK_INDEX_FILE
            val json = buildChunkIndexJson()
            fileSystem.write(indexPath) { writeUtf8(json) }
        } catch (e: Exception) {
            Log.error { "$TAG: Failed to save chunk cache index: ${e.message}" }
        }
    }
    
    private fun parseChunkIndexJson(json: String) {
        try {
            val entriesMatch = Regex(""""chunks"\s*:\s*\[(.*?)\]""", RegexOption.DOT_MATCHES_ALL)
                .find(json) ?: return
            
            val entriesContent = entriesMatch.groupValues[1]
            // Match objects that may contain nested arrays
            val entryPattern = Regex("""\{[^{}]*"paragraphIndices"\s*:\s*\[[^\]]*\][^{}]*\}""")
            
            entryPattern.findAll(entriesContent).forEach { match ->
                parseChunkEntry(match.value)?.let { entry ->
                    chunkCacheIndex[chunkKey(entry.chapterId, entry.chunkIndex)] = entry
                }
            }
        } catch (e: Exception) {
            Log.error { "$TAG: Failed to parse chunk cache index: ${e.message}" }
        }
    }
    
    private fun parseChunkEntry(json: String): ChunkCacheEntry? {
        return try {
            fun extractLong(key: String): Long = 
                Regex(""""$key"\s*:\s*(\d+)""").find(json)?.groupValues?.get(1)?.toLongOrNull() ?: 0L
            fun extractInt(key: String): Int = 
                Regex(""""$key"\s*:\s*(\d+)""").find(json)?.groupValues?.get(1)?.toIntOrNull() ?: 0
            fun extractString(key: String): String = 
                Regex(""""$key"\s*:\s*"([^"]+)"""").find(json)?.groupValues?.get(1) ?: ""
            fun extractIntList(key: String): List<Int> {
                val match = Regex(""""$key"\s*:\s*\[([^\]]*)\]""").find(json)
                return match?.groupValues?.get(1)?.split(",")?.mapNotNull { it.trim().toIntOrNull() } ?: emptyList()
            }
            
            ChunkCacheEntry(
                chapterId = extractLong("chapterId"),
                chunkIndex = extractInt("chunkIndex"),
                filename = extractString("filename"),
                sizeBytes = extractLong("sizeBytes"),
                createdAt = extractLong("createdAt"),
                expiresAt = extractLong("expiresAt"),
                engineId = extractString("engineId"),
                paragraphIndices = extractIntList("paragraphIndices")
            )
        } catch (e: Exception) {
            null
        }
    }
    
    private fun buildChunkIndexJson(): String {
        val entries = chunkCacheIndex.values.joinToString(",\n") { entry ->
            val indices = entry.paragraphIndices.joinToString(",")
            """{"chapterId":${entry.chapterId},"chunkIndex":${entry.chunkIndex},"filename":"${entry.filename}","sizeBytes":${entry.sizeBytes},"createdAt":${entry.createdAt},"expiresAt":${entry.expiresAt},"engineId":"${entry.engineId}","paragraphIndices":[$indices]}"""
        }
        return """{"chunks":[$entries]}"""
    }
    
    private fun cleanupExpiredChunks() {
        val expiredKeys = chunkCacheIndex.filter { it.value.isExpired() }.keys.toList()
        expiredKeys.forEach { key ->
            val entry = chunkCacheIndex.remove(key)
            entry?.let {
                try {
                    val filePath = cacheDir / it.filename
                    if (fileSystem.exists(filePath)) {
                        fileSystem.delete(filePath)
                    }
                } catch (e: Exception) {
                    Log.error { "$TAG: Failed to delete expired chunk: ${e.message}" }
                }
            }
        }
        if (expiredKeys.isNotEmpty()) {
            saveChunkCacheIndex()
            Log.info { "$TAG: Cleaned up ${expiredKeys.size} expired chunk entries" }
        }
    }
}
