package ireader.domain.services.common

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.ConcurrentHashMap

/**
 * Desktop implementation of CacheService
 */
class DesktopCacheService : CacheService {
    
    private val cacheDir = File(System.getProperty("user.home"), ".ireader/cache").apply { mkdirs() }
    private val cacheMetadata = ConcurrentHashMap<String, CacheMetadata>()
    private var cacheSizeLimit = 100 * 1024 * 1024L // 100MB default
    
    private val _cacheStats = MutableStateFlow(CacheStats())
    override val cacheStats: StateFlow<CacheStats> = _cacheStats.asStateFlow()
    
    override suspend fun initialize() {
        loadMetadata()
        updateStats()
    }
    
    override suspend fun start() {}
    override suspend fun stop() {}
    override fun isRunning(): Boolean = true
    override suspend fun cleanup() {
        clear()
    }
    
    override suspend fun putString(key: String, value: String, expirationMillis: Long?): ServiceResult<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val file = File(cacheDir, key.hashCode().toString())
                file.writeText(value)
                
                cacheMetadata[key] = CacheMetadata(
                    size = value.length.toLong(),
                    createdAt = System.currentTimeMillis(),
                    expiresAt = expirationMillis?.let { System.currentTimeMillis() + it }
                )
                
                updateStats()
                enforceSizeLimit()
                ServiceResult.Success(Unit)
            } catch (e: Exception) {
                ServiceResult.Error("Failed to cache string: ${e.message}", e)
            }
        }
    }
    
    override suspend fun getString(key: String): ServiceResult<String?> {
        return withContext(Dispatchers.IO) {
            try {
                val metadata = cacheMetadata[key]
                if (metadata != null && metadata.isExpired()) {
                    remove(key)
                    return@withContext ServiceResult.Success(null)
                }
                
                val file = File(cacheDir, key.hashCode().toString())
                if (!file.exists()) {
                    return@withContext ServiceResult.Success(null)
                }
                
                updateStats(hit = true)
                ServiceResult.Success(file.readText())
            } catch (e: Exception) {
                updateStats(hit = false)
                ServiceResult.Error("Failed to read cache: ${e.message}", e)
            }
        }
    }
    
    override suspend fun putBytes(key: String, value: ByteArray, expirationMillis: Long?): ServiceResult<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val file = File(cacheDir, key.hashCode().toString())
                file.writeBytes(value)
                
                cacheMetadata[key] = CacheMetadata(
                    size = value.size.toLong(),
                    createdAt = System.currentTimeMillis(),
                    expiresAt = expirationMillis?.let { System.currentTimeMillis() + it }
                )
                
                updateStats()
                enforceSizeLimit()
                ServiceResult.Success(Unit)
            } catch (e: Exception) {
                ServiceResult.Error("Failed to cache bytes: ${e.message}", e)
            }
        }
    }
    
    override suspend fun getBytes(key: String): ServiceResult<ByteArray?> {
        return withContext(Dispatchers.IO) {
            try {
                val metadata = cacheMetadata[key]
                if (metadata != null && metadata.isExpired()) {
                    remove(key)
                    return@withContext ServiceResult.Success(null)
                }
                
                val file = File(cacheDir, key.hashCode().toString())
                if (!file.exists()) {
                    return@withContext ServiceResult.Success(null)
                }
                
                updateStats(hit = true)
                ServiceResult.Success(file.readBytes())
            } catch (e: Exception) {
                updateStats(hit = false)
                ServiceResult.Error("Failed to read cache: ${e.message}", e)
            }
        }
    }
    
    override suspend fun <T> putObject(key: String, value: T, expirationMillis: Long?): ServiceResult<Unit> {
        return ServiceResult.Success(Unit)
    }
    
    override suspend fun <T : Any> getObject(key: String, type: kotlin.reflect.KClass<T>): ServiceResult<T?> {
        return ServiceResult.Success(null)
    }
    
    override suspend fun contains(key: String): Boolean {
        return cacheMetadata.containsKey(key) && !cacheMetadata[key]!!.isExpired()
    }
    
    override suspend fun remove(key: String): ServiceResult<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val file = File(cacheDir, key.hashCode().toString())
                if (file.exists()) {
                    file.delete()
                }
                cacheMetadata.remove(key)
                updateStats()
                ServiceResult.Success(Unit)
            } catch (e: Exception) {
                ServiceResult.Error("Failed to remove cache: ${e.message}", e)
            }
        }
    }
    
    override suspend fun clear(): ServiceResult<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                cacheDir.listFiles()?.forEach { it.delete() }
                cacheMetadata.clear()
                updateStats()
                ServiceResult.Success(Unit)
            } catch (e: Exception) {
                ServiceResult.Error("Failed to clear cache: ${e.message}", e)
            }
        }
    }
    
    override suspend fun clearExpired(): ServiceResult<Int> {
        return withContext(Dispatchers.IO) {
            try {
                var count = 0
                cacheMetadata.entries.filter { it.value.isExpired() }.forEach {
                    remove(it.key)
                    count++
                }
                ServiceResult.Success(count)
            } catch (e: Exception) {
                ServiceResult.Error("Failed to clear expired: ${e.message}", e)
            }
        }
    }
    
    override suspend fun getCacheSize(): ServiceResult<Long> {
        return withContext(Dispatchers.IO) {
            try {
                val size = cacheDir.walkTopDown().filter { it.isFile }.map { it.length() }.sum()
                ServiceResult.Success(size)
            } catch (e: Exception) {
                ServiceResult.Error("Failed to get cache size: ${e.message}", e)
            }
        }
    }
    
    override suspend fun setCacheSizeLimit(bytes: Long): ServiceResult<Unit> {
        cacheSizeLimit = bytes
        enforceSizeLimit()
        return ServiceResult.Success(Unit)
    }
    
    override suspend fun getAllKeys(): ServiceResult<List<String>> {
        return ServiceResult.Success(cacheMetadata.keys.toList())
    }
    
    override suspend fun getKeysMatching(pattern: String): ServiceResult<List<String>> {
        val regex = pattern.toRegex()
        return ServiceResult.Success(cacheMetadata.keys.filter { regex.matches(it) })
    }
    
    private fun loadMetadata() {}
    
    private fun updateStats(hit: Boolean? = null) {
        val currentStats = _cacheStats.value
        _cacheStats.value = currentStats.copy(
            totalSize = cacheMetadata.values.sumOf { it.size },
            itemCount = cacheMetadata.size,
            hitCount = if (hit == true) currentStats.hitCount + 1 else currentStats.hitCount,
            missCount = if (hit == false) currentStats.missCount + 1 else currentStats.missCount,
            hitRate = if (currentStats.hitCount + currentStats.missCount > 0) {
                currentStats.hitCount.toFloat() / (currentStats.hitCount + currentStats.missCount)
            } else 0f
        )
    }
    
    private suspend fun enforceSizeLimit() {
        val currentSize = getCacheSize()
        if (currentSize is ServiceResult.Success && currentSize.data > cacheSizeLimit) {
            val sortedEntries = cacheMetadata.entries.sortedBy { it.value.createdAt }
            var sizeToFree = currentSize.data - cacheSizeLimit
            
            for (entry in sortedEntries) {
                if (sizeToFree <= 0) break
                remove(entry.key)
                sizeToFree -= entry.value.size
            }
        }
    }
    
    private data class CacheMetadata(
        val size: Long,
        val createdAt: Long,
        val expiresAt: Long?
    ) {
        fun isExpired(): Boolean = expiresAt != null && System.currentTimeMillis() > expiresAt
    }
}
