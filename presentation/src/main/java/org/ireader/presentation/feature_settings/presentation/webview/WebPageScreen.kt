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
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.SwipeRefreshIndicator
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.google.accompanist.web.WebContent
import com.google.accompanist.web.rememberWebViewState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.ireader.core.utils.DEFAULT_USER_AGENT
import org.ireader.core.utils.UiEvent
import org.ireader.core.utils.getHtml
import org.ireader.domain.utils.setDefaultSettings
import org.ireader.presentation.presentation.reusable_composable.MidSizeTextComposable
import org.ireader.presentation.presentation.reusable_composable.TopAppBarTitle
import org.ireader.presentation.ui.BookDetailScreenSpec
import tachiyomi.source.HttpSource


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
    var isDialogShown by remember {
        mutableStateOf(false)
    }

    var dialogTitle by remember {
        mutableStateOf("")
    }

    val source = viewModel.state.source
    val scope = rememberCoroutineScope()

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
                is WebPageEvents.ShowDialog -> {
                    dialogTitle = event.title
                    isDialogShown = true
                }
                is WebPageEvents.OnDismiss -> {
                    isDialogShown = false
                }
                is WebPageEvents.OnConfirm -> {
                    isDialogShown = false
                }
                is WebPageEvents.GoTo -> {
                    try {
                        navController.navigate(BookDetailScreenSpec.buildRoute(bookId = event.bookId,
                            sourceId = event.sourceId))
                    } catch (e: Exception) {

                    }
                }
                else -> {}
            }
        }

    }
    val webUrl = remember {
        mutableStateOf(viewModel.state.webUrl)
    }
    val webViewState = rememberWebViewState(url = webUrl.value)


    val refreshState = rememberSwipeRefreshState(isRefreshing = viewModel.state.isLoading)


    Scaffold(
        topBar = {
            WebPageTopBar(
                navController = navController,
                urlToRender = viewModel.state.url,
                fetchType = viewModel.state.fetcher,
                source = source,
                onGo = {
                    webViewState.content = WebContent.Url(viewModel.state.url)
                    // webView.value?.loadUrl(viewModel.state.url)
                    viewModel.updateWebUrl(viewModel.state.url)
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
                fetchBook = {
                    if (source != null) {
                        scope.launch {
                            val userAgent: String? = when (source) {
                                is HttpSource -> {
                                    source.getCoverRequest("").second.headers["User-Agent"]

                                }
                                else -> null
                            }
                            webView?.setUserAgent(userAgent ?: DEFAULT_USER_AGENT)
                            viewModel.getBookDetailAndChapter(pageSource = webView?.getHtml()
                                ?: "",
                                url = webViewState.content.getCurrentUrl() ?: "",
                                source)
                        }
                    }
                },
                fetchChapter = {
                    if (source != null) {
                        scope.launch {
                            viewModel.getContentFromWebView(pageSource = webView?.getHtml()
                                ?: "",
                                url = webViewState.content.getCurrentUrl() ?: "",
                                source)
                        }
                    }
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

    ) {
        ShowAlert(isDialogShown,
            dialogTitle,
            onConfirm = {
                scope.launch {
                    viewModel.onEvent(WebPageEvents.OnConfirm(
                        webView?.getHtml()
                            ?: "",
                        url = webViewState.content.getCurrentUrl() ?: "",
                    ))
                }
                isDialogShown = false
            },
            onDismiss = {
                viewModel.onEvent(WebPageEvents.OnDismiss)
                isDialogShown = false
            },
            onUpdate = {
                scope.launch {
                    viewModel.onEvent(WebPageEvents.OnUpdate(
                        webView?.getHtml()
                            ?: "",
                        url = webViewState.content.getCurrentUrl() ?: "",

                        ))
                }
                isDialogShown = false
            },
            showUpdate = isDialogShown
        )
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
            TopAppBarTitle(title = title)
        }
    }
}

@Composable
fun ShowAlert(
    isShown: Boolean = false,
    title: String = "",
    content: String = "",
    onConfirm: () -> Unit = {},
    onDismiss: () -> Unit = {},
    onUpdate: () -> Unit = {},
    showUpdate: Boolean = false,
) {
    if (isShown) {
        AlertDialog(
            onDismissRequest = { onDismiss() },
            title = { MidSizeTextComposable(text = title) },
            text = {
                MidSizeTextComposable(text = content)
            },
            contentColor = MaterialTheme.colors.onBackground,
            backgroundColor = MaterialTheme.colors.background,
            buttons = {
                Row(horizontalArrangement = Arrangement.SpaceAround,
                    modifier = Modifier.fillMaxWidth()) {
                    if (showUpdate) {
                        OutlinedButton(onClick = { onUpdate() },
                            colors = ButtonDefaults.textButtonColors(
                                backgroundColor = MaterialTheme.colors.background,
                                contentColor = MaterialTheme.colors.onBackground
                            )) {
                            MidSizeTextComposable(text = "Update")
                        }
                    }
                    OutlinedButton(onClick = { onDismiss() },
                        colors = ButtonDefaults.textButtonColors(
                            backgroundColor = MaterialTheme.colors.background,
                            contentColor = MaterialTheme.colors.onBackground
                        )) {
                        MidSizeTextComposable(text = "DISMISS")
                    }
                    Button(onClick = { onConfirm() }, colors = ButtonDefaults.textButtonColors(
                        backgroundColor = MaterialTheme.colors.primary,
                        contentColor = MaterialTheme.colors.background
                    )) {
                        MidSizeTextComposable(text = "CHECK IT OUT")
                    }
                }

            },
        )
    }
}
