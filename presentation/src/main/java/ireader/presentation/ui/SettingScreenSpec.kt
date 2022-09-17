package ireader.presentation.ui

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChromeReaderMode
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import ireader.ui.component.Controller
import ireader.ui.component.components.Toolbar
import ireader.ui.component.reusable_composable.BigSizeTextComposable
import ireader.ui.component.reusable_composable.TopAppBarBackButton
import ireader.ui.settings.SettingsSection
import ireader.ui.settings.SetupLayout
import ireader.presentation.R
import org.koin.androidx.compose.get
object SettingScreenSpec : ScreenSpec {
    override val navHostRoute: String = "settings"

    @ExperimentalMaterial3Api
    @OptIn(ExperimentalMaterialApi::class)
    @Composable
    override fun TopBar(
        controller: Controller
    ) {
        Toolbar(
            scrollBehavior = controller.scrollBehavior,
            title = {
                BigSizeTextComposable(text = stringResource(R.string.settings))
            },
            navigationIcon = { TopAppBarBackButton(onClick = { controller.navController.popBackStack() }) },
        )
    }

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
        SetupLayout(modifier = Modifier.padding(controller.scaffoldPadding), items = settingItems)
    }
}
