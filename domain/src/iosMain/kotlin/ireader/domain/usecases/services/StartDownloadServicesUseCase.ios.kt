package ireader.domain.usecases.services

import platform.BackgroundTasks.*
import platform.Foundation.*
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.*

/**
 * iOS implementation of StartDownloadServicesUseCase
 */
@OptIn(ExperimentalForeignApi::class)
actual class StartDownloadServicesUseCase {
    
    private var downloadJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    
    companion object {
        const val DOWNLOAD_TASK_ID = "com.ireader.download.processing"
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
        BGTaskScheduler.sharedScheduler.cancelTaskRequestWithIdentifier(DOWNLOAD_TASK_ID)
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
        }
    }

    private fun startImmediateDownload(bookIds: LongArray?, chapterIds: LongArray?, downloadModes: Boolean) {
        downloadJob?.cancel()
        
        downloadJob = scope.launch {
            try {
                if (bookIds != null && bookIds.isNotEmpty()) {
                    println("[DownloadService] Starting download for ${bookIds.size} books")
                }
                if (chapterIds != null && chapterIds.isNotEmpty()) {
                    println("[DownloadService] Starting download for ${chapterIds.size} chapters")
                }
            } catch (e: CancellationException) {
                println("[DownloadService] Download cancelled")
                throw e
            } catch (e: Exception) {
                println("[DownloadService] Download error: ${e.message}")
            }
        }
    }
    
    fun handleBackgroundTask(task: BGTask) {
        task.setExpirationHandler { downloadJob?.cancel() }
        
        scope.launch {
            try {
                val bookIds = pendingBookIds
                val chapterIds = pendingChapterIds
                
                if (bookIds != null || chapterIds != null) {
                    startImmediateDownload(bookIds, chapterIds, pendingDownloadModes)
                    downloadJob?.join()
                }
                
                task.setTaskCompletedWithSuccess(true)
            } catch (e: Exception) {
                println("[DownloadService] Background task failed: ${e.message}")
                task.setTaskCompletedWithSuccess(false)
            }
            
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
