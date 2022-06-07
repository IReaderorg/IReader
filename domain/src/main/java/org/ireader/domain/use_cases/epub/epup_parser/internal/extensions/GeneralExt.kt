package org.ireader.domain.use_cases.epub.epup_parser.internal.extensions

internal fun <T> T.orValidationError(action: (() -> Unit)?): T {
    if (this == null) action?.invoke()
    return this
}
