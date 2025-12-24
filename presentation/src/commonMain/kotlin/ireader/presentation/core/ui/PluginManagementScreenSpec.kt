package ireader.presentation.core.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ireader.presentation.core.LocalNavigator
import ireader.presentation.core.safePopBackStack
import ireader.presentation.ui.component.IScaffold
import ireader.presentation.ui.plugins.management.PluginManagementScreen
import ireader.presentation.ui.plugins.management.PluginManagementViewModel

/**
 * Screen specification for Plugin Management - manage installed plugins
 */
class PluginManagementScreenSpec {
    
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Content() {
        val navController = LocalNavigator.current
        val viewModel: PluginManagementViewModel = getIViewModel()
        
        PluginManagementScreen(
            viewModel = viewModel,
            onNavigateBack = {
                navController?.safePopBackStack()
            }
        )
    }
}
