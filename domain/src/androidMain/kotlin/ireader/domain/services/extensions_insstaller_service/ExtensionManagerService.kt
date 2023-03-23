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
import ireader.domain.utils.NotificationManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance

class ExtensionManagerService constructor(
    private val context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params),DIAware {
    override val di: DI = (context.applicationContext as DIAware).di
    private val repository: CatalogRemoteRepository by instance()
    val getInstalledCatalog: GetInstalledCatalog by instance()
    private val installCatalog: InstallCatalog by instance()
    val defaultNotificationHelper: DefaultNotificationHelper by instance()
    private val notificationManager: NotificationManager by instance()
    private val downloadJob = Job()

    val scope = CoroutineScope(Dispatchers.Main.immediate + downloadJob)


    override suspend fun doWork(): Result {
        NotificationManagerCompat.from(applicationContext.applicationContext).apply {
            try {
                val builder = defaultNotificationHelper.baseInstallerNotification(
                    id
                )
                builder.setProgress(100, 0, true)
                notificationManager.show(Notifications.ID_INSTALLER_PROGRESS, builder.build())
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
                    notificationManager.show(Notifications.ID_INSTALLER_PROGRESS, builder.build())
                }
                withContext(Dispatchers.Main) {
                    notificationManager.cancel(Notifications.ID_INSTALLER_PROGRESS)
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
                    notificationManager.show(Notifications.ID_INSTALLER_COMPLETE,
                        notification)
                }

                return Result.success()
            } catch (e: Throwable) {
                notificationManager.cancel(Notifications.ID_INSTALLER_PROGRESS)
               notificationManager.show(Notifications.ID_INSTALLER_ERROR,
                   defaultNotificationHelper.baseInstallerNotification(
                       id,
                       false
                   ).apply {
                       setContentTitle("Installation was stopped.")
                       setOngoing(false)
                   }.build())
                return Result.failure()
            }
        }
    }
}
