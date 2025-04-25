package ireader.presentation.core.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ireader.domain.usecases.translate.WebscrapingTranslateEngine

/**
 * Desktop placeholder implementation
 * WebView is not available on desktop, so we show a placeholder
 */
@Composable
actual fun ChatGptWebViewImpl(
    engine: WebscrapingTranslateEngine,
    onTranslationDone: () -> Unit,
    onClose: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "ChatGPT WebView is only available on Android",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                "This feature uses a WebView to log into ChatGPT directly",
                modifier = Modifier.padding(vertical = 8.dp)
            )
            Button(onClick = onClose) {
                Text("Go Back")
            }
        }
    }
} 