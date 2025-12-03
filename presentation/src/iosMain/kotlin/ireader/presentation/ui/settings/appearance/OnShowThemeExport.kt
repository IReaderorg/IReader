package ireader.presentation.ui.settings.appearance

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import ireader.domain.utils.extensions.launchIO
import platform.UIKit.*
import platform.Foundation.*

@Composable
actual fun OnShowThemeExport(
    show: Boolean,
    themeJson: String,
    onFileSelected: suspend (Boolean) -> Unit
) {
    val scope = rememberCoroutineScope()
    
    if (show) {
        scope.launchIO {
            try {
                // Copy to clipboard as a simple export method
                UIPasteboard.generalPasteboard.string = themeJson
                onFileSelected(true)
            } catch (e: Exception) {
                e.printStackTrace()
                onFileSelected(false)
            }
        }
    }
}
