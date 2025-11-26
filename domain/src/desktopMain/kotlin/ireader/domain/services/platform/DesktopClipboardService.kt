package ireader.domain.services.platform

import ireader.domain.services.common.ServiceResult
import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.StringSelection

/**
 * Desktop implementation of ClipboardService using Java AWT
 */
class DesktopClipboardService : ClipboardService {
    
    private val clipboard by lazy {
        Toolkit.getDefaultToolkit().systemClipboard
    }
    
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
    
    override suspend fun copyText(text: String, label: String?): ServiceResult<Unit> {
        return try {
            val selection = StringSelection(text)
            clipboard.setContents(selection, selection)
            ServiceResult.Success(Unit)
        } catch (e: Exception) {
            ServiceResult.Error("Failed to copy to clipboard: ${e.message}")
        }
    }
    
    override suspend fun getText(): ServiceResult<String> {
        return try {
            if (clipboard.isDataFlavorAvailable(DataFlavor.stringFlavor)) {
                val text = clipboard.getData(DataFlavor.stringFlavor) as? String
                if (text != null) {
                    ServiceResult.Success(text)
                } else {
                    ServiceResult.Error("Clipboard is empty")
                }
            } else {
                ServiceResult.Error("Clipboard does not contain text")
            }
        } catch (e: Exception) {
            ServiceResult.Error("Failed to get clipboard text: ${e.message}")
        }
    }
    
    override suspend fun hasText(): Boolean {
        return try {
            clipboard.isDataFlavorAvailable(DataFlavor.stringFlavor)
        } catch (e: Exception) {
            false
        }
    }
    
    override suspend fun clear(): ServiceResult<Unit> {
        return try {
            val emptySelection = StringSelection("")
            clipboard.setContents(emptySelection, emptySelection)
            ServiceResult.Success(Unit)
        } catch (e: Exception) {
            ServiceResult.Error("Failed to clear clipboard: ${e.message}")
        }
    }
}
