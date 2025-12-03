package ireader.presentation.ui.settings.advance

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import ireader.domain.models.common.Uri
import ireader.domain.utils.extensions.launchIO
import ireader.presentation.core.util.FilePicker

@Composable
actual fun OnShowImportEpub(show: Boolean, onFileSelected: suspend (List<Uri>) -> Unit) {
    val scope = rememberCoroutineScope()
    FilePicker(
        show = show,
        initialDirectory = null,
        fileExtensions = listOf("epub", "zip"),
        onFileSelected = { uri ->
            scope.launchIO {
                if (uri != null) {
                    onFileSelected(listOf(uri))
                } else {
                    // User cancelled - call with empty list to reset the dialog state
                    onFileSelected(emptyList())
                }
            }
        }
    )
}
