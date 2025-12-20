package ireader.presentation.core.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import ireader.presentation.core.LocalNavigator
import ireader.presentation.core.safePopBackStack
import ireader.presentation.ui.readinghub.ReadingHubScreen
import ireader.presentation.ui.readinghub.ReadingHubViewModel
import ireader.presentation.ui.readingbuddy.rememberQuoteCardSharer
import kotlinx.coroutines.launch

/**
 * Screen specification for the unified Reading Hub screen.
 * Combines statistics, reading buddy, and quotes into a single screen.
 */
class ReadingHubScreenSpec {
    
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Content() {
        val navController = requireNotNull(LocalNavigator.current) { "LocalNavigator not provided" }
        val viewModel: ReadingHubViewModel = getIViewModel()
        val quoteSharer = rememberQuoteCardSharer()
        val scope = rememberCoroutineScope()
        
        ReadingHubScreen(
            vm = viewModel,
            onBack = { navController.safePopBackStack() },
            onShareQuote = { quote, style ->
                scope.launch {
                    quoteSharer.shareQuoteCard(
                        quote = quote,
                        style = style,
                        onSuccess = {
                            viewModel.showMessage("Quote card shared! 🎉")
                        },
                        onError = { error ->
                            viewModel.showError(error)
                        }
                    )
                }
            }
        )
    }
}
