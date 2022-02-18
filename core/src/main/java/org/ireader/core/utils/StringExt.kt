package org.ireader.core.utils

import org.jsoup.Jsoup

fun List<String>.formatBasedOnDot(): String {

    return this.joinToString { it.trim().formatHtmlText() }
}

fun List<String>.formatList(): String {
    return this.map { it.trim() }.joinToString("-").replace("\"", "").replace("[", "")
        .replace("]", "")
}


fun String.replaceImageFormat(condition: Boolean): String {
    return if (condition) {
        this.replace(".webp", "")
    } else {
        this
    }
}

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

fun String.applyPageFormat(page: Int): String {
    return this.replace("{page}", page.toString())
}

fun String.applySearchFormat(query: String, page: Int): String {
    return this.replace("{query}", query.toString()).replace("{page}", page.toString())
}

fun String.applyIdFormat(id: String, page: Int): String {
    return this.replace("{id}", id.toString()).replace("{page}", page.toString())
}

