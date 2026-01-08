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
import ireader.domain.utils.extensions.ioDispatcher
import ireader.i18n.LocalizeHelper
import ireader.i18n.UiText
import ireader.i18n.asString
import ireader.i18n.resources.Res
import ireader.i18n.resources.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

/**
 * Fetch button state for WebView operations
 */
sealed class FetchButtonState {
    /**
     * Button is enabled and ready for user interaction
     */
    object Enabled : FetchButtonState()
    
    /**
     * Fetch operation is in progress
     */
    data class Fetching(val type: FetchType) : FetchButtonState()
    
    /**
     * Fetch operation completed successfully
     */
    data class Success(val message: String) : FetchButtonState()
    
    /**
     * Fetch operation failed with an error
     */
    data class Error(val message: String) : FetchButtonState()
    
    /**
     * Type of fetch operation
     */
    enum class FetchType {
        BOOK,
        CHAPTER,
        CHAPTERS
    }
}

/**
 * Custom WebViewState class to replace the one from Accompanist
 */
class WebViewState(webContent: WebContent) {
    var content by mutableStateOf<WebContent>(webContent)
    var loadingState by mutableStateOf<LoadingState>(LoadingState.Initializing)
    var pageTitle by mutableStateOf<String>("")
    var lastLoadedUrl by mutableStateOf<String?>(null)
    var canGoBack by mutableStateOf(false)
    var canGoForward by mutableStateOf(false)
    
    // Secondary constructor for URL string (backward compatibility)
    constructor(url: String) : this(WebContent.Url(url))
    
    /**
     * Content type for WebView
     */
    sealed class WebContent {
        data class Url(
            val url: String,
            val additionalHttpHeaders: Map<String, String> = emptyMap()
        ) : WebContent()
        data class Data(
            val data: String,
            val baseUrl: String,
            val mimeType: String = "text/html"
        ) : WebContent()
        /**
         * Used for popup windows where content is loaded via WebViewTransport
         */
        object NavigatorOnly : WebContent()
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

/**
 * Navigator class to control WebView navigation
 */
class WebViewNavigator(private val scope: kotlinx.coroutines.CoroutineScope) {
    var canGoBack by mutableStateOf(false)
        internal set
    var canGoForward by mutableStateOf(false)
        internal set
    
    private var webView: WebView? = null
    
    fun setWebView(view: WebView?) {
        webView = view
        updateNavigationState()
    }
    
    fun navigateBack() {
        webView?.goBack()
    }
    
    fun navigateForward() {
        webView?.goForward()
    }
    
    fun reload() {
        webView?.reload()
    }
    
    fun stopLoading() {
        webView?.stopLoading()
    }
    
    fun loadUrl(url: String, headers: Map<String, String> = emptyMap()) {
        if (headers.isNotEmpty()) {
            webView?.loadUrl(url, headers)
        } else {
            webView?.loadUrl(url)
        }
    }
    
    internal fun updateNavigationState() {
        canGoBack = webView?.canGoBack() == true
        canGoForward = webView?.canGoForward() == true
    }
}

/**
 * Represents a single WebView window in the window stack.
 * Each window has its own state, navigator, and WebView instance.
 */
class WebViewWindow(
    webContent: WebViewState.WebContent,
    val navigator: WebViewNavigator
) {
    var state by mutableStateOf(WebViewState(webContent))
    var popupMessage: android.os.Message? = null
        private set
    var webView: WebView? = null
    
    /**
     * Secondary constructor for popup windows created via onCreateWindow
     */
    constructor(
        popupMessage: android.os.Message,
        navigator: WebViewNavigator
    ) : this(WebViewState.WebContent.NavigatorOnly, navigator) {
        this.popupMessage = popupMessage
    }
}

class WebViewPageModel(
    private val insertUseCases: ireader.domain.usecases.local.LocalInsertUseCases,
    private val getBookUseCases: ireader.domain.usecases.local.LocalGetBookUseCases,
    private val getChapterUseCase: ireader.domain.usecases.local.LocalGetChapterUseCase,
    private val extensions: CatalogStore,
    private val remoteUseCases: RemoteUseCases,
    private val param: Param,
    private val webpageImpl: WebViewPageStateImpl,
    val webViewManager : WebViewManger,
    val autoFetchDetector: AutoFetchDetector = DefaultAutoFetchDetector(),
    @Suppress("UNUSED_PARAMETER") localizeHelper: LocalizeHelper
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
        val decodedUrl = URLDecoder.decode(
            param.url,
            StandardCharsets.UTF_8.name()
        )
        
        // Ensure URL is absolute before using it in WebView
        val url = ensureAbsoluteUrl(decodedUrl, param.sourceId)
        
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
    
    /**
     * Ensure URL is absolute by prepending source baseUrl if needed.
     * This is critical for WebView which requires absolute URLs.
     */
    private fun ensureAbsoluteUrl(url: String, sourceId: Long?): String {
        // If already absolute, return as-is
        if (url.startsWith("http://") || url.startsWith("https://")) {
            return url
        }
        
        // Try to get baseUrl from source
        sourceId?.let { id ->
            val source = extensions.get(id)
            if (source is ireader.core.source.HttpSource) {
                val baseUrl = source.baseUrl
                if (baseUrl.isNotBlank() && (baseUrl.startsWith("http://") || baseUrl.startsWith("https://"))) {
                    // Construct absolute URL
                    return if (url.startsWith("/")) {
                        "$baseUrl$url"
                    } else {
                        "$baseUrl/$url"
                    }
                }
            }
        }
        
        // If we can't make it absolute, return as-is and log warning
        ireader.core.log.Log.warn("WebViewPageModel: Cannot convert relative URL to absolute: $url")
        return url
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
        fetchChapterState = FetchButtonState.Fetching(FetchButtonState.FetchType.CHAPTER)
        scope.launch {
            try {
                val pageSource = webView.getHtml()
                val url = webView.url.orEmpty()
                remoteUseCases.getRemoteReadingContent(
                    chapter,
                    catalog,
                    onError = {
                        fetchChapterState = FetchButtonState.Enabled // Reset to enabled on error
                        showSnackBar(it)
                        showSnackBar(UiText.MStringResource(Res.string.failed_to_get_content))
                    },
                    onSuccess = { result ->
                        if (result.content.isNotEmpty()) {
                            webChapter = result
                            insertChapter(result)
                            fetchChapterState = FetchButtonState.Enabled // Reset to enabled after success
                            showSnackBar(UiText.MStringResource(Res.string.download_notifier_download_finish))
                        } else {
                            // Use SmartContentExtractor as fallback when source parser fails
                            trySmartExtraction(chapter, pageSource, url)
                        }
                    },
                    commands = listOf(Command.Content.Fetch(url = url, pageSource))
                )
            } catch (e: Exception) {
                // Try smart extraction on error
                try {
                    val pageSource = webView.getHtml()
                    val url = webView.url.orEmpty()
                    trySmartExtraction(chapter, pageSource, url)
                } catch (fallbackError: Exception) {
                    fetchChapterState = FetchButtonState.Enabled // Reset to enabled on error
                    showSnackBar(UiText.MStringResource(ireader.i18n.resources.Res.string.error_failed_to_fetch_chapter, arrayOf(e.message ?: "Failed to fetch chapter")))
                }
            }
        }
    }
    
    @Suppress("UNUSED_PARAMETER")
    private fun trySmartExtraction(chapter: Chapter, pageSource: String, url: String) {
        try {
            val extractor = SmartContentExtractor()
            val result = extractor.extractContent(pageSource)
            
            if (result.content.isNotEmpty() && result.confidence > 0.5) {
                // Convert HTML string to List<Page>
                val contentPages = listOf(ireader.core.source.model.Text(result.content))
                val extractedChapter = chapter.copy(content = contentPages)
                webChapter = extractedChapter
                insertChapter(extractedChapter)
                fetchChapterState = FetchButtonState.Enabled // Reset to enabled
                showSnackBar(UiText.MStringResource(ireader.i18n.resources.Res.string.chapter_content_extracted, arrayOf(result.method)))
            } else {
                fetchChapterState = FetchButtonState.Enabled // Reset to enabled
                showSnackBar(UiText.MStringResource(ireader.i18n.resources.Res.string.no_reliable_content_found))
            }
        } catch (e: Exception) {
            fetchChapterState = FetchButtonState.Enabled // Reset to enabled
            showSnackBar(UiText.MStringResource(ireader.i18n.resources.Res.string.failed_to_extract_content, arrayOf(e.message ?: "Unknown error")))
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getChapters(
        book: Book,
        webView: WebView,
    ) {
        fetchChaptersState = FetchButtonState.Fetching(FetchButtonState.FetchType.CHAPTERS)
        scope.launch {
            try {
                val pageSource = webView.getHtml()
                val url = webView.url.orEmpty()
                val localChapters = getChapterUseCase.findChaptersByBookId(book.id)
                remoteUseCases.getRemoteChapters(
                    book,
                    catalog,
                    onError = {
                        fetchChaptersState = FetchButtonState.Enabled // Reset to enabled on error
                        showSnackBar(it)
                        showSnackBar(UiText.MStringResource(ireader.i18n.resources.Res.string.failed_to_fetch_chapters))
                    },
                    onSuccess = { result ->
                        webChapters = result
                        if (result.isNotEmpty()) {
                            insertChapters(result.map { it.copy(bookId = book.id) })
                            val message = if (result.size == 1) 
                                UiText.MStringResource(Res.string.download_notifier_download_finish) 
                            else 
                                UiText.DynamicString("${result.size} chapters have been downloaded")
                            fetchChaptersState = FetchButtonState.Enabled // Reset to enabled after success
                            showSnackBar(message)
                        } else {
                            fetchChaptersState = FetchButtonState.Enabled // Reset to enabled
                            showSnackBar(UiText.MStringResource(ireader.i18n.resources.Res.string.no_chapters_found))
                        }
                    },
                    commands = listOf(Command.Chapter.Fetch(url = url, pageSource)),
                    oldChapters = localChapters
                )
            } catch (e: Exception) {
                fetchChaptersState = FetchButtonState.Enabled // Reset to enabled on error
                showSnackBar(UiText.MStringResource(ireader.i18n.resources.Res.string.error_failed_to_fetch_chapter, arrayOf(e.message ?: "Failed to fetch chapters")))
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getDetails(
        webView: WebView,
        book: Book? = null,
    ) {
        fetchBookState = FetchButtonState.Fetching(FetchButtonState.FetchType.BOOK)
        scope.launch {
            try {
                val pageSource = webView.getHtml()
                val url = webView.originalUrl ?: webView.url.orEmpty()
                remoteUseCases.getBookDetail(
                    book ?: Book(key = url, title = "", sourceId = source?.id ?: 0),
                    catalog,
                    onError = {
                        fetchBookState = FetchButtonState.Enabled // Reset to enabled on error
                        showSnackBar(it)
                        showSnackBar(UiText.MStringResource(ireader.i18n.resources.Res.string.failed_to_fetch_book_details))
                    },
                    onSuccess = { result ->
                        if (result.title.isNotBlank()) {
                            webBook = result
                            insertBook(result.copy(favorite = true))
                            fetchBookState = FetchButtonState.Enabled // Reset to enabled after success
                            showSnackBar(UiText.MStringResource(ireader.i18n.resources.Res.string.book_added_to_library, arrayOf(result.title)))
                        } else {
                            fetchBookState = FetchButtonState.Enabled // Reset to enabled
                            showSnackBar(UiText.MStringResource(ireader.i18n.resources.Res.string.could_not_extract_book_info))
                        }
                    },
                    commands = listOf(Command.Detail.Fetch(url = url, pageSource))
                )
            } catch (e: Exception) {
                fetchBookState = FetchButtonState.Enabled // Reset to enabled on error
                showSnackBar(UiText.MStringResource(ireader.i18n.resources.Res.string.error_failed_to_fetch_chapter, arrayOf(e.message ?: "Failed to fetch book details")))
            }
        }
    }

    fun insertBookDetailToLocal(book: Book) {
        scope.launch(ioDispatcher) {
            insertUseCases.insertBook(book)
        }
    }

    fun insertBook(book: Book) {
        scope.launch(ioDispatcher) {
            insertUseCases.insertBook(book)
        }
    }

    fun insertChapter(chapter: Chapter) {
        scope.launch(ioDispatcher) {
            insertUseCases.insertChapter(chapter)
        }
    }

    fun insertChapters(chapter: List<Chapter>) {
        scope.launch(ioDispatcher) {
            insertUseCases.insertChapters(chapter)
        }
    }
    
    /**
     * Trigger automatic novel fetching based on page content detection
     */
    fun triggerAutoFetch(webView: WebView) {
        scope.launch {
            try {
                val result = autoFetchDetector.autoFetch(
                    url = webView.url.orEmpty(),
                    webView = webView,
                    source = source,
                    viewModel = this@WebViewPageModel
                )
                
                when (result) {
                    is FetchResult.Success -> {
                        // Success message is already shown by individual fetch methods
                    }
                    is FetchResult.Error -> {
                        showSnackBar(UiText.MStringResource(ireader.i18n.resources.Res.string.error_failed_to_fetch_chapter, arrayOf(result.message)))
                    }
                    FetchResult.Skipped -> {
                        // Do nothing, auto-fetch was skipped
                    }
                }
            } catch (e: Exception) {
                // Silently fail auto-fetch to not disrupt user experience
            }
        }
    }
    
    /**
     * Detect novel content on the current page
     */
    suspend fun detectNovelContent(webView: WebView): NovelDetectionResult {
        return autoFetchDetector.detectNovelContent(
            url = webView.url.orEmpty(),
            webView = webView,
            source = source
        )
    }
    
    // ═══════════════════════════════════════════════════════════════
    // Window Stack Management (for configuration change preservation)
    // ═══════════════════════════════════════════════════════════════
    
    /**
     * Add a window URL to the stack. Called when a new window is opened.
     */
    fun addWindowUrl(url: String, headers: Map<String, String> = emptyMap(), isPopup: Boolean = false) {
        windowUrls.add(WindowUrlInfo(url, headers, isPopup))
    }
    
    /**
     * Remove the last window URL from the stack. Called when a window is closed.
     */
    fun removeLastWindowUrl() {
        if (windowUrls.isNotEmpty()) {
            windowUrls.removeAt(windowUrls.lastIndex)
        }
    }
    
    /**
     * Update the URL of a window at a specific index. Called when navigation occurs.
     */
    fun updateWindowUrl(index: Int, url: String) {
        if (index in windowUrls.indices) {
            val current = windowUrls[index]
            windowUrls[index] = current.copy(url = url)
        }
    }
    
    /**
     * Initialize the window URLs list with the initial URL if empty.
     */
    fun initializeWindowUrls(initialUrl: String, headers: Map<String, String> = emptyMap()) {
        if (windowUrls.isEmpty()) {
            windowUrls.add(WindowUrlInfo(initialUrl, headers, isPopup = false))
        }
    }
    
    /**
     * Get the current window count.
     */
    fun getWindowCount(): Int = windowUrls.size
}

/**
 * Data class to hold window URL information for state preservation across configuration changes.
 * This is stored in the ViewModel to survive config changes.
 */
data class WindowUrlInfo(
    val url: String,
    val headers: Map<String, String> = emptyMap(),
    val isPopup: Boolean = false
)

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
    
    var fetchBookState: FetchButtonState
    var fetchChapterState: FetchButtonState
    var fetchChaptersState: FetchButtonState
    
    var autoFetchEnabled: Boolean
    
    /**
     * List of window URLs for preserving window stack across configuration changes.
     * The first item is the main window, subsequent items are popup windows.
     */
    var windowUrls: SnapshotStateList<WindowUrlInfo>
}

open class WebViewPageStateImpl : WebViewPageState {
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
    
    override var fetchBookState: FetchButtonState by mutableStateOf(FetchButtonState.Enabled)
    override var fetchChapterState: FetchButtonState by mutableStateOf(FetchButtonState.Enabled)
    override var fetchChaptersState: FetchButtonState by mutableStateOf(FetchButtonState.Enabled)
    
    override var autoFetchEnabled: Boolean by mutableStateOf(false)
    
    override var windowUrls: SnapshotStateList<WindowUrlInfo> = mutableStateListOf()
}
