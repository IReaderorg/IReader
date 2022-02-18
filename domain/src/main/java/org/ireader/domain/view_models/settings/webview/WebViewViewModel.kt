package org.ireader.presentation.feature_settings.presentation.webview

import android.annotation.SuppressLint
import android.webkit.WebView
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.ireader.core.utils.Constants
import org.ireader.core.utils.UiEvent
import org.ireader.core.utils.UiText
import org.ireader.core.utils.getHtml
import org.ireader.domain.R
import org.ireader.domain.models.entities.Book
import org.ireader.domain.models.entities.Chapter
import org.ireader.domain.models.source.FetchType
import org.ireader.domain.models.source.HttpSource
import org.ireader.domain.models.source.Source
import org.ireader.domain.source.Extensions
import org.ireader.domain.ui.NavigationArgs
import org.ireader.domain.use_cases.fetchers.FetchUseCase
import org.ireader.domain.use_cases.local.DeleteUseCase
import org.ireader.domain.use_cases.local.LocalGetChapterUseCase
import org.ireader.domain.use_cases.local.LocalInsertUseCases
import org.ireader.domain.utils.Resource
import org.ireader.domain.view_models.settings.webview.mapFetcher
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import javax.inject.Inject

/**This is fake Alert **/
@SuppressLint("StaticFieldLeak")
@HiltViewModel
class WebViewPageModel @Inject constructor(
    private val insetUseCases: LocalInsertUseCases,
    private val deleteUseCase: DeleteUseCase,
    private val getBookUseCases: org.ireader.domain.use_cases.local.LocalGetBookUseCases,
    private val getChapterUseCase: LocalGetChapterUseCase,
    private val fetcherUseCase: FetchUseCase,
    private val webView: WebView,
    private val savedStateHandle: SavedStateHandle,
    private val extensions: Extensions,
) : ViewModel() {

    var state by mutableStateOf<WebViewPageState>(WebViewPageState(webView = webView))
        private set

    init {
        val sourceId = savedStateHandle.get<Long>(NavigationArgs.sourceId.name)
        val bookId = savedStateHandle.get<Long>(NavigationArgs.bookId.name)
        val chapterId = savedStateHandle.get<Long>(NavigationArgs.chapterId.name)
        val url = URLDecoder.decode(savedStateHandle.get<String>(NavigationArgs.url.name),
            StandardCharsets.UTF_8.name())
        val fetcher = savedStateHandle.get<Int>(NavigationArgs.fetchType.name)

        if (sourceId != null && chapterId != null && bookId != null) {
            state = state.copy(source = extensions.findSourceById(sourceId))
        }
        if (fetcher != null) {
            state = state.copy(fetcher = mapFetcher(fetcher))
        }
        state = state.copy(url = url)

        if (bookId != null && bookId != Constants.NULL_VALUE) {
            getLocalChaptersByBookName(bookId)
            getBookById(bookId = bookId)
        }
        if (bookId != null && chapterId != null && bookId != Constants.NULL_VALUE && chapterId != Constants.NULL_VALUE) {
            getLocalChapterById(chapterId)
        }

    }

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()


    @ExperimentalCoroutinesApi
    fun getInfo(source: Source) {
        viewModelScope.launch {
            _eventFlow.emit(UiEvent.ShowSnackbar(
                uiText = UiText.StringResource(R.string.trying_to_fetch)
            ))
            webView.settings.userAgentString =
                source.headers.get("User-Agent") ?: HttpSource.DEFAULT_USER_AGENT
            fetcherUseCase.fetchBookDetailAndChapterDetailFromWebView(
                localBook = state.book,
                localChapters = state.chapters,
                source = source,
                insertUseCases = insetUseCases,
                deleteUseCase = deleteUseCase,
                pageSource = webView.getHtml(),
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


    private fun getLocalChaptersByBookName(bookId: Long) {
        getChapterUseCase.getChaptersByBookId(bookId = bookId)
            .onEach { chapters ->
                if (chapters.isNotEmpty()) {
                    state = state.copy(
                        chapters = chapters,
                    )
                }
            }.launchIn(viewModelScope)
    }

    private fun getBookById(bookId: Long) {
        getBookUseCases.getBookById(id = bookId).onEach { book ->
            if (book != null) {
                state = state.copy(
                    book = book,
                )
            }
        }.launchIn(viewModelScope)
    }

    private fun getLocalChapterById(chapterId: Long) {
        viewModelScope.launch {
            getChapterUseCase.getOneChapterById(chapterId).first { result ->
                if (result != null) {
                    state = state.copy(
                        chapter = result,
                    )
                    //insertChaptersToLocal(state.chapters)
                }
                true
            }
        }
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
    val book: Book? = null,
    val books: List<Book> = emptyList<Book>(),
    val chapters: List<Chapter> = emptyList<Chapter>(),
    val chapter: Chapter? = null,
    val fetcher: FetchType = FetchType.LatestFetchType,
    val source: Source? = null,
)