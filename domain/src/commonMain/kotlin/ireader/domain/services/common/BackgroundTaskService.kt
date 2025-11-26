package ireader.domain.services.common

import kotlinx.coroutines.flow.StateFlow

/**
 * Common background task service for scheduling and managing background work
 */
interface BackgroundTaskService : PlatformService {
    /**
     * Schedule a one-time task
     */
    suspend fun scheduleOneTimeTask(
        taskId: String,
        taskType: TaskType,
        delayMillis: Long = 0,
        constraints: TaskConstraints = TaskConstraints()
    ): ServiceResult<String>
    
    /**
     * Schedule a periodic task
     */
    suspend fun schedulePeriodicTask(
        taskId: String,
        taskType: TaskType,
        intervalMillis: Long,
        constraints: TaskConstraints = TaskConstraints()
    ): ServiceResult<String>
    
    /**
     * Cancel a scheduled task
     */
    suspend fun cancelTask(taskId: String): ServiceResult<Unit>
    
    /**
     * Cancel all tasks of a specific type
     */
    suspend fun cancelTasksByType(taskType: TaskType): ServiceResult<Unit>
    
    /**
     * Get task status
     */
    fun getTaskStatus(taskId: String): StateFlow<TaskStatus?>
}

/**
 * Task types
 */
enum class TaskType {
    DOWNLOAD,
    LIBRARY_UPDATE,
    BACKUP,
    EXTENSION_UPDATE,
    CLEANUP
}

/**
 * Task constraints
 */
data class TaskConstraints(
    val requiresNetwork: Boolean = true,
    val requiresCharging: Boolean = false,
    val requiresBatteryNotLow: Boolean = false,
    val requiresStorageNotLow: Boolean = false
)

/**
 * Task status
 */
sealed class TaskStatus {
    data object Enqueued : TaskStatus()
    data object Running : TaskStatus()
    data class Success(val outputData: Map<String, Any> = emptyMap()) : TaskStatus()
    data class Failed(val error: String) : TaskStatus()
    data object Cancelled : TaskStatus()
}
