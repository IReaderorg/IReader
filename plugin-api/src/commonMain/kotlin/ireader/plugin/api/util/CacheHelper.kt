package ireader.plugin.api.util

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.time.ExperimentalTime

/**
 * Simple in-memory cache for plugins.
 * Useful for caching API responses, translations, etc.
 */
class PluginCache<K, V>(
    private val maxSize: Int = 100,
    private val expirationMs: Long = 5 * 60 * 1000 // 5 minutes default
) {
    class CacheEntry<V>(
        val value: V,
        val timestamp: Long
    )
    
    private val cache = mutableMapOf<K, CacheEntry<V>>()
    private val mutex = Mutex()
    
    /**
     * Get a value from cache, or null if not present or expired.
     */
    suspend fun get(key: K): V? = mutex.withLock {
        val entry = cache[key] ?: return@withLock null
        val now = currentTimeMillis()
        
        if (now - entry.timestamp > expirationMs) {
            cache.remove(key)
            return@withLock null
        }
        
        entry.value
    }
    
    /**
     * Put a value in the cache.
     */
    suspend fun put(key: K, value: V) = mutex.withLock {
        // Evict oldest entries if at capacity
        if (cache.size >= maxSize) {
            val oldest = cache.entries.minByOrNull { it.value.timestamp }
            oldest?.let { cache.remove(it.key) }
        }
        
        cache[key] = CacheEntry(value, currentTimeMillis())
    }
    
    /**
     * Get a value from cache, or compute and cache it if not present.
     */
    suspend fun getOrPut(key: K, compute: suspend () -> V): V = mutex.withLock {
        val existing = cache[key]
        val now = currentTimeMillis()
        
        if (existing != null && now - existing.timestamp <= expirationMs) {
            return@withLock existing.value
        }
        
        val value = compute()
        
        if (cache.size >= maxSize) {
            val oldest = cache.entries.minByOrNull { it.value.timestamp }
            oldest?.let { cache.remove(it.key) }
        }
        
        cache[key] = CacheEntry(value, now)
        value
    }
    
    /**
     * Remove a value from cache.
     */
    suspend fun remove(key: K) = mutex.withLock {
        cache.remove(key)
    }
    
    /**
     * Clear all cached values.
     */
    suspend fun clear() = mutex.withLock {
        cache.clear()
    }
    
    /**
     * Get current cache size.
     */
    suspend fun size(): Int = mutex.withLock {
        cache.size
    }
    
    /**
     * Remove expired entries.
     */
    suspend fun evictExpired(): Unit = mutex.withLock {
        val now = currentTimeMillis()
        val keysToRemove = cache.entries
            .filter { now - it.value.timestamp > expirationMs }
            .map { it.key }
        keysToRemove.forEach { cache.remove(it) }
    }
    
    @OptIn(ExperimentalTime::class)
    private fun currentTimeMillis(): Long {
        return kotlin.time.Clock.System.now().toEpochMilliseconds()
    }
}

/**
 * Create a simple string-keyed cache.
 */
fun <V> createStringCache(
    maxSize: Int = 100,
    expirationMs: Long = 5 * 60 * 1000
): PluginCache<String, V> {
    return PluginCache(maxSize, expirationMs)
}
