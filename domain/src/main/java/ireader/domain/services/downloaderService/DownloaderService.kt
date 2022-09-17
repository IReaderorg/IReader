package ireader.domain.services.downloaderService

import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import ireader.domain.data.repository.BookRepository
import ireader.domain.data.repository.ChapterRepository
import ireader.domain.utils.extensions.launchIO
import ireader.common.models.entities.Chapter
import ireader.common.models.entities.SavedDownload
import ireader.common.models.entities.buildSavedDownload
import ireader.common.resources.asString
import ireader.domain.catalogs.CatalogStore
import ireader.domain.R
import ireader.domain.notification.Notifications
import ireader.domain.notification.Notifications.ID_DOWNLOAD_CHAPTER_COMPLETE
import ireader.domain.notification.Notifications.ID_DOWNLOAD_CHAPTER_ERROR
import ireader.domain.notification.Notifications.ID_DOWNLOAD_CHAPTER_PROGRESS
import ireader.domain.usecases.download.DownloadUseCases
import ireader.domain.usecases.local.LocalInsertUseCases
import ireader.domain.usecases.remote.RemoteUseCases
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

class DownloaderService  constructor(
    private val context: Context,
    params: WorkerParameters,
    private val bookRepo: BookRepository,
    private val chapterRepo: ChapterRepository,
    private val remoteUseCases: RemoteUseCases,
    private val extensions: CatalogStore,
    private val insertUseCases: LocalInsertUseCases,
    private val defaultNotificationHelper: DefaultNotificationHelper,
    private val downloadUseCases: DownloadUseCases,
    private val downloadServiceState: DownloadServiceStateImpl,
) : CoroutineWorker(context, params) {

    private val downloadJob = Job()

    val scope = CoroutineScope(Dispatchers.Main.immediate + downloadJob)

    companion object {
        const val DOWNLOADER_SERVICE_NAME = "DOWNLOAD_SERVICE"
        const val DOWNLOADER_Chapters_IDS = "chapterIds"
        const val DOWNLOADER_MODE = "downloader_mode"
        const val DOWNLOADER_BOOKS_IDS = "booksIds"
    }

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
        NotificationManagerCompat.from(applicationContext.applicationContext).apply {
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
                notify(ID_DOWNLOAD_CHAPTER_PROGRESS, builder.build())
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
                                            notify(ID_DOWNLOAD_CHAPTER_PROGRESS, builder.build())
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
                                                throw Exception(message?.asString(context))
                                            }

                                        }
                                    )
                                }
                            }
                    }
                    ireader.core.api.log.Log.debug { "getNotifications: Successfully to downloaded ${savedDownload.bookName} chapter ${savedDownload.chapterName}" }
                    delay(1000)
                }
            } catch (e: Throwable) {
                ireader.core.api.log.Log.error { "getNotifications: Failed to download ${savedDownload.chapterName}" }
                cancel(ID_DOWNLOAD_CHAPTER_PROGRESS)
                notify(
                    ID_DOWNLOAD_CHAPTER_ERROR,
                    defaultNotificationHelper.baseCancelledNotificationDownloader(
                        bookName = savedDownload.bookName,
                        e
                    ).build()
                )
                downloadUseCases.insertDownload(savedDownload.copy(priority = 0).toDownload())
                downloadServiceState.downloads = emptyList()
                downloadServiceState.isEnable = false
                return Result.failure()
            }

            withContext(Dispatchers.Main) {
                cancel(ID_DOWNLOAD_CHAPTER_PROGRESS)
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
                notify(
                    ID_DOWNLOAD_CHAPTER_COMPLETE,
                    notification
                )
            }
        }
        downloadServiceState.downloads = emptyList()
        downloadServiceState.isEnable = false
        return Result.success()
    }
}
