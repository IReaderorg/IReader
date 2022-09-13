package ireader.ui.settings.downloader

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import ireader.common.models.entities.SavedDownload
import ireader.core.ui.viewmodel.BaseViewModel
import ireader.domain.services.downloaderService.DownloadServiceStateImpl
import ireader.domain.services.downloaderService.DownloaderService
import ireader.domain.usecases.download.DownloadUseCases
import ireader.domain.usecases.services.ServiceUseCases
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class DownloaderViewModel(
    private val downloadUseCases: DownloadUseCases,
    private val serviceUseCases: ServiceUseCases,
    private val downloadState: DownloadStateImpl,
    val downloadServiceStateImpl: DownloadServiceStateImpl
) : BaseViewModel(), DownloadState by downloadState {

    init {
        subscribeDownloads()
    }

    fun insertSavedDownload(download: SavedDownload) {
        viewModelScope.launch(Dispatchers.IO) {
            downloadUseCases.insertDownload(
                download = download.toDownload()
            )
        }
    }

    private var getBooksJob: Job? = null
    private fun subscribeDownloads() {
        getBooksJob?.cancel()
        getBooksJob = viewModelScope.launch {
            downloadUseCases.subscribeDownloadsUseCase().distinctUntilChanged().collect { list ->
                downloads = list.filter { it.chapterId != 0L }
            }
        }
    }

    fun startDownloadService(chapterIds: List<Long>) {
        if (downloads.isEmpty()) return
        serviceUseCases.startDownloadServicesUseCase(
            downloadModes = true
        )
    }

    fun stopDownloads() {
        serviceUseCases.stopServicesUseCase(
            DownloaderService.DOWNLOADER_SERVICE_NAME
        )
    }

    fun toggleExpandMenu(enable: Boolean = true) {
        isMenuExpanded = enable
    }

    fun deleteAllDownloads() {
        viewModelScope.launch(Dispatchers.IO) {
            downloadUseCases.deleteAllSavedDownload()
        }
    }
    fun deleteSelectedDownloads(list: List<SavedDownload>) {
        viewModelScope.launch(Dispatchers.IO) {
            downloadUseCases.deleteSavedDownloads(list.map { it.toDownload() })
        }
    }
}
