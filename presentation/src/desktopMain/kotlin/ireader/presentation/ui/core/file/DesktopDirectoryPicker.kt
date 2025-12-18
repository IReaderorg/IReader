package ireader.presentation.ui.core.file

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import io.github.vinceglb.filekit.dialogs.compose.rememberDirectoryPickerLauncher

/**
 * Desktop implementation of DirectoryPickerLauncher.
 * Uses FileKit's directory picker.
 */
private class DesktopDirectoryPickerLauncherImpl(
    private val launcher: io.github.vinceglb.filekit.dialogs.DirectoryPickerLauncher
) : DirectoryPickerLauncher {
    override fun launch() {
        launcher.launch()
    }
}

/**
 * Desktop implementation - uses FileKit's directory picker.
 */
@Composable
actual fun rememberPlatformDirectoryPickerLauncher(
    title: String,
    onDirectorySelected: (String?) -> Unit
): DirectoryPickerLauncher {
    val fileKitLauncher = rememberDirectoryPickerLauncher(title = title) { directory ->
        onDirectorySelected(directory?.toString())
    }
    
    return remember(fileKitLauncher) {
        DesktopDirectoryPickerLauncherImpl(fileKitLauncher)
    }
}
