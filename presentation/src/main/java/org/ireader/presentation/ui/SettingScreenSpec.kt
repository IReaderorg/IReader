package org.ireader.presentation.ui

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChromeReaderMode
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import org.ireader.components.components.Toolbar
import org.ireader.components.reusable_composable.BigSizeTextComposable
import org.ireader.components.reusable_composable.TopAppBarBackButton
import org.ireader.settings.setting.SettingsSection
import org.ireader.settings.setting.SetupLayout
import org.ireader.ui_settings.R

object SettingScreenSpec : ScreenSpec {
    override val navHostRoute: String = "settings"

    @ExperimentalMaterial3Api
    @OptIn(ExperimentalMaterialApi::class)
    @Composable
    override fun TopBar(
                controller: ScreenSpec.Controller
    ) {
        Toolbar(
            title = {
                BigSizeTextComposable(text = stringResource(org.ireader.ui_settings.R.string.settings))
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
        controller: ScreenSpec.Controller
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
                    R.string.reader,
                    Icons.Default.ChromeReaderMode
                    ,
                ) {
                    controller.navController.navigate(ReaderSettingSpec.navHostRoute)
                },
                SettingsSection(
                    R.string.advance_setting,
                    Icons.Default.Code
                ) {
                    controller.navController.navigate(AdvanceSettingSpec.navHostRoute)
                },
            )
        }
        SetupLayout(modifier = Modifier.padding(controller.scaffoldPadding),items = settingItems)

    }
}