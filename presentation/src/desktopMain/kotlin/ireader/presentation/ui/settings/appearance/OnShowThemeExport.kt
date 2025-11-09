package ireader.presentation.ui.settings.appearance

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import ireader.core.storage.BackupDir
import ireader.domain.utils.extensions.launchIO
import ireader.presentation.core.util.DirectoryPicker
import java.io.File

@Composable
actual fun OnShowThemeExport(
    show: Boolean,
    themeJson: String,
    onFileSelected: suspend (Boolean) -> Unit
) {
    val scope = rememberCoroutineScope()
    DirectoryPicker(show = show, BackupDir.absolutePath, onFileSelected = { directoryPath ->
        scope.launchIO {
            try {
                if (directoryPath != null) {
                    val fn = "IReader_Theme_${System.currentTimeMillis()}.json"
                    val file = File(directoryPath, fn)
                    if (!file.exists()) {
                        file.createNewFile()
                    }
                    file.writeText(themeJson)
                    onFileSelected(true)
                } else {
                    onFileSelected(false)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                onFileSelected(false)
            }
        }
    })
}
