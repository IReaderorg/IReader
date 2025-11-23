package ireader.presentation.ui.component.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Showcase screen to preview and compare different navigation bar styles
 * This is optional and can be added to your settings or debug menu
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavigationStyleShowcase(
    onBackClick: () -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf(0) }
    var currentStyle by remember { mutableStateOf(NavigationStyle.MODERN) }
    
    val tabs = listOf(
        NavigationItem(
            label = "Library",
            icon = Icons.Default.Home,
            selectedIcon = Icons.Default.Home
        ),
        NavigationItem(
            label = "Updates",
            icon = Icons.Default.Notifications,
            selectedIcon = Icons.Default.Notifications
        ),
        NavigationItem(
            label = "History",
            icon = Icons.Default.DateRange,
            selectedIcon = Icons.Default.DateRange
        ),
        NavigationItem(
            label = "Browse",
            icon = Icons.Default.Search,
            selectedIcon = Icons.Default.Search
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Navigation Styles Showcase") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Style selector
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Select Navigation Style",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    NavigationStyle.entries.forEach { style ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = currentStyle == style,
                                onClick = { currentStyle = style }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = style.displayName,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(
                                    text = style.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Preview section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(500.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Content area
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Bounds checking to prevent IndexOutOfBoundsException
                        val safeSelectedTab = selectedTab.coerceIn(0, tabs.lastIndex)
                        
                        Icon(
                            imageVector = tabs[safeSelectedTab].icon,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = tabs[safeSelectedTab].label,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Current Style: ${currentStyle.displayName}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Navigation bar preview
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                    ) {
                        when (currentStyle) {
                            NavigationStyle.CLASSIC -> {
                                NavigationBar {
                                    tabs.forEachIndexed { index, item ->
                                        NavigationBarItem(
                                            selected = selectedTab == index,
                                            onClick = { selectedTab = index },
                                            icon = {
                                                Icon(
                                                    painter = rememberVectorPainter(
                                                        if (selectedTab == index) 
                                                            item.selectedIcon else item.icon
                                                    ),
                                                    contentDescription = item.label
                                                )
                                            },
                                            label = { Text(item.label) }
                                        )
                                    }
                                }
                            }
                            NavigationStyle.MODERN -> {
                                ModernBottomNavigationBar {
                                    tabs.forEachIndexed { index, item ->
                                        ModernNavigationItem(
                                            selected = selectedTab == index,
                                            onClick = { selectedTab = index },
                                            icon = rememberVectorPainter(item.icon),
                                            label = item.label
                                        )
                                    }
                                }
                            }
                            NavigationStyle.FLOATING, NavigationStyle.COMPACT -> {
                                // Placeholder for floating navigation
                                ModernBottomNavigationBar {
                                    tabs.forEachIndexed { index, item ->
                                        ModernNavigationItem(
                                            selected = selectedTab == index,
                                            onClick = { selectedTab = index },
                                            icon = rememberVectorPainter(item.icon),
                                            label = item.label
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Features comparison
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Style Features",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    StyleFeature("Original", "Standard Material 3 design", "✓ Simple and clean")
                    StyleFeature("Modern", "Enhanced with animations", "✓ Smooth transitions\n✓ Better visual hierarchy\n✓ Rounded corners")
                    StyleFeature("Floating", "Premium floating design", "✓ Pill-shaped container\n✓ Gradient backgrounds\n✓ Scale animations\n✓ Compact layout")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun StyleFeature(
    title: String,
    subtitle: String,
    features: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = features,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        if (title != "Floating") {
            Divider(modifier = Modifier.padding(top = 8.dp))
        }
    }
}

data class NavigationItem(
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val selectedIcon: androidx.compose.ui.graphics.vector.ImageVector
)
