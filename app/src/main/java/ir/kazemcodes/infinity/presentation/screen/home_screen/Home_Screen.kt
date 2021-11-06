package ir.kazemcodes.infinity.presentation.screen.home_screen


import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import ir.kazemcodes.infinity.domain.model.model_component.BottomNavItem

@Composable
fun HomeScreen(navController: NavHostController) {

    val bottomNavItems = listOf<BottomNavItem>(
        BottomNavItem(route = BottomBarScreen.Library.route, Icons.Default.Book, "Library"),
        BottomNavItem(route = BottomBarScreen.Browse.route, Icons.Default.Explore, "Explore"),
        BottomNavItem(
            route = BottomBarScreen.Setting.route,
            Icons.Default.Settings,
            "Setting"
        ),
    )
    var screenTitle by remember {
        mutableStateOf(bottomNavItems[0].title)
    }


    Scaffold(
        topBar = { TopBar(screenTitle) },
        bottomBar = {
            BottomNavigationBar(
                navController = navController,
                items = bottomNavItems,
                modifier = Modifier,
                onItemClick = {

                    navController.navigate(it.route) {
                        screenTitle = it.title
                        launchSingleTop = true
                        restoreState = true
                        // Pop up backstack to the first destination and save state. This makes going back
                        // to the start destination when pressing back in any other bottom tab.
                        popUpTo(bottomNavItems[0].route) {
                            saveState = true
                        }
                    }

                })
        },

        ) {
        BottomNavGraph(navController)
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


sealed class BottomBarScreen(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    object Library :
        BottomBarScreen(
            "library_destination_route",
            "Library",
            icon = Icons.Default.Book
        )

    object Browse : BottomBarScreen(
        "browse_route",
        "Explore",
        Icons.Default.Explore
    )
    object Setting : BottomBarScreen(
        "setting_route",
        "Setting",
        Icons.Default.Settings
    )
}
