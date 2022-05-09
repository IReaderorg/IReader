package org.ireader.web

import android.webkit.WebView
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.Checkbox
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.Scaffold
import androidx.compose.material.Snackbar
import androidx.compose.material.SnackbarHost
import androidx.compose.material.Surface
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.rememberScaffoldState
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.SwipeRefreshIndicator
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.google.accompanist.web.AccompanistWebChromeClient
import com.google.accompanist.web.LoadingState
import com.google.accompanist.web.WebContent
import com.google.accompanist.web.WebView
import com.google.accompanist.web.rememberWebViewState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import org.ireader.common_resources.UiEvent
import org.ireader.components.reusable_composable.AppIconButton
import org.ireader.components.reusable_composable.BigSizeTextComposable
import org.ireader.components.reusable_composable.MidSizeTextComposable
import org.ireader.core_api.source.CatalogSource
import org.ireader.domain.utils.setDefaultSettings

@OptIn(ExperimentalMaterialApi::class)
@ExperimentalCoroutinesApi
@Composable
fun WebPageScreen(
    modifier: Modifier = Modifier,
    viewModel: WebViewPageModel,
    onPopBackStack: () -> Unit,
    onModalBottomSheetShow: () -> Unit,
    onModalBottomSheetHide: () -> Unit,
    modalBottomSheetState: ModalBottomSheetState,
    onBookNavigation: (Long) -> Unit,
    onModalSheetConfirm: (WebView) -> Unit,
    source: CatalogSource?,
    onFetchBook: (WebView) -> Unit,
    onFetchChapter: (WebView) -> Unit,
    onFetchChapters: (WebView) -> Unit,
) {
    val scaffoldState = rememberScaffoldState()
    val context = LocalContext.current
    var webView by remember {
        mutableStateOf<WebView?>(null)
    }
    val accompanistState = AccompanistWebChromeClient()

    DisposableEffect(key1 = true) {
        onDispose {
            webView?.destroy()
        }
    }


    LaunchedEffect(key1 = true) {
        viewModel.uiFLow.collectLatest { event ->
            when (event) {
                is WebPageEvents.ShowModalSheet -> {
                    onModalBottomSheetShow()
                }
                is WebPageEvents.Cancel -> {
                    onModalBottomSheetHide()
                }
                is WebPageEvents.OnConfirm -> {
                    onModalBottomSheetHide()
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

    val webViewState = rememberWebViewState(url = webUrl.value)

    LaunchedEffect(key1 = webViewState.loadingState ) {
        if (webViewState.loadingState == LoadingState.Finished) {

            viewModel.updateCookies(webView?.url?:"")
        }
    }

    val refreshState = rememberSwipeRefreshState(isRefreshing = viewModel.isLoading)
    ModalBottomSheetLayout(
        modifier = Modifier.statusBarsPadding(),
        sheetState = modalBottomSheetState,
        sheetContent = {
            Box(modifier.defaultMinSize(minHeight = 1.dp)) {
                WebPageBottomLayout(
                    onConfirm = {
                        onModalBottomSheetHide()
                        webView?.let { onModalSheetConfirm(it) }
                    },
                    onCancel = {
                        onModalBottomSheetHide()
                        viewModel.onEvent(WebPageEvents.Cancel)
                    },
                    state = viewModel,
                    onBook = { id ->
                        onBookNavigation(id)
                    }
                )
            }
        },
        sheetBackgroundColor = MaterialTheme.colors.background,

        ) {
        Scaffold(
            topBar = {
                WebPageTopBar(
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
                    },
                    onPopBackStack = onPopBackStack,
                    source = source,
                    onFetchBook = {
                        webView?.let { onFetchBook(it) }
                    },
                    onFetchChapter = {
                        webView?.let { onFetchChapter(it) }
                    },
                    onFetchChapters = {
                        webView?.let { onFetchChapters(it) }
                    },
                    state = viewModel
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
            Box(modifier = Modifier.padding(padding)) {

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
                        LinearProgressIndicator(
                            Modifier
                                .fillMaxWidth(),
                        )
                    }

                    WebView(
                        state = webViewState,
                        onCreated = {
                            webView = it
                            it.setDefaultSettings()
                        },
                        chromeClient = accompanistState
                    )
//                    WebView(
//                        modifier = Modifier.fillMaxSize(),
//                        state = webViewState,
//                        captureBackPresses = false,
//                        isLoading = {
//                            viewModel.toggleLoading(it)
//                        },
//                        onCreated = {
//                            webView = it
//                            it.setDefaultSettings()
//                        },
//                        updateUrl = {
//                            viewModel.updateUrl(it)
//                        },
//                    )
                }
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
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
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
            if (state.stateBook != null) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start
                    ) {
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier,
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start
                    ) {
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