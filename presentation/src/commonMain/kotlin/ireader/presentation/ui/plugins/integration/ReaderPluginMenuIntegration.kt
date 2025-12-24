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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import ireader.domain.plugins.PluginMenuItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.i18n.resources.*
import ireader.i18n.resources.Res

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
        val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
        if (menuItems.isEmpty()) {
            return
        }
        
        Column(modifier = Modifier.fillMaxWidth()) {
            // Section header
            Text(
                text = localizeHelper.localize(Res.string.plugin_features),
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
     * This triggers the plugin's action - navigates to plugin screen if route is available
     */
    private suspend fun handleMenuItemClick(
        menuItem: PluginMenuItem,
        navController: NavHostController
    ) {
        try {
            // Navigate to the plugin screen if a route is defined
            // Plugin menu items can define a route like "plugin/reading-stats/main"
            val route = menuItem.route
            if (!route.isNullOrBlank()) {
                navController.navigate(route)
            }
        } catch (e: Exception) {
            // Log error but don't crash
            println("[ReaderPluginMenuIntegration] Failed to handle menu item click: ${e.message}")
        }
    }
}

/**
 * Composable for rendering plugin menu items in a bottom sheet
 * Requirements: 6.1
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PluginMenuBottomSheet(
    menuItems: List<PluginMenuItem>,
    navController: NavHostController,
    scope: CoroutineScope,
    onDismiss: () -> Unit
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    val sheetState = rememberModalBottomSheetState()
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Text(
                text = localizeHelper.localize(Res.string.plugin_features),
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
            
            // Add some bottom padding for navigation bar
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
