package ireader.domain.models.entities

/**
 * Smart categories are auto-populated categories based on reading status
 * They cannot be edited or deleted by users
 */
sealed class SmartCategory(
    val id: Long,
    val name: String,
    val isSystemCategory: Boolean = true
) {
    /**
     * Books with chapters read in the past 7 days and not completed
     */
    object CurrentlyReading : SmartCategory(
        id = CURRENTLY_READING_ID,
        name = "Currently Reading"
    )
    
    /**
     * Books added in the past 30 days
     */
    object RecentlyAdded : SmartCategory(
        id = RECENTLY_ADDED_ID,
        name = "Recently Added"
    )
    
    /**
     * Books where all chapters are marked as read
     */
    object Completed : SmartCategory(
        id = COMPLETED_ID,
        name = "Completed"
    )
    
    /**
     * Books with no chapters marked as read
     */
    object Unread : SmartCategory(
        id = UNREAD_ID,
        name = "Unread"
    )
    
    /**
     * Archived books
     */
    object Archived : SmartCategory(
        id = ARCHIVED_ID,
        name = "Archived"
    )
    
    companion object {
        // Use negative IDs to avoid conflicts with user categories
        const val CURRENTLY_READING_ID = -1L
        const val RECENTLY_ADDED_ID = -2L
        const val COMPLETED_ID = -3L
        const val UNREAD_ID = -4L
        const val ARCHIVED_ID = -5L
        
        /**
         * Get all smart categories
         */
        fun all(): List<SmartCategory> = listOf(
            CurrentlyReading,
            RecentlyAdded,
            Completed,
            Unread,
            Archived
        )
        
        /**
         * Check if a category ID is a smart category
         */
        fun isSmartCategory(id: Long): Boolean {
            return id in listOf(
                CURRENTLY_READING_ID,
                RECENTLY_ADDED_ID,
                COMPLETED_ID,
                UNREAD_ID,
                ARCHIVED_ID
            )
        }
        
        /**
         * Get smart category by ID
         */
        fun getById(id: Long): SmartCategory? {
            return all().find { it.id == id }
        }
    }
    
    /**
     * Convert to CategoryWithCount for display
     */
    fun toCategoryWithCount(bookCount: Int): CategoryWithCount {
        return CategoryWithCount(
            category = Category(
                id = id,
                name = name,
                order = 0,
                flags = 0,
            ),
            bookCount = bookCount
        )
    }
}
