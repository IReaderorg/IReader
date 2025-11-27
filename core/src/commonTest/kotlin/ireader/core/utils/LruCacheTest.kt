package ireader.core.utils

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class LruCacheTest {

    @Test
    fun `test basic put and get operations`() = runTest {
        val cache = LruCache<String, Int>(maxSize = 3)
        
        cache.put("one", 1)
        cache.put("two", 2)
        cache.put("three", 3)
        
        assertEquals(1, cache.get("one"))
        assertEquals(2, cache.get("two"))
        assertEquals(3, cache.get("three"))
        assertEquals(3, cache.size())
    }

    @Test
    fun `test LRU eviction when cache is full`() = runTest {
        val evictedKeys = mutableListOf<String>()
        val cache = LruCache<String, Int>(
            maxSize = 3,
            onEvicted = { key, _ -> evictedKeys.add(key) }
        )
        
        cache.put("one", 1)
        cache.put("two", 2)
        cache.put("three", 3)
        cache.put("four", 4) // Should evict "one"
        
        assertNull(cache.get("one"))
        assertEquals(4, cache.get("four"))
        assertEquals(listOf("one"), evictedKeys)
        assertEquals(3, cache.size())
    }

    @Test
    fun `test access order updates LRU`() = runTest {
        val evictedKeys = mutableListOf<String>()
        val cache = LruCache<String, Int>(
            maxSize = 3,
            onEvicted = { key, _ -> evictedKeys.add(key) }
        )
        
        cache.put("one", 1)
        cache.put("two", 2)
        cache.put("three", 3)
        
        // Access "one" to make it recently used
        cache.get("one")
        
        // Add new item - should evict "two" (least recently used)
        cache.put("four", 4)
        
        assertEquals(1, cache.get("one"))
        assertNull(cache.get("two"))
        assertEquals(listOf("two"), evictedKeys)
    }

    @Test
    fun `test containsKey operation`() = runTest {
        val cache = LruCache<String, Int>(maxSize = 3)
        
        cache.put("one", 1)
        
        assertTrue(cache.containsKey("one"))
        assertFalse(cache.containsKey("two"))
    }

    @Test
    fun `test remove operation`() = runTest {
        val cache = LruCache<String, Int>(maxSize = 3)
        
        cache.put("one", 1)
        cache.put("two", 2)
        
        assertEquals(1, cache.remove("one"))
        assertNull(cache.get("one"))
        assertEquals(1, cache.size())
    }

    @Test
    fun `test clear operation`() = runTest {
        val cache = LruCache<String, Int>(maxSize = 3)
        
        cache.put("one", 1)
        cache.put("two", 2)
        cache.put("three", 3)
        
        cache.clear()
        
        assertEquals(0, cache.size())
        assertNull(cache.get("one"))
    }

    @Test
    fun `test keys operation returns snapshot`() = runTest {
        val cache = LruCache<String, Int>(maxSize = 3)
        
        cache.put("one", 1)
        cache.put("two", 2)
        cache.put("three", 3)
        
        val keys = cache.keys()
        assertEquals(3, keys.size)
        assertTrue(keys.contains("one"))
        assertTrue(keys.contains("two"))
        assertTrue(keys.contains("three"))
    }

    @Test
    fun `test values operation returns snapshot`() = runTest {
        val cache = LruCache<String, Int>(maxSize = 3)
        
        cache.put("one", 1)
        cache.put("two", 2)
        cache.put("three", 3)
        
        val values = cache.values()
        assertEquals(3, values.size)
        assertTrue(values.contains(1))
        assertTrue(values.contains(2))
        assertTrue(values.contains(3))
    }

    @Test
    fun `test concurrent access is thread-safe`() = runTest {
        val cache = LruCache<Int, String>(maxSize = 100)
        
        // Launch 100 concurrent operations
        val jobs = (1..100).map { i ->
            async {
                cache.put(i, "value$i")
                cache.get(i)
            }
        }
        
        jobs.awaitAll()
        
        // Cache should have exactly 100 items (or maxSize if smaller)
        assertEquals(100, cache.size())
    }

    @Test
    fun `test withLock for atomic operations`() = runTest {
        val cache = LruCache<String, Int>(maxSize = 3)
        
        cache.put("one", 1)
        cache.put("two", 2)
        
        val sum = cache.withLock { map ->
            map.values.sum()
        }
        
        assertEquals(3, sum)
    }

    @Test
    fun `test put returns previous value`() = runTest {
        val cache = LruCache<String, Int>(maxSize = 3)
        
        assertNull(cache.put("one", 1))
        assertEquals(1, cache.put("one", 10))
        assertEquals(10, cache.get("one"))
    }

    @Test
    fun `test entries operation returns snapshot`() = runTest {
        val cache = LruCache<String, Int>(maxSize = 3)
        
        cache.put("one", 1)
        cache.put("two", 2)
        
        val entries = cache.entries()
        assertEquals(2, entries.size)
        assertEquals(1, entries["one"])
        assertEquals(2, entries["two"])
    }
}
