package ireader.domain.models.library

import kotlinx.serialization.Serializable

/**
 * Library widget configuration
 */
@Serializable
data class LibraryWidgetConfig(
    val widgetId: Int,
    val widgetType: WidgetType = WidgetType.UPDATES_GRID,
    val categoryFilter: List<Long> = emptyList(), // Empty means all categories
    val maxItems: Int = 10,
    val showCover: Boolean = true,
    val showTitle: Boolean = true,
    val showUnreadCount: Boolean = true,
    val refreshInterval: Long = 60 * 60 * 1000L, // 1 hour in milliseconds
    val backgroundColor: Int = 0xFF000000.toInt(),
    val textColor: Int = 0xFFFFFFFF.toInt(),
    val accentColor: Int = 0xFF6200EE.toInt()
)

/**
 * Widget types
 */
enum class WidgetType {
    UPDATES_GRID,       // Grid of recently updated books
    READING_LIST,       // List of currently reading books
    FAVORITES,          // Grid of favorite books
    STATISTICS,         // Reading statistics display
    QUICK_ACCESS        // Quick access buttons
}

/**
 * Widget data for display
 */
@Serializable
data class LibraryWidgetData(
    val widgetId: Int,
    val items: List<WidgetItem>,
    val lastUpdated: Long = System.currentTimeMillis()
)

/**
 * Individual widget item
 */
@Serializable
data class WidgetItem(
    val bookId: Long,
    val title: String,
    val coverUrl: String?,
    val unreadCount: Int = 0,
    val lastReadChapter: String? = null,
    val progress: Float = 0f // 0.0 to 1.0
)

/**
 * Widget statistics data
 */
@Serializable
data class WidgetStatistics(
    val totalBooks: Int,
    val readingBooks: Int,
    val completedBooks: Int,
    val chaptersReadToday: Int,
    val readingStreak: Int,
    val totalReadingTime: Long // in milliseconds
)
