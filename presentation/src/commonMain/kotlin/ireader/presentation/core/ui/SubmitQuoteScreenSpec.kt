package ireader.presentation.core.ui

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import ireader.presentation.core.LocalNavigator
import ireader.presentation.core.safePopBackStack
import ireader.presentation.ui.readinghub.ReadingHubViewModel
import ireader.presentation.ui.readinghub.SubmitQuoteScreen

/**
 * Screen specification for the Submit Quote screen.
 * Allows users to submit their favorite book quotes.
 */
class SubmitQuoteScreenSpec {
    
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Content() {
        val navController = requireNotNull(LocalNavigator.current) { "LocalNavigator not provided" }
        val viewModel: ReadingHubViewModel = getIViewModel()
        val state by viewModel.state.collectAsState()
        
        SubmitQuoteScreen(
            isSubmitting = state.isSubmitting,
            onBack = { navController.safePopBackStack() },
            onSubmit = { text, bookTitle, author, category ->
                viewModel.submitQuote(text, bookTitle, author, category)
                // Navigate back after successful submission
                if (!state.isSubmitting) {
                    navController.safePopBackStack()
                }
            }
        )
    }
}
