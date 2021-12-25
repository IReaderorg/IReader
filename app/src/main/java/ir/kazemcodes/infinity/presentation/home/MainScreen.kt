package ir.kazemcodes.infinity.presentation.home


import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.zhuinden.simplestackcomposeintegration.services.rememberService
import ir.kazemcodes.infinity.presentation.extension.ExtensionScreen
import ir.kazemcodes.infinity.presentation.library.LibraryScreen
import ir.kazemcodes.infinity.setting_feature.presentation.SettingScreen


sealed class BottomNavigationScreens(
    val index: Int,
    val title: String,
    val icon: ImageVector,
) {
    object Library :
        BottomNavigationScreens(
            0,
            "Library",
            icon = Icons.Default.Book
        )

    object ExtensionScreen : BottomNavigationScreens(
        1,
        "Explore",
        Icons.Default.Explore
    )

    object Setting : BottomNavigationScreens(
        2,
        "Setting",
        Icons.Default.Settings
    )
}

@ExperimentalFoundationApi
@ExperimentalMaterialApi
@Composable
fun MainScreen(modifier: Modifier = Modifier) {
    val viewModel = rememberService<MainViewModel>()
    val currentIndex = viewModel.state.value.index
    val bottomNavigationItems = listOf(
        BottomNavigationScreens.Library,
        BottomNavigationScreens.ExtensionScreen,
        BottomNavigationScreens.Setting
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
                        selected = screen.title == bottomNavigationItems[currentIndex].title,
                        onClick = {
                            viewModel.onEvent(MainScreenEvent.ChangeScreenIndex(screen.index))
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
        if (currentIndex == 0) {
            LibraryScreen()
        } else if (currentIndex == 1) {
            ExtensionScreen()
        } else if (currentIndex == 2) {
            SettingScreen()
        }
    }
}