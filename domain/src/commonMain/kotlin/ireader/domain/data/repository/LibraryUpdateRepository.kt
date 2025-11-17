package ireader.domain.data.repository

import ireader.domain.models.library.LibraryUpdateJob
import ireader.domain.models.library.LibraryUpdateProgress
import ireader.domain.models.library.LibraryUpdateResult
import kotlinx.coroutines.flow.Flow

/**
 * Repository for managing library update operations
 */
interface LibraryUpdateRepository {
    
    // Job management
    suspend fun scheduleUpdate(job: LibraryUpdateJob): Boolean
    suspend fun cancelUpdate(jobId: String): Boolean
    suspend fun getActiveJobs(): List<LibraryUpdateJob>
    suspend fun getJobHistory(): List<LibraryUpdateResult>
    
    // Progress tracking
    fun getUpdateProgress(jobId: String): Flow<LibraryUpdateProgress>
    suspend fun updateProgress(progress: LibraryUpdateProgress)
    
    // Update execution
    suspend fun executeUpdate(job: LibraryUpdateJob): LibraryUpdateResult
    suspend fun canExecuteUpdate(): Boolean
    
    // Settings
    suspend fun getUpdateSettings(): LibraryUpdateSettings
    suspend fun updateSettings(settings: LibraryUpdateSettings): Boolean
    
    // Statistics
    suspend fun getUpdateStatistics(): LibraryUpdateStatistics
}

/**
 * Library update settings
 */
data class LibraryUpdateSettings(
    val autoUpdateEnabled: Boolean = false,
    val updateInterval: Long = 24 * 60 * 60 * 1000L, // 24 hours in milliseconds
    val requiresWifi: Boolean = true,
    val requiresCharging: Boolean = false,
    val updateOnlyFavorites: Boolean = false,
    val skipCompleted: Boolean = false,
    val skipRead: Boolean = false,
    val maxConcurrentUpdates: Int = 5,
    val updateTimeWindow: TimeWindow? = null,
    val excludedCategories: List<Long> = emptyList(),
    val excludedSources: List<Long> = emptyList()
)

/**
 * Time window for automatic updates
 */
data class TimeWindow(
    val startHour: Int, // 0-23
    val endHour: Int,   // 0-23
    val daysOfWeek: Set<Int> = setOf(1, 2, 3, 4, 5, 6, 7) // 1=Monday, 7=Sunday
)

/**
 * Library update statistics
 */
data class LibraryUpdateStatistics(
    val totalUpdatesRun: Int = 0,
    val successfulUpdates: Int = 0,
    val failedUpdates: Int = 0,
    val averageUpdateDuration: Long = 0,
    val totalNewChaptersFound: Int = 0,
    val lastUpdateTime: Long = 0,
    val averageBooksPerUpdate: Float = 0f,
    val mostActiveUpdateHour: Int = 0
)