package ireader.presentation.core.ui

import ireader.presentation.core.LocalNavigator
import ireader.presentation.core.NavigationRoutes

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ireader.i18n.localize
import ireader.i18n.resources.Res
import ireader.i18n.resources.*
import ireader.presentation.ui.component.IScaffold
import ireader.presentation.ui.component.components.Toolbar
import ireader.presentation.ui.component.reusable_composable.BigSizeTextComposable
import ireader.presentation.ui.component.reusable_composable.TopAppBarBackButton
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.presentation.ui.settings.components.SettingsItem
import ireader.presentation.ui.settings.components.SettingsSectionHeader

class SettingScreenSpec {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Content() {
        val navController = requireNotNull(LocalNavigator.current) { "LocalNavigator not provided" }
        val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
        
        IScaffold(
            topBar = { scrollBehavior ->
                Toolbar(
                    scrollBehavior = scrollBehavior,
                    title = {
                        BigSizeTextComposable(text = localize(Res.string.settings))
                    },
                    navigationIcon = { TopAppBarBackButton(onClick = { navController.popBackStack() }) },
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
                        title = localizeHelper.localize(Res.string.appearance),
                        icon = Icons.Default.Palette
                    )
                }
                
                item {
                    SettingsItem(
                        title = localizeHelper.localize(Res.string.appearance),
                        description = "Customize app theme and colors",
                        icon = Icons.Default.Palette,
                        onClick = { navController.navigate(NavigationRoutes.appearance) }
                    )
                }
                
                item {
                    SettingsItem(
                        title = localizeHelper.localize(Res.string.font),
                        description = "Choose reading fonts and sizes",
                        icon = Icons.Default.FontDownload,
                        onClick = { navController.navigate(NavigationRoutes.fontSettings) }
                    )
                }
                
                // General Settings Section
                item {
                    SettingsSectionHeader(
                        title = localizeHelper.localize(Res.string.general),
                        icon = Icons.Default.Tune
                    )
                }
                
                item {
                    SettingsItem(
                        title = localizeHelper.localize(Res.string.general),
                        description = "General app preferences",
                        icon = Icons.Default.Tune,
                        onClick = { navController.navigate(NavigationRoutes.generalSettings) }
                    )
                }
                
                item {
                    SettingsItem(
                        title = localizeHelper.localize(Res.string.translation_settings),
                        description = "Configure translation preferences",
                        icon = Icons.Default.Translate,
                        onClick = { navController.navigate(NavigationRoutes.translationSettings) }
                    )
                }
                item {
                    SettingsItem(
                        title = "Supabase Configuration",
                        description = "Configure custom Supabase instance for sync",
                        icon = Icons.Outlined.Cloud,
                        onClick = {navController.navigate(NavigationRoutes.supabaseConfig)}
                    )
                }

                // Reading Experience Section
                item {
                    SettingsSectionHeader(
                        title = localizeHelper.localize(Res.string.reader),
                        icon = Icons.Default.ChromeReaderMode
                    )
                }
                
                item {
                    SettingsItem(
                        title = localizeHelper.localize(Res.string.reader),
                        description = "Customize reading experience",
                        icon = Icons.Default.ChromeReaderMode,
                        onClick = { navController.navigate(NavigationRoutes.readerSettings) }
                    )
                }
                
                item {
                    SettingsItem(
                        title = localizeHelper.localize(Res.string.statistics),
                        description = "View reading statistics and progress",
                        icon = Icons.Default.BarChart,
                        onClick = { navController.navigate(NavigationRoutes.statistics) }
                    )
                }
                
                // Security & Privacy Section
                item {
                    SettingsSectionHeader(
                        title = localizeHelper.localize(Res.string.security),
                        icon = Icons.Default.Security
                    )
                }
                
                item {
                    SettingsItem(
                        title = localizeHelper.localize(Res.string.security),
                        description = "Manage security and privacy settings",
                        icon = Icons.Default.Security,
                        onClick = { navController.navigate(NavigationRoutes.securitySettings) }
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
                        title = localizeHelper.localize(Res.string.repository),
                        description = "Manage content sources and extensions",
                        icon = Icons.Default.Extension,
                        onClick = { navController.navigate(NavigationRoutes.repository) }
                    )
                }
                
                item {
                    SettingsItem(
                        title = localizeHelper.localize(Res.string.advance_setting),
                        description = "Advanced configuration options",
                        icon = Icons.Default.Code,
                        onClick = { navController.navigate(NavigationRoutes.advanceSettings) }
                    )
                }
            }
        }
    }
}
