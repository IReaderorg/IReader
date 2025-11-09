package ireader.core.os

import android.content.Context
import android.content.Intent
import android.net.Uri

/**
 * Opens the given URL in the system's default browser on Android.
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
        
        // Parse and validate URI
        val uri = Uri.parse(formattedUrl)
        if (uri == null || uri.scheme == null) {
            return Result.failure(IllegalArgumentException("Invalid URL format: $url"))
        }
        
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(Exception("Failed to open browser: ${e.message}", e))
    }
}

/**
 * Opens the given URL in the system's default browser on Android with context.
 * This is the actual implementation that requires Android context.
 * 
 * @param context Android context required to start the browser activity
 * @param url The URL to open. Should be a valid HTTP/HTTPS URL.
 * @return Result indicating success or failure with error message.
 */
fun openInBrowser(context: Context, url: String): Result<Unit> {
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
        
        // Parse and validate URI
        val uri = Uri.parse(formattedUrl)
        if (uri == null || uri.scheme == null) {
            return Result.failure(IllegalArgumentException("Invalid URL format: $url"))
        }
        
        // Create intent to open browser
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            // Add flags to ensure browser opens in a new task
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        
        // Check if there's an app that can handle this intent
        if (intent.resolveActivity(context.packageManager) == null) {
            return Result.failure(Exception("No browser app found to open URL"))
        }
        
        // Launch the browser
        context.startActivity(intent)
        
        Result.success(Unit)
    } catch (e: SecurityException) {
        Result.failure(Exception("Permission denied to open browser", e))
    } catch (e: Exception) {
        Result.failure(Exception("Failed to open browser: ${e.message}", e))
    }
}
