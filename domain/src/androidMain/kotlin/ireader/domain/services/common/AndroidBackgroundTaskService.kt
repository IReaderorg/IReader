package ireader.domain.services.common

import android.content.Context
import androidx.work.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.concurrent.TimeUnit

/**
 * Android implementation using WorkManager
 */
class AndroidBackgroundTaskService(
    private val context: Context
) : BackgroundTaskService {
    
    private val workManager = WorkManager.getInstance(context)
    private val taskStatusMap = mutableMapOf<String, MutableStateFlow<TaskStatus?>>()
    
    override suspend fun initialize() {
        // WorkManager is initialized automatically
    }
    
    override suspend fun start() {
        // No-op for WorkManager
    }
    
    override suspend fun stop() {
        // No-op for WorkManager
    }
    
    override fun isRunning(): Boolean = true
    
    override suspend fun cleanup() {
        taskStatusMap.clear()
    }
    
    override suspend fun scheduleOneTimeTask(
        taskId: String,
        taskType: TaskType,
        delayMillis: Long,
        constraints: TaskConstraints
    ): ServiceResult<String> {
        return try {
            val workConstraints = buildConstraints(constraints)
            val workRequest = OneTimeWorkRequestBuilder<TaskWorker>()
                .setConstraints(workConstraints)
                .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
                .setInputData(workDataOf("taskType" to taskType.name))
                .addTag(taskId)
                .addTag(taskType.name)
                .build()
            
            workManager.enqueueUniqueWork(
                taskId,
                ExistingWorkPolicy.REPLACE,
                workRequest
            )
            
            observeWorkStatus(taskId, workRequest.id)
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
            val workConstraints = buildConstraints(constraints)
            val workRequest = PeriodicWorkRequestBuilder<TaskWorker>(
                intervalMillis,
                TimeUnit.MILLISECONDS
            )
                .setConstraints(workConstraints)
                .setInputData(workDataOf("taskType" to taskType.name))
                .addTag(taskId)
                .addTag(taskType.name)
                .build()
            
            workManager.enqueueUniquePeriodicWork(
                taskId,
                ExistingPeriodicWorkPolicy.REPLACE,
                workRequest
            )
            
            observeWorkStatus(taskId, workRequest.id)
            ServiceResult.Success(taskId)
        } catch (e: Exception) {
            ServiceResult.Error("Failed to schedule periodic task: ${e.message}", e)
        }
    }
    
    override suspend fun cancelTask(taskId: String): ServiceResult<Unit> {
        return try {
            workManager.cancelUniqueWork(taskId)
            taskStatusMap[taskId]?.value = TaskStatus.Cancelled
            ServiceResult.Success(Unit)
        } catch (e: Exception) {
            ServiceResult.Error("Failed to cancel task: ${e.message}", e)
        }
    }
    
    override suspend fun cancelTasksByType(taskType: TaskType): ServiceResult<Unit> {
        return try {
            workManager.cancelAllWorkByTag(taskType.name)
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
    
    private fun buildConstraints(constraints: TaskConstraints): Constraints {
        return Constraints.Builder()
            .setRequiredNetworkType(
                if (constraints.requiresNetwork) NetworkType.CONNECTED
                else NetworkType.NOT_REQUIRED
            )
            .setRequiresCharging(constraints.requiresCharging)
            .setRequiresBatteryNotLow(constraints.requiresBatteryNotLow)
            .setRequiresStorageNotLow(constraints.requiresStorageNotLow)
            .build()
    }
    
    private fun observeWorkStatus(taskId: String, workId: java.util.UUID) {
        val statusFlow = taskStatusMap.getOrPut(taskId) {
            MutableStateFlow(TaskStatus.Enqueued)
        }
        
        workManager.getWorkInfoByIdLiveData(workId).observeForever { workInfo ->
            if (workInfo != null) {
                statusFlow.value = when (workInfo.state) {
                    WorkInfo.State.ENQUEUED -> TaskStatus.Enqueued
                    WorkInfo.State.RUNNING -> TaskStatus.Running
                    WorkInfo.State.SUCCEEDED -> TaskStatus.Success()
                    WorkInfo.State.FAILED -> TaskStatus.Failed("Task failed")
                    WorkInfo.State.CANCELLED -> TaskStatus.Cancelled
                    else -> null
                }
            }
        }
    }
}

/**
 * Worker class for executing tasks
 */
class TaskWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    override suspend fun doWork(): Result {
        val taskType = inputData.getString("taskType") ?: return Result.failure()
        
        // Task execution logic would be delegated to appropriate services
        // This is a placeholder for the actual implementation
        
        return Result.success()
    }
}
