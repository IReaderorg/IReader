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
import org.ireader.domain.FetchType
import org.ireader.domain.utils.setDefaultSettings
import org.ireader.presentation.presentation.reusable_composable.AppIconButton
import org.ireader.presentation.presentation.reusable_composable.BigSizeTextComposable
import org.ireader.presentation.presentation.reusable_composable.MidSizeTextComposable
import org.ireader.presentation.ui.BookDetailScreenSpec
import tachiyomi.source.HttpSource


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
        mutableStateOf<WebView?>(viewModel.webView)
    }
    val bottomSheetState =
        rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)

    val source = viewModel.source
    val scope = rememberCoroutineScope()

//    DisposableEffect(key1 = true) {
//        onDispose {
//            webView?.destroy()
//        }
//    }
    LaunchedEffect(key1 = true) {
        viewModel.uiFLow.collectLatest { event ->
            when (event) {
                is WebPageEvents.ShowModalSheet -> {
                    scope.launch {
                        bottomSheetState.show()
                    }
                }
                is WebPageEvents.Cancel -> {
                    scope.launch {
                        bottomSheetState.hide()

                    }
                }
                is WebPageEvents.OnConfirm -> {
                    scope.launch {
                        bottomSheetState.hide()

                    }
                }
                else -> {}
            }
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

    LaunchedEffect(key1 = viewModel.url) {
        source?.let {
            val listing = source.getListings().map { it.name }
            if (listing.contains(DetailParse().name) || listing.contains(ChaptersParse().name) || listing.contains(
                    ChapterParse().name)
            ) {
                viewModel.getBooksByKey(url = viewModel.url)
            }
        }
    }
    val webViewState = rememberWebViewState(url = webUrl.value)


    val refreshState = rememberSwipeRefreshState(isRefreshing = viewModel.isLoading)


    ModalBottomSheetLayout(
        modifier = Modifier.statusBarsPadding(),
        sheetState = bottomSheetState,
        sheetContent = {
            Box(modifier.defaultMinSize(minHeight = 1.dp)) {
                WebPageBottomLayout(
                    onConfirm = {
                        scope.launch {
                            bottomSheetState.hide()
                            viewModel.onEvent(WebPageEvents.OnConfirm(
                                webView?.getHtml()
                                    ?: "",
                                url = webViewState.content.getCurrentUrl() ?: "",
                            ))
                        }
                    },
                    onCancel = {
                        scope.launch {
                            bottomSheetState.hide()
                        }
                        viewModel.onEvent(WebPageEvents.Cancel)
                    },
                    state = viewModel,
                    onBook = { id ->
                        try {
                            val book = viewModel.availableBooks.find { it.id == id }
                            book?.let {
                                navController.navigate(
                                    BookDetailScreenSpec.buildRoute(
                                        book.sourceId,
                                        bookId = book.id
                                    )
                                )
                            }

                        } catch (e: Exception) {
                        }
                    }
                )
            }
        },
        sheetBackgroundColor = MaterialTheme.colors.background,

        ) {
        Scaffold(
            topBar = {
                WebPageTopBar(
                    navController = navController,
                    urlToRender = viewModel.url,
                    fetchType = viewModel.fetcher,
                    source = source,
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
                    fetchBook = {
                        if (source != null) {
                            scope.launch {
                                try {
                                    val userAgent: String? = when (source) {
                                        is HttpSource -> {
                                            source.getCoverRequest("").second.headers.get("User-Agent")

                                        }
                                        else -> null
                                    }
                                    webView?.setUserAgent(userAgent ?: DEFAULT_USER_AGENT)
                                } catch (e: Exception) {
                                }

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
                    fetchChapters = {
                        if (source != null) {
                            scope.launch {
                                try {
                                    val userAgent: String? = when (source) {
                                        is HttpSource -> {
                                            source.getCoverRequest("").second.headers.get("User-Agent")

                                        }
                                        else -> null
                                    }
                                    webView?.setUserAgent(userAgent ?: DEFAULT_USER_AGENT)
                                } catch (e: Exception) {
                                }
                                viewModel.getChapters(pageSource = webView?.getHtml()
                                    ?: "",
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


@Composable
private fun WebPageBottomLayout(
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    onBook: (Long) -> Unit,
    state: WebViewPageState,
) {
    Column(modifier = Modifier
        .fillMaxSize()
        .padding(8.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween) {
            TextButton(onClick = {
                onCancel()
            }, modifier = Modifier.width(92.dp), shape = RoundedCornerShape(4.dp)) {
                MidSizeTextComposable(text = "Cancel", color = MaterialTheme.colors.primary)
            }
            Button(onClick = {
                onConfirm()
            }, modifier = Modifier.width(92.dp), shape = RoundedCornerShape(4.dp)) {
                MidSizeTextComposable(text = "Apply", color = MaterialTheme.colors.onPrimary)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {
            if (state.fetcher is FetchType.DetailFetchType) {
                item {
                    Row(modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start) {
                        Checkbox(checked = -1 in state.selectedBooks,
                            onCheckedChange = {
                                if (it) {
                                    state.selectedBooks.add(-1)
                                } else {
                                    state.selectedBooks.remove(-1)
                                }
                            })
                        MidSizeTextComposable(text = "New")

                    }
                }
            }

            items(state.availableBooks.size) { index ->
                val item = state.availableBooks[index]
                Row(modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween) {
                    Row(modifier = Modifier,
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start) {
                        Checkbox(checked = item.id in state.selectedBooks,
                            onCheckedChange = {
                                if (it) {
                                    state.selectedBooks.add(item.id)
                                } else {
                                    state.selectedBooks.remove(item.id)
                                }
                            })
                        MidSizeTextComposable(text = item.title)
                    }
                    AppIconButton(imageVector = Icons.Default.ArrowForward,
                        title = "View Book",
                        onClick = {
                            onBook(item.id)
                        })
                }
            }
        }

    }
}