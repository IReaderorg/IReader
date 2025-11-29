package ireader.domain.models.entities

import kotlin.test.*

/**
 * Unit tests for SmartCategory sealed class
 */
class SmartCategoryTest {

    // ==================== SmartCategory Constants Tests ====================

    @Test
    fun `SmartCategory IDs are unique negative values`() {
        val ids = listOf(
            SmartCategory.CURRENTLY_READING_ID,
            SmartCategory.RECENTLY_ADDED_ID,
            SmartCategory.COMPLETED_ID,
            SmartCategory.UNREAD_ID,
            SmartCategory.ARCHIVED_ID
        )
        
        // All IDs should be unique
        assertEquals(ids.size, ids.distinct().size)
        
        // All IDs should be negative (to avoid conflicts with user categories)
        assertTrue(ids.all { it < 0 })
    }

    @Test
    fun `SmartCategory IDs do not conflict with system categories`() {
        val smartCategoryIds = listOf(
            SmartCategory.CURRENTLY_READING_ID,
            SmartCategory.RECENTLY_ADDED_ID,
            SmartCategory.COMPLETED_ID,
            SmartCategory.UNREAD_ID,
            SmartCategory.ARCHIVED_ID
        )
        
        // Should not conflict with Category.ALL_ID (0) or Category.UNCATEGORIZED_ID (-1)
        assertFalse(smartCategoryIds.contains(Category.ALL_ID))
        assertFalse(smartCategoryIds.contains(Category.UNCATEGORIZED_ID))
    }

    // ==================== SmartCategory Object Tests ====================

    @Test
    fun `CurrentlyReading has correct properties`() {
        val category = SmartCategory.CurrentlyReading
        
        assertEquals(SmartCategory.CURRENTLY_READING_ID, category.id)
        assertEquals("Currently Reading", category.name)
        assertEquals("AutoStories", category.iconName)
        assertTrue(category.isSystemCategory)
    }

    @Test
    fun `RecentlyAdded has correct properties`() {
        val category = SmartCategory.RecentlyAdded
        
        assertEquals(SmartCategory.RECENTLY_ADDED_ID, category.id)
        assertEquals("Recently Added", category.name)
        assertEquals("NewReleases", category.iconName)
        assertTrue(category.isSystemCategory)
    }

    @Test
    fun `Completed has correct properties`() {
        val category = SmartCategory.Completed
        
        assertEquals(SmartCategory.COMPLETED_ID, category.id)
        assertEquals("Completed", category.name)
        assertEquals("CheckCircle", category.iconName)
        assertTrue(category.isSystemCategory)
    }

    @Test
    fun `Unread has correct properties`() {
        val category = SmartCategory.Unread
        
        assertEquals(SmartCategory.UNREAD_ID, category.id)
        assertEquals("Unread", category.name)
        assertEquals("Circle", category.iconName)
        assertTrue(category.isSystemCategory)
    }

    @Test
    fun `Archived has correct properties`() {
        val category = SmartCategory.Archived
        
        assertEquals(SmartCategory.ARCHIVED_ID, category.id)
        assertEquals("Archived", category.name)
        assertEquals("Archive", category.iconName)
        assertTrue(category.isSystemCategory)
    }

    // ==================== all() Tests ====================

    @Test
    fun `all returns all smart categories`() {
        val allCategories = SmartCategory.all()
        
        assertEquals(5, allCategories.size)
        assertTrue(allCategories.contains(SmartCategory.CurrentlyReading))
        assertTrue(allCategories.contains(SmartCategory.RecentlyAdded))
        assertTrue(allCategories.contains(SmartCategory.Completed))
        assertTrue(allCategories.contains(SmartCategory.Unread))
        assertTrue(allCategories.contains(SmartCategory.Archived))
    }

    @Test
    fun `all returns categories in expected order`() {
        val allCategories = SmartCategory.all()
        
        assertEquals(SmartCategory.CurrentlyReading, allCategories[0])
        assertEquals(SmartCategory.RecentlyAdded, allCategories[1])
        assertEquals(SmartCategory.Completed, allCategories[2])
        assertEquals(SmartCategory.Unread, allCategories[3])
        assertEquals(SmartCategory.Archived, allCategories[4])
    }

    // ==================== isSmartCategory() Tests ====================

    @Test
    fun `isSmartCategory returns true for smart category IDs`() {
        assertTrue(SmartCategory.isSmartCategory(SmartCategory.CURRENTLY_READING_ID))
        assertTrue(SmartCategory.isSmartCategory(SmartCategory.RECENTLY_ADDED_ID))
        assertTrue(SmartCategory.isSmartCategory(SmartCategory.COMPLETED_ID))
        assertTrue(SmartCategory.isSmartCategory(SmartCategory.UNREAD_ID))
        assertTrue(SmartCategory.isSmartCategory(SmartCategory.ARCHIVED_ID))
    }

    @Test
    fun `isSmartCategory returns false for non-smart category IDs`() {
        assertFalse(SmartCategory.isSmartCategory(0L)) // ALL_ID
        assertFalse(SmartCategory.isSmartCategory(-1L)) // UNCATEGORIZED_ID
        assertFalse(SmartCategory.isSmartCategory(1L)) // User category
        assertFalse(SmartCategory.isSmartCategory(100L)) // User category
        assertFalse(SmartCategory.isSmartCategory(-100L)) // Random negative
    }

    // ==================== getById() Tests ====================

    @Test
    fun `getById returns correct category for valid ID`() {
        assertEquals(SmartCategory.CurrentlyReading, SmartCategory.getById(SmartCategory.CURRENTLY_READING_ID))
        assertEquals(SmartCategory.RecentlyAdded, SmartCategory.getById(SmartCategory.RECENTLY_ADDED_ID))
        assertEquals(SmartCategory.Completed, SmartCategory.getById(SmartCategory.COMPLETED_ID))
        assertEquals(SmartCategory.Unread, SmartCategory.getById(SmartCategory.UNREAD_ID))
        assertEquals(SmartCategory.Archived, SmartCategory.getById(SmartCategory.ARCHIVED_ID))
    }

    @Test
    fun `getById returns null for invalid ID`() {
        assertNull(SmartCategory.getById(0L))
        assertNull(SmartCategory.getById(-1L))
        assertNull(SmartCategory.getById(1L))
        assertNull(SmartCategory.getById(-100L))
    }

    // ==================== toCategoryWithCount() Tests ====================

    @Test
    fun `toCategoryWithCount creates correct CategoryWithCount`() {
        val smartCategory = SmartCategory.CurrentlyReading
        
        val categoryWithCount = smartCategory.toCategoryWithCount(42)
        
        assertEquals(SmartCategory.CURRENTLY_READING_ID, categoryWithCount.category.id)
        assertEquals("Currently Reading", categoryWithCount.category.name)
        assertEquals(42, categoryWithCount.bookCount)
        assertEquals(0L, categoryWithCount.category.order)
        assertEquals(0L, categoryWithCount.category.flags)
    }

    @Test
    fun `toCategoryWithCount with zero books`() {
        val smartCategory = SmartCategory.Unread
        
        val categoryWithCount = smartCategory.toCategoryWithCount(0)
        
        assertEquals(0, categoryWithCount.bookCount)
    }

    @Test
    fun `toCategoryWithCount with large book count`() {
        val smartCategory = SmartCategory.Completed
        
        val categoryWithCount = smartCategory.toCategoryWithCount(10000)
        
        assertEquals(10000, categoryWithCount.bookCount)
    }

    // ==================== Singleton Tests ====================

    @Test
    fun `SmartCategory objects are singletons`() {
        val reading1 = SmartCategory.CurrentlyReading
        val reading2 = SmartCategory.CurrentlyReading
        
        assertSame(reading1, reading2)
    }

    @Test
    fun `getById returns same singleton instance`() {
        val fromAll = SmartCategory.all().find { it.id == SmartCategory.COMPLETED_ID }
        val fromGetById = SmartCategory.getById(SmartCategory.COMPLETED_ID)
        
        assertSame(fromAll, fromGetById)
    }

    // ==================== When Expression Tests ====================

    @Test
    fun `when expression handles all SmartCategory types`() {
        fun getCategoryDescription(category: SmartCategory): String {
            return when (category) {
                is SmartCategory.CurrentlyReading -> "Books you're reading"
                is SmartCategory.RecentlyAdded -> "New additions"
                is SmartCategory.Completed -> "Finished books"
                is SmartCategory.Unread -> "Books to start"
                is SmartCategory.Archived -> "Archived books"
            }
        }
        
        assertEquals("Books you're reading", getCategoryDescription(SmartCategory.CurrentlyReading))
        assertEquals("New additions", getCategoryDescription(SmartCategory.RecentlyAdded))
        assertEquals("Finished books", getCategoryDescription(SmartCategory.Completed))
        assertEquals("Books to start", getCategoryDescription(SmartCategory.Unread))
        assertEquals("Archived books", getCategoryDescription(SmartCategory.Archived))
    }

    // ==================== Practical Usage Tests ====================

    @Test
    fun `filter smart categories by icon`() {
        val allCategories = SmartCategory.all()
        
        val checkmarkCategories = allCategories.filter { it.iconName.contains("Check") }
        
        assertEquals(1, checkmarkCategories.size)
        assertEquals(SmartCategory.Completed, checkmarkCategories[0])
    }

    @Test
    fun `map smart categories to display names`() {
        val displayNames = SmartCategory.all().map { it.name }
        
        assertEquals(
            listOf("Currently Reading", "Recently Added", "Completed", "Unread", "Archived"),
            displayNames
        )
    }
}
