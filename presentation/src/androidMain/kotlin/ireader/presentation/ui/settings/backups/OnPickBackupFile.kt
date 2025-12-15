package ireader.presentation.ui.settings.backups

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import ireader.domain.models.common.Uri

/**
 * Android implementation of backup file picker for restore
 */
@Composable
actual fun OnPickBackupFile(
    show: Boolean,
    onFileSelected: (Uri?) -> Unit
) {
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data?.data != null) {
            val androidUri = result.data?.data
            onFileSelected(androidUri?.let { Uri(it) })
        } else {
            onFileSelected(null)
        }
    }
    
    LaunchedEffect(show) {
        if (show) {
            // IReader backups are GZIP or JSON files
            val mimeTypes = arrayOf("application/gzip", "application/json", "application/x-gzip")
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                .addCategory(Intent.CATEGORY_OPENABLE)
                .setType("*/*")
                .putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            launcher.launch(intent)
        }
    }
}

/**
 * Android implementation of backup file saver
 */
@Composable
actual fun OnSaveBackupFile(
    show: Boolean,
    defaultFileName: String,
    onLocationSelected: (Uri?) -> Unit
) {
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data?.data != null) {
            val androidUri = result.data?.data
            onLocationSelected(androidUri?.let { Uri(it) })
        } else {
            onLocationSelected(null)
        }
    }
    
    LaunchedEffect(show) {
        if (show) {
            val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
                .addCategory(Intent.CATEGORY_OPENABLE)
                .setType("application/gzip")
                .putExtra(Intent.EXTRA_TITLE, "$defaultFileName.gz")
            launcher.launch(intent)
        }
    }
}
