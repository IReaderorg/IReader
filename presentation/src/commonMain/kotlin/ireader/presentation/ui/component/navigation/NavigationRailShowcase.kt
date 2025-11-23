package ireader.presentation.ui.component.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.unit.dp

/**
 * Showcase composable to demonstrate NavigationRail vs BottomNavigation
 * Useful for testing and comparing different navigation styles
 */
@Composable
fun NavigationRailShowcase() {
    var selectedIndex by remember { mutableStateOf(0) }
    var useRail by remember { mutableStateOf(true) }
    
    val items = listOf(
        ShowcaseItem("Library", rememberVectorPainter(Icons.Default.Home)),
        ShowcaseItem("Updates", rememberVectorPainter(Icons.Default.Notifications)),
        ShowcaseItem("History", rememberVectorPainter(Icons.Default.DateRange)),
        ShowcaseItem("Browse", rememberVectorPainter(Icons.Default.Search)),
        ShowcaseItem("More", rememberVectorPainter(Icons.Default.Menu))
    )
    
    Column(modifier = Modifier.fillMaxSize()) {
        // Toggle switch
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (useRail) "Navigation Rail" else "Bottom Navigation",
                style = MaterialTheme.typography.titleMedium
            )
            Switch(
                checked = useRail,
                onCheckedChange = { useRail = it }
            )
        }
        
        Divider()
        
        // Navigation showcase
        if (useRail) {
            Row(modifier = Modifier.fillMaxSize()) {
                Material3NavigationRail(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ) {
                    items.forEachIndexed { index, item ->
                        Material3NavigationRailItem(
                            selected = selectedIndex == index,
                            onClick = { selectedIndex = index },
                            icon = item.icon,
                            label = item.label,
                            alwaysShowLabel = false
                        )
                    }
                }
                
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Selected: ${items[selectedIndex].label}",
                        style = MaterialTheme.typography.headlineMedium
                    )
                }
            }
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Selected: ${items[selectedIndex].label}",
                        style = MaterialTheme.typography.headlineMedium
                    )
                }
                
                ModernBottomNavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ) {
                    items.forEachIndexed { index, item ->
                        ModernNavigationItem(
                            selected = selectedIndex == index,
                            onClick = { selectedIndex = index },
                            icon = item.icon,
                            label = item.label,
                            alwaysShowLabel = true
                        )
                    }
                }
            }
        }
    }
}

private data class ShowcaseItem(
    val label: String,
    val icon: Painter
)

/**
 * Comparison view showing both navigation styles side by side
 */
@Composable
fun NavigationComparisonView() {
    var selectedRail by remember { mutableStateOf(0) }
    var selectedBottom by remember { mutableStateOf(0) }
    
    val items = listOf(
        ShowcaseItem("Library", rememberVectorPainter(Icons.Default.Home)),
        ShowcaseItem("Updates", rememberVectorPainter(Icons.Default.Notifications)),
        ShowcaseItem("Browse", rememberVectorPainter(Icons.Default.Search)),
        ShowcaseItem("More", rememberVectorPainter(Icons.Default.Menu))
    )
    
    Row(modifier = Modifier.fillMaxSize()) {
        // Navigation Rail side
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            Text(
                text = "Navigation Rail",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(16.dp)
            )
            
            Row(modifier = Modifier.fillMaxSize()) {
                Material3NavigationRail {
                    items.forEachIndexed { index, item ->
                        Material3NavigationRailItem(
                            selected = selectedRail == index,
                            onClick = { selectedRail = index },
                            icon = item.icon,
                            label = item.label
                        )
                    }
                }
                
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(items[selectedRail].label)
                }
            }
        }
        
        VerticalDivider()
        
        // Bottom Navigation side
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            Text(
                text = "Bottom Navigation",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(16.dp)
            )
            
            Column(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(items[selectedBottom].label)
                }
                
                ModernBottomNavigationBar {
                    items.forEachIndexed { index, item ->
                        ModernNavigationItem(
                            selected = selectedBottom == index,
                            onClick = { selectedBottom = index },
                            icon = item.icon,
                            label = item.label,
                            alwaysShowLabel = true
                        )
                    }
                }
            }
        }
    }
}
