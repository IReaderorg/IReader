package ireader.presentation.core.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ireader.presentation.core.LocalNavigator
import ireader.presentation.core.NavigationRoutes
import ireader.presentation.ui.component.IScaffold
import ireader.presentation.ui.featurestore.FeatureStoreScreen
import ireader.presentation.ui.featurestore.FeatureStoreViewModel

/**
 * Screen specification for the Feature Store - plugin monetization marketplace
 */
class FeatureStoreScreenSpec {
    
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Content() {
        val navController = requireNotNull(LocalNavigator.current) { "LocalNavigator not provided" }
        val viewModel: FeatureStoreViewModel = getIViewModel()
        
        IScaffold { padding ->
            FeatureStoreScreen(
                viewModel = viewModel,
                modifier = Modifier.padding(padding),
                onNavigateBack = {
                    navController.popBackStack()
                },
                onPluginClick = { pluginId ->
                    // Navigate to plugin details screen
                    navController.navigate("pluginDetails/$pluginId")
                }
            )
        }
    }
}
