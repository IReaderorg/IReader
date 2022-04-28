package org.ireader.presentation.ui

import androidx.compose.material.ScaffoldState
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import com.google.accompanist.pager.ExperimentalPagerApi
import org.ireader.settings.setting.SettingViewModel
import org.ireader.settings.setting.dns.AdvanceSettings

object AdvanceSettingSpec : ScreenSpec {

    override val navHostRoute: String = "advance_setting_route"


    @OptIn(ExperimentalPagerApi::class, androidx.compose.animation.ExperimentalAnimationApi::class,
        androidx.compose.material.ExperimentalMaterialApi::class)
    @Composable
    override fun Content(
        navController: NavController,
        navBackStackEntry: NavBackStackEntry,
        scaffoldState: ScaffoldState,
    ) {
        val vm : SettingViewModel = hiltViewModel()
        AdvanceSettings(
            navController = navController,
            vm = vm
        )
    }

}