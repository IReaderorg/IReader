package org.ireader.presentation.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import com.google.accompanist.pager.ExperimentalPagerApi
import org.ireader.components.components.TitleToolbar
import org.ireader.settings.setting.SettingViewModel
import org.ireader.settings.setting.advance_setting.AdvanceSettings
import org.ireader.ui_settings.R

object AdvanceSettingSpec : ScreenSpec {

    override val navHostRoute: String = "advance_setting_route"
    @OptIn(ExperimentalMaterialApi::class)
    @Composable
    override fun TopBar(
        navController: NavController,
        navBackStackEntry: NavBackStackEntry,
        snackBarHostState: SnackbarHostState,
        sheetState: ModalBottomSheetState
    ) {
        TitleToolbar(
            title = stringResource(R.string.advance_setting),
            navController = navController
        )
    }

    @OptIn(
        ExperimentalPagerApi::class, androidx.compose.animation.ExperimentalAnimationApi::class,
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
        val vm: SettingViewModel = hiltViewModel()
        AdvanceSettings(
            vm = vm,
            onBackStack = {
                navController.popBackStack()
            },
            snackBarHostState = snackBarHostState
        )
    }
}
