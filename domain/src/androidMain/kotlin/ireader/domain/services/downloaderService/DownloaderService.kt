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
import ireader.domain.notification.NotificationsIds.ID_DOWNLOAD_CHAPTER_COMPLETE
import ireader.domain.notification.NotificationsIds.ID_DOWNLOAD_CHAPTER_ERROR
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

class DownloaderService  constructor(
    private val context: Context,
    params: WorkerParameters,
    private val bookRepo: BookRepository,
private val chapterRepo: ChapterRepository,
private val remoteUseCases: RemoteUseCases,
private val localizeHelper: LocalizeHelper,
private val extensions: CatalogStore,
private val insertUseCases: ireader.domain.usecases.local.LocalInsertUseCases ,
private val defaultNotificationHelper: DefaultNotificationHelper,
private val downloadUseCases: DownloadUseCases,
private val downloadServiceState: DownloadServiceStateImpl,
private val notificationManager : NotificationManager ,
) : CoroutineWorker(context, params) {

    private val downloadJob = Job()
    val scope = createICoroutineScope(Dispatchers.Main.immediate + downloadJob)


    override suspend fun doWork(): Result {
        val builder = defaultNotificationHelper.baseNotificationDownloader(
            chapter = null,
            id
        )
        val inputtedChapterIds = inputData.getLongArray(DOWNLOADER_Chapters_IDS)?.distinct()
        val inputtedBooksIds = inputData.getLongArray(DOWNLOADER_BOOKS_IDS)?.distinct()
        val inputtedDownloaderMode = inputData.getBoolean(DOWNLOADER_MODE, false)
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
                notificationManager.cancel(ID_DOWNLOAD_CHAPTER_PROGRESS)
                notificationManager.show(
                    ID_DOWNLOAD_CHAPTER_ERROR,
                    defaultNotificationHelper.baseCancelledNotificationDownloader(
                        bookName = bookName,
                        error
                    ).build()
                )
            },
            onSuccess = {
                val notification = NotificationCompat.Builder(
                    applicationContext.applicationContext,
                    CHANNEL_DOWNLOADER_COMPLETE
                ).apply {
                    setContentTitle("Download was successfully completed.")
                    setSmallIcon(R.drawable.ic_downloading)
                    priority = NotificationCompat.PRIORITY_DEFAULT
                    setSubText("It was Downloaded Successfully")
                    setAutoCancel(true)
                    setContentIntent(defaultNotificationHelper.openDownloadsPendingIntent)
                }.build()
                notificationManager.show(
                    ID_DOWNLOAD_CHAPTER_COMPLETE,
                    notification
                )
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
                notificationManager.show(it, builder.build())
            }
        )
        return if (result) Result.success() else Result.failure()
    }
}