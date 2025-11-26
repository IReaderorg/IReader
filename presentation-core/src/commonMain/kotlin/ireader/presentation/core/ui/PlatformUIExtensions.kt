package ireader.presentation.core.ui

import androidx.compose.runtime.*
import ireader.domain.models.common.Uri
import kotlinx.coroutines.launch

/**
 * Extension functions and helpers for easier usage of Platform UI abstractions
 */

/**
 * Remember a file picker launcher that can be invoked from onClick handlers
 */
@Composable
fun rememberFilePickerLauncher(
    mimeTypes: List<String> = emptyList(),
    initialDirectory: String? = null,
    onFileSelected: (Uri?) -> Unit
): () -> Unit {
    val filePicker = remember { getPlatformFilePicker() }
    val scope = rememberCoroutineScope()
    
    return remember(mimeTypes, initialDirectory) {
        {
            scope.launch {
                val result = filePicker.pickFile(mimeTypes, initialDirectory)
                result.onSuccess { uri ->
                    onFileSelected(uri)
                }.onFailure { error ->
                    // Log error or show to user
                    println("File picker error: ${error.message}")
                    onFileSelected(null)
                }
            }
        }
    }
}

/**
 * Remember a directory picker launcher
 */
@Composable
fun rememberDirectoryPickerLauncher(
    initialDirectory: String? = null,
    onDirectorySelected: (String?) -> Unit
): () -> Unit {
    val filePicker = remember { getPlatformFilePicker() }
    val scope = rememberCoroutineScope()
    
    return remember(initialDirectory) {
        {
            scope.launch {
                val result = filePicker.pickDirectory(initialDirectory)
                result.onSuccess { path ->
                    onDirectorySelected(path)
                }.onFailure { error ->
                    println("Directory picker error: ${error.message}")
                    onDirectorySelected(null)
                }
            }
        }
    }
}

/**
 * Remember a multi-file picker launcher
 */
@Composable
fun rememberMultiFilePickerLauncher(
    mimeTypes: List<String> = emptyList(),
    initialDirectory: String? = null,
    onFilesSelected: (List<Uri>?) -> Unit
): () -> Unit {
    val filePicker = remember { getPlatformFilePicker() }
    val scope = rememberCoroutineScope()
    
    return remember(mimeTypes, initialDirectory) {
        {
            scope.launch {
                val result = filePicker.pickFiles(mimeTypes, initialDirectory)
                result.onSuccess { uris ->
                    onFilesSelected(uris)
                }.onFailure { error ->
                    println("Multi-file picker error: ${error.message}")
                    onFilesSelected(null)
                }
            }
        }
    }
}

/**
 * Composable wrapper for back handling
 */
@Composable
fun PlatformBackHandler(
    enabled: Boolean = true,
    onBack: () -> Unit
) {
    val backHandler = remember { getPlatformBackHandler() }
    backHandler.Handle(enabled, onBack)
}

/**
 * Composable wrapper for vertical scrollbar
 */
@Composable
fun PlatformVerticalScrollbar(
    modifier: androidx.compose.ui.Modifier = androidx.compose.ui.Modifier,
    rightSide: Boolean = true,
    content: @Composable () -> Unit
) {
    val scrollbar = remember { getPlatformScrollbar() }
    scrollbar.Vertical(modifier, rightSide, content)
}
