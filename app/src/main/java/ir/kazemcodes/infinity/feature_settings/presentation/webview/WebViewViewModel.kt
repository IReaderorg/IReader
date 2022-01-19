package ir.kazemcodes.infinity.feature_settings.presentation.webview

import android.webkit.WebView
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import com.zhuinden.simplestack.ScopedServices
import ir.kazemcodes.infinity.core.data.network.models.Source
import ir.kazemcodes.infinity.core.domain.models.Book
import ir.kazemcodes.infinity.core.domain.models.Chapter
import ir.kazemcodes.infinity.core.domain.repository.LocalBookRepository
import ir.kazemcodes.infinity.core.domain.repository.LocalChapterRepository
import ir.kazemcodes.infinity.core.utils.Resource
import ir.kazemcodes.infinity.core.utils.getHtml
import ir.kazemcodes.infinity.feature_sources.sources.models.FetchType
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.jsoup.Jsoup
import timber.log.Timber

class WebViewPageModel(
    private val url: String,
    private val source: Source,
    private val fetcher: FetchType? = null,
    private val webView: WebView,
    private val localBookRepository: LocalBookRepository,
    private val localChapterRepository: LocalChapterRepository,
) : ScopedServices.Registered {

    private val _state =
        mutableStateOf<WebViewPageState>(WebViewPageState(webView = webView, url = url))
    val state: State<WebViewPageState> = _state

    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    override fun onServiceRegistered() {

    }

    override fun onServiceUnregistered() {

    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun fetchInfo() {
        coroutineScope.launch {
            when(fetcher) {
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
                    insertChaptersToLocal(chapters.chapters)
                    getLocalBookByName(book.book)

                    Timber.d("WebView: ${chapters}")

                }
                FetchType.Content -> {
                    val docs = source.contentFromElementParse(Jsoup.parse(webView.getHtml()))
                    Timber.d("WebView: ${docs}")
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
    private fun getLocalBookByName(book: Book) {
        localBookRepository.getLocalBookByName(state.value.book.bookName).onEach { result ->
            when (result) {
                is Resource.Success -> {
                    if (result.data != null && result.data != Book.create()) {
                        _state.value = state.value.copy(
                            book = result.data,
                        )
                        insertBookDetailToLocal(result.data.copy(category = book.category, status = book.status, description = book.description, author = book.author, rating = book.rating))
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
}

data class WebViewPageState(
    val webView: WebView,
    val url: String,
    val book: Book =  Book.create(),
    val books: List<Book> = emptyList<Book>(),
    val chapters: List<Chapter> = emptyList<Chapter>(),
)