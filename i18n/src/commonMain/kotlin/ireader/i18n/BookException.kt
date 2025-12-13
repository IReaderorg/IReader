package ireader.i18n

class SourceNotFoundException : Exception()
class EmptyQuery : Exception()

/**
 * Exception thrown when a source fails to parse content from the website.
 * This indicates the website structure has changed and the source needs updating.
 * 
 * This is different from network errors - the request completed successfully
 * but the parsing logic failed due to website changes.
 */
class SourceBrokenException(
    message: String = "Source parsing failed - website structure may have changed",
    cause: Throwable? = null
) : Exception(message, cause)

/**
 * Exception thrown when parsing HTML/JSON content fails.
 * Usually indicates the source website has been updated.
 */
class ParsingException(
    message: String = "Failed to parse content",
    cause: Throwable? = null
) : Exception(message, cause)
