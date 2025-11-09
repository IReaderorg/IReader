package ireader.core.os

import java.awt.Desktop
import java.net.URI

/**
 * Opens the given URL in the system's default browser on Desktop (Windows/Mac/Linux).
 * 
 * @param url The URL to open. Should be a valid HTTP/HTTPS URL.
 * @return Result indicating success or failure with error message.
 */
actual fun openInBrowser(url: String): Result<Unit> {
    return try {
        // Validate URL format
        if (url.isBlank()) {
            return Result.failure(IllegalArgumentException("URL cannot be empty"))
        }
        
        // Ensure URL has a scheme
        val formattedUrl = if (!url.startsWith("http://") && !url.startsWith("https://")) {
            "https://$url"
        } else {
            url
        }
        
        // Check if Desktop is supported on this platform
        if (!Desktop.isDesktopSupported()) {
            return Result.failure(UnsupportedOperationException("Desktop operations are not supported on this platform"))
        }
        
        val desktop = Desktop.getDesktop()
        
        // Check if browse action is supported
        if (!desktop.isSupported(Desktop.Action.BROWSE)) {
            return Result.failure(UnsupportedOperationException("Browser opening is not supported on this platform"))
        }
        
        // Parse and validate URI
        val uri = URI(formattedUrl)
        
        // Open the browser
        desktop.browse(uri)
        
        Result.success(Unit)
    } catch (e: java.net.URISyntaxException) {
        Result.failure(IllegalArgumentException("Invalid URL format: $url", e))
    } catch (e: java.io.IOException) {
        Result.failure(Exception("Failed to open browser: ${e.message}", e))
    } catch (e: Exception) {
        Result.failure(Exception("Unexpected error opening browser: ${e.message}", e))
    }
}
