package ireader.presentation.core.ui

import androidx.compose.runtime.Composable
import ireader.domain.usecases.translate.WebscrapingTranslateEngine

@Composable
actual fun DeepSeekWebViewImpl(
    engine: WebscrapingTranslateEngine, 
    onTranslationDone: () -> Unit, 
    onClose: () -> Unit
) {
    // iOS implementation is a placeholder
    // WebView-based login is not available on iOS in the same way
}
