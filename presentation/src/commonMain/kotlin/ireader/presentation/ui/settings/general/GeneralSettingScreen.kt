package ireader.presentation.ui.settings.general

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
                Components.Switch(
                        preference = vm.appUpdater,
                        title = localizeHelper.localize(MR.strings.updater_is_enable)
                ),
                Components.Switch(
                        preference = vm.showHistory,
                        title = localizeHelper.localize(MR.strings.show_history)
                ),
                Components.Switch(
                        preference = vm.showUpdate,
                        title = localizeHelper.localize(MR.strings.show_update)
                ),
                Components.Switch(
                        preference = vm.confirmExit,
                        title = localizeHelper.localize(MR.strings.confirm_exit)
                ),
                Components.Dynamic {
                    NavigationPreferenceCustom(
                        title = localizeHelper.localize(MR.strings.translation_settings),
                        subtitle = localizeHelper.localize(MR.strings.api_settings),
                        icon = { Icon(Icons.Default.Translate, contentDescription = null) },
                        onClick = onTranslationSettingsClick
                    )
                },
                Components.Slider(
                    preferenceAsLong = vm.downloadDelayMs,
                    title = "Download Delay (milliseconds)",
                    subtitle = "Delay between chapter downloads to avoid IP blocking",
                    valueRange = 0f..10000f,
                    steps = 19,
                    trailing = "${vm.downloadDelayMs.value}ms"
                ),
                Components.Slider(
                    preferenceAsInt = vm.concurrentDownloads,
                    title = "Concurrent Downloads",
                    subtitle = "Maximum number of simultaneous downloads",
                    valueRange = 1f..10f,
                    steps = 8,
                    trailing = vm.concurrentDownloads.value.toString()
                ),
                Components.Dynamic {
                    ChoicePreference<PreferenceValues.Installer>(
                            preference = vm.installer,
                            choices = mapOf(
                                    PreferenceValues.Installer.AndroidPackageManager to localizeHelper.localize(MR.strings.package_manager),
                                    PreferenceValues.Installer.LocalInstaller to localizeHelper.localize(MR.strings.local_installer),
                            ),
                            title = localizeHelper.localize(
                                    MR.strings.installer_mode
                            ),
                    )
                },
                Components.Switch(
                        preference = vm.showSystemWideCatalogs,
                        title = localizeHelper.localize(MR.strings.show_system_catalogs)
                ),
                Components.Switch(
                        preference = vm.showLocalCatalogs,
                        title = localizeHelper.localize(MR.strings.show_local_catalogs)
                ),
                Components.Switch(
                        preference = vm.autoInstaller,
                        title = localizeHelper.localize(MR.strings.auto_installer)
                ),
                Components.Switch(
                        preference = vm.localSourceLocation,
                        title = localizeHelper.localize(MR.strings.saved_local_source_location)
                ),
                manageNotificationComponent,
                Components.Dynamic {
                    ChoicePreference<String>(
                            preference = vm.language,
                            choices = vm.getLanguageChoices(),
                            title = localizeHelper.localize(
                                    MR.strings.languages
                            ),
                            onValue = { value ->
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

@Composable
fun NavigationPreferenceCustom(
    title: String,
    subtitle: String? = null,
    icon: @Composable (() -> Unit)? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Box(
                modifier = Modifier
                    .padding(end = 16.dp)
                    .size(24.dp),
                contentAlignment = Alignment.Center
            ) {
                icon()
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}

class GeneralSettingScreenViewModel(
        private val appPreferences: AppPreferences,
        private val uiPreferences: UiPreferences,
        private val downloadPreferences: ireader.domain.preferences.prefs.DownloadPreferences,
        val localeHelper: LocaleHelper
) : ireader.presentation.ui.core.viewmodel.BaseViewModel() {

    val appUpdater = appPreferences.appUpdater().asState()

    var showHistory = uiPreferences.showHistoryInButtonBar().asState()
    var showUpdate = uiPreferences.showUpdatesInButtonBar().asState()
    var confirmExit = uiPreferences.confirmExit().asState()
    var installer = uiPreferences.installerMode().asState()
    var language = uiPreferences.language().asState()
    var showSystemWideCatalogs = uiPreferences.showSystemWideCatalogs().asState()
    var showLocalCatalogs = uiPreferences.showLocalCatalogs().asState()
    var autoInstaller = uiPreferences.autoCatalogUpdater().asState()
    var localSourceLocation = uiPreferences.savedLocalCatalogLocation().asState()
    
    // Download preferences
    val downloadDelayMs = downloadPreferences.downloadDelayMs().asState()
    val concurrentDownloads = downloadPreferences.concurrentDownloadsLimit().asState()

    @Composable
    fun getLanguageChoices(): Map<String, String> {
        val lanuages = localeHelper.languages.mapNotNull { SubtitleHelper.fromTwoLettersToLanguage(it)?.let { lang -> it to lang } }
        return lanuages.toMap()
    }
}
