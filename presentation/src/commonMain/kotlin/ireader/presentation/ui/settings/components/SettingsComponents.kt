package ireader.presentation.ui.settings.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Unified settings component library with consistent Material3 styling.
 * 
 * This library provides reusable components for building settings screens
 * with consistent spacing, typography, colors, and navigation patterns.
 * 
 * Performance optimizations:
 * - Reduced nested layouts
 * - Pre-computed modifiers where possible
 * - Stable composables to reduce recomposition
 */

// Pre-computed modifiers for better performance
private val sectionHeaderModifier = Modifier
    .fillMaxWidth()
    .padding(horizontal = 16.dp, vertical = 16.dp)

private val settingsItemModifier = Modifier
    .fillMaxWidth()
    .padding(horizontal = 16.dp, vertical = 4.dp)

private val settingsItemContentModifier = Modifier
    .fillMaxWidth()
    .padding(horizontal = 16.dp, vertical = 12.dp)

/**
 * Section header for grouping related settings.
 * Optimized with pre-computed modifiers.
 * 
 * @param title The section title text
 * @param icon Optional icon to display before the title
 * @param modifier Optional modifier for customization
 */
@Composable
fun SettingsSectionHeader(
    title: String,
    icon: ImageVector? = null,
    modifier: Modifier = Modifier
) {
    // Pre-compute content description
    val contentDesc = remember(title) { "$title section" }
    
    Row(
        modifier = modifier
            .then(sectionHeaderModifier)
            .semantics(mergeDescendants = true) {
                contentDescription = contentDesc
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
        }
        
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant,
            modifier = Modifier.weight(1f)
        )
    }
}

/**
 * Standard settings item with title, description, icon, and navigation indicator.
 * Optimized with reduced nesting and pre-computed values.
 * 
 * @param title The main title text
 * @param description Optional descriptive text below the title
 * @param icon Optional leading icon
 * @param onClick Click handler for the item
 * @param modifier Optional modifier for customization
 * @param enabled Whether the item is clickable (default: true)
 * @param showNavigationIcon Whether to show the chevron icon (default: true)
 */
@Composable
fun SettingsItem(
    title: String,
    description: String? = null,
    icon: ImageVector? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    showNavigationIcon: Boolean = true
) {
    // Pre-compute content description to avoid string building on every recomposition
    val contentDesc = remember(title, description) {
        buildString {
            append(title)
            if (description != null) {
                append(". ")
                append(description)
            }
        }
    }
    
    // Pre-compute colors to avoid recalculation
    val iconTint = if (enabled) {
        MaterialTheme.colorScheme.onSurfaceVariant
    } else {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
    }
    
    val titleColor = if (enabled) {
        MaterialTheme.colorScheme.onSurface
    } else {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
    }
    
    val descriptionColor = if (enabled) {
        MaterialTheme.colorScheme.onSurfaceVariant
    } else {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
    }
    
    // Simplified layout - removed Surface wrapper, using Box instead
    Box(
        modifier = modifier
            .then(settingsItemModifier)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .semantics {
                contentDescription = contentDesc
                role = androidx.compose.ui.semantics.Role.Button
            }
            .clickable(enabled = enabled, onClick = onClick)
    ) {
        Row(
            modifier = settingsItemContentModifier,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp)
                )
                
                Spacer(modifier = Modifier.width(16.dp))
            }
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = titleColor
                )
                
                if (description != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = descriptionColor
                    )
                }
            }
            
            if (showNavigationIcon) {
                Spacer(modifier = Modifier.width(8.dp))
                
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

/**
 * Settings item with a switch control.
 * Optimized with reduced nesting and pre-computed values.
 * 
 * @param title The main title text
 * @param description Optional descriptive text below the title
 * @param icon Optional leading icon
 * @param checked Current switch state
 * @param onCheckedChange Callback when switch state changes
 * @param modifier Optional modifier for customization
 * @param enabled Whether the switch is interactive (default: true)
 */
@Composable
fun SettingsSwitchItem(
    title: String,
    description: String? = null,
    icon: ImageVector? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    // Pre-compute content description
    val contentDesc = remember(title, description, checked) {
        buildString {
            append(title)
            if (description != null) {
                append(". ")
                append(description)
            }
            append(". ")
            append(if (checked) "Enabled" else "Disabled")
        }
    }
    
    // Pre-compute colors
    val iconTint = if (enabled) {
        MaterialTheme.colorScheme.onSurfaceVariant
    } else {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
    }
    
    val titleColor = if (enabled) {
        MaterialTheme.colorScheme.onSurface
    } else {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
    }
    
    val descriptionColor = if (enabled) {
        MaterialTheme.colorScheme.onSurfaceVariant
    } else {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
    }
    
    // Simplified layout - removed Surface wrapper
    Box(
        modifier = modifier
            .then(settingsItemModifier)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .semantics {
                contentDescription = contentDesc
                role = androidx.compose.ui.semantics.Role.Switch
            }
            .clickable(enabled = enabled) { onCheckedChange(!checked) }
    ) {
        Row(
            modifier = settingsItemContentModifier,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp)
                )
                
                Spacer(modifier = Modifier.width(16.dp))
            }
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = titleColor
                )
                
                if (description != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = descriptionColor
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                enabled = enabled,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                    checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                    checkedIconColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    }
}

/**
 * Settings item with custom trailing content.
 * 
 * @param title The main title text
 * @param description Optional descriptive text below the title
 * @param icon Optional leading icon
 * @param onClick Click handler for the item
 * @param modifier Optional modifier for customization
 * @param enabled Whether the item is clickable (default: true)
 * @param trailingContent Custom composable content to display at the end
 */
@Composable
fun SettingsItemWithTrailing(
    title: String,
    description: String? = null,
    icon: ImageVector? = null,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    trailingContent: @Composable () -> Unit
) {
    val contentDesc = buildString {
        append(title)
        if (description != null) {
            append(". ")
            append(description)
        }
    }
    
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .semantics {
                contentDescription = contentDesc
                role = androidx.compose.ui.semantics.Role.Button
            }
            .clickable(enabled = enabled, onClick = onClick),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (enabled) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    },
                    modifier = Modifier.size(24.dp)
                )
                
                Spacer(modifier = Modifier.width(16.dp))
            }
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = if (enabled) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    }
                )
                
                if (description != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (enabled) {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        } else {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                        }
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            trailingContent()
        }
    }
}

/**
 * Highlighted settings card for important features or status.
 * 
 * @param title The main title text
 * @param description Optional descriptive text below the title
 * @param icon Optional leading icon
 * @param onClick Click handler for the card
 * @param modifier Optional modifier for customization
 * @param containerColor Background color of the card
 */
@Composable
fun SettingsHighlightCard(
    title: String,
    description: String? = null,
    icon: ImageVector? = null,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    containerColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.surfaceVariant
) {
    val contentDesc = buildString {
        append(title)
        if (description != null) {
            append(". ")
            append(description)
        }
    }
    
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(16.dp))
            .semantics {
                contentDescription = contentDesc
                role = androidx.compose.ui.semantics.Role.Button
            }
            .clickable(onClick = onClick),
        color = containerColor.copy(alpha = 0.3f),
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
            }
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                if (description != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Settings divider for visual separation between groups.
 * 
 * @param modifier Optional modifier for customization
 */
@Composable
fun SettingsDivider(
    modifier: Modifier = Modifier
) {
    HorizontalDivider(
        modifier = modifier.padding(vertical = 8.dp),
        thickness = 1.dp,
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    )
}

/**
 * Settings spacer for vertical spacing between sections.
 * 
 * @param height The height of the spacer (default: 16.dp)
 * @param modifier Optional modifier for customization
 */
@Composable
fun SettingsSpacer(
    height: androidx.compose.ui.unit.Dp = 16.dp,
    modifier: Modifier = Modifier
) {
    Spacer(modifier = modifier.height(height))
}
