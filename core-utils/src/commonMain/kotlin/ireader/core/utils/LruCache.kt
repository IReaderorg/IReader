package ireader.core.utils

/**
 * A thread-safe LRU (Least Recently Used) cache implementation.
 * Uses a LinkedHashMap for true O(1) access and eviction across all KMP targets.
 */
class LruCache<K, V>(
    private val maxSize: Int,
    private val onEvicted: ((key: K, value: V) -> Unit)? = null
) {
    private val lock = Any()
    private val cache = LinkedHashMap<K, V>(maxSize, 0.75f)
    
    suspend fun get(key: K): V? = synchronized(lock) {
        val value = cache.remove(key)
        if (value != null) {
            // Move to end (most recently used)
            cache[key] = value
        }
        value
    }
    
    fun getOrNull(key: K): V? = synchronized(lock) {
        val value = cache.remove(key)
        if (value != null) {
            cache[key] = value
        }
        value
    }
    
    suspend fun put(key: K, value: V): V? = synchronized(lock) {
        val oldValue = cache.remove(key)
        cache[key] = value
        
        // Evict oldest entries if over capacity
        while (cache.size > maxSize) {
            val iterator = cache.iterator()
            if (iterator.hasNext()) {
                val (eldestKey, evictedValue) = iterator.next()
                iterator.remove()
                onEvicted?.invoke(eldestKey, evictedValue)
            } else {
                break
            }
        }
        
        oldValue
    }
    
    suspend fun remove(key: K): V? = synchronized(lock) {
        cache.remove(key)
    }
    
    suspend fun containsKey(key: K): Boolean = synchronized(lock) {
        cache.containsKey(key)
    }
    
    suspend fun clear() = synchronized(lock) {
        cache.clear()
    }
    
    suspend fun size(): Int = synchronized(lock) {
        cache.size
    }
    
    suspend fun keys(): Set<K> = synchronized(lock) {
        cache.keys.toSet()
    }
    
    suspend fun values(): List<V> = synchronized(lock) {
        cache.values.toList()
    }
    
    suspend fun entries(): Map<K, V> = synchronized(lock) {
        cache.toMap()
    }
    
    suspend fun <R> withLock(block: (Map<K, V>) -> R): R = synchronized(lock) {
        block(cache.toMap())
    }
}
