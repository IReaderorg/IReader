package ireader.domain.usecases.services

import ireader.domain.catalogs.interactor.GetInstalledCatalog
import ireader.domain.catalogs.interactor.InstallCatalog
import ireader.domain.catalogs.service.CatalogRemoteRepository
import ireader.domain.services.extensions_insstaller_service.runExtensionService
import ireader.domain.utils.NotificationManager
import ireader.domain.utils.extensions.launchIO
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job

actual class StartExtensionManagerService(
    private val repository: CatalogRemoteRepository,
    private val getInstalledCatalog: GetInstalledCatalog,
    private val installCatalog: InstallCatalog,
    private val notificationManager: NotificationManager
) {

    private val workerJob = Job()

    val scope = CoroutineScope(Dispatchers.Main.immediate + workerJob)


    actual fun start() {
            scope.launchIO {
                runExtensionService(
                    repository = repository,
                    getInstalledCatalog = getInstalledCatalog,
                    installCatalog = installCatalog,
                    notificationManager = notificationManager,
                    updateProgress = { max, progress, inProgess ->

                    },
                    updateTitle = {
                    },
                    onCancel = {

                    },
                    updateNotification = {

                    },
                    onSuccess = { notInstalled ->

                    }
                )
            }


    }

    actual fun stop() {
        workerJob.cancel()
    }
}