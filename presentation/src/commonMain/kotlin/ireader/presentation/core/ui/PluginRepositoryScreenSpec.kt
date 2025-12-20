package ireader.presentation.core.ui

import androidx.compose.runtime.Composable
import ireader.presentation.core.LocalNavigator
import ireader.presentation.ui.pluginrepository.PluginRepositoryScreen
import ireader.presentation.ui.pluginrepository.PluginRepositoryViewModel
import ireader.presentation.core.safePopBackStack
/**
 * Screen specification for Plugin Repository management
 */
class PluginRepositoryScreenSpec {
    
    @Composable
    fun Content() {
        val navController = LocalNavigator.current
        val viewModel: PluginRepositoryViewModel = getIViewModel()
        
        PluginRepositoryScreen(
            viewModel = viewModel,
            onNavigateBack = {
                navController?.safePopBackStack()
            }
        )
    }
}
