package ir.kazemcodes.infinity.feature_settings.presentation.webview

import android.view.ViewGroup
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.zhuinden.simplestackcomposeintegration.core.LocalBackstack
import com.zhuinden.simplestackcomposeintegration.services.rememberService
import ir.kazemcodes.infinity.core.data.network.utils.setDefaultSettings
import ir.kazemcodes.infinity.core.presentation.reusable_composable.MidTextComposable
import ir.kazemcodes.infinity.core.presentation.reusable_composable.TopAppBarActionButton
import ir.kazemcodes.infinity.core.presentation.reusable_composable.TopAppBarBackButton


@Composable
fun WebPageScreen() {
    val backStack = LocalBackstack.current
    val viewModel = rememberService<WebViewPageModel>()
    val webView = viewModel.state.value.webView
    val urlToRender = viewModel.state.value.url
    Scaffold(topBar = {
        TopAppBar(
            title = { MidTextComposable(title = urlToRender) },
            navigationIcon = {
                TopAppBarBackButton(backStack = backStack)
            },
            actions = {
                      TopAppBarActionButton(imageVector = Icons.Default.TrackChanges, title = "Menu", onClick = {
                          viewModel.fetchInfo()
                      })
            },
            backgroundColor = MaterialTheme.colors.background
        )
    }) {
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

