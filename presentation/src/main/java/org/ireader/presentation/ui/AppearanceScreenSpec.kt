package org.ireader.presentation.ui

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import org.ireader.appearance.AppearanceSettingScreen
import org.ireader.appearance.AppearanceViewModel
import org.ireader.components.components.Toolbar
import org.ireader.components.reusable_composable.BigSizeTextComposable
import org.ireader.components.reusable_composable.TopAppBarBackButton
import org.ireader.ui_appearance.R

object AppearanceScreenSpec : ScreenSpec {

    override val navHostRoute: String = "appearance_setting_route"

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
        Toolbar(
            title = {
                BigSizeTextComposable(text = stringResource(R.string.appearance))
            },
            navigationIcon = {
                TopAppBarBackButton() {
                    popBackStack(navController)
                }
            }
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
        val viewModel: AppearanceViewModel = hiltViewModel(navBackStackEntry)
        AppearanceSettingScreen(
            modifier = Modifier.padding(scaffoldPadding),
            saveDarkModePreference = { theme ->
                viewModel.saveNightModePreferences(theme)
            },
            onPopBackStack = {
                navController.popBackStack()
            },
            vm = viewModel
        )
    }
}

fun popBackStack(navController: NavController) {
    navController.popBackStack()
}