package ireader.domain.usecases.services

import ireader.domain.catalogs.interactor.GetLocalCatalog
import ireader.domain.services.library_update_service.runLibraryUpdateService
import ireader.domain.usecases.remote.RemoteUseCases
import ireader.domain.utils.NotificationManager
import ireader.domain.utils.extensions.launchIO
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job


actual class StartLibraryUpdateServicesUseCase(
    private val getBookUseCases: ireader.domain.usecases.local.LocalGetBookUseCases ,
private val getChapterUseCase: ireader.domain.usecases.local.LocalGetChapterUseCase ,
private val remoteUseCases: RemoteUseCases ,
private val getLocalCatalog: GetLocalCatalog ,
private val insertUseCases: ireader.domain.usecases.local.LocalInsertUseCases ,
private val notificationManager: NotificationManager,
)  {

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