package ireader.presentation.ui.settings.downloader

import ireader.domain.models.entities.SavedDownload
import ireader.domain.services.downloaderService.DownloadServiceStateImpl
import ireader.domain.usecases.download.DownloadUseCases
import ireader.domain.usecases.services.ServiceUseCases
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch


class DownloaderViewModel(
        private val downloadUseCases: DownloadUseCases,
        private val serviceUseCases: ServiceUseCases,
        private val downloadState: DownloadStateImpl,
        val downloadServiceStateImpl: DownloadServiceStateImpl
) : ireader.presentation.ui.core.viewmodel.BaseViewModel(), DownloadState by downloadState {

    init {
        subscribeDownloads()
    }

    private var getBooksJob: Job? = null
    private fun subscribeDownloads() {
        getBooksJob?.cancel()
        getBooksJob = scope.launch {
            downloadUseCases.subscribeDownloadsUseCase().distinctUntilChanged().collect { list ->
                downloads = list.filter { it.chapterId != 0L }
            }
        }
    }

    fun startDownloadService(chapterIds: List<Long>) {
        if (downloads.isEmpty()) return
        serviceUseCases.startDownloadServicesUseCase.start(
            downloadModes = true
        )
    }

    fun stopDownloads() {
        serviceUseCases.startDownloadServicesUseCase.stop()
    }

    fun toggleExpandMenu(enable: Boolean = true) {
        isMenuExpanded = enable
    }

    fun deleteAllDownloads() {
        scope.launch(Dispatchers.IO) {
            downloadUseCases.deleteAllSavedDownload()
        }
    }
    fun deleteSelectedDownloads(list: List<SavedDownload>) {
        scope.launch(Dispatchers.IO) {
            downloadUseCases.deleteSavedDownloads(list.map { it.toDownload() })
        }
    }
}
