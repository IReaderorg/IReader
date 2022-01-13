package ir.kazemcodes.infinity.service

import android.content.Context
import android.webkit.WebView
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import ir.kazemcodes.infinity.R
import ir.kazemcodes.infinity.domain.repository.Repository
import ir.kazemcodes.infinity.notification.Notifications
import ir.kazemcodes.infinity.notification.Notifications.CHANNEL_DOWNLOADER_PROGRESS
import ir.kazemcodes.infinity.util.SourceMapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectIndexed
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import timber.log.Timber
import kotlin.random.Random.Default.nextInt


@HiltWorker
class DownloadService @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val repository: Repository,
) : CoroutineWorker(context, params) {

    companion object {

        const val DOWNLOAD_SERVICE_NAME = "DOWNLOAD_SERVICE"
        const val NOTIFICATION_ID = 1

        const val DOWNLOAD_BOOK_NAME = "DOWNLOAD_BOOK_NAME"
        const val DOWNLOAD_SOURCE_NAME = "DOWNLOAD_SOURCE_NAME"

        const val DOWNLOADED_CHAPTER = "DOWNLOADED_CHAPTER"

    }


    override suspend fun doWork(): Result {
        val bookName = inputData.getString(DOWNLOAD_BOOK_NAME)!!
        val source = inputData.getString(DOWNLOAD_SOURCE_NAME)!!
        val book = repository.localBookRepository.getBookByName(bookName).first()!!
        val chapters = repository.localChapterRepository.getChapterByName(bookName, source = source).first()
            .map { it.toChapter() }

        val notification = NotificationCompat.Builder(applicationContext,
            Notifications.CHANNEL_DOWNLOADER_PROGRESS)
            .setContentTitle("Downloading ${book.bookName}")
            .setSmallIcon(R.drawable.ic_downloading)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)


        NotificationManagerCompat.from(applicationContext).apply {
            notification.setProgress(chapters.size, 0, false)
            notify(NOTIFICATION_ID, notification.build())

            try {
                repository.remoteRepository.downloadChapter(
                    book = book.toBook(),
                    source = SourceMapper(context = applicationContext).mappingSourceNameToSource(source),
                    chapters = chapters,
                    factory = {
                        WebView(it).apply { settings.javaScriptEnabled = true }
                    }
                ).flowOn(Dispatchers.Main)
                    .collectIndexed { index, chapter ->
                        repository.localChapterRepository.updateChapter(chapter.toChapterEntity())
                        notification.setContentText(chapter.title.toString())
                        notification.setProgress(chapters.size, index, false)
                        notify(NOTIFICATION_ID, notification.build())
                    }
            } catch (e: Exception) {
                Timber.e("getNotifications: Failed to download $book")
                notify(
                    kotlin.math.abs(nextInt()),
                    NotificationCompat.Builder(applicationContext, CHANNEL_DOWNLOADER_PROGRESS)
                        .apply {
                            setContentTitle("Failed to download ${book.bookName}")
                            setSmallIcon(R.raw.downloading)
                            priority = NotificationCompat.PRIORITY_DEFAULT
                        }.build()
                )
                notification.setProgress(0, 0, false)
                cancel(NOTIFICATION_ID)
                return Result.failure()
            }
            val output = workDataOf(DOWNLOADED_CHAPTER to chapters)
            return Result.success(output)
        }

    }
}

