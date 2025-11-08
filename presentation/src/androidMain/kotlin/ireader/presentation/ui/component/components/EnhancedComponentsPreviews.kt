package ireader.presentation.ui.component.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp

/**
 * Android-specific preview functions for EnhancedComponents.
 * 
 * These previews use @Preview annotations to enable Android Studio's
 * Compose preview functionality, allowing developers to see component
 * variations without running the app.
 */

/**
 * Preview of RowPreference in various states.
 */
@Preview(name = "Row Preference - Light", showBackground = true)
@Preview(name = "Row Preference - Dark", showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun RowPreferencePreviewAndroid() {
    MaterialTheme {
        Surface {
            Column {
                RowPreference(
                    title = "Theme",
                    subtitle = "Choose your preferred theme",
                    icon = Icons.Default.Palette,
                    onClick = { }
                )
                
                RowPreference(
                    title = "Notifications",
                    subtitle = "Manage notification settings",
                    icon = Icons.Default.Notifications,
                    onClick = { },
                    trailing = {
                        Switch(
                            checked = true,
                            onCheckedChange = { }
                        )
                    }
                )
                
                RowPreference(
                    title = "Disabled Option",
                    subtitle = "This option is currently disabled",
                    icon = Icons.Default.Block,
                    enabled = false,
                    onClick = { }
                )
                
                RowPreference(
                    title = "Simple Row",
                    subtitle = "No icon, just text",
                    onClick = { }
                )
            }
        }
    }
}

/**
 * Preview of RowPreference with different content lengths.
 */
@Preview(name = "Row Preference - Long Text", showBackground = true)
@Composable
fun RowPreferenceLongTextPreview() {
    MaterialTheme {
        Surface {
            Column {
                RowPreference(
                    title = "Very Long Title That Might Wrap To Multiple Lines In Some Cases",
                    subtitle = "This is a very long subtitle that demonstrates how the component handles text overflow and wrapping when the content is too long to fit on a single line",
                    icon = Icons.Default.Settings,
                    onClick = { }
                )
            }
        }
    }
}

/**
 * Preview of NavigationRowPreference.
 */
@Preview(name = "Navigation Row Preference", showBackground = true)
@Preview(name = "Navigation Row Preference - Dark", showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun NavigationRowPreferencePreviewAndroid() {
    MaterialTheme {
        Surface {
            Column {
                NavigationRowPreference(
                    title = "Advanced Settings",
                    subtitle = "Configure advanced options",
                    icon = Icons.Default.Settings,
                    onClick = { }
                )
                
                NavigationRowPreference(
                    title = "About",
                    icon = Icons.Default.Info,
                    onClick = { }
                )
                
                NavigationRowPreference(
                    title = "Disabled Navigation",
                    subtitle = "This option is disabled",
                    icon = Icons.Default.Block,
                    enabled = false,
                    onClick = { }
                )
            }
        }
    }
}

/**
 * Preview of SectionHeader.
 */
@Preview(name = "Section Header", showBackground = true)
@Preview(name = "Section Header - Dark", showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun SectionHeaderPreviewAndroid() {
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
                
                SectionHeader(
                    text = "Advanced Options",
                    icon = Icons.Default.Settings
                )
            }
        }
    }
}

/**
 * Preview of EnhancedCard.
 */
@Preview(name = "Enhanced Card", showBackground = true)
@Preview(name = "Enhanced Card - Dark", showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun EnhancedCardPreviewAndroid() {
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
                    onClick = { }
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
 */
@Preview(name = "Preference Group", showBackground = true)
@Preview(name = "Preference Group - Dark", showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PreferenceGroupPreviewAndroid() {
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
                        onClick = { }
                    )
                    RowPreference(
                        title = "Font Size",
                        subtitle = "Medium",
                        onClick = { }
                    )
                }
                
                PreferenceDivider()
                
                PreferenceGroup(
                    title = "Notifications",
                    icon = Icons.Default.Notifications
                ) {
                    RowPreference(
                        title = "Enable Notifications",
                        onClick = { },
                        trailing = {
                            Switch(
                                checked = true,
                                onCheckedChange = { }
                            )
                        }
                    )
                }
            }
        }
    }
}

/**
 * Preview of PreferenceDivider.
 */
@Preview(name = "Preference Divider", showBackground = true)
@Composable
fun PreferenceDividerPreviewAndroid() {
    MaterialTheme {
        Surface {
            Column {
                RowPreference(title = "Option 1", onClick = { })
                RowPreference(title = "Option 2", onClick = { })
                PreferenceDivider()
                RowPreference(title = "Option 3", onClick = { })
                RowPreference(title = "Option 4", onClick = { })
            }
        }
    }
}

/**
 * Comprehensive preview showing all components together.
 */
@Preview(name = "Complete Settings Screen", showBackground = true, heightDp = 800)
@Preview(name = "Complete Settings Screen - Dark", showBackground = true, heightDp = 800, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun CompleteSettingsScreenPreviewAndroid() {
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
                    onClick = { }
                )
                
                RowPreference(
                    title = "Auto-rotate",
                    subtitle = "Rotate screen automatically",
                    onClick = { },
                    trailing = {
                        Switch(
                            checked = false,
                            onCheckedChange = { }
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
                    onClick = { }
                )
                
                RowPreference(
                    title = "Disabled Feature",
                    subtitle = "This feature is not available",
                    icon = Icons.Default.Block,
                    enabled = false,
                    onClick = { }
                )
            }
        }
    }
}

/**
 * Preview showing component states for testing.
 */
@Preview(name = "Component States", showBackground = true)
@Composable
fun ComponentStatesPreview() {
    MaterialTheme {
        Surface {
            Column {
                SectionHeader(text = "Enabled State")
                RowPreference(
                    title = "Enabled Preference",
                    subtitle = "This is enabled",
                    icon = Icons.Default.Settings,
                    enabled = true,
                    onClick = { }
                )
                
                PreferenceDivider()
                
                SectionHeader(text = "Disabled State")
                RowPreference(
                    title = "Disabled Preference",
                    subtitle = "This is disabled",
                    icon = Icons.Default.Block,
                    enabled = false,
                    onClick = { }
                )
                
                PreferenceDivider()
                
                SectionHeader(text = "With Trailing Content")
                RowPreference(
                    title = "Switch Preference",
                    subtitle = "Toggle this option",
                    icon = Icons.Default.Notifications,
                    onClick = { },
                    trailing = {
                        Switch(checked = true, onCheckedChange = { })
                    }
                )
                
                RowPreference(
                    title = "Text Trailing",
                    subtitle = "Shows value",
                    icon = Icons.Default.Palette,
                    onClick = { },
                    trailing = {
                        Text(
                            text = "Dark",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )
            }
        }
    }
}
