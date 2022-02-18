package org.ireader.domain.feature_services.notification

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import org.ireader.domain.R
import org.ireader.domain.models.entities.Book
import org.ireader.domain.models.entities.Chapter
import org.ireader.domain.repository.LocalBookRepository
import org.ireader.domain.repository.LocalChapterRepository
import org.ireader.infinity.feature_services.flags
import timber.log.Timber

private const val CHANNEL_ID: String = "42000"

@HiltWorker
class NotificationWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val bookRepo: LocalBookRepository,
    private val chaptersRepo: LocalChapterRepository,
) : CoroutineWorker(context, params) {

    private val notificationId = 42069
    override suspend fun doWork(): Result {
        Notifications.createChannels(context = applicationContext)
        createChannel(
            applicationContext, Channel(
                name = applicationContext.getString(R.string.library_updates_notification),
                id = CHANNEL_ID
            )
        )
        getNotifications().collect {
            with(NotificationManagerCompat.from(applicationContext)) {
                notify(it.book.id.toInt(), it.createNotification())
            }
        }
        return Result.success()
    }

    private fun getNotifications() = flow {
        val books = bookRepo.subscribeAllInLibraryBooks().first()

        progressNotification(books) { book ->
            try {
                val lastGroup = chaptersRepo.subscribeChaptersByBookId(book.id).first()
                val refreshedGroups =
                    chaptersRepo.subscribeChaptersByBookId(book.id, true).first()
                val updateCount = refreshedGroups.lastChapter() - lastGroup.lastChapter()
                if (updateCount > 0)
                    emit(
                        BookNotification(
                            applicationContext, book, refreshedGroups, updateCount
                        )
                    )
            } catch (e: Exception) {
                Timber.e("getNotifications: Failed to query $book")
            }
        }
    }.flowOn(Dispatchers.IO)

    private fun List<Chapter>.lastChapter(): Int {
        return maxByOrNull { it.id }?.id?.toInt() ?: 0
    }

    private suspend fun progressNotification(
        books: List<Book>,
        action: suspend (Book) -> Unit,
    ) {
        val builder = NotificationCompat.Builder(applicationContext, CHANNEL_ID).apply {
            setContentTitle("Checking for updates")
            setSmallIcon(R.drawable.ic_infinity)
            setOnlyAlertOnce(true)
            priority = NotificationCompat.PRIORITY_LOW
        }

        NotificationManagerCompat.from(applicationContext).apply {
            builder.setProgress(books.size, 0, false)
            notify(notificationId, builder.build())

            books.onEachIndexed { counter, book ->
                builder.apply {
                    setContentText(book.title)
                    setProgress(books.size, counter, false)
                    notify(notificationId, build())
                }

                action(book)
            }

            builder.setProgress(0, 0, false)
            cancel(notificationId)
        }
    }

    private data class BookNotification(
        val context: Context,
        val book: Book,
        val chapters: List<Chapter>,
        val updateCount: Int,
    ) {

        private val lastChapter = chapters.maxByOrNull { it.id }!!
        private val text = "$updateCount new chapter${if (updateCount > 1) "s" else ""} available"

        private val groupKey = "com.ubadahj.qidianunderground.CHAPTER_UPDATES"


        suspend fun createNotification(): Notification =
            NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_infinity)
                .setContentTitle(book.title)
                .setContentText(text)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(createIntent(lastChapter))
                .addAction(R.drawable.ic_add, "Open Book", createIntent())
                .setAutoCancel(true)
                .setLargeIcon(context, book.cover)
                .setGroup(groupKey)
                .build()

        private fun createIntent(chapter: Chapter? = null) =
            PendingIntent.getActivity(
                context,
                hashCode() + chapter.hashCode(),
                Intent(context, context.applicationContext::class.java).apply {
                    putExtra("book", book.id)
                    putExtra("group", chapter?.link)
                },
                flags
            )!!

    }

}