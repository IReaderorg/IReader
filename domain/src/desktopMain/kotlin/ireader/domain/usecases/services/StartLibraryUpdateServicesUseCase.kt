package ireader.domain.usecases.services

import ireader.domain.catalogs.interactor.GetLocalCatalog
import ireader.domain.services.library_update_service.runLibraryUpdateService
import ireader.domain.usecases.remote.RemoteUseCases
import ireader.domain.utils.NotificationManager
import ireader.domain.utils.extensions.launchIO
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance

actual class StartLibraryUpdateServicesUseCase(override val di: DI) : DIAware {
    private val getBookUseCases: ireader.domain.usecases.local.LocalGetBookUseCases by instance()
    private val getChapterUseCase: ireader.domain.usecases.local.LocalGetChapterUseCase by instance()
    private val remoteUseCases: RemoteUseCases by instance()
    private val getLocalCatalog: GetLocalCatalog by instance()
    private val insertUseCases: ireader.domain.usecases.local.LocalInsertUseCases by instance()
    private val notificationManager: NotificationManager by instance()
    private val workerJob = Job()
    val scope = CoroutineScope(Dispatchers.Main.immediate + workerJob)
    actual fun start(forceUpdate: Boolean) {
        scope.launchIO {
            runLibraryUpdateService(
                onCancel = {e ->

                },
                updateProgress = {max: Int,progress:Int, inProgress: Boolean ->

                },
                updateNotification = {

                },
                onSuccess = { size , skipped ->


                },
                updateTitle = {

                },
                updateSubtitle = {

                },
                remoteUseCases = remoteUseCases,
                notificationManager = notificationManager,
                insertUseCases = insertUseCases,
                forceUpdate = forceUpdate,
                getBookUseCases = getBookUseCases,
                getChapterUseCase = getChapterUseCase,
                getLocalCatalog = getLocalCatalog
            )
        }
    }

    actual fun stop() {
        workerJob.cancel()
    }

}