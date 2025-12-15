package ireader.presentation.ui.settings.backups

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import ireader.domain.models.common.Uri

/**
 * Android implementation of LNReader backup file picker
 */
@Composable
actual fun OnPickLNReaderBackup(
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
            // LNReader backups are ZIP files
            val mimeTypes = arrayOf("application/zip", "application/x-zip-compressed")
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                .addCategory(Intent.CATEGORY_OPENABLE)
                .setType("application/zip")
                .putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            launcher.launch(intent)
        }
    }
}
