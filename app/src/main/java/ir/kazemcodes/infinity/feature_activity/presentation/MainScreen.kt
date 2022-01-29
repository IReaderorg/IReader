package ir.kazemcodes.infinity.feature_activity.presentation


import androidx.compose.animation.Crossfade
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.google.accompanist.pager.ExperimentalPagerApi
import ir.kazemcodes.infinity.feature_activity.domain.models.BottomNavigationScreen
import ir.kazemcodes.infinity.feature_library.presentation.LibraryScreen
import ir.kazemcodes.infinity.feature_settings.presentation.setting.SettingScreen
import ir.kazemcodes.infinity.feature_sources.presentation.extension.ExtensionScreen


//@ExperimentalMaterialApi
//@OptIn(ExperimentalPagerApi::class)
//@ExperimentalAnimationApi
//@Composable
//fun MainScreen(
//    modifier: Modifier = Modifier,
//    viewModel: MainViewModel = hiltViewModel(),
//    navController: NavHostController,
//) {
//    Scaffold(
//    ) {
//        SetupNavHost(navController = navController)
//    }
//}

@OptIn(ExperimentalPagerApi::class, androidx.compose.material.ExperimentalMaterialApi::class,
    androidx.compose.animation.ExperimentalAnimationApi::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: MainViewModel = hiltViewModel(),
    navController: NavHostController,
) {
    Scaffold(
        bottomBar = {
            val bottomNavigationItems = listOf(
                BottomNavigationScreen.Library,
                BottomNavigationScreen.ExtensionScreen,
                BottomNavigationScreen.Setting
            )
            BottomNavigationComposable(viewModel = viewModel,
                navController = navController,
                items = bottomNavigationItems)
        }
    ) {
        Crossfade(targetState = viewModel.state.value.currentScreen) { screen ->
            when (screen) {
                is BottomNavigationScreen.Library -> LibraryScreen(navController = navController)
                is BottomNavigationScreen.ExtensionScreen -> ExtensionScreen(navController = navController)
                is BottomNavigationScreen.Setting -> SettingScreen(navController = navController)
            }
        }
    }
}






