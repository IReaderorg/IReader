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
import ir.kazemcodes.infinity.core.presentation.components.WebViewFetcher
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

    val viewModelExt = WebViewFetcher(coroutineScope = coroutineScope,
        source = source,
        fetcher = fetcher,
        localBookRepository = localBookRepository,
        localChapterRepository = localChapterRepository,
        url = url)
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

    fun getInfo() {
        viewModelExt.fetchInfo().onEach { result ->
                when (result) {
                    is Resource.Success -> {
                        if (result.data != null) {
                            _eventFlow.emit(UiEvent.ShowSnackbar(
                                uiText =result.data
                            ))
                        }
                    }
                    is Resource.Error -> {
                        _eventFlow.emit(UiEvent.ShowSnackbar(
                            uiText = UiText.DynamicString(result.message.toString()) ?: UiText.DynamicString("Failed to to get the content")
                        ))
                    }
                    is Resource.Loading -> {
                        _eventFlow.emit(UiEvent.ShowSnackbar(
                            uiText = UiText.DynamicString("Trying to fetch...")
                        ))
                    }
                }
            }.launchIn(coroutineScope)

    }


    @OptIn(ExperimentalCoroutinesApi::class)
    fun fetchInfo() {
        coroutineScope.launch {
            when (fetcher) {
                FetchType.Detail -> {
                    try {
                        _eventFlow.emit(UiEvent.ShowSnackbar(
                            uiText = UiText.DynamicString("Trying to fetch...")
                        ))
                        val book = source.detailParse(Jsoup.parse(webView.getHtml()))
                        val chapters = source.chaptersParse(Jsoup.parse(webView.getHtml()))
                        if (!chapters.chapters.isNullOrEmpty()) {
                            _state.value =
                                state.value.copy(book = book.book)
                            getLocalBook(book.book)
                            getLocalChaptersByBookName(book.book.bookName)
                            if (state.value.chapters.isNotEmpty() && chapters.chapters.isNotEmpty()) {
                                val list = mutableListOf<Chapter>()
                                val sum = state.value.chapters + chapters.chapters

                                val uniqueList = sum.distinctBy {
                                    it.title
                                }.sortedBy {
                                    "s(\\d+)".toRegex()
                                        .matchEntire(it.title)?.groups?.get(1)?.value?.toInt()
                                }

                                list.addAll(uniqueList)
                                deleteChapterDetails()
                                insertChaptersToLocal(uniqueList)
                                _eventFlow.emit(UiEvent.ShowSnackbar(
                                    uiText = UiText.DynamicString("${book.book.bookName} was fetched with ${chapters.chapters.size}  chapters")
                                ))
                            }

                        } else {
                            _eventFlow.emit(UiEvent.ShowSnackbar(
                                uiText = UiText.DynamicString("Failed to to get the content")
                            ))
                        }
                    } catch (e: Exception) {
                        _eventFlow.emit(UiEvent.ShowSnackbar(
                            uiText = UiText.DynamicString("Failed to to get the content")
                        ))
                    }


                }
                else -> {}
            }
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

    private fun getLocalChaptersByBookName(bookName: String) {
        localChapterRepository.getChapterByName(bookName, source.name)
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