package ireader.domain.services.downloaderService

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import ireader.core.log.Log
import ireader.domain.usecases.download.DownloadUseCases
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * BroadcastReceiver to handle download notification actions (pause/resume/cancel).
 * 
 * This receiver handles actions from notification buttons:
 * - ACTION_PAUSE: Pause all downloads
 * - ACTION_RESUME: Resume paused downloads
 * - ACTION_CANCEL: Cancel all downloads
 * - ACTION_ALLOW_MOBILE_DATA: Allow downloads on mobile data temporarily
 * - ACTION_RETRY_ALL: Retry all failed downloads
 * - ACTION_CLEAR_FAILED: Clear all failed downloads from queue
 * 
 * NOTE: Currently uses DownloadStateHolder (the working legacy system).
 * The new DownloadManager will be integrated in a future update after proper testing.
 */
class DownloadActionReceiver : BroadcastReceiver(), KoinComponent {
    
    // Use DownloadStateHolder - the working legacy system
    private val downloadStateHolder: DownloadStateHolder by inject()
    private val downloadUseCases: DownloadUseCases by inject()
    
    // Scope for coroutine operations
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            DownloadActions.ACTION_PAUSE -> {
                Log.info { "DownloadActionReceiver: Pause action received" }
                downloadStateHolder.setPaused(true)
            }
            DownloadActions.ACTION_RESUME -> {
                Log.info { "DownloadActionReceiver: Resume action received" }
                downloadStateHolder.setPaused(false)
            }
            DownloadActions.ACTION_CANCEL -> {
                Log.info { "DownloadActionReceiver: Cancel action received" }
                // Stop the download service by setting running to false
                // The runDownloadService loop checks this and will exit
                downloadStateHolder.setRunning(false)
                downloadStateHolder.setPaused(false)
                
                // Also cancel the WorkManager work to ensure cleanup
                try {
                    WorkManager.getInstance(context)
                        .cancelUniqueWork(DownloadServiceConstants.DOWNLOADER_SERVICE_NAME)
                    Log.info { "DownloadActionReceiver: Cancelled WorkManager work" }
                } catch (e: Exception) {
                    Log.error { "DownloadActionReceiver: Failed to cancel WorkManager work: ${e.message}" }
                }
            }
            DownloadActions.ACTION_ALLOW_MOBILE_DATA -> {
                Log.info { "DownloadActionReceiver: Allow mobile data action received" }
                // Resume downloads - the network check in runDownloadService will allow it
                downloadStateHolder.setPaused(false)
            }
            DownloadActions.ACTION_RETRY_ALL -> {
                Log.info { "DownloadActionReceiver: Retry all failed action received" }
                scope.launch {
                    // Reset failed downloads to queued status
                    val currentProgress = downloadStateHolder.downloadProgress.value.toMutableMap()
                    currentProgress.forEach { (chapterId, progress) ->
                        if (progress.status == DownloadStatus.FAILED) {
                            currentProgress[chapterId] = progress.copy(
                                status = DownloadStatus.QUEUED,
                                errorMessage = null,
                                retryCount = 0
                            )
                        }
                    }
                    downloadStateHolder.setDownloadProgress(currentProgress)
                    
                    // Restart downloads if not running
                    if (!downloadStateHolder.isRunning.value) {
                        // Start WorkManager directly
                        startDownloadWorkManager(context)
                    }
                }
            }
            DownloadActions.ACTION_CLEAR_FAILED -> {
                Log.info { "DownloadActionReceiver: Clear failed action received" }
                scope.launch {
                    // Remove failed downloads from progress tracking
                    val currentProgress = downloadStateHolder.downloadProgress.value.toMutableMap()
                    val failedChapterIds = currentProgress.filter { it.value.status == DownloadStatus.FAILED }.keys
                    failedChapterIds.forEach { chapterId ->
                        currentProgress.remove(chapterId)
                    }
                    downloadStateHolder.setDownloadProgress(currentProgress)
                    
                    // Also remove from database
                    failedChapterIds.forEach { chapterId ->
                        val download = downloadStateHolder.downloads.value.find { it.chapterId == chapterId }
                        if (download != null) {
                            downloadUseCases.deleteSavedDownload(download.toDownload())
                        }
                    }
                    
                    // Update downloads list
                    val updatedDownloads = downloadStateHolder.downloads.value.filter { 
                        it.chapterId !in failedChapterIds 
                    }
                    downloadStateHolder.setDownloads(updatedDownloads)
                }
            }
        }
    }
    
    /**
     * Start the download WorkManager directly.
     * This is used when we need to restart downloads from the receiver.
     */
    private fun startDownloadWorkManager(context: Context) {
        val workData = Data.Builder()
            .putBoolean(DownloadServiceConstants.DOWNLOADER_MODE, true)
            .build()
        
        val workRequest = OneTimeWorkRequestBuilder<DownloaderService>()
            .setInputData(workData)
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .addTag(DownloadServiceConstants.DOWNLOADER_SERVICE_NAME)
            .build()
        
        downloadStateHolder.setRunning(true)
        downloadStateHolder.setPaused(false)
        
        WorkManager.getInstance(context).enqueueUniqueWork(
            DownloadServiceConstants.DOWNLOADER_SERVICE_NAME,
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
        
        Log.info { "DownloadActionReceiver: Started download WorkManager" }
    }
}
