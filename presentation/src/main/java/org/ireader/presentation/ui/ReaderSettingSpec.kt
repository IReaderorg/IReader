package org.ireader.presentation.ui

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import org.ireader.components.components.Components
import org.ireader.components.components.SetupUiComponent
import org.ireader.components.components.TitleToolbar
import org.ireader.ui_settings.R

object ReaderSettingSpec : ScreenSpec {

    override val navHostRoute: String = "reader_settings_screen_route"
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
            title = stringResource(R.string.reader),
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
        val context = LocalContext.current
        val items = remember {
            listOf<Components>(
                Components.Header(
                  context.getString(R.string.font)
                ),
                Components.Row(
                    title = context.getString(R.string.font),
                    onClick = {
                        navController.navigate(FontScreenSpec.navHostRoute)
                    },
                ),
            )
        }

        LazyColumn(
            modifier = Modifier.padding(scaffoldPadding).fillMaxSize()
        ) {
            SetupUiComponent(items)
        }

    }

}