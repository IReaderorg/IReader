package ireader.presentation.ui.reader.custom

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
import ireader.presentation.ui.reader.custom.WebViewNavigator
import ireader.presentation.ui.web.WebViewState
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
        modifier: Modifier = Modifier,
        preconfigureWebView: WebView? = null,
        captureBackPresses: Boolean = true,
        navigator: WebViewNavigator = rememberWebViewNavigator(),
        onCreate: (WebView) -> Unit = {},
//    client: ireader.ui.reader.custom.AccompanistWebViewClient = remember { ireader.ui.reader.custom.AccompanistWebViewClient() },
//    chromeClient: ireader.ui.reader.custom.AccompanistWebChromeClient = remember { ireader.ui.reader.custom.AccompanistWebChromeClient() }
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
                ireader.presentation.ui.reader.custom.WebViewNavigator.NavigationEvent.BACK -> goBack()
                ireader.presentation.ui.reader.custom.WebViewNavigator.NavigationEvent.FORWARD -> goForward()
                ireader.presentation.ui.reader.custom.WebViewNavigator.NavigationEvent.RELOAD -> reload()
                ireader.presentation.ui.reader.custom.WebViewNavigator.NavigationEvent.STOP_LOADING -> stopLoading()
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
) = remember(coroutineScope) { WebViewNavigator(coroutineScope) }


