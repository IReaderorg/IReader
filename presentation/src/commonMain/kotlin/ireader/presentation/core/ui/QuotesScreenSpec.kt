package ireader.presentation.core.ui

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import ireader.presentation.core.LocalNavigator
import ireader.presentation.core.NavigationRoutes
import ireader.presentation.core.safePopBackStack
import ireader.presentation.ui.readinghub.QuotesScreen
import ireader.presentation.ui.readinghub.ReadingHubViewModel
import ireader.presentation.ui.readingbuddy.rememberQuoteCardSharer
import kotlinx.coroutines.launch

/**
 * Screen specification for the Instagram-style Quotes screen.
 * Provides immersive vertical pager for browsing quotes.
 */
class QuotesScreenSpec {
    
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Content() {
        val navController = requireNotNull(LocalNavigator.current) { "LocalNavigator not provided" }
        val viewModel: ReadingHubViewModel = getIViewModel()
        val state by viewModel.state.collectAsState()
        val quoteSharer = rememberQuoteCardSharer()
        val scope = rememberCoroutineScope()
        
        QuotesScreen(
            quotes = state.quotes,
            dailyQuote = state.dailyQuote,
            selectedStyle = state.selectedCardStyle,
            isLoading = state.isLoading,
            onBack = { navController.safePopBackStack() },
            onToggleLike = { quote -> viewModel.toggleLike(quote) },
            onShare = { quote ->
                scope.launch {
                    quoteSharer.shareQuoteCard(
                        quote = quote,
                        style = state.selectedCardStyle,
                        onSuccess = {
                            viewModel.showMessage("Quote card shared! 🎉")
                        },
                        onError = { error ->
                            viewModel.showError(error)
                        }
                    )
                }
            },
            onStyleChange = { style -> viewModel.setCardStyle(style) },
            onSubmitQuote = {
                navController.navigate(NavigationRoutes.submitQuote)
            }
        )
    }
}
