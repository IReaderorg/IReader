package org.ireader.web

import android.webkit.WebView
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.Checkbox
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.Surface
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.SwipeRefreshIndicator
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.google.accompanist.web.AccompanistWebChromeClient
import com.google.accompanist.web.LoadingState
import com.google.accompanist.web.WebView
import com.google.accompanist.web.rememberWebViewState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import org.ireader.common_resources.UiEvent
import org.ireader.common_resources.UiText
import org.ireader.components.reusable_composable.AppIconButton
import org.ireader.components.reusable_composable.BigSizeTextComposable
import org.ireader.components.reusable_composable.MidSizeTextComposable
import org.ireader.core.R
import org.ireader.core_api.source.CatalogSource
import org.ireader.domain.utils.setDefaultSettings

@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@ExperimentalCoroutinesApi
@Composable
fun WebPageScreen(
    modifier: Modifier = Modifier,
    viewModel: WebViewPageModel,
    onPopBackStack: () -> Unit,
    onModalBottomSheetShow: () -> Unit,
    onModalBottomSheetHide: () -> Unit,
    source: CatalogSource?,
    snackBarHostState: SnackbarHostState,
    scaffoldPadding: PaddingValues
) {

    val context = LocalContext.current
    var webView by remember {
        mutableStateOf<WebView?>(null)
    }
    val webUrl = remember {
        mutableStateOf(viewModel.webUrl)
    }

    LaunchedEffect(key1 = webView.hashCode()) {
        viewModel.webView = webView
    }
    val webViewState = rememberWebViewState(url = webUrl.value)

    LaunchedEffect(key1 = webViewState.hashCode()) {
        viewModel.webViewState = webViewState
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
                    snackBarHostState.showSnackbar(
                        event.uiText.asString(context)
                    )
                }
                else -> {}
            }
        }
    }



    val refreshState = rememberSwipeRefreshState(isRefreshing = viewModel.isLoading)
    Box(modifier = Modifier.padding(scaffoldPadding)) {

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
                    backgroundColor = MaterialTheme.colorScheme.background,
                    contentColor = MaterialTheme.colorScheme.primaryContainer,
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
        }
    }
}

@Composable
fun ScrollableAppBar(
    title: UiText,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable (() -> Unit)? = null,
    background: Color = MaterialTheme.colorScheme.primary,
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
            BigSizeTextComposable(text = title.asString(LocalContext.current))
        }
    }
}

@Composable
fun WebPageBottomLayout(
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
                MidSizeTextComposable(
                    text =stringResource(R.string.cancel),
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Button(onClick = {
                onConfirm()
            }, modifier = Modifier.width(92.dp), shape = RoundedCornerShape(4.dp)) {
                MidSizeTextComposable(
                    text = stringResource(R.string.apply),
                    color = MaterialTheme.colorScheme.onPrimary
                )
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
                        MidSizeTextComposable(text = stringResource(R.string.add_as_new))

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
                        contentDescription = stringResource(R.string.view_book),
                        onClick = {
                            onBook(item.id)
                        })
                }
            }
        }

    }
}