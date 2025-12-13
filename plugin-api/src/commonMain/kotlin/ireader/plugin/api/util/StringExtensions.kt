package ireader.plugin.api.util

/**
 * String utility extensions for plugin development.
 */

/**
 * URL encode a string.
 */
fun String.urlEncode(): String {
    return this
        .replace(" ", "%20")
        .replace("&", "%26")
        .replace("=", "%3D")
        .replace("?", "%3F")
        .replace("/", "%2F")
        .replace("#", "%23")
        .replace("+", "%2B")
}

/**
 * URL decode a string.
 */
fun String.urlDecode(): String {
    return this
        .replace("%20", " ")
        .replace("%26", "&")
        .replace("%3D", "=")
        .replace("%3F", "?")
        .replace("%2F", "/")
        .replace("%23", "#")
        .replace("%2B", "+")
}

/**
 * Remove HTML tags from a string.
 */
fun String.stripHtml(): String {
    return this.replace(Regex("<[^>]*>"), "")
}

/**
 * Truncate string to specified length with ellipsis.
 */
fun String.truncate(maxLength: Int, ellipsis: String = "..."): String {
    return if (this.length <= maxLength) {
        this
    } else {
        this.take(maxLength - ellipsis.length) + ellipsis
    }
}

/**
 * Check if string is a valid URL.
 */
fun String.isValidUrl(): Boolean {
    return this.startsWith("http://") || this.startsWith("https://")
}

/**
 * Extract domain from URL.
 */
fun String.extractDomain(): String? {
    return try {
        val withoutProtocol = this.removePrefix("https://").removePrefix("http://")
        withoutProtocol.substringBefore("/").substringBefore("?")
    } catch (e: Exception) {
        null
    }
}
