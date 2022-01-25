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
import ir.kazemcodes.infinity.core.domain.use_cases.fetchers.FetchUseCase
import ir.kazemcodes.infinity.core.domain.use_cases.local.DeleteUseCase
import ir.kazemcodes.infinity.core.domain.use_cases.local.LocalGetBookUseCases
import ir.kazemcodes.infinity.core.domain.use_cases.local.LocalGetChapterUseCase
import ir.kazemcodes.infinity.core.domain.use_cases.local.LocalInsertUseCases
import ir.kazemcodes.infinity.core.utils.*
import ir.kazemcodes.infinity.feature_sources.sources.models.FetchType
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class WebViewPageModel(
    private val url: String,
    private val bookId: Int? = null,
    private val source: Source,
    private val chapterId: Int? = null,
    private val fetcher: FetchType,
    private val webView: WebView,
    private val insetUseCases: LocalInsertUseCases,
    private val deleteUseCase: DeleteUseCase,
    private val getBookUseCases: LocalGetBookUseCases,
    private val getChapterUseCase: LocalGetChapterUseCase,
    private val fetcherUseCase: FetchUseCase,
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
        if (bookId != null) {
            getLocalChaptersByBookName(bookId)
            getBookById(bookId = bookId)
        }
        if (bookId != null && chapterId != null) {
            getLocalChapterByName(chapterId)
        }
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
                source = source,
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
                            uiText = UiText.DynamicString(result.uiText?:UiText.unknownError().asString()).asString()
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
            if (bookId != null) {
                deleteUseCase.deleteChaptersByBookId(bookId)
            }
        }
    }

    override fun onServiceUnregistered() {
        coroutineScope.cancel()
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