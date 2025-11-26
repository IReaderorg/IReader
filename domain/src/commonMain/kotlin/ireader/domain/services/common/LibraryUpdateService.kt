package ireader.domain.services.common

import ireader.domain.models.entities.Book
import kotlinx.coroutines.flow.StateFlow

/**
 * Common library update service for both Android and Desktop
 */
interface LibraryUpdateService : PlatformService {
    /**
     * Current update state
     */
    val state: StateFlow<ServiceState>
    
    /**
     * Books being updated
     */
    val updatingBooks: StateFlow<List<Book>>
    
    /**
     * Update progress (bookId -> progress)
     */
    val updateProgress: StateFlow<Map<Long, UpdateProgress>>
    
    /**
     * Update all books in library
     */
    suspend fun updateLibrary(
        categoryIds: List<Long>? = null,
        showNotification: Boolean = true
    ): ServiceResult<UpdateResult>
    
    /**
     * Update specific books
     */
    suspend fun updateBooks(
        bookIds: List<Long>,
        showNotification: Boolean = true
    ): ServiceResult<UpdateResult>
    
    /**
     * Cancel ongoing update
     */
    suspend fun cancelUpdate(): ServiceResult<Unit>
    
    /**
     * Schedule automatic library updates
     */
    suspend fun scheduleAutoUpdate(
        intervalHours: Int,
        constraints: TaskConstraints = TaskConstraints()
    ): ServiceResult<String>
    
    /**
     * Cancel scheduled auto-update
     */
    suspend fun cancelAutoUpdate(): ServiceResult<Unit>
}

/**
 * Update progress for a single book
 */
data class UpdateProgress(
    val bookId: Long,
    val bookTitle: String = "",
    val status: UpdateStatus = UpdateStatus.QUEUED,
    val newChaptersCount: Int = 0,
    val errorMessage: String? = null
)

/**
 * Update status enum
 */
enum class UpdateStatus {
    QUEUED,
    UPDATING,
    COMPLETED,
    FAILED,
    SKIPPED
}

/**
 * Update result summary
 */
data class UpdateResult(
    val totalBooks: Int,
    val updatedBooks: Int,
    val failedBooks: Int,
    val newChapters: Int,
    val errors: List<String> = emptyList()
)
