package ireader.core.utils

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * A thread-safe LRU (Least Recently Used) cache implementation.
 * Uses a simple map-based approach that works across all KMP targets.
 */
class LruCache<K, V>(
    private val maxSize: Int,
    private val onEvicted: ((key: K, value: V) -> Unit)? = null
) {
    private val mutex = Mutex()
    private val cache = mutableMapOf<K, V>()
    private val accessOrder = mutableListOf<K>()
    
    suspend fun get(key: K): V? = mutex.withLock {
        val value = cache[key]
        if (value != null) {
            // Move to end (most recently used)
            accessOrder.remove(key)
            accessOrder.add(key)
        }
        value
    }
    
    fun getOrNull(key: K): V? {
        return if (mutex.tryLock()) {
            try {
                val value = cache[key]
                if (value != null) {
                    accessOrder.remove(key)
                    accessOrder.add(key)
                }
                value
            } finally {
                mutex.unlock()
            }
        } else {
            null
        }
    }
    
    suspend fun put(key: K, value: V): V? = mutex.withLock {
        val oldValue = cache.put(key, value)
        
        if (oldValue == null) {
            accessOrder.add(key)
        } else {
            accessOrder.remove(key)
            accessOrder.add(key)
        }
        
        // Evict oldest entries if over capacity
        while (cache.size > maxSize && accessOrder.isNotEmpty()) {
            val eldestKey = accessOrder.removeAt(0)
            val evictedValue = cache.remove(eldestKey)
            if (evictedValue != null) {
                onEvicted?.invoke(eldestKey, evictedValue)
            }
        }
        
        oldValue
    }
    
    suspend fun remove(key: K): V? = mutex.withLock {
        accessOrder.remove(key)
        cache.remove(key)
    }
    
    suspend fun containsKey(key: K): Boolean = mutex.withLock {
        cache.containsKey(key)
    }
    
    suspend fun clear() = mutex.withLock {
        cache.clear()
        accessOrder.clear()
    }
    
    suspend fun size(): Int = mutex.withLock {
        cache.size
    }
    
    suspend fun keys(): Set<K> = mutex.withLock {
        cache.keys.toSet()
    }
    
    suspend fun values(): List<V> = mutex.withLock {
        cache.values.toList()
    }
    
    suspend fun entries(): Map<K, V> = mutex.withLock {
        cache.toMap()
    }
    
    suspend fun <R> withLock(block: (Map<K, V>) -> R): R = mutex.withLock {
        block(cache)
    }
}
