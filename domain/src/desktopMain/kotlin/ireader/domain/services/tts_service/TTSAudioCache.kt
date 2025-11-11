package ireader.domain.services.tts_service

import ireader.core.log.Log
import ireader.domain.services.tts_service.piper.AudioData
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.File
import java.security.MessageDigest

/**
 * Audio cache for TTS engines
 * Caches generated audio to avoid re-synthesizing the same text
 * 
 * Features:
 * - LRU cache with configurable size limit
 * - Disk-based persistence
 * - Thread-safe operations
 * - Automatic cleanup of old entries
 */
class TTSAudioCache(
    private val cacheDir: File,
    private val maxCacheSizeMB: Long = 500 // 500 MB default
) {
    private val mutex = Mutex()
    private val cacheIndex = mutableMapOf<String, CacheEntry>()
    private var currentCacheSizeBytes: Long = 0
    
    init {
        cacheDir.mkdirs()
        loadCacheIndex()
    }
    
    /**
     * Get cached audio for text
     * Returns null if not in cache
     */
    suspend fun get(text: String, voice: String, speed: Float): AudioData? = mutex.withLock {
        val key = generateKey(text, voice, speed)
        val entry = cacheIndex[key] ?: return null
        
        val file = File(cacheDir, entry.filename)
        if (!file.exists()) {
            // File was deleted, remove from index
            cacheIndex.remove(key)
            return null
        }
        
        // Update access time (LRU)
        entry.lastAccessTime = System.currentTimeMillis()
        
        try {
            // Read audio data from file
            val audioBytes = file.readBytes()
            
            Log.debug { "Cache hit for text: ${text.take(50)}..." }
            
            // Reconstruct AudioData
            return AudioData(
                samples = audioBytes,
                sampleRate = entry.sampleRate,
                channels = entry.channels,
                format = entry.format
            )
        } catch (e: Exception) {
            Log.error { "Failed to read cached audio: ${e.message}" }
            cacheIndex.remove(key)
            file.delete()
            return null
        }
    }
    
    /**
     * Store audio in cache
     */
    suspend fun put(text: String, voice: String, speed: Float, audioData: AudioData) = mutex.withLock {
        val key = generateKey(text, voice, speed)
        val filename = "$key.wav"
        val file = File(cacheDir, filename)
        
        try {
            // Write audio to file
            file.writeBytes(audioData.samples)
            
            val entry = CacheEntry(
                key = key,
                filename = filename,
                sizeBytes = audioData.samples.size.toLong(),
                sampleRate = audioData.sampleRate,
                channels = audioData.channels,
                format = audioData.format,
                createdTime = System.currentTimeMillis(),
                lastAccessTime = System.currentTimeMillis()
            )
            
            cacheIndex[key] = entry
            currentCacheSizeBytes += entry.sizeBytes
            
            Log.debug { "Cached audio for text: ${text.take(50)}... (${entry.sizeBytes} bytes)" }
            
            // Cleanup if cache is too large
            if (currentCacheSizeBytes > maxCacheSizeMB * 1024 * 1024) {
                cleanupOldEntries()
            }
        } catch (e: Exception) {
            Log.error { "Failed to cache audio: ${e.message}" }
        }
    }
    
    /**
     * Pre-generate and cache audio for multiple paragraphs
     * This enables parallel generation for faster playback
     */
    suspend fun preGenerate(
        paragraphs: List<String>,
        voice: String,
        speed: Float,
        synthesizer: suspend (String) -> Result<AudioData>,
        onProgress: (Int, Int) -> Unit = { _, _ -> }
    ) {
        Log.info { "Pre-generating audio for ${paragraphs.size} paragraphs..." }
        
        val startTime = System.currentTimeMillis()
        var cached = 0
        var generated = 0
        
        paragraphs.forEachIndexed { index, text ->
            if (text.isBlank()) return@forEachIndexed
            
            // Check if already cached
            val existing = get(text, voice, speed)
            if (existing != null) {
                cached++
            } else {
                // Generate audio
                val result = synthesizer(text)
                result.onSuccess { audioData ->
                    put(text, voice, speed, audioData)
                    generated++
                }.onFailure { error ->
                    Log.error { "Failed to pre-generate paragraph ${index + 1}: ${error.message}" }
                }
            }
            
            onProgress(index + 1, paragraphs.size)
        }
        
        val duration = System.currentTimeMillis() - startTime
        Log.info { "Pre-generation complete: $cached cached, $generated generated in ${duration}ms" }
    }
    
    /**
     * Clear all cached audio
     */
    suspend fun clear() = mutex.withLock {
        cacheDir.listFiles()?.forEach { it.delete() }
        cacheIndex.clear()
        currentCacheSizeBytes = 0
        Log.info { "Audio cache cleared" }
    }
    
    /**
     * Get cache statistics
     */
    suspend fun getStats(): CacheStats = mutex.withLock {
        return CacheStats(
            entryCount = cacheIndex.size,
            totalSizeBytes = currentCacheSizeBytes,
            maxSizeBytes = maxCacheSizeMB * 1024 * 1024
        )
    }
    
    /**
     * Generate cache key from text, voice, and speed
     */
    private fun generateKey(text: String, voice: String, speed: Float): String {
        val input = "$text|$voice|$speed"
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(input.toByteArray())
        return hash.joinToString("") { "%02x".format(it) }.take(32)
    }
    
    /**
     * Load cache index from disk
     */
    private fun loadCacheIndex() {
        try {
            cacheDir.listFiles()?.forEach { file ->
                if (file.extension == "wav") {
                    val key = file.nameWithoutExtension
                    val entry = CacheEntry(
                        key = key,
                        filename = file.name,
                        sizeBytes = file.length(),
                        sampleRate = 22050, // Default, will be overwritten on access
                        channels = 1,
                        format = AudioData.AudioFormat.PCM_16,
                        createdTime = file.lastModified(),
                        lastAccessTime = file.lastModified()
                    )
                    cacheIndex[key] = entry
                    currentCacheSizeBytes += entry.sizeBytes
                }
            }
            Log.info { "Loaded ${cacheIndex.size} cached audio entries (${currentCacheSizeBytes / 1024 / 1024} MB)" }
        } catch (e: Exception) {
            Log.error { "Failed to load cache index: ${e.message}" }
        }
    }
    
    /**
     * Cleanup old entries using LRU strategy
     */
    private fun cleanupOldEntries() {
        val targetSize = (maxCacheSizeMB * 1024 * 1024 * 0.8).toLong() // Clean to 80% capacity
        
        // Sort by last access time (oldest first)
        val sortedEntries = cacheIndex.values.sortedBy { it.lastAccessTime }
        
        var removed = 0
        for (entry in sortedEntries) {
            if (currentCacheSizeBytes <= targetSize) break
            
            val file = File(cacheDir, entry.filename)
            if (file.exists()) {
                file.delete()
            }
            
            cacheIndex.remove(entry.key)
            currentCacheSizeBytes -= entry.sizeBytes
            removed++
        }
        
        Log.info { "Cleaned up $removed old cache entries" }
    }
    
    data class CacheEntry(
        val key: String,
        val filename: String,
        val sizeBytes: Long,
        val sampleRate: Int,
        val channels: Int,
        val format: AudioData.AudioFormat,
        val createdTime: Long,
        var lastAccessTime: Long
    )
    
    data class CacheStats(
        val entryCount: Int,
        val totalSizeBytes: Long,
        val maxSizeBytes: Long
    ) {
        val usagePercent: Float
            get() = if (maxSizeBytes > 0) (totalSizeBytes.toFloat() / maxSizeBytes * 100) else 0f
    }
}
