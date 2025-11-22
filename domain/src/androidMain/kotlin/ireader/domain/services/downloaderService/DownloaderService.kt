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
import ireader.domain.utils.NotificationManager
import ireader.i18n.LocalizeHelper
import ireader.i18n.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
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
    private val notificationManager: NotificationManager by inject()
    private val downloadPreferences: ireader.domain.preferences.prefs.DownloadPreferences by inject()
    private val downloadJob = Job()
    val scope = createICoroutineScope(Dispatchers.Main.immediate + downloadJob)


    override suspend fun doWork(): Result {
        val inputtedChapterIds = inputData.getLongArray(DOWNLOADER_Chapters_IDS)?.distinct()
        val inputtedBooksIds = inputData.getLongArray(DOWNLOADER_BOOKS_IDS)?.distinct()
        val inputtedDownloaderMode = inputData.getBoolean(DOWNLOADER_MODE, false)
        
        val notificationId = ID_DOWNLOAD_CHAPTER_PROGRESS
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
                notificationManager.show(
                    notificationId + 1,
                    defaultNotificationHelper.baseCancelledNotificationDownloader(
                        bookName = bookName,
                        error
                    ).apply {
                        setContentText(errorMessage)
                        setStyle(NotificationCompat.BigTextStyle().bigText(errorMessage))
                    }.build()
                )
            },
            onSuccess = {
                notificationManager.cancel(notificationId)
                
                // Count completed and failed downloads
                val completedCount = downloadServiceState.downloadProgress.values
                    .count { it.status == ireader.domain.services.downloaderService.DownloadStatus.COMPLETED }
                val failedCount = downloadServiceState.downloadProgress.values
                    .count { it.status == ireader.domain.services.downloaderService.DownloadStatus.FAILED }
                
                // Show completion notification
                if (completedCount > 0 || failedCount > 0) {
                    val summaryText = when {
                        failedCount == 0 -> "$completedCount chapters downloaded successfully"
                        completedCount == 0 -> "$failedCount chapters failed to download"
                        else -> "$completedCount succeeded, $failedCount failed"
                    }
                    
                    val notification = NotificationCompat.Builder(
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
                    
                    notificationManager.show(notificationId + 1, notification)
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
                notificationManager.show(notificationId, builder.build())
            },
            downloadDelayMs = downloadPreferences.downloadDelayMs().get(),
            concurrentLimit = 1 // Sequential downloads for simplicity
        )
        
        return if (result) Result.success() else Result.failure()
    }
}