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
import org.ireader.domain.repository.LocalChapterRepository
import org.ireader.domain.source.Extensions
import org.ireader.infinity.core.domain.repository.LocalBookRepository
import org.ireader.infinity.core.domain.use_cases.local.LocalInsertUseCases
import org.ireader.use_cases.remote.RemoteUseCases
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
) : CoroutineWorker(context, params) {
    companion object {
        const val DOWNLOADER_SERVICE_NAME = "DOWNLOAD_SERVICE"
        const val DOWNLOADER_BOOK_ID = "book_id"
        const val DOWNLOADER_SOURCE_ID = "sourceId"
    }


    override suspend fun doWork(): Result {


        val bookId = inputData.getInt("book_id", 0)
        val sourceId = inputData.getLong("sourceId", 0)
        val bookResource = bookRepo.getBookById(bookId).first()
        if (bookResource.uiText?.asString(context = context)?.isNotBlank() == true) {
            throw IllegalArgumentException(
                "Invalid bookId as argument: $bookId"
            )
        }
        val book = bookResource.data!!

        val source = extensions.mappingSourceNameToSource(sourceId)

        val chapters = chapterRepo.getChaptersByBookId(bookId).first() ?: emptyList()
        val localBook = bookRepo.getBookById(bookId).first().data

        withContext(Dispatchers.IO) {
            insertUseCases.insertBook(book = book.copy(isDownloaded = true, beingDownloaded = true))
        }


        val cancelDownloadIntent = WorkManager.getInstance(applicationContext)
            .createCancelPendingIntent(id)


        val builder =
            NotificationCompat.Builder(applicationContext, CHANNEL_DOWNLOADER_PROGRESS).apply {
                setContentTitle("Downloading ${book.bookName}")
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

                                notify(ID_DOWNLOAD_CHAPTER_PROGRESS, builder.build())
                            }
                        Timber.d("getNotifications: Successfully to downloaded ${book.bookName} chapter ${chapter.title}")
                        delay(2000)
                    }
                }
            } catch (e: CancellationException) {

                Timber.e("getNotifications:Download of ${book.bookName} was cancelled.")
                notify(
                    ID_DOWNLOAD_CHAPTER_ERROR,
                    NotificationCompat.Builder(applicationContext,
                        Notifications.CHANNEL_DOWNLOADER_ERROR).apply {
                        setContentTitle("Download of ${book.bookName} was canceled.")
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

                return Result.failure()
            } catch (e: Exception) {

                Timber.e("getNotifications: Failed to download ${book.bookName}")
                notify(
                    ID_DOWNLOAD_CHAPTER_ERROR,
                    NotificationCompat.Builder(applicationContext,
                        Notifications.CHANNEL_DOWNLOADER_ERROR).apply {
                        setContentTitle("Failed to download ${book.bookName}")
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

                return Result.failure()
            }

            builder.setProgress(0, 0, false)
            cancel(ID_DOWNLOAD_CHAPTER_PROGRESS)

            notify(
                ID_DOWNLOAD_CHAPTER_COMPLETE,
                NotificationCompat.Builder(applicationContext,
                    Notifications.CHANNEL_DOWNLOADER_COMPLETE).apply {
                    setContentTitle("${book.bookName} downloaded")
                    setSmallIcon(R.drawable.ic_downloading)
                    priority = NotificationCompat.PRIORITY_DEFAULT
                    setSubText("It was Downloaded Successfully")
                    setAutoCancel(true)
                    setContentIntent(defaultNotificationHelper.openBookDetailPendingIntent(bookId,
                        sourceId))
                }.build()
            )
        }

        withContext(Dispatchers.IO) {
            insertUseCases.insertBook(book = book.copy(beingDownloaded = false))
        }

        return Result.success()
    }

}