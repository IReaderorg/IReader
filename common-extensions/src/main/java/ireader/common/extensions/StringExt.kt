package ireader.common.extensions

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
