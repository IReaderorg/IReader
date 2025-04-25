package ireader.presentation.core.ui

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import ireader.domain.usecases.translate.WebscrapingTranslateEngine
import org.koin.compose.koinInject
import java.util.UUID

/**
 * Screen for logging into ChatGPT and handling translation
 */
class ChatGptLoginScreenSpec : Screen {
    
    // Create a unique key for this screen instance
    private val uniqueKey = UUID.randomUUID().toString()
    
    // Override key property to provide a unique value
    override val key: String = "chatgpt_login_screen_$uniqueKey"
    
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val engine = koinInject<WebscrapingTranslateEngine>()
        
        // On Android, we use the platform-specific WebView
        ChatGptWebViewImpl(
            engine = engine,
            onTranslationDone = {
                // Return to previous screen when translation is done
                navigator.pop()
            },
            onClose = {
                // Return to previous screen when user clicks back
                navigator.pop()
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