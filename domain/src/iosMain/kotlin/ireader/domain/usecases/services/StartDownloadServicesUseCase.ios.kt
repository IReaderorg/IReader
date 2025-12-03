package ireader.domain.usecases.services

import platform.BackgroundTasks.*
import platform.Foundation.*
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.*
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import ireader.domain.catalogs.CatalogStore
import ireader.domain.data.repository.BookRepository
import ireader.domain.data.repository.ChapterRepository
import ireader.domain.services.downloaderService.DownloadStateHolder
import ireader.domain.services.downloaderService.runDownloadService
import ireader.domain.usecases.download.DownloadUseCases
import ireader.domain.usecases.remote.RemoteUseCases
import ireader.domain.notification.PlatformNotificationManager
import ireader.i18n.LocalizeHelper

/**
 * iOS implementation of StartDownloadServicesUseCase
 * 
 * Uses Koin Service Locator pattern to inject dependencies since expect/actual
 * classes don't support constructor parameters in commonMain.
 * 
 * Features:
 * - BGTaskScheduler for background downloads
 * - Full download service implementation using runDownloadService
 * - Pause/resume support via DownloadStateHolder
 */
@OptIn(ExperimentalForeignApi::class)
actual class StartDownloadServicesUseCase : KoinComponent {
    
    // Dependencies injected via Koin Service Locator
    private val bookRepo: BookRepository by inject()
    private val chapterRepo: ChapterRepository by inject()
    private val remoteUseCases: RemoteUseCases by inject()
    private val localizeHelper: LocalizeHelper by inject()
    private val extensions: CatalogStore by inject()
    private val insertUseCases: ireader.domain.usecases.local.LocalInsertUseCases by inject()
    private val downloadUseCases: DownloadUseCases by inject()
    private val downloadServiceState: DownloadStateHolder by inject()
    private val notificationManager: PlatformNotificationManager by inject()
    
    private var downloadJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    
    companion object {
        const val DOWNLOAD_TASK_ID = "com.ireader.download.processing"
        const val DOWNLOAD_REFRESH_TASK_ID = "com.ireader.download.refresh"
        private var pendingBookIds: LongArray? = null
        private var pendingChapterIds: LongArray? = null
        private var pendingDownloadModes: Boolean = false
    }
    
    actual fun start(bookIds: LongArray?, chapterIds: LongArray?, downloadModes: Boolean) {
        pendingBookIds = bookIds
        pendingChapterIds = chapterIds
        pendingDownloadModes = downloadModes
        
        scheduleBackgroundTask()
        startImmediateDownload(bookIds, chapterIds, downloadModes)
    }
    
    actual fun stop() {
        downloadJob?.cancel()
        downloadJob = null
        downloadServiceState.setRunning(false)
        downloadServiceState.setPaused(false)
        BGTaskScheduler.sharedScheduler.cancelTaskRequestWithIdentifier(DOWNLOAD_TASK_ID)
        BGTaskScheduler.sharedScheduler.cancelTaskRequestWithIdentifier(DOWNLOAD_REFRESH_TASK_ID)
        pendingBookIds = null
        pendingChapterIds = null
        pendingDownloadModes = false
    }
    
    private fun scheduleBackgroundTask() {
        val request = BGProcessingTaskRequest(identifier = DOWNLOAD_TASK_ID).apply {
            requiresNetworkConnectivity = true
            requiresExternalPower = false
            earliestBeginDate = NSDate()
        }
        
        try {
            BGTaskScheduler.sharedScheduler.submitTaskRequest(request, null)
            println("[DownloadService] Background task scheduled successfully")
        } catch (e: Exception) {
            println("[DownloadService] Failed to schedule background task: ${e.message}")
            // Try app refresh task as fallback
            scheduleRefreshTask()
        }
    }
    
    private fun scheduleRefreshTask() {
        val request = BGAppRefreshTaskRequest(identifier = DOWNLOAD_REFRESH_TASK_ID).apply {
            earliestBeginDate = NSDate()
        }
        
        try {
            BGTaskScheduler.sharedScheduler.submitTaskRequest(request, null)
            println("[DownloadService] Refresh task scheduled as fallback")
        } catch (e: Exception) {
            println("[DownloadService] Failed to schedule refresh task: ${e.message}")
        }
    }

    private fun startImmediateDownload(bookIds: LongArray?, chapterIds: LongArray?, downloadModes: Boolean) {
        downloadJob?.cancel()
        
        downloadJob = scope.launch {
            try {
                println("[DownloadService] Starting download service")
                
                val result = runDownloadService(
                    inputtedBooksIds = bookIds,
                    inputtedChapterIds = chapterIds,
                    inputtedDownloaderMode = downloadModes,
                    bookRepo = bookRepo,
                    downloadServiceState = downloadServiceState,
                    downloadUseCases = downloadUseCases,
                    chapterRepo = chapterRepo,
                    extensions = extensions,
                    insertUseCases = insertUseCases,
                    localizeHelper = localizeHelper,
                    notificationManager = notificationManager,
                    onCancel = { error, bookName ->
                        println("[DownloadService] Download failed for $bookName: ${error.message}")
                    },
                    onSuccess = {
                        val completedCount = downloadServiceState.downloadProgress.value.values
                            .count { it.status == ireader.domain.services.downloaderService.DownloadStatus.COMPLETED }
                        val failedCount = downloadServiceState.downloadProgress.value.values
                            .count { it.status == ireader.domain.services.downloaderService.DownloadStatus.FAILED }
                        
                        println("[DownloadService] Download completed: $completedCount succeeded, $failedCount failed")
                    },
                    remoteUseCases = remoteUseCases,
                    updateProgress = { max, progress, inProgress ->
                        println("[DownloadService] Progress: $progress/$max")
                    },
                    updateSubtitle = { subtitle ->
                        println("[DownloadService] $subtitle")
                    },
                    updateTitle = { title ->
                        println("[DownloadService] $title")
                    },
                    updateNotification = { /* iOS handles notifications differently */ },
                    downloadDelayMs = 1000L
                )
                
                println("[DownloadService] Download service finished with result: $result")
                
            } catch (e: CancellationException) {
                println("[DownloadService] Download cancelled")
                throw e
            } catch (e: Exception) {
                println("[DownloadService] Download error: ${e.message}")
            }
        }
    }
    
    fun handleBackgroundTask(task: BGTask) {
        task.setExpirationHandler { 
            downloadJob?.cancel()
            downloadServiceState.setRunning(false)
        }
        
        scope.launch {
            try {
                val bookIds = pendingBookIds
                val chapterIds = pendingChapterIds
                
                if (bookIds != null || chapterIds != null || pendingDownloadModes) {
                    startImmediateDownload(bookIds, chapterIds, pendingDownloadModes)
                    downloadJob?.join()
                }
                
                task.setTaskCompletedWithSuccess(true)
            } catch (e: Exception) {
                println("[DownloadService] Background task failed: ${e.message}")
                task.setTaskCompletedWithSuccess(false)
            }
            
            // Reschedule if there are still pending downloads
            if (pendingBookIds != null || pendingChapterIds != null) {
                scheduleBackgroundTask()
            }
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
fun registerDownloadBackgroundTasks(downloadService: StartDownloadServicesUseCase) {
    BGTaskScheduler.sharedScheduler.registerForTaskWithIdentifier(
        identifier = StartDownloadServicesUseCase.DOWNLOAD_TASK_ID,
        usingQueue = null
    ) { task ->
        if (task != null) {
            downloadService.handleBackgroundTask(task)
        }
    }
}
