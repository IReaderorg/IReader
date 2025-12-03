package ireader.domain.services.tts_service

import ireader.domain.models.tts.VoiceModel
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ireader.domain.utils.extensions.currentTimeToLong

/**
 * Service for managing local storage of voice models
 * Requirements: 4.5, 5.5
 */
class VoiceStorage(
    private val storageDirectory: File
) {
    
    init {
        if (!storageDirectory.exists()) {
            storageDirectory.mkdirs()
        }
    }
    
    /**
     * Get the directory for a specific voice
     */
    fun getVoiceDirectory(voiceId: String): File {
        return File(storageDirectory, voiceId)
    }
    
    /**
     * Get the model file for a voice
     */
    fun getModelFile(voiceId: String): File {
        return File(getVoiceDirectory(voiceId), "$voiceId.onnx")
    }
    
    /**
     * Get the config file for a voice
     */
    fun getConfigFile(voiceId: String): File {
        return File(getVoiceDirectory(voiceId), "$voiceId.onnx.json")
    }
    
    /**
     * Check if a voice is downloaded
     */
    fun isVoiceDownloaded(voiceId: String): Boolean {
        val modelFile = getModelFile(voiceId)
        val configFile = getConfigFile(voiceId)
        return modelFile.exists() && configFile.exists()
    }
    
    /**
     * Get list of all downloaded voice IDs
     */
    suspend fun getDownloadedVoiceIds(): List<String> = withContext(Dispatchers.IO) {
        storageDirectory.listFiles()
            ?.filter { it.isDirectory }
            ?.map { it.name }
            ?.filter { isVoiceDownloaded(it) }
            ?: emptyList()
    }
    
    /**
     * Delete a voice from storage
     */
    suspend fun deleteVoice(voiceId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val voiceDir = getVoiceDirectory(voiceId)
            if (voiceDir.exists()) {
                voiceDir.deleteRecursively()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get total storage used by all voices
     */
    suspend fun getTotalStorageUsage(): Long = withContext(Dispatchers.IO) {
        calculateDirectorySize(storageDirectory)
    }
    
    /**
     * Get storage used by a specific voice
     */
    suspend fun getVoiceStorageUsage(voiceId: String): Long = withContext(Dispatchers.IO) {
        val voiceDir = getVoiceDirectory(voiceId)
        if (voiceDir.exists()) {
            calculateDirectorySize(voiceDir)
        } else {
            0L
        }
    }
    
    /**
     * Calculate the size of a directory recursively
     */
    private fun calculateDirectorySize(directory: File): Long {
        var size = 0L
        directory.listFiles()?.forEach { file ->
            size += if (file.isDirectory) {
                calculateDirectorySize(file)
            } else {
                file.length()
            }
        }
        return size
    }
    
    /**
     * Clean up orphaned files (directories without both model and config)
     */
    suspend fun cleanupOrphanedFiles(): Int = withContext(Dispatchers.IO) {
        var cleaned = 0
        storageDirectory.listFiles()?.forEach { dir ->
            if (dir.isDirectory) {
                val modelFile = File(dir, "${dir.name}.onnx")
                val configFile = File(dir, "${dir.name}.onnx.json")
                
                if (!modelFile.exists() || !configFile.exists()) {
                    dir.deleteRecursively()
                    cleaned++
                }
            }
        }
        cleaned
    }
}

/**
 * LRU cache for managing loaded voice instances
 * Requirements: 5.3, 5.5
 */
class VoiceModelCache<T>(
    private val maxCacheSize: Int = 3
) {
    private val cache = LinkedHashMap<String, CacheEntry<T>>(maxCacheSize, 0.75f, true)
    
    data class CacheEntry<T>(
        val value: T,
        val timestamp: Long = currentTimeToLong()
    )
    
    /**
     * Get a value from the cache
     */
    @Synchronized
    fun get(key: String): T? {
        return cache[key]?.value
    }
    
    /**
     * Put a value in the cache
     */
    @Synchronized
    fun put(key: String, value: T) {
        // Remove oldest entry if cache is full
        if (cache.size >= maxCacheSize && !cache.containsKey(key)) {
            val oldestKey = cache.keys.first()
            cache.remove(oldestKey)
        }
        
        cache[key] = CacheEntry(value)
    }
    
    /**
     * Remove a value from the cache
     */
    @Synchronized
    fun remove(key: String): T? {
        return cache.remove(key)?.value
    }
    
    /**
     * Clear the entire cache
     */
    @Synchronized
    fun clear() {
        cache.clear()
    }
    
    /**
     * Get the current size of the cache
     */
    @Synchronized
    fun size(): Int {
        return cache.size
    }
    
    /**
     * Check if a key exists in the cache
     */
    @Synchronized
    fun contains(key: String): Boolean {
        return cache.containsKey(key)
    }
    
    /**
     * Get all keys in the cache
     */
    @Synchronized
    fun keys(): Set<String> {
        return cache.keys.toSet()
    }
    
    /**
     * Evict least recently used entry
     */
    @Synchronized
    fun evictLeastUsed(): T? {
        if (cache.isEmpty()) return null
        val oldestKey = cache.keys.first()
        return cache.remove(oldestKey)?.value
    }
}
