package ireader.domain.models.migration

import ireader.domain.models.entities.BookItem
import ireader.domain.models.entities.Book
import ireader.domain.models.entities.Chapter

/**
 * Request to migrate a novel from one source to another
 */
data class MigrationRequest(
    val novelId: Long,
    val sourceId: Long,
    val targetSourceId: Long,
    val preserveProgress: Boolean = true,
    val flags: MigrationFlags = MigrationFlags()
)

/**
 * Migration flags for selective data transfer
 */
data class MigrationFlags(
    val chapters: Boolean = true,
    val bookmarks: Boolean = true,
    val categories: Boolean = true,
    val customCover: Boolean = true,
    val readingProgress: Boolean = true,
    val lastReadChapter: Boolean = true
)

/**
 * A potential match for a novel during migration
 */
data class MigrationMatch(
    val novel: BookItem,
    val confidenceScore: Float,
    val matchReason: String,
    val isAutoMatch: Boolean = false
)

/**
 * Result of a migration operation
 */
data class MigrationResult(
    val novelId: Long,
    val success: Boolean,
    val newNovelId: Long?,
    val error: String?,
    val transferredData: MigrationTransferredData? = null
)

/**
 * Data that was transferred during migration
 */
data class MigrationTransferredData(
    val chaptersTransferred: Int,
    val bookmarksTransferred: Int,
    val categoriesTransferred: Int,
    val progressPreserved: Boolean,
    val customCoverTransferred: Boolean
)

/**
 * Progress information for an ongoing migration
 */
data class MigrationProgress(
    val novelId: Long,
    val status: MigrationStatus,
    val progress: Float,
    val currentStep: String = "",
    val error: String? = null
)

/**
 * Status of a migration operation
 */
enum class MigrationStatus {
    PENDING,
    SEARCHING,
    MATCHING,
    TRANSFERRING_CHAPTERS,
    TRANSFERRING_BOOKMARKS,
    TRANSFERRING_CATEGORIES,
    TRANSFERRING_PROGRESS,
    TRANSFERRING_COVER,
    COMPLETED,
    FAILED
}

/**
 * History record of a migration
 */
data class MigrationHistory(
    val id: String,
    val oldBookId: Long,
    val newBookId: Long,
    val oldSourceId: Long,
    val newSourceId: Long,
    val timestamp: Long,
    val chaptersTransferred: Int,
    val progressPreserved: Boolean,
    val canRollback: Boolean = true,
    val flags: MigrationFlags,
    val transferredData: MigrationTransferredData
)

/**
 * Migration source configuration
 */
data class MigrationSource(
    val sourceId: Long,
    val sourceName: String,
    val isEnabled: Boolean = true,
    val priority: Int = 0
)

/**
 * Migration job for batch operations
 */
data class MigrationJob(
    val id: String,
    val books: List<Book>,
    val targetSources: List<MigrationSource>,
    val flags: MigrationFlags,
    val status: MigrationJobStatus = MigrationJobStatus.PENDING,
    val progress: Float = 0f,
    val completedBooks: Int = 0,
    val failedBooks: Int = 0,
    val startTime: Long? = null,
    val endTime: Long? = null
)

/**
 * Status of a migration job
 */
enum class MigrationJobStatus {
    PENDING,
    RUNNING,
    PAUSED,
    COMPLETED,
    CANCELLED,
    FAILED
}

/**
 * Migration search result
 */
data class MigrationSearchResult(
    val sourceId: Long,
    val sourceName: String,
    val matches: List<MigrationMatch>,
    val isSearching: Boolean = false,
    val error: String? = null
)
