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
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DisplaySettings
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.Update
import androidx.compose.material.icons.filled.Warning
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
import ireader.i18n.resources.add_novels_to_library_only
import ireader.i18n.resources.allow_loading_lnreader_compatible_javascript
import ireader.i18n.resources.api_settings
import ireader.i18n.resources.app_updates_display
import ireader.i18n.resources.auto_download
import ireader.i18n.resources.auto_installer
import ireader.i18n.resources.auto_installer_subtitle
import ireader.i18n.resources.automatically_download_newly_detected_chapters
import ireader.i18n.resources.automatically_refresh_leaderboard_when_other
import ireader.i18n.resources.catalog_settings
import ireader.i18n.resources.community_leaderboard
import ireader.i18n.resources.configure_js_plugins_and_enable
import ireader.i18n.resources.confirm_exit
import ireader.i18n.resources.default_chapter_sort
import ireader.i18n.resources.default_sorting_method_for_chapter_lists
import ireader.i18n.resources.delay_between_chapter_downloads_to
import ireader.i18n.resources.disable_haptic_feedback
import ireader.i18n.resources.disable_loading_animations
import ireader.i18n.resources.display_auto_populated_categories_like
import ireader.i18n.resources.display_when_each_novel_was
import ireader.i18n.resources.download_delay
import ireader.i18n.resources.download_new_chapters
import ireader.i18n.resources.download_settings
import ireader.i18n.resources.enable_javascript_plugins
import ireader.i18n.resources.global_search
import ireader.i18n.resources.hybrid_installer
import ireader.i18n.resources.installer_mode
import ireader.i18n.resources.javascript_plugin_settings
import ireader.i18n.resources.language_translation
import ireader.i18n.resources.languages
import ireader.i18n.resources.leaderboard_realtime_updates
import ireader.i18n.resources.library_settings
import ireader.i18n.resources.local_installer
import ireader.i18n.resources.max_performance_mode
import ireader.i18n.resources.max_performance_mode_subtitle
import ireader.i18n.resources.notifications
import ireader.i18n.resources.only_update_on_finding_novel
import ireader.i18n.resources.package_manager
import ireader.i18n.resources.replace_toolbar_buttons_with_floating
import ireader.i18n.resources.saved_local_source_location
import ireader.i18n.resources.show_history
import ireader.i18n.resources.show_last_update_time
import ireader.i18n.resources.show_local_catalogs
import ireader.i18n.resources.show_local_catalogs_subtitle
import ireader.i18n.resources.show_smart_categories
import ireader.i18n.resources.show_system_catalogs
import ireader.i18n.resources.show_system_catalogs_subtitle
import ireader.i18n.resources.show_update
import ireader.i18n.resources.translation_settings
import ireader.i18n.resources.turn_off_vibration_feedback_for_interactions
import ireader.i18n.resources.updater_is_enable
import ireader.i18n.resources.use_fab_in_library
import ireader.i18n.resources.use_static_indicators_instead_of_animated_ones
import ireader.i18n.resources.user_interface
import ireader.presentation.core.theme.LocaleHelper
import ireader.presentation.ui.component.components.ChoicePreference
import ireader.presentation.ui.component.components.Components
import ireader.presentation.ui.component.components.SetupSettingComponents
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.presentation.ui.core.ui.asStateIn

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
                // Language Section - Moved to top for better visibility
                Components.Header(
                        text = localizeHelper.localize(Res.string.language_translation),
                        padding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                        icon = Icons.Filled.Language
                ),
                Components.Dynamic {
                    ChoicePreference(
                            preference = vm.language,
                            choices = vm.getLanguageChoices(),
                            title = localizeHelper.localize(
                                    Res.string.languages
                            ),
                            icon = Icons.Filled.Language,
                            onValue = { value: String ->
                                vm.language.value = value
                                vm.localeHelper.updateLocal()
                            }
                    )
                },
                Components.Dynamic {
                    NavigationPreferenceCustom(
                        title = localizeHelper.localize(Res.string.translation_settings),
                        subtitle = localizeHelper.localize(Res.string.api_settings),
                        icon = { Icon(Icons.Filled.Translate, contentDescription = null) },
                        onClick = onTranslationSettingsClick
                    )
                },
                
                Components.Space,
                
                // App Updates & Display Section
                Components.Header(
                        text = localizeHelper.localize(Res.string.app_updates_display),
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
                        text = localizeHelper.localize(Res.string.library_settings),
                        padding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                        icon = Icons.Filled.Storage
                ),
                Components.Switch(
                        preference = vm.showSmartCategories,
                        title = localizeHelper.localize(Res.string.show_smart_categories),
                        subtitle = localizeHelper.localize(Res.string.display_auto_populated_categories_like),
                        icon = Icons.Filled.Storage
                ),
                Components.Switch(
                        preference = vm.useFabInLibrary,
                        title = localizeHelper.localize(Res.string.use_fab_in_library),
                        subtitle = localizeHelper.localize(Res.string.replace_toolbar_buttons_with_floating),
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
                        title = localizeHelper.localize(Res.string.default_chapter_sort),
                        subtitle = localizeHelper.localize(Res.string.default_sorting_method_for_chapter_lists)
                    )
                },
                Components.Switch(
                        preference = vm.showCharacterArtInDetails,
                        title = "Show Character Art",
                        subtitle = "Display AI-generated character art gallery in book details",
                        icon = Icons.Filled.Settings
                ),
                
                Components.Space,
                
                // Global Search Section
                Components.Header(
                        text = localizeHelper.localize(Res.string.global_search),
                        padding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                        icon = Icons.Filled.Search
                ),
                Components.Switch(
                        preference = vm.onlyUpdateOnFinding,
                        title = localizeHelper.localize(Res.string.only_update_on_finding_novel),
                        subtitle = localizeHelper.localize(Res.string.add_novels_to_library_only),
                        icon = Icons.Filled.Search
                ),
                Components.Switch(
                        preference = vm.showLastUpdateTime,
                        title = localizeHelper.localize(Res.string.show_last_update_time),
                        subtitle = localizeHelper.localize(Res.string.display_when_each_novel_was),
                        icon = Icons.Filled.Update
                ),
                
                Components.Space,
                
                // Auto Download Section
                Components.Header(
                        text = localizeHelper.localize(Res.string.auto_download),
                        padding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                        icon = Icons.Filled.Download
                ),
                Components.Switch(
                        preference = vm.autoDownloadNewChapters,
                        title = localizeHelper.localize(Res.string.download_new_chapters),
                        subtitle = localizeHelper.localize(Res.string.automatically_download_newly_detected_chapters),
                        icon = Icons.Filled.Download
                ),
                
                Components.Space,
                
                // Mass Translation Section
                Components.Header(
                        text = "Mass Translation",
                        padding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                        icon = Icons.Filled.Translate
                ),
                Components.Dynamic {
                    vm.bypassTranslationWarning?.let { pref ->
                        ireader.presentation.ui.component.components.SwitchPreference(
                            preference = pref,
                            title = "Bypass Translation Warning",
                            subtitle = "Skip rate limit warnings when mass translating chapters. May exhaust API credits or cause IP blocks.",
                            icon = Icons.Filled.Warning
                        )
                    }
                },
                Components.Dynamic {
                    vm.translationRateLimitDelay?.let { pref ->
                        ireader.presentation.ui.component.components.SliderPreference(
                            preferenceAsLong = pref,
                            title = "Translation Rate Limit Delay",
                            subtitle = "Delay between translation requests to prevent rate limiting",
                            icon = Icons.Filled.Timer,
                            valueRange = 1000f..10000f,
                            steps = 17,
                            trailingFormatter = { value -> "${(value / 1000).toInt()}s" }
                        )
                    }
                },
                
                Components.Space,
                
                // User Interface Section
                Components.Header(
                        text = localizeHelper.localize(Res.string.user_interface),
                        padding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                        icon = Icons.Filled.DisplaySettings
                ),
                Components.Switch(
                        preference = vm.maxPerformanceMode,
                        title = localizeHelper.localize(Res.string.max_performance_mode),
                        subtitle = localizeHelper.localize(Res.string.max_performance_mode_subtitle),
                        icon = Icons.Filled.Speed
                ),
                Components.Switch(
                        preference = vm.disableHapticFeedback,
                        title = localizeHelper.localize(Res.string.disable_haptic_feedback),
                        subtitle = localizeHelper.localize(Res.string.turn_off_vibration_feedback_for_interactions),
                        icon = Icons.Filled.TouchApp
                ),
                Components.Switch(
                        preference = vm.disableLoadingAnimations,
                        title = localizeHelper.localize(Res.string.disable_loading_animations),
                        subtitle = localizeHelper.localize(Res.string.use_static_indicators_instead_of_animated_ones),
                        icon = Icons.Filled.Autorenew
                ),
                
                Components.Space,
                
                // Community & Leaderboard Section
                Components.Header(
                        text = localizeHelper.localize(Res.string.community_leaderboard),
                        padding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                        icon = Icons.Filled.Settings
                ),
                Components.Switch(
                        preference = vm.leaderboardRealtimeEnabled,
                        title = localizeHelper.localize(Res.string.leaderboard_realtime_updates),
                        subtitle = localizeHelper.localize(Res.string.automatically_refresh_leaderboard_when_other),
                        icon = Icons.Filled.Autorenew
                ),
                
                Components.Space,
                
                // Download Settings Section
                Components.Header(
                        text = localizeHelper.localize(Res.string.download_settings),
                        padding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                        icon = Icons.Filled.Download
                ),
                Components.Slider(
                    preferenceAsLong = vm.downloadDelayMs,
                    title = localizeHelper.localize(Res.string.download_delay),
                    subtitle = localizeHelper.localize(Res.string.delay_between_chapter_downloads_to),
                    icon = Icons.Filled.Download,
                    valueRange = 0f..10000f,
                    steps = 19,
                    trailingFormatter = { value -> vm.formatDownloadDelay(value.toLong()) }
                ),

                
                Components.Space,
                
                // Catalog Settings Section
                Components.Header(
                        text = localizeHelper.localize(Res.string.catalog_settings),
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
                        title = localizeHelper.localize(Res.string.javascript_plugin_settings),
                        subtitle = localizeHelper.localize(Res.string.configure_js_plugins_and_enable),
                        icon = { Icon(Icons.Filled.Code, contentDescription = null) },
                        onClick = onJSPluginSettingsClick
                    )
                },
                Components.Switch(
                        preference = vm.enableJSPlugins,
                        title = localizeHelper.localize(Res.string.enable_javascript_plugins),
                        subtitle = localizeHelper.localize(Res.string.allow_loading_lnreader_compatible_javascript),
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
                            PreferenceValues.Installer.HybridInstaller to localizeHelper.localize(Res.string.hybrid_installer),
                        ),
                        title = localizeHelper.localize(
                            Res.string.installer_mode
                        )
                    )
                },
                
                Components.Space,
                
                // Notifications Section
                Components.Header(
                        text = localizeHelper.localize(Res.string.notifications),
                        padding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                        icon = Icons.Filled.Notifications
                ),
                manageNotificationComponent,
                
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
        private val translationPreferences: ireader.domain.preferences.prefs.TranslationPreferences? = null,
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
    
    // Translation preferences
    val bypassTranslationWarning = translationPreferences?.bypassTranslationWarning()?.asStateIn(scope)
    val translationRateLimitDelay = translationPreferences?.translationRateLimitDelayMs()?.asStateIn(scope)
    
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
    val maxPerformanceMode = uiPreferences.maxPerformanceMode().asStateIn(scope)
    val disableHapticFeedback = uiPreferences.disableHapticFeedback().asStateIn(scope)
    val disableLoadingAnimations = uiPreferences.disableLoadingAnimations().asStateIn(scope)
    
    // Leaderboard preferences
    val leaderboardRealtimeEnabled = uiPreferences.leaderboardRealtimeEnabled().asStateIn(scope)
    
    // Character Art preferences
    val showCharacterArtInDetails = uiPreferences.showCharacterArtInDetails().asStateIn(scope)

    /**
     * Returns language choices filtered to only include languages that have
     * available translations in the app. This ensures users only see languages
     * they can actually use.
     */
    @Composable
    fun getLanguageChoices(): Map<String, String> {
        // Use the predefined list of available locales from AppLocales
        return ireader.presentation.core.theme.AppLocales.AVAILABLE_LOCALES
            .associateWith { code ->
                ireader.presentation.core.theme.AppLocales.getDisplayName(code)
            }
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
                    "${ireader.presentation.ui.core.utils.formatSeconds(seconds)}"
                }
            }
            else -> "${delayMs}ms"
        }
    }
}
