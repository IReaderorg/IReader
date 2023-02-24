package ireader.presentation.ui.settings

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SettingsBackupRestore
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import ireader.presentation.ui.component.components.LogoHeader
import ireader.presentation.ui.component.components.component.Divider
import ireader.presentation.ui.component.components.component.PreferenceRow
import ireader.presentation.ui.component.components.component.SwitchPreference
import ireader.domain.preferences.prefs.UiPreferences
import ireader.presentation.ui.core.viewmodel.BaseViewModel
import ireader.presentation.R
import org.koin.android.annotation.KoinViewModel

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
                title = stringResource(R.string.pref_incognito_mode),
                subtitle = stringResource(R.string.pref_incognito_mode_summary),
                painter = painterResource(R.drawable.ic_glasses_24dp),
            )
        }
        item {
            Divider()
        }

        item {
            PreferenceRow(
                title = stringResource(id = R.string.download),
                icon = Icons.Default.Download,
                onClick = onDownloadScreen
            )
        }
        item {
            PreferenceRow(
                title = stringResource(id = R.string.backup_and_restore),
                icon = Icons.Default.SettingsBackupRestore,
                onClick = onBackupScreen,
            )
        }
        item {
            PreferenceRow(
                title = stringResource(id = R.string.category),
                icon = Icons.Default.Label,
                onClick = onCategory,
            )
        }

        item {
            Divider()
        }
        item {
            PreferenceRow(
                title = stringResource(id = R.string.settings),
                icon = Icons.Default.Settings,
                onClick = onSettings,
            )
        }
        item {
            PreferenceRow(
                title = stringResource(id = R.string.about),
                icon = Icons.Default.Info,
                onClick = onAbout,
            )
        }

        item {
            PreferenceRow(
                title = stringResource(id = R.string.help),
                icon = Icons.Default.Help,
                onClick = onHelp,
            )
        }
    }
}

data class SettingsSection(
    @StringRes val titleRes: Int,
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
        modifier = if (padding != null)modifier.padding(padding) else modifier,
    ) {
        items.map {
            item {
                PreferenceRow(
                    title = stringResource(it.titleRes),
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
