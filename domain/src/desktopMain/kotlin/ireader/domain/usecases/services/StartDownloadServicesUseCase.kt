package ireader.domain.usecases.services

import ireader.core.util.createICoroutineScope
import ireader.domain.catalogs.CatalogStore
import ireader.domain.data.repository.BookRepository
import ireader.domain.data.repository.ChapterRepository
import ireader.domain.models.entities.SavedDownload
import ireader.domain.services.downloaderService.DownloadStateHolder
import ireader.domain.services.downloaderService.runDownloadService
import ireader.domain.usecases.download.DownloadUseCases
import ireader.domain.usecases.remote.RemoteUseCases
import ireader.domain.notification.PlatformNotificationManager
import ireader.domain.utils.extensions.launchIO
import ireader.i18n.LocalizeHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job

/**
 * Desktop implementation of StartDownloadServicesUseCase
 * 
 * Uses DownloadStateHolder to share state with DownloadService implementations.
 */
actual class StartDownloadServicesUseCase(
    private val bookRepo: BookRepository,
    private val chapterRepo: ChapterRepository,
    private val remoteUseCases: RemoteUseCases,
    private val localizeHelper: LocalizeHelper,
    private val extensions: CatalogStore,
    private val insertUseCases: ireader.domain.usecases.local.LocalInsertUseCases,
    private val downloadUseCases: DownloadUseCases,
    private val downloadServiceState: DownloadStateHolder,
    private val notificationManager: PlatformNotificationManager,
    private val downloadPreferences: ireader.domain.preferences.prefs.DownloadPreferences
) {
    private val parentJob = Job()
    private val scope = createICoroutineScope(Dispatchers.Main.immediate + parentJob)
    private val activeJobs = mutableListOf<Job>()
    
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
        val job = scope.launchIO {
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
                onCancel = { error, bookName ->
                    // Log error for desktop - could show system notification in future
                    ireader.core.log.Log.error { "Download failed for $bookName: ${error.message}" }
                },
                onSuccess = {
                    // Count completed and failed downloads
                    val completedCount = downloadServiceState.downloadProgress.value.values
                        .count { it.status == ireader.domain.services.downloaderService.DownloadStatus.COMPLETED }
                    val failedCount = downloadServiceState.downloadProgress.value.values
                        .count { it.status == ireader.domain.services.downloaderService.DownloadStatus.FAILED }
                    
                    // Log success for desktop
                    if (completedCount > 0) {
                        ireader.core.log.Log.info { "Successfully downloaded $completedCount chapters" }
                    }
                    
                    // Log failures if any
                    if (failedCount > 0) {
                        ireader.core.log.Log.warn { "$failedCount chapters failed to download" }
                    }
                },
                remoteUseCases = remoteUseCases,
                updateProgress = { max, progress, inProgess ->

                },
                updateSubtitle = {


                },
                updateTitle = {

                }, updateNotification = {},
                downloadDelayMs = downloadPreferences.downloadDelayMs().get(),
                concurrentLimit = downloadPreferences.concurrentDownloadsLimit().get()
            )
        }
        
        synchronized(activeJobs) {
            activeJobs.add(job)
            job.invokeOnCompletion {
                synchronized(activeJobs) {
                    activeJobs.remove(job)
                }
            }
        }
    }

    actual fun stop() {
        synchronized(activeJobs) {
            activeJobs.forEach { it.cancel() }
            activeJobs.clear()
        }
        parentJob.cancel()
    }

}