package ireader.presentation.core.ui

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import ireader.presentation.core.LocalNavigator
import ireader.presentation.core.safePopBackStack
import ireader.presentation.ui.settings.admin.AdminUserPanelScreen
import ireader.presentation.ui.settings.admin.AdminUserPanelViewModel

/**
 * Screen specification for the Admin User Panel - allows admins to manage users,
 * assign badges, and reset passwords.
 */
class AdminUserPanelScreenSpec {
    
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Content() {
        val navController = requireNotNull(LocalNavigator.current) { "LocalNavigator not provided" }
        val vm: AdminUserPanelViewModel = getIViewModel()
        
        AdminUserPanelScreen(
            viewModel = vm,
            onNavigateBack = {
                navController.safePopBackStack()
            }
        )
    }
}
