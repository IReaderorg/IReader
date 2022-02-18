package org.ireader.presentation.feature_settings.presentation.webview

import android.webkit.WebView
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.web.rememberWebViewState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.ireader.core.utils.UiEvent
import org.ireader.core.utils.getHtml
import org.ireader.domain.models.source.HttpSource
import org.ireader.infinity.core.data.network.utils.setDefaultSettings
import org.ireader.presentation.presentation.reusable_composable.TopAppBarTitle


@ExperimentalCoroutinesApi
@Composable
fun WebPageScreen(
    modifier: Modifier = Modifier,
    navController: NavController = rememberNavController(),
    viewModel: WebViewPageModel = hiltViewModel(),
) {
    val urlToRender = viewModel.state.url
    val scaffoldState = rememberScaffoldState()
    val context = LocalContext.current
    val webView = remember {
        mutableStateOf(WebView(context))
    }
    val source = viewModel.state.source
    val scope = rememberCoroutineScope()

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
    val webViewState = rememberWebViewState(url = urlToRender)


    Scaffold(
        topBar = {
            WebPageTopBar(
                navController = navController,
                urlToRender = webViewState.content.getCurrentUrl() ?: urlToRender,
                onTrackButtonClick = {
                    if (source != null) {
                        scope.launch {
                            webView.value.setUserAgent(source.headers.get("User-Agent")
                                ?: HttpSource.DEFAULT_USER_AGENT)
                            viewModel.getInfo(pageSource = webView.value.getHtml(),
                                url = webViewState.content.getCurrentUrl() ?: "",
                                source = source)
                        }
                    }
                },
                fetchType = viewModel.state.fetcher,
                source = source,
                refresh = {
                    webView.value.reload()
                },
                goBack = {
                    webView.value.goBack()
                },
                goForward = {
                    webView.value.goForward()
                },
                fetchBook = {
                    if (source != null) {
                        scope.launch {
                            webView.value.setUserAgent(source.headers.get("User-Agent")
                                ?: HttpSource.DEFAULT_USER_AGENT)
                            viewModel.getInfo(webView.value.getHtml(),
                                url = webViewState.content.getCurrentUrl() ?: "",
                                source)
                        }
                    }
                },
                fetchBooks = {
                    //webView.goForward()
                },
                fetchChapter = {
                    if (source != null) {
                        scope.launch {
                            webView.value.setUserAgent(source.headers.get("User-Agent")
                                ?: HttpSource.DEFAULT_USER_AGENT)
                            viewModel.getFromWebView(webView.value.getHtml(),
                                url = webViewState.content.getCurrentUrl() ?: "",
                                source)
                        }
                    }
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

    ) {
        org.ireader.presentation.feature_settings.presentation.webview.WebView(
            modifier = Modifier.fillMaxSize(),
            newWebView = webView.value,
            state = webViewState,
            captureBackPresses = false,
            onCreated = {
                it.setDefaultSettings()
            }
        )
//        AndroidView(factory = {
//            webView.apply {
//                layoutParams = ViewGroup.LayoutParams(
//                    ViewGroup.LayoutParams.MATCH_PARENT,
//                    ViewGroup.LayoutParams.MATCH_PARENT
//                )
//                setDefaultSettings()
//                source?.headers?.get("User-Agent").let {
//                    webView.settings.userAgentString = it
//                }
//
//                webViewClient = object : WebViewClientCompat() {
//                    override fun shouldOverrideUrlCompat(view: WebView, url: String): Boolean {
//                        return false
//                    }
//                }
//            }
//
//            webView
//        }, update = {
//            it.loadUrl(urlToRender)
//        }, modifier = Modifier.fillMaxSize())
//    }


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
            TopAppBarTitle(title = title)
        }
    }
}