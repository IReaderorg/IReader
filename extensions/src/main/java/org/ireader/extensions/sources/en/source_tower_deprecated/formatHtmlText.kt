package org.ireader.extensions.sources.en.source_tower_deprecated

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element

fun String.formatHtmlText(): String {
    val formated_text = this
        .replace("\\\\<.*?\\\\>", "")
        .replace("<em>", "")
        .replace("</em>", "")
        .replace("<b>", "")
        .replace("</b>", "")
        .replace("<strong>", "")
        .replace("</strong>", "")
        .replace("</i>", "")
        .replace("</class=\"_hr\">", "")
        .replace("<p>", "")
        .replace("<p/>", "")
        .replace("<br>\n<br>", "\n")
        .replace("<br>", "\n")
        .replace("\\u003C", "<")
        .replace("\\n", "")
        .replace("\\t", "")
        .replace("\\\"", "\"")
        .replace("<hr />", "")

    return Jsoup.parse(this).wholeText()
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