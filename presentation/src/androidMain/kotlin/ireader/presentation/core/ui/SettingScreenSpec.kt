package ireader.presentation.core.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import ireader.i18n.localize
import ireader.i18n.resources.MR
import ireader.presentation.R
import ireader.presentation.core.VoyagerScreen
import ireader.presentation.ui.component.IScaffold
import ireader.presentation.ui.component.components.Toolbar
import ireader.presentation.ui.component.reusable_composable.BigSizeTextComposable
import ireader.presentation.ui.component.reusable_composable.TopAppBarBackButton
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.presentation.ui.settings.SettingsSection
import ireader.presentation.ui.settings.SetupLayout

class SettingScreenSpec : VoyagerScreen() {

    @OptIn(
         ExperimentalMaterial3Api::class
    )
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val localizeHelper = LocalLocalizeHelper.currentOrThrow
        val settingItems = remember {
            listOf(
                SettingsSection(
                    R.string.appearance,
                    Icons.Default.Palette,
                ) {
                    navigator.push(AppearanceScreenSpec())
                },
                SettingsSection(
                    R.string.general,
                    Icons.Default.Tune,
                ) {
                    navigator.push(GeneralScreenSpec())
                },
                SettingsSection(
                    R.string.reader,
                    Icons.Default.ChromeReaderMode,
                ) {
                    navigator.push(ReaderSettingSpec())

                },
                SettingsSection(
                    R.string.security,
                    Icons.Default.Security,
                ) {
                    navigator.push(SecuritySettingSpec())

                },
                SettingsSection(
                    R.string.repository,
                    Icons.Default.Extension,
                ) {
                    navigator.push(RepositoryScreenSpec())

                },
                SettingsSection(
                    R.string.advance_setting,
                    Icons.Default.Code
                ) {
                    navigator.push(AdvanceSettingSpec())

                },
            )
        }
        IScaffold(
            topBar = { scrollBehavior ->
                Toolbar(
                    scrollBehavior = scrollBehavior,
                    title = {
                        BigSizeTextComposable(text = localize(MR.strings.settings))
                    },
                    navigationIcon = { TopAppBarBackButton(onClick = { popBackStack(navigator) }) },
                )
            }
        ) { padding ->
            SetupLayout(modifier = Modifier.padding(padding), items = settingItems)
        }

    }
}
