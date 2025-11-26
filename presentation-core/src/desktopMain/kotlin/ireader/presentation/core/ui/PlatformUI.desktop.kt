package ireader.presentation.core.ui

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import ireader.domain.models.common.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

/**
 * Desktop implementation of PlatformFilePicker
 */
class DesktopFilePicker : PlatformFilePicker {
    override suspend fun pickFile(
        mimeTypes: List<String>,
        initialDirectory: String?
    ): Result<Uri?> = runCatching {
        withContext(Dispatchers.IO) {
            val fileChooser = createFileChooser(mimeTypes, initialDirectory, multiSelect = false)
            val result = fileChooser.showOpenDialog(null)
            
            if (result == JFileChooser.APPROVE_OPTION) {
                fileChooser.selectedFile?.let { Uri(it.absolutePath) }
            } else {
                null
            }
        }
    }

    override suspend fun pickFiles(
        mimeTypes: List<String>,
        initialDirectory: String?
    ): Result<List<Uri>?> = runCatching {
        withContext(Dispatchers.IO) {
            val fileChooser = createFileChooser(mimeTypes, initialDirectory, multiSelect = true)
            val result = fileChooser.showOpenDialog(null)
            
            if (result == JFileChooser.APPROVE_OPTION) {
                fileChooser.selectedFiles?.map { Uri(it.absolutePath) }
            } else {
                null
            }
        }
    }

    override suspend fun pickDirectory(
        initialDirectory: String?
    ): Result<String?> = runCatching {
        withContext(Dispatchers.IO) {
            val fileChooser = JFileChooser().apply {
                fileSelectionMode = JFileChooser.DIRECTORIES_ONLY
                dialogTitle = "Select Directory"
                initialDirectory?.let { currentDirectory = File(it) }
            }
            
            val result = fileChooser.showOpenDialog(null)
            
            if (result == JFileChooser.APPROVE_OPTION) {
                fileChooser.selectedFile?.absolutePath
            } else {
                null
            }
        }
    }

    private fun createFileChooser(
        mimeTypes: List<String>,
        initialDirectory: String?,
        multiSelect: Boolean
    ): JFileChooser {
        return JFileChooser().apply {
            isMultiSelectionEnabled = multiSelect
            fileSelectionMode = JFileChooser.FILES_ONLY
            
            if (mimeTypes.isNotEmpty()) {
                val extensions = mimeTypes.map { it.removePrefix(".") }
                val description = "Files (${extensions.joinToString(", ") { ".$it" }})"
                fileFilter = FileNameExtensionFilter(description, *extensions.toTypedArray())
            }
            
            initialDirectory?.let { currentDirectory = File(it) }
            dialogTitle = "Select File${if (multiSelect) "s" else ""}"
        }
    }
}

/**
 * Desktop implementation of PlatformBackHandler
 */
class DesktopBackHandler : PlatformBackHandler {
    @Composable
    override fun Handle(enabled: Boolean, onBack: () -> Unit) {
        // Desktop doesn't have a system back button
        // This could be implemented with keyboard shortcuts if needed
    }
}

/**
 * Desktop implementation of PlatformScrollbar
 */
class DesktopScrollbar : PlatformScrollbar {
    @Composable
    override fun Vertical(
        modifier: Modifier,
        rightSide: Boolean,
        content: @Composable () -> Unit
    ) {
        Box(modifier = modifier.fillMaxSize()) {
            content()
            // Note: This is a simplified version
            // For full implementation, you'd need to pass ScrollState
        }
    }
}

actual fun getPlatformFilePicker(): PlatformFilePicker = DesktopFilePicker()

actual fun getPlatformBackHandler(): PlatformBackHandler = DesktopBackHandler()

actual fun getPlatformScrollbar(): PlatformScrollbar = DesktopScrollbar()
