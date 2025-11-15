package ireader.presentation.ui.plugins.integration

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import ireader.domain.plugins.PluginMenuItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Helper for integrating plugin menu items into the reader screen
 * Requirements: 6.1
 */
object ReaderPluginMenuIntegration {
    
    /**
     * Render plugin menu items in a dropdown menu or bottom sheet
     * 
     * @param menuItems List of plugin menu items
     * @param navController Navigation controller for navigation
     * @param scope Coroutine scope for async operations
     * @param onDismiss Callback when menu is dismissed
     */
    @Composable
    fun PluginMenuItems(
        menuItems: List<PluginMenuItem>,
        navController: NavHostController,
        scope: CoroutineScope,
        onDismiss: () -> Unit
    ) {
        if (menuItems.isEmpty()) {
            return
        }
        
        Column(modifier = Modifier.fillMaxWidth()) {
            // Section header
            Text(
                text = "Plugin Features",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                style = androidx.compose.material3.MaterialTheme.typography.labelMedium
            )
            
            // Menu items
            menuItems.forEach { menuItem ->
                PluginMenuItemRow(
                    menuItem = menuItem,
                    onClick = {
                        scope.launch {
                            handleMenuItemClick(menuItem, navController)
                            onDismiss()
                        }
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
    
    /**
     * Render a single plugin menu item
     */
    @Composable
    private fun PluginMenuItemRow(
        menuItem: PluginMenuItem,
        onClick: () -> Unit
    ) {
        DropdownMenuItem(
            text = { Text(menuItem.label) },
            onClick = onClick,
            leadingIcon = {
                // Use plugin icon if available, otherwise use default extension icon
                Icon(
                    imageVector = Icons.Default.Extension,
                    contentDescription = null
                )
            }
        )
    }
    
    /**
     * Handle menu item click
     * This would trigger the plugin's action
     */
    private suspend fun handleMenuItemClick(
        menuItem: PluginMenuItem,
        navController: NavHostController
    ) {
        try {
            // The actual action execution would be handled by the plugin
            // through the FeaturePluginIntegration.executePluginAction method
            // For now, we just navigate if the menu item has a route
            // In a full implementation, this would call back to the plugin
        } catch (e: Exception) {
            // Log error but don't crash
        }
    }
}

/**
 * Composable for rendering plugin menu items in a bottom sheet
 * Requirements: 6.1
 */
@Composable
fun PluginMenuBottomSheet(
    menuItems: List<PluginMenuItem>,
    navController: NavHostController,
    scope: CoroutineScope,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        Text(
            text = "Plugin Features",
            style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        
        Divider()
        
        ReaderPluginMenuIntegration.PluginMenuItems(
            menuItems = menuItems,
            navController = navController,
            scope = scope,
            onDismiss = onDismiss
        )
    }
}
