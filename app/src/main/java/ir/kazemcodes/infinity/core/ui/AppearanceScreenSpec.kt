package ir.kazemcodes.infinity.core.ui

import androidx.compose.material.ScaffoldState
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import com.google.accompanist.pager.ExperimentalPagerApi
import ir.kazemcodes.infinity.feature_activity.presentation.MainViewModel
import ir.kazemcodes.infinity.feature_settings.presentation.appearance.AppearanceSettingScreen


object AppearanceScreenSpec : ScreenSpec {

    override val navHostRoute: String = "appearance_setting_route"



    @OptIn(ExperimentalPagerApi::class, androidx.compose.animation.ExperimentalAnimationApi::class,
        androidx.compose.material.ExperimentalMaterialApi::class)
    @Composable
    override fun Content(
        navController: NavController,
        navBackStackEntry: NavBackStackEntry,
        scaffoldState: ScaffoldState
    ) {
        val viewModel: MainViewModel = hiltViewModel()
        AppearanceSettingScreen(viewModel = viewModel, navController = navController)
    }

}