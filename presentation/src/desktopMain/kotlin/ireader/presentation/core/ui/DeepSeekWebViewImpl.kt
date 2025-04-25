package ireader.presentation.core.ui

import androidx.compose.runtime.Composable
import ireader.domain.usecases.translate.WebscrapingTranslateEngine

@Composable
actual fun DeepSeekWebViewImpl(
    engine: WebscrapingTranslateEngine, 
    onTranslationDone: () -> Unit, 
    onClose: () -> Unit
) {
    // Desktop implementation is a placeholder
    // Add desktop implementation if needed in the future
} 