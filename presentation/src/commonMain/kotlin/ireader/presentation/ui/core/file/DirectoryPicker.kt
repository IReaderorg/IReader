package ireader.presentation.ui.core.file

import androidx.compose.runtime.Composable

/**
 * Platform-specific directory picker launcher.
 * On Android, uses OpenDocumentTree for proper SAF permissions.
 * On other platforms, uses FileKit's directory picker.
 */
interface DirectoryPickerLauncher {
    fun launch()
}

/**
 * Remember a directory picker launcher.
 * 
 * On Android: Uses OpenDocumentTree which returns a tree URI with persistable permissions.
 * On Desktop/iOS: Uses FileKit's directory picker.
 * 
 * @param title Title for the picker dialog (used on non-Android platforms)
 * @param onDirectorySelected Callback with the selected directory path/URI string, or null if cancelled
 */
@Composable
expect fun rememberPlatformDirectoryPickerLauncher(
    title: String = "Select Folder",
    onDirectorySelected: (String?) -> Unit
): DirectoryPickerLauncher
