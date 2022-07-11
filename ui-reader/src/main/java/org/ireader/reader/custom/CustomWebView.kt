package org.ireader.reader.custom

import android.graphics.Bitmap
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.viewinterop.AndroidView
import com.google.accompanist.web.LoadingState
import com.google.accompanist.web.WebContent
import com.google.accompanist.web.WebViewError
import com.google.accompanist.web.WebViewNavigator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * A wrapper around the Android View WebView to provide a basic WebView composable.
 *
 * If you require more customisation you are most likely better rolling your own and using this
 * wrapper as an example.
 *
 * @param state The webview state holder where the Uri to load is defined.
 * @param captureBackPresses Set to true to have this Composable capture back presses and navigate
 * the WebView back.
 * @param navigator An optional navigator object that can be used to control the WebView's
 * navigation from outside the composable.
 * @param onCreated Called when the WebView is first created, this can be used to set additional
 * settings on the WebView. WebChromeClient and WebViewClient should not be set here as they will be
 * subsequently overwritten after this lambda is called.
 * @param client Provides access to WebViewClient via subclassing
 * @param chromeClient Provides access to WebChromeClient via subclassing
 * @sample com.google.accompanist.sample.webview.BasicWebViewSample
 */
@OptIn(ExperimentalCoroutinesApi::class)
@Composable
fun WebView(
    preconfigureWebView: WebView? = null,
    modifier: Modifier = Modifier,
    captureBackPresses: Boolean = true,
    navigator: org.ireader.reader.custom.WebViewNavigator = rememberWebViewNavigator(),
    onCreated: (WebView) -> Unit = {},
//    client: org.ireader.reader.custom.AccompanistWebViewClient = remember { org.ireader.reader.custom.AccompanistWebViewClient() },
//    chromeClient: org.ireader.reader.custom.AccompanistWebChromeClient = remember { org.ireader.reader.custom.AccompanistWebChromeClient() }
) {
    val scope = rememberCoroutineScope()
    var webView by remember { mutableStateOf<WebView?>(null) }

    BackHandler(captureBackPresses && navigator.canGoBack) {
        webView?.goBack()
    }

    LaunchedEffect(webView, navigator) {
        with(navigator) { webView?.handleNavigationEvents() }
    }

    val runningInPreview = LocalInspectionMode.current

    AndroidView(
        factory = { context ->
            (
                preconfigureWebView
                    ?: WebView(context)
                )
                .apply {
//                onCreated(this)
//                    this.setDefaultSettings()
//
//
//                layoutParams = ViewGroup.LayoutParams(
//                    ViewGroup.LayoutParams.MATCH_PARENT,
//                    ViewGroup.LayoutParams.MATCH_PARENT
//                )

//                webChromeClient = chromeClient
//                webViewClient = client
                }
                .also { webView = it }
        },
        modifier = modifier
    ) { view ->
        // AndroidViews are not supported by preview, bail early
        if (runningInPreview) return@AndroidView

//        when (val content = state.content) {
//            is WebContent.Url -> {
//                val url = content.url
//
//                if (url.isNotEmpty() && url != view.url) {
//                    view.loadUrl(url, content.additionalHttpHeaders.toMutableMap())
//                }
//            }
//            is WebContent.Data -> {
//                view.loadDataWithBaseURL(content.baseUrl, content.data, null, "utf-8", null)
//            }
//        }

        navigator.canGoBack = view.canGoBack()
        navigator.canGoForward = view.canGoForward()
    }
}

@Stable
class WebViewNavigator(private val coroutineScope: CoroutineScope) {

    private enum class NavigationEvent { BACK, FORWARD, RELOAD, STOP_LOADING }

    private val navigationEvents: MutableSharedFlow<NavigationEvent> = MutableSharedFlow()

    // Use Dispatchers.Main to ensure that the webview methods are called on UI thread
    internal suspend fun WebView.handleNavigationEvents(): Nothing = withContext(Dispatchers.Main) {
        navigationEvents.collect { event ->
            when (event) {
                NavigationEvent.BACK -> goBack()
                NavigationEvent.FORWARD -> goForward()
                NavigationEvent.RELOAD -> reload()
                NavigationEvent.STOP_LOADING -> stopLoading()
            }
        }
    }

    /**
     * True when the web view is able to navigate backwards, false otherwise.
     */
    var canGoBack: Boolean by mutableStateOf(false)
        internal set

    /**
     * True when the web view is able to navigate forwards, false otherwise.
     */
    var canGoForward: Boolean by mutableStateOf(false)
        internal set

    /**
     * Navigates the webview back to the previous page.
     */
    fun navigateBack() {
        coroutineScope.launch { navigationEvents.emit(NavigationEvent.BACK) }
    }

    /**
     * Navigates the webview forward after going back from a page.
     */
    fun navigateForward() {
        coroutineScope.launch { navigationEvents.emit(NavigationEvent.FORWARD) }
    }

    /**
     * Reloads the current page in the webview.
     */
    fun reload() {
        coroutineScope.launch { navigationEvents.emit(NavigationEvent.RELOAD) }
    }

    /**
     * Stops the current page load (if one is loading).
     */
    fun stopLoading() {
        coroutineScope.launch { navigationEvents.emit(NavigationEvent.STOP_LOADING) }
    }
}

/**
 * Creates and remembers a [WebViewNavigator] using the default [CoroutineScope] or a provided
 * override.
 */
@Composable
fun rememberWebViewNavigator(
    coroutineScope: CoroutineScope = rememberCoroutineScope()
) = remember(coroutineScope) { org.ireader.reader.custom.WebViewNavigator(coroutineScope) }

open class AccompanistWebViewClient : WebViewClient() {
    open lateinit var state: WebViewState
        internal set
    open lateinit var navigator: org.ireader.reader.custom.WebViewNavigator
        internal set

    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)
        state.loadingState = LoadingState.Loading(0.0f)
        state.errorsForCurrentRequest.clear()
        state.pageTitle = null
        state.pageIcon = null
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)
        state.loadingState = LoadingState.Finished
        navigator.canGoBack = view?.canGoBack() ?: false
        navigator.canGoForward = view?.canGoForward() ?: false
    }

    override fun doUpdateVisitedHistory(
        view: WebView?,
        url: String?,
        isReload: Boolean
    ) {
        super.doUpdateVisitedHistory(view, url, isReload)
        // WebView will often update the current url itself.
        // This happens in situations like redirects and navigating through
        // history. We capture this change and update our state holder url.
        // On older APIs (28 and lower), this method is called when loading
        // html data. We don't want to update the state in this case as that will
        // overwrite the html being loaded.
        if (url != null &&
            !url.startsWith("data:text/html") &&
            state.content.getCurrentUrl() != url
        ) {
            state.content = state.content.withUrl(url)
        }
    }

    override fun onReceivedError(
        view: WebView?,
        request: WebResourceRequest?,
        error: WebResourceError?
    ) {
        super.onReceivedError(view, request, error)

        if (error != null) {
            state.errorsForCurrentRequest.add(WebViewError(request, error))
        }
    }

    override fun shouldOverrideUrlLoading(
        view: WebView?,
        request: WebResourceRequest?
    ): Boolean {
        // Override all url loads to make the single source of truth
        // of the URL the state holder Url
        request?.let {
            state.content = state.content.withUrl(it.url.toString())
        }
        return true
    }
}

@Stable
class WebViewState(webContent: WebContent) {
    /**
     *  The content being loaded by the WebView
     */
    var content by mutableStateOf<WebContent>(webContent)

    var html by mutableStateOf("")

    /**
     * Whether the WebView is currently [LoadingState.Loading] data in its main frame (along with
     * progress) or the data loading has [LoadingState.Finished]. See [LoadingState]
     */
    var loadingState: LoadingState by mutableStateOf(LoadingState.Initializing)
        internal set

    /**
     * Whether the webview is currently loading data in its main frame
     */
    val isLoading: Boolean
        get() = loadingState !is LoadingState.Finished

    /**
     * The title received from the loaded content of the current page
     */
    var pageTitle: String? by mutableStateOf(null)
        internal set

    /**
     * the favicon received from the loaded content of the current page
     */
    var pageIcon: Bitmap? by mutableStateOf(null)
        internal set

    /**
     * A list for errors captured in the last load. Reset when a new page is loaded.
     * Errors could be from any resource (iframe, image, etc.), not just for the main page.
     * For more fine grained control use the OnError callback of the WebView.
     */
    val errorsForCurrentRequest = mutableStateListOf<WebViewError>()
}

internal fun WebContent.withUrl(url: String) = when (this) {
    is WebContent.Url -> copy(url = url)
    else -> WebContent.Url(url)
}

open class AccompanistWebChromeClient : WebChromeClient() {
    open lateinit var state: WebViewState
        internal set

    override fun onReceivedTitle(view: WebView?, title: String?) {
        super.onReceivedTitle(view, title)
        state.pageTitle = title
    }

    override fun onReceivedIcon(view: WebView?, icon: Bitmap?) {
        super.onReceivedIcon(view, icon)
        state.pageIcon = icon
    }

    override fun onProgressChanged(view: WebView?, newProgress: Int) {
        super.onProgressChanged(view, newProgress)
        if (state.loadingState is LoadingState.Finished) return
        state.loadingState = LoadingState.Loading(newProgress / 100.0f)
    }
}

@Composable
fun rememberWebViewState(url: String, additionalHttpHeaders: Map<String, String> = emptyMap()) =
// Rather than using .apply {} here we will recreate the state, this prevents
    // a recomposition loop when the webview updates the url itself.
    remember(url, additionalHttpHeaders) {
        WebViewState(
            WebContent.Url(
                url = url,
                additionalHttpHeaders = additionalHttpHeaders
            )
        )
    }
