package ireader.presentation.ui.web

import android.webkit.WebView
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
import com.google.accompanist.web.*
import io.ktor.http.*
import ireader.core.http.setDefaultSettings
import ireader.core.source.HttpSource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@ExperimentalCoroutinesApi
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
    val webViewState = rememberWebViewState(url = viewModel.url)
    var progressVisible by remember { mutableStateOf(false) }
    var progressValue by remember { mutableStateOf(0f) }
    val context = LocalContext.current

    LaunchedEffect(key1 = webViewState.hashCode()) {
        viewModel.webViewState = webViewState
    }

    LaunchedEffect(key1 = webViewState.content.getCurrentUrl()) {
        webViewState.content.getCurrentUrl()?.let { viewModel.webUrl = it }
    }
    
    // Handle loading state and progress updates
    LaunchedEffect(webViewState.isLoading, webViewState.loadingState) {
        viewModel.toggleLoading(webViewState.isLoading)
        
        when (val loadingState = webViewState.loadingState) {
            is LoadingState.Loading -> {
                progressVisible = true
                progressValue = loadingState.progress
            }
            is LoadingState.Finished -> {
                delay(300) // Brief delay before hiding progress
                progressVisible = false
            }
            else -> {
                // Handle other states if needed
            }
        }
    }
    
    val chromeClient = remember {
        object : AccompanistWebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
                progressValue = newProgress / 100f
            }
        }
    }
    
    val webclient = remember {
        object : AccompanistWebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                view?.let { webView ->
                    // Apply any JavaScript injection or page modifications here if needed
                }
            }
        }
    }
    
    val webNavigator = rememberWebViewNavigator()

    Box(
        modifier = modifier
            .padding(scaffoldPadding)
    ) {
        WebView(
            state = webViewState,
            onCreated = { webView ->
                userAgent?.let { ua -> webView.setUserAgent(ua) }
                webView.setDefaultSettings()
                
                // Enhanced web view settings
                webView.apply {
                    isVerticalScrollBarEnabled = true
                    isHorizontalScrollBarEnabled = false
                    settings.apply {
                        setSupportZoom(true)
                        builtInZoomControls = true
                        displayZoomControls = false
                        loadWithOverviewMode = true
                        useWideViewPort = true
                        domStorageEnabled = true
                        
                        // Improve performance
                        setRenderPriority(android.webkit.WebSettings.RenderPriority.HIGH)
                    }
                    
                    // Enable hardware acceleration
                    setLayerType(WebView.LAYER_TYPE_HARDWARE, null)
                }
            },
            chromeClient = chromeClient,
            client = webclient,
            factory = {
                viewModel.webViewManager.init() as WebView
            },
            navigator = webNavigator,
            modifier = Modifier.fillMaxSize(),
            captureBackPresses = false  // Let the app handle back presses
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
}
