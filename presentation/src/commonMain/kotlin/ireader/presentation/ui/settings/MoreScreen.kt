package ireader.presentation.ui.settings

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import cafe.adriel.voyager.navigator.currentOrThrow
import ireader.domain.preferences.prefs.UiPreferences
import ireader.i18n.Images.incognito
import ireader.i18n.localize
import ireader.presentation.ui.component.components.Divider
import ireader.presentation.ui.component.components.LogoHeader
import ireader.presentation.ui.component.components.PreferenceRow
import ireader.presentation.ui.component.components.SwitchPreference
import ireader.presentation.ui.core.theme.LocalLocalizeHelper


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreScreen(
    modifier: Modifier = Modifier,
    vm: MainSettingScreenViewModel,
    onDownloadScreen: () -> Unit,
    onBackupScreen: () -> Unit,
    onCategory: () -> Unit,
    onSettings: () -> Unit,
    onAbout: () -> Unit,
    onHelp: () -> Unit,
) {
    val localizeHelper = LocalLocalizeHelper.currentOrThrow
    LazyColumn(
        modifier = modifier,
        state = rememberLazyListState()
    ) {
        item {
            LogoHeader()
        }
        item {
            SwitchPreference(
                preference = vm.incognitoMode,
                title = localize { xml -> xml.prefIncognitoMode },
                subtitle = localize { xml -> xml.prefIncognitoModeSummary },
                icon = incognito(),
            )
        }
        item {
            Divider()
        }

        item {
            PreferenceRow(
                title = localizeHelper.localize { xml -> xml.download },
                icon = Icons.Default.Download,
                onClick = onDownloadScreen
            )
        }
        item {
            PreferenceRow(
                title = localizeHelper.localize() { xml ->
                    xml.backupAndRestore
                },
                icon = Icons.Default.SettingsBackupRestore,
                onClick = onBackupScreen,
            )
        }
        item {
            PreferenceRow(
                title = localizeHelper.localize { xml -> xml.category },
                icon = Icons.Default.Label,
                onClick = onCategory,
            )
        }

        item {
            Divider()
        }
        item {
            PreferenceRow(
                title = localizeHelper.localize { xml -> xml.settings },
                icon = Icons.Default.Settings,
                onClick = onSettings,
            )
        }
        item {
            PreferenceRow(
                title = localizeHelper.localize() { xml ->
                    xml.about
                },
                icon = Icons.Default.Info,
                onClick = onAbout,
            )
        }

        item {
            PreferenceRow(
                title = localizeHelper.localize { xml -> xml.help },
                icon = Icons.Default.Help,
                onClick = onHelp,
            )
        }
    }
}

data class SettingsSection(
    val titleRes: String,
    val icon: ImageVector? = null,
    val onClick: () -> Unit,
)

@Composable
fun SetupLayout(
    modifier: Modifier = Modifier,
    items: List<SettingsSection>,
    padding: PaddingValues? = null,
) {
    LazyColumn(
        modifier = if (padding != null) modifier.padding(padding) else modifier,
    ) {
        items.map {
            item {
                PreferenceRow(
                    title = localize { _ -> it.titleRes },
                    icon = it.icon,
                    onClick = it.onClick,
                )
            }
        }
    }
}


class MainSettingScreenViewModel(
    uiPreferences: UiPreferences
) : ireader.presentation.ui.core.viewmodel.BaseViewModel() {
    val incognitoMode = uiPreferences.incognitoMode().asState()
}
