package ir.kazemcodes.infinity.util

import android.webkit.WebView
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.suspendCancellableCoroutine
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

@ExperimentalCoroutinesApi
suspend fun WebView.getHtml(): String = suspendCancellableCoroutine { continuation ->
    if (!settings.javaScriptEnabled)
        throw IllegalStateException("Javascript is disabled")

    evaluateJavascript(
        "(function() {\n" +
                "    return (\n" +
                "        '<html>' +\n" +
                "        document.getElementsByTagName('html')[0].innerHTML +\n" +
                "        '</html>'\n" +
                "    );\n" +
                "})();"
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