package org.ireader.presentation.feature_settings.presentation.webview

import android.webkit.WebView
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.SwipeRefreshIndicator
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.google.accompanist.web.WebContent
import com.google.accompanist.web.rememberWebViewState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.ireader.core.ChapterParse
import org.ireader.core.ChaptersParse
import org.ireader.core.DetailParse
import org.ireader.core.utils.DEFAULT_USER_AGENT
import org.ireader.core.utils.UiEvent
import org.ireader.core.utils.getHtml
import org.ireader.domain.utils.setDefaultSettings
import org.ireader.presentation.presentation.reusable_composable.AppIconButton
import org.ireader.presentation.presentation.reusable_composable.BigSizeTextComposable
import org.ireader.presentation.presentation.reusable_composable.MidSizeTextComposable
import org.ireader.presentation.ui.BookDetailScreenSpec
import org.ireader.core_api.source.HttpSource


@OptIn(ExperimentalMaterialApi::class)
@ExperimentalCoroutinesApi
@Composable
fun WebPageScreen(
    modifier: Modifier = Modifier,
    navController: NavController = rememberNavController(),
    viewModel: WebViewPageModel = hiltViewModel(),
) {
    val scaffoldState = rememberScaffoldState()
    val context = LocalContext.current
    var webView by remember {
        mutableStateOf<WebView?>(null)
    }
    DisposableEffect(key1 = true) {
        onDispose {
            webView?.destroy()
        }
    }
    LaunchedEffect(key1 = true) {
        viewModel.eventFlow.collectLatest { event ->
            when (event) {
                is UiEvent.ShowSnackbar -> {
                    scaffoldState.snackbarHostState.showSnackbar(
                        event.uiText.asString(context)
                    )
                }
                else -> {}
            }
        }

    }
    val webUrl = remember {
        mutableStateOf(viewModel.webUrl)
    }


    val webViewState = rememberWebViewState(url = webUrl.value)


    val refreshState = rememberSwipeRefreshState(isRefreshing = viewModel.isLoading)


    Scaffold(
        topBar = {
            WebPageTopBar(
                navController = navController,
                urlToRender = viewModel.url,
                onGo = {
                    webViewState.content = WebContent.Url(viewModel.url)
                    // webView.value?.loadUrl(viewModel.state.url)
                    viewModel.updateWebUrl(viewModel.url)
                },
                refresh = {
                    webView?.reload()

                },
                goBack = {
                    webView?.goBack()
                },
                goForward = {
                    webView?.goForward()
                },
                onValueChange = {
                    viewModel.updateUrl(it)
                }
            )
        },
        scaffoldState = scaffoldState,
        snackbarHost = {
            SnackbarHost(it) { data ->
                Snackbar(
                    actionColor = MaterialTheme.colors.primary,
                    snackbarData = data,
                    backgroundColor = MaterialTheme.colors.background,
                    contentColor = MaterialTheme.colors.onBackground,
                )
            }
        }

    ) { padding ->
        SwipeRefresh(
            state = refreshState,
            onRefresh = {
                webView?.reload()
                viewModel.toggleLoading(true)
            },
            indicator = { state, trigger ->
                SwipeRefreshIndicator(
                    state = state,
                    refreshTriggerDistance = trigger,
                    scale = true,
                    backgroundColor = MaterialTheme.colors.background,
                    contentColor = MaterialTheme.colors.primaryVariant,
                    elevation = 8.dp
                )
            }
        ) {
            if (webViewState.isLoading) {
                LinearProgressIndicator(Modifier.fillMaxWidth())
            }

            WebView(
                modifier = Modifier.fillMaxSize(),
                state = webViewState,
                captureBackPresses = false,
                isLoading = {
                    viewModel.toggleLoading(it)
                },
                onCreated = {
                    webView = it
                    it.setDefaultSettings()
                },
                updateUrl = {
                    viewModel.updateUrl(it)
                },
            )
        }
    }


}


@Composable
fun ScrollableAppBar(
    title: String,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable (() -> Unit)? = null,
    background: Color = MaterialTheme.colors.primary,
    scrollUpState: Boolean,
) {
    val position by animateFloatAsState(if (scrollUpState) -150f else 0f)

    Surface(modifier = Modifier.graphicsLayer { translationY = (position) }, elevation = 8.dp) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(color = background),
        )
        Row(modifier = modifier.padding(start = 12.dp)) {
            if (navigationIcon != null) {
                navigationIcon()
            }
            BigSizeTextComposable(text = title)
        }
    }
}