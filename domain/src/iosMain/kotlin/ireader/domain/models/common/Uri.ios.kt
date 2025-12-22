package ireader.domain.models.common

/**
 * iOS implementation of Uri using NSURL
 */
actual class Uri private constructor(private val urlString: String) {
    
    /**
     * Get the path from the URI string.
     * For file:// URLs, extracts the path portion.
     * For other strings, returns the string as-is.
     */
    val path: String get() {
        return when {
            urlString.startsWith("file://") -> urlString.removePrefix("file://")
            urlString.startsWith("/") -> urlString
            else -> urlString
        }
    }
    
    actual override fun toString(): String = urlString
    
    actual companion object {
        actual fun parse(uriString: String): Uri = Uri(uriString)
    }
}
