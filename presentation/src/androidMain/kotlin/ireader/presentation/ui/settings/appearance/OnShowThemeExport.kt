package ireader.presentation.ui.settings.appearance

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import cafe.adriel.voyager.navigator.currentOrThrow
import ireader.domain.utils.extensions.launchIO
import ireader.presentation.ui.core.theme.LocalGlobalCoroutineScope
import java.io.OutputStreamWriter

@Composable
actual fun OnShowThemeExport(
    show: Boolean,
    themeJson: String,
    onFileSelected: suspend (Boolean) -> Unit
) {
    val globalScope = LocalGlobalCoroutineScope.currentOrThrow
    val context = LocalContext.current
    val onExport =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { resultIntent ->
            if (resultIntent.resultCode == Activity.RESULT_OK && resultIntent.data != null) {
                val uri = resultIntent.data!!.data!!
                globalScope.launchIO {
                    try {
                        context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                            OutputStreamWriter(outputStream).use { writer ->
                                writer.write(themeJson)
                            }
                        }
                        onFileSelected(true)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        onFileSelected(false)
                    }
                }
            }
        }
    LaunchedEffect(show) {
        if (show) {
            val mimeTypes = arrayOf("application/json")
            val fn = "IReader_Theme_${System.currentTimeMillis()}.json"
            val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
                .addCategory(Intent.CATEGORY_OPENABLE)
                .setType("application/json")
                .putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
                .putExtra(Intent.EXTRA_TITLE, fn)
            onExport.launch(intent)
        }
    }
}
