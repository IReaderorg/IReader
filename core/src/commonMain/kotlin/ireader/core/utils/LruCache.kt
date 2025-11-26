package ireader.core.utils

/**
 * A simple LRU (Least Recently Used) cache implementation.
 * 
 * This cache maintains a maximum size and evicts the least recently used items
 * when the cache is full and a new item is added.
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
    private val cache = LinkedHashMap<K, V>(maxSize, 0.75f, true)
    
    /**
     * Returns the value associated with the key, or null if not found.
     * Accessing an entry marks it as recently used.
     */
    fun get(key: K): V? {
        return cache[key]
    }
    
    /**
     * Associates the specified value with the specified key.
     * If the cache is full, the least recently used entry is evicted.
     */
    fun put(key: K, value: V): V? {
        val previous = cache.put(key, value)
        
        // Evict oldest entry if cache exceeds max size
        if (cache.size > maxSize) {
            val eldest = cache.entries.first()
            cache.remove(eldest.key)
            onEvicted?.invoke(eldest.key, eldest.value)
        }
        
        return previous
    }
    
    /**
     * Removes the entry for the specified key if present.
     */
    fun remove(key: K): V? {
        return cache.remove(key)
    }
    
    /**
     * Returns true if the cache contains an entry for the specified key.
     */
    fun containsKey(key: K): Boolean {
        return cache.containsKey(key)
    }
    
    /**
     * Removes all entries from the cache.
     */
    fun clear() {
        cache.clear()
    }
    
    /**
     * Returns the current number of entries in the cache.
     */
    val size: Int
        get() = cache.size
    
    /**
     * Returns all keys in the cache (in access order, least recent first).
     */
    val keys: Set<K>
        get() = cache.keys.toSet()
}
