package ireader.domain.usecases.services

import platform.BackgroundTasks.*
import platform.Foundation.*
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.*
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import ireader.domain.catalogs.interactor.GetLocalCatalog
import ireader.domain.services.library_update_service.runLibraryUpdateService
import ireader.domain.usecases.remote.RemoteUseCases
import ireader.domain.notification.PlatformNotificationManager

/**
 * iOS implementation of StartLibraryUpdateServicesUseCase
 * 
 * Uses Koin Service Locator pattern to inject dependencies since expect/actual
 * classes don't support constructor parameters in commonMain.
 * 
 * Features:
 * - BGTaskScheduler for background library updates
 * - Full library update implementation using runLibraryUpdateService
 * - Automatic rescheduling for periodic updates
 */
@OptIn(ExperimentalForeignApi::class)
actual class StartLibraryUpdateServicesUseCase : KoinComponent {
    
    // Dependencies injected via Koin Service Locator
    private val getBookUseCases: ireader.domain.usecases.local.LocalGetBookUseCases by inject()
    private val getChapterUseCase: ireader.domain.usecases.local.LocalGetChapterUseCase by inject()
    private val remoteUseCases: RemoteUseCases by inject()
    private val getLocalCatalog: GetLocalCatalog by inject()
    private val insertUseCases: ireader.domain.usecases.local.LocalInsertUseCases by inject()
    private val notificationManager: PlatformNotificationManager by inject()
    
    private var updateJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    
    companion object {
        const val LIBRARY_UPDATE_TASK_ID = "com.ireader.library.update"
        const val LIBRARY_REFRESH_TASK_ID = "com.ireader.library.refresh"
        const val UPDATE_INTERVAL_SECONDS: Double = 6.0 * 60.0 * 60.0 // 6 hours
        private var forceUpdatePending = false
    }
    
    actual fun start(forceUpdate: Boolean) {
        forceUpdatePending = forceUpdate
        startImmediateUpdate(forceUpdate)
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
            println("[LibraryUpdate] Background refresh scheduled for ${UPDATE_INTERVAL_SECONDS / 3600} hours from now")
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
    
    private fun startImmediateUpdate(forceUpdate: Boolean = forceUpdatePending) {
        updateJob?.cancel()
        updateJob = scope.launch {
            try {
                println("[LibraryUpdate] Starting library update (force=$forceUpdate)")
                
                val result = runLibraryUpdateService(
                    getBookUseCases = getBookUseCases,
                    getChapterUseCase = getChapterUseCase,
                    remoteUseCases = remoteUseCases,
                    getLocalCatalog = getLocalCatalog,
                    insertUseCases = insertUseCases,
                    notificationManager = notificationManager,
                    forceUpdate = forceUpdate,
                    updateProgress = { max, progress, inProgress ->
                        println("[LibraryUpdate] Progress: $progress/$max")
                    },
                    updateTitle = { title ->
                        println("[LibraryUpdate] Updating: $title")
                    },
                    updateSubtitle = { subtitle ->
                        println("[LibraryUpdate] $subtitle")
                    },
                    updateNotification = { /* iOS handles notifications differently */ },
                    onSuccess = { bookSize, skippedBooks ->
                        println("[LibraryUpdate] Completed: $bookSize books updated, $skippedBooks skipped")
                    },
                    onCancel = { error ->
                        println("[LibraryUpdate] Cancelled: ${error.message}")
                    }
                )
                
                println("[LibraryUpdate] Library update finished with result: $result")
                
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
                startImmediateUpdate(forceUpdatePending)
                updateJob?.join()
                task.setTaskCompletedWithSuccess(true)
            } catch (e: Exception) {
                println("[LibraryUpdate] Background task failed: ${e.message}")
                task.setTaskCompletedWithSuccess(false)
            }
            // Always reschedule for periodic updates
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
