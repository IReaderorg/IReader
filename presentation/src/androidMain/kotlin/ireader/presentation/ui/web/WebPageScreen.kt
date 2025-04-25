package ireader.presentation.ui.web

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import io.ktor.http.*
import ireader.core.http.setDefaultSettings
import ireader.core.source.HttpSource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import ireader.presentation.ui.web.WebViewState.LoadingState
import ireader.presentation.ui.web.WebViewState.WebContent
import ireader.presentation.ui.web.WebViewState.WebViewError

@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@ExperimentalCoroutinesApi
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebPageScreen(
    viewModel: WebViewPageModel,
    source: ireader.core.source.CatalogSource?,
    snackBarHostState: SnackbarHostState,
    scaffoldPadding: PaddingValues,
    modifier: Modifier = Modifier.fillMaxSize()
) {
    val userAgent = remember {
        (source as? HttpSource)?.getCoverRequest("")?.second?.headers?.get(HttpHeaders.UserAgent)
    }
    
    // Create a custom WebViewState
    val webViewState = remember { WebViewState(viewModel.url) }
    var progressVisible by remember { mutableStateOf(false) }
    var progressValue by remember { mutableStateOf(0f) }
    var webView by remember { mutableStateOf<WebView?>(null) }
    val context = LocalContext.current
    var canGoBack by remember { mutableStateOf(false) }
    var canGoForward by remember { mutableStateOf(false) }

    // Update viewModel with our state
    LaunchedEffect(webViewState) {
        viewModel.webViewState = webViewState
    }

    // Handle loading state updates
    LaunchedEffect(webViewState.loadingState) {
        when (val loadingState = webViewState.loadingState) {
            is WebViewState.LoadingState.Loading -> {
                progressVisible = true
                progressValue = loadingState.progress
                viewModel.toggleLoading(true)
            }
            is LoadingState.Finished -> {
                delay(300) // Brief delay before hiding progress
                progressVisible = false
                viewModel.toggleLoading(false)
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

    Box(
        modifier = modifier
            .padding(scaffoldPadding)
    ) {
        // WebView implementation using AndroidView
        AndroidView(
            factory = { ctx ->
                // Get existing WebView from manager or create a new one
                val webViewInstance = viewModel.webViewManager.init() as WebView
                webView = webViewInstance
                
                // Configure WebView settings
                webViewInstance.apply {
                    // Set user agent if available
                    userAgent?.let { ua ->
                        settings.userAgentString = ua
                    }
                    
                    // Apply default settings
                    setDefaultSettings()
                    
                    // Additional settings
                    settings.apply {
                        javaScriptEnabled = true
                        domStorageEnabled = true
                        databaseEnabled = true
                        setSupportZoom(true)
                        builtInZoomControls = true
                        displayZoomControls = false
                        loadWithOverviewMode = true
                        useWideViewPort = true
                        
                        // Performance improvements
                        setRenderPriority(android.webkit.WebSettings.RenderPriority.HIGH)
                    }
                    
                    // Scrollbar settings
                    isVerticalScrollBarEnabled = true
                    isHorizontalScrollBarEnabled = false
                    
                    // Set hardware acceleration
                    setLayerType(WebView.LAYER_TYPE_HARDWARE, null)
                    
                    // WebViewClient implementation
                    webViewClient = object : WebViewClient() {
                        override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
                            super.onPageStarted(view, url, favicon)
                            // Update state
                            webViewState.loadingState = LoadingState.Loading(0f)
                            webViewState.lastLoadedUrl = url
                            canGoBack = view.canGoBack()
                            canGoForward = view.canGoForward()
                        }
                        
                        override fun onPageFinished(view: WebView, url: String) {
                            super.onPageFinished(view, url)
                            // Update state
                            webViewState.loadingState = LoadingState.Finished()
                            webViewState.lastLoadedUrl = url
                            webViewState.pageTitle = view.title ?: ""
                            canGoBack = view.canGoBack()
                            canGoForward = view.canGoForward()
                            
                            // Apply any JavaScript injection or page modifications here if needed
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
                    
                    // WebChromeClient for progress reporting
                    webChromeClient = object : WebChromeClient() {
                        override fun onProgressChanged(view: WebView, newProgress: Int) {
                            super.onProgressChanged(view, newProgress)
                            progressValue = newProgress / 100f
                            webViewState.loadingState = LoadingState.Loading(progressValue)
                        }
                    }
                    
                    // Load initial URL
                    loadUrl(viewModel.url)
                }
                
                webViewInstance
            },
            update = { view ->
                // Update WebView state if needed
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
            },
            modifier = Modifier.fillMaxSize()
        )
        
        // Animated progress indicator based on actual loading progress
        AnimatedVisibility(
            visible = progressVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            LinearProgressIndicator(
                progress = { progressValue },
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            )
        }
    }
    
    // Cleanup on dispose
    DisposableEffect(Unit) {
        onDispose {
            // Any cleanup needed for the WebView
        }
    }
}
