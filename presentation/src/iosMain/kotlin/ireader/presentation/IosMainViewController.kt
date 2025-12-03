package ireader.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController
import ireader.presentation.core.theme.IReaderTheme

/**
 * Creates the main UIViewController for the iOS app that hosts Compose UI.
 * This is called from Swift to get the Compose-based UI.
 */
fun MainViewController(): UIViewController = ComposeUIViewController {
    IReaderApp()
}

/**
 * The main composable entry point for the iOS app.
 * This wraps the shared presentation layer.
 */
@Composable
fun IReaderApp() {
    IReaderTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "IReader iOS",
                    style = MaterialTheme.typography.headlineMedium
                )
            }
        }
    }
}
