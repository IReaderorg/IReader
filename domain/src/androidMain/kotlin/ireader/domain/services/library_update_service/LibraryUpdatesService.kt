package ireader.domain.services.library_update_service

import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import ireader.domain.catalogs.interactor.GetLocalCatalog
import ireader.domain.notification.NotificationsIds
import ireader.domain.usecases.remote.RemoteUseCases
import ireader.domain.utils.NotificationManager
import ireader.i18n.R
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.time.ExperimentalTime

class LibraryUpdatesService(
    private val context: Context,
    params: WorkerParameters,

    ) : CoroutineWorker(context, params), KoinComponent {
    private val getBookUseCases: ireader.domain.usecases.local.LocalGetBookUseCases by inject()
    private val getChapterUseCase: ireader.domain.usecases.local.LocalGetChapterUseCase by inject()
    private val remoteUseCases: RemoteUseCases by inject()
    private val getLocalCatalog: GetLocalCatalog by inject()
    private val insertUseCases: ireader.domain.usecases.local.LocalInsertUseCases by inject()
    private val notificationManager: NotificationManager by inject()

    companion object {
        const val LibraryUpdateTag = "Library_Update_SERVICE"
        const val FORCE_UPDATE = "force_update"
    }


    @OptIn(ExperimentalTime::class)
    override suspend fun doWork(): Result {
        val cancelIntent = WorkManager.getInstance(applicationContext)
            .createCancelPendingIntent(id)
        val builder =
            NotificationCompat.Builder(
                applicationContext,
                NotificationsIds.CHANNEL_LIBRARY_PROGRESS
            ).apply {
                setContentTitle("Checking Updates")
                setSmallIcon(R.drawable.ic_update)
                setOnlyAlertOnce(true)
                priority = NotificationCompat.PRIORITY_LOW
                setAutoCancel(true)
                setOngoing(true)
                addAction(R.drawable.baseline_close_24, "Cancel", cancelIntent)
            }
        val forceUpdate = inputData.getBoolean(FORCE_UPDATE, false)
        val result = runLibraryUpdateService(
            onCancel = {e ->
                notificationManager.show(NotificationsIds.ID_LIBRARY_ERROR,
                    NotificationCompat.Builder(
                        applicationContext,
                        NotificationsIds.CHANNEL_LIBRARY_ERROR
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
                    }.build())
            },
            updateProgress = {max: Int,progress:Int, inProgress: Boolean ->
                builder.setProgress(max, progress, inProgress)
            },
            updateNotification = {
                notificationManager.show(it, builder.build())
            },
            onSuccess = { size , skipped ->

                notificationManager.cancel(NotificationsIds.ID_LIBRARY_PROGRESS)
                notificationManager.show(NotificationsIds.ID_LIBRARY_PROGRESS,
                    NotificationCompat.Builder(
                        applicationContext,
                        NotificationsIds.CHANNEL_LIBRARY_PROGRESS
                    ).apply {
                        val title = "$size book was updated.".plus(
                            if (skipped != 0) " $skipped books was skipped." else ""
                        )
                        setContentTitle(title)
                        setSmallIcon(R.drawable.ic_update)
                        priority = NotificationCompat.PRIORITY_DEFAULT
                        setSubText("It was Updated Successfully")
                        setAutoCancel(true)
                    }.build())
            },
            updateTitle = {
                builder.setContentText(it)
            },
            updateSubtitle = {
                builder.setSubText(it)
            },
            remoteUseCases = remoteUseCases,
            notificationManager = notificationManager,
            insertUseCases = insertUseCases,
            forceUpdate = forceUpdate,
            getBookUseCases = getBookUseCases,
            getChapterUseCase = getChapterUseCase,
            getLocalCatalog = getLocalCatalog
        )
        return if (result) Result.success() else Result.failure()
    }
}