package ireader.domain.services.tts_service
import ireader.domain.utils.extensions.ioDispatcher

import ireader.domain.models.tts.VoiceModel
import okio.FileSystem
import okio.Path
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ireader.domain.utils.extensions.currentTimeToLong

/**
 * Service for managing local storage of voice models.
 * Uses Okio for KMP compatibility.
 * Requirements: 4.5, 5.5
 */
class VoiceStorage(
    private val storageDirectory: Path,
    private val fileSystem: FileSystem
) {
    
    init {
        if (!fileSystem.exists(storageDirectory)) {
            fileSystem.createDirectories(storageDirectory)
        }
    }
    
    /**
     * Get the directory for a specific voice
     */
    fun getVoiceDirectory(voiceId: String): Path {
        return storageDirectory / voiceId
    }
    
    /**
     * Get the model file for a voice
     */
    fun getModelFile(voiceId: String): Path {
        return getVoiceDirectory(voiceId) / "$voiceId.onnx"
    }
    
    /**
     * Get the config file for a voice
     */
    fun getConfigFile(voiceId: String): Path {
        return getVoiceDirectory(voiceId) / "$voiceId.onnx.json"
    }
    
    /**
     * Check if a voice is downloaded
     */
    fun isVoiceDownloaded(voiceId: String): Boolean {
        val modelFile = getModelFile(voiceId)
        val configFile = getConfigFile(voiceId)
        return fileSystem.exists(modelFile) && fileSystem.exists(configFile)
    }
    
    /**
     * Get list of all downloaded voice IDs
     */
    suspend fun getDownloadedVoiceIds(): List<String> = withContext(ioDispatcher) {
        if (!fileSystem.exists(storageDirectory)) return@withContext emptyList()
        
        fileSystem.list(storageDirectory)
            .filter { fileSystem.metadata(it).isDirectory }
            .map { it.name }
            .filter { isVoiceDownloaded(it) }
    }
    
    /**
     * Delete a voice from storage
     */
    suspend fun deleteVoice(voiceId: String): Result<Unit> = withContext(ioDispatcher) {
        try {
            val voiceDir = getVoiceDirectory(voiceId)
            if (fileSystem.exists(voiceDir)) {
                fileSystem.deleteRecursively(voiceDir)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get total storage used by all voices
     */
    suspend fun getTotalStorageUsage(): Long = withContext(ioDispatcher) {
        calculateDirectorySize(storageDirectory)
    }
    
    /**
     * Get storage used by a specific voice
     */
    suspend fun getVoiceStorageUsage(voiceId: String): Long = withContext(ioDispatcher) {
        val voiceDir = getVoiceDirectory(voiceId)
        if (fileSystem.exists(voiceDir)) {
            calculateDirectorySize(voiceDir)
        } else {
            0L
        }
    }
    
    /**
     * Calculate the size of a directory recursively
     */
    private fun calculateDirectorySize(directory: Path): Long {
        if (!fileSystem.exists(directory)) return 0L
        
        var size = 0L
        val metadata = fileSystem.metadata(directory)
        if (metadata.isDirectory) {
            fileSystem.list(directory).forEach { child ->
                size += calculateDirectorySize(child)
            }
        } else {
            size = metadata.size ?: 0L
        }
        return size
    }
    
    /**
     * Clean up orphaned files (directories without both model and config)
     */
    suspend fun cleanupOrphanedFiles(): Int = withContext(ioDispatcher) {
        if (!fileSystem.exists(storageDirectory)) return@withContext 0
        
        var cleaned = 0
        fileSystem.list(storageDirectory).forEach { dir ->
            if (fileSystem.metadata(dir).isDirectory) {
                val modelFile = dir / "${dir.name}.onnx"
                val configFile = dir / "${dir.name}.onnx.json"
                
                if (!fileSystem.exists(modelFile) || !fileSystem.exists(configFile)) {
                    fileSystem.deleteRecursively(dir)
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
    private val cache = mutableMapOf<String, CacheEntry<T>>()
    private val accessOrder = mutableListOf<String>()
    private val lock = kotlinx.coroutines.sync.Mutex()
    
    data class CacheEntry<T>(
        val value: T,
        val timestamp: Long = currentTimeToLong()
    )
    
    /**
     * Get a value from the cache
     */
    fun get(key: String): T? {
        val entry = cache[key]
        if (entry != null) {
            // Update access order for LRU
            accessOrder.remove(key)
            accessOrder.add(key)
        }
        return entry?.value
    }
    
    /**
     * Put a value in the cache
     */
    fun put(key: String, value: T) {
        // Remove oldest entry if cache is full
        if (cache.size >= maxCacheSize && !cache.containsKey(key)) {
            val oldestKey = accessOrder.firstOrNull()
            if (oldestKey != null) {
                cache.remove(oldestKey)
                accessOrder.removeAt(0)
            }
        }
        
        cache[key] = CacheEntry(value)
        accessOrder.remove(key)
        accessOrder.add(key)
    }
    
    /**
     * Remove a value from the cache
     */
    fun remove(key: String): T? {
        accessOrder.remove(key)
        return cache.remove(key)?.value
    }
    
    /**
     * Clear the entire cache
     */
    fun clear() {
        cache.clear()
        accessOrder.clear()
    }
    
    /**
     * Get the current size of the cache
     */
    fun size(): Int {
        return cache.size
    }
    
    /**
     * Check if a key exists in the cache
     */
    fun contains(key: String): Boolean {
        return cache.containsKey(key)
    }
    
    /**
     * Get all keys in the cache
     */
    fun keys(): Set<String> {
        return cache.keys.toSet()
    }
    
    /**
     * Evict least recently used entry
     */
    fun evictLeastUsed(): T? {
        if (cache.isEmpty()) return null
        val oldestKey = accessOrder.firstOrNull() ?: return null
        accessOrder.removeAt(0)
        return cache.remove(oldestKey)?.value
    }
}
