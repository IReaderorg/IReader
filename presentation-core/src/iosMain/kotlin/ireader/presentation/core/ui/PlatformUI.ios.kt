package ireader.presentation.core.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ireader.domain.models.common.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import platform.UIKit.*
import platform.Foundation.*
import platform.UniformTypeIdentifiers.*
import kotlinx.cinterop.ExperimentalForeignApi

/**
 * iOS implementation of PlatformFilePicker
 */
@OptIn(ExperimentalForeignApi::class)
class IosFilePicker : PlatformFilePicker {
    override suspend fun pickFile(
        mimeTypes: List<String>,
        initialDirectory: String?
    ): Result<Uri?> = runCatching {
        withContext(Dispatchers.Main) {
            // iOS file picking requires UIDocumentPickerViewController
            // This is a simplified implementation
            null
        }
    }

    override suspend fun pickFiles(
        mimeTypes: List<String>,
        initialDirectory: String?
    ): Result<List<Uri>?> = runCatching {
        withContext(Dispatchers.Main) {
            // iOS file picking requires UIDocumentPickerViewController
            null
        }
    }

    override suspend fun pickDirectory(
        initialDirectory: String?
    ): Result<String?> = runCatching {
        withContext(Dispatchers.Main) {
            // iOS directory picking requires UIDocumentPickerViewController
            null
        }
    }
}

/**
 * iOS implementation of PlatformBackHandler
 */
class IosBackHandler : PlatformBackHandler {
    @Composable
    override fun Handle(enabled: Boolean, onBack: () -> Unit) {
        // iOS uses swipe gestures for back navigation
        // This is handled by the navigation controller
    }
}

/**
 * iOS implementation of PlatformScrollbar
 */
class IosScrollbar : PlatformScrollbar {
    @Composable
    override fun Vertical(
        modifier: Modifier,
        rightSide: Boolean,
        content: @Composable () -> Unit
    ) {
        // iOS uses native scrollbars
        Box(modifier = modifier.fillMaxSize()) {
            content()
        }
    }
}

actual fun getPlatformFilePicker(): PlatformFilePicker = IosFilePicker()

actual fun getPlatformBackHandler(): PlatformBackHandler = IosBackHandler()

actual fun getPlatformScrollbar(): PlatformScrollbar = IosScrollbar()
