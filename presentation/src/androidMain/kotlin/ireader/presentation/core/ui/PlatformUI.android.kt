package ireader.presentation.core.ui

import android.webkit.MimeTypeMap
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ireader.domain.models.common.Uri
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Android implementation of PlatformFilePicker
 */
class AndroidFilePicker : PlatformFilePicker {
    override suspend fun pickFile(
        mimeTypes: List<String>,
        initialDirectory: String?
    ): Result<Uri?> = runCatching {
        suspendCancellableCoroutine { continuation ->
            // Note: This needs to be called from a Composable context
            // For non-composable usage, consider using ActivityResultContracts directly
            continuation.resume(null)
        }
    }

    override suspend fun pickFiles(
        mimeTypes: List<String>,
        initialDirectory: String?
    ): Result<List<Uri>?> = runCatching {
        suspendCancellableCoroutine { continuation ->
            continuation.resume(null)
        }
    }

    override suspend fun pickDirectory(
        initialDirectory: String?
    ): Result<String?> = runCatching {
        suspendCancellableCoroutine { continuation ->
            continuation.resume(null)
        }
    }
}

/**
 * Android implementation of PlatformBackHandler
 */
class AndroidBackHandler : PlatformBackHandler {
    @Composable
    override fun Handle(enabled: Boolean, onBack: () -> Unit) {
        BackHandler(enabled = enabled, onBack = onBack)
    }
}

/**
 * Android implementation of PlatformScrollbar
 */
class AndroidScrollbar : PlatformScrollbar {
    @Composable
    override fun Vertical(
        modifier: Modifier,
        rightSide: Boolean,
        content: @Composable () -> Unit
    ) {
        // Android typically doesn't show scrollbars, just render content
        content()
    }
}

actual fun getPlatformFilePicker(): PlatformFilePicker = AndroidFilePicker()

actual fun getPlatformBackHandler(): PlatformBackHandler = AndroidBackHandler()

actual fun getPlatformScrollbar(): PlatformScrollbar = AndroidScrollbar()

/**
 * Composable helper for file picking on Android
 */
@Composable
fun rememberFilePicker(
    onFileSelected: (Uri?) -> Unit
): () -> Unit {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { result ->
        onFileSelected(result?.let { Uri(it) })
    }
    return { launcher.launch(arrayOf("*/*")) }
}

/**
 * Composable helper for directory picking on Android
 */
@Composable
fun rememberDirectoryPicker(
    onDirectorySelected: (String?) -> Unit
): () -> Unit {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { result ->
        onDirectorySelected(result?.toString())
    }
    return { launcher.launch(null) }
}
