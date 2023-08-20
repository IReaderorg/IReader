package ireader.presentation.ui.settings.backups

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import cafe.adriel.voyager.navigator.currentOrThrow
import ireader.domain.models.common.Uri
import ireader.domain.utils.extensions.convertLongToTime
import ireader.domain.utils.extensions.launchIO
import ireader.presentation.ui.core.theme.LocalGlobalCoroutineScope
import java.util.Calendar

@Composable
actual fun OnShowRestore(show: Boolean, onFileSelected:suspend  (Uri) -> Unit) {
    val globalScope = LocalGlobalCoroutineScope.currentOrThrow
    val onRestore =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { resultIntent ->
            if (resultIntent.resultCode == Activity.RESULT_OK && resultIntent.data != null) {
                val uri = resultIntent.data!!.data!!
                globalScope.launchIO {
                    onFileSelected(Uri(uri))
                }
            }
        }
    LaunchedEffect(show) {
        if(show) {
            val mimeTypes = arrayOf("*")
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                .addCategory(Intent.CATEGORY_OPENABLE)
                .setType("*/*")
                //.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
                .addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            onRestore.launch(intent)
        }
    }
}

@Composable
actual fun OnShowBackup(
    show: Boolean,
    onFileSelected: suspend (Uri) -> Unit
) {
    val globalScope = LocalGlobalCoroutineScope.currentOrThrow
    val onBackup =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { resultIntent ->
            if (resultIntent.resultCode == Activity.RESULT_OK && resultIntent.data != null) {
                val uri = resultIntent.data!!.data!!

                globalScope.launchIO {
                    onFileSelected(Uri(uri))
                }
            }
        }
    LaunchedEffect(show) {
        if(show) {
            val mimeTypes = arrayOf("application/gzip")
            val fn = "IReader_${convertLongToTime(Calendar.getInstance().timeInMillis)}.gz"
            val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
                .addCategory(Intent.CATEGORY_OPENABLE)
                .setType("application/gzip")
                .putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
                .putExtra(
                    Intent.EXTRA_TITLE, fn
                )
            onBackup.launch(intent)
        }
    }
}