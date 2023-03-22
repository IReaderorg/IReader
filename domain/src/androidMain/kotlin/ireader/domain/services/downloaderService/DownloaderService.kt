package ireader.domain.services.downloaderService

import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import ireader.core.util.createICoroutineScope
import ireader.domain.R
import ireader.domain.catalogs.CatalogStore
import ireader.domain.data.repository.BookRepository
import ireader.domain.data.repository.ChapterRepository
import ireader.domain.models.entities.Chapter
import ireader.domain.models.entities.SavedDownload
import ireader.domain.models.entities.buildSavedDownload
import ireader.domain.notification.Notifications
import ireader.domain.notification.Notifications.ID_DOWNLOAD_CHAPTER_COMPLETE
import ireader.domain.notification.Notifications.ID_DOWNLOAD_CHAPTER_ERROR
import ireader.domain.notification.Notifications.ID_DOWNLOAD_CHAPTER_PROGRESS
import ireader.domain.services.downloaderService.DownloadServiceStateImpl.Companion.DOWNLOADER_BOOKS_IDS
import ireader.domain.services.downloaderService.DownloadServiceStateImpl.Companion.DOWNLOADER_Chapters_IDS
import ireader.domain.services.downloaderService.DownloadServiceStateImpl.Companion.DOWNLOADER_MODE
import ireader.domain.usecases.download.DownloadUseCases
import ireader.domain.usecases.remote.RemoteUseCases
import ireader.domain.utils.NotificationManager
import ireader.i18n.LocalizeHelper
import ireader.i18n.asString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
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



    var savedDownload: SavedDownload =
        SavedDownload(
            bookId = 0,
            priority = 1,
            chapterName = "",
            chapterKey = "",
            translator = "",
            chapterId = 0,
            bookName = "",
        )

    override suspend fun doWork(): Result {
        try {
            val inputtedChapterIds = inputData.getLongArray(DOWNLOADER_Chapters_IDS)?.distinct()
            val inputtedBooksIds = inputData.getLongArray(DOWNLOADER_BOOKS_IDS)?.distinct()
            val inputtedDownloaderMode = inputData.getBoolean(DOWNLOADER_MODE, false)

            var tries = 0

            val chapters: List<Chapter> = when {
                inputtedBooksIds != null -> {
                    inputtedBooksIds.flatMap {
                        chapterRepo.findChaptersByBookId(it)
                    }
                }
                inputtedChapterIds != null -> {
                    inputtedChapterIds.mapNotNull {
                        chapterRepo.findChapterById(it)
                    }.filterNotNull()
                }
                inputtedDownloaderMode -> {
                    downloadUseCases.findAllDownloadsUseCase().mapNotNull {
                        chapterRepo.findChapterById(it.chapterId)
                    }
                }
                else -> {
                    throw Exception("There is no chapter.")
                }
            }
            val distinctBookIds = chapters.map { it.bookId }.distinct()
            val books = distinctBookIds.mapNotNull {
                bookRepo.findBookById(it)
            }
            val distinctSources = books.map { it.sourceId }.distinct().filterNotNull()
            val sources =
                extensions.catalogs.filter { it.sourceId in distinctSources }

            val downloads =
                chapters.filter { it.content.joinToString().length < 10 }
                    .mapNotNull { chapter ->
                        val book = books.find { book -> book.id == chapter.bookId }
                        book?.let { b ->
                            buildSavedDownload(b, chapter)
                        }
                    }

            downloadUseCases.insertDownloads(downloads.map { it.toDownload() })

            val builder = defaultNotificationHelper.baseNotificationDownloader(
                chapter = null,
                id
            )
            builder.setProgress(downloads.size, 0, true)
            notificationManager.show(ID_DOWNLOAD_CHAPTER_PROGRESS, builder.build())
            downloadServiceState.downloads = downloads
            downloadServiceState.isEnable = true
            downloads.forEachIndexed { index, download ->
                chapters.find { it.id == download.chapterId }?.let { chapter ->
                    sources.find { it.sourceId == books.find { it.id == chapter.bookId }?.sourceId }
                        ?.let { source ->
                            if (chapter.content.joinToString().length < 10) {
                                remoteUseCases.getRemoteReadingContent(
                                    chapter = chapter,
                                    catalog = source,
                                    onSuccess = { content ->
                                        withContext(Dispatchers.IO) {
                                            insertUseCases.insertChapter(chapter = content)
                                        }
                                        builder.setContentText(chapter.name)
                                        builder.setSubText(index.toString())

                                        builder.setProgress(downloads.size, index, false)
                                        notificationManager.show(ID_DOWNLOAD_CHAPTER_PROGRESS, builder.build())
                                        savedDownload = savedDownload.copy(
                                            bookId = download.bookId,
                                            priority = 1,
                                            chapterName = chapter.name,
                                            chapterKey = chapter.key,
                                            translator = chapter.translator,
                                            chapterId = chapter.id,
                                            bookName = download.bookName,
                                        )
                                        withContext(Dispatchers.IO) {
                                            downloadUseCases.insertDownload(
                                                savedDownload.copy(
                                                    priority = 1
                                                ).toDownload()
                                            )
                                        }
                                    },
                                    onError = { message ->
                                        tries++
                                        if (tries > 3) {
                                            throw Exception(message?.asString(localizeHelper))
                                        }

                                    }
                                )
                            }
                        }
                }
                ireader.core.log.Log.debug { "getNotifications: Successfully to downloaded ${savedDownload.bookName} chapter ${savedDownload.chapterName}" }
                delay(1000)
            }
        } catch (e: Throwable) {
            ireader.core.log.Log.error { "getNotifications: Failed to download ${savedDownload.chapterName}" }
            notificationManager.cancel(ID_DOWNLOAD_CHAPTER_PROGRESS)
            notificationManager.show(ID_DOWNLOAD_CHAPTER_ERROR,
                defaultNotificationHelper.baseCancelledNotificationDownloader(
                    bookName = savedDownload.bookName,
                    e
                ).build())
            downloadUseCases.insertDownload(savedDownload.copy(priority = 0).toDownload())
            downloadServiceState.downloads = emptyList()
            downloadServiceState.isEnable = false
            return Result.failure()
        }

        withContext(Dispatchers.Main) {
            notificationManager.cancel(ID_DOWNLOAD_CHAPTER_PROGRESS)
            withContext(Dispatchers.IO) {
                downloadUseCases.insertDownload(savedDownload.copy(priority = 0).toDownload())
            }
            val notification = NotificationCompat.Builder(
                applicationContext.applicationContext,
                Notifications.CHANNEL_DOWNLOADER_COMPLETE
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
        }
        downloadServiceState.downloads = emptyList()
        downloadServiceState.isEnable = false
        return Result.success()
    }
}
