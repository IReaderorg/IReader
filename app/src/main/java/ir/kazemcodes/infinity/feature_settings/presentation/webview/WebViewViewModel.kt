package ir.kazemcodes.infinity.feature_settings.presentation.webview

import android.content.Context
import android.webkit.WebView
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import com.zhuinden.simplestack.ScopedServices
import ir.kazemcodes.infinity.core.data.network.models.Source
import ir.kazemcodes.infinity.core.data.network.utils.toast
import ir.kazemcodes.infinity.core.domain.models.Book
import ir.kazemcodes.infinity.core.domain.models.Chapter
import ir.kazemcodes.infinity.core.domain.repository.LocalBookRepository
import ir.kazemcodes.infinity.core.domain.repository.LocalChapterRepository
import ir.kazemcodes.infinity.core.utils.Resource
import ir.kazemcodes.infinity.core.utils.UiEvent
import ir.kazemcodes.infinity.core.utils.UiText
import ir.kazemcodes.infinity.core.utils.getHtml
import ir.kazemcodes.infinity.feature_sources.sources.models.FetchType
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.jsoup.Jsoup
import timber.log.Timber

class WebViewPageModel(
    private val url: String,
    private val bookName: String? = null,
    private val source: Source,
    private val chapterTitle: String? = null,
    private val fetcher: FetchType,
    private val webView: WebView,
    private val localBookRepository: LocalBookRepository,
    private val localChapterRepository: LocalChapterRepository,
) : ScopedServices.Registered {

    private val _state =
        mutableStateOf<WebViewPageState>(WebViewPageState(webView = webView,
            url = url,
            fetcher = fetcher))
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

    override fun onServiceRegistered() {
        if (bookName != null) {
            getLocalBookByName(bookName = bookName)

        }
        if (bookName != null && chapterTitle != null) {
            getLocalChapterByName(bookName, chapterTitle)
        }
    }

    override fun onServiceUnregistered() {

    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun fetchInfo() {
        coroutineScope.launch {
            when (fetcher) {
                FetchType.Popular -> {
                    val docs = source.popularParse(Jsoup.parse(webView.getHtml()))
                    Timber.d("WebView: ${docs}")
                }
                FetchType.Latest -> {
                    val docs = source.latestParse(Jsoup.parse(webView.getHtml()))
                    Timber.d("WebView: ${docs}")
                }
                FetchType.Search -> {
                    val docs = source.searchParse(Jsoup.parse(webView.getHtml()))
                    Timber.d("WebView: ${docs}")
                }
                FetchType.Detail -> {
                    val book = source.detailParse(Jsoup.parse(webView.getHtml()))
                    val chapters = source.chaptersParse(Jsoup.parse(webView.getHtml()))
                    _state.value = state.value.copy(book = book.book, chapters = chapters.chapters)
                    getLocalBook(book.book)
                    if (chapters.chapters.isNotEmpty()) {
                        deleteChapterDetails()
                        insertChaptersToLocal(chapters.chapters)
                    }
                    _eventFlow.emit(UiEvent.ShowSnackbar(
                        uiText = UiText.DynamicString("${book.book.bookName} was fetched with ${chapters.chapters.size} chapters")
                    ))

                    Timber.d("WebView: ${chapters}")

                }
                FetchType.Content -> {
                    val content = source.contentFromElementParse(Jsoup.parse(webView.getHtml()))
                    localChapterRepository.updateChapter(state.value.chapter!!.toChapterEntity())
                    _eventFlow.emit(UiEvent.ShowSnackbar(
                        uiText = UiText.DynamicString("${state.value.book.bookName} content was fetched with ${content.content.size} letter")
                    ))
                }
                FetchType.Chapter -> {
                    val docs = source.chaptersParse(Jsoup.parse(webView.getHtml()))
                    Timber.d("WebView: ${docs}")
                }
            }
            val docs = source?.detailParse(Jsoup.parse(webView.getHtml()))
            Timber.d("WebView: ${docs}")
        }


    }

    fun insertChaptersToLocal(chapters: List<Chapter>) {
        coroutineScope.launch(Dispatchers.IO) {
            localChapterRepository.insertChapters(
                chapters,
                state.value.book,
                source = source,
                inLibrary = state.value.book.inLibrary
            )
        }
    }

    private fun getLocalBook(book: Book) {
        localBookRepository.getLocalBookByName(state.value.book.bookName).onEach { result ->
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
                is Resource.Loading -> {

                }
            }
        }.launchIn(coroutineScope)
    }

    private fun getLocalBookByName(bookName: String) {
        localBookRepository.getLocalBookByName(bookName).onEach { result ->
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
                is Resource.Loading -> {

                }
            }
        }.launchIn(coroutineScope)
    }

    private fun getLocalChapterByName(bookName: String, chapterTitle: String) {
        localChapterRepository.getChapterByChapter(chapterTitle = chapterTitle,
            bookName = bookName,
            source = source.name).onEach { result ->
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
                is Resource.Loading -> {

                }
            }
        }.launchIn(coroutineScope)
    }

    fun insertBookDetailToLocal(book: Book) {
        coroutineScope.launch(Dispatchers.IO) {
            localBookRepository.insertBook(book)
        }
    }

    fun deleteChapterDetails() {
        coroutineScope.launch(Dispatchers.IO) {
            localChapterRepository.deleteChapters(state.value.book.bookName, source.name)
        }
    }
}

data class WebViewPageState(
    val webView: WebView,
    val url: String,
    val book: Book = Book.create(),
    val books: List<Book> = emptyList<Book>(),
    val chapters: List<Chapter> = emptyList<Chapter>(),
    val chapter: Chapter? = null,
    val fetcher: FetchType,
)