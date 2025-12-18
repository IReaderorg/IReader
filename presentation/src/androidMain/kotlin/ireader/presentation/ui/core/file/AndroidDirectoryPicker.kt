package ireader.presentation.ui.core.file

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

/**
 * Android implementation of DirectoryPickerLauncher.
 * Uses OpenDocumentTree for proper SAF permissions.
 */
private class AndroidDirectoryPickerLauncherImpl(
    private val launcher: androidx.activity.result.ActivityResultLauncher<Uri?>
) : DirectoryPickerLauncher {
    override fun launch() {
        launcher.launch(null)
    }
}

/**
 * Extract the tree URI from a document URI.
 * OpenDocumentTree can return URIs like:
 * - content://com.android.externalstorage.documents/tree/primary%3AIReader/document/primary%3AIReader
 * We need to extract just the tree part:
 * - content://com.android.externalstorage.documents/tree/primary%3AIReader
 */
private fun extractTreeUri(uri: Uri): Uri {
    val uriString = uri.toString()
    val treeIndex = uriString.indexOf("/tree/")
    if (treeIndex == -1) return uri
    
    val documentIndex = uriString.indexOf("/document/", treeIndex)
    return if (documentIndex != -1) {
        Uri.parse(uriString.substring(0, documentIndex))
    } else {
        uri
    }
}

/**
 * Android implementation - uses OpenDocumentTree which returns a tree URI with persistable permissions.
 */
@Composable
actual fun rememberPlatformDirectoryPickerLauncher(
    title: String,
    onDirectorySelected: (String?) -> Unit
): DirectoryPickerLauncher {
    val context = LocalContext.current
    
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri: Uri? ->
        if (uri != null) {
            android.util.Log.d("AndroidDirectoryPicker", "Received URI from picker: $uri")
            
            // Extract the tree URI (remove /document/ suffix if present)
            val treeUri = extractTreeUri(uri)
            android.util.Log.d("AndroidDirectoryPicker", "Tree URI: $treeUri")
            
            // Take persistent permissions for the tree URI
            var permissionTaken = false
            try {
                val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION or 
                               Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                context.contentResolver.takePersistableUriPermission(treeUri, takeFlags)
                permissionTaken = true
                android.util.Log.d("AndroidDirectoryPicker", "Took persistent permissions for: $treeUri")
            } catch (e: SecurityException) {
                android.util.Log.e("AndroidDirectoryPicker", "SecurityException taking permissions: ${e.message}")
                // This can happen if the URI doesn't have persistable grants
            } catch (e: Exception) {
                android.util.Log.e("AndroidDirectoryPicker", "Failed to take persistent permissions", e)
            }
            
            // Log the persisted permissions for debugging
            val persistedPermissions = context.contentResolver.persistedUriPermissions
            android.util.Log.d("AndroidDirectoryPicker", "Persisted permissions: ${persistedPermissions.map { "${it.uri} r=${it.isReadPermission} w=${it.isWritePermission}" }}")
            
            // Return the tree URI (not the document URI) for consistency
            // The caller (SecureStorageHelper/SafStorageManager) will verify accessibility
            onDirectorySelected(treeUri.toString())
        } else {
            onDirectorySelected(null)
        }
    }
    
    return remember(launcher) {
        AndroidDirectoryPickerLauncherImpl(launcher)
    }
}
