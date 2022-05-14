package org.ireader.presentation.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import org.ireader.components.components.Toolbar
import org.ireader.components.reusable_composable.BigSizeTextComposable
import org.ireader.components.reusable_composable.TopAppBarBackButton
import org.ireader.settings.setting.SettingsSection
import org.ireader.settings.setting.SetupLayout
import org.ireader.ui_settings.R

object SettingScreenSpec : ScreenSpec {
    override val navHostRoute: String = "settings"

    @OptIn(ExperimentalMaterialApi::class)
    @Composable
    override fun TopBar(
        navController: NavController,
        navBackStackEntry: NavBackStackEntry,
        snackBarHostState: SnackbarHostState,
        sheetState: ModalBottomSheetState
    ) {
        Toolbar(
            title = {
                BigSizeTextComposable(text = stringResource(org.ireader.ui_settings.R.string.settings))
            },
            navigationIcon = { TopAppBarBackButton(onClick = { navController.popBackStack() }) },
        )
    }

    @OptIn(
        androidx.compose.animation.ExperimentalAnimationApi::class,
        androidx.compose.material.ExperimentalMaterialApi::class
    )
    @Composable
    override fun Content(
        navController: NavController,
        navBackStackEntry: NavBackStackEntry,
        snackBarHostState: SnackbarHostState,
        scaffoldPadding:PaddingValues,
        sheetState: ModalBottomSheetState
    ) {
        val settingItems = remember {
            listOf(
                SettingsSection(
                    R.string.appearance,
                    Icons.Default.Palette,
                ) {
                    navController.navigate(AppearanceScreenSpec.navHostRoute)
                },
                SettingsSection(
                    R.string.advance_setting,
                    Icons.Default.Code
                ) {
                    navController.navigate(AdvanceSettingSpec.navHostRoute)
                },
            )
        }
        SetupLayout(modifier = Modifier.padding(scaffoldPadding),items = settingItems)

    }
}