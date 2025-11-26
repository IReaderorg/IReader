package ireader.domain.services.platform

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import ireader.domain.services.common.ServiceResult

/**
 * Android implementation of ClipboardService
 */
class AndroidClipboardService(
    private val context: Context
) : ClipboardService {
    
    private val clipboardManager: ClipboardManager by lazy {
        context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    }
    
    override suspend fun initialize() {
        // No initialization needed
    }
    
    override suspend fun start() {
        // No start needed
    }
    
    override suspend fun stop() {
        // No stop needed
    }
    
    override fun isRunning(): Boolean = true
    
    override suspend fun cleanup() {
        // No cleanup needed
    }
    
    override suspend fun copyText(text: String, label: String?): ServiceResult<Unit> {
        return try {
            val clip = ClipData.newPlainText(label ?: "text", text)
            clipboardManager.setPrimaryClip(clip)
            ServiceResult.Success(Unit)
        } catch (e: Exception) {
            ServiceResult.Error("Failed to copy to clipboard: ${e.message}")
        }
    }
    
    override suspend fun getText(): ServiceResult<String> {
        return try {
            val clip = clipboardManager.primaryClip
            if (clip != null && clip.itemCount > 0) {
                val text = clip.getItemAt(0).text?.toString()
                if (text != null) {
                    ServiceResult.Success(text)
                } else {
                    ServiceResult.Error("Clipboard is empty")
                }
            } else {
                ServiceResult.Error("Clipboard is empty")
            }
        } catch (e: Exception) {
            ServiceResult.Error("Failed to get clipboard text: ${e.message}")
        }
    }
    
    override suspend fun hasText(): Boolean {
        return try {
            clipboardManager.hasPrimaryClip() && 
                clipboardManager.primaryClipDescription?.hasMimeType("text/plain") == true
        } catch (e: Exception) {
            false
        }
    }
    
    override suspend fun clear(): ServiceResult<Unit> {
        return try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                clipboardManager.clearPrimaryClip()
            } else {
                // For older versions, set empty clip
                val clip = ClipData.newPlainText("", "")
                clipboardManager.setPrimaryClip(clip)
            }
            ServiceResult.Success(Unit)
        } catch (e: Exception) {
            ServiceResult.Error("Failed to clear clipboard: ${e.message}")
        }
    }
}
