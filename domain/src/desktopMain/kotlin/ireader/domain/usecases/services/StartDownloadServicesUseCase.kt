package ireader.domain.usecases.services

import ireader.core.util.createICoroutineScope
import ireader.domain.catalogs.CatalogStore
import ireader.domain.data.repository.BookRepository
import ireader.domain.data.repository.ChapterRepository
import ireader.domain.models.entities.SavedDownload
import ireader.domain.services.downloaderService.DownloadServiceStateImpl
import ireader.domain.services.downloaderService.runDownloadService
import ireader.domain.usecases.download.DownloadUseCases
import ireader.domain.usecases.remote.RemoteUseCases
import ireader.domain.utils.NotificationManager
import ireader.domain.utils.extensions.launchIO
import ireader.i18n.LocalizeHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import org.kodein.di.DI
import org.kodein.di.DIAware
import org.kodein.di.instance

actual class StartDownloadServicesUseCase(override val di: DI) : DIAware  {

    private val bookRepo: BookRepository by instance()
    private val chapterRepo: ChapterRepository by instance()
    private val remoteUseCases: RemoteUseCases by instance()
    private val localizeHelper: LocalizeHelper by instance()
    private val extensions: CatalogStore by instance()
    private val insertUseCases: ireader.domain.usecases.local.LocalInsertUseCases by instance()
    private val downloadUseCases: DownloadUseCases by instance()
    private val downloadServiceState: DownloadServiceStateImpl by instance()

    val workerJob= Job()
    private val notificationManager : NotificationManager by instance()
    val scope = createICoroutineScope(Dispatchers.Main.immediate + workerJob)
    var savedDownload: SavedDownload =
        SavedDownload(
            bookId = 0,
            priority = 1,
            chapterName = "",
            chapterKey = "",
            translator = "",
            chapterId = 0,
            bookName = "",
        )
    actual fun start(bookIds: LongArray?, chapterIds: LongArray?, downloadModes: Boolean) {
        scope.launchIO {
            val result = runDownloadService(
                inputtedBooksIds = bookIds,
                inputtedChapterIds = chapterIds,
                inputtedDownloaderMode = downloadModes,
                bookRepo = bookRepo,
                downloadServiceState = downloadServiceState,
                downloadUseCases = downloadUseCases,
                chapterRepo = chapterRepo,
                extensions = extensions,
                insertUseCases = insertUseCases,
                localizeHelper = localizeHelper,
                notificationManager = notificationManager,
                onCancel = { error, bookName->

                },
                onSuccess = {

                },
                remoteUseCases = remoteUseCases,
                updateProgress = { max, progress, inProgess ->

                },
                updateSubtitle = {


                },
                updateTitle = {

                }, updateNotification = {}
            )
        }
        }

    actual fun stop() {
        workerJob.cancel()
    }

}