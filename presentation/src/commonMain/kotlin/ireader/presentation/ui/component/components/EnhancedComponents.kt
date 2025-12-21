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
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.i18n.resources.*
import ireader.i18n.resources.Res

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
 *     title = localizeHelper.localize(Res.string.theme),
 *     subtitle = localizeHelper.localize(Res.string.choose_your_preferred_theme),
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
 *     text = localizeHelper.localize(Res.string.appearance),
 *     icon = Icons.Default.Palette
 * )
 * RowPreference(title = localizeHelper.localize(Res.string.theme), ...)
 * RowPreference(title = localizeHelper.localize(Res.string.font_size), ...)
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
 *     title = localizeHelper.localize(Res.string.advanced_settings),
 *     subtitle = localizeHelper.localize(Res.string.configure_advanced_options),
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
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
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
                contentDescription = localizeHelper.localize(Res.string.navigate),
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
 *     title = localizeHelper.localize(Res.string.display),
 *     icon = Icons.Default.Palette
 * ) {
 *     RowPreference(title = localizeHelper.localize(Res.string.theme), ...)
 *     RowPreference(title = localizeHelper.localize(Res.string.font_size), ...)
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
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    MaterialTheme {
        Surface {
            Column {
                RowPreference(
                    title = localizeHelper.localize(Res.string.theme),
                    subtitle = localizeHelper.localize(Res.string.choose_your_preferred_theme),
                    icon = Icons.Default.Palette,
                    onClick = { /* Handle click */ }
                )
                
                RowPreference(
                    title = localizeHelper.localize(Res.string.notifications),
                    subtitle = localizeHelper.localize(Res.string.manage_notification_settings),
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
                    title = localizeHelper.localize(Res.string.disabled_option),
                    subtitle = localizeHelper.localize(Res.string.this_option_is_currently_disabled),
                    icon = Icons.Default.Block,
                    enabled = false,
                    onClick = { /* Handle click */ }
                )
                
                RowPreference(
                    title = localizeHelper.localize(Res.string.simple_row),
                    subtitle = localizeHelper.localize(Res.string.no_icon_just_text),
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
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    MaterialTheme {
        Surface {
            Column {
                NavigationRowPreference(
                    title = localizeHelper.localize(Res.string.advanced_settings),
                    subtitle = localizeHelper.localize(Res.string.configure_advanced_options),
                    icon = Icons.Default.Settings,
                    onClick = { /* Navigate */ }
                )
                
                NavigationRowPreference(
                    title = localizeHelper.localize(Res.string.about),
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
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    MaterialTheme {
        Surface {
            Column {
                SectionHeader(
                    text = localizeHelper.localize(Res.string.appearance),
                    icon = Icons.Default.Palette
                )
                
                SectionHeader(
                    text = localizeHelper.localize(Res.string.general_settings)
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
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    MaterialTheme {
        Surface {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                EnhancedCard {
                    Text(
                        text = localizeHelper.localize(Res.string.card_title),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = localizeHelper.localize(Res.string.this_is_an_example_of),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                
                EnhancedCard(
                    onClick = { /* Handle click */ }
                ) {
                    Text(
                        text = localizeHelper.localize(Res.string.clickable_card),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = localizeHelper.localize(Res.string.this_card_can_be_clicked),
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
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    MaterialTheme {
        Surface {
            Column {
                PreferenceGroup(
                    title = localizeHelper.localize(Res.string.display),
                    icon = Icons.Default.Palette
                ) {
                    RowPreference(
                        title = localizeHelper.localize(Res.string.theme),
                        subtitle = localizeHelper.localize(Res.string.dark_1),
                        onClick = { /* Handle click */ }
                    )
                    RowPreference(
                        title = localizeHelper.localize(Res.string.font_size),
                        subtitle = localizeHelper.localize(Res.string.medium),
                        onClick = { /* Handle click */ }
                    )
                }
                
                PreferenceDivider()
                
                PreferenceGroup(
                    title = localizeHelper.localize(Res.string.notifications),
                    icon = Icons.Default.Notifications
                ) {
                    RowPreference(
                        title = localizeHelper.localize(Res.string.enable_notifications),
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
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    MaterialTheme {
        Surface {
            Column {
                // Header section
                SectionHeader(
                    text = localizeHelper.localize(Res.string.appearance),
                    icon = Icons.Default.Palette
                )
                
                // Navigation preferences
                NavigationRowPreference(
                    title = localizeHelper.localize(Res.string.theme),
                    subtitle = localizeHelper.localize(Res.string.dark_mode),
                    icon = Icons.Default.Palette,
                    onClick = { /* Navigate */ }
                )
                
                RowPreference(
                    title = localizeHelper.localize(Res.string.auto_rotate),
                    subtitle = localizeHelper.localize(Res.string.rotate_screen_automatically),
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
                            text = localizeHelper.localize(Res.string.pro_tip),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = localizeHelper.localize(Res.string.long_press_on_any_preference),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                
                PreferenceDivider()
                
                // Another section
                SectionHeader(
                    text = localizeHelper.localize(Res.string.advanced),
                    icon = Icons.Default.Settings
                )
                
                NavigationRowPreference(
                    title = localizeHelper.localize(Res.string.advanced_settings),
                    subtitle = localizeHelper.localize(Res.string.configure_advanced_options),
                    icon = Icons.Default.Settings,
                    onClick = { /* Navigate */ }
                )
                
                RowPreference(
                    title = localizeHelper.localize(Res.string.disabled_feature),
                    subtitle = localizeHelper.localize(Res.string.this_feature_is_not_available),
                    icon = Icons.Default.Block,
                    enabled = false,
                    onClick = { /* Handle click */ }
                )
            }
        }
    }
}
