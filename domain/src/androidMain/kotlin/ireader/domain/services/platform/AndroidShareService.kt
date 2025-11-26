package ireader.domain.services.platform

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import ireader.domain.models.common.Uri
import ireader.domain.services.common.ServiceResult
import java.io.File

/**
 * Android implementation of ShareService
 */
class AndroidShareService(
    private val context: Context
) : ShareService {
    
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
    
    override suspend fun shareText(text: String, title: String?): ServiceResult<Unit> {
        return try {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, text)
                if (title != null) {
                    putExtra(Intent.EXTRA_SUBJECT, title)
                }
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            
            val chooser = Intent.createChooser(intent, title ?: "Share")
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            
            context.startActivity(chooser)
            ServiceResult.Success(Unit)
        } catch (e: Exception) {
            ServiceResult.Error("Failed to share text: ${e.message}")
        }
    }
    
    override suspend fun shareFile(
        uri: Uri,
        mimeType: String,
        title: String?
    ): ServiceResult<Unit> {
        return try {
            val file = File(uri.path)
            val contentUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, contentUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            
            val chooser = Intent.createChooser(intent, title ?: "Share File")
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            
            context.startActivity(chooser)
            ServiceResult.Success(Unit)
        } catch (e: Exception) {
            ServiceResult.Error("Failed to share file: ${e.message}")
        }
    }
    
    override suspend fun shareFiles(
        uris: List<Uri>,
        mimeType: String,
        title: String?
    ): ServiceResult<Unit> {
        return try {
            val contentUris = uris.map { uri ->
                val file = File(uri.path)
                FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )
            }
            
            val intent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                type = mimeType
                putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(contentUris))
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            
            val chooser = Intent.createChooser(intent, title ?: "Share Files")
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            
            context.startActivity(chooser)
            ServiceResult.Success(Unit)
        } catch (e: Exception) {
            ServiceResult.Error("Failed to share files: ${e.message}")
        }
    }
    
    override suspend fun shareUrl(url: String, title: String?): ServiceResult<Unit> {
        return shareText(url, title)
    }
    
    override fun isSharingSupported(): Boolean = true
}
