package ireader.presentation.ui.core.file

import io.github.vinceglb.filekit.PlatformFile

/**
 * FileKit utility extensions for file operations.
 * 
 * For file picking in Compose UI, use the compose launchers directly:
 * - rememberFilePickerLauncher
 * - rememberDirectoryPickerLauncher
 * - rememberFileSaverLauncher
 * 
 * These are imported from: io.github.vinceglb.filekit.dialogs.compose
 */

/**
 * Result wrapper for file operations
 */
sealed class FilePickerResult<out T> {
    data class Success<T>(val data: T) : FilePickerResult<T>()
    data class Error(val message: String, val exception: Throwable? = null) : FilePickerResult<Nothing>()
    data object Cancelled : FilePickerResult<Nothing>()
}

/**
 * Extension to convert PlatformFile to a path string.
 * Uses toString() which returns the path on all platforms.
 */
fun PlatformFile.toPathString(): String {
    // PlatformFile.toString() returns the path (see FileKit source)
    return this.toString()
}
