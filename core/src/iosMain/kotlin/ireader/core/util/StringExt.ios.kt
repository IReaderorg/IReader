package ireader.core.util

actual fun String.Companion.fromCodePoints(vararg codePoints: Int): String {
    return codePoints.map { Char(it) }.joinToString("")
}
