package ireader.core.util

import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.*
import platform.UIKit.*
import platform.UniformTypeIdentifiers.*
import kotlinx.cinterop.ExperimentalForeignApi
import kotlin.coroutines.resume

/**
 * iOS implementation of PlatformFilePicker using UIDocumentPickerViewController
 * 
 * Note: This requires a UIViewController to present the picker.
 * The actual presentation must be handled by the iOS app layer.
 * This implementation provides the configuration and result handling.
 */
@OptIn(ExperimentalForeignApi::class)
actual object PlatformFilePicker {
    
    // Callback holder for async file picking
    private var pendingCallback: ((List<String>?) -> Unit)? = null
    
    actual suspend fun pickFiles(
        fileTypes: List<String>,
        multiSelect: Boolean
    ): List<String>? = suspendCancellableCoroutine { continuation ->
        // Convert file extensions to UTTypes
        val utTypes = fileTypes.mapNotNull { extension ->
            when (extension.lowercase().removePrefix(".")) {
                "epub" -> UTTypeEPUB
                "pdf" -> UTTypePDF
                "txt" -> UTTypePlainText
                "json" -> UTTypeJSON
                "zip" -> UTTypeZIP
                "html", "htm" -> UTTypeHTML
                "xml" -> UTTypeXML
                "png" -> UTTypePNG
                "jpg", "jpeg" -> UTTypeJPEG
                "gif" -> UTTypeGIF
                "webp" -> UTTypeWebP
                else -> UTTypeItem // Fallback to generic item
            }
        }.ifEmpty { listOf(UTTypeItem) }
        
        // Store callback for when picker completes
        pendingCallback = { urls ->
            continuation.resume(urls)
        }
        
        // Post notification to iOS layer to show picker
        // The iOS app should observe this notification and present UIDocumentPickerViewController
        val userInfo = mapOf<Any?, Any?>(
            "utTypes" to utTypes.map { it?.identifier ?: "" },
            "multiSelect" to multiSelect
        )
        
        NSNotificationCenter.defaultCenter.postNotificationName(
            "IReaderShowFilePicker",
            `object` = null,
            userInfo = userInfo
        )
        
        continuation.invokeOnCancellation {
            pendingCallback = null
        }
    }
    
    /**
     * Called by iOS layer when file picker completes
     * @param urls List of file URLs selected, or null if cancelled
     */
    fun onFilePickerResult(urls: List<String>?) {
        pendingCallback?.invoke(urls)
        pendingCallback = null
    }
    
    /**
     * Get UTType identifiers for given file extensions
     * Helper for iOS layer to configure document picker
     */
    fun getUTTypeIdentifiers(extensions: List<String>): List<String> {
        return extensions.mapNotNull { extension ->
            when (extension.lowercase().removePrefix(".")) {
                "epub" -> UTTypeEPUB?.identifier
                "pdf" -> UTTypePDF?.identifier
                "txt" -> UTTypePlainText?.identifier
                "json" -> UTTypeJSON?.identifier
                "zip" -> UTTypeZIP?.identifier
                "html", "htm" -> UTTypeHTML?.identifier
                "xml" -> UTTypeXML?.identifier
                "png" -> UTTypePNG?.identifier
                "jpg", "jpeg" -> UTTypeJPEG?.identifier
                "gif" -> UTTypeGIF?.identifier
                "webp" -> UTTypeWebP?.identifier
                else -> UTTypeItem?.identifier
            }
        }
    }
}
