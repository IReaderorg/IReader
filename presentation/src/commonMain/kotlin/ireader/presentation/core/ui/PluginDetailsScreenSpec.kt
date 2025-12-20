package ireader.presentation.core.ui

import androidx.compose.runtime.Composable
import ireader.presentation.core.LocalNavigator
import ireader.presentation.ui.plugins.details.PluginDetailsScreen
import ireader.presentation.ui.plugins.details.PluginDetailsViewModel
import org.koin.core.parameter.parametersOf
import ireader.presentation.core.safePopBackStack
/**
 * Screen specification for Plugin Details - shows detailed plugin information
 */
data class PluginDetailsScreenSpec(
    val pluginId: String
) {
    
    @Composable
    fun Content() {
        val navController = LocalNavigator.current
        val viewModel: PluginDetailsViewModel = getIViewModel(
            key = pluginId,
            parameters = { parametersOf(pluginId) }
        )
        
        PluginDetailsScreen(
            viewModel = viewModel,
            onNavigateBack = {
                navController?.safePopBackStack()
            },
            onPluginClick = { otherPluginId ->
                // Navigate to another plugin's details
                navController?.navigate("pluginDetails/$otherPluginId")
            }
        )
    }
}
