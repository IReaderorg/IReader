package ireader.core.util

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * A thread-safe mutable map implementation for Kotlin Multiplatform.
 * Synchronizes all synchronous operations on an internal lock object
 * and provides suspend helper functions with Coroutines Mutex.
 */
class SynchronizedMap<K, V> : MutableMap<K, V> {
    private val lock = Any()
    private val mutex = Mutex()
    private val delegate = mutableMapOf<K, V>()
    
    override val size: Int get() = synchronized(lock) { delegate.size }
    override val entries: MutableSet<MutableMap.MutableEntry<K, V>> get() = synchronized(lock) { delegate.entries.toMutableSet() }
    override val keys: MutableSet<K> get() = synchronized(lock) { delegate.keys.toMutableSet() }
    override val values: MutableCollection<V> get() = synchronized(lock) { delegate.values.toMutableList() }
    
    override fun containsKey(key: K): Boolean = synchronized(lock) { delegate.containsKey(key) }
    override fun containsValue(value: V): Boolean = synchronized(lock) { delegate.containsValue(value) }
    override fun get(key: K): V? = synchronized(lock) { delegate[key] }
    override fun isEmpty(): Boolean = synchronized(lock) { delegate.isEmpty() }
    
    override fun clear() = synchronized(lock) {
        delegate.clear()
    }
    
    override fun put(key: K, value: V): V? = synchronized(lock) {
        delegate.put(key, value)
    }
    
    override fun putAll(from: Map<out K, V>) = synchronized(lock) {
        delegate.putAll(from)
    }
    
    override fun remove(key: K): V? = synchronized(lock) {
        delegate.remove(key)
    }
    
    fun toMap(): Map<K, V> = synchronized(lock) {
        delegate.toMap()
    }
    
    /**
     * Thread-safe put operation using suspend function.
     */
    suspend fun putSafe(key: K, value: V): V? = mutex.withLock {
        synchronized(lock) {
            delegate.put(key, value)
        }
    }
    
    /**
     * Thread-safe get operation using suspend function.
     */
    suspend fun getSafe(key: K): V? = mutex.withLock {
        synchronized(lock) {
            delegate[key]
        }
    }
    
    /**
     * Thread-safe remove operation using suspend function.
     */
    suspend fun removeSafe(key: K): V? = mutex.withLock {
        synchronized(lock) {
            delegate.remove(key)
        }
    }
    
    /**
     * Thread-safe compute if absent.
     */
    suspend fun getOrPutSafe(key: K, defaultValue: () -> V): V = mutex.withLock {
        synchronized(lock) {
            delegate.getOrPut(key, defaultValue)
        }
    }
}

/**
 * A thread-safe mutable set implementation for Kotlin Multiplatform.
 */
class SynchronizedSet<E> : MutableSet<E> {
    private val lock = Any()
    private val mutex = Mutex()
    private val delegate = mutableSetOf<E>()
    
    override val size: Int get() = synchronized(lock) { delegate.size }
    
    override fun add(element: E): Boolean = synchronized(lock) { delegate.add(element) }
    override fun addAll(elements: Collection<E>): Boolean = synchronized(lock) { delegate.addAll(elements) }
    override fun clear() = synchronized(lock) { delegate.clear() }
    override fun contains(element: E): Boolean = synchronized(lock) { delegate.contains(element) }
    override fun containsAll(elements: Collection<E>): Boolean = synchronized(lock) { delegate.containsAll(elements) }
    override fun isEmpty(): Boolean = synchronized(lock) { delegate.isEmpty() }
    override fun iterator(): MutableIterator<E> = synchronized(lock) { delegate.toMutableList().iterator() }
    override fun remove(element: E): Boolean = synchronized(lock) { delegate.remove(element) }
    override fun removeAll(elements: Collection<E>): Boolean = synchronized(lock) { delegate.removeAll(elements) }
    override fun retainAll(elements: Collection<E>): Boolean = synchronized(lock) { delegate.retainAll(elements) }
    
    fun toSet(): Set<E> = synchronized(lock) {
        delegate.toSet()
    }
    
    /**
     * Thread-safe add operation using suspend function.
     */
    suspend fun addSafe(element: E): Boolean = mutex.withLock {
        synchronized(lock) {
            delegate.add(element)
        }
    }
    
    /**
     * Thread-safe remove operation using suspend function.
     */
    suspend fun removeSafe(element: E): Boolean = mutex.withLock {
        synchronized(lock) {
            delegate.remove(element)
        }
    }
    
    /**
     * Thread-safe contains check using suspend function.
     */
    suspend fun containsSafe(element: E): Boolean = mutex.withLock {
        synchronized(lock) {
            delegate.contains(element)
        }
    }
}

/**
 * Create a synchronized map.
 */
fun <K, V> synchronizedMapOf(): SynchronizedMap<K, V> = SynchronizedMap()

/**
 * Create a synchronized set.
 */
fun <E> synchronizedSetOf(): SynchronizedSet<E> = SynchronizedSet()
