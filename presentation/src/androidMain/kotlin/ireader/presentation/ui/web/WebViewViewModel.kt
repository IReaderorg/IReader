package ireader.presentation.ui.web


import android.webkit.WebView
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import ireader.core.http.WebViewManger
import ireader.core.source.model.Command
import ireader.domain.catalogs.CatalogStore
import ireader.domain.models.entities.Book
import ireader.domain.models.entities.CatalogLocal
import ireader.domain.models.entities.Chapter
import ireader.domain.usecases.remote.RemoteUseCases
import ireader.i18n.UiText
import ireader.i18n.resources.MR
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

/**
 * Custom WebViewState class to replace the one from Accompanist
 */
class WebViewState(url: String) {
    var content by mutableStateOf<WebContent>(WebContent.Url(url))
    var loadingState by mutableStateOf<LoadingState>(LoadingState.Initializing)
    var pageTitle by mutableStateOf<String>("")
    var lastLoadedUrl by mutableStateOf<String?>(null)
    var canGoBack by mutableStateOf(false)
    var canGoForward by mutableStateOf(false)
    
    /**
     * Content type for WebView
     */
    sealed class WebContent {
        data class Url(val url: String) : WebContent()
        data class Data(val data: String, val baseUrl: String, val mimeType: String = "text/html") : WebContent()
    }
    
    /**
     * Loading state for WebView
     */
    sealed class LoadingState {
        object Initializing : LoadingState()
        data class Loading(val progress: Float) : LoadingState()
        data class Finished(val success: Boolean = true) : LoadingState()
        data class Error(val error: WebViewError) : LoadingState()
    }
    
    /**
     * Error information from WebView
     */
    data class WebViewError(
        val errorCode: Int,
        val description: String?
    )
}

class WebViewPageModel(
    private val insertUseCases: ireader.domain.usecases.local.LocalInsertUseCases,
    private val getBookUseCases: ireader.domain.usecases.local.LocalGetBookUseCases,
    private val getChapterUseCase: ireader.domain.usecases.local.LocalGetChapterUseCase,
    private val extensions: CatalogStore,
    private val remoteUseCases: RemoteUseCases,
    private val param: Param,
    private val webpageImpl: WebViewPageStateImpl,
    val webViewManager : WebViewManger
) : ireader.presentation.ui.core.viewmodel.BaseViewModel(), WebViewPageState by webpageImpl {
    data class Param(
        val url: String?,
        val bookId: Long?,
        val sourceId: Long?,
        val chapterId: Long?,
        val enableChapterFetch: Boolean?,
        val enableChaptersFetch: Boolean?,
        val enableBookFetch: Boolean?
    )

    init {
        val url = URLDecoder.decode(
            param.url,
            StandardCharsets.UTF_8.name()
        )
        val bookId = param.bookId
        val sourceId = param.sourceId
        val chapterId = param.chapterId
        enableChapterFetch = param.enableChapterFetch == true
        enableChaptersFetch = param.enableChaptersFetch == true
        enableBookFetch = param.enableBookFetch == true
        bookId?.let {
            scope.launch {
                stateBook = getBookUseCases.findBookById(bookId)
            }
        }
        sourceId?.let {
            scope.launch {
                extensions.get(sourceId)?.let {
                    catalog = it
                }
            }
        }
        chapterId?.let {
            scope.launch {
                stateChapter = getChapterUseCase.findChapterById(chapterId, null)
            }
        }
        updateUrl(url)
        updateWebUrl(url = url)
    }

    fun toggleLoading(loading: Boolean) {
        isLoading = loading
    }

    private fun updateUrl(url: String) {
        this.url = url
    }

    fun updateWebUrl(url: String) {
        webUrl = url
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getContentFromWebView(
            chapter: Chapter,
            webView: WebView,
    ) {
        val catalog = catalog
        scope.launch {
            val pageSource = webView.getHtml()
            val url = webView.url ?: ""
            remoteUseCases.getRemoteReadingContent(
                chapter,
                catalog,
                onError = {
                    showSnackBar(it)
                    showSnackBar(UiText.MStringResource(MR.strings.failed_to_get_content))
                },
                onSuccess = { result ->
                    if (result.content.isNotEmpty()) {
                        webChapter = result
                        insertChapter(result)
                        showSnackBar(UiText.MStringResource(MR.strings.download_notifier_download_finish))
                    } else {
                        showSnackBar(UiText.MStringResource(MR.strings.failed_to_get_content))
                    }
                },
                commands = listOf(Command.Content.Fetch(url = url, pageSource))
            )
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getChapters(
        book: Book,
        webView: WebView,
    ) {
        scope.launch {
            val pageSource = webView.getHtml()
            val url = webView.url ?: ""
            val localChapters = getChapterUseCase.findChaptersByBookId(book.id)
            remoteUseCases.getRemoteChapters(
                book,
                catalog,
                onError = {
                    showSnackBar(it)
                    showSnackBar(UiText.MStringResource(MR.strings.no_chapters_found_error))
                },
                onSuccess = { result ->
                    webChapters = result
                    if (result.isNotEmpty()) {
                        insertChapters(result.map { it.copy(bookId = book.id) })
                        val message = if (result.size == 1) 
                            UiText.MStringResource(MR.strings.download_notifier_download_finish) 
                        else 
                            UiText.DynamicString("${result.size} chapters have been downloaded")
                        showSnackBar(message)
                    } else {
                        showSnackBar(UiText.MStringResource(MR.strings.no_chapters_found_error))
                    }
                },
                commands = listOf(Command.Chapter.Fetch(url = url, pageSource)),
                oldChapters = localChapters
            )
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getDetails(
        webView: WebView,
        book: Book? = null,
    ) {
        scope.launch {
            val pageSource = webView.getHtml()
            val url = webView.originalUrl ?: ""
            remoteUseCases.getBookDetail(
                book ?: Book(key = "", title = "", sourceId = source?.id ?: 0),
                catalog,
                onError = {
                    showSnackBar(it)
                    showSnackBar(UiText.MStringResource(MR.strings.something_is_wrong_with_this_book))
                },
                onSuccess = { result ->
                    if (result.title.isNotBlank()) {
                        webBook = result
                        insertBook(result.copy(favorite = true))
                        showSnackBar(UiText.DynamicString("Book '${result.title}' added to library"))
                    } else {
                        showSnackBar(UiText.MStringResource(MR.strings.something_is_wrong_with_this_book))
                    }
                },
                commands = listOf(Command.Detail.Fetch(url = url, pageSource))
            )
        }
    }

    fun insertBookDetailToLocal(book: Book) {
        scope.launch(Dispatchers.IO) {
            insertUseCases.insertBook(book)
        }
    }

    fun insertBook(book: Book) {
        scope.launch(Dispatchers.IO) {
            insertUseCases.insertBook(book)
        }
    }

    fun insertChapter(chapter: Chapter) {
        scope.launch(Dispatchers.IO) {
            insertUseCases.insertChapter(chapter)
        }
    }

    fun insertChapters(chapter: List<Chapter>) {
        scope.launch(Dispatchers.IO) {
            insertUseCases.insertChapters(chapter)
        }
    }
}

interface WebViewPageState {
    var stateChapter: Chapter?
    var stateBook: Book?

    var webViewState: WebViewState?

    var url: String
    var webUrl: String
    var isLoading: Boolean

    val source: ireader.core.source.CatalogSource?
    var catalog: CatalogLocal?

    var bookId: Long?
    var bookTitle: String

    var availableBooks: SnapshotStateList<Book>
    val selectedBooks: SnapshotStateList<Long>
    val isAvailable: Boolean
    val isSelected: Boolean

    var webChapter: Chapter?
    var webBook: Book?
    var webChapters: List<Chapter>

    var enableChapterFetch: Boolean
    var enableChaptersFetch: Boolean
    var enableBookFetch: Boolean
}

open class WebViewPageStateImpl() : WebViewPageState {
    override var url: String by mutableStateOf("")
    override var webUrl: String by mutableStateOf("")

    override var webViewState: WebViewState? by mutableStateOf(null)

    override var isLoading: Boolean by mutableStateOf(false)

    override val source: ireader.core.source.CatalogSource? by derivedStateOf {
        val source = catalog?.source
        if (source is ireader.core.source.CatalogSource) source else null
    }
    override var catalog: CatalogLocal? by mutableStateOf(null)

    override var bookId: Long? by mutableStateOf(null)
    override var bookTitle: String by mutableStateOf("")
    override var stateChapter: Chapter? by mutableStateOf(null)
    override var stateBook: Book? by mutableStateOf(null)

    override var availableBooks: SnapshotStateList<Book> = mutableStateListOf()
    override var selectedBooks: SnapshotStateList<Long> = mutableStateListOf()
    override val isAvailable: Boolean by derivedStateOf { availableBooks.isNotEmpty() }
    override val isSelected: Boolean by derivedStateOf { selectedBooks.isNotEmpty() }

    override var webChapter: Chapter? by mutableStateOf(null)
    override var webBook: Book? by mutableStateOf(null)
    override var webChapters: List<Chapter> by mutableStateOf(emptyList())
    override var enableChapterFetch: Boolean by mutableStateOf(false)
    override var enableChaptersFetch: Boolean by mutableStateOf(false)
    override var enableBookFetch: Boolean by mutableStateOf(false)
}
