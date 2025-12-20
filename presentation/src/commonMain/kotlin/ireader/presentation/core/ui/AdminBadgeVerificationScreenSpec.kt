package ireader.presentation.core.ui

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import ireader.presentation.core.LocalNavigator
import ireader.presentation.core.safePopBackStack
import ireader.presentation.ui.settings.admin.AdminBadgeVerificationScreen
import ireader.presentation.ui.settings.admin.AdminBadgeVerificationViewModel

/**
 * Screen specification for Admin Badge Verification
 */
class AdminBadgeVerificationScreenSpec {
    
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Content() {
        val navController = requireNotNull(LocalNavigator.current) { "LocalNavigator not provided" }
        val viewModel: AdminBadgeVerificationViewModel = getIViewModel()
        
        AdminBadgeVerificationScreen(
            viewModel = viewModel,
            onNavigateBack = {
                navController.safePopBackStack()
            }
        )
    }
}
