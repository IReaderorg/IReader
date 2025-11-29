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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.Update
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Code
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
import ireader.domain.models.prefs.PreferenceValues
import ireader.domain.preferences.prefs.AppPreferences
import ireader.domain.preferences.prefs.UiPreferences
import ireader.i18n.resources.Res
import ireader.i18n.resources.*
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
        onJSPluginSettingsClick: () -> Unit = {},
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
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
                        title = localizeHelper.localize(Res.string.updater_is_enable),
                        icon = Icons.Filled.Update
                ),
                Components.Switch(
                        preference = vm.showHistory,
                        title = localizeHelper.localize(Res.string.show_history),
                        icon = Icons.Filled.History
                ),
                Components.Switch(
                        preference = vm.showUpdate,
                        title = localizeHelper.localize(Res.string.show_update),
                        icon = Icons.Filled.Update
                ),
                Components.Switch(
                        preference = vm.confirmExit,
                        title = localizeHelper.localize(Res.string.confirm_exit),
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
                Components.Switch(
                        preference = vm.useFabInLibrary,
                        title = "Use FAB in Library",
                        subtitle = "Replace toolbar buttons with floating action button",
                        icon = Icons.Filled.Settings
                ),
                Components.Dynamic {
                    ChoicePreference<String>(
                        preference = vm.defaultChapterSort,
                        choices = mapOf(
                            "SOURCE_ORDER" to "By Source Order",
                            "CHAPTER_NUMBER" to "By Chapter Number",
                            "UPLOAD_DATE_ASC" to "By Upload Date (Ascending)",
                            "UPLOAD_DATE_DESC" to "By Upload Date (Descending)"
                        ),
                        title = "Default Chapter Sort",
                        subtitle = "Default sorting method for chapter lists"
                    )
                },
                
                Components.Space,
                
                // Global Search Section
                Components.Header(
                        text = "Global Search",
                        padding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                        icon = Icons.Filled.Search
                ),
                Components.Switch(
                        preference = vm.onlyUpdateOnFinding,
                        title = "Only Update on Finding Novel",
                        subtitle = "Add novels to library only when found in search results",
                        icon = Icons.Filled.Search
                ),
                Components.Switch(
                        preference = vm.showLastUpdateTime,
                        title = "Show Last Update Time",
                        subtitle = "Display when each novel was last checked for updates",
                        icon = Icons.Filled.Update
                ),
                
                Components.Space,
                
                // Auto Download Section
                Components.Header(
                        text = "Auto Download",
                        padding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                        icon = Icons.Filled.Download
                ),
                Components.Switch(
                        preference = vm.autoDownloadNewChapters,
                        title = "Download New Chapters",
                        subtitle = "Automatically download newly detected chapters in background",
                        icon = Icons.Filled.Download
                ),
                
                Components.Space,
                
                // User Interface Section
                Components.Header(
                        text = "User Interface",
                        padding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                        icon = Icons.Filled.DisplaySettings
                ),
                Components.Switch(
                        preference = vm.disableHapticFeedback,
                        title = "Disable Haptic Feedback",
                        subtitle = "Turn off vibration feedback for interactions",
                        icon = Icons.Filled.TouchApp
                ),
                Components.Switch(
                        preference = vm.disableLoadingAnimations,
                        title = "Disable Loading Animations",
                        subtitle = "Use static indicators instead of animated ones",
                        icon = Icons.Filled.Autorenew
                ),
                
                Components.Space,
                
                // Community & Leaderboard Section
                Components.Header(
                        text = "Community & Leaderboard",
                        padding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                        icon = Icons.Filled.Settings
                ),
                Components.Switch(
                        preference = vm.leaderboardRealtimeEnabled,
                        title = "Leaderboard Realtime Updates",
                        subtitle = "Automatically refresh leaderboard when other users sync (uses more data)",
                        icon = Icons.Filled.Autorenew
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
                    trailingFormatter = { value -> vm.formatDownloadDelay(value.toLong()) }
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
                        title = localizeHelper.localize(Res.string.show_system_catalogs),
                        subtitle = localizeHelper.localize(Res.string.show_system_catalogs_subtitle),
                        icon = Icons.Filled.Storage
                ),
                Components.Switch(
                        preference = vm.showLocalCatalogs,
                        title = localizeHelper.localize(Res.string.show_local_catalogs),
                        subtitle = localizeHelper.localize(Res.string.show_local_catalogs_subtitle),
                        icon = Icons.Filled.Storage
                ),
                Components.Dynamic {
                    NavigationPreferenceCustom(
                        title = "JavaScript Plugin Settings",
                        subtitle = "Configure JS plugins and enable LNReader-compatible sources",
                        icon = { Icon(Icons.Filled.Code, contentDescription = null) },
                        onClick = onJSPluginSettingsClick
                    )
                },
                Components.Switch(
                        preference = vm.enableJSPlugins,
                        title = "Enable JavaScript Plugins",
                        subtitle = "Allow loading LNReader-compatible JavaScript plugins",
                        icon = Icons.Filled.Code
                ),
                Components.Switch(
                        preference = vm.autoInstaller,
                        title = localizeHelper.localize(Res.string.auto_installer),
                        subtitle = localizeHelper.localize(Res.string.auto_installer_subtitle),
                        icon = Icons.Filled.Settings
                ),
                Components.Switch(
                        preference = vm.localSourceLocation,
                        title = localizeHelper.localize(Res.string.saved_local_source_location),
                        icon = Icons.Filled.Storage
                ),
                Components.Dynamic {
                    ChoicePreference<PreferenceValues.Installer>(
                        preference = vm.installer,
                        choices = mapOf(
                            PreferenceValues.Installer.AndroidPackageManager to localizeHelper.localize(
                                Res.string.package_manager
                            ),
                            PreferenceValues.Installer.LocalInstaller to localizeHelper.localize(Res.string.local_installer),
                        ),
                        title = localizeHelper.localize(
                            Res.string.installer_mode
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
                        title = localizeHelper.localize(Res.string.translation_settings),
                        subtitle = localizeHelper.localize(Res.string.api_settings),
                        icon = { Icon(Icons.Filled.Translate, contentDescription = null) },
                        onClick = onTranslationSettingsClick
                    )
                },
                Components.Dynamic {
                    ChoicePreference(
                            preference = vm.language,
                            choices = vm.getLanguageChoices(),
                            title = localizeHelper.localize(
                                    Res.string.languages
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
    var enableJSPlugins = uiPreferences.enableJSPlugins().asStateIn(scope)
    var autoInstaller = uiPreferences.autoCatalogUpdater().asStateIn(scope)
    var localSourceLocation = uiPreferences.savedLocalCatalogLocation().asStateIn(scope)
    
    // Library preferences
    val showSmartCategories = libraryPreferences.showSmartCategories().asStateIn(scope)
    
    // Download preferences
    val downloadDelayMs = downloadPreferences.downloadDelayMs().asStateIn(scope)
    
    // New General Settings Enhancements
    // Library preferences
    val useFabInLibrary = uiPreferences.useFabInLibrary().asStateIn(scope)
    val defaultChapterSort = uiPreferences.defaultChapterSort().asStateIn(scope)
    
    // Global search preferences
    val onlyUpdateOnFinding = uiPreferences.onlyUpdateOnFinding().asStateIn(scope)
    val showLastUpdateTime = uiPreferences.showLastUpdateTime().asStateIn(scope)
    
    // Auto download preferences
    val autoDownloadNewChapters = uiPreferences.autoDownloadNewChapters().asStateIn(scope)
    
    // User interface preferences
    val disableHapticFeedback = uiPreferences.disableHapticFeedback().asStateIn(scope)
    val disableLoadingAnimations = uiPreferences.disableLoadingAnimations().asStateIn(scope)
    
    // Leaderboard preferences
    val leaderboardRealtimeEnabled = uiPreferences.leaderboardRealtimeEnabled().asStateIn(scope)

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
