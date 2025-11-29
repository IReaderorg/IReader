package ireader.domain.services

import kotlin.test.*

/**
 * Unit tests for CacheStats data class
 */
class CacheStatsTest {

    // ==================== Hit Rate Tests ====================

    @Test
    fun `hitRate calculates correctly with hits and misses`() {
        val stats = CacheStats(
            cachedChapters = 10,
            maxCapacity = 100,
            memoryUsedBytes = 1024L,
            maxMemoryBytes = 10240L,
            hitCount = 80L,
            missCount = 20L,
            evictionCount = 5L
        )
        
        assertEquals(0.8f, stats.hitRate, 0.001f)
    }

    @Test
    fun `hitRate returns zero when no requests`() {
        val stats = CacheStats(
            cachedChapters = 0,
            maxCapacity = 100,
            memoryUsedBytes = 0L,
            maxMemoryBytes = 10240L,
            hitCount = 0L,
            missCount = 0L,
            evictionCount = 0L
        )
        
        assertEquals(0f, stats.hitRate)
    }

    @Test
    fun `hitRate is 1 when all hits`() {
        val stats = CacheStats(
            cachedChapters = 10,
            maxCapacity = 100,
            memoryUsedBytes = 1024L,
            maxMemoryBytes = 10240L,
            hitCount = 100L,
            missCount = 0L,
            evictionCount = 0L
        )
        
        assertEquals(1f, stats.hitRate)
    }

    @Test
    fun `hitRate is 0 when all misses`() {
        val stats = CacheStats(
            cachedChapters = 0,
            maxCapacity = 100,
            memoryUsedBytes = 0L,
            maxMemoryBytes = 10240L,
            hitCount = 0L,
            missCount = 50L,
            evictionCount = 0L
        )
        
        assertEquals(0f, stats.hitRate)
    }

    // ==================== Memory Usage Tests ====================

    @Test
    fun `memoryUsedMB calculates correctly`() {
        val stats = CacheStats(
            cachedChapters = 10,
            maxCapacity = 100,
            memoryUsedBytes = 1024L * 1024L, // 1 MB
            maxMemoryBytes = 10L * 1024L * 1024L,
            hitCount = 0L,
            missCount = 0L,
            evictionCount = 0L
        )
        
        assertEquals(1f, stats.memoryUsedMB, 0.001f)
    }

    @Test
    fun `memoryUsedMB handles zero bytes`() {
        val stats = CacheStats(
            cachedChapters = 0,
            maxCapacity = 100,
            memoryUsedBytes = 0L,
            maxMemoryBytes = 10240L,
            hitCount = 0L,
            missCount = 0L,
            evictionCount = 0L
        )
        
        assertEquals(0f, stats.memoryUsedMB)
    }

    @Test
    fun `memoryUsedMB handles fractional MB`() {
        val stats = CacheStats(
            cachedChapters = 10,
            maxCapacity = 100,
            memoryUsedBytes = 512L * 1024L, // 0.5 MB
            maxMemoryBytes = 10240L,
            hitCount = 0L,
            missCount = 0L,
            evictionCount = 0L
        )
        
        assertEquals(0.5f, stats.memoryUsedMB, 0.001f)
    }

    @Test
    fun `maxMemoryMB calculates correctly`() {
        val stats = CacheStats(
            cachedChapters = 10,
            maxCapacity = 100,
            memoryUsedBytes = 1024L,
            maxMemoryBytes = 50L * 1024L * 1024L, // 50 MB
            hitCount = 0L,
            missCount = 0L,
            evictionCount = 0L
        )
        
        assertEquals(50f, stats.maxMemoryMB, 0.001f)
    }

    // ==================== Utilization Tests ====================

    @Test
    fun `utilizationPercent calculates correctly`() {
        val stats = CacheStats(
            cachedChapters = 25,
            maxCapacity = 100,
            memoryUsedBytes = 1024L,
            maxMemoryBytes = 10240L,
            hitCount = 0L,
            missCount = 0L,
            evictionCount = 0L
        )
        
        assertEquals(25f, stats.utilizationPercent, 0.001f)
    }

    @Test
    fun `utilizationPercent returns zero when maxCapacity is zero`() {
        val stats = CacheStats(
            cachedChapters = 0,
            maxCapacity = 0,
            memoryUsedBytes = 0L,
            maxMemoryBytes = 10240L,
            hitCount = 0L,
            missCount = 0L,
            evictionCount = 0L
        )
        
        assertEquals(0f, stats.utilizationPercent)
    }

    @Test
    fun `utilizationPercent is 100 when cache is full`() {
        val stats = CacheStats(
            cachedChapters = 50,
            maxCapacity = 50,
            memoryUsedBytes = 1024L,
            maxMemoryBytes = 10240L,
            hitCount = 0L,
            missCount = 0L,
            evictionCount = 0L
        )
        
        assertEquals(100f, stats.utilizationPercent, 0.001f)
    }

    @Test
    fun `utilizationPercent handles fractional values`() {
        val stats = CacheStats(
            cachedChapters = 33,
            maxCapacity = 100,
            memoryUsedBytes = 1024L,
            maxMemoryBytes = 10240L,
            hitCount = 0L,
            missCount = 0L,
            evictionCount = 0L
        )
        
        assertEquals(33f, stats.utilizationPercent, 0.001f)
    }

    // ==================== Edge Cases ====================

    @Test
    fun `stats with large values`() {
        val stats = CacheStats(
            cachedChapters = 10000,
            maxCapacity = 10000,
            memoryUsedBytes = 1024L * 1024L * 1024L, // 1 GB
            maxMemoryBytes = 2L * 1024L * 1024L * 1024L, // 2 GB
            hitCount = Long.MAX_VALUE / 2,
            missCount = Long.MAX_VALUE / 4,
            evictionCount = 1000000L
        )
        
        assertEquals(1024f, stats.memoryUsedMB, 0.001f)
        assertEquals(2048f, stats.maxMemoryMB, 0.001f)
        assertEquals(100f, stats.utilizationPercent, 0.001f)
    }

    @Test
    fun `stats equality`() {
        val stats1 = CacheStats(
            cachedChapters = 10,
            maxCapacity = 100,
            memoryUsedBytes = 1024L,
            maxMemoryBytes = 10240L,
            hitCount = 50L,
            missCount = 10L,
            evictionCount = 5L
        )
        
        val stats2 = CacheStats(
            cachedChapters = 10,
            maxCapacity = 100,
            memoryUsedBytes = 1024L,
            maxMemoryBytes = 10240L,
            hitCount = 50L,
            missCount = 10L,
            evictionCount = 5L
        )
        
        assertEquals(stats1, stats2)
    }

    @Test
    fun `stats copy creates new instance`() {
        val original = CacheStats(
            cachedChapters = 10,
            maxCapacity = 100,
            memoryUsedBytes = 1024L,
            maxMemoryBytes = 10240L,
            hitCount = 50L,
            missCount = 10L,
            evictionCount = 5L
        )
        
        val copy = original.copy(cachedChapters = 20)
        
        assertEquals(10, original.cachedChapters)
        assertEquals(20, copy.cachedChapters)
        assertEquals(original.maxCapacity, copy.maxCapacity)
    }

    @Test
    fun `stats with minimum values`() {
        val stats = CacheStats(
            cachedChapters = 0,
            maxCapacity = 1,
            memoryUsedBytes = 0L,
            maxMemoryBytes = 1L,
            hitCount = 0L,
            missCount = 0L,
            evictionCount = 0L
        )
        
        assertEquals(0f, stats.hitRate)
        assertEquals(0f, stats.memoryUsedMB)
        assertEquals(0f, stats.utilizationPercent)
    }
}
