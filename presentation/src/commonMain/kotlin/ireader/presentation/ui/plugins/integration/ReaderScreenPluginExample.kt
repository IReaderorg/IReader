package ireader.presentation.ui.plugins.integration

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController

/**
 * Example integration of feature plugins into the reader screen
 * This file demonstrates how to integrate plugin menu items and handle reader context
 * 
 * Requirements: 6.1, 6.2, 6.3
 */

/**
 * Example: Adding plugin menu items to reader overflow menu
 */
@Composable
fun ReaderOverflowMenuWithPlugins(
    bookId: Long,
    chapterId: Long,
    currentPosition: Int,
    selectedText: String?,
    featurePluginIntegration: FeaturePluginIntegration,
    navController: NavHostController,
    expanded: Boolean,
    onDismiss: () -> Unit,
    // Regular menu items
    onSettingsClick: () -> Unit,
    onBookmarkClick: () -> Unit,
    // ... other menu items
) {
    val scope = rememberCoroutineScope()
    
    // Get plugin menu items
    val pluginMenuItems = remember(featurePluginIntegration) {
        featurePluginIntegration.getPluginMenuItems()
    }
    
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss
    ) {
        // Regular menu items
        DropdownMenuItem(
            text = { Text("Settings") },
            onClick = {
                onSettingsClick()
                onDismiss()
            }
        )
        
        DropdownMenuItem(
            text = { Text("Bookmark") },
            onClick = {
                onBookmarkClick()
                onDismiss()
            }
        )
        
        // Add plugin menu items if any exist
        if (pluginMenuItems.isNotEmpty()) {
            // Divider
            androidx.compose.material3.Divider()
            
            // Plugin menu items
            ReaderPluginMenuIntegration.PluginMenuItems(
                menuItems = pluginMenuItems,
                navController = navController,
                scope = scope,
                onDismiss = onDismiss
            )
        }
    }
}

/**
 * Example: Handling reader context changes
 */
@Composable
fun ReaderScreenWithPluginIntegration(
    bookId: Long,
    chapterId: Long,
    featurePluginIntegration: FeaturePluginIntegration,
    navController: NavHostController
) {
    val scope = rememberCoroutineScope()
    var currentPosition by remember { mutableStateOf(0) }
    var selectedText by remember { mutableStateOf<String?>(null) }
    
    // Create plugin integration state
    val pluginState = rememberReaderPluginIntegration(
        featurePluginIntegration = featurePluginIntegration,
        bookId = bookId,
        chapterId = chapterId,
        currentPosition = currentPosition,
        selectedText = selectedText,
        navController = navController,
        scope = scope
    )
    
    // Your reader UI
    Column {
        // Reader content...
        
        // When text is selected, notify plugins
        // This would be called from your text selection handler
        // pluginState.contextHandler.onTextSelection(...)
        
        // When chapter changes, notify plugins
        // This would be called from your chapter navigation handler
        // pluginState.contextHandler.onChapterChange(...)
    }
}

/**
 * Example: Using plugin data storage
 */
@Composable
fun PluginDataStorageExample(
    pluginId: String,
    featurePluginIntegration: FeaturePluginIntegration
) {
    val dataStore = remember(pluginId) {
        featurePluginIntegration.getPluginDataStorage(pluginId)
    }
    
    // Example: Save and retrieve data
    // In a real implementation, this would be done in a ViewModel or use case
    /*
    LaunchedEffect(Unit) {
        // Save data
        dataStore.putString("last_action", "bookmark_created")
        dataStore.putInt("action_count", 5)
        
        // Retrieve data
        val lastAction = dataStore.getString("last_action")
        val actionCount = dataStore.getInt("action_count")
        
        // Observe data changes
        dataStore.observeString("last_action").collect { action ->
            // Handle changes
        }
    }
    */
}

/**
 * Example: Navigating to plugin screens
 */
fun navigateToPluginScreen(
    navController: NavHostController,
    pluginScreenRoute: String
) {
    navController.navigateToPluginScreen(pluginScreenRoute)
}

/**
 * Example: Checking if current route is a plugin screen
 */
@Composable
fun CurrentScreenInfo(
    navController: NavHostController,
    featurePluginIntegration: FeaturePluginIntegration
) {
    val currentRoute = navController.currentBackStackEntry?.destination?.route
    val isPlugin = isPluginScreen(currentRoute, featurePluginIntegration)
    
    if (isPlugin) {
        Text("Currently viewing a plugin screen")
    }
}
