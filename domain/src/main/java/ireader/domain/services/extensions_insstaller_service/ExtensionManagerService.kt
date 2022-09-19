package ireader.domain.services.extensions_insstaller_service

import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import ireader.core.os.InstallStep
import ireader.domain.R
import ireader.domain.catalogs.interactor.GetInstalledCatalog
import ireader.domain.catalogs.interactor.InstallCatalog
import ireader.domain.catalogs.service.CatalogRemoteRepository
import ireader.domain.models.prefs.PreferenceValues
import ireader.domain.notification.Notifications
import ireader.domain.services.downloaderService.DefaultNotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class ExtensionManagerService constructor(
    private val context: Context,
    params: WorkerParameters,
    private val repository: CatalogRemoteRepository,
    val getDefaultRepo: GetDefaultRepo,
    val getInstalledCatalog: GetInstalledCatalog,
    private val installCatalog: InstallCatalog,
    val defaultNotificationHelper: DefaultNotificationHelper
) : CoroutineWorker(context, params) {

    private val downloadJob = Job()

    val scope = CoroutineScope(Dispatchers.Main.immediate + downloadJob)


    override suspend fun doWork(): Result {
        NotificationManagerCompat.from(applicationContext.applicationContext).apply {
            try {
                val builder = defaultNotificationHelper.baseInstallerNotification(
                    id
                )
                builder.setProgress(100, 0, true)
                notify(Notifications.ID_INSTALLER_PROGRESS, builder.build())
                val remote = repository.getRemoteCatalogs()
                val installed = getInstalledCatalog.get().map { Pair(it.pkgName, it.versionCode) }
                val notInstalled = remote
                    .filter { catalog ->
                        catalog.pkgName !in installed.map { it.first }
                                || catalog.versionCode !in installed.map { it.second }
                    }
                notInstalled.forEachIndexed { index, catalogRemote ->
                    installCatalog.await(PreferenceValues.Installer.LocalInstaller)
                        .install(catalogRemote).first {
                            it is InstallStep.Idle
                        }
                    builder.setProgress(notInstalled.size, index, false)
                    builder.setContentTitle("Installing ${index + 1} of ${notInstalled.size}")
                    notify(Notifications.ID_INSTALLER_PROGRESS, builder.build())
                }
                withContext(Dispatchers.Main) {
                    cancel(Notifications.ID_INSTALLER_PROGRESS)
                    val notification = NotificationCompat.Builder(
                        applicationContext.applicationContext,
                        Notifications.CHANNEL_INSTALLER_COMPLETE
                    ).apply {
                        setContentTitle("${notInstalled.size} sources was Installed successfully.")
                        setSmallIcon(R.drawable.ic_downloading)
                        priority = NotificationCompat.PRIORITY_DEFAULT
                        setSubText("Installed Successfully")
                        setAutoCancel(true)
                        setContentIntent(defaultNotificationHelper.openDownloadsPendingIntent)
                    }.build()
                    notify(
                        Notifications.ID_INSTALLER_COMPLETE,
                        notification
                    )
                }

                return Result.success()
            } catch (e: Throwable) {
                cancel(Notifications.ID_INSTALLER_PROGRESS)
                notify(
                    Notifications.ID_INSTALLER_ERROR,
                    defaultNotificationHelper.baseInstallerNotification(
                        id,
                        false
                    ).apply {
                        setContentTitle("Installation was stopped.")
                        setOngoing(false)
                    }.build()
                )
                return Result.failure()
            }
        }
    }
}
