package org.ireader.core.utils

fun String.Companion.fromCodePoints(vararg codePoints: Int): String {
    var buffer = charArrayOf()
    for (codePoint in codePoints) {
        buffer += Character.toChars(codePoint)
    }
    return String(buffer)
}
