package org.ireader.presentation.ui

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import org.ireader.components.components.TitleToolbar
import org.ireader.settings.setting.SettingViewModel
import org.ireader.settings.setting.advance_setting.AdvanceSettings
import org.ireader.ui_settings.R

object AdvanceSettingSpec : ScreenSpec {

    override val navHostRoute: String = "advance_setting_route"
    @ExperimentalMaterial3Api
    @OptIn(ExperimentalMaterialApi::class)
    @Composable
    override fun TopBar(
        navController: NavController,
        navBackStackEntry: NavBackStackEntry,
        snackBarHostState: SnackbarHostState,
        sheetState: ModalBottomSheetState,
        drawerState: DrawerState
    ) {
        TitleToolbar(
            title = stringResource(R.string.advance_setting),
            navController = navController
        )
    }

    @OptIn(
        ExperimentalAnimationApi::class,
        ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class
    )
    @Composable
    override fun Content(
        navController: NavController,
        navBackStackEntry: NavBackStackEntry,
        snackBarHostState: SnackbarHostState,
        scaffoldPadding: PaddingValues,
        sheetState: ModalBottomSheetState,
        drawerState: DrawerState
    ) {
        val vm: SettingViewModel = hiltViewModel(navBackStackEntry)
        AdvanceSettings(
            vm = vm,
            onBackStack = {
                navController.popBackStack()
            },
            snackBarHostState = snackBarHostState
        )
    }
}
