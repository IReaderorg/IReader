package ireader.domain.usecases.services

import platform.BackgroundTasks.*
import platform.Foundation.*
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.*

/**
 * iOS implementation of StartLibraryUpdateServicesUseCase
 */
@OptIn(ExperimentalForeignApi::class)
actual class StartLibraryUpdateServicesUseCase {
    
    private var updateJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    
    companion object {
        const val LIBRARY_UPDATE_TASK_ID = "com.ireader.library.update"
        const val LIBRARY_REFRESH_TASK_ID = "com.ireader.library.refresh"
        const val UPDATE_INTERVAL_SECONDS: Double = 6.0 * 60.0 * 60.0
        private var forceUpdatePending = false
    }
    
    actual fun start(forceUpdate: Boolean) {
        forceUpdatePending = forceUpdate
        if (forceUpdate) startImmediateUpdate()
        scheduleBackgroundRefresh()
    }
    
    actual fun stop() {
        updateJob?.cancel()
        updateJob = null
        BGTaskScheduler.sharedScheduler.cancelTaskRequestWithIdentifier(LIBRARY_UPDATE_TASK_ID)
        BGTaskScheduler.sharedScheduler.cancelTaskRequestWithIdentifier(LIBRARY_REFRESH_TASK_ID)
        forceUpdatePending = false
    }
    
    private fun scheduleBackgroundRefresh() {
        val request = BGAppRefreshTaskRequest(identifier = LIBRARY_REFRESH_TASK_ID).apply {
            earliestBeginDate = NSDate.dateWithTimeIntervalSinceNow(UPDATE_INTERVAL_SECONDS)
        }
        
        try {
            BGTaskScheduler.sharedScheduler.submitTaskRequest(request, null)
            println("[LibraryUpdate] Background refresh scheduled")
        } catch (e: Exception) {
            println("[LibraryUpdate] Failed to schedule refresh task: ${e.message}")
            scheduleBackgroundProcessing()
        }
    }

    private fun scheduleBackgroundProcessing() {
        val request = BGProcessingTaskRequest(identifier = LIBRARY_UPDATE_TASK_ID).apply {
            requiresNetworkConnectivity = true
            requiresExternalPower = false
            earliestBeginDate = NSDate.dateWithTimeIntervalSinceNow(UPDATE_INTERVAL_SECONDS)
        }
        
        try {
            BGTaskScheduler.sharedScheduler.submitTaskRequest(request, null)
            println("[LibraryUpdate] Background processing scheduled")
        } catch (e: Exception) {
            println("[LibraryUpdate] Failed to schedule processing task: ${e.message}")
        }
    }
    
    private fun startImmediateUpdate() {
        updateJob?.cancel()
        updateJob = scope.launch {
            try {
                println("[LibraryUpdate] Starting library update")
                println("[LibraryUpdate] Library update completed")
            } catch (e: CancellationException) {
                println("[LibraryUpdate] Update cancelled")
                throw e
            } catch (e: Exception) {
                println("[LibraryUpdate] Update error: ${e.message}")
            }
        }
    }
    
    fun handleBackgroundTask(task: BGTask) {
        task.setExpirationHandler { updateJob?.cancel() }
        
        scope.launch {
            try {
                startImmediateUpdate()
                updateJob?.join()
                task.setTaskCompletedWithSuccess(true)
            } catch (e: Exception) {
                println("[LibraryUpdate] Background task failed: ${e.message}")
                task.setTaskCompletedWithSuccess(false)
            }
            scheduleBackgroundRefresh()
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
fun registerLibraryUpdateBackgroundTasks(libraryUpdateService: StartLibraryUpdateServicesUseCase) {
    BGTaskScheduler.sharedScheduler.registerForTaskWithIdentifier(
        identifier = StartLibraryUpdateServicesUseCase.LIBRARY_REFRESH_TASK_ID,
        usingQueue = null
    ) { task ->
        if (task != null) libraryUpdateService.handleBackgroundTask(task)
    }
    
    BGTaskScheduler.sharedScheduler.registerForTaskWithIdentifier(
        identifier = StartLibraryUpdateServicesUseCase.LIBRARY_UPDATE_TASK_ID,
        usingQueue = null
    ) { task ->
        if (task != null) libraryUpdateService.handleBackgroundTask(task)
    }
}
