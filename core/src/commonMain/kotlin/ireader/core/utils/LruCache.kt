package ireader.core.utils

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * A thread-safe LRU (Least Recently Used) cache implementation.
 * 
 * This cache maintains a maximum size and evicts the least recently used items
 * when the cache is full and a new item is added. All operations are thread-safe
 * using a mutex for synchronization.
 * 
 * @param K The type of keys maintained by this cache
 * @param V The type of mapped values
 * @param maxSize The maximum number of entries the cache can hold
 * @param onEvicted Optional callback invoked when an entry is evicted
 */
class LruCache<K, V>(
    private val maxSize: Int,
    private val onEvicted: ((key: K, value: V) -> Unit)? = null
) {
    private val mutex = Mutex()
    private val cache = object : LinkedHashMap<K, V>(maxSize, 0.75f, true) {
        override fun removeEldestEntry(eldest: Map.Entry<K, V>): Boolean {
            val shouldRemove = size > maxSize
            if (shouldRemove) {
                onEvicted?.invoke(eldest.key, eldest.value)
            }
            return shouldRemove
        }
    }
    
    /**
     * Returns the value associated with the key, or null if not found.
     * Accessing an entry marks it as recently used.
     * 
     * This is a suspending function to ensure thread safety.
     */
    suspend fun get(key: K): V? = mutex.withLock {
        cache[key]
    }
    
    /**
     * Returns the value associated with the key, or null if not found.
     * Non-suspending version for synchronous contexts.
     * 
     * Note: This uses tryLock and may return null if the lock cannot be acquired immediately.
     * Prefer using the suspending get() method when possible.
     */
    fun getOrNull(key: K): V? {
        return if (mutex.tryLock()) {
            try {
                cache[key]
            } finally {
                mutex.unlock()
            }
        } else {
            null
        }
    }
    
    /**
     * Associates the specified value with the specified key.
     * If the cache is full, the least recently used entry is evicted.
     * Returns the previous value associated with the key, or null if there was none.
     * 
     * This is a suspending function to ensure thread safety.
     */
    suspend fun put(key: K, value: V): V? = mutex.withLock {
        cache.put(key, value)
    }
    
    /**
     * Removes the entry for the specified key if present.
     * Returns the value that was associated with the key, or null if there was none.
     * 
     * This is a suspending function to ensure thread safety.
     */
    suspend fun remove(key: K): V? = mutex.withLock {
        cache.remove(key)
    }
    
    /**
     * Returns true if the cache contains an entry for the specified key.
     * 
     * This is a suspending function to ensure thread safety.
     */
    suspend fun containsKey(key: K): Boolean = mutex.withLock {
        cache.containsKey(key)
    }
    
    /**
     * Removes all entries from the cache.
     * 
     * This is a suspending function to ensure thread safety.
     */
    suspend fun clear() = mutex.withLock {
        cache.clear()
    }
    
    /**
     * Returns the current number of entries in the cache.
     * 
     * This is a suspending function to ensure thread safety.
     */
    suspend fun size(): Int = mutex.withLock {
        cache.size
    }
    
    /**
     * Returns all keys in the cache (in access order, least recent first).
     * Returns a snapshot of the keys at the time of the call.
     * 
     * This is a suspending function to ensure thread safety.
     */
    suspend fun keys(): Set<K> = mutex.withLock {
        cache.keys.toSet()
    }
    
    /**
     * Returns all values in the cache (in access order, least recent first).
     * Returns a snapshot of the values at the time of the call.
     * 
     * This is a suspending function to ensure thread safety.
     */
    suspend fun values(): List<V> = mutex.withLock {
        cache.values.toList()
    }
    
    /**
     * Returns a snapshot of all entries in the cache.
     * 
     * This is a suspending function to ensure thread safety.
     */
    suspend fun entries(): Map<K, V> = mutex.withLock {
        cache.toMap()
    }
    
    /**
     * Executes the given block with exclusive access to the cache.
     * Useful for performing multiple operations atomically.
     * 
     * Example:
     * ```
     * cache.withLock { cache ->
     *     val value1 = cache[key1]
     *     val value2 = cache[key2]
     *     // Process values atomically
     * }
     * ```
     */
    suspend fun <R> withLock(block: (Map<K, V>) -> R): R = mutex.withLock {
        block(cache)
    }
}
