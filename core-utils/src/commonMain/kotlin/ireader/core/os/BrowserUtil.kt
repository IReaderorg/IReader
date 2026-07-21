package ireader.core.os

/**
 * Opens the given URL in the system's default browser.
 * 
 * @param url The URL to open. Should be a valid HTTP/HTTPS URL.
 * @return Result indicating success or failure with error message.
 */
expect fun openInBrowser(url: String): Result<Unit>
