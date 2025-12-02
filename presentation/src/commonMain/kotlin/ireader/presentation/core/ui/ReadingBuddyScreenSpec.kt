package ireader.presentation.core.ui

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import ireader.presentation.core.LocalNavigator
import ireader.presentation.ui.readingbuddy.ReadingBuddyScreen
import ireader.presentation.ui.readingbuddy.ReadingBuddyViewModel
import ireader.presentation.ui.readingbuddy.rememberQuoteCardSharer
import kotlinx.coroutines.launch

/**
 * Screen specification for the Reading Buddy & Daily Quotes screen
 */
class ReadingBuddyScreenSpec {
    
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Content() {
        val navController = requireNotNull(LocalNavigator.current) { "LocalNavigator not provided" }
        val viewModel: ReadingBuddyViewModel = getIViewModel()
        val quoteSharer = rememberQuoteCardSharer()
        val scope = rememberCoroutineScope()
        Scaffold { paddingValues ->
            ReadingBuddyScreen(
                vm = viewModel,
                onBack = { navController.popBackStack() },
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
                },
                paddingValues= paddingValues
            )
        }

    }
}
