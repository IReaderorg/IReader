package ireader.presentation.ui.web

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.google.accompanist.web.*
import io.ktor.http.*
import ireader.core.http.setDefaultSettings
import ireader.core.source.HttpSource
import ireader.presentation.ui.core.ui.SnackBarListener
import kotlinx.coroutines.ExperimentalCoroutinesApi

@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@ExperimentalCoroutinesApi
@Composable
fun WebPageScreen(
        viewModel: WebViewPageModel,
        source: ireader.core.source.CatalogSource?,
        snackBarHostState: SnackbarHostState,
        scaffoldPadding: PaddingValues
) {
    val userAgent = remember {
        (source as? HttpSource)?.getCoverRequest("")?.second?.headers?.get(HttpHeaders.UserAgent)
    }
    val webViewState = rememberWebViewState(url = viewModel.url)

    LaunchedEffect(key1 = webViewState.hashCode()) {
        viewModel.webViewState = webViewState
    }

    LaunchedEffect(key1 = webViewState.content.getCurrentUrl()) {
        webViewState.content.getCurrentUrl()?.let { viewModel.webUrl = it }
    }
    val chromeClient = AccompanistWebChromeClient()
    val webclient = AccompanistWebViewClient()
    val webNavigator = rememberWebViewNavigator()

    webViewState.content.getCurrentUrl()

    val refresh = viewModel.isLoading
    val refreshState = rememberPullRefreshState(viewModel.isLoading, onRefresh = {
        webNavigator.reload()
        viewModel.toggleLoading(true)
    })

    Box(modifier = Modifier
            .padding(scaffoldPadding)
            .pullRefresh(refreshState)) {

        WebView(
                state = webViewState,
                onCreated = {
                    userAgent?.let { ua -> it.setUserAgent(ua) }
                    it.setDefaultSettings()
                },
                chromeClient = chromeClient,
                client = webclient,
                factory = {
                    viewModel.webViewManager.init()
                },
                navigator = webNavigator

        )
        if (webViewState.isLoading) {
            androidx.compose.material3.LinearProgressIndicator(
                    modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.TopCenter),
            )
        }
        PullRefreshIndicator(refresh, refreshState, Modifier.align(Alignment.TopCenter))
    }
}
