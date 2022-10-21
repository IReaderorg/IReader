package ireader.presentation.ui.settings.general

import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import ireader.domain.models.prefs.PreferenceValues
import ireader.domain.preferences.prefs.AppPreferences
import ireader.domain.preferences.prefs.UiPreferences
import ireader.presentation.R
import ireader.presentation.ui.component.components.Components
import ireader.presentation.ui.component.components.SetupSettingComponents
import ireader.presentation.ui.component.components.component.ChoicePreference
import ireader.presentation.ui.core.viewmodel.BaseViewModel
import org.koin.android.annotation.KoinViewModel

@Composable
fun GeneralSettingScreen(
    scaffoldPadding: PaddingValues,
    vm: GeneralSettingScreenViewModel,
) {
    val context = LocalContext.current
    val items = remember {
        listOf<Components>(
            Components.Switch(
                preference = vm.appUpdater,
                title = context.getString(R.string.updater_is_enable)
            ),
            Components.Switch(
                preference = vm.showHistory,
                title = context.getString(R.string.show_history)
            ),
            Components.Switch(
                preference = vm.showUpdate,
                title = context.getString(R.string.show_update)
            ),
            Components.Switch(
                preference = vm.confirmExit,
                title = context.getString(R.string.confirm_exit)
            ),
            Components.Dynamic {
                ChoicePreference<PreferenceValues.Installer>(
                    preference = vm.installer,
                    choices = mapOf(
                        PreferenceValues.Installer.AndroidPackageManager to context.getString(R.string.package_manager),
                        PreferenceValues.Installer.LocalInstaller to context.getString(R.string.local_installer),
                    ),
                    title = stringResource(
                        id = R.string.installer_mode
                    ),
                )
            },
            Components.Switch(
                preference = vm.showSystemWideCatalogs,
                title = context.getString(R.string.show_system_catalogs)
            ),
            Components.Switch(
                preference = vm.showLocalCatalogs,
                title = context.getString(R.string.show_local_catalogs)
            ),
            Components.Switch(
                preference = vm.autoInstaller,
                title = context.getString(R.string.auto_installer)
            ),
            Components.Row(
                title = context.getString(R.string.manage_notification),
                onClick = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                            putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                        }
                        context.startActivity(intent)
                    }
                },
                visible = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
            ),
            Components.Dynamic {
                ChoicePreference<String>(
                    preference = vm.language,
                    choices = vm.getLanguageChoices(),
                    title = stringResource(
                        id = R.string.languages
                    ),
                )
            }
        )
    }

    SetupSettingComponents(
        scaffoldPadding = scaffoldPadding,
        items = items
    )
}

@KoinViewModel
class GeneralSettingScreenViewModel(
    private val appPreferences: AppPreferences,
    private val uiPreferences: UiPreferences
) : BaseViewModel() {

    val appUpdater = appPreferences.appUpdater().asState()

    var showHistory = uiPreferences.showHistoryInButtonBar().asState()
    var showUpdate = uiPreferences.showUpdatesInButtonBar().asState()
    var confirmExit = uiPreferences.confirmExit().asState()
    var installer = uiPreferences.installerMode().asState()
    var language = uiPreferences.language().asState()
    var showSystemWideCatalogs = uiPreferences.showSystemWideCatalogs().asState()
    var showLocalCatalogs = uiPreferences.showLocalCatalogs().asState()
    var autoInstaller = uiPreferences.autoCatalogUpdater().asState()

    @Composable
    fun getLanguageChoices(): Map<String, String> {
        val currentLocaleDisplayName = androidx.compose.ui.text.intl.Locale.current.toLanguageTag()
        return mapOf(
            "" to "${stringResource(R.string.system_default)} ($currentLocaleDisplayName)"
        )
    }
}
