package ireader.core.util

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * A thread-safe mutable map implementation for Kotlin Multiplatform.
 * Uses a Mutex for synchronization instead of JVM's ConcurrentHashMap.
 * 
 * Note: For high-contention scenarios, consider using platform-specific
 * implementations via expect/actual.
 */
class SynchronizedMap<K, V> : MutableMap<K, V> {
    private val mutex = Mutex()
    private val delegate = mutableMapOf<K, V>()
    
    override val size: Int get() = delegate.size
    override val entries: MutableSet<MutableMap.MutableEntry<K, V>> get() = delegate.entries
    override val keys: MutableSet<K> get() = delegate.keys
    override val values: MutableCollection<V> get() = delegate.values
    
    override fun containsKey(key: K): Boolean = delegate.containsKey(key)
    override fun containsValue(value: V): Boolean = delegate.containsValue(value)
    override fun get(key: K): V? = delegate[key]
    override fun isEmpty(): Boolean = delegate.isEmpty()
    
    override fun clear() {
        delegate.clear()
    }
    
    override fun put(key: K, value: V): V? {
        return delegate.put(key, value)
    }
    
    override fun putAll(from: Map<out K, V>) {
        delegate.putAll(from)
    }
    
    override fun remove(key: K): V? {
        return delegate.remove(key)
    }
    
    /**
     * Thread-safe put operation using suspend function.
     */
    suspend fun putSafe(key: K, value: V): V? = mutex.withLock {
        delegate.put(key, value)
    }
    
    /**
     * Thread-safe get operation using suspend function.
     */
    suspend fun getSafe(key: K): V? = mutex.withLock {
        delegate[key]
    }
    
    /**
     * Thread-safe remove operation using suspend function.
     */
    suspend fun removeSafe(key: K): V? = mutex.withLock {
        delegate.remove(key)
    }
    
    /**
     * Thread-safe compute if absent.
     */
    suspend fun getOrPutSafe(key: K, defaultValue: () -> V): V = mutex.withLock {
        delegate.getOrPut(key, defaultValue)
    }
}

/**
 * A thread-safe mutable set implementation for Kotlin Multiplatform.
 */
class SynchronizedSet<E> : MutableSet<E> {
    private val mutex = Mutex()
    private val delegate = mutableSetOf<E>()
    
    override val size: Int get() = delegate.size
    
    override fun add(element: E): Boolean = delegate.add(element)
    override fun addAll(elements: Collection<E>): Boolean = delegate.addAll(elements)
    override fun clear() = delegate.clear()
    override fun contains(element: E): Boolean = delegate.contains(element)
    override fun containsAll(elements: Collection<E>): Boolean = delegate.containsAll(elements)
    override fun isEmpty(): Boolean = delegate.isEmpty()
    override fun iterator(): MutableIterator<E> = delegate.iterator()
    override fun remove(element: E): Boolean = delegate.remove(element)
    override fun removeAll(elements: Collection<E>): Boolean = delegate.removeAll(elements)
    override fun retainAll(elements: Collection<E>): Boolean = delegate.retainAll(elements)
    
    /**
     * Thread-safe add operation using suspend function.
     */
    suspend fun addSafe(element: E): Boolean = mutex.withLock {
        delegate.add(element)
    }
    
    /**
     * Thread-safe remove operation using suspend function.
     */
    suspend fun removeSafe(element: E): Boolean = mutex.withLock {
        delegate.remove(element)
    }
    
    /**
     * Thread-safe contains check using suspend function.
     */
    suspend fun containsSafe(element: E): Boolean = mutex.withLock {
        delegate.contains(element)
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
