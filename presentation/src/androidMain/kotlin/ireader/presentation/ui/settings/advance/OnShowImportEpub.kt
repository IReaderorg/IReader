package ireader.presentation.ui.settings.advance

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import ireader.domain.models.common.Uri
import ireader.domain.utils.extensions.launchIO

@Composable
actual fun OnShowImportEpub(show:Boolean, onFileSelected: suspend (List<Uri>) -> Unit) {
    val scope = rememberCoroutineScope()
    val onImportEpub = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { resultIntent ->
        if (resultIntent.resultCode == Activity.RESULT_OK && resultIntent.data != null) {
            val selectedFiles = mutableListOf<Uri>()
            val clipData = resultIntent.data?.clipData
            if (clipData != null) {
                for (i in 0 until clipData.itemCount) {
                    val uri = clipData.getItemAt(i).uri
                    selectedFiles.add(Uri(uri))
                }
            } else {
                resultIntent.data?.data?.let { uri ->
                    selectedFiles.add(Uri(uri))
                }
            }
            scope.launchIO {
                onFileSelected(selectedFiles)
            }
        } else {
            // User cancelled - call with empty list to reset the dialog state
            scope.launchIO {
                onFileSelected(emptyList())
            }
        }
    }
    LaunchedEffect(show) {
        if(show) {
            val mimeTypes = arrayOf("application/epub+zip")
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                .addCategory(Intent.CATEGORY_OPENABLE)
                .setType("application/epub+zip")
                .putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
                .addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                .putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            onImportEpub.launch(intent)
        }
    }
}
