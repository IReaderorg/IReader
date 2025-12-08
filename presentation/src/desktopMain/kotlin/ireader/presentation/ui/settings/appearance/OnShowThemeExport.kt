package ireader.presentation.ui.settings.appearance

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import ireader.core.storage.BackupDir
import ireader.domain.utils.extensions.launchIO
import ireader.presentation.core.util.FileChooser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@Composable
actual fun OnShowThemeExport(
    show: Boolean,
    themeJson: String,
    onFileSelected: suspend (Boolean) -> Unit
) {
    val scope = rememberCoroutineScope()
    LaunchedEffect(show) {
        if (show) {
            scope.launch(Dispatchers.Default) {
                val directoryPath = FileChooser.chooseDirectory(
                    initialDirectory = BackupDir.absolutePath
                )
                withContext(Dispatchers.Main) {
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
                }
            }
        }
    }
}
