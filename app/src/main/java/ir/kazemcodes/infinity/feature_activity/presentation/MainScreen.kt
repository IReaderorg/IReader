package ir.kazemcodes.infinity.feature_activity.presentation


import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.zhuinden.simplestackcomposeintegration.services.rememberService
import ir.kazemcodes.infinity.feature_activity.domain.models.BottomNavigationScreen
import ir.kazemcodes.infinity.feature_library.presentation.LibraryScreen
import ir.kazemcodes.infinity.feature_settings.presentation.setting.SettingScreen
import ir.kazemcodes.infinity.feature_sources.presentation.extension.ExtensionScreen





@ExperimentalMaterialApi
@OptIn(ExperimentalPagerApi::class)
@ExperimentalAnimationApi
@Composable
fun MainScreen(modifier: Modifier = Modifier) {
    val viewModel = rememberService<MainViewModel>()
    val currentScreen: BottomNavigationScreen = viewModel.state.value.currentScreen
    val bottomNavigationItems = listOf(
        BottomNavigationScreen.Library,
        BottomNavigationScreen.ExtensionScreen,
        BottomNavigationScreen.Setting
    )

    Scaffold(
        bottomBar = {
            BottomNavigation(
                modifier = modifier,
                backgroundColor = MaterialTheme.colors.background,
                elevation = 8.dp,
            ) {
                bottomNavigationItems.forEach { screen ->
                    BottomNavigationItem(
                        selected = screen.title == bottomNavigationItems[currentScreen.index].title,
                        onClick = {
                            viewModel.onEvent(MainScreenEvent.NavigateTo(screen))
                        },
                        label = { Text(text = screen.title) },
                        icon = {
                            Icon(
                                imageVector = screen.icon,
                                contentDescription = "${screen.title} icon"
                            )
                        },
                        selectedContentColor = MaterialTheme.colors.primary,
                        unselectedContentColor = MaterialTheme.colors.onSurface.copy(0.4f),
                    )

                }
            }
        }
    ) {

        Crossfade(targetState = currentScreen) { screen ->
            when (screen) {
                is BottomNavigationScreen.Library -> LibraryScreen()
                is BottomNavigationScreen.ExtensionScreen -> ExtensionScreen()
                is BottomNavigationScreen.Setting -> SettingScreen()
            }
        }

    }
}
