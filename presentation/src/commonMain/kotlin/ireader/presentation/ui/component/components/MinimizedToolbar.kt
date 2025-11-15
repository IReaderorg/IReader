package ireader.presentation.ui.component.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import ireader.presentation.core.theme.ToolbarDimensions

/**
 * A minimized toolbar component designed for immersive experiences like WebView screens.
 * 
 * This toolbar uses a reduced height (48dp vs standard 64dp) to maximize content viewing area
 * while maintaining all functionality and ensuring touch targets remain accessible.
 * 
 * Key features:
 * - Reduced height (48dp) provides ~2.5% more vertical space on typical phone screens
 * - Smaller visual elements (20dp icons, titleMedium typography)
 * - Maintains minimum 48dp touch targets for accessibility
 * - Supports scroll behavior for collapsing/expanding
 * - Consistent color scheme with standard toolbars
 * 
 * @param title The title text to display in the toolbar
 * @param navigationIcon Optional composable for the navigation icon (typically a back button)
 * @param actions Optional composable for action buttons in the toolbar
 * @param scrollBehavior Optional scroll behavior for collapsing/expanding the toolbar
 * @param modifier Optional modifier for the toolbar
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MinimizedToolbar(
    title: String,
    navigationIcon: @Composable (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    scrollBehavior: TopAppBarScrollBehavior? = null,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        navigationIcon = navigationIcon ?: {},
        actions = actions,
        scrollBehavior = scrollBehavior,
        modifier = modifier.height(ToolbarDimensions.MinimizedHeight),
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
            actionIconContentColor = MaterialTheme.colorScheme.onSurface
        )
    )
}
