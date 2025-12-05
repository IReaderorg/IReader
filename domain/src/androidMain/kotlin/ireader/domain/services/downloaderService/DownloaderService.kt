package ireader.domain.services.downloaderService

import android.content.Context
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import ireader.core.util.createICoroutineScope
import ireader.domain.catalogs.CatalogStore
import ireader.domain.data.repository.BookRepository
import ireader.domain.data.repository.ChapterRepository
import ireader.domain.notification.NotificationsIds.CHANNEL_DOWNLOADER_COMPLETE
import ireader.domain.notification.NotificationsIds.CHANNEL_DOWNLOADER_PROGRESS
import ireader.domain.notification.NotificationsIds.ID_DOWNLOAD_CHAPTER_PROGRESS
import ireader.domain.services.downloaderService.DownloadServiceConstants.DOWNLOADER_BOOKS_IDS
import ireader.domain.services.downloaderService.DownloadServiceConstants.DOWNLOADER_CHAPTERS_IDS
import ireader.domain.services.downloaderService.DownloadServiceConstants.DOWNLOADER_MODE
import ireader.domain.usecases.download.DownloadUseCases
import ireader.domain.usecases.remote.RemoteUseCases
import ireader.domain.notification.PlatformNotificationManager
import ireader.i18n.LocalizeHelper
import ireader.i18n.R
import ireader.i18n.resources.the_downloads_was_interrupted
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class DownloaderService constructor(
    private val context: Context,
    params: WorkerParameters,

    ) : CoroutineWorker(context, params), KoinComponent {

    private val bookRepo: BookRepository by inject()
    private val chapterRepo: ChapterRepository by inject()
    private val remoteUseCases: RemoteUseCases by inject()
    private val localizeHelper: LocalizeHelper by inject()
    private val extensions: CatalogStore by inject()
    private val insertUseCases: ireader.domain.usecases.local.LocalInsertUseCases by inject()
    private val defaultNotificationHelper: DefaultNotificationHelper by inject()
    private val downloadUseCases: DownloadUseCases by inject()
    private val downloadServiceState: DownloadStateHolder by inject()
    private val notificationManager: PlatformNotificationManager by inject()
    private val downloadPreferences: ireader.domain.preferences.prefs.DownloadPreferences by inject()
    private val downloadJob = Job()
    val scope = createICoroutineScope(Dispatchers.Main.immediate + downloadJob)
    
    @Volatile
    private var isCancelled = false

    /**
     * Provides foreground service info to prevent the 10-minute WorkManager timeout.
     * This is critical for long-running downloads to prevent automatic restarts.
     */
    override suspend fun getForegroundInfo(): ForegroundInfo {
        val notification = defaultNotificationHelper.createDownloadNotification(
            workManagerId = id,
            bookName = "Preparing download...",
            isPaused = false
        ).build()
        
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ForegroundInfo(
                ID_DOWNLOAD_CHAPTER_PROGRESS,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } else {
            ForegroundInfo(ID_DOWNLOAD_CHAPTER_PROGRESS, notification)
        }
    }

    private fun cleanupOnCancel() {
        ireader.core.log.Log.info { "DownloaderService: Cleaning up on cancellation" }
        
        // Cancel the download job
        downloadJob.cancel()
        
        // Reset download service state - this will cause the download loop to exit
        downloadServiceState.setRunning(false)
        downloadServiceState.setPaused(false)
        
        // Get the current download info for the notification
        val currentDownload = downloadServiceState.downloads.value.firstOrNull()
        val bookName = currentDownload?.bookName ?: "Downloads"
        
        // Count how many were in progress
        val totalDownloads = downloadServiceState.downloads.value.size
        val completedCount = downloadServiceState.downloadProgress.value.values
            .count { it.status == DownloadStatus.COMPLETED }
        
        downloadServiceState.setDownloadProgress(emptyMap())
        
        // Cancel the progress notification immediately
        notificationManager.cancel(ID_DOWNLOAD_CHAPTER_PROGRESS)
        ireader.core.log.Log.info { "DownloaderService: Cancelled notification $ID_DOWNLOAD_CHAPTER_PROGRESS" }
        
        // Show cancellation notification
        val cancelMessage = if (completedCount > 0) {
            "$completedCount of $totalDownloads downloaded"
        } else {
            "Download cancelled"
        }
        
        notificationManager.showPlatformNotification(
            ID_DOWNLOAD_CHAPTER_PROGRESS + 1,
            defaultNotificationHelper.createModernDownloadNotification(
                workManagerId = java.util.UUID.randomUUID(),
                bookName = bookName,
                chapterName = cancelMessage,
                status = DefaultNotificationHelper.DownloadNotificationStatus.CANCELLED
            ).build()
        )
        ireader.core.log.Log.info { "DownloaderService: Showed cancellation notification: $cancelMessage" }
    }

    override suspend fun doWork(): Result {
        ireader.core.log.Log.info { "DownloaderService: Starting doWork()" }
        
        val notificationId = ID_DOWNLOAD_CHAPTER_PROGRESS
        
        try {
            // Set foreground immediately to prevent 10-minute timeout
            try {
                setForeground(getForegroundInfo())
            } catch (e: Exception) {
                // Continue without foreground - some devices may not support it
            }
            
            // Check if work is stopped at the beginning
            if (isStopped) {
                ireader.core.log.Log.info { "DownloaderService: Work already stopped at start" }
                cleanupOnCancel()
                return Result.failure()
            }
            val inputtedChapterIds = inputData.getLongArray(DOWNLOADER_CHAPTERS_IDS)?.distinct()
            val inputtedBooksIds = inputData.getLongArray(DOWNLOADER_BOOKS_IDS)?.distinct()
            val inputtedDownloaderMode = inputData.getBoolean(DOWNLOADER_MODE, false)
            
            // Track notification state
            var currentBookName = ""
            var currentChapterName: String? = null
            var currentProgress = 0
            var currentChapterIndex = 0
            var totalChapters = 0
            
            val builder = defaultNotificationHelper.createDownloadNotification(
                workManagerId = id,
                bookName = "Preparing download...",
                isPaused = false
            )
        
        val result = runDownloadService(
            inputtedBooksIds = inputtedBooksIds?.toLongArray(),
            inputtedChapterIds = inputtedChapterIds?.toLongArray(),
            inputtedDownloaderMode = inputtedDownloaderMode,
            bookRepo = bookRepo,
            downloadServiceState = downloadServiceState,
            downloadUseCases = downloadUseCases,
            chapterRepo = chapterRepo,
            extensions = extensions,
            insertUseCases = insertUseCases,
            localizeHelper = localizeHelper,
            notificationManager = notificationManager,
            onCancel = { error, bookName ->
                notificationManager.cancel(notificationId)
                
                val errorMessage = error.message ?: "Download failed"
                notificationManager.showPlatformNotification(
                    notificationId + 1,
                    defaultNotificationHelper.createModernDownloadNotification(
                        workManagerId = id,
                        bookName = bookName.ifEmpty { "Download" },
                        chapterName = errorMessage,
                        status = DefaultNotificationHelper.DownloadNotificationStatus.FAILED
                    ).build()
                )
            },
            onSuccess = {
                notificationManager.cancel(notificationId)
                
                // Count completed and failed downloads
                val completedCount = downloadServiceState.downloadProgress.value.values
                    .count { it.status == ireader.domain.services.downloaderService.DownloadStatus.COMPLETED }
                val failedCount = downloadServiceState.downloadProgress.value.values
                    .count { it.status == ireader.domain.services.downloaderService.DownloadStatus.FAILED }
                
                // Show modern completion notification
                if (completedCount > 0 || failedCount > 0) {
                    val bookName = downloadServiceState.downloads.value.firstOrNull()?.bookName ?: "Downloads"
                    notificationManager.showPlatformNotification(
                        notificationId + 1,
                        defaultNotificationHelper.createCompletionNotification(
                            bookName = bookName,
                            completedCount = completedCount,
                            failedCount = failedCount
                        ).build()
                    )
                }
            },
            remoteUseCases = remoteUseCases,
            updateProgress = { max, progress, inProgress ->
                currentProgress = if (max > 0) (progress * 100 / max) else 0
                totalChapters = max
                currentChapterIndex = progress
            },
            updateSubtitle = { subtitle ->
                currentChapterName = subtitle
            },
            updateTitle = { title ->
                currentBookName = title
            },
            updateNotification = {
                // Only show notification if not cancelled
                if (!isCancelled && !isStopped) {
                    val isPaused = downloadServiceState.isPaused.value
                    val status = if (isPaused) {
                        DefaultNotificationHelper.DownloadNotificationStatus.PAUSED
                    } else {
                        DefaultNotificationHelper.DownloadNotificationStatus.DOWNLOADING
                    }
                    
                    val notification = defaultNotificationHelper.createDownloadNotification(
                        workManagerId = id,
                        bookName = currentBookName.ifEmpty { "Downloading..." },
                        chapterName = currentChapterName,
                        progress = currentProgress,
                        currentChapter = currentChapterIndex,
                        totalChapters = totalChapters,
                        isPaused = isPaused
                    )
                    notificationManager.showPlatformNotification(notificationId, notification.build())
                }
            },
            downloadDelayMs = downloadPreferences.downloadDelayMs().get(),
            checkCancellation = { 
                if (isStopped && !isCancelled) {
                    ireader.core.log.Log.info { "DownloaderService: Cancellation detected in checkCancellation" }
                    isCancelled = true
                    // Immediately stop the service state to break out of loops
                    downloadServiceState.setRunning(false)
                    // Cancel notification immediately and show cancellation notification
                    notificationManager.cancel(notificationId)
                    
                    // Show cancellation notification immediately
                    val currentDownload = downloadServiceState.downloads.value.firstOrNull()
                    val totalDownloads = downloadServiceState.downloads.value.size
                    val completedCount = downloadServiceState.downloadProgress.value.values
                        .count { it.status == DownloadStatus.COMPLETED }
                    
                    val cancelMessage = if (completedCount > 0) {
                        "$completedCount of $totalDownloads downloaded"
                    } else {
                        "Download cancelled"
                    }
                    
                    val bookName = currentDownload?.bookName ?: "Downloads"
                    notificationManager.showPlatformNotification(
                        notificationId + 1,
                        defaultNotificationHelper.createModernDownloadNotification(
                            workManagerId = id,
                            bookName = bookName,
                            chapterName = cancelMessage,
                            status = DefaultNotificationHelper.DownloadNotificationStatus.CANCELLED
                        ).build()
                    )
                    
                    ireader.core.log.Log.info { "DownloaderService: Immediately showed cancellation notification" }
                }
                isStopped 
            }
        )
        
            // Check if cancelled during execution
            if (isStopped || isCancelled) {
                ireader.core.log.Log.info { "DownloaderService: Work stopped during execution" }
                // Make absolutely sure notification is cancelled
                notificationManager.cancel(notificationId)
                cleanupOnCancel()
                return Result.failure()
            }
            
            ireader.core.log.Log.info { "DownloaderService: doWork() completed with result=$result" }
            return if (result) Result.success() else Result.failure()
            
        } finally {
            // Always ensure notification is cleaned up if work was cancelled
            if (isStopped || isCancelled) {
                ireader.core.log.Log.info { "DownloaderService: Finally block - ensuring notification cleanup" }
                notificationManager.cancel(notificationId)
            }
        }
    }
}