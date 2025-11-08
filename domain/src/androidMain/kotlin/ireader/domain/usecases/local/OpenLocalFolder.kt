package ireader.domain.usecases.local

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import ireader.core.source.LocalCatalogSource
import java.io.File

/**
 * Android implementation to open the local folder
 */
actual class OpenLocalFolder actual constructor(
    private val localSource: LocalCatalogSource
) {
    private var context: Context? = null
    
    constructor(localSource: LocalCatalogSource, context: Context) : this(localSource) {
        this.context = context
    }
    
    actual fun open(): Boolean {
        val ctx = context ?: return false
        val folderPath = localSource.getLocalFolderPath()
        val folder = File(folderPath)
        
        return try {
            // Try to open with file manager
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(
                    Uri.parse(folderPath),
                    "resource/folder"
                )
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            
            // Check if there's an app that can handle this
            if (intent.resolveActivity(ctx.packageManager) != null) {
                ctx.startActivity(intent)
                true
            } else {
                // Fallback: try to open parent directory
                openWithDocumentsUI(ctx, folder)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    private fun openWithDocumentsUI(context: Context, folder: File): Boolean {
        return try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(
                    Uri.parse("content://com.android.externalstorage.documents/document/primary:${folder.absolutePath.removePrefix("/storage/emulated/0/")}"),
                    "vnd.android.document/directory"
                )
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    actual fun getPath(): String {
        return localSource.getLocalFolderPath()
    }
}
