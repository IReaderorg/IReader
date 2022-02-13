package org.ireader.presentation.feature_settings.presentation.setting.downloader

import android.content.Context
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.work.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.ireader.core.utils.Constants
import org.ireader.core.utils.UiEvent
import org.ireader.core.utils.UiText
import org.ireader.domain.feature_services.DownloaderService.DownloadService
import org.ireader.domain.models.entities.Book
import org.ireader.domain.models.entities.Chapter
import org.ireader.domain.use_cases.local.LocalGetChapterUseCase
import org.ireader.domain.use_cases.local.LocalInsertUseCases
import javax.inject.Inject

@HiltViewModel
class DownloaderViewModel @Inject constructor(
    private val getBookUseCases: org.ireader.domain.use_cases.local.LocalGetBookUseCases,
    private val getChapterUseCase: LocalGetChapterUseCase,
    private val insertUseCases: LocalInsertUseCases,

    ) : ViewModel() {

    private val _books = MutableStateFlow<PagingData<Book>>(PagingData.empty())
    val book = _books
    private val _chapters = MutableStateFlow<PagingData<Chapter>>(PagingData.empty())
    val chapters = _chapters
    lateinit var work: OneTimeWorkRequest
    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()
    private val _state = mutableStateOf<DownloaderState>(DownloaderState())
    val state: State<DownloaderState> = _state

    init {
        getLocalChaptersByPaging()
    }

    fun showSnackBar(text: UiText) {
        viewModelScope.launch {
            _eventFlow.emit(UiEvent.ShowSnackbar(text))
        }
    }

    private var getBooksJob: Job? = null
    private fun getLocalChaptersByPaging() {
        getBooksJob?.cancel()
        getBooksJob = viewModelScope.launch {
            //TODO need to recreate a func for this

        }
    }

    fun startDownloadService(context: Context, bookId: Long, sourceId: Long) {
        viewModelScope.launch {
            val book = getBookUseCases.getBookById(bookId).first()
            if (book != null) {
                getChapters(book)
            }

        }
        work =
            OneTimeWorkRequestBuilder<DownloadService>().apply {
                setInputData(
                    Data.Builder().apply {
                        putLong(DownloadService.DOWNLOADER_BOOK_ID,
                            bookId)
                        putLong(DownloadService.DOWNLOADER_SOURCE_ID,
                            sourceId)
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
                        putLong(DownloadService.DOWNLOADER_BOOK_ID,
                            bookId)
                        putLong(DownloadService.DOWNLOADER_SOURCE_ID,
                            sourceId)
                    }.build()
                )
            }.build()
        val work = WorkManager.getInstance(context).cancelUniqueWork(
            DownloadService.DOWNLOADER_SERVICE_NAME.plus(
                bookId + sourceId)
        )
    }

    fun getChapters(book: Book) {
        viewModelScope.launch {
            getChapterUseCase.getLocalChaptersByPaging(bookId = book.id, isAsc = true)
                .cachedIn(viewModelScope)
                .collect { snapshot ->
                    _chapters.value = snapshot
                    try {
                        _state.value =
                            state.value.copy(progress = ((state.value.chapters.filter {
                                it.content.joinToString().isNotBlank()
                            }
                                .size * 100) / state.value.chapters.size).toFloat(),
                                downloadBookId = book.id)
                    } catch (e: Exception) {

                    }

                }

        }
    }

    fun updateChapters(chapters: List<Chapter>) {
        _state.value = state.value.copy(chapters = chapters)
    }
}

data class DownloaderState(
    val totalChapter: Int = 0,
    val progress: Float = 0f,
    val downloadBookId: Long = Constants.NULL_VALUE,
    val chapters: List<Chapter> = emptyList(),
)