package ireader.domain.services.downloaderService

import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import ireader.core.util.createICoroutineScope
import ireader.domain.catalogs.CatalogStore
import ireader.domain.data.repository.BookRepository
import ireader.domain.data.repository.ChapterRepository
import ireader.domain.notification.NotificationsIds.CHANNEL_DOWNLOADER_COMPLETE
import ireader.domain.notification.NotificationsIds.ID_DOWNLOAD_CHAPTER_PROGRESS
import ireader.domain.services.downloaderService.DownloadServiceStateImpl.Companion.DOWNLOADER_BOOKS_IDS
import ireader.domain.services.downloaderService.DownloadServiceStateImpl.Companion.DOWNLOADER_Chapters_IDS
import ireader.domain.services.downloaderService.DownloadServiceStateImpl.Companion.DOWNLOADER_MODE
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
    private val downloadServiceState: DownloadServiceStateImpl by inject()
    private val notificationManager: PlatformNotificationManager by inject()
    private val downloadPreferences: ireader.domain.preferences.prefs.DownloadPreferences by inject()
    private val downloadJob = Job()
    val scope = createICoroutineScope(Dispatchers.Main.immediate + downloadJob)
    
    @Volatile
    private var isCancelled = false

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
        
        // Show dismissable cancellation notification
        val cancelMessage = if (completedCount > 0) {
            "$completedCount of $totalDownloads chapters downloaded before cancellation"
        } else {
            "Download cancelled"
        }
        
        notificationManager.showPlatformNotification(
            ID_DOWNLOAD_CHAPTER_PROGRESS + 1,
            NotificationCompat.Builder(
                context,
                ireader.domain.notification.NotificationsIds.CHANNEL_DOWNLOADER_ERROR
            ).apply {
                setContentTitle("Download Cancelled")
                setContentText(cancelMessage)
                setSmallIcon(R.drawable.ic_downloading)
                priority = NotificationCompat.PRIORITY_DEFAULT
                setAutoCancel(true)
                setOngoing(false)
                setContentIntent(defaultNotificationHelper.openDownloadsPendingIntent)
            }.build()
        )
        ireader.core.log.Log.info { "DownloaderService: Showed cancellation notification: $cancelMessage" }
    }

    override suspend fun doWork(): Result {
        ireader.core.log.Log.info { "DownloaderService: Starting doWork()" }
        
        val notificationId = ID_DOWNLOAD_CHAPTER_PROGRESS
        
        try {
            // Check if work is stopped at the beginning
            if (isStopped) {
                ireader.core.log.Log.info { "DownloaderService: Work already stopped at start" }
                cleanupOnCancel()
                return Result.failure()
            }
            val inputtedChapterIds = inputData.getLongArray(DOWNLOADER_Chapters_IDS)?.distinct()
            val inputtedBooksIds = inputData.getLongArray(DOWNLOADER_BOOKS_IDS)?.distinct()
            val inputtedDownloaderMode = inputData.getBoolean(DOWNLOADER_MODE, false)
            
            val builder = defaultNotificationHelper.baseNotificationDownloader(
                chapter = null,
                id
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
                    NotificationCompat.Builder(
                        applicationContext,
                        ireader.domain.notification.NotificationsIds.CHANNEL_DOWNLOADER_ERROR
                    ).apply {
                        setContentTitle("Download Failed")
                        setContentText("$bookName: $errorMessage")
                        setSmallIcon(R.drawable.ic_downloading)
                        priority = NotificationCompat.PRIORITY_DEFAULT
                        setAutoCancel(true)
                        setOngoing(false)
                        setStyle(NotificationCompat.BigTextStyle().bigText(errorMessage))
                        setContentIntent(defaultNotificationHelper.openDownloadsPendingIntent)
                    }.build()
                )
            },
            onSuccess = {
                notificationManager.cancel(notificationId)
                
                // Count completed and failed downloads
                val completedCount = downloadServiceState.downloadProgress.value.values
                    .count { it.status == ireader.domain.services.downloaderService.DownloadStatus.COMPLETED }
                val failedCount = downloadServiceState.downloadProgress.value.values
                    .count { it.status == ireader.domain.services.downloaderService.DownloadStatus.FAILED }
                
                // Show completion notification
                if (completedCount > 0 || failedCount > 0) {
                    val summaryText = when {
                        failedCount == 0 -> "$completedCount chapters downloaded successfully"
                        completedCount == 0 -> "$failedCount chapters failed to download"
                        else -> "$completedCount succeeded, $failedCount failed"
                    }
                    
                    notificationManager.showPlatformNotification(
                        notificationId + 1,
                        NotificationCompat.Builder(
                            applicationContext,
                            CHANNEL_DOWNLOADER_COMPLETE
                        ).apply {
                            setContentTitle("Downloads completed")
                            setContentText(summaryText)
                            setSmallIcon(R.drawable.ic_downloading)
                            priority = NotificationCompat.PRIORITY_DEFAULT
                            setAutoCancel(true)
                            setContentIntent(defaultNotificationHelper.openDownloadsPendingIntent)
                        }.build()
                    )
                }
            },
            remoteUseCases = remoteUseCases,
            updateProgress = { max, progress, inProgress ->
                builder.setProgress(max, progress, inProgress)
            },
            updateSubtitle = {
                builder.setSubText(it)
            },
            updateTitle = {
                builder.setContentText(it)
            },
            updateNotification = {
                // Only show notification if not cancelled
                if (!isCancelled && !isStopped) {
                    notificationManager.showPlatformNotification(notificationId, builder.build())
                }
            },
            downloadDelayMs = downloadPreferences.downloadDelayMs().get(),
            concurrentLimit = 1, // Sequential downloads for simplicity
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
                        "$completedCount of $totalDownloads chapters downloaded before cancellation"
                    } else {
                        "Download cancelled"
                    }
                    
                    notificationManager.showPlatformNotification(
                        notificationId + 1,
                        NotificationCompat.Builder(
                            context,
                            ireader.domain.notification.NotificationsIds.CHANNEL_DOWNLOADER_ERROR
                        ).apply {
                            setContentTitle("Download Cancelled")
                            setContentText(cancelMessage)
                            setSmallIcon(R.drawable.ic_downloading)
                            priority = NotificationCompat.PRIORITY_DEFAULT
                            setAutoCancel(true)
                            setOngoing(false)
                            setContentIntent(defaultNotificationHelper.openDownloadsPendingIntent)
                        }.build()
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