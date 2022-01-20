package ir.kazemcodes.infinity.core.utils

import android.annotation.SuppressLint
import android.webkit.WebView
import ir.kazemcodes.infinity.core.data.network.models.Source
import ir.kazemcodes.infinity.feature_sources.sources.Extensions
import ir.kazemcodes.infinity.feature_sources.sources.models.FetchType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.suspendCancellableCoroutine
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import uy.kohesive.injekt.injectLazy
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


    fun mappingSourceNameToSource(apiName: String): Source {
        val extensions: Extensions by injectLazy()
        val sources = extensions.getSources()
        var source = extensions.getSources()[0]
        sources.forEach { apiItem ->
            if (apiItem.name == apiName) {
                source = apiItem
            }
        }
        return source
    }
fun mappingFetcherTypeWithIndex(index: Int): FetchType {
    return when(index) {
        FetchType.Latest.index -> FetchType.Latest
        FetchType.Popular.index -> FetchType.Popular
        FetchType.Search.index -> FetchType.Search
        FetchType.Detail.index -> FetchType.Detail
        FetchType.Chapter.index -> FetchType.Chapter
        FetchType.Content.index -> FetchType.Content
        else -> FetchType.Latest
    }
}


fun String.getHtml(): String {
    return this.replace("\\<.*?>", "")
}

fun selectorReturnerStringType(
    document: Document,
    selector: String? = null,
    att: String? = null,
): String {
    if (selector.isNullOrEmpty() && !att.isNullOrEmpty()) {
        return document.attr(att)
    } else if (!selector.isNullOrEmpty() && att.isNullOrEmpty()) {
        return document.select(selector).text()
    } else if (!selector.isNullOrEmpty() && !att.isNullOrEmpty()) {
        return document.select(selector).attr(att)
    } else {
        return ""
    }
}

fun selectorReturnerStringType(
    element: Element,
    selector: String? = null,
    att: String? = null,
): String {
    if (selector.isNullOrEmpty() && !att.isNullOrEmpty()) {
        return element.attr(att)
    } else if (!selector.isNullOrEmpty() && att.isNullOrEmpty()) {
        return element.select(selector).text().formatHtmlText()
    } else if (!selector.isNullOrEmpty() && !att.isNullOrEmpty()) {
        return element.select(selector).attr(att)
    } else {
        return ""
    }
}

fun selectorReturnerListType(
    element: Element,
    selector: String? = null,
    att: String? = null,
): List<String> {
    if (selector.isNullOrEmpty() && !att.isNullOrEmpty()) {
        return listOf(element.attr(att))
    } else if (!selector.isNullOrEmpty() && att.isNullOrEmpty()) {
        return element.select(selector).eachText()
    } else if (!selector.isNullOrEmpty() && !att.isNullOrEmpty()) {
        return listOf(element.select(selector).attr(att))
    } else {
        return emptyList()
    }
}

fun selectorReturnerListType(
    document: Document,
    selector: String? = null,
    att: String? = null,
): List<String> {
    if (selector.isNullOrEmpty() && !att.isNullOrEmpty()) {
        return listOf(document.attr(att))
    } else if (!selector.isNullOrEmpty() && att.isNullOrEmpty()) {
        return document.select(selector).map {
            it.html().formatHtmlText()
        }
    } else if (!selector.isNullOrEmpty() && !att.isNullOrEmpty()) {
        return listOf(document.select(selector).attr(att))
    } else {
        return emptyList()
    }
}