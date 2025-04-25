package ireader.presentation.ui.web

import android.graphics.Bitmap
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView


//@Composable
//fun WebView(
//    state: WebViewState,
//    modifier: Modifier = Modifier,
//    captureBackPresses: Boolean = true,
//    onCreated: (WebView) -> Unit = {},
//    onError: (request: WebResourceRequest?, error: WebResourceError?) -> Unit = { _, _ -> },
//    isLoading: (isLoading: Boolean) -> Unit,
//    updateUrl: (url: String) -> Unit,
//) {
//    var webView by remember { mutableStateOf<WebView?>(null) }
//    var canGoBack: Boolean by remember { mutableStateOf(false) }
//
//    BackHandler(captureBackPresses && canGoBack) {
//        webView?.goBack()
//    }
//
//    AndroidView(
//        factory = { context ->
//            WebView(context).apply {
//                onCreated(this)
//
//                webViewClient = object : WebViewClient() {
//                    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
//                        super.onPageStarted(view, url, favicon)
//                        isLoading(true)
//                        if (url != null) {
//                            updateUrl(url)
//                        }
//                        state.errorsForCurrentRequest.clear()
//                    }
//
//                    override fun onPageFinished(view: WebView?, url: String?) {
//                        super.onPageFinished(view, url)
//                        isLoading(false)
//                        if (url != null) {
//                            updateUrl(url)
//                        }
//                        canGoBack = view?.canGoBack() ?: false
//                    }
//
//                    override fun doUpdateVisitedHistory(
//                        view: WebView?,
//                        url: String?,
//                        isReload: Boolean,
//                    ) {
//                        super.doUpdateVisitedHistory(view, url, isReload)
//                        // WebView will often update the current url itself.
//                        // This happens in situations like redirects and navigating through
//                        // history. We capture this change and update our state holder url.
//                        // On older APIs (28 and lower), this method is called when loading
//                        // html data. We don't want to update the state in this case as that will
//                        // overwrite the html being loaded.
//                        if (url != null &&
//                            !url.startsWith("data:text/html") &&
//                            state.content.getCurrentUrl() != url
//                        ) {
//                            state.content = WebContent.Url(url)
//                        }
//                    }
//
//                    override fun onReceivedError(
//                        view: WebView?,
//                        request: WebResourceRequest?,
//                        error: WebResourceError?,
//                    ) {
//                        super.onReceivedError(view, request, error)
//                        isLoading(false)
//                        url?.let { updateUrl(it) }
//                        if (error != null) {
//                            state.errorsForCurrentRequest.add(WebViewError(request, error))
//                        }
//
//                        onError(request, error)
//                    }
//
//                    override fun shouldOverrideUrlLoading(
//                        view: WebView?,
//                        request: WebResourceRequest?,
//                    ): Boolean {
//                        // Override all url loads to make the single source of truth
//                        // of the URL the state holder Url
//                        request?.let {
//                            val content = WebContent.Url(it.url.toString())
//                            state.content = content
//                        }
//                        return true
//                    }
//                }
//            }.also { webView = it }
//        },
//        modifier = modifier
//    ) { view ->
//        when (val content = state.content) {
//            is WebViewState.WebContent.Url -> {
//                val url = content.url
//
//                if (url.isNotEmpty() && url != view.url) {
//                    view.loadUrl(url)
//                }
//            }
//            is WebViewState.WebContent.Data -> {
//                view.loadDataWithBaseURL(content.baseUrl, content.data, null, "utf-8", null)
//            }
//        }
//
//        canGoBack = view.canGoBack()
//    }
//}

//class WebViewState(webContent: WebContent) {
//    /**
//     *  The content being loaded by the WebView
//     */
//    var content by mutableStateOf<WebContent>(webContent)
//
//    /**
//     * Whether the WebView is currently loading data in its main frame
//     */
//    var isLoading: Boolean by mutableStateOf(false)
//        internal set
//
//    /**
//     * A list for errors captured in the last load. Reset when a new page is loaded.
//     * Errors could be from any resource (iframe, image, etc.), not just for the main page.
//     * For more fine grained control use the OnError callback of the WebView.
//     */
//    val errorsForCurrentRequest = mutableStateListOf<WebViewError>()
//}
