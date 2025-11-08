package ireader.presentation.ui.component.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ireader.presentation.ui.core.theme.ContentAlpha
import ireader.presentation.ui.core.utils.horizontalPadding

/**
 * Enhanced UI Components Library
 * 
 * This file contains reusable, enhanced UI components following Material Design 3 principles.
 * All components are designed with:
 * - Consistent styling and spacing
 * - Full accessibility support
 * - Proper touch feedback
 * - Clean code principles
 * 
 * Components included:
 * - [RowPreference]: Enhanced preference row with flexible layout options
 * - [SectionHeader]: Section header for grouping related preferences
 * - [EnhancedCard]: Material Design 3 styled card component
 * - [PreferenceDivider]: Divider for separating preference groups
 * - [NavigationRowPreference]: Preference row with navigation indicator
 */

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
 * - Minimum touch target size of 48dp for accessibility
 * 
 * The component automatically adjusts its height based on content:
 * - 56dp minimum when only title is present
 * - 72dp minimum when subtitle is included
 * 
 * Example usage:
 * ```kotlin
 * RowPreference(
 *     title = "Theme",
 *     subtitle = "Choose your preferred theme",
 *     icon = Icons.Default.Palette,
 *     onClick = { navigateToThemeSettings() },
 *     trailing = {
 *         Text("Dark", style = MaterialTheme.typography.bodyMedium)
 *     }
 * )
 * ```
 * 
 * @param title The main text to display (required)
 * @param subtitle Optional secondary text displayed below the title
 * @param icon Optional leading icon as ImageVector
 * @param painter Optional leading icon as Painter (takes precedence over icon if both provided)
 * @param onClick Callback invoked when the row is clicked (default: no-op)
 * @param onLongClick Callback invoked when the row is long-clicked (default: no-op)
 * @param trailing Optional composable content displayed at the end of the row
 * @param enabled Whether the row is enabled and interactive (default: true)
 * @param modifier Modifier for the row container
 * 
 * @see SectionHeader for grouping related preferences
 * @see NavigationRowPreference for preferences that navigate to another screen
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
 * Displays a styled header with optional icon to visually separate and label
 * groups of related preferences. Uses primary color for emphasis and follows
 * Material Design 3 typography guidelines.
 * 
 * Example usage:
 * ```kotlin
 * SectionHeader(
 *     text = "Appearance",
 *     icon = Icons.Default.Palette
 * )
 * RowPreference(title = "Theme", ...)
 * RowPreference(title = "Font Size", ...)
 * ```
 * 
 * @param text The header text to display (required)
 * @param icon Optional leading icon to display before the text
 * @param modifier Modifier for the header container
 * 
 * @see RowPreference for preference items to display under this header
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
 * Provides a container with elevation, rounded corners, and proper spacing
 * for displaying grouped content. Supports optional click handling for
 * interactive cards.
 * 
 * Features:
 * - Material Design 3 surface variant color
 * - 2dp elevation for subtle depth
 * - Medium corner radius (8dp)
 * - 16dp internal padding
 * - Optional click handling with ripple effect
 * 
 * Example usage:
 * ```kotlin
 * EnhancedCard {
 *     Text("Card Title", style = MaterialTheme.typography.titleMedium)
 *     Spacer(modifier = Modifier.height(8.dp))
 *     Text("Card content goes here")
 * }
 * 
 * // Clickable card
 * EnhancedCard(onClick = { navigateToDetails() }) {
 *     Text("Tap to view details")
 * }
 * ```
 * 
 * @param modifier Modifier for the card container
 * @param onClick Optional click handler. When provided, the card becomes interactive
 * @param content The content to display inside the card (ColumnScope for vertical layout)
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
 * Preference row with navigation indicator for screens that lead to another page.
 * 
 * A specialized version of [RowPreference] that includes a chevron icon to indicate
 * that tapping will navigate to another screen. This provides a clear visual cue
 * for navigation actions.
 * 
 * Example usage:
 * ```kotlin
 * NavigationRowPreference(
 *     title = "Advanced Settings",
 *     subtitle = "Configure advanced options",
 *     icon = Icons.Default.Settings,
 *     onClick = { navigateToAdvancedSettings() }
 * )
 * ```
 * 
 * @param title The main text to display (required)
 * @param subtitle Optional secondary text displayed below the title
 * @param icon Optional leading icon as ImageVector
 * @param painter Optional leading icon as Painter
 * @param onClick Callback invoked when the row is clicked
 * @param enabled Whether the row is enabled and interactive (default: true)
 * @param modifier Modifier for the row container
 * 
 * @see RowPreference for the base preference row component
 */
@Composable
fun NavigationRowPreference(
    title: String,
    subtitle: String? = null,
    icon: ImageVector? = null,
    painter: Painter? = null,
    onClick: () -> Unit = {},
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    RowPreference(
        title = title,
        subtitle = subtitle,
        icon = icon,
        painter = painter,
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
        trailing = {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Navigate",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    )
}

/**
 * Divider component for separating preference groups.
 * 
 * Provides a subtle visual separator between groups of preferences with
 * appropriate padding and color.
 * 
 * Example usage:
 * ```kotlin
 * RowPreference(title = "Option 1", ...)
 * RowPreference(title = "Option 2", ...)
 * PreferenceDivider()
 * RowPreference(title = "Option 3", ...)
 * ```
 * 
 * @param modifier Modifier for the divider
 * @param thickness The thickness of the divider line (default: 1.dp)
 * @param startIndent The start padding before the divider (default: 0.dp)
 */
@Composable
fun PreferenceDivider(
    modifier: Modifier = Modifier,
    thickness: Dp = 1.dp,
    startIndent: Dp = 0.dp
) {
    Divider(
        modifier = modifier.padding(vertical = 8.dp),
        thickness = thickness,
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    )
}

/**
 * Utility composable for creating a preference group with header and items.
 * 
 * Combines a [SectionHeader] with a column of preference items for convenient
 * grouping of related preferences.
 * 
 * Example usage:
 * ```kotlin
 * PreferenceGroup(
 *     title = "Display",
 *     icon = Icons.Default.Palette
 * ) {
 *     RowPreference(title = "Theme", ...)
 *     RowPreference(title = "Font Size", ...)
 * }
 * ```
 * 
 * @param title The header text for the group
 * @param icon Optional icon for the header
 * @param modifier Modifier for the group container
 * @param content The preference items to display in the group
 */
@Composable
fun PreferenceGroup(
    title: String,
    icon: ImageVector? = null,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = modifier) {
        SectionHeader(text = title, icon = icon)
        content()
    }
}

// ============================================================================
// Preview Functions
// ============================================================================

/**
 * Preview functions for the enhanced components.
 * These demonstrate various states and configurations.
 * 
 * Note: @Preview annotations are typically used in Android-specific code.
 * For multiplatform projects, these serve as example usage patterns and
 * can be used for manual testing or documentation purposes.
 */

/**
 * Preview of RowPreference in various states.
 * 
 * Demonstrates:
 * - Basic row with icon and subtitle
 * - Row with trailing switch
 * - Disabled row
 * - Row without icon
 */
@Composable
fun RowPreferencePreview() {
    MaterialTheme {
        Surface {
            Column {
                RowPreference(
                    title = "Theme",
                    subtitle = "Choose your preferred theme",
                    icon = Icons.Default.Palette,
                    onClick = { /* Handle click */ }
                )
                
                RowPreference(
                    title = "Notifications",
                    subtitle = "Manage notification settings",
                    icon = Icons.Default.Notifications,
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
                    icon = Icons.Default.Block,
                    enabled = false,
                    onClick = { /* Handle click */ }
                )
                
                RowPreference(
                    title = "Simple Row",
                    subtitle = "No icon, just text",
                    onClick = { /* Handle click */ }
                )
            }
        }
    }
}

/**
 * Preview of NavigationRowPreference.
 * 
 * Demonstrates navigation-style preference rows with chevron indicators.
 */
@Composable
fun NavigationRowPreferencePreview() {
    MaterialTheme {
        Surface {
            Column {
                NavigationRowPreference(
                    title = "Advanced Settings",
                    subtitle = "Configure advanced options",
                    icon = Icons.Default.Settings,
                    onClick = { /* Navigate */ }
                )
                
                NavigationRowPreference(
                    title = "About",
                    icon = Icons.Default.Info,
                    onClick = { /* Navigate */ }
                )
            }
        }
    }
}

/**
 * Preview of SectionHeader in various configurations.
 * 
 * Demonstrates:
 * - Header with icon
 * - Header without icon
 */
@Composable
fun SectionHeaderPreview() {
    MaterialTheme {
        Surface {
            Column {
                SectionHeader(
                    text = "Appearance",
                    icon = Icons.Default.Palette
                )
                
                SectionHeader(
                    text = "General Settings"
                )
            }
        }
    }
}

/**
 * Preview of EnhancedCard in various states.
 * 
 * Demonstrates:
 * - Static card with content
 * - Clickable card
 */
@Composable
fun EnhancedCardPreview() {
    MaterialTheme {
        Surface {
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
    }
}

/**
 * Preview of PreferenceGroup utility.
 * 
 * Demonstrates how to group related preferences with a header.
 */
@Composable
fun PreferenceGroupPreview() {
    MaterialTheme {
        Surface {
            Column {
                PreferenceGroup(
                    title = "Display",
                    icon = Icons.Default.Palette
                ) {
                    RowPreference(
                        title = "Theme",
                        subtitle = "Dark",
                        onClick = { /* Handle click */ }
                    )
                    RowPreference(
                        title = "Font Size",
                        subtitle = "Medium",
                        onClick = { /* Handle click */ }
                    )
                }
                
                PreferenceDivider()
                
                PreferenceGroup(
                    title = "Notifications",
                    icon = Icons.Default.Notifications
                ) {
                    RowPreference(
                        title = "Enable Notifications",
                        onClick = { /* Handle click */ },
                        trailing = {
                            Switch(
                                checked = true,
                                onCheckedChange = { /* Handle change */ }
                            )
                        }
                    )
                }
            }
        }
    }
}

/**
 * Comprehensive preview showing all components together.
 * 
 * Demonstrates a complete settings screen layout using all enhanced components.
 */
@Composable
fun CompleteSettingsScreenPreview() {
    MaterialTheme {
        Surface {
            Column {
                // Header section
                SectionHeader(
                    text = "Appearance",
                    icon = Icons.Default.Palette
                )
                
                // Navigation preferences
                NavigationRowPreference(
                    title = "Theme",
                    subtitle = "Dark mode",
                    icon = Icons.Default.Palette,
                    onClick = { /* Navigate */ }
                )
                
                RowPreference(
                    title = "Auto-rotate",
                    subtitle = "Rotate screen automatically",
                    onClick = { /* Handle click */ },
                    trailing = {
                        Switch(
                            checked = false,
                            onCheckedChange = { /* Handle change */ }
                        )
                    }
                )
                
                PreferenceDivider()
                
                // Card section
                Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    EnhancedCard {
                        Text(
                            text = "Pro Tip",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Long press on any preference to see additional options.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                
                PreferenceDivider()
                
                // Another section
                SectionHeader(
                    text = "Advanced",
                    icon = Icons.Default.Settings
                )
                
                NavigationRowPreference(
                    title = "Advanced Settings",
                    subtitle = "Configure advanced options",
                    icon = Icons.Default.Settings,
                    onClick = { /* Navigate */ }
                )
                
                RowPreference(
                    title = "Disabled Feature",
                    subtitle = "This feature is not available",
                    icon = Icons.Default.Block,
                    enabled = false,
                    onClick = { /* Handle click */ }
                )
            }
        }
    }
}
