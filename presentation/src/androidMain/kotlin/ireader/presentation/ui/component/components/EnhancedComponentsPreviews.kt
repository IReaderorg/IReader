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
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.i18n.resources.*
import ireader.i18n.resources.Res

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
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    MaterialTheme {
        Surface {
            Column {
                RowPreference(
                    title = localizeHelper.localize(Res.string.theme),
                    subtitle = localizeHelper.localize(Res.string.choose_your_preferred_theme),
                    icon = Icons.Default.Palette,
                    onClick = { }
                )
                
                RowPreference(
                    title = localizeHelper.localize(Res.string.notifications),
                    subtitle = localizeHelper.localize(Res.string.manage_notification_settings),
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
                    title = localizeHelper.localize(Res.string.disabled_option),
                    subtitle = localizeHelper.localize(Res.string.this_option_is_currently_disabled),
                    icon = Icons.Default.Block,
                    enabled = false,
                    onClick = { }
                )
                
                RowPreference(
                    title = localizeHelper.localize(Res.string.simple_row),
                    subtitle = localizeHelper.localize(Res.string.no_icon_just_text),
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
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    MaterialTheme {
        Surface {
            Column {
                RowPreference(
                    title = localizeHelper.localize(Res.string.very_long_title_that_might),
                    subtitle = localizeHelper.localize(Res.string.this_is_a_very_long),
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
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    MaterialTheme {
        Surface {
            Column {
                NavigationRowPreference(
                    title = localizeHelper.localize(Res.string.advanced_settings),
                    subtitle = localizeHelper.localize(Res.string.configure_advanced_options),
                    icon = Icons.Default.Settings,
                    onClick = { }
                )
                
                NavigationRowPreference(
                    title = localizeHelper.localize(Res.string.about),
                    icon = Icons.Default.Info,
                    onClick = { }
                )
                
                NavigationRowPreference(
                    title = localizeHelper.localize(Res.string.disabled_navigation),
                    subtitle = localizeHelper.localize(Res.string.this_option_is_disabled),
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
                
                SectionHeader(
                    text = localizeHelper.localize(Res.string.advanced_options),
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
                    onClick = { }
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
 */
@Preview(name = "Preference Group", showBackground = true)
@Preview(name = "Preference Group - Dark", showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PreferenceGroupPreviewAndroid() {
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
                        onClick = { }
                    )
                    RowPreference(
                        title = localizeHelper.localize(Res.string.font_size),
                        subtitle = localizeHelper.localize(Res.string.medium),
                        onClick = { }
                    )
                }
                
                PreferenceDivider()
                
                PreferenceGroup(
                    title = localizeHelper.localize(Res.string.notifications),
                    icon = Icons.Default.Notifications
                ) {
                    RowPreference(
                        title = localizeHelper.localize(Res.string.enable_notifications),
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
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    MaterialTheme {
        Surface {
            Column {
                RowPreference(title = localizeHelper.localize(Res.string.option_1), onClick = { })
                RowPreference(title = localizeHelper.localize(Res.string.option_2), onClick = { })
                PreferenceDivider()
                RowPreference(title = localizeHelper.localize(Res.string.option_3), onClick = { })
                RowPreference(title = localizeHelper.localize(Res.string.option_4), onClick = { })
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
                    onClick = { }
                )
                
                RowPreference(
                    title = localizeHelper.localize(Res.string.auto_rotate),
                    subtitle = localizeHelper.localize(Res.string.rotate_screen_automatically),
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
                    onClick = { }
                )
                
                RowPreference(
                    title = localizeHelper.localize(Res.string.disabled_feature),
                    subtitle = localizeHelper.localize(Res.string.this_feature_is_not_available),
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
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    MaterialTheme {
        Surface {
            Column {
                SectionHeader(text = localizeHelper.localize(Res.string.enabled_state))
                RowPreference(
                    title = localizeHelper.localize(Res.string.enabled_preference),
                    subtitle = localizeHelper.localize(Res.string.this_is_enabled),
                    icon = Icons.Default.Settings,
                    enabled = true,
                    onClick = { }
                )
                
                PreferenceDivider()
                
                SectionHeader(text = localizeHelper.localize(Res.string.disabled_state))
                RowPreference(
                    title = localizeHelper.localize(Res.string.disabled_preference),
                    subtitle = localizeHelper.localize(Res.string.this_is_disabled),
                    icon = Icons.Default.Block,
                    enabled = false,
                    onClick = { }
                )
                
                PreferenceDivider()
                
                SectionHeader(text = localizeHelper.localize(Res.string.with_trailing_content))
                RowPreference(
                    title = localizeHelper.localize(Res.string.switch_preference),
                    subtitle = localizeHelper.localize(Res.string.toggle_this_option),
                    icon = Icons.Default.Notifications,
                    onClick = { },
                    trailing = {
                        Switch(checked = true, onCheckedChange = { })
                    }
                )
                
                RowPreference(
                    title = localizeHelper.localize(Res.string.text_trailing),
                    subtitle = localizeHelper.localize(Res.string.shows_value),
                    icon = Icons.Default.Palette,
                    onClick = { },
                    trailing = {
                        Text(
                            text = localizeHelper.localize(Res.string.dark_1),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )
            }
        }
    }
}
