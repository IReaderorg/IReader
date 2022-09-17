package ireader.domain.utils.extensions

import android.annotation.SuppressLint
import android.webkit.WebView
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.suspendCancellableCoroutine
import ireader.core.api.source.model.MangasPageInfo
import java.net.URI
import java.net.URISyntaxException

@SuppressLint("SetJavaScriptEnabled")
@ExperimentalCoroutinesApi
suspend fun WebView.getHtml(): String = suspendCancellableCoroutine { continuation ->
    settings.javaScriptEnabled = true
    if (!settings.javaScriptEnabled)
        throw IllegalStateException("Javascript is disabled")

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

fun getUrlWithoutDomain(orig: String): String {
    return try {
        val uri = URI(orig.replace(" ", "%20"))
        var out = uri.path
        if (uri.query != null) {
            out += "?" + uri.query
        }
        if (uri.fragment != null) {
            out += "#" + uri.fragment
        }
        out
    } catch (e: URISyntaxException) {
        orig
    }
}

fun String.getHtml(): String {
    return this.replace("\\<.*?>", "")
}

fun emptyMangaInfoPage(): MangasPageInfo {
    return MangasPageInfo(emptyList(), false)
}

const val DEFAULT_USER_AGENT =
    "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:86.0) Gecko/20100101 Firefox/86.0"

// @SuppressLint("SetJavaScriptEnabled")
// @ExperimentalCoroutinesApi
// suspend fun WebView.getHtml(): String = suspendCancellableCoroutine { continuation ->
//    settings.javaScriptEnabled = true
//    if (!settings.javaScriptEnabled)
//        throw IllegalStateException("Javascript is disabled")
//
//    evaluateJavascript(
//        "(function() { return ('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>'); })();"
//    ) {
//        continuation.resume(
//            it!!.replace("\\u003C", "<")
//                .replace("\\n", "")
//                .replace("\\t", "")
//                .replace("\\\"", "\"")
//                .replace("<hr />", "")
//        ) {
//
//        }
//    }
// }
//
// fun getUrlWithoutDomain(orig: String): String {
//    return try {
//        val uri = URI(orig.replace(" ", "%20"))
//        var out = uri.path
//        if (uri.query != null) {
//            out += "?" + uri.query
//        }
//        if (uri.fragment != null) {
//            out += "#" + uri.fragment
//        }
//        out
//    } catch (e: URISyntaxException) {
//        orig
//    }
// }
//
//
// fun String.getHtml(): String {
//    return this.replace("\\<.*?>", "")
// }
