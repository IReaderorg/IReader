package ireader.presentation.core.ui

import androidx.compose.runtime.Composable
import ireader.domain.usecases.translate.WebscrapingTranslateEngine
import ireader.presentation.core.LocalNavigator
import ireader.presentation.core.safePopBackStack
import org.koin.compose.koinInject

/**
 * Screen for logging into ChatGPT and handling translation
 */
class ChatGptLoginScreenSpec {
    
    @Composable
    fun Content() {
        val navController = requireNotNull(LocalNavigator.current) { "LocalNavigator not provided" }
        val engine = koinInject<WebscrapingTranslateEngine>()
        
        // On Android, we use the platform-specific WebView
        ChatGptWebViewImpl(
            engine = engine,
            onTranslationDone = {
                // Return to previous screen when translation is done
                navController.safePopBackStack()
            },
            onClose = {
                // Return to previous screen when user clicks back
                navController.safePopBackStack()
            }
        )
    }
}

/**
 * Platform-specific implementation of ChatGptWebView
 * On Android, this will be the actual WebView
 * On other platforms, this will be a placeholder
 */
@Composable
expect fun ChatGptWebViewImpl(
    engine: WebscrapingTranslateEngine,
    onTranslationDone: () -> Unit,
    onClose: () -> Unit
) 
