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
 * Desktop implementation of backup file picker for restore
 */
@Composable
actual fun OnPickBackupFile(
    show: Boolean,
    onFileSelected: (Uri?) -> Unit
) {
    val scope = rememberCoroutineScope()
    
    LaunchedEffect(show) {
        if (show) {
            scope.launch(Dispatchers.Default) {
                val fileChosen = FileChooser.chooseFile(
                    initialDirectory = BackupDir.absolutePath,
                    fileExtensions = "gz,json"
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

/**
 * Desktop implementation of backup file saver
 */
@Composable
actual fun OnSaveBackupFile(
    show: Boolean,
    defaultFileName: String,
    onLocationSelected: (Uri?) -> Unit
) {
    val scope = rememberCoroutineScope()
    
    LaunchedEffect(show) {
        if (show) {
            scope.launch(Dispatchers.Default) {
                val fileChosen = FileChooser.saveFile(
                    initialDirectory = BackupDir.absolutePath,
                    defaultFileName = "$defaultFileName.gz",
                    fileExtensions = "gz"
                )
                withContext(Dispatchers.Main) {
                    if (fileChosen != null) {
                        onLocationSelected(Uri(fileChosen))
                    } else {
                        onLocationSelected(null)
                    }
                }
            }
        }
    }
}
