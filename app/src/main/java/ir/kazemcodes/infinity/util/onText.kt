package ir.kazemcodes.infinity.util

fun List<String>.formatBasedOnDot(): String {
    return this.joinToString { it.trim() }.replace(".", ".\n")
}

fun List<String>.formatList(): String {
    return this.map { it.trim() }.joinToString("-").replace("\"", "").replace("[", "").replace("]", "")
}