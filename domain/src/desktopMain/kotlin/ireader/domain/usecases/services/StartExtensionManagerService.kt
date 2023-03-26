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
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance

actual class StartExtensionManagerService(override val di: DI): DIAware {

    private val repository: CatalogRemoteRepository by instance()
    val getInstalledCatalog: GetInstalledCatalog by instance()
    private val installCatalog: InstallCatalog by instance()

    private val notificationManager: NotificationManager by instance()
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