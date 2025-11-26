package ireader.domain.services.common

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.concurrent.ConcurrentHashMap

/**
 * Desktop implementation using coroutines and scheduled executors
 */
class DesktopBackgroundTaskService : BackgroundTaskService {
    
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val taskJobs = ConcurrentHashMap<String, Job>()
    private val taskStatusMap = ConcurrentHashMap<String, MutableStateFlow<TaskStatus?>>()
    
    override suspend fun initialize() {}
    
    override suspend fun start() {}
    
    override suspend fun stop() {
        taskJobs.values.forEach { it.cancel() }
        taskJobs.clear()
    }
    
    override fun isRunning(): Boolean = scope.isActive
    
    override suspend fun cleanup() {
        stop()
        taskStatusMap.clear()
    }
    
    override suspend fun scheduleOneTimeTask(
        taskId: String,
        taskType: TaskType,
        delayMillis: Long,
        constraints: TaskConstraints
    ): ServiceResult<String> {
        return try {
            // Cancel existing task with same ID
            taskJobs[taskId]?.cancel()
            
            val statusFlow = taskStatusMap.getOrPut(taskId) {
                MutableStateFlow(TaskStatus.Enqueued)
            }
            
            val job = scope.launch {
                try {
                    statusFlow.value = TaskStatus.Enqueued
                    
                    if (delayMillis > 0) {
                        delay(delayMillis)
                    }
                    
                    // Check constraints
                    if (!checkConstraints(constraints)) {
                        statusFlow.value = TaskStatus.Failed("Constraints not met")
                        return@launch
                    }
                    
                    statusFlow.value = TaskStatus.Running
                    
                    // Execute task (placeholder - actual implementation would delegate)
                    executeTask(taskType)
                    
                    statusFlow.value = TaskStatus.Success()
                } catch (e: CancellationException) {
                    statusFlow.value = TaskStatus.Cancelled
                    throw e
                } catch (e: Exception) {
                    statusFlow.value = TaskStatus.Failed(e.message ?: "Unknown error")
                }
            }
            
            taskJobs[taskId] = job
            ServiceResult.Success(taskId)
        } catch (e: Exception) {
            ServiceResult.Error("Failed to schedule task: ${e.message}", e)
        }
    }
    
    override suspend fun schedulePeriodicTask(
        taskId: String,
        taskType: TaskType,
        intervalMillis: Long,
        constraints: TaskConstraints
    ): ServiceResult<String> {
        return try {
            taskJobs[taskId]?.cancel()
            
            val statusFlow = taskStatusMap.getOrPut(taskId) {
                MutableStateFlow(TaskStatus.Enqueued)
            }
            
            val job = scope.launch {
                while (isActive) {
                    try {
                        statusFlow.value = TaskStatus.Enqueued
                        delay(intervalMillis)
                        
                        if (!checkConstraints(constraints)) {
                            continue
                        }
                        
                        statusFlow.value = TaskStatus.Running
                        executeTask(taskType)
                        statusFlow.value = TaskStatus.Success()
                    } catch (e: CancellationException) {
                        statusFlow.value = TaskStatus.Cancelled
                        throw e
                    } catch (e: Exception) {
                        statusFlow.value = TaskStatus.Failed(e.message ?: "Unknown error")
                    }
                }
            }
            
            taskJobs[taskId] = job
            ServiceResult.Success(taskId)
        } catch (e: Exception) {
            ServiceResult.Error("Failed to schedule periodic task: ${e.message}", e)
        }
    }
    
    override suspend fun cancelTask(taskId: String): ServiceResult<Unit> {
        return try {
            taskJobs[taskId]?.cancel()
            taskJobs.remove(taskId)
            taskStatusMap[taskId]?.value = TaskStatus.Cancelled
            ServiceResult.Success(Unit)
        } catch (e: Exception) {
            ServiceResult.Error("Failed to cancel task: ${e.message}", e)
        }
    }
    
    override suspend fun cancelTasksByType(taskType: TaskType): ServiceResult<Unit> {
        return try {
            // Note: We'd need to track task types to implement this properly
            ServiceResult.Success(Unit)
        } catch (e: Exception) {
            ServiceResult.Error("Failed to cancel tasks: ${e.message}", e)
        }
    }
    
    override fun getTaskStatus(taskId: String): StateFlow<TaskStatus?> {
        return taskStatusMap.getOrPut(taskId) {
            MutableStateFlow(null)
        }
    }
    
    private fun checkConstraints(constraints: TaskConstraints): Boolean {
        // Desktop constraint checking is simplified
        // Network check could be implemented using Java's NetworkInterface
        return true
    }
    
    private suspend fun executeTask(taskType: TaskType) {
        // Placeholder for task execution
        // Actual implementation would delegate to appropriate services
        delay(100)
    }
}
