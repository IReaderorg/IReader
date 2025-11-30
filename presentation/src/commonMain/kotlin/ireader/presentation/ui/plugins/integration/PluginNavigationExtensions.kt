@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package ireader.presentation.ui.plugins.integration

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import ireader.domain.plugins.PluginScreen
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.i18n.resources.*

/**
 * Navigation extensions for plugin screen registration
 * Requirements: 6.2
 */
object PluginNavigationExtensions {
    
    /**
     * Register all plugin screens with the navigation graph
     * 
     * @param navGraphBuilder Navigation graph builder
     * @param featurePluginIntegration Feature plugin integration instance
     */
    fun registerPluginScreens(
        navGraphBuilder: NavGraphBuilder,
        featurePluginIntegration: FeaturePluginIntegration
    ) {
        try {
            val screens = featurePluginIntegration.getPluginScreens()
            
            screens.forEach { screen ->
                registerPluginScreen(navGraphBuilder, screen)
            }
        } catch (e: Exception) {
            // Log error but don't disrupt navigation setup
        }
    }
    
    /**
     * Register a single plugin screen
     * 
     * @param navGraphBuilder Navigation graph builder
     * @param screen Plugin screen to register
     */
    private fun registerPluginScreen(
        navGraphBuilder: NavGraphBuilder,
        screen: PluginScreen
    ) {
        try {
            navGraphBuilder.composable(screen.route) {
                PluginScreenWrapper(screen)
            }
        } catch (e: Exception) {
            // Log error but continue with other screens
        }
    }
    
    /**
     * Wrapper composable for plugin screens with error handling
     * Requirements: 6.4
     */
    @Composable
    private fun PluginScreenWrapper(screen: PluginScreen) {
        // Cast the content to a Composable function outside of try-catch
        val composableContent = screen.content as? @Composable () -> Unit
        
        if (composableContent != null) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text(screen.title) }
                    )
                }
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    // Composable invocation cannot be wrapped in try-catch
                    // Error handling should be done at a higher level or within the plugin content itself
                    composableContent()
                }
            }
        } else {
            PluginScreenError("Invalid plugin screen content")
        }
    }
    
    /**
     * Error screen for plugin loading failures
     * Requirements: 6.4
     */
    @Composable
    private fun PluginScreenError(message: String) {
        val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(localizeHelper.localize(Res.string.plugin_error)) }
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

/**
 * Extension function to navigate to a plugin screen
 * Requirements: 6.2
 * 
 * @param route Plugin screen route
 */
fun NavHostController.navigateToPluginScreen(route: String) {
    try {
        navigate(route) {
            // Proper back stack handling
            launchSingleTop = true
            restoreState = true
        }
    } catch (e: Exception) {
        // Log error but don't crash
    }
}

/**
 * Extension function to check if a route is a plugin screen
 * 
 * @param route Route to check
 * @param featurePluginIntegration Feature plugin integration instance
 * @return True if the route belongs to a plugin screen
 */
fun isPluginScreen(
    route: String?,
    featurePluginIntegration: FeaturePluginIntegration
): Boolean {
    if (route == null) return false
    
    return try {
        val screens = featurePluginIntegration.getPluginScreens()
        screens.any { it.route == route }
    } catch (e: Exception) {
        false
    }
}
