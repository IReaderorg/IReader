package ir.kazemcodes.infinity.feature_settings.presentation.webview

import android.content.Context
import android.webkit.WebView
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.kazemcodes.infinity.core.data.network.models.Source
import ir.kazemcodes.infinity.core.data.network.utils.toast
import ir.kazemcodes.infinity.core.domain.models.Book
import ir.kazemcodes.infinity.core.domain.models.Chapter
import ir.kazemcodes.infinity.core.domain.use_cases.fetchers.FetchUseCase
import ir.kazemcodes.infinity.core.domain.use_cases.local.DeleteUseCase
import ir.kazemcodes.infinity.core.domain.use_cases.local.LocalGetBookUseCases
import ir.kazemcodes.infinity.core.domain.use_cases.local.LocalGetChapterUseCase
import ir.kazemcodes.infinity.core.domain.use_cases.local.LocalInsertUseCases
import ir.kazemcodes.infinity.core.utils.*
import ir.kazemcodes.infinity.feature_activity.presentation.NavigationArgs
import ir.kazemcodes.infinity.feature_sources.sources.Extensions
import ir.kazemcodes.infinity.feature_sources.sources.models.FetchType
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import javax.inject.Inject

@HiltViewModel
class WebViewPageModel @Inject constructor(
    private val insetUseCases: LocalInsertUseCases,
    private val deleteUseCase: DeleteUseCase,
    private val getBookUseCases: LocalGetBookUseCases,
    private val getChapterUseCase: LocalGetChapterUseCase,
    private val fetcherUseCase: FetchUseCase,
    private val webView: WebView,
    private val savedStateHandle: SavedStateHandle,
    private val extensions: Extensions,
) : ViewModel() {


    init {
        val sourceId = savedStateHandle.get<Long>(NavigationArgs.sourceId.name)
        val chapterId = savedStateHandle.get<Int>(NavigationArgs.chapterId.name)
        val bookId = savedStateHandle.get<Int>(NavigationArgs.bookId.name)
        val url = URLDecoder.decode(savedStateHandle.get<String>(NavigationArgs.url.name),StandardCharsets.UTF_8.name())
        val fetcher = savedStateHandle.get<Int>(NavigationArgs.fetchType.name)
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _state.value = state.value.copy(url=url)
                sourceId?.let {
                    _state.value = state.value.copy(source = extensions.mappingSourceNameToSource(it))
                }
                bookId?.let { _state.value = state.value.copy(book = state.value.book.copy(id = it)) }
                chapterId?.let {
                    _state.value = state.value.copy(chapter = state.value.chapter?.copy(chapterId = it))
                }
                if (bookId != null) {
                    getLocalChaptersByBookName(bookId)
                    getBookById(bookId = bookId)
                }
                if (bookId != null && chapterId != null) {
                    getLocalChapterByName(chapterId)
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _state.value = state.value.copy(url=url)
                    sourceId?.let {
                        _state.value = state.value.copy(source = extensions.mappingSourceNameToSource(it))
                    }
                    bookId?.let { _state.value = state.value.copy(book = state.value.book.copy(id = it)) }
                    chapterId?.let {
                        _state.value = state.value.copy(chapter = state.value.chapter?.copy(chapterId = it))
                    }
                    if (bookId != null) {
                        getLocalChaptersByBookName(bookId)
                        getBookById(bookId = bookId)
                    }
                    if (bookId != null && chapterId != null) {
                        getLocalChapterByName(chapterId)
                    }
                }
            }
        }



    }

    private val _state =
        mutableStateOf<WebViewPageState>(WebViewPageState(webView = webView,
            source = extensions.mappingSourceNameToSource(0)))
    val state: State<WebViewPageState> = _state

    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()


    fun onEvent(event: WebPageEvent) {
        when (event) {
            is WebPageEvent.OnFetched -> {
                showToast(event.context)
            }
        }
    }

    fun showToast(context: Context) {
        context.toast("Event is Occured")
    }


    @ExperimentalCoroutinesApi
    fun getInfo() {
        coroutineScope.launch(Dispatchers.Main) {
            _eventFlow.emit(UiEvent.ShowSnackbar(
                uiText = UiText.DynamicString("Trying to fetch...").asString()
            ))
            fetcherUseCase.fetchBookDetailAndChapterDetailFromWebView(
                localBook = state.value.book,
                localChapters = state.value.chapters,
                source = state.value.source,
                insertUseCases = insetUseCases,
                deleteUseCase = deleteUseCase,
                pageSource = webView.getHtml()
            ).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        if (result.data != null) {
                            _eventFlow.emit(UiEvent.ShowSnackbar(
                                uiText = result.data.asString()
                            ))
                        }
                    }
                    is Resource.Error -> {
                        _eventFlow.emit(UiEvent.ShowSnackbar(
                            uiText = UiText.DynamicString(result.uiText ?: UiText.unknownError()
                                .asString()).asString()
                        ))
                    }
                }
            }
        }


    }


    fun insertChaptersToLocal(chapters: List<Chapter>) {
        coroutineScope.launch(Dispatchers.IO) {
            insetUseCases.insertChapters(
                chapters
            )
        }
    }

    private fun getLocalBook(book: Book) {
        getBookUseCases.getBookById(state.value.book.id).onEach { result ->
            when (result) {
                is Resource.Success -> {
                    if (result.data != null && result.data != Book.create()) {
                        _state.value = state.value.copy(
                            book = result.data,
                        )
                        insertBookDetailToLocal(result.data.copy(category = book.category,
                            status = book.status,
                            description = book.description,
                            author = book.author,
                            rating = book.rating))
                        //insertChaptersToLocal(state.value.chapters)
                    }
                }
                is Resource.Error -> {

                }
            }
        }.launchIn(coroutineScope)
    }

    private fun getLocalChaptersByBookName(bookId: Int) {
        getChapterUseCase.getChaptersByBookId(bookId = bookId)
            .onEach { result ->
                when (result) {
                    is Resource.Success -> {
                        if (!result.data.isNullOrEmpty()) {
                            _state.value = state.value.copy(
                                chapters = result.data,
                            )
                        }
                    }
                    is Resource.Error -> {

                    }
                }
            }.launchIn(coroutineScope)
    }

    private fun getBookById(bookId: Int) {
        getBookUseCases.getBookById(id = bookId).onEach { result ->
            when (result) {
                is Resource.Success -> {
                    if (result.data != null && result.data != Book.create()) {
                        _state.value = state.value.copy(
                            book = result.data,
                        )
                        //insertChaptersToLocal(state.value.chapters)
                    }
                }
                is Resource.Error -> {

                }
            }
        }.launchIn(coroutineScope)
    }

    private fun getLocalChapterByName(chapterId: Int) {
        getChapterUseCase.getOneChapterById(chapterId).onEach { result ->
            when (result) {
                is Resource.Success -> {
                    if (result.data != null) {
                        _state.value = state.value.copy(
                            chapter = result.data,
                        )
                        //insertChaptersToLocal(state.value.chapters)
                    }
                }
                is Resource.Error -> {

                }
            }
        }.launchIn(coroutineScope)
    }

    fun insertBookDetailToLocal(book: Book) {
        coroutineScope.launch(Dispatchers.IO) {
            insetUseCases.insertBook(book)
        }
    }

    fun deleteChapterDetails() {
        coroutineScope.launch(Dispatchers.IO) {
            if (state.value.book.id != 0) {
                deleteUseCase.deleteChaptersByBookId(state.value.book.id)
            }
        }
    }

    override fun onCleared() {
        coroutineScope.cancel()
        super.onCleared()
    }

}

data class WebViewPageState(
    val webView: WebView,
    val url: String = "",
    val book: Book = Book.create(),
    val books: List<Book> = emptyList<Book>(),
    val chapters: List<Chapter> = emptyList<Chapter>(),
    val chapter: Chapter? = null,
    val fetcher: FetchType = FetchType.Latest,
    val source: Source,
)