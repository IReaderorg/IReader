package org.ireader.domain.use_cases.epub.epup_parser.internal.extensions

internal fun String.orNullIfEmpty() = if (this.isBlank()) null else this