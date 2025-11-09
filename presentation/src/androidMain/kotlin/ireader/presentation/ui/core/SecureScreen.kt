package ireader.presentation.ui.core

import android.view.WindowManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView

/**
 * Android implementation of SecureScreen
 * Sets FLAG_SECURE to prevent screenshots and screen recording
 */
@Composable
actual fun SecureScreen(
    enabled: Boolean,
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    val window = (view.context as? android.app.Activity)?.window
    
    DisposableEffect(enabled) {
        if (enabled) {
            window?.setFlags(
                WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE
            )
        } else {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
        
        onDispose {
            // Clear flag when composable is disposed
            window?.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }
    }
    
    content()
}
