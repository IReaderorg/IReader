package ir.kazemcodes.infinity.feature_settings.presentation.webview

import android.view.ViewGroup
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.viewinterop.AndroidView
import com.zhuinden.simplestackcomposeintegration.core.LocalBackstack
import com.zhuinden.simplestackcomposeintegration.services.rememberService
import ir.kazemcodes.infinity.core.data.network.utils.setDefaultSettings
import ir.kazemcodes.infinity.core.presentation.reusable_composable.MidTextComposable
import ir.kazemcodes.infinity.core.presentation.reusable_composable.TopAppBarActionButton
import ir.kazemcodes.infinity.core.presentation.reusable_composable.TopAppBarBackButton
import ir.kazemcodes.infinity.core.utils.UiEvent
import ir.kazemcodes.infinity.core.utils.asString
import ir.kazemcodes.infinity.feature_sources.sources.models.FetchType
import kotlinx.coroutines.flow.collectLatest


@Composable
fun WebPageScreen() {
    val backStack = LocalBackstack.current
    val viewModel = rememberService<WebViewPageModel>()
    val webView = viewModel.state.value.webView
    val urlToRender = viewModel.state.value.url
    val scaffoldState = rememberScaffoldState()
    val context = LocalContext.current
    LaunchedEffect(key1 = true) {
        viewModel.eventFlow.collectLatest { event ->
            when (event) {
                is UiEvent.ShowSnackbar -> {
                    scaffoldState.snackbarHostState.showSnackbar(
                        event.uiText.asString(context)
                    )
                }
            }
        }

    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { MidTextComposable(title = urlToRender, overflow = TextOverflow.Ellipsis,) },
                navigationIcon = {
                    TopAppBarBackButton(backStack = backStack)
                },
                actions = {
                    if (viewModel.state.value.fetcher == FetchType.Detail) {
                        TopAppBarActionButton(imageVector = Icons.Default.TrackChanges,
                            title = "Menu",
                            onClick = {
                                viewModel.getInfo()
                            })
                    }
                },
                backgroundColor = MaterialTheme.colors.background,

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
        AndroidView(factory = {
            webView.apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                setDefaultSettings()
            }

            webView
        }, update = {
            if (webView.originalUrl != urlToRender) {
                it.loadUrl(urlToRender)
            }
        }, modifier = Modifier.fillMaxSize())
    }


}

