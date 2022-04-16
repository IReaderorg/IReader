package org.ireader.presentation.feature_settings.presentation.setting.downloader

import android.content.Context
import androidx.annotation.Keep
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
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
import org.ireader.domain.services.downloaderService.DownloadService
import javax.inject.Inject


@HiltViewModel
class DownloaderViewModel @Inject constructor(
    private val getBookUseCases: org.ireader.domain.use_cases.local.LocalGetBookUseCases,
    private val getChapterUseCase: LocalGetChapterUseCase,
    private val insertUseCases: LocalInsertUseCases,
    private val downloadUseCases: DownloadUseCases,
) : ViewModel() {

    var savedDownload by mutableStateOf<List<SavedDownload>>(emptyList())

    var chapters = mutableStateOf<List<Chapter>>(emptyList())

    lateinit var work: OneTimeWorkRequest
    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()
    var state by mutableStateOf<DownloaderState>(DownloaderState())
        private set

    init {
        subscribeDownloads()
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
    private fun subscribeDownloads() {
        getBooksJob?.cancel()
        getBooksJob = viewModelScope.launch {
            downloadUseCases.subscribeDownloadsUseCase().collect {
                savedDownload = it
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
            getChapterUseCase.subscribeChaptersByBookId(bookId = book.id, isAsc = true)
                .collect { snapshot ->
                    chapters.value = snapshot
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

@Keep
data class DownloaderState(
    val downloadBookId: Long = Constants.NULL_VALUE,
    val chapters: List<Chapter> = emptyList(),
    val isMenuExpanded: Boolean = false,
)