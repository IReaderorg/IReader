package org.ireader.presentation.ui

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Box
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
import org.ireader.common_resources.R
import org.ireader.components.components.TitleToolbar
import org.ireader.settings.setting.category_screen.CategoryScreen
import org.ireader.settings.setting.category_screen.CategoryScreenViewModel

object CategoryScreenSpec : ScreenSpec {

    override val navHostRoute: String = "edit_category_screen_route"
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
            title = stringResource(R.string.edit_category),
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
        val vm : CategoryScreenViewModel = hiltViewModel(navBackStackEntry)
        Box(modifier = Modifier.padding(scaffoldPadding)) {
            CategoryScreen(
                vm = vm
            )
        }
    }

}

