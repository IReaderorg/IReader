package ireader.domain.services.tts_service.v2

import ireader.core.log.Log
import ireader.domain.utils.extensions.currentTimeToLong
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okio.FileSystem
import okio.Path

/**
 * TTS Audio Cache - Size-limited cache for Gradio TTS audio files
 * 
 * Features:
 * - Size-based limit (removes oldest files when limit reached)
 * - Hash-based file naming for quick lookup
 * - Automatic cleanup on cache operations
 * - Thread-safe operations
 * 
 * File naming: {textHash}_{engineId}.wav
 * This allows the engine to check cache before making web requests.
 */
class TTSAudioCache(
    private val fileSystem: FileSystem,
    private val cacheDir: Path,
    private val maxCacheSizeMB: Int = 500 // Default 500MB limit
) {
    companion object {
        private const val TAG = "TTSAudioCache"
        private const val INDEX_FILE = "tts_audio_cache.json"
        private const val BYTES_PER_MB = 1024L * 1024L
    }
    
    /**
     * Cache entry metadata
     */
    data class CacheEntry(
        val key: String,           // Hash of text + engineId
        val filename: String,      // File name in cache dir
        val sizeBytes: Long,       // File size
        val lastAccessedAt: Long,  // For LRU eviction
        val createdAt: Long,
        val engineId: String,
        val textPreview: String    // First 50 chars for debugging
    )
    
    // In-memory index for fast lookup
    private val cacheIndex = mutableMapOf<String, CacheEntry>()
    private val mutex = Mutex()
    private var totalSizeBytes: Long = 0
    
    init {
        ensureCacheDir()
        loadIndex()
    }
    
    /**
     * Generate cache key from text and engine ID
     */
    fun generateKey(text: String, engineId: String): String {
        // Simple hash: combine text hash with engine ID
        val textHash = text.hashCode().toUInt().toString(16)
        val engineHash = engineId.hashCode().toUInt().toString(16)
        return "${textHash}_${engineHash}"
    }
    
    /**
     * Check if audio for text is cached
     */
    suspend fun isCached(text: String, engineId: String): Boolean {
        val key = generateKey(text, engineId)
        return mutex.withLock {
            val entry = cacheIndex[key]
            if (entry != null) {
                val filePath = cacheDir / entry.filename
                if (fileSystem.exists(filePath)) {
                    // Update last accessed time
                    cacheIndex[key] = entry.copy(lastAccessedAt = currentTimeToLong())
                    true
                } else {
                    // File missing, remove entry
                    cacheIndex.remove(key)
                    totalSizeBytes -= entry.sizeBytes
                    false
                }
            } else {
                false
            }
        }
    }

    
    /**
     * Get cached audio data
     * Returns null if not cached or file missing
     */
    suspend fun get(text: String, engineId: String): ByteArray? {
        val key = generateKey(text, engineId)
        return mutex.withLock {
            val entry = cacheIndex[key] ?: return@withLock null
            val filePath = cacheDir / entry.filename
            
            try {
                if (fileSystem.exists(filePath)) {
                    // Update last accessed time
                    cacheIndex[key] = entry.copy(lastAccessedAt = currentTimeToLong())
                    saveIndex()
                    
                    val data = fileSystem.read(filePath) { readByteArray() }
                    Log.warn { "$TAG: Cache HIT for key=$key (${data.size} bytes)" }
                    data
                } else {
                    // File missing, remove entry
                    cacheIndex.remove(key)
                    totalSizeBytes -= entry.sizeBytes
                    saveIndex()
                    Log.warn { "$TAG: Cache MISS (file missing) for key=$key" }
                    null
                }
            } catch (e: Exception) {
                Log.error { "$TAG: Failed to read cached audio: ${e.message}" }
                null
            }
        }
    }
    
    /**
     * Store audio data in cache
     * Automatically evicts old entries if size limit exceeded
     */
    suspend fun put(text: String, engineId: String, audioData: ByteArray): Boolean {
        val key = generateKey(text, engineId)
        val filename = "${key}.wav"
        val filePath = cacheDir / filename
        
        return mutex.withLock {
            try {
                // Check if we need to evict old entries
                val newSize = audioData.size.toLong()
                val maxBytes = maxCacheSizeMB * BYTES_PER_MB
                
                // Evict oldest entries until we have space
                while (totalSizeBytes + newSize > maxBytes && cacheIndex.isNotEmpty()) {
                    evictOldest()
                }
                
                // Write file
                fileSystem.write(filePath) {
                    write(audioData)
                }
                
                // Create entry
                val entry = CacheEntry(
                    key = key,
                    filename = filename,
                    sizeBytes = newSize,
                    lastAccessedAt = currentTimeToLong(),
                    createdAt = currentTimeToLong(),
                    engineId = engineId,
                    textPreview = text.take(50)
                )
                
                // Remove old entry if exists
                cacheIndex[key]?.let { old ->
                    totalSizeBytes -= old.sizeBytes
                }
                
                cacheIndex[key] = entry
                totalSizeBytes += newSize
                saveIndex()
                
                Log.warn { "$TAG: Cached audio for key=$key (${audioData.size} bytes, total=${totalSizeBytes / BYTES_PER_MB}MB)" }
                true
            } catch (e: Exception) {
                Log.error { "$TAG: Failed to cache audio: ${e.message}" }
                false
            }
        }
    }
    
    /**
     * Remove specific entry from cache
     */
    suspend fun remove(text: String, engineId: String) {
        val key = generateKey(text, engineId)
        mutex.withLock {
            val entry = cacheIndex.remove(key) ?: return@withLock
            totalSizeBytes -= entry.sizeBytes
            
            try {
                val filePath = cacheDir / entry.filename
                if (fileSystem.exists(filePath)) {
                    fileSystem.delete(filePath)
                }
                saveIndex()
            } catch (e: Exception) {
                Log.error { "$TAG: Failed to remove cached file: ${e.message}" }
            }
        }
    }
    
    /**
     * Clear all cached audio
     */
    suspend fun clearAll() {
        mutex.withLock {
            cacheIndex.values.forEach { entry ->
                try {
                    val filePath = cacheDir / entry.filename
                    if (fileSystem.exists(filePath)) {
                        fileSystem.delete(filePath)
                    }
                } catch (e: Exception) {
                    Log.error { "$TAG: Failed to delete file: ${e.message}" }
                }
            }
            cacheIndex.clear()
            totalSizeBytes = 0
            saveIndex()
            Log.warn { "$TAG: Cleared all cached audio" }
        }
    }
    
    /**
     * Get cache statistics
     */
    fun getStats(): CacheStats {
        return CacheStats(
            entryCount = cacheIndex.size,
            totalSizeBytes = totalSizeBytes,
            maxSizeBytes = maxCacheSizeMB * BYTES_PER_MB
        )
    }
    
    data class CacheStats(
        val entryCount: Int,
        val totalSizeBytes: Long,
        val maxSizeBytes: Long
    ) {
        val totalSizeMB: Float get() = totalSizeBytes / BYTES_PER_MB.toFloat()
        val maxSizeMB: Float get() = maxSizeBytes / BYTES_PER_MB.toFloat()
        val usagePercent: Float get() = if (maxSizeBytes > 0) 
            (totalSizeBytes.toFloat() / maxSizeBytes) * 100 else 0f
    }

    
    // ========== Private Methods ==========
    
    private fun ensureCacheDir() {
        try {
            if (!fileSystem.exists(cacheDir)) {
                fileSystem.createDirectories(cacheDir)
                Log.warn { "$TAG: Created cache directory: $cacheDir" }
            }
        } catch (e: Exception) {
            Log.error { "$TAG: Failed to create cache directory: ${e.message}" }
        }
    }
    
    /**
     * Evict the oldest (least recently accessed) entry
     */
    private fun evictOldest() {
        val oldest = cacheIndex.values.minByOrNull { it.lastAccessedAt } ?: return
        
        cacheIndex.remove(oldest.key)
        totalSizeBytes -= oldest.sizeBytes
        
        try {
            val filePath = cacheDir / oldest.filename
            if (fileSystem.exists(filePath)) {
                fileSystem.delete(filePath)
            }
            Log.warn { "$TAG: Evicted oldest entry: ${oldest.key} (${oldest.sizeBytes} bytes)" }
        } catch (e: Exception) {
            Log.error { "$TAG: Failed to delete evicted file: ${e.message}" }
        }
    }
    
    private fun loadIndex() {
        try {
            val indexPath = cacheDir / INDEX_FILE
            if (!fileSystem.exists(indexPath)) {
                Log.warn { "$TAG: No index file found, starting fresh" }
                return
            }
            
            val content = fileSystem.read(indexPath) { readUtf8() }
            parseIndexJson(content)
            
            // Recalculate total size
            totalSizeBytes = cacheIndex.values.sumOf { it.sizeBytes }
            
            Log.warn { "$TAG: Loaded ${cacheIndex.size} entries (${totalSizeBytes / BYTES_PER_MB}MB)" }
        } catch (e: Exception) {
            Log.error { "$TAG: Failed to load index: ${e.message}" }
        }
    }
    
    private fun saveIndex() {
        try {
            val indexPath = cacheDir / INDEX_FILE
            val json = buildIndexJson()
            fileSystem.write(indexPath) { writeUtf8(json) }
        } catch (e: Exception) {
            Log.error { "$TAG: Failed to save index: ${e.message}" }
        }
    }
    
    private fun parseIndexJson(json: String) {
        try {
            val entriesMatch = Regex(""""entries"\s*:\s*\[(.*?)\]""", RegexOption.DOT_MATCHES_ALL)
                .find(json) ?: return
            
            val entriesContent = entriesMatch.groupValues[1]
            val entryPattern = Regex("""\{[^}]+\}""")
            
            entryPattern.findAll(entriesContent).forEach { match ->
                parseEntry(match.value)?.let { entry ->
                    // Verify file exists
                    val filePath = cacheDir / entry.filename
                    if (fileSystem.exists(filePath)) {
                        cacheIndex[entry.key] = entry
                    }
                }
            }
        } catch (e: Exception) {
            Log.error { "$TAG: Failed to parse index: ${e.message}" }
        }
    }
    
    private fun parseEntry(json: String): CacheEntry? {
        return try {
            fun extractLong(key: String): Long = 
                Regex(""""$key"\s*:\s*(\d+)""").find(json)?.groupValues?.get(1)?.toLongOrNull() ?: 0L
            fun extractString(key: String): String = 
                Regex(""""$key"\s*:\s*"([^"]*?)"""").find(json)?.groupValues?.get(1) ?: ""
            
            CacheEntry(
                key = extractString("key"),
                filename = extractString("filename"),
                sizeBytes = extractLong("sizeBytes"),
                lastAccessedAt = extractLong("lastAccessedAt"),
                createdAt = extractLong("createdAt"),
                engineId = extractString("engineId"),
                textPreview = extractString("textPreview")
            )
        } catch (e: Exception) {
            null
        }
    }
    
    private fun buildIndexJson(): String {
        val entries = cacheIndex.values.joinToString(",") { entry ->
            // Escape special characters in textPreview
            val escapedPreview = entry.textPreview
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
            """{"key":"${entry.key}","filename":"${entry.filename}","sizeBytes":${entry.sizeBytes},"lastAccessedAt":${entry.lastAccessedAt},"createdAt":${entry.createdAt},"engineId":"${entry.engineId}","textPreview":"$escapedPreview"}"""
        }
        return """{"entries":[$entries]}"""
    }
}
