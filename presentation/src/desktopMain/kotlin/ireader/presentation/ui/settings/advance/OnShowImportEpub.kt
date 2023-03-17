package ireader.presentation.ui.settings.advance

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import ireader.core.storage.BackupDir
import ireader.domain.models.common.Uri
import ireader.domain.utils.extensions.launchIO
import ireader.presentation.core.util.FilePicker

@Composable
actual fun OnShowImportEpub(show:Boolean, onFileSelected: suspend (Uri) -> Unit) {
    val scope = rememberCoroutineScope()
    FilePicker(show = show, BackupDir.absolutePath, listOf("epub","zip"), onFileSelected = {
        scope.launchIO {
            if (it != null) {
                onFileSelected(it)
            }
        }
    })
}