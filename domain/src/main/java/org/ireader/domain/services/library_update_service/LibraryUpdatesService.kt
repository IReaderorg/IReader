package org.ireader.domain.services.library_update_service

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
import org.ireader.common_models.entities.Chapter
import org.ireader.common_models.entities.Update
import org.ireader.core.R
import org.ireader.core_api.log.Log
import org.ireader.core_catalogs.interactor.GetLocalCatalog
import org.ireader.domain.notification.Notifications
import org.ireader.domain.services.downloaderService.DefaultNotificationHelper
import org.ireader.domain.use_cases.local.DeleteUseCase
import org.ireader.domain.use_cases.local.LocalGetBookUseCases
import org.ireader.domain.use_cases.local.LocalGetChapterUseCase
import org.ireader.domain.use_cases.local.LocalInsertUseCases
import org.ireader.domain.use_cases.local.updates.InsertUpdatesUseCase
import org.ireader.domain.use_cases.remote.RemoteUseCases
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
    private val deleteUseCase: DeleteUseCase,
) : CoroutineWorker(context, params) {

    companion object {
        const val LibraryUpdateTag = "Library_Update_SERVICE"
    }

    @OptIn(ExperimentalTime::class)
    override suspend fun doWork(): Result {
        val libraryBooks = getBookUseCases.findAllInLibraryBooks()

        var updatedBookSize = 0
        val cancelIntent = WorkManager.getInstance(applicationContext)
            .createCancelPendingIntent(id)
        val builder =
            NotificationCompat.Builder(
                applicationContext,
                Notifications.CHANNEL_LIBRARY_PROGRESS
            ).apply {
                setContentTitle("Checking Updates")
                setSmallIcon(R.drawable.ic_update)
                setOnlyAlertOnce(true)
                priority = NotificationCompat.PRIORITY_LOW
                setAutoCancel(true)
                setOngoing(true)
                addAction(R.drawable.baseline_close_24, "Cancel", cancelIntent)
            }

        NotificationManagerCompat.from(applicationContext).apply {

            builder.setProgress(libraryBooks.size, 0, false)
            notify(Notifications.ID_LIBRARY_PROGRESS, builder.build())
            try {
                libraryBooks.forEachIndexed { index, book ->
                    val chapters = getChapterUseCase.findChaptersByBookId(bookId = book.id)
                    val source = getLocalCatalog.get(book.sourceId)
                    if (source != null) {
                        val remoteChapters = mutableListOf<Chapter>()
                        remoteUseCases.getRemoteChapters(
                            book, source,
                            onSuccess = {
                                builder.setContentText(book.title)
                                builder.setSubText(index.toString())
                                builder.setProgress(libraryBooks.size, index, false)
                                notify(Notifications.ID_LIBRARY_PROGRESS, builder.build())
                                remoteChapters.addAll(it)
                            },
                            onError = {}
                        )

                        val newChapters =
                            remoteChapters.filter { chapter -> chapter.link !in chapters.map { it.link } }

                        if (newChapters.isNotEmpty()) {
                            updatedBookSize += 1
                        }
                        withContext(Dispatchers.IO) {

                            val chapterIds = insertUseCases.insertChapters(
                                newChapters.map {
                                    it.copy(
                                        bookId = book.id,
                                        dateFetch = Clock.System.now().toEpochMilliseconds(),
                                    )
                                }
                            )
                            insertUseCases.insertBook(
                                book.copy(
                                    lastUpdated = Clock.System.now()
                                        .toEpochMilliseconds()
                                )
                            )
                            updatesUseCase(
                                newChapters.mapIndexed { index, chapter ->
                                    Update(
                                        chapterId = chapterIds[index],
                                        bookId = chapter.bookId,
                                        date = Clock.System.now().toEpochMilliseconds()
                                    )
                                }
                            )
                        }
                    }
                }
            } catch (e: Throwable) {
                Log.error { "getNotifications: Failed to Check for Book Update" }
                notify(
                    Notifications.ID_LIBRARY_ERROR,
                    NotificationCompat.Builder(
                        applicationContext,
                        Notifications.CHANNEL_LIBRARY_ERROR
                    ).apply {
                        if (e.localizedMessage == "Job was cancelled") {
                            setSubText("Library Updates was cancelled")
                            setContentTitle("Library Updates was canceled.")
                        } else {
                            setContentTitle("Failed to Check Library Updates.")
                            setSubText(e.localizedMessage)
                        }
                        setSmallIcon(R.drawable.ic_update)
                        priority = NotificationCompat.PRIORITY_DEFAULT
                        setAutoCancel(true)
                    }.build()
                )
                builder.setProgress(0, 0, false)
                cancel(Notifications.ID_LIBRARY_PROGRESS)
                withContext(Dispatchers.IO) {
                }
                return Result.failure()
            }

            builder.setProgress(0, 0, false)
            cancel(Notifications.ID_LIBRARY_PROGRESS)
            withContext(Dispatchers.IO) {
            }
            notify(
                Notifications.ID_LIBRARY_PROGRESS,
                NotificationCompat.Builder(
                    applicationContext,
                    Notifications.CHANNEL_LIBRARY_PROGRESS
                ).apply {
                    setContentTitle("$updatedBookSize book was updated.")
                    setSmallIcon(R.drawable.ic_update)
                    priority = NotificationCompat.PRIORITY_DEFAULT
                    setSubText("It was Updated Successfully")
                    setAutoCancel(true)
                }.build()
            )
        }

        return Result.success()
    }
}
