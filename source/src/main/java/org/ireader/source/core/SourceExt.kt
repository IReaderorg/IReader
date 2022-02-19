package org.ireader.domain.utils

import android.annotation.SuppressLint
import android.webkit.WebView
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.suspendCancellableCoroutine
import org.ireader.core.utils.formatHtmlText
import org.ireader.source.sources.en.source_tower_deprecated.FetchType
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
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


fun mappingFetcherTypeWithIndex(index: Int): FetchType {
    return when (index) {
        FetchType.LatestFetchType.index -> FetchType.LatestFetchType
        FetchType.PopularFetchType.index -> FetchType.PopularFetchType
        FetchType.SearchFetchType.index -> FetchType.SearchFetchType
        FetchType.DetailFetchType.index -> FetchType.DetailFetchType
        FetchType.ChapterFetchType.index -> FetchType.ChapterFetchType
        FetchType.ContentFetchType.index -> FetchType.ContentFetchType
        else -> FetchType.LatestFetchType
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
    if (selector.isNullOrBlank() && !att.isNullOrBlank()) {
        return document.attr(att)
    } else if (!selector.isNullOrBlank() && att.isNullOrBlank()) {
        return document.select(selector).text()
    } else if (!selector.isNullOrBlank() && !att.isNullOrBlank()) {
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
    if (selector.isNullOrBlank() && !att.isNullOrBlank()) {
        return element.attr(att)
    } else if (!selector.isNullOrBlank() && att.isNullOrBlank()) {
        return element.select(selector).text().formatHtmlText()
    } else if (!selector.isNullOrBlank() && !att.isNullOrBlank()) {
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
    if (selector.isNullOrBlank() && !att.isNullOrBlank()) {
        return listOf(element.attr(att))
    } else if (!selector.isNullOrBlank() && att.isNullOrBlank()) {
        return element.select(selector).eachText()
    } else if (!selector.isNullOrBlank() && !att.isNullOrBlank()) {
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
    if (selector.isNullOrBlank() && !att.isNullOrBlank()) {
        return listOf(document.attr(att))
    } else if (!selector.isNullOrBlank() && att.isNullOrBlank()) {
        return document.select(selector).map {
            it.html().formatHtmlText()
        }
    } else if (!selector.isNullOrBlank() && !att.isNullOrBlank()) {
        return listOf(document.select(selector).attr(att))
    } else {
        return emptyList()
    }
}