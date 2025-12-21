package ireader.presentation.ui.web

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
    modifier: Modifier = Modifier.fillMaxSize()
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    val userAgent = remember {
        (source as? HttpSource)?.getCoverRequest("")?.second?.headers?.get(HttpHeaders.UserAgent)
    }
    
    val webViewState = remember { WebViewState(viewModel.url) }
    val webPageScreenState = remember { WebPageScreenState() }
    
    // Setup effects and state management
    WebPageScreenEffects(
        webViewState = webViewState,
        viewModel = viewModel,
        screenState = webPageScreenState
    )

    Box(modifier = modifier.padding(scaffoldPadding)) {
        when {
            webPageScreenState.showError -> {
                WebPageErrorContent(
                    errorMessage = webPageScreenState.errorMessage,
                    localizeHelper = localizeHelper,
                    onRetry = { 
                        webPageScreenState.showError = false
                        webPageScreenState.webView?.reload()
                    }
                )
            }
            else -> {
                WebPageMainContent(
                    webViewState = webViewState,
                    viewModel = viewModel,
                    userAgent = userAgent,
                    screenState = webPageScreenState,
                    localizeHelper = localizeHelper
                )
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
@Suppress("UNUSED_PARAMETER")
private fun WebPageMainContent(
    webViewState: WebViewState,
    viewModel: WebViewPageModel,
    userAgent: String?,
    screenState: WebPageScreenState,
    localizeHelper: ireader.i18n.LocalizeHelper
) {
    AndroidView(
        factory = { ctx ->
            createOptimizedWebView(ctx, viewModel, userAgent, webViewState, screenState)
        },
        update = { view ->
            updateWebViewContent(view, webViewState)
        },
        modifier = Modifier.fillMaxSize()
    )
    
    // Cleanup on dispose
    DisposableEffect(Unit) {
        onDispose {
            // Any cleanup needed for the WebView
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

@Suppress("UNUSED_PARAMETER")
private fun createOptimizedWebView(
    context: Context,
    viewModel: WebViewPageModel,
    userAgent: String?,
    webViewState: WebViewState,
    screenState: WebPageScreenState
): WebView {
    // Get existing WebView from manager or create a new one
    val webViewInstance = viewModel.webViewManager.init() as WebView
    screenState.webView = webViewInstance
    
    return webViewInstance.apply {
        configureWebViewSettings(userAgent)
        setupWebViewClients(webViewState, screenState, viewModel)
        loadUrl(viewModel.url)
    }
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
    }
    
    // Scrollbar and performance settings
    isVerticalScrollBarEnabled = true
    isHorizontalScrollBarEnabled = false
    setLayerType(WebView.LAYER_TYPE_HARDWARE, null)
}

private fun WebView.setupWebViewClients(
    webViewState: WebViewState,
    screenState: WebPageScreenState,
    viewModel: WebViewPageModel
) {
    webViewClient = createOptimizedWebViewClient(webViewState, screenState, viewModel)
    webChromeClient = createOptimizedWebChromeClient(webViewState, screenState)
}

private fun createOptimizedWebViewClient(
    webViewState: WebViewState,
    screenState: WebPageScreenState,
    viewModel: WebViewPageModel
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

private fun updateWebViewContent(view: WebView, webViewState: WebViewState) {
    when (val content = webViewState.content) {
        is WebContent.Url -> {
            val url = content.url
            if (url.isNotEmpty() && url != view.url) {
                view.loadUrl(url)
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
    }
}
