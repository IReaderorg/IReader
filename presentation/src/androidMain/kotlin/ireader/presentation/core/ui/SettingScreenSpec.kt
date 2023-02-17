package ireader.presentation.core.ui

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import ireader.presentation.R
import ireader.presentation.ui.component.Controller
import ireader.presentation.ui.component.IScaffold
import ireader.presentation.ui.component.components.Toolbar
import ireader.presentation.ui.component.reusable_composable.BigSizeTextComposable
import ireader.presentation.ui.component.reusable_composable.TopAppBarBackButton
import ireader.presentation.ui.settings.SettingsSection
import ireader.presentation.ui.settings.SetupLayout

object SettingScreenSpec : ScreenSpec {
    override val navHostRoute: String = "settings"

    @OptIn(
        ExperimentalAnimationApi::class,
        ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class
    )
    @Composable
    override fun Content(
        controller: Controller
    ) {
        val settingItems = remember {
            listOf(
                SettingsSection(
                    R.string.appearance,
                    Icons.Default.Palette,
                ) {
                    controller.navController.navigate(AppearanceScreenSpec.navHostRoute)
                },
                SettingsSection(
                    R.string.general,
                    Icons.Default.Tune,
                ) {
                    controller.navController.navigate(GeneralScreenSpec.navHostRoute)
                },
                SettingsSection(
                    R.string.reader,
                    Icons.Default.ChromeReaderMode,
                ) {
                    controller.navController.navigate(ReaderSettingSpec.navHostRoute)
                },
                SettingsSection(
                    R.string.security,
                    Icons.Default.Security,
                ) {
                    controller.navController.navigate(SecuritySettingSpec.navHostRoute)
                },
                SettingsSection(
                    R.string.repository,
                    Icons.Default.Extension,
                ) {
                    controller.navController.navigate(RepositoryScreenSpec.navHostRoute)
                },
                SettingsSection(
                    R.string.advance_setting,
                    Icons.Default.Code
                ) {
                    controller.navController.navigate(AdvanceSettingSpec.navHostRoute)
                },
            )
        }
        IScaffold(
            topBar = { scrollBehavior ->
                Toolbar(
                    scrollBehavior = scrollBehavior,
                    title = {
                        BigSizeTextComposable(text = stringResource(R.string.settings))
                    },
                    navigationIcon = { TopAppBarBackButton(onClick = { controller.navController.popBackStack() }) },
                )
            }
        ) { padding ->
            SetupLayout(modifier = Modifier.padding(padding), items = settingItems)
        }

    }
}
