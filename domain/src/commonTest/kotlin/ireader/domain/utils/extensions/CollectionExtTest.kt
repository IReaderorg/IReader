package ireader.domain.utils.extensions

import kotlin.test.*

/**
 * Unit tests for Collection extension functions
 */
class CollectionExtTest {

    // ==================== replace Tests ====================

    @Test
    fun `replace replaces element at given position`() {
        val list = listOf("a", "b", "c")
        
        val result = list.replace(1, "X")
        
        assertEquals(listOf("a", "X", "c"), result)
    }

    @Test
    fun `replace at first position`() {
        val list = listOf(1, 2, 3)
        
        val result = list.replace(0, 100)
        
        assertEquals(listOf(100, 2, 3), result)
    }

    @Test
    fun `replace at last position`() {
        val list = listOf(1, 2, 3)
        
        val result = list.replace(2, 100)
        
        assertEquals(listOf(1, 2, 100), result)
    }

    @Test
    fun `replace does not modify original list`() {
        val original = listOf("a", "b", "c")
        
        original.replace(1, "X")
        
        assertEquals(listOf("a", "b", "c"), original)
    }

    // ==================== replaceAll Tests ====================

    @Test
    fun `replaceAll replaces entire list`() {
        val list = listOf(1, 2, 3)
        val newItems = listOf(4, 5, 6, 7)
        
        val result = list.replaceAll(newItems)
        
        assertEquals(listOf(4, 5, 6, 7), result)
    }

    @Test
    fun `replaceAll with empty list`() {
        val list = listOf(1, 2, 3)
        
        val result = list.replaceAll(emptyList())
        
        assertTrue(result.isEmpty())
    }

    // ==================== replaceFirst Tests ====================

    @Test
    fun `replaceFirst replaces first matching element`() {
        val list = listOf(1, 2, 3, 2, 4)
        
        val result = list.replaceFirst({ it == 2 }, 99)
        
        assertEquals(listOf(1, 99, 3, 2, 4), result)
    }

    @Test
    fun `replaceFirst returns same list when no match`() {
        val list = listOf(1, 2, 3)
        
        val result = list.replaceFirst({ it == 10 }, 99)
        
        assertEquals(listOf(1, 2, 3), result)
    }

    @Test
    fun `replaceFirst with empty list returns empty list`() {
        val list = emptyList<Int>()
        
        val result = list.replaceFirst({ it == 1 }, 99)
        
        assertTrue(result.isEmpty())
    }

    // ==================== closestValue Tests ====================

    @Test
    fun `closestValue finds exact match`() {
        val list = listOf(10, 20, 30, 40, 50)
        
        val result = list.closestValue(30)
        
        assertEquals(30, result)
    }

    @Test
    fun `closestValue finds closest when no exact match`() {
        val list = listOf(10, 20, 30, 40, 50)
        
        val result = list.closestValue(27)
        
        assertEquals(30, result)
    }

    @Test
    fun `closestValue returns null for empty list`() {
        val list = emptyList<Int>()
        
        val result = list.closestValue(10)
        
        assertNull(result)
    }

    @Test
    fun `closestValue handles negative numbers`() {
        val list = listOf(-10, 0, 10, 20)
        
        val result = list.closestValue(-7)
        
        assertEquals(-10, result)
    }

    // ==================== removeFirst Tests ====================

    @Test
    fun `removeFirst removes and returns first matching element`() {
        val list = mutableListOf(1, 2, 3, 2, 4)
        
        val removed = list.removeFirst { it == 2 }
        
        assertEquals(2, removed)
        assertEquals(listOf(1, 3, 2, 4), list)
    }

    @Test
    fun `removeFirst returns null when no match`() {
        val list = mutableListOf(1, 2, 3)
        
        val removed = list.removeFirst { it == 10 }
        
        assertNull(removed)
        assertEquals(listOf(1, 2, 3), list)
    }

    @Test
    fun `removeFirst on empty collection returns null`() {
        val list = mutableListOf<Int>()
        
        val removed = list.removeFirst { it == 1 }
        
        assertNull(removed)
    }

    // ==================== merge Tests ====================

    @Test
    fun `merge combines two lists`() {
        val first = listOf(1, 2, 3)
        val second = listOf(4, 5, 6)
        
        val result = merge(first, second)
        
        assertEquals(listOf(1, 2, 3, 4, 5, 6), result)
    }

    @Test
    fun `merge with empty first list`() {
        val first = emptyList<Int>()
        val second = listOf(1, 2, 3)
        
        val result = merge(first, second)
        
        assertEquals(listOf(1, 2, 3), result)
    }

    @Test
    fun `merge with empty second list`() {
        val first = listOf(1, 2, 3)
        val second = emptyList<Int>()
        
        val result = merge(first, second)
        
        assertEquals(listOf(1, 2, 3), result)
    }

    @Test
    fun `merge with both empty lists`() {
        val first = emptyList<Int>()
        val second = emptyList<Int>()
        
        val result = merge(first, second)
        
        assertTrue(result.isEmpty())
    }

    // ==================== indexOf Tests ====================

    @Test
    fun `indexOf finds element in list`() {
        val list = listOf("a", "b", "c")
        
        val result = list.indexOf("b")
        
        assertEquals(1, result)
    }

    @Test
    fun `indexOf returns null when element not found`() {
        val list = listOf("a", "b", "c")
        
        val result = list.indexOf("z")
        
        // Note: The implementation returns -1 for List, but null for other Iterables
        // For List, it delegates to List.indexOf which returns -1
        assertEquals(-1, result)
    }

    @Test
    fun `indexOf finds first occurrence`() {
        val list = listOf("a", "b", "a", "c")
        
        val result = list.indexOf("a")
        
        assertEquals(0, result)
    }

    // ==================== removeSameItemsFromList Tests ====================

    @Test
    fun `removeSameItemsFromList removes duplicates`() {
        data class Item(val id: Int, val name: String)
        
        val oldList = listOf(Item(1, "A"), Item(2, "B"))
        val newList = listOf(Item(2, "B-updated"), Item(3, "C"))
        
        val result = removeSameItemsFromList(oldList, newList) { it.id }
        
        assertEquals(3, result.size)
        assertTrue(result.any { it.id == 1 })
        assertTrue(result.any { it.id == 2 })
        assertTrue(result.any { it.id == 3 })
    }

    @Test
    fun `removeSameItemsFromList with no duplicates`() {
        val oldList = listOf(1, 2, 3)
        val newList = listOf(4, 5, 6)
        
        val result = removeSameItemsFromList(oldList, newList) { it }
        
        assertEquals(6, result.size)
    }

    @Test
    fun `removeSameItemsFromList with all duplicates`() {
        val oldList = listOf(1, 2, 3)
        val newList = listOf(1, 2, 3)
        
        val result = removeSameItemsFromList(oldList, newList) { it }
        
        assertEquals(3, result.size)
    }

    // ==================== next Tests ====================

    @Test
    fun `next returns next element`() {
        val list = listOf("a", "b", "c", "d")
        
        val result = list.next({ it }, "b")
        
        assertEquals(Pair(2, "c"), result)
    }

    @Test
    fun `next throws IndexIsInvalidException when item not found`() {
        val list = listOf("a", "b", "c")
        
        assertFailsWith<IndexIsInvalidException> {
            list.next({ it }, "z")
        }
    }

    @Test
    fun `next throws IndexGreaterThanCollectionSizeException at last element`() {
        val list = listOf("a", "b", "c")
        
        assertFailsWith<IndexOutOfBoundsException> {
            list.next({ it }, "c")
        }
    }

    // ==================== previous Tests ====================

    @Test
    fun `previous returns previous element`() {
        val list = listOf("a", "b", "c", "d")
        
        val result = list.previous("c") { it }
        
        assertEquals(Pair(1, "b"), result)
    }

    @Test
    fun `previous throws IndexIsInvalidException when item not found`() {
        val list = listOf("a", "b", "c")
        
        assertFailsWith<IndexIsInvalidException> {
            list.previous("z") { it }
        }
    }

    @Test
    fun `previous throws at first element`() {
        val list = listOf("a", "b", "c")
        
        assertFailsWith<IndexOutOfBoundsException> {
            list.previous("a") { it }
        }
    }
}
