package ireader.presentation.ui.settings.general

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DisplaySettings
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.currentOrThrow
import ireader.domain.models.prefs.PreferenceValues
import ireader.domain.preferences.prefs.AppPreferences
import ireader.domain.preferences.prefs.UiPreferences
import ireader.i18n.resources.MR
import ireader.presentation.core.theme.LocaleHelper
import ireader.presentation.ui.component.components.ChoicePreference
import ireader.presentation.ui.component.components.Components
import ireader.presentation.ui.component.components.SetupSettingComponents
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.presentation.ui.core.ui.asStateIn
import ireader.presentation.ui.video.component.cores.player.SubtitleHelper

@Composable
fun GeneralSettingScreen(
        scaffoldPadding: PaddingValues,
        vm: GeneralSettingScreenViewModel,
        onTranslationSettingsClick: () -> Unit,
) {
    val localizeHelper = LocalLocalizeHelper.currentOrThrow
    val manageNotificationComponent = mangeNotificationRow()
    val items = remember {
        listOf<Components>(
                // App Updates & Display Section
                Components.Header(
                        text = "App Updates & Display",
                        padding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                        icon = Icons.Filled.DisplaySettings
                ),
                Components.Switch(
                        preference = vm.appUpdater,
                        title = localizeHelper.localize(MR.strings.updater_is_enable),
                        icon = Icons.Filled.Update
                ),
                Components.Switch(
                        preference = vm.showHistory,
                        title = localizeHelper.localize(MR.strings.show_history),
                        icon = Icons.Filled.History
                ),
                Components.Switch(
                        preference = vm.showUpdate,
                        title = localizeHelper.localize(MR.strings.show_update),
                        icon = Icons.Filled.Update
                ),
                Components.Switch(
                        preference = vm.confirmExit,
                        title = localizeHelper.localize(MR.strings.confirm_exit),
                        icon = Icons.Filled.Settings
                ),
                
                Components.Space,
                
                // Library Settings Section
                Components.Header(
                        text = "Library Settings",
                        padding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                        icon = Icons.Filled.Storage
                ),
                Components.Switch(
                        preference = vm.showSmartCategories,
                        title = "Show Smart Categories",
                        subtitle = "Display auto-populated categories like Recently Added, Currently Reading, etc.",
                        icon = Icons.Filled.Storage
                ),
                
                Components.Space,
                
                // Download Settings Section
                Components.Header(
                        text = "Download Settings",
                        padding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                        icon = Icons.Filled.Download
                ),
                Components.Slider(
                    preferenceAsLong = vm.downloadDelayMs,
                    title = "Download Delay",
                    subtitle = "Delay between chapter downloads to avoid IP blocking",
                    icon = Icons.Filled.Download,
                    valueRange = 0f..10000f,
                    steps = 19,
                    trailing = vm.formatDownloadDelay(vm.downloadDelayMs.value)
                ),
                Components.Slider(
                    preferenceAsInt = vm.concurrentDownloads,
                    title = "Concurrent Downloads",
                    subtitle = "Maximum number of simultaneous downloads",
                    icon = Icons.Filled.Download,
                    valueRange = 1f..10f,
                    steps = 8,
                    trailing = "${vm.concurrentDownloads.value} ${if (vm.concurrentDownloads.value == 1) "download" else "downloads"}"
                ),
                
                Components.Space,
                
                // Catalog Settings Section
                Components.Header(
                        text = "Catalog Settings",
                        padding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                        icon = Icons.Filled.Storage
                ),
                Components.Switch(
                        preference = vm.showSystemWideCatalogs,
                        title = localizeHelper.localize(MR.strings.show_system_catalogs),
                        subtitle = localizeHelper.localize(MR.strings.show_system_catalogs_subtitle),
                        icon = Icons.Filled.Storage
                ),
                Components.Switch(
                        preference = vm.showLocalCatalogs,
                        title = localizeHelper.localize(MR.strings.show_local_catalogs),
                        subtitle = localizeHelper.localize(MR.strings.show_local_catalogs_subtitle),
                        icon = Icons.Filled.Storage
                ),
                Components.Switch(
                        preference = vm.autoInstaller,
                        title = localizeHelper.localize(MR.strings.auto_installer),
                        subtitle = localizeHelper.localize(MR.strings.auto_installer_subtitle),
                        icon = Icons.Filled.Settings
                ),
                Components.Switch(
                        preference = vm.localSourceLocation,
                        title = localizeHelper.localize(MR.strings.saved_local_source_location),
                        icon = Icons.Filled.Storage
                ),
                Components.Dynamic {
                    ChoicePreference<PreferenceValues.Installer>(
                        preference = vm.installer,
                        choices = mapOf(
                            PreferenceValues.Installer.AndroidPackageManager to localizeHelper.localize(
                                MR.strings.package_manager
                            ),
                            PreferenceValues.Installer.LocalInstaller to localizeHelper.localize(MR.strings.local_installer),
                        ),
                        title = localizeHelper.localize(
                            MR.strings.installer_mode
                        )
                    )
                },
                
                Components.Space,
                
                // Notifications Section
                Components.Header(
                        text = "Notifications",
                        padding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                        icon = Icons.Filled.Notifications
                ),
                manageNotificationComponent,
                
                Components.Space,
                
                // Language & Translation Section
                Components.Header(
                        text = "Language & Translation",
                        padding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                        icon = Icons.Filled.Language
                ),
                Components.Dynamic {
                    NavigationPreferenceCustom(
                        title = localizeHelper.localize(MR.strings.translation_settings),
                        subtitle = localizeHelper.localize(MR.strings.api_settings),
                        icon = { Icon(Icons.Filled.Translate, contentDescription = null) },
                        onClick = onTranslationSettingsClick
                    )
                },
                Components.Dynamic {
                    ChoicePreference(
                            preference = vm.language,
                            choices = vm.getLanguageChoices(),
                            title = localizeHelper.localize(
                                    MR.strings.languages
                            ),
                            onValue = { value: String ->
                                vm.language.value = value
                                vm.localeHelper.updateLocal()
                            }
                    )
                }
        )
    }

    SetupSettingComponents(
            scaffoldPadding = scaffoldPadding,
            items = items
    )
}

/**
 * Enhanced navigation preference component with Material Design 3 styling.
 * 
 * This component provides a consistent way to display navigation items with:
 * - Support for leading icons
 * - Optional subtitle text
 * - Proper touch feedback with ripple effects
 * - Long-press support
 * - Full accessibility support
 * - Material Design 3 styling with proper colors and typography
 * 
 * @param title The main text to display
 * @param subtitle Optional secondary text displayed below the title
 * @param icon Optional leading icon composable
 * @param onClick Callback invoked when the row is clicked
 * @param onLongClick Callback invoked when the row is long-clicked
 * @param enabled Whether the row is enabled and interactive
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NavigationPreferenceCustom(
    title: String,
    subtitle: String? = null,
    icon: @Composable (() -> Unit)? = null,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {},
    enabled: Boolean = true
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
        modifier = Modifier
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
                indication = androidx.compose.material3.ripple()
            ),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Leading icon with proper tinting
            if (icon != null) {
                CompositionLocalProvider(
                    LocalContentColor provides if (enabled) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    }
                ) {
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .size(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        icon()
                    }
                }
            }
            
            // Title and subtitle column
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(
                        start = if (icon == null) 16.dp else 0.dp,
                        end = 16.dp
                    ),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (enabled) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
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
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                        },
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

class GeneralSettingScreenViewModel(
        private val appPreferences: AppPreferences,
        private val uiPreferences: UiPreferences,
        private val downloadPreferences: ireader.domain.preferences.prefs.DownloadPreferences,
        private val libraryPreferences: ireader.domain.preferences.prefs.LibraryPreferences,
        val localeHelper: LocaleHelper
) : ireader.presentation.ui.core.viewmodel.BaseViewModel() {

    val appUpdater = appPreferences.appUpdater().asStateIn(scope)

    var showHistory = uiPreferences.showHistoryInButtonBar().asStateIn(scope)
    var showUpdate = uiPreferences.showUpdatesInButtonBar().asStateIn(scope)
    var confirmExit = uiPreferences.confirmExit().asStateIn(scope)
    var installer = uiPreferences.installerMode().asStateIn(scope)
    var language = uiPreferences.language().asStateIn(scope)
    var showSystemWideCatalogs = uiPreferences.showSystemWideCatalogs().asStateIn(scope)
    var showLocalCatalogs = uiPreferences.showLocalCatalogs().asStateIn(scope)
    var autoInstaller = uiPreferences.autoCatalogUpdater().asStateIn(scope)
    var localSourceLocation = uiPreferences.savedLocalCatalogLocation().asStateIn(scope)
    
    // Library preferences
    val showSmartCategories = libraryPreferences.showSmartCategories().asStateIn(scope)
    
    // Download preferences
    val downloadDelayMs = downloadPreferences.downloadDelayMs().asStateIn(scope)
    val concurrentDownloads = downloadPreferences.concurrentDownloadsLimit().asStateIn(scope)

    @Composable
    fun getLanguageChoices(): Map<String, String> {
        val lanuages = localeHelper.languages.mapNotNull { SubtitleHelper.fromTwoLettersToLanguage(it)?.let { lang -> it to lang } }
        return lanuages.toMap()
    }
    
    /**
     * Formats download delay value for better readability.
     * Shows seconds when >= 1000ms, otherwise shows milliseconds.
     */
    fun formatDownloadDelay(delayMs: Long): String {
        return when {
            delayMs == 0L -> "No delay"
            delayMs >= 1000L -> {
                val seconds = delayMs / 1000.0
                if (seconds % 1.0 == 0.0) {
                    "${seconds.toInt()}s"
                } else {
                    String.format("%.1fs", seconds)
                }
            }
            else -> "${delayMs}ms"
        }
    }
}
