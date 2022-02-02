package ir.kazemcodes.infinity.core.ui

import androidx.compose.material.ScaffoldState
import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import com.google.accompanist.pager.ExperimentalPagerApi
import ir.kazemcodes.infinity.feature_settings.presentation.setting.dns.DnsOverHttpScreen

object DnsOverHttpSettingSpec : ScreenSpec {

    override val navHostRoute: String = "dnh_over_http_route"



    @OptIn(ExperimentalPagerApi::class, androidx.compose.animation.ExperimentalAnimationApi::class,
        androidx.compose.material.ExperimentalMaterialApi::class)
    @Composable
    override fun Content(
        navController: NavController,
        navBackStackEntry: NavBackStackEntry,
        scaffoldState: ScaffoldState
    ) {
        DnsOverHttpScreen(navController = navController)
    }

}