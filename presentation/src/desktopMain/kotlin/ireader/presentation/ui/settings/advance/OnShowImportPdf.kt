package ireader.presentation.ui.settings.advance

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import ireader.core.storage.BackupDir
import ireader.domain.models.common.Uri
import ireader.domain.utils.extensions.launchIO
import ireader.presentation.core.util.FileChooser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
actual fun OnShowImportPdf(show: Boolean, onFileSelected: suspend (List<Uri>) -> Unit) {
    val scope = rememberCoroutineScope()
    LaunchedEffect(show) {
        if (show) {
            scope.launch(Dispatchers.Default) {
                val fileChosen = FileChooser.chooseFile(
                    initialDirectory = BackupDir.absolutePath,
                    fileExtensions = "pdf"
                )
                withContext(Dispatchers.Main) {
                    if (fileChosen != null) {
                        onFileSelected(listOf(Uri(fileChosen)))
                    } else {
                        // User cancelled - call with empty list to reset the dialog state
                        onFileSelected(emptyList())
                    }
                }
            }
        }
    }
}
