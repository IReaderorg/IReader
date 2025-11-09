package ireader.presentation.core.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import ireader.presentation.ui.component.IScaffold
import ireader.presentation.ui.settings.donation.DonationScreen
import ireader.presentation.ui.settings.donation.DonationViewModel

/**
 * Screen specification for the cryptocurrency donation page
 */
class DonationScreenSpec : Screen {
    
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val viewModel: DonationViewModel = getIViewModel()
        
        IScaffold { padding ->
            DonationScreen(
                viewModel = viewModel,
                modifier = Modifier.padding(padding),
                onPopBackStack = {
                    navigator.pop()
                }
            )
        }
    }
}
