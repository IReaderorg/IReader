package ireader.domain.js.engine

/**
 * Exception thrown when JavaScript execution fails.
 * Preserves the JavaScript stack trace for debugging.
 */
class JSException(
    message: String,
    val jsStackTrace: String? = null,
    cause: Throwable? = null
) : Exception(message, cause) {
    
    override fun toString(): String {
        val baseMessage = super.toString()
        return if (jsStackTrace != null) {
            "$baseMessage\nJavaScript Stack Trace:\n$jsStackTrace"
        } else {
            baseMessage
        }
    }
}
