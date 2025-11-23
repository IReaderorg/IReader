package ireader.presentation.ui.component.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.tab.Tab

/**
 * Navigation style options
 */
enum class NavigationStyle(val displayName: String, val description: String) {
    CLASSIC("Classic", "Original Material 3 navigation"),
    MODERN("Modern", "Modern with rounded corners and animations"),
    FLOATING("Floating", "Floating pill-shaped navigation"),
    COMPACT("Compact", "Compact floating with icon-only default")
}

/**
 * Adaptive navigation bar that switches between styles
 */
@Composable
fun AdaptiveBottomNavigationBar(
    style: NavigationStyle = NavigationStyle.MODERN,
    modifier: Modifier = Modifier,
    containerColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.surface,
    contentColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface,
    content: @Composable RowScope.() -> Unit
) {
    when (style) {
        NavigationStyle.CLASSIC -> {
            NavigationBar(
                modifier = modifier,
                containerColor = containerColor,
                contentColor = contentColor,
                tonalElevation = 3.dp,
                content = content
            )
        }
        NavigationStyle.MODERN -> {
            ModernBottomNavigationBar(
                modifier = modifier,
                containerColor = containerColor,
                contentColor = contentColor,
                content = content
            )
        }
        NavigationStyle.FLOATING, NavigationStyle.COMPACT -> {
            FloatingBottomNavigationBar(
                modifier = modifier,
                containerColor = if (style == NavigationStyle.FLOATING) {
                    MaterialTheme.colorScheme.surfaceContainerHigh
                } else {
                    containerColor
                },
                contentColor = contentColor,
                content = content
            )
        }
    }
}

/**
 * Adaptive navigation item that switches between styles
 */
@Composable
fun RowScope.AdaptiveNavigationItem(
    style: NavigationStyle,
    selected: Boolean,
    onClick: () -> Unit,
    icon: Painter,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    alwaysShowLabel: Boolean = true
) {
    when (style) {
        NavigationStyle.CLASSIC -> {
            NavigationBarItem(
                selected = selected,
                onClick = onClick,
                icon = {
                    Icon(
                        painter = icon,
                        contentDescription = label
                    )
                },
                label = {
                    Text(
                        text = label,
                        maxLines = 1
                    )
                },
                modifier = modifier,
                enabled = enabled,
                alwaysShowLabel = alwaysShowLabel
            )
        }
        NavigationStyle.MODERN -> {
            ModernNavigationItem(
                selected = selected,
                onClick = onClick,
                icon = icon,
                label = label,
                modifier = modifier,
                enabled = enabled,
                alwaysShowLabel = alwaysShowLabel
            )
        }
        NavigationStyle.FLOATING -> {
            FloatingNavigationItem(
                selected = selected,
                onClick = onClick,
                icon = icon,
                label = label,
                modifier = modifier,
                enabled = enabled,
                showLabel = alwaysShowLabel
            )
        }
        NavigationStyle.COMPACT -> {
            CompactFloatingNavigationItem(
                selected = selected,
                onClick = onClick,
                icon = icon,
                label = label,
                modifier = modifier,
                enabled = enabled
            )
        }
    }
}

/**
 * Helper composable for Tab-based navigation with style selection
 */
@Composable
fun RowScope.AdaptiveTabNavigationItem(
    tab: Tab,
    style: NavigationStyle,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val icon = tab.options.icon
    if (icon != null) {
        AdaptiveNavigationItem(
            style = style,
            selected = isSelected,
            onClick = onClick,
            icon = icon,
            label = tab.options.title,
            alwaysShowLabel = true
        )
    }
}

/**
 * Preview/Demo composable showing all navigation styles
 */
@Composable
fun NavigationStylePreview(
    currentStyle: NavigationStyle,
    onStyleSelected: (NavigationStyle) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Navigation Style",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        NavigationStyle.entries.forEach { style ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                RadioButton(
                    selected = currentStyle == style,
                    onClick = { onStyleSelected(style) }
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
