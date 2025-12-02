package ireader.presentation.ui.settings.backups

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import ireader.core.storage.BackupDir
import ireader.domain.models.common.Uri
import ireader.domain.utils.extensions.convertLongToTime
import ireader.domain.utils.extensions.launchIO
import ireader.presentation.core.util.DirectoryPicker
import ireader.presentation.core.util.FilePicker
import java.io.File
import java.util.*

@Composable
actual fun OnShowRestore(
    show: Boolean,
    onFileSelected: suspend (Uri) -> Unit
) {
    val scope = rememberCoroutineScope()
    FilePicker(show = show,BackupDir.absolutePath, listOf("gz",), onFileSelected = {
        scope.launchIO {
            if (it != null) {
                onFileSelected(it)
            }
        }
    })
}

@Composable
actual fun OnShowBackup(
    show: Boolean,
    onFileSelected: suspend (Uri) -> Unit
) {
    val scope = rememberCoroutineScope()
    DirectoryPicker(show = show, BackupDir.absolutePath,  onFileSelected = {
        val fn = "IReader_${convertLongToTime(Calendar.getInstance().timeInMillis)}.gz"
        val file = File(it, fn)
        if(!file.exists()) {
            file.createNewFile()
        }
        scope.launchIO {
            if (it != null) {
                onFileSelected(Uri(file.absolutePath))
            }
        }

    })


}

@Composable
actual fun OnShowLNReaderImport(
    show: Boolean,
    onFileSelected: suspend (Uri?) -> Unit
) {
    val scope = rememberCoroutineScope()
    FilePicker(
        show = show,
        initialDirectory = BackupDir.absolutePath,
        fileExtensions = listOf("zip"),
        onFileSelected = { uri ->
            scope.launchIO {
                onFileSelected(uri)
            }
        }
    )
}
