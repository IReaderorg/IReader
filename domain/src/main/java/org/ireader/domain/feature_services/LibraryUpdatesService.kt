package org.ireader.domain.feature_services

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
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import org.ireader.core.R
import org.ireader.domain.catalog.interactor.GetLocalCatalog
import org.ireader.domain.feature_services.notification.DefaultNotificationHelper
import org.ireader.domain.feature_services.notification.Notifications
import org.ireader.domain.models.entities.Chapter
import org.ireader.domain.models.entities.Update
import org.ireader.domain.use_cases.local.LocalGetBookUseCases
import org.ireader.domain.use_cases.local.LocalGetChapterUseCase
import org.ireader.domain.use_cases.local.LocalInsertUseCases
import org.ireader.domain.use_cases.local.updates.InsertUpdatesUseCase
import org.ireader.domain.use_cases.remote.RemoteUseCases
import timber.log.Timber
import kotlin.time.Duration.Companion.minutes
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime

@HiltWorker
class LibraryUpdatesService @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted params: WorkerParameters,
    private val getBookUseCases: LocalGetBookUseCases,
    private val getChapterUseCase: LocalGetChapterUseCase,
    private val remoteUseCases: RemoteUseCases,
    private val getLocalCatalog: GetLocalCatalog,
    private val defaultNotificationHelper: DefaultNotificationHelper,
    private val insertUseCases: LocalInsertUseCases,
    private val updatesUseCase: InsertUpdatesUseCase,
) : CoroutineWorker(context, params) {

    companion object {
        const val LibraryUpdateTag = "Library_Update_SERVICE"
    }

    @OptIn(ExperimentalTime::class)
    override suspend fun doWork(): Result {
        val libraryBooks = getBookUseCases.findAllInLibraryBooks()

        val cancelIntent = WorkManager.getInstance(applicationContext)
            .createCancelPendingIntent(id)
        val builder =
            NotificationCompat.Builder(applicationContext,
                Notifications.CHANNEL_LIBRARY_PROGRESS).apply {
                setContentTitle("Checking Updates")
                setSmallIcon(R.drawable.ic_downloading)
                setOnlyAlertOnce(true)
                priority = NotificationCompat.PRIORITY_LOW
                setAutoCancel(true)
                setOngoing(true)
                addAction(R.drawable.baseline_close_24, "Cancel", cancelIntent)
                setContentIntent(defaultNotificationHelper.openDownloadsPendingIntent)
            }

        NotificationManagerCompat.from(applicationContext).apply {

            builder.setProgress(libraryBooks.size, 0, false)
            notify(Notifications.ID_LIBRARY_PROGRESS, builder.build())
            try {
                libraryBooks.forEachIndexed { index, book ->
                    if ((Clock.System.now()
                            .toEpochMilliseconds() - book.lastUpdated) < 5.minutes.toLong(
                            DurationUnit.MINUTES)
                    ) {
                        val chapters = getChapterUseCase.findChaptersByBookId(bookId = book.id)
                        val source = getLocalCatalog.get(book.sourceId)!!.source
                        val remoteChapters = mutableListOf<Chapter>()
                        remoteUseCases.getRemoteChapters(
                            book, source,
                            onSuccess = {
                                builder.setContentText(book.title)
                                builder.setSubText(index.toString())
                                builder.setProgress(libraryBooks.size, index, false)
                                notify(Notifications.ID_LIBRARY_PROGRESS, builder.build())
                                remoteChapters.addAll(it)
                            }, onError = {})
                        val newChapters =
                            remoteChapters.filterNot { chapter -> chapter.title in chapters.map { it.title } }
                        withContext(Dispatchers.IO) {
                            insertUseCases.insertChapters(newChapters.map {
                                it.copy(
                                    bookId = book.id,
                                    dateFetch = Clock.System.now().toEpochMilliseconds(),
                                )
                            })
                            insertUseCases.insertBook(book.copy(lastUpdated = Clock.System.now()
                                .toEpochMilliseconds()))
                            updatesUseCase(newChapters.map { Update.toUpdates(book, it) })
                        }

                    }
                }
            } catch (e: Exception) {
                Timber.e("getNotifications: Failed to Check for Book Update")
                notify(
                    Notifications.ID_LIBRARY_ERROR,
                    NotificationCompat.Builder(applicationContext,
                        Notifications.CHANNEL_LIBRARY_ERROR).apply {
                        if (e.localizedMessage == "Job was cancelled") {
                            setSubText("Download was cancelled")
                            setContentTitle("Library Updates was canceled.")
                        } else {
                            setContentTitle("Failed to Check Library Updates.")
                            setSubText(e.localizedMessage)
                        }
                        setSmallIcon(R.drawable.ic_downloading)
                        priority = NotificationCompat.PRIORITY_DEFAULT
                        setAutoCancel(true)
                    }.build()
                )
                builder.setProgress(0, 0, false)
                cancel(Notifications.ID_LIBRARY_ERROR)
                withContext(Dispatchers.IO) {

                }
                return Result.failure()
            }

            builder.setProgress(0, 0, false)
            cancel(Notifications.ID_LIBRARY_ERROR)
            withContext(Dispatchers.IO) {

            }
            notify(
                Notifications.ID_LIBRARY_PROGRESS,
                NotificationCompat.Builder(applicationContext,
                    Notifications.CHANNEL_DOWNLOADER_COMPLETE).apply {
                    setContentTitle("${libraryBooks.size} book was update successfully.")
                    setSmallIcon(R.drawable.ic_downloading)
                    priority = NotificationCompat.PRIORITY_DEFAULT
                    setSubText("It was Updated Successfully")
                    setAutoCancel(true)

                }.build()
            )
        }

        return Result.success()
    }

}