package ir.kazemcodes.infinity.feature_settings.presentation.webview

import android.annotation.SuppressLint
import android.webkit.WebView
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import ir.kazemcodes.infinity.R
import ir.kazemcodes.infinity.core.data.network.models.Source
import ir.kazemcodes.infinity.core.domain.models.Book
import ir.kazemcodes.infinity.core.domain.models.Chapter
import ir.kazemcodes.infinity.core.domain.use_cases.fetchers.FetchUseCase
import ir.kazemcodes.infinity.core.domain.use_cases.local.DeleteUseCase
import ir.kazemcodes.infinity.core.domain.use_cases.local.LocalGetBookUseCases
import ir.kazemcodes.infinity.core.domain.use_cases.local.LocalGetChapterUseCase
import ir.kazemcodes.infinity.core.domain.use_cases.local.LocalInsertUseCases
import ir.kazemcodes.infinity.core.ui.NavigationArgs
import ir.kazemcodes.infinity.core.utils.*
import ir.kazemcodes.infinity.feature_sources.sources.Extensions
import ir.kazemcodes.infinity.feature_sources.sources.models.FetchType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import javax.inject.Inject

/**This is fake Alert **/
@SuppressLint("StaticFieldLeak")
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

    private val _state =
        mutableStateOf<WebViewPageState>(WebViewPageState(webView = webView,
            source = extensions.mappingSourceNameToSource(0)))
    val state: State<WebViewPageState> = _state

    init {
        val sourceId = savedStateHandle.get<Long>(NavigationArgs.sourceId.name)
        val chapterId = savedStateHandle.get<Int>(NavigationArgs.chapterId.name)
        val bookId = savedStateHandle.get<Int>(NavigationArgs.bookId.name)
        val url = URLDecoder.decode(savedStateHandle.get<String>(NavigationArgs.url.name),
            StandardCharsets.UTF_8.name())
        val fetcher = savedStateHandle.get<Int>(NavigationArgs.fetchType.name)

        if (sourceId != null && chapterId != null && bookId != null) {
            _state.value = state.value.copy(source = extensions.mappingSourceNameToSource(sourceId))
            _state.value = state.value.copy(book = state.value.book.copy(id = bookId))
            _state.value = state.value.copy(chapter = state.value.chapter?.copy(chapterId = chapterId))
        }
        if (fetcher != null) {
            _state.value = state.value.copy(fetcher = mapFetcher(fetcher))
        }
        _state.value = state.value.copy(url = url)

        if (bookId != null && bookId != Constants.NULL_VALUE) {
            getLocalChaptersByBookName(bookId)
            getBookById(bookId = bookId)
        }
        if (bookId != null && chapterId != null && bookId != Constants.NULL_VALUE && chapterId != Constants.NULL_VALUE) {
            getLocalChapterByName(chapterId)
        }

    }
    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()


    @ExperimentalCoroutinesApi
    fun getInfo() {
        viewModelScope.launch {
            _eventFlow.emit(UiEvent.ShowSnackbar(
                uiText = UiText.StringResource(R.string.trying_to_fetch)
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
                                uiText = result.data
                            ))
                        }
                    }
                    is Resource.Error -> {
                        _eventFlow.emit(UiEvent.ShowSnackbar(

                            uiText = result.uiText ?: UiText.StringResource(R.string.error_unknown)
                        ))
                    }
                }
            }
        }


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
            }.launchIn(viewModelScope)
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
        }.launchIn(viewModelScope)
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
        }.launchIn(viewModelScope)
    }

    fun insertBookDetailToLocal(book: Book) {
        viewModelScope.launch(Dispatchers.IO) {
            insetUseCases.insertBook(book)
        }
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