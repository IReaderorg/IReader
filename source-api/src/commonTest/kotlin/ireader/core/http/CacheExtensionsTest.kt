package ireader.core.http

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CacheExtensionsTest {

    @Test
    fun cacheControlShouldHaveDefaultValues() {
        val cacheControl = CacheControl()

        assertTrue(cacheControl.useCache)
        assertNull(cacheControl.cacheDurationMs)
        assertFalse(cacheControl.forceRefresh)
    }

    @Test
    fun cacheControlShouldSetUseCache() {
        val cacheControl = CacheControl(useCache = false)

        assertFalse(cacheControl.useCache)
    }

    @Test
    fun cacheControlShouldSetCacheDuration() {
        val cacheControl = CacheControl(cacheDurationMs = 60000L)

        assertEquals(60000L, cacheControl.cacheDurationMs)
    }

    @Test
    fun cacheControlShouldSetForceRefresh() {
        val cacheControl = CacheControl(forceRefresh = true)

        assertTrue(cacheControl.forceRefresh)
    }

    @Test
    fun cacheControlShouldBeEqualWhenSameValues() {
        val control1 = CacheControl(useCache = true, cacheDurationMs = 1000L, forceRefresh = false)
        val control2 = CacheControl(useCache = true, cacheDurationMs = 1000L, forceRefresh = false)

        assertEquals(control1, control2)
    }

    @Test
    fun cacheControlShouldHaveCorrectHashCode() {
        val control1 = CacheControl(useCache = true, cacheDurationMs = 1000L)
        val control2 = CacheControl(useCache = true, cacheDurationMs = 1000L)

        assertEquals(control1.hashCode(), control2.hashCode())
    }

    @Test
    fun cacheControlAttributeShouldBeDefined() {
        assertNotNull(CacheControlAttribute)
        assertEquals("CacheControl", CacheControlAttribute.name)
    }

    @Test
    fun cacheControlWithAllOptions() {
        val cacheControl = CacheControl(
            useCache = false,
            cacheDurationMs = 30000L,
            forceRefresh = true
        )

        assertFalse(cacheControl.useCache)
        assertEquals(30000L, cacheControl.cacheDurationMs)
        assertTrue(cacheControl.forceRefresh)
    }

    @Test
    fun cacheControlCopyShouldWork() {
        val original = CacheControl(useCache = true, cacheDurationMs = 1000L)
        val copy = original.copy(useCache = false)

        assertTrue(original.useCache)
        assertFalse(copy.useCache)
        assertEquals(original.cacheDurationMs, copy.cacheDurationMs)
    }

    @Test
    fun cacheControlToStringShouldIncludeAllFields() {
        val cacheControl = CacheControl(useCache = true, cacheDurationMs = 5000L, forceRefresh = false)
        val toString = cacheControl.toString()

        assertTrue(toString.contains("useCache=true"))
        assertTrue(toString.contains("cacheDurationMs=5000"))
        assertTrue(toString.contains("forceRefresh=false"))
    }
}
