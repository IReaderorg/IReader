package ireader.presentation.ui.component.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter

/**
 * Navigation layout modes
 */
enum class NavigationLayoutMode {
    BOTTOM_BAR,      // Bottom navigation bar (default for phones)
    NAVIGATION_RAIL, // Side navigation rail (default for tablets)
    AUTO             // Automatically choose based on screen size
}

/**
 * Adaptive navigation that switches between bottom bar and navigation rail
 * based on screen size or user preference
 */
@Composable
fun AdaptiveNavigationLayout(
    mode: NavigationLayoutMode = NavigationLayoutMode.AUTO,
    isTablet: Boolean,
    showNavigation: Boolean = true,
    bottomBarContent: @Composable RowScope.() -> Unit,
    navigationRailContent: @Composable ColumnScope.() -> Unit,
    content: @Composable (PaddingValues) -> Unit
) {
    val useNavigationRail = when (mode) {
        NavigationLayoutMode.BOTTOM_BAR -> false
        NavigationLayoutMode.NAVIGATION_RAIL -> true
        NavigationLayoutMode.AUTO -> isTablet
    }

    if (useNavigationRail) {
        Row(modifier = Modifier.fillMaxSize()) {
            if (showNavigation) {
                Material3NavigationRail(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    content = navigationRailContent
                )
            }
            Box(modifier = Modifier.weight(1f)) {
                content(PaddingValues())
            }
        }
    } else {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.weight(1f)) {
                content(PaddingValues())
            }
            if (showNavigation) {
                ModernBottomNavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    content = bottomBarContent
                )
            }
        }
    }
}

/**
 * Helper to create navigation items that work in both bottom bar and rail
 */
data class AdaptiveNavigationItem(
    val icon: Painter,
    val label: String,
    val selected: Boolean,
    val onClick: () -> Unit,
    val enabled: Boolean = true
)

@Composable
fun RowScope.BottomNavigationItem(item: AdaptiveNavigationItem) {
    ModernNavigationItem(
        selected = item.selected,
        onClick = item.onClick,
        icon = item.icon,
        label = item.label,
        enabled = item.enabled,
        alwaysShowLabel = true
    )
}

@Composable
fun RailNavigationItem(item: AdaptiveNavigationItem) {
    Material3NavigationRailItem(
        selected = item.selected,
        onClick = item.onClick,
        icon = item.icon,
        label = item.label,
        enabled = item.enabled,
        alwaysShowLabel = false
    )
}
