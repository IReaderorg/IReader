package ireader.presentation.ui.core.file

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import io.github.vinceglb.filekit.dialogs.compose.rememberDirectoryPickerLauncher

/**
 * iOS implementation of DirectoryPickerLauncher.
 * Uses FileKit's directory picker.
 */
private class IosDirectoryPickerLauncherImpl(
    private val launchFn: () -> Unit
) : DirectoryPickerLauncher {
    override fun launch() {
        launchFn()
    }
}

/**
 * iOS implementation - uses FileKit's directory picker.
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
        IosDirectoryPickerLauncherImpl { fileKitLauncher.launch() }
    }
}
