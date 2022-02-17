package org.ireader.domain.view_models.detail.book_detail

import android.annotation.SuppressLint
import android.content.Context
import android.webkit.WebView
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.work.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.ireader.core.R
import org.ireader.core.utils.UiEvent
import org.ireader.core.utils.UiText
import org.ireader.core.utils.getHtml
import org.ireader.core.utils.removeSameItemsFromList
import org.ireader.core_ui.viewmodel.BaseViewModel
import org.ireader.domain.feature_services.DownloaderService.DownloadService
import org.ireader.domain.feature_services.DownloaderService.DownloadService.Companion.DOWNLOADER_BOOK_ID
import org.ireader.domain.feature_services.DownloaderService.DownloadService.Companion.DOWNLOADER_SERVICE_NAME
import org.ireader.domain.feature_services.DownloaderService.DownloadService.Companion.DOWNLOADER_SOURCE_ID
import org.ireader.domain.models.entities.Book
import org.ireader.domain.models.entities.Chapter
import org.ireader.domain.models.entities.updateBook
import org.ireader.domain.models.source.Source
import org.ireader.domain.source.Extensions
import org.ireader.domain.source.HttpSource
import org.ireader.domain.use_cases.fetchers.FetchUseCase
import org.ireader.domain.use_cases.local.DeleteUseCase
import org.ireader.domain.use_cases.local.LocalGetChapterUseCase
import org.ireader.domain.use_cases.local.LocalInsertUseCases
import org.ireader.domain.use_cases.remote.RemoteUseCases
import org.ireader.domain.utils.Resource
import javax.inject.Inject


/**
 * need to change this to BaseViewModel later
 */
@SuppressLint("StaticFieldLeak")
@HiltViewModel
class BookDetailViewModel @Inject constructor(
    private val localInsertUseCases: LocalInsertUseCases,
    private val getChapterUseCase: LocalGetChapterUseCase,
    private val getBookUseCases: org.ireader.domain.use_cases.local.LocalGetBookUseCases,
    private val remoteUseCases: RemoteUseCases,
    private val deleteUseCase: DeleteUseCase,
    private val fetchUseCase: FetchUseCase,
    private val hiltWebView: WebView,
    savedStateHandle: SavedStateHandle,
    extensions: Extensions,
) : BaseViewModel() {

    var state by mutableStateOf(DetailState())
        private set

    var isRefreshing by mutableStateOf(false)
        private set

    var expandedSummary by mutableStateOf(false)
        private set

    var chapterState by mutableStateOf(ChapterState())
        private set

    var getFromWebViewJob: Job? = null
    var getBookDetailJob: Job? = null
    var getChapterDetailJob: Job? = null

    lateinit var work: OneTimeWorkRequest

    val webView: WebView = hiltWebView


    private val _eventFlow = MutableSharedFlow<UiEvent>()

    val eventFlow = _eventFlow.asSharedFlow()


    init {
        val bookId = savedStateHandle.get<Long>("bookId")
        val sourceId = savedStateHandle.get<Long>("sourceId")
        if (bookId != null && sourceId != null) {
            val source = extensions.mappingSourceNameToSource(sourceId)
            if (source != null) {
                state = state.copy(source = source)
                state = state.copy(isLoading = true)
                chapterState = chapterState.copy(isLoading = true)
                getLocalBookById(bookId, source)
                getLocalChaptersByBookId(bookId = bookId)
            } else {
                viewModelScope.launch {
                    showSnackBar(UiText.StringResource(R.string.the_source_is_not_found))
                }
            }
        } else {
            viewModelScope.launch {
                showSnackBar(UiText.StringResource(R.string.something_is_wrong_with_this_book))
            }
        }
    }

    fun onEvent(event: BookDetailEvent) {
        when (event) {
            is BookDetailEvent.ToggleSummary -> {
                toggleExpandedSummary()
            }

        }
    }


    fun getLocalBookById(bookId: Long, source: Source) {
        this.toggleLoading(true)
        clearBookError()
        getBookUseCases.getBookById(id = bookId)
            .onEach { book ->
                if (book != null) {
                    this.toggleLoading(false)
                    clearBookError()
                    setBook(book)
                    toggleInLibrary(book.favorite)
                    if (book.lastUpdated < 1L && !state.isRemoteLoaded) {
                        getRemoteBookDetail(book, source)
                        getRemoteChapterDetail(book, source)
                    }
                    isLocalBookLoaded(true)
                } else {
                    this.toggleLoading(false)
                    showSnackBar(UiText.StringResource(R.string.no_book_found_error))
                }
            }.launchIn(viewModelScope)

    }


    fun getLocalChaptersByBookId(bookId: Long) {
        clearChapterError()
        this.toggleChaptersLoading(true)
        getChapterUseCase.getChaptersByBookId(
            bookId = bookId,
            isAsc = true)
            .onEach { chapters ->
                if (chapters.isNotEmpty()) {
                    this.toggleChaptersLoading(false)
                    clearChapterError()
                    setChapters(chapters)
                    toggleAreChaptersLoaded(true)
                }
                this.toggleChaptersLoading(false)
            }.launchIn(viewModelScope)
    }


    fun getRemoteBookDetail(book: Book, source: Source) {
        this.toggleLoading(true)
        clearBookError()
        isLocalBookLoaded(false)
        getBookDetailJob?.cancel()
        getBookDetailJob = remoteUseCases.getBookDetail(book = book, source = source)
            .onEach { result ->
                when (result) {
                    is Resource.Success -> {
                        if (result.data != null && state.book != null) {
                            setBook(
                                book = updateBook(
                                    result.data,
                                    book
                                )
                            )
                            this.toggleLoading(false)
                            clearBookError()
                            isLocalBookLoaded(true)
                            isRemoteBookLoaded(true)
                            insertBookDetailToLocal(state.book!!)
                        }
                    }
                    is Resource.Error -> {
                        this.toggleLoading(false)
                        showSnackBar(result.uiText)
                    }
                }
            }.launchIn(viewModelScope)

    }

    fun getRemoteChapterDetail(book: Book, source: Source) {
        this.toggleChaptersLoading(true)
        clearChapterError()
        toggleAreChaptersLoaded(false)
        getChapterDetailJob?.cancel()
        getChapterDetailJob = remoteUseCases.getRemoteChapters(book = book, source = source)
            .onEach { result ->
                when (result) {
                    is Resource.Success -> {
                        if (result.data != null) {
                            val uniqueList =
                                removeSameItemsFromList(chapterState.chapters,
                                    result.data) {
                                    it.title
                                }
                            setChapters(chapters = uniqueList)
                            this.toggleChaptersLoading(false)
                            clearChapterError()
                            deleteUseCase.deleteChaptersByBookId(book.id)
                            insertChaptersToLocal(uniqueList, book.id)
                            getLocalChaptersByBookId(bookId = book.id)
                        }
                    }
                    is Resource.Error -> {
                        showSnackBar(result.uiText)
                        this.toggleChaptersLoading(false)
                    }
                }
            }.launchIn(viewModelScope)

    }

    @ExperimentalCoroutinesApi
    fun getWebViewData(source: Source) {
        getFromWebViewJob?.cancel()
        webView.settings.userAgentString =
            source.headers["User-Agent"] ?: HttpSource.DEFAULT_USER_AGENT
        getFromWebViewJob = viewModelScope.launch {
            showSnackBar(UiText.StringResource(org.ireader.core.R.string.trying_to_fetch))
            fetchUseCase.fetchBookDetailAndChapterDetailFromWebView(
                deleteUseCase = deleteUseCase,
                insertUseCases = localInsertUseCases,
                localBook = state.book,
                localChapters = chapterState.chapters,
                source = source,
                pageSource = webView.getHtml()
            ).onEach { result ->
                when (result) {
                    is Resource.Success -> {
                        if (result.data != null && state.book != null) {
                            showSnackBar(UiText.DynamicString(result.data.text))
                            this@BookDetailViewModel.toggleLoading(false)
                            this@BookDetailViewModel.toggleChaptersLoading(false)
                            getLocalChaptersByBookId(bookId = state.book!!.id)
                        }
                    }
                    is Resource.Error -> {
                        showSnackBar(result.uiText)
                    }
                }
            }.launchIn(viewModelScope)
        }
    }


    private fun insertBookDetailToLocal(book: Book) {
        viewModelScope.launch(Dispatchers.IO) {
            localInsertUseCases.insertBook(book)
        }
    }

    private fun updateChaptersEntity(inLibrary: Boolean, bookId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            deleteUseCase.deleteChaptersByBookId(bookId)
            localInsertUseCases.insertChapters(chapterState.chapters.map {
                it.copy(bookId = bookId, inLibrary = inLibrary)
            })
        }
    }

    fun toggleInLibrary(add: Boolean, book: Book) {
        state = state.copy(inLibrary = add)
        viewModelScope.launch(Dispatchers.IO) {
            if (add) {
                insertBookDetailToLocal(
                    book.copy(
                        id = book.id,
                        favorite = true,
                        dataAdded = System.currentTimeMillis(),
                    )
                )
                updateChaptersEntity(true, book.id)
            } else {
                insertBookDetailToLocal((
                        book.copy(
                            id = book.id,
                            favorite = false,
                        )))
                updateChaptersEntity(false, book.id)
            }
        }
    }


    private fun insertChaptersToLocal(chapters: List<Chapter>, bookId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            deleteUseCase.deleteChaptersByBookId(bookId = bookId)
            localInsertUseCases.insertChapters(chapters.map { it.copy(bookId = bookId) })
        }
    }

    fun startDownloadService(context: Context, book: Book) {
        work = OneTimeWorkRequestBuilder<DownloadService>().apply {
            setInputData(
                Data.Builder().apply {
                    putLong(DOWNLOADER_BOOK_ID, book.id)
                    putLong(DOWNLOADER_SOURCE_ID, book.sourceId)
                }.build()
            )
            addTag(DOWNLOADER_SERVICE_NAME)
        }.build()
        WorkManager.getInstance(context).enqueueUniqueWork(
            DOWNLOADER_SERVICE_NAME.plus(book.id + book.sourceId),
            ExistingWorkPolicy.REPLACE,
            work
        )
    }

    fun toggleExpandedSummary() {
        expandedSummary = !expandedSummary
    }

    /************************************************************/
    suspend fun showSnackBar(message: UiText?) {
        _eventFlow.emit(
            UiEvent.ShowSnackbar(
                uiText = message ?: UiText.StringResource(R.string.error_unknown)
            )
        )
    }

    private fun toggleChaptersLoading(isLoading: Boolean) {
        chapterState = chapterState.copy(isLoading = isLoading)
    }

    private fun clearChapterError() {
        chapterState = chapterState.copy(error = UiText.DynamicString(""))
    }

    private fun toggleAreChaptersLoaded(loaded: Boolean) {
        chapterState = chapterState.copy(loaded = loaded)
    }

    private fun setChapters(chapters: List<Chapter>) {
        chapterState = chapterState.copy(chapters = chapters)
    }


    /********************************************************/
    private fun isLocalBookLoaded(loaded: Boolean) {
        state = state.copy(isLocalLoaded = loaded)
    }

    private fun isRemoteBookLoaded(loaded: Boolean) {
        state = state.copy(isRemoteLoaded = loaded)
    }

    private fun toggleLoading(isLoading: Boolean) {
        state = state.copy(isLoading = isLoading)
    }


    private fun toggleInLibrary(enable: Boolean) {
        state = state.copy(inLibrary = enable)
    }

    private fun setBook(book: Book) {
        state = state.copy(book = book)
    }

    private fun clearBookError() {
        state = state.copy(error = UiText.DynamicString(""))
    }

    override fun onDestroy() {
        getBookDetailJob?.cancel()
        super.onDestroy()
    }
}


