package org.ireader.presentation.ui

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
import org.ireader.components.components.SearchToolbar
import org.ireader.settings.setting.font_screens.FontScreen
import org.ireader.settings.setting.font_screens.FontScreenViewModel
import org.ireader.ui_settings.R

@ExperimentalMaterial3Api
@OptIn(ExperimentalMaterialApi::class)
object FontScreenSpec : ScreenSpec {
    override val navHostRoute: String = "font_screen_spec"

    @Composable
    override fun TopBar(
        navController: NavController,
        navBackStackEntry: NavBackStackEntry,
        snackBarHostState: SnackbarHostState,
        sheetState: ModalBottomSheetState,
        drawerState: DrawerState
    ) {
        val vm: FontScreenViewModel = hiltViewModel(navBackStackEntry)
        SearchToolbar(
            title = stringResource(R.string.font),
            onPopBackStack = {
                navController.popBackStack()
            },
            onValueChange = {
                vm.searchQuery = it
            },
            onSearch = {
                vm.searchQuery = it
            }
        )
    }

    @Composable
    override fun Content(
        navController: NavController,
        navBackStackEntry: NavBackStackEntry,
        snackBarHostState: SnackbarHostState,
        scaffoldPadding: PaddingValues,
        sheetState: ModalBottomSheetState,
        drawerState: DrawerState
    ) {
        val vm: FontScreenViewModel = hiltViewModel(navBackStackEntry)

        FontScreen(
            vm
        )
    }
}


