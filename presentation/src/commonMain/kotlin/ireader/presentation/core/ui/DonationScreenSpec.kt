package ireader.presentation.core.ui

import ireader.presentation.core.LocalNavigator

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ireader.presentation.ui.component.IScaffold
import ireader.presentation.ui.settings.donation.DonationScreen
import ireader.presentation.ui.settings.donation.DonationViewModel

/**
 * Screen specification for the cryptocurrency donation page
 */
class DonationScreenSpec {
    
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Content() {
        val navController = requireNotNull(LocalNavigator.current) { "LocalNavigator not provided" }
        val viewModel: DonationViewModel = getIViewModel()
        
        IScaffold { padding ->
            DonationScreen(
                viewModel = viewModel,
                modifier = Modifier.padding(padding),
                onPopBackStack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
