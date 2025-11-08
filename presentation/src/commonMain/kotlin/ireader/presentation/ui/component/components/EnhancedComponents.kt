package ireader.presentation.ui.component.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ireader.presentation.ui.core.theme.ContentAlpha
import ireader.presentation.ui.core.utils.horizontalPadding

/**
 * Enhanced preference row component with improved styling, flexibility, and accessibility.
 * 
 * This component provides a consistent way to display preference items with:
 * - Support for leading icons (ImageVector or Painter)
 * - Optional subtitle text
 * - Customizable trailing content
 * - Proper touch feedback with ripple effects
 * - Full accessibility support with content descriptions and semantic roles
 * - Material Design 3 styling
 * 
 * @param title The main text to display
 * @param subtitle Optional secondary text displayed below the title
 * @param icon Optional leading icon as ImageVector
 * @param painter Optional leading icon as Painter (takes precedence over icon if both provided)
 * @param onClick Callback invoked when the row is clicked
 * @param onLongClick Callback invoked when the row is long-clicked
 * @param trailing Optional composable content displayed at the end of the row
 * @param enabled Whether the row is enabled and interactive
 * @param modifier Modifier for the row container
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RowPreference(
    title: String,
    subtitle: String? = null,
    icon: ImageVector? = null,
    painter: Painter? = null,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {},
    trailing: @Composable (() -> Unit)? = null,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    // Calculate height based on whether subtitle is present
    val minHeight = if (subtitle != null) 72.dp else 56.dp
    
    // Build content description for accessibility
    val contentDesc = buildString {
        append(title)
        if (subtitle != null) {
            append(". ")
            append(subtitle)
        }
    }
    
    // Create interaction source for ripple effect
    val interactionSource = remember { MutableInteractionSource() }
    
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = minHeight)
            .semantics {
                contentDescription = contentDesc
                role = Role.Button
            }
            .combinedClickable(
                enabled = enabled,
                onClick = onClick,
                onLongClick = onLongClick,
                interactionSource = interactionSource,
            ),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Leading icon
            if (painter != null) {
                Icon(
                    painter = painter,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(horizontal = horizontalPadding)
                        .size(24.dp),
                    tint = if (enabled) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = ContentAlpha.disabled())
                    }
                )
            } else if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(horizontal = horizontalPadding)
                        .size(24.dp),
                    tint = if (enabled) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = ContentAlpha.disabled())
                    }
                )
            }
            
            // Title and subtitle column
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(
                        start = if (painter == null && icon == null) horizontalPadding else 0.dp,
                        end = if (trailing == null) horizontalPadding else 8.dp
                    ),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (enabled) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = ContentAlpha.disabled())
                    },
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                if (subtitle != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (enabled) {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        } else {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = ContentAlpha.disabled())
                        },
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            
            // Trailing content
            if (trailing != null) {
                Box(
                    modifier = Modifier
                        .widthIn(min = 56.dp, max = 250.dp)
                        .padding(end = horizontalPadding),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    trailing()
                }
            }
        }
    }
}

/**
 * Section header component for grouping related preferences.
 * 
 * @param text The header text to display
 * @param icon Optional leading icon
 * @param modifier Modifier for the header
 */
@Composable
fun SectionHeader(
    text: String,
    icon: ImageVector? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = horizontalPadding,
                vertical = 12.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier
                    .size(20.dp)
                    .padding(end = 8.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

/**
 * Enhanced card component with Material Design 3 styling.
 * 
 * @param modifier Modifier for the card
 * @param onClick Optional click handler
 * @param content The content to display inside the card
 */
@Composable
fun EnhancedCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick ?: {},
        enabled = onClick != null,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            content = content
        )
    }
}

/**
 * Preview functions for the enhanced components.
 * These demonstrate various states and configurations.
 */

// Note: @Preview annotations are typically used in Android-specific code
// For multiplatform projects, these serve as example usage patterns

/**
 * Example usage of RowPreference with icon and subtitle
 */
@Composable
fun RowPreferenceExample() {
    Column {
        RowPreference(
            title = "Theme",
            subtitle = "Choose your preferred theme",
            icon = androidx.compose.material.icons.Icons.Default.Palette,
            onClick = { /* Handle click */ }
        )
        
        RowPreference(
            title = "Notifications",
            subtitle = "Manage notification settings",
            icon = androidx.compose.material.icons.Icons.Default.Notifications,
            onClick = { /* Handle click */ },
            trailing = {
                Switch(
                    checked = true,
                    onCheckedChange = { /* Handle change */ }
                )
            }
        )
        
        RowPreference(
            title = "Disabled Option",
            subtitle = "This option is currently disabled",
            icon = androidx.compose.material.icons.Icons.Default.Block,
            enabled = false,
            onClick = { /* Handle click */ }
        )
    }
}

/**
 * Example usage of SectionHeader
 */
@Composable
fun SectionHeaderExample() {
    Column {
        SectionHeader(
            text = "Appearance",
            icon = androidx.compose.material.icons.Icons.Default.Palette
        )
        
        SectionHeader(
            text = "General Settings"
        )
    }
}

/**
 * Example usage of EnhancedCard
 */
@Composable
fun EnhancedCardExample() {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        EnhancedCard {
            Text(
                text = "Card Title",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "This is an example of an enhanced card with Material Design 3 styling.",
                style = MaterialTheme.typography.bodyMedium
            )
        }
        
        EnhancedCard(
            onClick = { /* Handle click */ }
        ) {
            Text(
                text = "Clickable Card",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "This card can be clicked.",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
