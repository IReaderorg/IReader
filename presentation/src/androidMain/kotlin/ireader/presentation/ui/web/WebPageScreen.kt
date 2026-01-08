package ireader.presentation.ui.web

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.os.Message
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import io.ktor.http.HttpHeaders
import ireader.core.http.setDefaultSettings
import ireader.core.source.HttpSource
import ireader.presentation.ui.web.WebViewState.LoadingState
import ireader.presentation.ui.web.WebViewState.WebContent
import ireader.presentation.ui.web.WebViewState.WebViewError
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.i18n.resources.*
import ireader.i18n.resources.Res

@OptIn( ExperimentalMaterial3Api::class)
@ExperimentalCoroutinesApi
@SuppressLint("SetJavaScriptEnabled")
@Composable
@Suppress("UNUSED_PARAMETER")
fun WebPageScreen(
    viewModel: WebViewPageModel,
    source: ireader.core.source.CatalogSource?,
    snackBarHostState: SnackbarHostState,
    scaffoldPadding: PaddingValues,
    modifier: Modifier = Modifier.fillMaxSize(),
    onTabCountChange: ((Int) -> Unit)? = null,
    onCloseTab: (() -> Unit)? = null
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    val coroutineScope = rememberCoroutineScope()
    
    val userAgent = remember {
        (source as? HttpSource)?.getCoverRequest("")?.second?.headers?.get(HttpHeaders.UserAgent)
    }
    
    val headers = remember(userAgent) {
        userAgent?.let { mapOf(HttpHeaders.UserAgent to it) } ?: emptyMap()
    }
    
    // Window stack for managing multiple WebView windows (tabs)
    // Note: WebView state is managed by the WebViewManager which handles config changes
    val windowStack = remember {
        mutableStateStackOf(
            WebViewWindow(
                WebContent.Url(viewModel.url, headers),
                WebViewNavigator(coroutineScope)
            )
        )
    }
    
    val currentWindow = windowStack.lastItemOrNull
    val webPageScreenState = remember { WebPageScreenState() }
    
    // Notify parent about tab count changes
    LaunchedEffect(windowStack.size) {
        onTabCountChange?.invoke(windowStack.size)
    }
    
    // Handle back press for window stack
    BackHandler(enabled = windowStack.size > 1) {
        val poppedWindow = windowStack.pop()
        // Clean up the popped window's WebView
        poppedWindow?.webView?.destroy()
    }
    
    // Setup effects and state management for current window
    currentWindow?.let { window ->
        WebPageScreenEffects(
            webViewState = window.state,
            viewModel = viewModel,
            screenState = webPageScreenState
        )
    }

    Box(modifier = modifier.padding(scaffoldPadding)) {
        when {
            webPageScreenState.showError -> {
                WebPageErrorContent(
                    errorMessage = webPageScreenState.errorMessage,
                    localizeHelper = localizeHelper,
                    onRetry = { 
                        webPageScreenState.showError = false
                        currentWindow?.webView?.reload()
                    }
                )
            }
            currentWindow != null -> {
                // Calculate the window index for URL tracking
                val windowIndex = windowStack.items.indexOf(currentWindow).coerceAtLeast(0)
                
                // Key the WebView to the current window to properly handle window switches
                key(currentWindow) {
                    WebPageMainContent(
                        window = currentWindow,
                        windowStack = windowStack,
                        viewModel = viewModel,
                        userAgent = userAgent,
                        headers = headers,
                        screenState = webPageScreenState,
                        localizeHelper = localizeHelper,
                        coroutineScope = coroutineScope,
                        windowIndex = windowIndex
                    )
                }
            }
        }
        
        // Progress indicator overlay
        WebPageProgressIndicator(
            visible = webPageScreenState.progressVisible,
            progress = webPageScreenState.progressValue
        )
    }
}

// State holder for WebPageScreen
@Stable
private class WebPageScreenState {
    var progressVisible by mutableStateOf(false)
    var progressValue by mutableFloatStateOf(0f)
    var webView by mutableStateOf<WebView?>(null)
    var canGoBack by mutableStateOf(false)
    var canGoForward by mutableStateOf(false)
    var showError by mutableStateOf(false)
    var errorMessage by mutableStateOf("")
}

@Composable
private fun WebPageScreenEffects(
    webViewState: WebViewState,
    viewModel: WebViewPageModel,
    screenState: WebPageScreenState
) {
    // Update viewModel with our state
    LaunchedEffect(webViewState) {
        viewModel.webViewState = webViewState
    }

    // Handle loading state updates
    LaunchedEffect(webViewState.loadingState) {
        when (val loadingState = webViewState.loadingState) {
            is WebViewState.LoadingState.Loading -> {
                screenState.progressVisible = true
                screenState.progressValue = loadingState.progress
                viewModel.toggleLoading(true)
                screenState.showError = false
            }
            is LoadingState.Finished -> {
                delay(300) // Brief delay before hiding progress
                screenState.progressVisible = false
                viewModel.toggleLoading(false)
                screenState.showError = false
            }
            is LoadingState.Error -> {
                screenState.progressVisible = false
                viewModel.toggleLoading(false)
                screenState.showError = true
                screenState.errorMessage = loadingState.error.description ?: "Unknown error occurred"
            }
            else -> {
                // Handle other states if needed
            }
        }
    }

    // Update URL in view model when WebView URL changes
    LaunchedEffect(webViewState.lastLoadedUrl) {
        webViewState.lastLoadedUrl?.let { url ->
            viewModel.updateWebUrl(url)
        }
    }
}

@Composable
private fun WebPageErrorContent(
    errorMessage: String,
    localizeHelper: ireader.i18n.LocalizeHelper,
    onRetry: () -> Unit
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Error,
            contentDescription = localizeHelper.localize(Res.string.download_notifier_title_error),
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.error
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = localizeHelper.localize(Res.string.failed_to_load_page),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = errorMessage,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(onClick = onRetry) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = localizeHelper.localize(Res.string.retry),
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.size(8.dp))
            Text(localizeHelper.localize(Res.string.retry))
        }
    }
}

@Composable
@Suppress("UNUSED_PARAMETER", "LongParameterList")
private fun WebPageMainContent(
    window: WebViewWindow,
    windowStack: MutableStateStack<WebViewWindow>,
    viewModel: WebViewPageModel,
    userAgent: String?,
    headers: Map<String, String>,
    screenState: WebPageScreenState,
    localizeHelper: ireader.i18n.LocalizeHelper,
    coroutineScope: kotlinx.coroutines.CoroutineScope,
    windowIndex: Int
) {
    AndroidView(
        factory = { ctx ->
            createOrReuseWebView(ctx, window, windowStack, viewModel, userAgent, headers, screenState, coroutineScope, windowIndex)
        },
        update = { view ->
            updateWebViewContent(view, window.state)
        },
        modifier = Modifier.fillMaxSize()
    )
    
    // Cleanup on dispose
    DisposableEffect(window) {
        onDispose {
            // Check if this window is still in the stack
            val windowStillExists = window in windowStack
            if (!windowStillExists) {
                // Window was removed from stack, safe to destroy WebView
                window.webView?.destroy()
                window.webView = null
            } else {
                // Window still exists, preserve state for when it becomes active again
                window.state.content = WebContent.NavigatorOnly
            }
        }
    }
}

@Composable
private fun WebPageProgressIndicator(
    visible: Boolean,
    progress: Float
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = Modifier.fillMaxWidth()
    ) {
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    }
}

@Suppress("UNUSED_PARAMETER", "LongParameterList")
private fun createOrReuseWebView(
    context: Context,
    window: WebViewWindow,
    windowStack: MutableStateStack<WebViewWindow>,
    viewModel: WebViewPageModel,
    userAgent: String?,
    headers: Map<String, String>,
    screenState: WebPageScreenState,
    coroutineScope: kotlinx.coroutines.CoroutineScope,
    windowIndex: Int
): WebView {
    // Reuse existing WebView if available, otherwise create new one
    val webViewInstance = window.webView ?: run {
        // Try to get from manager first, or create new
        val newWebView = try {
            viewModel.webViewManager.init() as WebView
        } catch (e: Exception) {
            WebView(context)
        }
        window.webView = newWebView
        newWebView
    }
    
    // IMPORTANT: Remove WebView from its parent if it has one
    // This prevents "The specified child already has a parent" error
    // when reusing WebViews across configuration changes
    (webViewInstance.parent as? android.view.ViewGroup)?.removeView(webViewInstance)
    
    screenState.webView = webViewInstance
    window.navigator.setWebView(webViewInstance)
    
    return webViewInstance.apply {
        configureWebViewSettings(userAgent)
        setupWebViewClientsWithWindowStack(window.state, screenState, viewModel, windowStack, headers, coroutineScope, windowIndex)
        
        // Handle popup message if this is a popup window
        window.popupMessage?.let { message ->
            initializePopupWebView(this, message)
        } ?: run {
            // Normal window - load URL if needed
            val content = window.state.content
            if (content is WebContent.Url && content.url.isNotEmpty() && url != content.url) {
                if (content.additionalHttpHeaders.isNotEmpty()) {
                    loadUrl(content.url, content.additionalHttpHeaders)
                } else {
                    loadUrl(content.url)
                }
            }
        }
    }
}

/**
 * Initialize a WebView for a popup window using WebViewTransport
 */
private fun initializePopupWebView(webView: WebView, message: Message) {
    val transport = message.obj as? WebView.WebViewTransport
    transport?.webView = webView
    message.sendToTarget()
}

private fun WebView.configureWebViewSettings(userAgent: String?) {
    // Set user agent if available
    userAgent?.let { ua ->
        settings.userAgentString = ua
    }
    
    // Apply default settings
    setDefaultSettings()
    
    // Additional performance settings
    settings.apply {
        javaScriptEnabled = true
        domStorageEnabled = true
        databaseEnabled = true
        setSupportZoom(true)
        builtInZoomControls = true
        displayZoomControls = false
        loadWithOverviewMode = true
        useWideViewPort = true
        setRenderPriority(android.webkit.WebSettings.RenderPriority.HIGH)
        
        // Enable support for multiple windows (new tabs)
        setSupportMultipleWindows(true)
        javaScriptCanOpenWindowsAutomatically = true
    }
    
    // Scrollbar and performance settings
    isVerticalScrollBarEnabled = true
    isHorizontalScrollBarEnabled = false
    setLayerType(WebView.LAYER_TYPE_HARDWARE, null)
}

private fun WebView.setupWebViewClientsWithWindowStack(
    webViewState: WebViewState,
    screenState: WebPageScreenState,
    viewModel: WebViewPageModel,
    windowStack: MutableStateStack<WebViewWindow>,
    headers: Map<String, String>,
    coroutineScope: kotlinx.coroutines.CoroutineScope,
    windowIndex: Int
) {
    webViewClient = createOptimizedWebViewClient(webViewState, screenState, viewModel, headers, windowIndex)
    webChromeClient = createWebChromeClientWithWindowSupport(webViewState, screenState, windowStack, coroutineScope, viewModel)
}

// Keep old method for backward compatibility
private fun WebView.setupWebViewClients(
    webViewState: WebViewState,
    screenState: WebPageScreenState,
    viewModel: WebViewPageModel
) {
    webViewClient = createOptimizedWebViewClient(webViewState, screenState, viewModel, emptyMap(), 0)
    webChromeClient = createOptimizedWebChromeClient(webViewState, screenState)
}

private fun createOptimizedWebViewClient(
    webViewState: WebViewState,
    screenState: WebPageScreenState,
    viewModel: WebViewPageModel,
    headers: Map<String, String>,
    @Suppress("UNUSED_PARAMETER") windowIndex: Int
): WebViewClient {
    return object : WebViewClient() {
        override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
            super.onPageStarted(view, url, favicon)
            webViewState.loadingState = LoadingState.Loading(0f)
            webViewState.lastLoadedUrl = url
            screenState.canGoBack = view.canGoBack()
            screenState.canGoForward = view.canGoForward()
        }
        
        override fun onPageFinished(view: WebView, url: String) {
            super.onPageFinished(view, url)
            webViewState.loadingState = LoadingState.Finished()
            webViewState.lastLoadedUrl = url
            webViewState.pageTitle = view.title.orEmpty()
            screenState.canGoBack = view.canGoBack()
            screenState.canGoForward = view.canGoForward()
            
            // Trigger auto-fetch if enabled
            if (viewModel.autoFetchEnabled) {
                viewModel.triggerAutoFetch(view)
            }
        }
        
        override fun shouldOverrideUrlLoading(
            view: WebView?,
            request: WebResourceRequest?
        ): Boolean {
            val url = request?.url?.toString() ?: return false
            
            // Ignore intent:// URLs (app deep links)
            if (url.startsWith("intent://")) {
                return true
            }
            
            // Handle web URLs
            if (url.startsWith("http://") || url.startsWith("https://")) {
                // Only intercept if it's a different URL to prevent loops
                if (url != view?.url) {
                    if (headers.isNotEmpty()) {
                        view?.loadUrl(url, headers)
                    } else {
                        view?.loadUrl(url)
                    }
                    return true
                }
            }
            
            return false
        }
        
        override fun shouldInterceptRequest(
            view: WebView,
            request: WebResourceRequest
        ): WebResourceResponse? {
            return WebViewResourceOptimizer.interceptRequest(
                view = view,
                request = request,
                blockImages = false
            )
        }
        
        override fun onReceivedError(
            view: WebView,
            request: WebResourceRequest,
            error: WebResourceError
        ) {
            super.onReceivedError(view, request, error)
            // Only handle main frame errors
            if (request.isForMainFrame) {
                val errorDescription = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    error.description?.toString()
                } else {
                    "Error"
                }
                val errorCode = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    error.errorCode
                } else {
                    0
                }
                webViewState.loadingState = LoadingState.Error(
                    WebViewError(errorCode, errorDescription)
                )
            }
        }
        
        override fun doUpdateVisitedHistory(view: WebView?, url: String?, isReload: Boolean) {
            super.doUpdateVisitedHistory(view, url, isReload)
            url?.let {
                webViewState.lastLoadedUrl = it
                viewModel.updateWebUrl(it)
            }
            screenState.canGoBack = view?.canGoBack() == true
            screenState.canGoForward = view?.canGoForward() == true
        }
    }
}

private fun createOptimizedWebChromeClient(
    webViewState: WebViewState,
    screenState: WebPageScreenState
): WebChromeClient {
    return object : WebChromeClient() {
        override fun onProgressChanged(view: WebView, newProgress: Int) {
            super.onProgressChanged(view, newProgress)
            screenState.progressValue = newProgress / 100f
            webViewState.loadingState = LoadingState.Loading(screenState.progressValue)
        }
    }
}

/**
 * Creates a WebChromeClient that supports opening new windows (tabs)
 */
private fun createWebChromeClientWithWindowSupport(
    webViewState: WebViewState,
    screenState: WebPageScreenState,
    windowStack: MutableStateStack<WebViewWindow>,
    coroutineScope: kotlinx.coroutines.CoroutineScope,
    @Suppress("UNUSED_PARAMETER") viewModel: WebViewPageModel
): WebChromeClient {
    return object : WebChromeClient() {
        override fun onProgressChanged(view: WebView, newProgress: Int) {
            super.onProgressChanged(view, newProgress)
            screenState.progressValue = newProgress / 100f
            webViewState.loadingState = LoadingState.Loading(screenState.progressValue)
        }
        
        override fun onCreateWindow(
            view: WebView,
            isDialog: Boolean,
            isUserGesture: Boolean,
            resultMsg: Message
        ): Boolean {
            // Only handle user-initiated gestures to prevent unwanted popups
            // This is the same behavior as Mihon and standard browsers
            if (isUserGesture) {
                // Create a new window and push it to the stack
                val newWindow = WebViewWindow(
                    popupMessage = resultMsg,
                    navigator = WebViewNavigator(coroutineScope)
                )
                windowStack.push(newWindow)
                return true
            }
            return false
        }
        
        override fun onCloseWindow(window: WebView?) {
            super.onCloseWindow(window)
            // Find and remove the window from the stack
            if (windowStack.size > 1) {
                windowStack.pop()?.webView?.destroy()
            }
        }
        
        override fun onReceivedTitle(view: WebView?, title: String?) {
            super.onReceivedTitle(view, title)
            title?.let {
                webViewState.pageTitle = it
            }
        }
    }
}

private fun updateWebViewContent(view: WebView, webViewState: WebViewState) {
    when (val content = webViewState.content) {
        is WebContent.Url -> {
            val url = content.url
            if (url.isNotEmpty() && url != view.url) {
                if (content.additionalHttpHeaders.isNotEmpty()) {
                    view.loadUrl(url, content.additionalHttpHeaders)
                } else {
                    view.loadUrl(url)
                }
            }
        }
        is WebContent.Data -> {
            view.loadDataWithBaseURL(
                content.baseUrl,
                content.data,
                content.mimeType,
                "utf-8",
                null
            )
        }
        is WebContent.NavigatorOnly -> {
            // Do nothing - content is loaded via WebViewTransport for popup windows
            // or the WebView is preserving its state
        }
    }
}
