package org.ireader.domain.view_models.detail.book_detail

import android.annotation.SuppressLint
import android.content.Context
import android.webkit.WebView
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.ireader.core.R
import org.ireader.core.utils.UiEvent
import org.ireader.core.utils.UiText
import org.ireader.core.utils.getHtml
import org.ireader.core.utils.removeSameItemsFromList
import org.ireader.domain.feature_services.DownloaderService.DownloadService
import org.ireader.domain.feature_services.DownloaderService.DownloadService.Companion.DOWNLOADER_BOOK_ID
import org.ireader.domain.feature_services.DownloaderService.DownloadService.Companion.DOWNLOADER_SERVICE_NAME
import org.ireader.domain.feature_services.DownloaderService.DownloadService.Companion.DOWNLOADER_SOURCE_ID
import org.ireader.domain.models.entities.Book
import org.ireader.domain.models.entities.Chapter
import org.ireader.domain.source.Extensions
import org.ireader.domain.use_cases.fetchers.FetchUseCase
import org.ireader.domain.use_cases.local.LocalGetChapterUseCase
import org.ireader.domain.utils.Resource
import org.ireader.domain.view_models.base_view_model.asState
import org.ireader.infinity.core.domain.use_cases.local.DeleteUseCase
import org.ireader.infinity.core.domain.use_cases.local.LocalInsertUseCases
import org.ireader.use_cases.remote.RemoteUseCases
import timber.log.Timber
import javax.inject.Inject

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
) : ViewModel() {

    var state by mutableStateOf(DetailState())
        private set

    var isRefreshing by mutableStateOf(false)
        private set

    var expandedSummary by mutableStateOf(false)
        private set

    var chapterState by mutableStateOf(ChapterState())
        private set


    lateinit var work: OneTimeWorkRequest

    val webView: WebView = hiltWebView


    private val _eventFlow = MutableSharedFlow<UiEvent>()

    val eventFlow = _eventFlow.asSharedFlow()
    init {
        val bookId = savedStateHandle.get<Int>("bookId")
        val sourceId = savedStateHandle.get<Long>("sourceId")
        if (bookId != null && sourceId != null) {
            val source = extensions.mappingSourceNameToSource(sourceId)
            state = state.copy(source = source)
            state = state.copy(book = state.book.copy(id = bookId))
            state = state.copy(isLocalLoading = true)
            chapterState = chapterState.copy(isLocalLoading = true)
            getLocalBookById(state.book.id)
        }
    }

    val book by getBookUseCases.getBookByIdDirectly(id = state.book.id).onEach(::onBookUpdate)
        .asState(null, viewModelScope)

    fun onBookUpdate(book: Book?) {
        if (book != null && state.source != null) {
            viewModelScope.launch {
                updateBook(metadata = state.isExploreMode, chapters = state.isExploreMode)
            }
        }
    }

    fun updateBook(
        metadata: Boolean = false,
        chapters: Boolean = false,
    ) {
        val book = book ?: return
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                isRefreshing = true
                try {
                    if (chapters) {
                        getRemoteChapterDetail(book)
                    }
                    if (metadata) {
                        getRemoteBookDetail(book)
                    }
                } catch (e: Throwable) {
                    Timber.e(e, "Error while refreshing Book")
                }
                state = state.copy(isExploreMode = false)
                isRefreshing = false
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


    fun getLocalBookById(bookId: Int) {
        toggleLocalBookLoading(true)
        clearBookError()
        getBookUseCases.getBookById(id = bookId)
            .onEach { result ->
                when (result) {
                    is Resource.Success -> {
                        if (result.data != null) {
                            toggleLocalBookLoading(false)
                            clearBookError()
                            setBook(result.data)
                            toggleInLibrary(result.data.inLibrary)
                            isLocalBookLoaded(true)
                            setExploreMode(result.data.isExploreMode)
                            if (state.isExploreMode && !state.isRemoteLoaded) {
                                getRemoteBookDetail(state.book)
                                getRemoteChapterDetail(state.book)
                            }
                            getLocalChaptersByBookId(bookId = bookId)
                        }
                    }
                    is Resource.Error -> {
                        toggleLocalBookLoading(false)
                        showSnackBar(result.uiText)
                        _eventFlow.emit(UiEvent.ShowSnackbar(result.uiText
                            ?: UiText.DynamicString("")))
                    }
                }
            }.launchIn(viewModelScope)

    }


    private fun getLocalChaptersByBookId(bookId: Int) {
        clearChapterError()
        toggleLocalChapterLoading(true)
        getChapterUseCase.getChaptersByBookId(
            bookId = bookId,
            isAsc = true)
            .onEach { result ->
                when (result) {
                    is Resource.Success -> {
                        if (result.data != null) {
                            toggleLocalChapterLoading(false)
                            clearChapterError()
                            toggleAreChaptersLoaded(true)
                            if (result.data.isNotEmpty()) {
                                setChapters(result.data)
                                toggleAreChaptersLoaded(true)
                            }

                            if (state.book.totalChapters != result.data.size) {
                                insertBookDetailToLocal(state.book.copy(
                                    totalChapters = chapterState.chapters.size))
                            }
                        }
                    }
                    is Resource.Error -> {
                        toggleLocalChapterLoading(false)
                        getRemoteChapterDetail(book = state.book)
                    }
                }
            }.launchIn(viewModelScope)
    }


    fun getRemoteBookDetail(book: Book) {
        toggleRemoteBookLoading(true)
        clearBookError()
        isLocalBookLoaded(false)
        remoteUseCases.getBookDetail(book = book, source = state.source)
            .onEach { result ->
                when (result) {
                    is Resource.Success -> {
                        if (result.data != null) {
                            /**
                             * isExploreMode is needed for inserting because,
                             * the explore screen get a snapshot of explore books
                             * so the inserted book need to have an id and a explore mode.
                             * note: explore mode will toggle off when the user goes back to
                             * main screen
                             */
                            setBook(
                                book = result.data.copy(
                                    id = state.book.id,
                                    isExploreMode = state.isExploreMode,
                                    dataAdded = System.currentTimeMillis()
                                )
                            )
                            toggleRemoteBookLoading(false)
                            clearBookError()
                            isLocalBookLoaded(true)
                            isRemoteBookLoaded(true)
                            insertBookDetailToLocal(state.book)
                        }
                    }
                    is Resource.Error -> {
                        toggleRemoteBookLoading(false)
                        showSnackBar(result.uiText)
                    }
                }
            }.launchIn(viewModelScope)

    }

    fun getRemoteChapterDetail(book: Book) {
        toggleRemoteChaptersLoading(true)
        clearChapterError()
        toggleAreChaptersLoaded(false)
        remoteUseCases.getRemoteChapters(book = book, source = state.source)
            .onEach { result ->
                when (result) {
                    is Resource.Success -> {
                        if (result.data != null) {
                            val uniqueList =
                                removeSameItemsFromList(chapterState.chapters,
                                    result.data.map {
                                        it.copy(
                                            bookId = book.id,
                                            bookName = book.bookName)
                                    }) {
                                    it.title
                                }
                            setChapters(chapters = uniqueList)
                            toggleRemoteChaptersLoading(false)
                            clearChapterError()
//                            chapterState = chapterState.copy(
////                                chapters = result.data.map {
////                                    it.copy(bookId = state.book.id,
////                                        bookName = state.book.bookName)
////                                },
//                            )
                            deleteUseCase.deleteChaptersByBookId(state.book.id)
                            insertChaptersToLocal(uniqueList)
                            getLocalChaptersByBookId(bookId = state.book.id)
                        }
                    }
                    is Resource.Error -> {
                        toggleRemoteChaptersLoading(false)
                        showSnackBar(result.uiText)
                    }
                }
            }.launchIn(viewModelScope)

    }

    @ExperimentalCoroutinesApi
    fun getWebViewData() {
        viewModelScope.launch {
            showSnackBar(UiText.StringResource(org.ireader.core.R.string.trying_to_fetch))
            fetchUseCase.fetchBookDetailAndChapterDetailFromWebView(
                deleteUseCase = deleteUseCase,
                insertUseCases = localInsertUseCases,
                localBook = state.book,
                localChapters = chapterState.chapters,
                source = state.source,
                pageSource = webView.getHtml()
            ).onEach { result ->
                when (result) {
                    is Resource.Success -> {
                        if (result.data != null) {
                            showSnackBar(UiText.DynamicString(result.data.text))
                            toggleRemoteBookLoading(false)
                            toggleRemoteChaptersLoading(false)
                            getLocalChaptersByBookId(bookId = state.book.id)
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

    private fun updateChaptersEntity(inLibrary: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            localInsertUseCases.insertChapters(chapterState.chapters.map {
                it.copy(inLibrary = inLibrary, bookId = state.book.id)
            })
        }
    }

    fun toggleInLibrary(add: Boolean, book: Book? = null) {
        state = state.copy(inLibrary = add)
        viewModelScope.launch(Dispatchers.IO) {
            if (add) {
                insertBookDetailToLocal(
                    book
                        ?: state.book.copy(id = state.book.id,
                            inLibrary = true,
                            dataAdded = System.currentTimeMillis()),
                )
                updateChaptersEntity(true)
            } else {
                insertBookDetailToLocal((
                        book
                            ?: state.book).copy(id = state.book.id, inLibrary = false))
                updateChaptersEntity(false)
            }
        }
    }


    private fun insertChaptersToLocal(chapters: List<Chapter>) {
        viewModelScope.launch(Dispatchers.IO) {
            deleteUseCase.deleteChaptersByBookId(bookId = state.book.id)
            localInsertUseCases.insertChapters(chapters.map { it.copy(bookId = state.book.id) })
        }
    }

    fun startDownloadService(context: Context) {
        work = OneTimeWorkRequestBuilder<DownloadService>().apply {
            setInputData(
                Data.Builder().apply {
                    putInt(DOWNLOADER_BOOK_ID, state.book.id)
                    putLong(DOWNLOADER_SOURCE_ID, state.book.sourceId)
                }.build()
            )
            addTag(DOWNLOADER_SERVICE_NAME)
        }.build()
        WorkManager.getInstance(context).enqueueUniqueWork(
            DOWNLOADER_SERVICE_NAME.plus(state.book.id + state.book.sourceId),
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

    private fun toggleLocalChapterLoading(isLoading: Boolean) {
        chapterState = chapterState.copy(isLocalLoading = isLoading)
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

    private fun toggleRemoteChaptersLoading(isLoading: Boolean) {
        chapterState = chapterState.copy(isRemoteLoading = isLoading)
    }

    /********************************************************/
    private fun isLocalBookLoaded(loaded: Boolean) {
        state = state.copy(isLocalLoaded = loaded)
    }

    private fun isRemoteBookLoaded(loaded: Boolean) {
        state = state.copy(isRemoteLoaded = loaded)
    }

    private fun setExploreMode(isEnable: Boolean) {
        state = state.copy(isExploreMode = isEnable)
    }

    private fun toggleLocalBookLoading(isLoading: Boolean) {
        state = state.copy(isLocalLoading = isLoading)
    }

    private fun toggleRemoteBookLoading(isLoading: Boolean) {
        state = state.copy(isRemoteLoading = isLoading)
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


}


