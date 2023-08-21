package ireader.presentation.ui.settings.general

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import cafe.adriel.voyager.navigator.currentOrThrow
import ireader.domain.models.prefs.PreferenceValues
import ireader.domain.preferences.prefs.AppPreferences
import ireader.domain.preferences.prefs.UiPreferences

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
                title = localizeHelper.localize { xml ->
                    xml.updaterIsEnable
                }
            ),
            Components.Switch(
                preference = vm.showHistory,
                title = localizeHelper.localize { xml ->
                    xml.showHistory
                }
            ),
            Components.Switch(
                preference = vm.showUpdate,
                title = localizeHelper.localize { xml ->
                    xml.showUpdate
                }
            ),
            Components.Switch(
                preference = vm.confirmExit,
                title = localizeHelper.localize { xml ->
                    xml.confirmExit
                }
            ),
            Components.Dynamic {
                ChoicePreference<PreferenceValues.Installer>(
                    preference = vm.installer,
                    choices = mapOf(
                        PreferenceValues.Installer.AndroidPackageManager to localizeHelper.localize { xml ->
                            xml.packageManager
                        },
                        PreferenceValues.Installer.LocalInstaller to localizeHelper.localize { xml ->
                            xml.localInstaller
                        },
                    ),
                    title = localizeHelper.localize { xml ->
                        xml.installerMode
                    },
                )
            },
            Components.Switch(
                preference = vm.showSystemWideCatalogs,
                title = localizeHelper.localize { xml ->
                    xml.showSystemCatalogs
                }
            ),
            Components.Switch(
                preference = vm.showLocalCatalogs,
                title = localizeHelper.localize { xml ->
                    xml.showLocalCatalogs
                }
            ),
            Components.Switch(
                preference = vm.autoInstaller,
                title = localizeHelper.localize { xml ->
                    xml.autoInstaller
                }
            ),
            Components.Switch(
                preference = vm.localSourceLocation,
                title = localizeHelper.localize { xml ->
                    xml.savedLocalSourceLocation
                }
            ),
            manageNotificationComponent,
            Components.Dynamic {
                ChoicePreference<String>(
                    preference = vm.language,
                    choices = vm.getLanguageChoices(),
                    title = localizeHelper.localize { xml ->
                        xml.languages
                    },
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
        val lanuages = localeHelper.languages.mapNotNull {
            SubtitleHelper.fromTwoLettersToLanguage(it)?.let { lang -> it to lang }
        }
        return lanuages.toMap()
    }
}
