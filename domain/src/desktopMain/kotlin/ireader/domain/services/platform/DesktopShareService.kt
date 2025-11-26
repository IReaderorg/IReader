package ireader.domain.services.platform

import ireader.domain.models.common.Uri
import ireader.domain.services.common.ServiceResult
import java.awt.Desktop
import java.io.File

/**
 * Desktop implementation of ShareService
 * 
 * Note: Desktop doesn't have native sharing like mobile platforms.
 * This implementation provides basic functionality like opening files
 * with default applications and copying to clipboard.
 */
class DesktopShareService(
    private val clipboardService: ClipboardService
) : ShareService {
    
    private var running = false
    
    override suspend fun initialize() {
        // No initialization needed
    }
    
    override suspend fun start() { running = true }
    override suspend fun stop() { running = false }
    override fun isRunning(): Boolean = running
    
    override suspend fun cleanup() {
        // No cleanup needed
    }
    
    override suspend fun shareText(text: String, title: String?): ServiceResult<Unit> {
        // On desktop, "sharing" text means copying to clipboard
        return clipboardService.copyText(text, title)
    }
    
    override suspend fun shareFile(
        uri: Uri,
        mimeType: String,
        title: String?
    ): ServiceResult<Unit> {
        return try {
            if (Desktop.isDesktopSupported()) {
                val desktop = Desktop.getDesktop()
                val file = File(uri.path)
                
                if (file.exists()) {
                    if (desktop.isSupported(Desktop.Action.OPEN)) {
                        desktop.open(file)
                        ServiceResult.Success(Unit)
                    } else {
                        ServiceResult.Error("Opening files is not supported on this system")
                    }
                } else {
                    ServiceResult.Error("File does not exist: ${uri.path}")
                }
            } else {
                ServiceResult.Error("Desktop operations are not supported on this system")
            }
        } catch (e: Exception) {
            ServiceResult.Error("Failed to open file: ${e.message}")
        }
    }
    
    override suspend fun shareFiles(
        uris: List<Uri>,
        mimeType: String,
        title: String?
    ): ServiceResult<Unit> {
        // Open each file with default application
        return try {
            if (Desktop.isDesktopSupported()) {
                val desktop = Desktop.getDesktop()
                
                if (desktop.isSupported(Desktop.Action.OPEN)) {
                    uris.forEach { uri ->
                        val file = File(uri.path)
                        if (file.exists()) {
                            desktop.open(file)
                        }
                    }
                    ServiceResult.Success(Unit)
                } else {
                    ServiceResult.Error("Opening files is not supported on this system")
                }
            } else {
                ServiceResult.Error("Desktop operations are not supported on this system")
            }
        } catch (e: Exception) {
            ServiceResult.Error("Failed to open files: ${e.message}")
        }
    }
    
    override suspend fun shareUrl(url: String, title: String?): ServiceResult<Unit> {
        return try {
            if (Desktop.isDesktopSupported()) {
                val desktop = Desktop.getDesktop()
                
                if (desktop.isSupported(Desktop.Action.BROWSE)) {
                    desktop.browse(java.net.URI(url))
                    ServiceResult.Success(Unit)
                } else {
                    // Fallback: copy URL to clipboard
                    clipboardService.copyText(url, title)
                }
            } else {
                // Fallback: copy URL to clipboard
                clipboardService.copyText(url, title)
            }
        } catch (e: Exception) {
            ServiceResult.Error("Failed to open URL: ${e.message}")
        }
    }
    
    override fun isSharingSupported(): Boolean {
        return Desktop.isDesktopSupported()
    }
}
