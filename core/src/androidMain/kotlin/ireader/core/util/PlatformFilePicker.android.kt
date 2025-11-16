package ireader.core.util

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Android implementation of file picker using ActivityResultContracts
 */
actual object PlatformFilePicker {
    private var currentActivity: ComponentActivity? = null
    
    /**
     * Set the current activity for file picking
     * This should be called from the main activity
     */
    fun setActivity(activity: ComponentActivity) {
        currentActivity = activity
    }
    
    actual suspend fun pickFiles(fileTypes: List<String>, multiSelect: Boolean): List<String>? {
        val activity = currentActivity ?: return null
        
        return suspendCancellableCoroutine { continuation ->
            val mimeTypes = fileTypes.map { ext ->
                when (ext.lowercase()) {
                    "ttf" -> "font/ttf"
                    "otf" -> "font/otf"
                    else -> "application/octet-stream"
                }
            }.toTypedArray()
            
            val launcher = if (multiSelect) {
                activity.registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris: List<Uri> ->
                    if (uris.isNotEmpty()) {
                        val paths = uris.mapNotNull { uri ->
                            try {
                                // Copy file to cache and return path
                                copyUriToCache(activity, uri)
                            } catch (e: Exception) {
                                null
                            }
                        }
                        continuation.resume(paths.ifEmpty { null })
                    } else {
                        continuation.resume(null)
                    }
                }
            } else {
                activity.registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
                    if (uri != null) {
                        try {
                            val path = copyUriToCache(activity, uri)
                            continuation.resume(listOfNotNull(path))
                        } catch (e: Exception) {
                            continuation.resume(null)
                        }
                    } else {
                        continuation.resume(null)
                    }
                }
            }
            
            continuation.invokeOnCancellation {
                launcher.unregister()
            }
            
            // Launch the picker
            if (multiSelect) {
                (launcher as androidx.activity.result.ActivityResultLauncher<String>).launch("*/*")
            } else {
                (launcher as androidx.activity.result.ActivityResultLauncher<String>).launch("*/*")
            }
        }
    }
    
    private fun copyUriToCache(activity: Activity, uri: Uri): String? {
        val inputStream = activity.contentResolver.openInputStream(uri) ?: return null
        val fileName = getFileName(activity, uri) ?: "temp_${System.currentTimeMillis()}"
        val cacheFile = java.io.File(activity.cacheDir, fileName)
        
        inputStream.use { input ->
            cacheFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        
        return cacheFile.absolutePath
    }
    
    private fun getFileName(activity: Activity, uri: Uri): String? {
        val cursor = activity.contentResolver.query(uri, null, null, null, null)
        return cursor?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (nameIndex >= 0) it.getString(nameIndex) else null
            } else null
        }
    }
}
