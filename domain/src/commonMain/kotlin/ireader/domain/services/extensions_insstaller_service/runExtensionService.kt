package ireader.domain.services.extensions_insstaller_service


import ireader.core.os.InstallStep
import ireader.domain.catalogs.interactor.GetInstalledCatalog
import ireader.domain.catalogs.interactor.InstallCatalog
import ireader.domain.catalogs.service.CatalogRemoteRepository
import ireader.domain.models.prefs.PreferenceValues
import ireader.domain.notification.NotificationsIds
import ireader.domain.notification.NotificationsIds.ID_INSTALLER_PROGRESS
import ireader.domain.utils.NotificationManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

suspend fun runExtensionService(repository: CatalogRemoteRepository,
                                getInstalledCatalog: GetInstalledCatalog,
                                installCatalog: InstallCatalog,
                                notificationManager: NotificationManager,
                                updateProgress:(max: Int, current: Int, inProgress: Boolean) -> Unit,
                                updateTitle: (String) -> Unit,
                                onCancel:(error: Throwable) -> Unit,
                                updateNotification: (Int) -> Unit,
                                onSuccess: (notInstalled:String) -> Unit
) : Boolean{

        try {
            updateProgress(100, 0, true)
            updateNotification(ID_INSTALLER_PROGRESS)
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
                updateProgress(notInstalled.size, index, false)
                updateTitle("Installing ${index + 1} of ${notInstalled.size}")
                updateNotification(NotificationsIds.ID_INSTALLER_PROGRESS)
            }
            withContext(Dispatchers.Main) {
                onSuccess(notInstalled.size.toString())
            }

            return true
        } catch (e: Throwable) {
            notificationManager.cancel(NotificationsIds.ID_INSTALLER_PROGRESS)
            onCancel(e)
            return false
        }
}