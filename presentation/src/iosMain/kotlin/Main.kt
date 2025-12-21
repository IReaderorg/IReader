/**
 * Main iOS entry point - self-contained, no external dependencies
 * Swift accesses this as: MainKt.MainViewController()
 */
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
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.i18n.resources.*

/**
 * Creates the main UIViewController for iOS.
 * Call from Swift: MainKt.MainViewController()
 */
fun MainViewController(): UIViewController = ComposeUIViewController {
    App()
}

@Composable
fun App() {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    MaterialTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = localizeHelper.localize(Res.string.ireader_ios),
                    style = MaterialTheme.typography.headlineMedium
                )
            }
        }
    }
}
