package ireader.presentation.ui.settings.backups

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import ireader.domain.models.common.Uri
import ireader.domain.utils.extensions.launchIO
import ireader.presentation.core.util.FilePicker
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUserDomainMask
import kotlin.time.ExperimentalTime

@Composable
actual fun OnShowRestore(
    show: Boolean,
    onFileSelected: suspend (Uri) -> Unit
) {
    val scope = rememberCoroutineScope()
    FilePicker(
        show = show,
        initialDirectory = null,
        fileExtensions = listOf("gz"),
        onFileSelected = { uri ->
            scope.launchIO {
                if (uri != null) {
                    onFileSelected(uri)
                }
            }
        }
    )
}

@OptIn(ExperimentalTime::class)
@Composable
actual fun OnShowBackup(
    show: Boolean,
    onFileSelected: suspend (Uri) -> Unit
) {
    val scope = rememberCoroutineScope()
    
    LaunchedEffect(show) {
        if (show) {
            scope.launchIO {
                val timestamp = kotlin.time.Clock.System.now().toEpochMilliseconds()
                val fileName = "IReader_backup_$timestamp.gz"
                
                val paths = NSSearchPathForDirectoriesInDomains(
                    NSDocumentDirectory,
                    NSUserDomainMask,
                    true
                )
                val documentsPath = paths.firstOrNull() as? String
                
                if (documentsPath != null) {
                    val filePath = "$documentsPath/$fileName"
                    onFileSelected(Uri.parse(filePath))
                }
            }
        }
    }
}

@Composable
actual fun OnShowLNReaderImport(
    show: Boolean,
    onFileSelected: suspend (Uri?) -> Unit
) {
    val scope = rememberCoroutineScope()
    FilePicker(
        show = show,
        initialDirectory = null,
        fileExtensions = listOf("zip"),
        onFileSelected = { uri ->
            scope.launchIO {
                onFileSelected(uri)
            }
        }
    )
}
