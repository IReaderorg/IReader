package ireader.domain.services.downloaderService

import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import ireader.core.util.createICoroutineScope
import ireader.i18n.R
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance

class DownloaderService  constructor(
    private val context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params), DIAware {

    override val di: DI = (this@DownloaderService.applicationContext as DIAware).di
    private val bookRepo: BookRepository by instance()
    private val chapterRepo: ChapterRepository by instance()
    private val remoteUseCases: RemoteUseCases by instance()
    private val localizeHelper: LocalizeHelper by instance()
    private val extensions: CatalogStore by instance()
    private val insertUseCases: ireader.domain.usecases.local.LocalInsertUseCases by instance()
    private val defaultNotificationHelper: DefaultNotificationHelper by instance()
    private val downloadUseCases: DownloadUseCases by instance()
    private val downloadServiceState: DownloadServiceStateImpl by instance()
    private val downloadJob = Job()
    private val notificationManager : NotificationManager  by instance()

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