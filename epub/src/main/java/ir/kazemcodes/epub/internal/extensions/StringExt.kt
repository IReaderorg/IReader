package ir.kazemcodes.epub.internal.extensions

internal fun String.orNullIfEmpty() = if (this.isBlank()) null else this