package org.ireader.presentation.ui

import androidx.compose.material.ScaffoldState
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import com.google.accompanist.pager.ExperimentalPagerApi
import org.ireader.appearance.AppearanceSettingScreen
import org.ireader.appearance.AppearanceViewModel

object AppearanceScreenSpec : ScreenSpec {

    override val navHostRoute: String = "appearance_setting_route"

    @OptIn(
        ExperimentalPagerApi::class, androidx.compose.animation.ExperimentalAnimationApi::class,
        androidx.compose.material.ExperimentalMaterialApi::class
    )
    @Composable
    override fun Content(
        navController: NavController,
        navBackStackEntry: NavBackStackEntry,
        scaffoldState: ScaffoldState,
    ) {
        val viewModel: AppearanceViewModel = hiltViewModel()
        AppearanceSettingScreen(
            saveDarkModePreference = { theme ->
                viewModel.saveNightModePreferences(theme)
            },
            onPopBackStack = {
                navController.popBackStack()
            }
        )
    }
}
