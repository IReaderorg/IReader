package ir.kazemcodes.infinity.feature_activity.domain.service

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
import ir.kazemcodes.infinity.core.domain.models.Book
import ir.kazemcodes.infinity.core.domain.repository.Repository
import ir.kazemcodes.infinity.core.utils.mappingSourceNameToSource
import ir.kazemcodes.infinity.feature_activity.domain.notification.Notifications
import ir.kazemcodes.infinity.feature_activity.domain.notification.Notifications.CHANNEL_DOWNLOADER_PROGRESS
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
        val book = repository.localBookRepository.getBookById(1).first().data
        val chapters = repository.localChapterRepository.getChapterByName(bookName, source = source).first().data

        val notification = NotificationCompat.Builder(applicationContext,
            Notifications.CHANNEL_DOWNLOADER_PROGRESS)
            .setContentTitle("Downloading ${book?.bookName}")
            .setSmallIcon(R.drawable.ic_downloading)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)


        NotificationManagerCompat.from(applicationContext).apply {
            notification.setProgress(chapters?.size ?: 0 , 0, false)
            notify(NOTIFICATION_ID, notification.build())

            try {
                repository.remoteRepository.downloadChapter(
                    book = book ?: Book.create(),
                    source = mappingSourceNameToSource(source),
                    chapters = chapters?: emptyList(),
                    factory = {
                        WebView(it).apply { settings.javaScriptEnabled = true }
                    }
                ).flowOn(Dispatchers.Main)
                    .collectIndexed { index, chapter ->
                        repository.localChapterRepository.updateChapter(chapter)
                        notification.setContentText(chapter.title.toString())
                        notification.setProgress(chapters?.size ?: 0 , index, false)
                        notify(NOTIFICATION_ID, notification.build())
                    }
            } catch (e: Exception) {
                Timber.e("getNotifications: Failed to download $book")
                notify(
                    kotlin.math.abs(nextInt()),
                    NotificationCompat.Builder(applicationContext, CHANNEL_DOWNLOADER_PROGRESS)
                        .apply {
                            setContentTitle("Failed to download ${book?.bookName}")
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

