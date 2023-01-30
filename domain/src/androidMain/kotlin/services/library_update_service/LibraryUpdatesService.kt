package ireader.domain.services.library_update_service

import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import ireader.domain.models.entities.Chapter
import ireader.core.log.Log
import ireader.domain.catalogs.interactor.GetLocalCatalog
import ireader.domain.R
import ireader.domain.notification.Notifications
import ireader.domain.services.downloaderService.DefaultNotificationHelper
import ireader.domain.usecases.local.DeleteUseCase
import ireader.domain.usecases.local.LocalGetBookUseCases
import ireader.domain.usecases.local.LocalGetChapterUseCase
import ireader.domain.usecases.local.LocalInsertUseCases
import ireader.domain.usecases.remote.RemoteUseCases
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlin.time.ExperimentalTime

class LibraryUpdatesService  constructor(
    private val context: Context,
    params: WorkerParameters,
    private val getBookUseCases: LocalGetBookUseCases,
    private val getChapterUseCase: LocalGetChapterUseCase,
    private val remoteUseCases: RemoteUseCases,
    private val getLocalCatalog: GetLocalCatalog,
    private val defaultNotificationHelper: DefaultNotificationHelper,
    private val insertUseCases: LocalInsertUseCases,
    private val deleteUseCase: DeleteUseCase,
) : CoroutineWorker(context, params) {

    companion object {
        const val LibraryUpdateTag = "Library_Update_SERVICE"
        const val FORCE_UPDATE = "force_update"
    }

    @OptIn(ExperimentalTime::class)
    override suspend fun doWork(): Result {
        val forceUpdate = inputData.getBoolean(FORCE_UPDATE,false)
        val libraryBooks = getBookUseCases.findAllInLibraryBooks()
        var skippedBooks = 0


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
                    if (chapters.any { !it.read } && chapters.isNotEmpty() && !forceUpdate) {
                        skippedBooks++
                        return@forEachIndexed
                    }
                    val source = getLocalCatalog.get(book.sourceId)
                    if (source != null) {
                        val remoteChapters = mutableListOf<Chapter>()
                        remoteUseCases.getRemoteChapters(
                            book, source,
                            onRemoteSuccess = {
                                builder.setContentText(book.title)
                                builder.setSubText(index.toString())
                                builder.setProgress(libraryBooks.size, index, false)
                                notify(Notifications.ID_LIBRARY_PROGRESS, builder.build())
                                remoteChapters.addAll(it)
                            },
                            onError = {},
                            oldChapters = chapters,
                            onSuccess = {}
                        )

                        val newChapters =
                            remoteChapters.filter { chapter -> chapter.key !in chapters.map { it.key } }

                        if (newChapters.isNotEmpty()) {
                            updatedBookSize += 1
                        }
                        withContext(Dispatchers.IO) {

                            insertUseCases.insertChapters(
                                newChapters.map {
                                    it.copy(
                                        bookId = book.id,
                                        dateFetch = Clock.System.now().toEpochMilliseconds(),
                                    )
                                }
                            )
                            insertUseCases.updateBook.update(
                                book.copy(
                                    lastUpdate = Clock.System.now()
                                        .toEpochMilliseconds()
                                )
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
                    val title = "$updatedBookSize book was updated.".plus(
                        if (skippedBooks != 0) " $skippedBooks books was skipped." else ""
                    )
                    setContentTitle(title)
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
