package org.ireader.extensions.sources.en.source_tower_deprecated

fun List<String>.formatBasedOnDot(): String {

    return this.joinToString { it.trim().formatHtmlText() }
}
