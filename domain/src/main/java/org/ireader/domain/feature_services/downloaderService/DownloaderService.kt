package org.ireader.domain.feature_services.downloaderService

import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.ireader.core.R
import org.ireader.domain.catalog.service.CatalogStore
import org.ireader.domain.feature_services.notification.DefaultNotificationHelper
import org.ireader.domain.feature_services.notification.Notifications
import org.ireader.domain.feature_services.notification.Notifications.CHANNEL_DOWNLOADER_PROGRESS
import org.ireader.domain.feature_services.notification.Notifications.ID_DOWNLOAD_CHAPTER_COMPLETE
import org.ireader.domain.feature_services.notification.Notifications.ID_DOWNLOAD_CHAPTER_ERROR
import org.ireader.domain.feature_services.notification.Notifications.ID_DOWNLOAD_CHAPTER_PROGRESS
import org.ireader.domain.models.entities.Chapter
import org.ireader.domain.models.entities.SavedDownload
import org.ireader.domain.repository.LocalBookRepository
import org.ireader.domain.repository.LocalChapterRepository
import org.ireader.domain.use_cases.download.DownloadUseCases
import org.ireader.domain.use_cases.local.LocalInsertUseCases
import org.ireader.domain.use_cases.remote.RemoteUseCases
import timber.log.Timber


@HiltWorker
class DownloadService @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted params: WorkerParameters,
    private val bookRepo: LocalBookRepository,
    private val chapterRepo: LocalChapterRepository,
    private val remoteUseCases: RemoteUseCases,
    private val extensions: CatalogStore,
    private val insertUseCases: LocalInsertUseCases,
    private val defaultNotificationHelper: DefaultNotificationHelper,
    private val downloadUseCases: DownloadUseCases,
) : CoroutineWorker(context, params) {
    companion object {
        const val DOWNLOADER_SERVICE_NAME = "DOWNLOAD_SERVICE"
        const val DOWNLOADER_BOOK_ID = "book_id"
        const val DOWNLOADER_SOURCE_ID = "sourceId"
        const val DOWNLOADER_Chapters_IDS = "chapterIds"
        const val DOWNLOADER_BOOKS_IDS = "booksIds"
    }


    lateinit var savedDownload: SavedDownload
    override suspend fun doWork(): Result {


        val bookId = inputData.getLong("book_id", 0)
        val sourceId = inputData.getLong("sourceId", 0)
        val downloadIds = inputData.getLongArray(DOWNLOADER_Chapters_IDS)?.distinct()
        val booksIds = inputData.getLongArray(DOWNLOADER_BOOKS_IDS)?.distinct()
        val bookResource = bookRepo.subscribeBookById(bookId).first()
            ?: throw IllegalArgumentException(
                "Invalid bookId as argument: $bookId"
            )
        //Faking
        savedDownload = SavedDownload(
            bookId = bookId,
            totalChapter = 100,
            priority = 1,
            chapterName = "",
            chapterKey = "",
            progress = 100,
            translator = "",
            chapterId = 0,
            bookName = bookResource.title,
            sourceId = bookResource.sourceId,
        )

        val source = extensions.get(sourceId)?.source

        val chapters = mutableListOf<Chapter>()

        if (booksIds?.isNotEmpty() == true) {
            booksIds.forEach {
                chapters.addAll(chapterRepo.findChaptersByBookId(it))
            }
        } else {
            chapters.addAll(chapterRepo.findChaptersByBookId(bookId).filter {
                if (downloadIds != null) {
                    it.id in downloadIds
                } else {
                    true
                }
            })
        }


        val cancelDownloadIntent = WorkManager.getInstance(applicationContext)
            .createCancelPendingIntent(id)

        val builder =
            NotificationCompat.Builder(applicationContext, CHANNEL_DOWNLOADER_PROGRESS).apply {
                setContentTitle("Downloading ${bookResource.title}")
                setSmallIcon(R.drawable.ic_downloading)
                setOnlyAlertOnce(true)
                priority = NotificationCompat.PRIORITY_LOW
                setAutoCancel(true)
                setOngoing(true)
                addAction(R.drawable.baseline_close_24, "Cancel", cancelDownloadIntent)
                setContentIntent(defaultNotificationHelper.openDownloadsPendingIntent)
            }


        NotificationManagerCompat.from(applicationContext).apply {

            builder.setProgress(chapters.size, 0, false)
            notify(ID_DOWNLOAD_CHAPTER_PROGRESS, builder.build())
            try {
                chapters.forEachIndexed { index, chapter ->
                    if (chapter.content.joinToString().length < 10) {
                        remoteUseCases.getRemoteReadingContent(
                            chapter = chapter,
                            source = source!!,
                            onSuccess = { content ->
                                withContext(Dispatchers.IO) {
                                    insertUseCases.insertChapter(chapter = content)

                                }
                                builder.setContentText(chapter.title)
                                builder.setSubText(index.toString())
                                builder.setProgress(chapters.size, index, false)
                                savedDownload = savedDownload.copy(
                                    bookId = bookId,
                                    totalChapter = chapters.size,
                                    priority = 1,
                                    chapterName = chapter.title,
                                    chapterKey = chapter.link,
                                    progress = index,
                                    translator = chapter.translator,
                                    chapterId = chapter.id,
                                    bookName = bookResource.title,
                                    sourceId = bookResource.sourceId,
                                )
                                notify(ID_DOWNLOAD_CHAPTER_PROGRESS, builder.build())
                                withContext(Dispatchers.IO) {
                                    downloadUseCases.insertDownload(savedDownload.copy(priority = 1))
                                }


                            },
                            onError = { message ->
                                if (message?.asString(context)?.isNotBlank() == true) {
                                    throw Exception(message.asString(context))
                                }
                            }
                        )
                        Timber.d("getNotifications: Successfully to downloaded ${bookResource.title} chapter ${chapter.title}")
                        delay(1000)
                    }
                }

            } catch (e: Exception) {
                Timber.e("getNotifications: Failed to download ${bookResource.title}")
                notify(
                    ID_DOWNLOAD_CHAPTER_ERROR,
                    NotificationCompat.Builder(applicationContext,
                        Notifications.CHANNEL_DOWNLOADER_ERROR).apply {
                        if (e.localizedMessage == "Job was cancelled") {
                            setSubText("Download was cancelled")
                            setContentTitle("Download of ${bookResource.title} was canceled.")
                        } else {
                            setContentTitle("Failed to download ${bookResource.title}")
                            setSubText(e.localizedMessage)
                        }
                        setSmallIcon(R.drawable.ic_downloading)
                        priority = NotificationCompat.PRIORITY_DEFAULT
                        setAutoCancel(true)
                        setContentIntent(defaultNotificationHelper.openBookDetailPendingIntent(
                            bookId,
                            sourceId))
                    }.build()
                )
                builder.setProgress(0, 0, false)
                cancel(ID_DOWNLOAD_CHAPTER_PROGRESS)
                withContext(Dispatchers.IO) {
                    downloadUseCases.insertDownload(savedDownload.copy(priority = 0))
                }
                return Result.failure()
            }

            builder.setProgress(0, 0, false)
            cancel(ID_DOWNLOAD_CHAPTER_PROGRESS)
            withContext(Dispatchers.IO) {
                downloadUseCases.insertDownload(savedDownload.copy(priority = 0))
            }
            notify(
                ID_DOWNLOAD_CHAPTER_COMPLETE,
                NotificationCompat.Builder(applicationContext,
                    Notifications.CHANNEL_DOWNLOADER_COMPLETE).apply {
                    setContentTitle("${bookResource.title} downloaded")
                    setSmallIcon(R.drawable.ic_downloading)
                    priority = NotificationCompat.PRIORITY_DEFAULT
                    setSubText("It was Downloaded Successfully")
                    setAutoCancel(true)
                    setContentIntent(defaultNotificationHelper.openBookDetailPendingIntent(bookId,
                        sourceId))
                }.build()
            )
        }

        return Result.success()
    }


}