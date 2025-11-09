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
        // Create unique notification ID for this download batch
        val inputtedChapterIds = inputData.getLongArray(DOWNLOADER_Chapters_IDS)?.distinct()
        val inputtedBooksIds = inputData.getLongArray(DOWNLOADER_BOOKS_IDS)?.distinct()
        val inputtedDownloaderMode = inputData.getBoolean(DOWNLOADER_MODE, false)
        
        // Generate unique notification ID based on work ID
        val notificationId = if (inputtedDownloaderMode) {
            ID_DOWNLOAD_CHAPTER_PROGRESS
        } else {
            // Use hashCode to generate unique notification ID for each work
            ID_DOWNLOAD_CHAPTER_PROGRESS + (id.hashCode() and 0x7FFFFFFF) % 1000
        }
        
        val builder = defaultNotificationHelper.baseNotificationDownloader(
            chapter = null,
            id  // Use the actual work UUID
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
            onCancel = { error, bookName->
                notificationManager.cancel(notificationId)
                notificationManager.show(
                    notificationId + 1,
                    defaultNotificationHelper.baseCancelledNotificationDownloader(
                        bookName = bookName,
                        error
                    ).build()
                )
            },
            onSuccess = { completedDownloads ->
                // Cancel progress notification first
                notificationManager.cancel(notificationId)
                
                // Add completed downloads to the state
                val currentCompleted = downloadServiceState.completedDownloads.toMutableList()
                currentCompleted.addAll(completedDownloads)
                downloadServiceState.completedDownloads = currentCompleted
                
                // Create notifications for completed downloads
                if (completedDownloads.isNotEmpty()) {
                    if (completedDownloads.size == 1) {
                        // Single download notification
                        val download = completedDownloads.first()
                        val notification = NotificationCompat.Builder(
                            applicationContext.applicationContext,
                            CHANNEL_DOWNLOADER_COMPLETE
                        ).apply {
                            setContentTitle("${download.bookName}")
                            setContentText("${download.chapterName} downloaded")
                            setSmallIcon(R.drawable.ic_downloading)
                            priority = NotificationCompat.PRIORITY_DEFAULT
                            setAutoCancel(true)
                            setContentIntent(defaultNotificationHelper.openDownloadsPendingIntent)
                            setGroup("download_complete_group")
                        }.build()
                        notificationManager.show(
                            notificationId + 2,
                            notification
                        )
                    } else {
                        // Multiple downloads - show individual notifications and a summary
                        completedDownloads.forEachIndexed { index, download ->
                            val notification = NotificationCompat.Builder(
                                applicationContext.applicationContext,
                                CHANNEL_DOWNLOADER_COMPLETE
                            ).apply {
                                setContentTitle("${download.bookName}")
                                setContentText("${download.chapterName} downloaded")
                                setSmallIcon(R.drawable.ic_downloading)
                                priority = NotificationCompat.PRIORITY_LOW
                                setAutoCancel(true)
                                setContentIntent(defaultNotificationHelper.openDownloadsPendingIntent)
                                setGroup("download_complete_group")
                            }.build()
                            notificationManager.show(
                                notificationId + 2 + index,
                                notification
                            )
                        }
                        
                        // Summary notification
                        val summaryNotification = NotificationCompat.Builder(
                            applicationContext.applicationContext,
                            CHANNEL_DOWNLOADER_COMPLETE
                        ).apply {
                            setContentTitle("Downloads completed")
                            setContentText("${completedDownloads.size} chapters downloaded")
                            setSmallIcon(R.drawable.ic_downloading)
                            priority = NotificationCompat.PRIORITY_DEFAULT
                            setAutoCancel(true)
                            setContentIntent(defaultNotificationHelper.openDownloadsPendingIntent)
                            setGroup("download_complete_group")
                            setGroupSummary(true)
                        }.build()
                        notificationManager.show(
                            notificationId + 1000,
                            summaryNotification
                        )
                    }
                }
            },
            remoteUseCases = remoteUseCases,
            updateProgress = { max, progress, inProgess ->
                builder.setProgress(max, progress, inProgess)
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
            concurrentLimit = downloadPreferences.concurrentDownloadsLimit().get()
        )
        return if (result) Result.success() else Result.failure()
    }
}