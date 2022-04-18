package org.ireader.presentation.feature_settings.presentation.setting.downloader

import android.content.Context
import androidx.lifecycle.viewModelScope
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import org.ireader.core_ui.viewmodel.BaseViewModel
import org.ireader.domain.models.entities.SavedDownload
import org.ireader.domain.services.downloaderService.DownloadService
import org.ireader.domain.services.downloaderService.DownloadServiceStateImpl
import org.ireader.domain.use_cases.download.DownloadUseCases
import org.ireader.domain.use_cases.local.LocalGetChapterUseCase
import org.ireader.domain.use_cases.local.LocalInsertUseCases
import org.ireader.domain.use_cases.services.ServiceUseCases
import javax.inject.Inject



@HiltViewModel
class DownloaderViewModel @Inject constructor(
    private val getBookUseCases: org.ireader.domain.use_cases.local.LocalGetBookUseCases,
    private val getChapterUseCase: LocalGetChapterUseCase,
    private val insertUseCases: LocalInsertUseCases,
    private val downloadUseCases: DownloadUseCases,
    private val serviceUseCases: ServiceUseCases,
    private val downloadState: DownloadStateImpl,
    val downloadServiceStateImpl: DownloadServiceStateImpl
) : BaseViewModel(), DownloadState by downloadState {


    lateinit var work: OneTimeWorkRequest


    init {
        subscribeDownloads()
    }


    fun insertSavedDownload(download: SavedDownload) {
        viewModelScope.launch(Dispatchers.IO) {
            downloadUseCases.insertDownload(
                download = download
            )
        }
    }


    private var getBooksJob: Job? = null
    private fun subscribeDownloads() {
        getBooksJob?.cancel()
        getBooksJob = viewModelScope.launch {
            downloadUseCases.subscribeDownloadsUseCase().distinctUntilChanged().collect {
                downloads = it.filter { it.chapterId != 0L }
            }

        }
    }

    fun startDownloadService(chapterIds: List<Long>) {
        serviceUseCases.startDownloadServicesUseCase(
            chapterIds = chapterIds.toLongArray()
        )
    }

    fun stopDownloads() {
        serviceUseCases.stopServicesUseCase(
            DownloadService.DOWNLOADER_SERVICE_NAME
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
            downloadUseCases.deleteSavedDownloads(list)
        }
    }

    fun checkState(context:Context) {
        WorkManager.getInstance(context).getWorkInfosByTag(DownloadService.DOWNLOADER_SERVICE_NAME).get()
    }
}


