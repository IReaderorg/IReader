package org.ireader.presentation.feature_settings.presentation.setting.downloader

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.work.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.ireader.core.utils.Constants
import org.ireader.core.utils.UiEvent
import org.ireader.core.utils.UiText
import org.ireader.domain.models.entities.Book
import org.ireader.domain.models.entities.Chapter
import org.ireader.domain.models.entities.SavedDownload
import org.ireader.domain.use_cases.download.DownloadUseCases
import org.ireader.domain.use_cases.local.LocalGetChapterUseCase
import org.ireader.domain.use_cases.local.LocalInsertUseCases
import org.ireader.presentation.feature_services.downloaderService.DownloadService
import javax.inject.Inject


@HiltViewModel
class DownloaderViewModel @Inject constructor(
    private val getBookUseCases: org.ireader.domain.use_cases.local.LocalGetBookUseCases,
    private val getChapterUseCase: LocalGetChapterUseCase,
    private val insertUseCases: LocalInsertUseCases,
    private val downloadUseCases: DownloadUseCases,
) : ViewModel() {

    var savedDownload = MutableStateFlow<PagingData<SavedDownload>>(PagingData.empty())
        private set
    var chapters = MutableStateFlow<PagingData<Chapter>>(PagingData.empty())
        private set
    lateinit var work: OneTimeWorkRequest
    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()
    var state by mutableStateOf<DownloaderState>(DownloaderState())
        private set

    init {
        getLocalChaptersByPaging()
    }

    fun showSnackBar(text: UiText) {
        viewModelScope.launch {
            _eventFlow.emit(UiEvent.ShowSnackbar(text))
        }
    }

    fun insertSavedDownload(download: SavedDownload) {
        viewModelScope.launch(Dispatchers.IO) {
            downloadUseCases.insertDownload(
                download = download
            )
        }
    }


    private var getBooksJob: Job? = null
    private fun getLocalChaptersByPaging() {
        getBooksJob?.cancel()
        getBooksJob = viewModelScope.launch {
            downloadUseCases.getAllDownloadsUseCaseByPaging().cachedIn(viewModelScope).collect {
                savedDownload.value = it
            }

        }
    }

    fun startDownloadService(context: Context, bookId: Long, sourceId: Long) {
        viewModelScope.launch {
            val book = getBookUseCases.subscribeBookById(bookId).first()
            if (book != null) {
                // getChapters(book)
            }
        }
        work =
            OneTimeWorkRequestBuilder<DownloadService>().apply {
                setInputData(
                    Data.Builder().apply {

                        putLongArray(DownloadService.DOWNLOADER_BOOKS_IDS, longArrayOf(bookId))
                    }.build()
                )
                addTag(DownloadService.DOWNLOADER_SERVICE_NAME)
            }.build()
        WorkManager.getInstance(context).enqueueUniqueWork(
            DownloadService.DOWNLOADER_SERVICE_NAME.plus(
                bookId + sourceId),
            ExistingWorkPolicy.REPLACE,
            work
        )

    }

    fun stopDownloads(context: Context, bookId: Long, sourceId: Long) {
        work =
            OneTimeWorkRequestBuilder<DownloadService>().apply {
                setInputData(
                    Data.Builder().apply {
                        putLongArray(DownloadService.DOWNLOADER_BOOKS_IDS, longArrayOf(bookId))
                    }.build()
                )
            }.build()
        WorkManager.getInstance(context).cancelUniqueWork(
            DownloadService.DOWNLOADER_SERVICE_NAME.plus(
                bookId + sourceId)
        )
    }

    fun getChapters(book: Book) {
        viewModelScope.launch {
            getChapterUseCase.getLocalChaptersByPaging(bookId = book.id, isAsc = true)
                .cachedIn(viewModelScope)
                .collect { snapshot ->
                    chapters.value = snapshot
                    try {
//                        state =
//                            state.copy(progress = ((state.chapters.filter {
//                                it.content.joinToString().isNotBlank()
//                            }
//                                .size * 100) / state.chapters.size).toFloat(),
//                                downloadBookId = book.id)
                    } catch (e: Exception) {

                    }

                }

        }
    }

    fun updateChapters(chapters: List<Chapter>) {
        state = state.copy(chapters = chapters)
    }

    fun toggleExpandMenu(enable: Boolean = true) {
        state = state.copy(isMenuExpanded = enable)
    }

    fun deleteAllDownloads() {
        viewModelScope.launch(Dispatchers.IO) {
            downloadUseCases.deleteAllSavedDownload()
        }
    }
}

data class DownloaderState(
    val downloadBookId: Long = Constants.NULL_VALUE,
    val chapters: List<Chapter> = emptyList(),
    val isMenuExpanded: Boolean = false,
)