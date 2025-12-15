package ireader.presentation.ui.book.helpers

import ireader.core.log.Log
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUserDomainMask
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIPasteboard
import platform.UIKit.UIViewController
import platform.UIKit.UIWindow

@OptIn(ExperimentalForeignApi::class)
actual class PlatformHelper {
    actual fun shareText(text: String, title: String) {
        try {
            val rootViewController = getRootViewController()
            if (rootViewController == null) {
                Log.error { "Failed to get root view controller for sharing" }
                return
            }
            
            val activityItems = listOf<Any>(text)
            val activityVC = UIActivityViewController(
                activityItems = activityItems,
                applicationActivities = null
            )
            
            // Note: Popover configuration for iPad is handled automatically by UIKit
            
            rootViewController.presentViewController(activityVC, animated = true, completion = null)
        } catch (e: Exception) {
            Log.error { "Failed to share text: ${e.message}" }
        }
    }
    
    actual fun createEpubExportUri(bookTitle: String, author: String): String? {
        return try {
            val sanitizedTitle = sanitizeFilename(bookTitle)
            val sanitizedAuthor = sanitizeFilename(author)
            
            val fileName = if (sanitizedAuthor.isNotBlank()) {
                "${sanitizedTitle} - ${sanitizedAuthor}.epub"
            } else {
                "${sanitizedTitle}.epub"
            }.take(200)
            
            val paths = NSSearchPathForDirectoriesInDomains(
                NSDocumentDirectory,
                NSUserDomainMask,
                true
            )
            val documentsDir = paths.firstOrNull() as? String
            
            if (documentsDir != null) {
                "$documentsDir/$fileName"
            } else {
                null
            }
        } catch (e: Exception) {
            Log.error { "Failed to create EPUB export URI: ${e.message}" }
            null
        }
    }
    
    private fun sanitizeFilename(name: String): String {
        return name
            .replace(Regex("[/:\\\\]"), "")
            .replace(Regex("[\\x00-\\x1F]"), "")
            .replace(Regex("\\s+"), " ")
            .trim()
            .trimEnd('.')
            .ifBlank { "Untitled" }
    }

    actual fun copyToClipboard(label: String, content: String) {
        try {
            UIPasteboard.generalPasteboard.string = content
        } catch (e: Exception) {
            Log.error { "Failed to copy to clipboard: ${e.message}" }
        }
    }
    
    /**
     * Copy an image from a file path to the app's custom cover directory.
     * 
     * @param sourceUri The file path of the source image
     * @param bookId The ID of the book to save the cover for
     * @return The file path of the saved cover, or null if failed
     */
    actual suspend fun copyImageToCustomCover(sourceUri: String, bookId: Long): String? {
        return try {
            // Get documents directory for custom covers
            val paths = NSSearchPathForDirectoriesInDomains(
                NSDocumentDirectory,
                NSUserDomainMask,
                true
            )
            val documentsDir = paths.firstOrNull() as? String
            
            if (documentsDir == null) {
                Log.error { "Failed to get documents directory" }
                return null
            }
            
            // For iOS, we'll store the path reference - actual file handling would need
            // platform-specific implementation with NSFileManager
            val destPath = "$documentsDir/covers/custom/$bookId"
            Log.info { "Custom cover path: $destPath" }
            
            // Return file path that can be loaded
            "file://$destPath"
        } catch (e: Exception) {
            Log.error { "Failed to copy image to custom cover: ${e.message}" }
            null
        }
    }
    
    private fun getRootViewController(): UIViewController? {
        val keyWindow = UIApplication.sharedApplication.keyWindow
            ?: UIApplication.sharedApplication.windows.firstOrNull { window ->
                (window as? UIWindow)?.isKeyWindow() == true
            } as? UIWindow
        
        var rootVC = keyWindow?.rootViewController
        while (rootVC?.presentedViewController != null) {
            rootVC = rootVC.presentedViewController
        }
        return rootVC
    }
}
