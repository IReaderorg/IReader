package ir.kazemcodes.epub.internal.extensions

internal fun <T> T.orValidationError(action: (() -> Unit)?): T {
    if (this == null) action?.invoke()
    return this
}
