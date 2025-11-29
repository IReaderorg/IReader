package ireader.domain.models.entities

import kotlin.test.*

/**
 * Unit tests for Category entity and related functions
 */
class CategoryTest {

    // ==================== Category Creation Tests ====================

    @Test
    fun `category default values are correct`() {
        val category = Category()
        
        assertEquals(0L, category.id)
        assertEquals("", category.name)
        assertEquals(0L, category.order)
        assertEquals(0L, category.flags)
    }

    @Test
    fun `category with all fields set`() {
        val category = Category(
            id = 5L,
            name = "Favorites",
            order = 1L,
            flags = 2L
        )
        
        assertEquals(5L, category.id)
        assertEquals("Favorites", category.name)
        assertEquals(1L, category.order)
        assertEquals(2L, category.flags)
    }

    // ==================== System Category Tests ====================

    @Test
    fun `ALL_ID constant is correct`() {
        assertEquals(0L, Category.ALL_ID)
    }

    @Test
    fun `UNCATEGORIZED_ID constant is correct`() {
        assertEquals(-1L, Category.UNCATEGORIZED_ID)
    }

    @Test
    fun `isSystemCategory returns true for ALL category`() {
        val allCategory = Category(id = Category.ALL_ID, name = "All")
        
        assertTrue(allCategory.isSystemCategory)
    }

    @Test
    fun `isSystemCategory returns true for UNCATEGORIZED category`() {
        val uncategorized = Category(id = Category.UNCATEGORIZED_ID, name = "Uncategorized")
        
        assertTrue(uncategorized.isSystemCategory)
    }

    @Test
    fun `isSystemCategory returns false for user category`() {
        val userCategory = Category(id = 1L, name = "My Category")
        
        assertFalse(userCategory.isSystemCategory)
    }

    @Test
    fun `isSystemCategory returns false for negative non-uncategorized id`() {
        val category = Category(id = -2L, name = "Other")
        
        assertFalse(category.isSystemCategory)
    }

    // ==================== Base Categories Tests ====================

    @Test
    fun `baseCategories contains ALL and UNCATEGORIZED`() {
        val baseCategories = Category.baseCategories
        
        assertEquals(2, baseCategories.size)
        
        val allCategory = baseCategories.find { it.id == Category.ALL_ID }
        assertNotNull(allCategory)
        assertEquals("All", allCategory.name)
        
        val uncategorized = baseCategories.find { it.id == Category.UNCATEGORIZED_ID }
        assertNotNull(uncategorized)
        assertEquals("Uncategorized", uncategorized.name)
    }

    @Test
    fun `baseCategories have correct order`() {
        val baseCategories = Category.baseCategories
        
        baseCategories.forEach { category ->
            assertEquals(0L, category.order)
        }
    }

    @Test
    fun `baseCategories have zero flags`() {
        val baseCategories = Category.baseCategories
        
        baseCategories.forEach { category ->
            assertEquals(0L, category.flags)
        }
    }

    // ==================== BookCategory Tests ====================

    @Test
    fun `BookCategory creation`() {
        val bookCategory = BookCategory(bookId = 10L, categoryId = 5L)
        
        assertEquals(10L, bookCategory.bookId)
        assertEquals(5L, bookCategory.categoryId)
    }

    @Test
    fun `toBookCategory converts category to list of BookCategory`() {
        val category = Category(id = 3L, name = "Test Category")
        val bookIds = listOf(1L, 2L, 3L)
        
        val bookCategories = category.toBookCategory(bookIds)
        
        assertEquals(3, bookCategories.size)
        bookCategories.forEach { bc ->
            assertEquals(3L, bc.categoryId)
        }
        assertEquals(1L, bookCategories[0].bookId)
        assertEquals(2L, bookCategories[1].bookId)
        assertEquals(3L, bookCategories[2].bookId)
    }

    @Test
    fun `toBookCategory with empty book list`() {
        val category = Category(id = 1L, name = "Empty")
        
        val bookCategories = category.toBookCategory(emptyList())
        
        assertTrue(bookCategories.isEmpty())
    }

    @Test
    fun `toBookCategory with single book`() {
        val category = Category(id = 2L, name = "Single")
        
        val bookCategories = category.toBookCategory(listOf(100L))
        
        assertEquals(1, bookCategories.size)
        assertEquals(100L, bookCategories[0].bookId)
        assertEquals(2L, bookCategories[0].categoryId)
    }

    // ==================== CategoryUpdate Tests ====================

    @Test
    fun `CategoryUpdate with all fields`() {
        val update = CategoryUpdate(
            id = 5L,
            name = "Updated Name",
            order = 10L,
            flags = 3L
        )
        
        assertEquals(5L, update.id)
        assertEquals("Updated Name", update.name)
        assertEquals(10L, update.order)
        assertEquals(3L, update.flags)
    }

    @Test
    fun `CategoryUpdate with only id`() {
        val update = CategoryUpdate(id = 5L)
        
        assertEquals(5L, update.id)
        assertNull(update.name)
        assertNull(update.order)
        assertNull(update.flags)
    }

    @Test
    fun `CategoryUpdate with partial fields`() {
        val update = CategoryUpdate(
            id = 5L,
            name = "New Name"
        )
        
        assertEquals(5L, update.id)
        assertEquals("New Name", update.name)
        assertNull(update.order)
        assertNull(update.flags)
    }

    // ==================== Edge Cases ====================

    @Test
    fun `category with very long name`() {
        val longName = "A".repeat(500)
        val category = Category(id = 1L, name = longName)
        
        assertEquals(500, category.name.length)
    }

    @Test
    fun `category with special characters in name`() {
        val specialName = "My Category! @#\$%^&*() 日本語"
        val category = Category(id = 1L, name = specialName)
        
        assertEquals(specialName, category.name)
    }

    @Test
    fun `category with unicode name`() {
        val unicodeName = "カテゴリー 📚 Категория"
        val category = Category(id = 1L, name = unicodeName)
        
        assertEquals(unicodeName, category.name)
    }

    @Test
    fun `category equality`() {
        val category1 = Category(id = 1L, name = "Test", order = 0L, flags = 0L)
        val category2 = Category(id = 1L, name = "Test", order = 0L, flags = 0L)
        val category3 = Category(id = 2L, name = "Test", order = 0L, flags = 0L)
        
        assertEquals(category1, category2)
        assertNotEquals(category1, category3)
    }

    @Test
    fun `category copy creates new instance`() {
        val original = Category(id = 1L, name = "Original", order = 1L, flags = 0L)
        val copy = original.copy(name = "Copy")
        
        assertEquals("Original", original.name)
        assertEquals("Copy", copy.name)
        assertEquals(original.id, copy.id)
        assertEquals(original.order, copy.order)
    }

    @Test
    fun `multiple categories with same name but different ids`() {
        val cat1 = Category(id = 1L, name = "Favorites")
        val cat2 = Category(id = 2L, name = "Favorites")
        
        assertNotEquals(cat1, cat2)
        assertEquals(cat1.name, cat2.name)
    }
}
