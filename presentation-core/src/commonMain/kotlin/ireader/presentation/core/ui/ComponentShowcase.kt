package ireader.presentation.core.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Component showcase demonstrating the enhanced UI components.
 * This is for demonstration purposes and shows how to use the new components.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComponentShowcase() {
    IReaderScaffold(
        topBar = { scrollBehavior ->
            AppBar(
                title = { Text("Component Showcase") },
                navigationIcon = {
                    ActionButton(
                        title = "Back",
                        icon = Icons.Default.Home,
                        onClick = { }
                    )
                },
                actions = {
                    ActionButton(
                        title = "Settings",
                        icon = Icons.Default.Settings,
                        onClick = { }
                    )
                },
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { },
                icon = { androidx.compose.material3.Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Add") }
            )
        }
    ) { paddingValues ->
        IReaderFastScrollLazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                IReaderElevatedCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Card Example")
                        Text("This is an elevated card with proper Material Design 3 styling.")
                    }
                }
            }
            
            item {
                SimpleListItem(
                    title = "List Item Example",
                    subtitle = "This shows how to use the enhanced list items",
                    icon = Icons.Default.Favorite,
                    onClick = { }
                )
            }
            
            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Pills Example:")
                    Pill(text = "Primary")
                    OutlinedPill(text = "Outlined")
                    SmallPill(text = "Small")
                }
            }
            
            item {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Buttons Example:")
                    ActionButton(
                        title = "Action Button",
                        icon = Icons.Default.Refresh,
                        onClick = { }
                    )
                    OutlinedActionButton(
                        title = "Outlined Button",
                        icon = Icons.Default.Settings,
                        onClick = { }
                    )
                }
            }
        }
    }
}

/**
 * Error screen showcase
 */
@Composable
fun ErrorScreenShowcase() {
    IReaderErrorScreen(
        message = "Something went wrong. Please try again.",
        actions = listOf(
            ErrorScreenAction(
                title = "Retry",
                icon = Icons.Default.Refresh,
                onClick = { }
            ),
            ErrorScreenAction(
                title = "Go Home",
                icon = Icons.Default.Home,
                onClick = { }
            )
        )
    )
}

/**
 * Empty screen showcase
 */
@Composable
fun EmptyScreenShowcase() {
    IReaderEmptyScreen(
        message = "No items found. Try adding some content.",
        actions = listOf(
            EmptyScreenAction(
                title = "Add Item",
                icon = Icons.Default.Add,
                onClick = { }
            ),
            EmptyScreenAction(
                title = "Refresh",
                icon = Icons.Default.Refresh,
                onClick = { }
            )
        )
    )
}

/**
 * Loading screen showcase
 */
@Composable
fun LoadingScreenShowcase() {
    IReaderLoadingScreen(
        progress = 0.7f,
        message = "Loading content..."
    )
}