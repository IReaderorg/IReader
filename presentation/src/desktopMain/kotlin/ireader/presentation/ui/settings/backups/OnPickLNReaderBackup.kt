package ireader.presentation.ui.settings.backups

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import ireader.core.storage.BackupDir
import ireader.domain.models.common.Uri
import ireader.presentation.core.util.FileChooser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Desktop implementation of LNReader backup file picker
 */
@Composable
actual fun OnPickLNReaderBackup(
    show: Boolean,
    onFileSelected: (Uri?) -> Unit
) {
    val scope = rememberCoroutineScope()
    
    LaunchedEffect(show) {
        if (show) {
            scope.launch(Dispatchers.Default) {
                val fileChosen = FileChooser.chooseFile(
                    initialDirectory = BackupDir.absolutePath,
                    fileExtensions = "zip"
                )
                withContext(Dispatchers.Main) {
                    if (fileChosen != null) {
                        onFileSelected(Uri(fileChosen))
                    } else {
                        onFileSelected(null)
                    }
                }
            }
        }
    }
}
