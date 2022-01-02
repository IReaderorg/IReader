package ir.kazemcodes.infinity.presentation.webview

import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView


@Composable
fun WebPageScreen(urlToRender: String) {
    val webView =
    AndroidView(factory = {
        WebView(it).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            settings.javaScriptEnabled
            webViewClient = WebViewClient()
            loadUrl(urlToRender)
        }


    }, update = {
        it.loadUrl(urlToRender)
    }, modifier = Modifier.fillMaxSize())

}