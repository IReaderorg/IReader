package ireader.presentation.ui.settings.general

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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


class GeneralSettingScreenViewModel(
        private val appPreferences: AppPreferences,
        private val uiPreferences: UiPreferences,
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

    @Composable
    fun getLanguageChoices(): Map<String, String> {
        val lanuages = localeHelper.languages.mapNotNull { SubtitleHelper.fromTwoLettersToLanguage(it)?.let { lang -> it to lang } }
        return lanuages.toMap()
    }
}
