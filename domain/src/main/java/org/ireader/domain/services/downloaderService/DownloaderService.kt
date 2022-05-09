package org.ireader.domain.services.downloaderService

import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.ireader.common_extensions.launchIO
import org.ireader.common_models.entities.SavedDownload
import org.ireader.common_models.entities.buildSavedDownload
import org.ireader.core.R
import org.ireader.core_api.log.Log
import org.ireader.core_catalogs.CatalogStore
import org.ireader.domain.notification.Notifications
import org.ireader.domain.notification.Notifications.ID_DOWNLOAD_CHAPTER_COMPLETE
import org.ireader.domain.notification.Notifications.ID_DOWNLOAD_CHAPTER_ERROR
import org.ireader.domain.notification.Notifications.ID_DOWNLOAD_CHAPTER_PROGRESS
import org.ireader.domain.use_cases.download.DownloadUseCases
import org.ireader.domain.use_cases.local.LocalInsertUseCases
import org.ireader.domain.use_cases.remote.RemoteUseCases

@HiltWorker
class DownloaderService @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted params: WorkerParameters,
    private val bookRepo: org.ireader.common_data.repository.LocalBookRepository,
    private val chapterRepo: org.ireader.common_data.repository.LocalChapterRepository,
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
            sourceId = 0,
        )

    override suspend fun doWork(): Result {
        NotificationManagerCompat.from(applicationContext.applicationContext).apply {
            try {
                val inputtedChapterIds = inputData.getLongArray(DOWNLOADER_Chapters_IDS)?.distinct()
                val inputtedBooksIds = inputData.getLongArray(DOWNLOADER_BOOKS_IDS)?.distinct()
                val inputtedDownloaderMode = inputData.getBoolean(DOWNLOADER_MODE, false)

                val chapters = if (inputtedBooksIds != null) {
                    chapterRepo.findChaptersByBookIds(inputtedBooksIds)
                } else {
                    if (inputtedChapterIds != null) {
                        chapterRepo.findChapterByIdByBatch(inputtedChapterIds)
                    } else {
                        throw Exception("There is no chapter.")
                    }
                }
                val distinctBookIds = chapters.map { it.bookId }.distinct()
                val books = bookRepo.findBookByIds(distinctBookIds)
                val distinctSources = books.map { it.sourceId }.distinct()
                val sources =
                    extensions.catalogs.filter { it.sourceId in distinctSources }

                val mappedChapters =
                    chapters.filter { it.content.joinToString().length < 10 }.map { chapter ->
                        val book = books.find { book -> book.id == chapter.bookId }
                        book?.let { b ->
                            buildSavedDownload(b, chapter)
                        }
                    }.filterNotNull()

                val downloadIds = downloadUseCases.insertDownloads(mappedChapters)

                val downloads = mappedChapters // downloadUseCases.findDownloadsUseCase(downloadIds)
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
                        sources.find { it.sourceId == download.sourceId }?.let { source ->
                            if (chapter.content.joinToString().length < 10) {
                                remoteUseCases.getRemoteReadingContent(
                                    chapter = chapter,
                                    catalog = source,
                                    onSuccess = { content ->
                                        withContext(Dispatchers.IO) {
                                            insertUseCases.insertChapter(chapter = content)
                                        }
                                        builder.setContentText(chapter.title)
                                        builder.setSubText(index.toString())

                                        builder.setProgress(downloads.size, index, false)
                                        notify(ID_DOWNLOAD_CHAPTER_PROGRESS, builder.build())
                                        savedDownload = savedDownload.copy(
                                            bookId = download.bookId,
                                            priority = 1,
                                            chapterName = chapter.title,
                                            chapterKey = chapter.link,
                                            translator = chapter.translator,
                                            chapterId = chapter.id,
                                            bookName = download.bookName,
                                            sourceId = download.sourceId,
                                        )
                                        withContext(Dispatchers.IO) {
                                            downloadUseCases.insertDownload(
                                                savedDownload.copy(
                                                    priority = 1
                                                )
                                            )
                                        }
                                    },
                                    onError = { message ->
                                        throw Exception(message?.asString(context))
                                    }
                                )
                            }
                        }
                    }
                    Log.debug { "getNotifications: Successfully to downloaded ${savedDownload.bookName} chapter ${savedDownload.chapterName}" }
                    delay(1000)
                }
            } catch (e: Throwable) {
                Log.error { "getNotifications: Failed to download ${savedDownload.chapterName}" }

                cancel(ID_DOWNLOAD_CHAPTER_PROGRESS)
                notify(
                    ID_DOWNLOAD_CHAPTER_ERROR,
                    defaultNotificationHelper.baseCancelledNotificationDownloader(
                        bookName = savedDownload.bookName,
                        e
                    ).build()
                )

                scope.launchIO {
                    downloadUseCases.insertDownload(savedDownload.copy(priority = 0))
                }
                downloadServiceState.downloads = emptyList()
                downloadServiceState.isEnable = false
                return Result.failure()
            }

            withContext(Dispatchers.Main) {
                cancel(ID_DOWNLOAD_CHAPTER_PROGRESS)
                withContext(Dispatchers.IO) {
                    downloadUseCases.insertDownload(savedDownload.copy(priority = 0))
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
//
//
//
//            NotificationManagerCompat.from(applicationContext.applicationContext).apply {
//
//                builder.setProgress(chapters.size, 0, false)
//                notify(ID_DOWNLOAD_CHAPTER_PROGRESS, builder.build())
//                try {
//                    chapters.forEachIndexed { index, chapter ->
//                        if (chapter.content.joinToString().length < 10) {
//                            remoteUseCases.getRemoteReadingContent(
//                                chapter = chapter,
//                                source = source!!,
//                                onSuccess = { content ->
//                                    withContext(Dispatchers.IO) {
//                                        insertUseCases.insertChapter(chapter = content)
//
//                                    }
//                                    builder.setContentText(chapter.title)
//                                    builder.setSubText(index.toString())
//                                    builder.setProgress(chapters.size, index, false)
//                                    savedDownload = savedDownload.copy(
//                                        bookId = bookId,
//                                        totalChapter = chapters.size,
//                                        priority = 1,
//                                        chapterName = chapter.title,
//                                        chapterKey = chapter.link,
//                                        progress = index,
//                                        translator = chapter.translator,
//                                        chapterId = chapter.id,
//                                        bookName = bookResource.title,
//                                        sourceId = bookResource.sourceId,
//                                    )
//                                    notify(ID_DOWNLOAD_CHAPTER_PROGRESS, builder.build())
//                                    withContext(Dispatchers.IO) {
//                                        downloadUseCases.insertDownload(savedDownload.copy(priority = 1))
//                                    }
//
//
//                                },
//                                onError = { message ->
//                                    if (message?.asString(context)?.isNotBlank() == true) {
//                                        throw Exception(message.asString(context))
//                                    }
//                                }
//                            )
//                            Timber.d("getNotifications: Successfully to downloaded ${bookResource.title} chapter ${chapter.title}")
//                            delay(1000)
//                        }
//                    }
//
//                } catch (e: Throwable) {
//                    Timber.e("getNotifications: Failed to download ${bookResource.title}")
//                    cancel(ID_DOWNLOAD_CHAPTER_PROGRESS)
//                    notify(ID_DOWNLOAD_CHAPTER_ERROR,
//                        defaultNotificationHelper.baseCancelledNotificationDownloader(book = bookResource,
//                            e).build())
//
//                    scope.launchIO {
//                        downloadUseCases.insertDownload(savedDownload.copy(priority = 0))
//                    }
//                    return Result.failure()
//                }
//                withContext(Dispatchers.Main) {
//
//                    builder.setProgress(0, 0, false)
//                    cancel(ID_DOWNLOAD_CHAPTER_PROGRESS)
//                    withContext(Dispatchers.IO) {
//                        downloadUseCases.insertDownload(savedDownload.copy(priority = 0))
//                    }
//                    notify(
//                        ID_DOWNLOAD_CHAPTER_COMPLETE,
//                        NotificationCompat.Builder(applicationContext,
//                            Notifications.CHANNEL_DOWNLOADER_COMPLETE).apply {
//                            setContentTitle("${bookResource.title} downloaded")
//                            setSmallIcon(R.drawable.ic_downloading)
//                            priority = NotificationCompat.PRIORITY_DEFAULT
//                            setSubText("It was Downloaded Successfully")
//                            setAutoCancel(true)
//                            setContentIntent(defaultNotificationHelper.openBookDetailPendingIntent(
//                                bookId,
//                                bookResource.sourceId))
//                        }.build()
//                    )
//                }
//            }
//
//        }
//        return Result.success()
    }
}
