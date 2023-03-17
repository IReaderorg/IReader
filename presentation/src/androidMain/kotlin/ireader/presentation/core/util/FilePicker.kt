package ireader.presentation.core.util

import android.webkit.MimeTypeMap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import ireader.domain.models.common.Uri
@Composable
actual fun FilePicker(
    show: Boolean,
    initialDirectory: String?,
    fileExtensions: List<String>,
    onFileSelected: (ireader.domain.models.common.Uri?) -> Unit
) {
    val launcher = rememberLauncherForActivityResult(contract = ActivityResultContracts.OpenDocument()) { result ->
        onFileSelected(result?.let { Uri(it) })
    }

    val mimeTypeMap = MimeTypeMap.getSingleton()
    val mimeTypes = if (fileExtensions.isNotEmpty()) {
        fileExtensions.mapNotNull { ext ->
            mimeTypeMap.getMimeTypeFromExtension(ext)
        }.toTypedArray()
    } else {
        emptyArray()
    }

    LaunchedEffect(show) {
        if (show) {
            launcher.launch(mimeTypes)
        }
    }
}

@Composable
actual fun DirectoryPicker(
    show: Boolean,
    initialDirectory: String?,
    onFileSelected: (String?) -> Unit
) {
    val launcher = rememberLauncherForActivityResult(contract = ActivityResultContracts.OpenDocumentTree()) { result ->
        onFileSelected(result?.toString())
    }

    LaunchedEffect(show) {
        if (show) {
            launcher.launch(null)
        }
    }
}