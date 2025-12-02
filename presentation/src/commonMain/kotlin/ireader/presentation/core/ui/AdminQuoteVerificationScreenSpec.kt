package ireader.presentation.core.ui

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import ireader.presentation.core.LocalNavigator
import ireader.presentation.ui.readingbuddy.AdminQuoteVerificationScreen
import ireader.presentation.ui.readingbuddy.ReadingBuddyViewModel

/**
 * Screen specification for the Admin Quote Verification screen
 */
class AdminQuoteVerificationScreenSpec {
    
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Content() {
        val navController = requireNotNull(LocalNavigator.current) { "LocalNavigator not provided" }
        val viewModel: ReadingBuddyViewModel = getIViewModel()
        
        AdminQuoteVerificationScreen(
            vm = viewModel,
            onBack = { navController.popBackStack() }
        )
    }
}
