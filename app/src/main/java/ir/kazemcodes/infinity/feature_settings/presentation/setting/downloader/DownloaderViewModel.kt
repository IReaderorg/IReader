package ir.kazemcodes.infinity.feature_settings.presentation.setting.downloader

import android.content.Context
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.work.*
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.kazemcodes.infinity.core.domain.models.Book
import ir.kazemcodes.infinity.core.domain.models.Chapter
import ir.kazemcodes.infinity.core.domain.use_cases.local.LocalGetBookUseCases
import ir.kazemcodes.infinity.core.domain.use_cases.local.LocalGetChapterUseCase
import ir.kazemcodes.infinity.core.domain.use_cases.local.LocalInsertUseCases
import ir.kazemcodes.infinity.core.utils.Constants
import ir.kazemcodes.infinity.core.utils.UiEvent
import ir.kazemcodes.infinity.core.utils.UiText
import ir.kazemcodes.infinity.feature_library.presentation.components.SortType
import ir.kazemcodes.infinity.feature_services.DownloaderService.DownloadService
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DownloaderViewModel @Inject constructor(
    private val getBookUseCases: LocalGetBookUseCases,
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
            getBookUseCases.getAllInDownloadsPagingData(
                isAsc = true,
                sortType = SortType.Alphabetically,
                unreadFilter = false
            ).cachedIn(viewModelScope)
                .collect { snapshot ->
                    _books.value = snapshot
                }
        }
    }

    fun startDownloadService(context: Context, bookId: Int, sourceId: Long) {
        viewModelScope.launch {
            val book = getBookUseCases.getBookById(bookId).first().data
            if (book != null) {
                getChapters(book)
            }

        }
        work = OneTimeWorkRequestBuilder<DownloadService>().apply {
            setInputData(
                Data.Builder().apply {
                    putInt(DownloadService.DOWNLOADER_BOOK_ID, bookId)
                    putLong(DownloadService.DOWNLOADER_SOURCE_ID, sourceId)
                }.build()
            )
            addTag(DownloadService.DOWNLOADER_SERVICE_NAME)
        }.build()
        WorkManager.getInstance(context).enqueueUniqueWork(
            DownloadService.DOWNLOADER_SERVICE_NAME.plus(bookId + sourceId),
            ExistingWorkPolicy.REPLACE,
            work
        )

    }

    fun stopDownloads(context: Context, bookId: Int, sourceId: Long) {
        work = OneTimeWorkRequestBuilder<DownloadService>().apply {
            setInputData(
                Data.Builder().apply {
                    putInt(DownloadService.DOWNLOADER_BOOK_ID, bookId)
                    putLong(DownloadService.DOWNLOADER_SOURCE_ID, sourceId)
                }.build()
            )
        }.build()
        val work = WorkManager.getInstance(context).cancelUniqueWork(
            DownloadService.DOWNLOADER_SERVICE_NAME.plus(bookId + sourceId)
        )

        viewModelScope.launch {
            val book = getBookUseCases.getBookById(bookId).first().data
            if (book != null) {

                insertUseCases.insertBook(book.copy(beingDownloaded = false))
            }

        }

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
    val downloadBookId: Int = Constants.NULL_VALUE,
    val chapters: List<Chapter> = emptyList(),
)