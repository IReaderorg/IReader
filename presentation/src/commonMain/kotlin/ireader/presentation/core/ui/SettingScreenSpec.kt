package ireader.presentation.core.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import ireader.i18n.localize
import ireader.i18n.resources.MR
import ireader.presentation.core.VoyagerScreen
import ireader.presentation.ui.component.IScaffold
import ireader.presentation.ui.component.components.Toolbar
import ireader.presentation.ui.component.reusable_composable.BigSizeTextComposable
import ireader.presentation.ui.component.reusable_composable.TopAppBarBackButton
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.presentation.ui.settings.components.SettingsItem
import ireader.presentation.ui.settings.components.SettingsSectionHeader

class SettingScreenSpec : VoyagerScreen() {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val localizeHelper = LocalLocalizeHelper.currentOrThrow
        
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
            LazyColumn(
                modifier = Modifier.padding(padding),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                // Appearance & Display Section
                item {
                    SettingsSectionHeader(
                        title = localizeHelper.localize(MR.strings.appearance),
                        icon = Icons.Default.Palette
                    )
                }
                
                item {
                    SettingsItem(
                        title = localizeHelper.localize(MR.strings.appearance),
                        description = "Customize app theme and colors",
                        icon = Icons.Default.Palette,
                        onClick = { navigator.push(AppearanceScreenSpec()) }
                    )
                }
                
                item {
                    SettingsItem(
                        title = localizeHelper.localize(MR.strings.font),
                        description = "Choose reading fonts and sizes",
                        icon = Icons.Default.FontDownload,
                        onClick = { navigator.push(FontScreenSpec()) }
                    )
                }
                
                // General Settings Section
                item {
                    SettingsSectionHeader(
                        title = localizeHelper.localize(MR.strings.general),
                        icon = Icons.Default.Tune
                    )
                }
                
                item {
                    SettingsItem(
                        title = localizeHelper.localize(MR.strings.general),
                        description = "General app preferences",
                        icon = Icons.Default.Tune,
                        onClick = { navigator.push(GeneralScreenSpec()) }
                    )
                }
                
                item {
                    SettingsItem(
                        title = localizeHelper.localize(MR.strings.translation_settings),
                        description = "Configure translation preferences",
                        icon = Icons.Default.Translate,
                        onClick = { navigator.push(TranslationScreenSpec()) }
                    )
                }
                
                // Reading Experience Section
                item {
                    SettingsSectionHeader(
                        title = localizeHelper.localize(MR.strings.reader),
                        icon = Icons.Default.ChromeReaderMode
                    )
                }
                
                item {
                    SettingsItem(
                        title = localizeHelper.localize(MR.strings.reader),
                        description = "Customize reading experience",
                        icon = Icons.Default.ChromeReaderMode,
                        onClick = { navigator.push(ReaderSettingSpec()) }
                    )
                }
                
                item {
                    SettingsItem(
                        title = localizeHelper.localize(MR.strings.statistics),
                        description = "View reading statistics and progress",
                        icon = Icons.Default.BarChart,
                        onClick = { navigator.push(StatisticsScreenSpec()) }
                    )
                }
                
                // Security & Privacy Section
                item {
                    SettingsSectionHeader(
                        title = localizeHelper.localize(MR.strings.security),
                        icon = Icons.Default.Security
                    )
                }
                
                item {
                    SettingsItem(
                        title = localizeHelper.localize(MR.strings.security),
                        description = "Manage security and privacy settings",
                        icon = Icons.Default.Security,
                        onClick = { navigator.push(SecuritySettingSpec()) }
                    )
                }
                
                // Advanced Section
                item {
                    SettingsSectionHeader(
                        title = "Advanced",
                        icon = Icons.Default.Code
                    )
                }
                
                item {
                    SettingsItem(
                        title = localizeHelper.localize(MR.strings.repository),
                        description = "Manage content sources and extensions",
                        icon = Icons.Default.Extension,
                        onClick = { navigator.push(RepositoryScreenSpec()) }
                    )
                }
                
                item {
                    SettingsItem(
                        title = localizeHelper.localize(MR.strings.advance_setting),
                        description = "Advanced configuration options",
                        icon = Icons.Default.Code,
                        onClick = { navigator.push(AdvanceSettingSpec()) }
                    )
                }
            }
        }
    }
}
