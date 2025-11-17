package ireader.domain.models.library

import kotlinx.serialization.Serializable

/**
 * Represents a library update job with scheduling and filtering options
 */
@Serializable
data class LibraryUpdateJob(
    val id: String,
    val categoryIds: List<Long> = emptyList(), // Empty means all categories
    val updateStrategy: UpdateStrategy = UpdateStrategy.ALWAYS_UPDATE,
    val skipCompleted: Boolean = false,
    val skipRead: Boolean = false,
    val onlyFavorites: Boolean = false,
    val sourceIds: List<Long> = emptyList(), // Empty means all sources
    val scheduledTime: Long = 0L, // 0 means immediate
    val isAutomatic: Boolean = false,
    val requiresWifi: Boolean = true,
    val requiresCharging: Boolean = false,
    val maxConcurrentUpdates: Int = 5
) {
    companion object {
        fun createImmediate(
            categoryIds: List<Long> = emptyList(),
            onlyFavorites: Boolean = false
        ) = LibraryUpdateJob(
            id = "immediate_${System.currentTimeMillis()}",
            categoryIds = categoryIds,
            onlyFavorites = onlyFavorites,
            isAutomatic = false
        )
        
        fun createScheduled(
            scheduledTime: Long,
            categoryIds: List<Long> = emptyList()
        ) = LibraryUpdateJob(
            id = "scheduled_${scheduledTime}",
            categoryIds = categoryIds,
            scheduledTime = scheduledTime,
            isAutomatic = true
        )
    }
}

/**
 * Update strategies for library updates
 */
enum class UpdateStrategy {
    ALWAYS_UPDATE,      // Always check for updates
    FETCH_ONCE,         // Only fetch if never fetched before
    SMART_UPDATE        // Update based on release patterns
}

/**
 * Library update progress information
 */
data class LibraryUpdateProgress(
    val jobId: String,
    val totalBooks: Int,
    val processedBooks: Int,
    val currentBookTitle: String = "",
    val newChaptersFound: Int = 0,
    val errors: List<LibraryUpdateError> = emptyList(),
    val isCompleted: Boolean = false,
    val isCancelled: Boolean = false
) {
    val progressPercentage: Float
        get() = if (totalBooks > 0) (processedBooks.toFloat() / totalBooks) * 100f else 0f
}

/**
 * Library update error information
 */
data class LibraryUpdateError(
    val bookId: Long,
    val bookTitle: String,
    val sourceId: Long,
    val error: String,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Library update result
 */
data class LibraryUpdateResult(
    val jobId: String,
    val totalBooks: Int,
    val updatedBooks: Int,
    val newChapters: Int,
    val skippedBooks: Int,
    val errors: List<LibraryUpdateError>,
    val duration: Long,
    val timestamp: Long = System.currentTimeMillis()
)