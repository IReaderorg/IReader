package ir.kazemcodes.infinity.presentation.components

import android.os.Build
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView


@Composable
fun WebPageScreen(urlToRender: String) {
    val webView = WebView(LocalContext.current)
    AndroidView(factory = {
        webView.apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            webViewClient = WebViewClient()
            loadUrl(urlToRender)
        }
    }, update = {
        it.loadUrl(urlToRender)
    }, modifier = Modifier.fillMaxSize())

}