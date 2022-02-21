package org.ireader.extensions.sources.en.source_tower_deprecated

fun String.applyPageFormat(page: Int): String {
    return this.replace("{page}", page.toString())
}

fun String.applySearchFormat(query: String, page: Int): String {
    return this.replace("{query}", query.toString()).replace("{page}", page.toString())
}

fun String.applyIdFormat(id: String, page: Int): String {
    return this.replace("{id}", id.toString()).replace("{page}", page.toString())
}
