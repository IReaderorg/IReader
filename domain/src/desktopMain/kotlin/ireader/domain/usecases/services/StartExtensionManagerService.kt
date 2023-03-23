package ireader.domain.usecases.services

import ireader.core.os.InstallStep
import ireader.domain.catalogs.interactor.GetInstalledCatalog
import ireader.domain.catalogs.interactor.InstallCatalog
import ireader.domain.catalogs.service.CatalogRemoteRepository
import ireader.domain.models.prefs.PreferenceValues
import ireader.domain.utils.NotificationManager
import ireader.domain.utils.extensions.launchIO
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
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
                try {
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
                    }



                } catch (e: Throwable) {

                }
            }


    }

    actual fun stop() {
        workerJob.cancel()
    }
}