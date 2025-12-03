package ireader.domain.services.common

import kotlinx.coroutines.flow.StateFlow

/**
 * Common cache service for managing cached data
 */
interface CacheService : PlatformService {
    /**
     * Cache statistics
     */
    val cacheStats: StateFlow<CacheStats>
    
    /**
     * Put string value in cache
     */
    suspend fun putString(
        key: String,
        value: String,
        expirationMillis: Long? = null
    ): ServiceResult<Unit>
    
    /**
     * Get string value from cache
     */
    suspend fun getString(key: String): ServiceResult<String?>
    
    /**
     * Put bytes in cache
     */
    suspend fun putBytes(
        key: String,
        value: ByteArray,
        expirationMillis: Long? = null
    ): ServiceResult<Unit>
    
    /**
     * Get bytes from cache
     */
    suspend fun getBytes(key: String): ServiceResult<ByteArray?>
    
    /**
     * Put serializable object in cache
     */
    suspend fun <T> putObject(
        key: String,
        value: T,
        expirationMillis: Long? = null
    ): ServiceResult<Unit>
    
    /**
     * Get serializable object from cache
     */
    suspend fun <T : Any> getObject(
        key: String,
        type: kotlin.reflect.KClass<T>
    ): ServiceResult<T?>
    
    /**
     * Check if key exists in cache
     */
    suspend fun contains(key: String): Boolean
    
    /**
     * Remove item from cache
     */
    suspend fun remove(key: String): ServiceResult<Unit>
    
    /**
     * Clear all cache
     */
    suspend fun clear(): ServiceResult<Unit>
    
    /**
     * Clear expired items
     */
    suspend fun clearExpired(): ServiceResult<Int>
    
    /**
     * Get cache size in bytes
     */
    suspend fun getCacheSize(): ServiceResult<Long>
    
    /**
     * Set cache size limit
     */
    suspend fun setCacheSizeLimit(bytes: Long): ServiceResult<Unit>
    
    /**
     * Get all cache keys
     */
    suspend fun getAllKeys(): ServiceResult<List<String>>
    
    /**
     * Get keys matching pattern
     */
    suspend fun getKeysMatching(pattern: String): ServiceResult<List<String>>
}

/**
 * Cache statistics
 */
data class CacheStats(
    val totalSize: Long = 0,
    val itemCount: Int = 0,
    val hitCount: Long = 0,
    val missCount: Long = 0,
    val evictionCount: Long = 0,
    val hitRate: Float = 0f
)

/**
 * Cache entry information
 */
data class CacheEntry(
    val key: String,
    val size: Long,
    val createdAt: Long,
    val expiresAt: Long?,
    val isExpired: Boolean
)
