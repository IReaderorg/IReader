package ir.kazemcodes.infinity.presentation.webview

import android.view.ViewGroup
import android.webkit.WebView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.zhuinden.simplestackcomposeintegration.core.LocalBackstack
import com.zhuinden.simplestackextensions.servicesktx.lookup
import ir.kazemcodes.infinity.data.network.utils.setDefaultSettings
import ir.kazemcodes.infinity.presentation.reusable_composable.TopAppBarBackButton


@Composable
fun WebPageScreen(urlToRender: String, title: String? = null) {
    val backStack = LocalBackstack.current
    val webView = remember {
        backStack.lookup<WebView>()
    }
    Scaffold(topBar = {
        TopAppBar(
            title = { Text(text = title ?: "") },
            navigationIcon = {
                TopAppBarBackButton(backStack = backStack)
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