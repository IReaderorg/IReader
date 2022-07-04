package org.ireader.web

import android.annotation.SuppressLint
import android.webkit.WebView
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.suspendCancellableCoroutine

@SuppressLint("SetJavaScriptEnabled")
@ExperimentalCoroutinesApi
suspend fun WebView.getHtml(): String = suspendCancellableCoroutine { continuation ->
    settings.javaScriptEnabled = true
    if (!settings.javaScriptEnabled) {
        throw IllegalStateException("Javascript is disabled")
    }

    evaluateJavascript(
        "(function() { return ('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>'); })();"
    ) {
        continuation.resume(
            it!!.replace("\\u003C", "<")
                .replace("\\n", "")
                .replace("\\t", "")
                .replace("\\\"", "\"")
                .replace("<hr />", "")
        ) {
        }
    }
}