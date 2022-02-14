package org.ireader.domain.feature_services.DownloaderService

import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectIndexed
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import org.ireader.core.R
import org.ireader.domain.feature_services.notification.DefaultNotificationHelper
import org.ireader.domain.feature_services.notification.Notifications
import org.ireader.domain.feature_services.notification.Notifications.CHANNEL_DOWNLOADER_PROGRESS
import org.ireader.domain.feature_services.notification.Notifications.ID_DOWNLOAD_CHAPTER_COMPLETE
import org.ireader.domain.feature_services.notification.Notifications.ID_DOWNLOAD_CHAPTER_ERROR
import org.ireader.domain.feature_services.notification.Notifications.ID_DOWNLOAD_CHAPTER_PROGRESS
import org.ireader.domain.models.entities.SavedDownload
import org.ireader.domain.repository.LocalBookRepository
import org.ireader.domain.repository.LocalChapterRepository
import org.ireader.domain.source.Extensions
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
    private val extensions: Extensions,
    private val insertUseCases: LocalInsertUseCases,
    private val defaultNotificationHelper: DefaultNotificationHelper,
    private val downloadUseCases: DownloadUseCases,
) : CoroutineWorker(context, params) {
    companion object {
        const val DOWNLOADER_SERVICE_NAME = "DOWNLOAD_SERVICE"
        const val DOWNLOADER_BOOK_ID = "book_id"
        const val DOWNLOADER_SOURCE_ID = "sourceId"
    }


    lateinit var savedDownload: SavedDownload
    override suspend fun doWork(): Result {


        val bookId = inputData.getLong("book_id", 0)
        val sourceId = inputData.getLong("sourceId", 0)
        val bookResource = bookRepo.getBookById(bookId).first()
            ?: throw IllegalArgumentException(
                "Invalid bookId as argument: $bookId"
            )
        savedDownload = SavedDownload(
            id = bookId,
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

        val source = extensions.mappingSourceNameToSource(sourceId)

        val chapters = chapterRepo.getChaptersByBookId(bookId).first()

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
                        remoteUseCases.getRemoteReadingContent(chapter = chapter, source = source)
                            .flowOn(Dispatchers.Main)
                            .collectIndexed { i, chapterPage ->
                                if (chapterPage.data != null) {
                                    insertUseCases.insertChapter(chapter = chapter.copy(content = chapterPage.data.content))
                                }
                                if (chapterPage.uiText?.asString(context)?.isNotBlank() == true) {
                                    throw Exception(chapterPage.uiText.asString(context))
                                }
                                builder.setContentText(chapter.title)
                                builder.setSubText(index.toString())
                                builder.setProgress(chapters.size, index, false)
                                savedDownload = savedDownload.copy(
                                    id = bookId,
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
                            }
                        Timber.d("getNotifications: Successfully to downloaded ${bookResource.title} chapter ${chapter.title}")
                        delay(2000)
                    }
                }
            } catch (e: CancellationException) {

                Timber.e("getNotifications:Download of ${bookResource.title} was cancelled.")
                notify(
                    ID_DOWNLOAD_CHAPTER_ERROR,
                    NotificationCompat.Builder(applicationContext,
                        Notifications.CHANNEL_DOWNLOADER_ERROR).apply {
                        setContentTitle("Download of ${bookResource.title} was canceled.")
                        setSubText("Download was cancelled")
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
            } catch (e: Exception) {
                Timber.e("getNotifications: Failed to download ${bookResource.title}")
                notify(
                    ID_DOWNLOAD_CHAPTER_ERROR,
                    NotificationCompat.Builder(applicationContext,
                        Notifications.CHANNEL_DOWNLOADER_ERROR).apply {
                        setContentTitle("Failed to download ${bookResource.title}")
                        setSubText(e.localizedMessage)
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
        //TODO create a viewmodel that store the download preccess
        return Result.success()
    }


}