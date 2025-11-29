package ireader.domain.utils

import kotlin.test.*

/**
 * Unit tests for FastMappers utility functions
 * Tests optimized list operations that avoid iterator allocation
 */
class FastMappersTest {

    // ==================== fastForEach Tests ====================

    @Test
    fun `fastForEach iterates through all elements`() {
        val list = listOf(1, 2, 3, 4, 5)
        val result = mutableListOf<Int>()
        
        list.fastForEach { result.add(it) }
        
        assertEquals(listOf(1, 2, 3, 4, 5), result)
    }

    @Test
    fun `fastForEach handles empty list`() {
        val list = emptyList<Int>()
        var count = 0
        
        list.fastForEach { count++ }
        
        assertEquals(0, count)
    }

    @Test
    fun `fastForEach handles single element list`() {
        val list = listOf("single")
        val result = mutableListOf<String>()
        
        list.fastForEach { result.add(it) }
        
        assertEquals(listOf("single"), result)
    }

    // ==================== fastForEachReversed Tests ====================

    @Test
    fun `fastForEachReversed iterates in reverse order`() {
        val list = listOf(1, 2, 3, 4, 5)
        val result = mutableListOf<Int>()
        
        list.fastForEachReversed { result.add(it) }
        
        assertEquals(listOf(5, 4, 3, 2, 1), result)
    }

    @Test
    fun `fastForEachReversed handles empty list`() {
        val list = emptyList<Int>()
        var count = 0
        
        list.fastForEachReversed { count++ }
        
        assertEquals(0, count)
    }

    @Test
    fun `fastForEachReversed handles single element`() {
        val list = listOf("only")
        val result = mutableListOf<String>()
        
        list.fastForEachReversed { result.add(it) }
        
        assertEquals(listOf("only"), result)
    }

    // ==================== fastForEachIndexed Tests ====================

    @Test
    fun `fastForEachIndexed provides correct indices`() {
        val list = listOf("a", "b", "c")
        val indices = mutableListOf<Int>()
        val values = mutableListOf<String>()
        
        list.fastForEachIndexed { index, value ->
            indices.add(index)
            values.add(value)
        }
        
        assertEquals(listOf(0, 1, 2), indices)
        assertEquals(listOf("a", "b", "c"), values)
    }

    @Test
    fun `fastForEachIndexed handles empty list`() {
        val list = emptyList<String>()
        var count = 0
        
        list.fastForEachIndexed { _, _ -> count++ }
        
        assertEquals(0, count)
    }

    // ==================== fastAll Tests ====================

    @Test
    fun `fastAll returns true when all elements match`() {
        val list = listOf(2, 4, 6, 8, 10)
        
        val result = list.fastAll { it % 2 == 0 }
        
        assertTrue(result)
    }

    @Test
    fun `fastAll returns false when one element does not match`() {
        val list = listOf(2, 4, 5, 8, 10)
        
        val result = list.fastAll { it % 2 == 0 }
        
        assertFalse(result)
    }

    @Test
    fun `fastAll returns true for empty list`() {
        val list = emptyList<Int>()
        
        val result = list.fastAll { it > 0 }
        
        assertTrue(result)
    }

    // ==================== fastAny Tests ====================

    @Test
    fun `fastAny returns true when at least one element matches`() {
        val list = listOf(1, 3, 5, 6, 7)
        
        val result = list.fastAny { it % 2 == 0 }
        
        assertTrue(result)
    }

    @Test
    fun `fastAny returns false when no elements match`() {
        val list = listOf(1, 3, 5, 7, 9)
        
        val result = list.fastAny { it % 2 == 0 }
        
        assertFalse(result)
    }

    @Test
    fun `fastAny returns false for empty list`() {
        val list = emptyList<Int>()
        
        val result = list.fastAny { it > 0 }
        
        assertFalse(result)
    }

    // ==================== fastFirstOrNull Tests ====================

    @Test
    fun `fastFirstOrNull returns first matching element`() {
        val list = listOf(1, 2, 3, 4, 5)
        
        val result = list.fastFirstOrNull { it > 2 }
        
        assertEquals(3, result)
    }

    @Test
    fun `fastFirstOrNull returns null when no match`() {
        val list = listOf(1, 2, 3)
        
        val result = list.fastFirstOrNull { it > 10 }
        
        assertNull(result)
    }

    @Test
    fun `fastFirstOrNull returns null for empty list`() {
        val list = emptyList<Int>()
        
        val result = list.fastFirstOrNull { it > 0 }
        
        assertNull(result)
    }

    // ==================== fastSumBy Tests ====================

    @Test
    fun `fastSumBy calculates correct sum`() {
        val list = listOf("a", "bb", "ccc")
        
        val result = list.fastSumBy { it.length }
        
        assertEquals(6, result)
    }

    @Test
    fun `fastSumBy returns zero for empty list`() {
        val list = emptyList<String>()
        
        val result = list.fastSumBy { it.length }
        
        assertEquals(0, result)
    }

    @Test
    fun `fastSumBy handles negative values`() {
        val list = listOf(1, -2, 3, -4, 5)
        
        val result = list.fastSumBy { it }
        
        assertEquals(3, result)
    }

    // ==================== fastMap Tests ====================

    @Test
    fun `fastMap transforms all elements`() {
        val list = listOf(1, 2, 3)
        
        val result = list.fastMap { it * 2 }
        
        assertEquals(listOf(2, 4, 6), result)
    }

    @Test
    fun `fastMap returns empty list for empty input`() {
        val list = emptyList<Int>()
        
        val result = list.fastMap { it * 2 }
        
        assertTrue(result.isEmpty())
    }

    @Test
    fun `fastMap can change element types`() {
        val list = listOf(1, 2, 3)
        
        val result = list.fastMap { "Item $it" }
        
        assertEquals(listOf("Item 1", "Item 2", "Item 3"), result)
    }

    // ==================== fastMaxBy Tests ====================

    @Test
    fun `fastMaxBy returns element with maximum value`() {
        val list = listOf("a", "bbb", "cc")
        
        val result = list.fastMaxBy { it.length }
        
        assertEquals("bbb", result)
    }

    @Test
    fun `fastMaxBy returns null for empty list`() {
        val list = emptyList<String>()
        
        val result = list.fastMaxBy { it.length }
        
        assertNull(result)
    }

    @Test
    fun `fastMaxBy returns first element when all equal`() {
        val list = listOf("aa", "bb", "cc")
        
        val result = list.fastMaxBy { it.length }
        
        assertEquals("aa", result)
    }

    // ==================== fastMapTo Tests ====================

    @Test
    fun `fastMapTo appends to existing collection`() {
        val list = listOf(1, 2, 3)
        val destination = mutableListOf(0)
        
        list.fastMapTo(destination) { it * 10 }
        
        assertEquals(listOf(0, 10, 20, 30), destination)
    }

    @Test
    fun `fastMapTo works with empty source`() {
        val list = emptyList<Int>()
        val destination = mutableListOf(1, 2)
        
        list.fastMapTo(destination) { it * 10 }
        
        assertEquals(listOf(1, 2), destination)
    }

    @Test
    fun `fastMapTo returns the destination collection`() {
        val list = listOf(1, 2)
        val destination = mutableListOf<Int>()
        
        val result = list.fastMapTo(destination) { it }
        
        assertSame(destination, result)
    }

    // ==================== removeIf Tests ====================

    @Test
    fun `removeIf removes matching elements`() {
        val list = mutableListOf(1, 2, 3, 4, 5)
        
        list.removeIf { it % 2 == 0 }
        
        assertEquals(listOf(1, 3, 5), list)
    }

    @Test
    fun `removeIf handles empty list`() {
        val list = mutableListOf<Int>()
        
        list.removeIf { it > 0 }
        
        assertTrue(list.isEmpty())
    }

    @Test
    fun `removeIf removes all when all match`() {
        val list = mutableListOf(2, 4, 6)
        
        list.removeIf { it % 2 == 0 }
        
        assertTrue(list.isEmpty())
    }

    @Test
    fun `removeIf removes none when none match`() {
        val list = mutableListOf(1, 3, 5)
        
        list.removeIf { it % 2 == 0 }
        
        assertEquals(listOf(1, 3, 5), list)
    }
}
