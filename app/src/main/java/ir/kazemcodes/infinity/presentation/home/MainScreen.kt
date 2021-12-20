package ir.kazemcodes.infinity.presentation.home


import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ir.kazemcodes.infinity.extension_feature.presentation.extension_screen.ExtensionScreen
import ir.kazemcodes.infinity.presentation.library.LibraryScreen
import ir.kazemcodes.infinity.setting_feature.presentation.SettingScreen


sealed class BottomNavigationScreens(
    val index : Int,
    val title: String,
    val icon: ImageVector
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

@ExperimentalMaterialApi
@Composable
fun MainScreen(modifier : Modifier = Modifier) {
    var currentIndex by remember { mutableStateOf(0) }
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
                            currentIndex = screen.index
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


    @Composable
    fun TopBar(title: String) {
        TopAppBar(
            title = {
                Text(
                    text = title,
                    fontSize = 18.sp,
                )
            },
            backgroundColor = MaterialTheme.colors.background,
            contentColor = MaterialTheme.colors.onBackground,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
