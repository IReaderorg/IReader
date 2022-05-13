package org.ireader.settings.setting

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SettingsBackupRestore
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dagger.hilt.android.lifecycle.HiltViewModel
import org.ireader.common_resources.UiText
import org.ireader.components.components.LogoHeader
import org.ireader.components.components.Toolbar
import org.ireader.components.components.component.Divider
import org.ireader.components.components.component.PreferenceRow
import org.ireader.components.components.component.SwitchPreference
import org.ireader.components.reusable_composable.BigSizeTextComposable
import org.ireader.core_ui.preferences.UiPreferences
import org.ireader.core_ui.viewmodel.BaseViewModel
import org.ireader.ui_settings.R
import javax.inject.Inject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingScreen(
    modifier: Modifier = Modifier,
    vm: MainSettingScreenViewModel,
    onDownloadScreen: () -> Unit,
    onAppearanceScreen: () -> Unit,
    onBackupScreen: () -> Unit,
    onAdvance: () -> Unit,
    onAbout: () -> Unit,
    onHelp: () -> Unit,
) {

    androidx.compose.material3.Scaffold(
        modifier = Modifier
            .padding(bottom = 50.dp)
            .fillMaxSize(),
        topBar = {
            Toolbar(
                title = {
                    BigSizeTextComposable(text = UiText.StringResource(R.string.more))
                },
            )
        }) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding),
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
                    title = stringResource(id = R.string.appearance),
                    icon = Icons.Default.Palette,
                    onClick = onAppearanceScreen,
                )
            }
            item {
                SettingsSection(
                    R.string.download,
                    Icons.Default.Download,
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
                Divider()
            }
            item {
                PreferenceRow(
                    title = stringResource(id = R.string.advance_setting),
                    icon = Icons.Default.Settings,
                    onClick = onAdvance,
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
}

data class SettingsSection(
    @StringRes val titleRes: Int,
    val icon: ImageVector? = null,
    val onClick: () -> Unit,
)

@Composable
fun SetupLayout(
    padding: PaddingValues,
    items: List<SettingsSection>
) {
    LazyColumn(
        modifier = Modifier.padding(padding),
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

@HiltViewModel
class MainSettingScreenViewModel @Inject constructor(
    uiPreferences: UiPreferences
) : BaseViewModel() {
    val incognitoMode = uiPreferences.incognitoMode().asState()
}


