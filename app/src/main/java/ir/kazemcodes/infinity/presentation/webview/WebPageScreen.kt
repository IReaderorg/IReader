package ir.kazemcodes.infinity.presentation.webview

import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.zhuinden.simplestackcomposeintegration.core.LocalBackstack
import ir.kazemcodes.infinity.data.network.utils.setDefaultSettings
import ir.kazemcodes.infinity.presentation.reusable_composable.TopAppBarBackButton
import org.kodein.di.compose.localDI
import org.kodein.di.instance


@Composable
fun WebPageScreen(urlToRender: String, title: String? = null) {
    val backStack = LocalBackstack.current
    val di = localDI().di
    val webView by di.instance<WebView>()
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
                    webViewClient = WebViewClient()
                    loadUrl(urlToRender)
                }


            }, update = {
            if (webView.url != urlToRender) {
                it.loadUrl(urlToRender)
            }
            }, modifier = Modifier.fillMaxSize())
    }


}