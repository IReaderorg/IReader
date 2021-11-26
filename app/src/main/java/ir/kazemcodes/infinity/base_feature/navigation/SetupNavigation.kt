package ir.kazemcodes.infinity.base_feature.navigation


import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState


sealed class BottomNavigationScreens(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    object Library :
        BottomNavigationScreens(
            "library_destination_route",
            "Library",
            icon = Icons.Default.Book
        )

    object Browse : BottomNavigationScreens(
        "browse_route",
        "Explore",
        Icons.Default.Explore
    )

    object Setting : BottomNavigationScreens(
        "setting_route",
        "Setting",
        Icons.Default.Settings
    )
}

@ExperimentalMaterialApi
@Composable
fun SetupNavigation(
    navController: NavHostController
) {

    val bottomNavigationItems = listOf(
        BottomNavigationScreens.Library,
        BottomNavigationScreens.Browse,
        BottomNavigationScreens.Setting
    )

    Scaffold(
        bottomBar = {
            val bottomBarRoutes = bottomNavigationItems.map { it.route }
            val shouldShowBottomBar: Boolean = navController
                .currentBackStackEntryAsState().value?.destination?.route in bottomBarRoutes
            if(shouldShowBottomBar) {

            InfReaderBottomNavigation(
                navController = navController,
                bottomNavigationItems = bottomNavigationItems
            )
            }
        }
    ) {
        MainNavGraph(navController = navController)
    }

}

@Composable
fun InfReaderBottomNavigation(
    navController: NavController,
    bottomNavigationItems: List<BottomNavigationScreens>,
) {
    BottomBar(
        navController = navController,
        items = bottomNavigationItems
    )
}

@Composable
fun BottomBar(
    items: List<BottomNavigationScreens>,
    navController: NavController,
    modifier: Modifier = Modifier,
) {
    val backStackEntry = navController.currentBackStackEntryAsState()
    BottomNavigation(
        modifier = modifier,
        backgroundColor = MaterialTheme.colors.background,
        elevation = 8.dp,
    ) {
        items.forEach { screen ->

            BottomNavigationItem(
                selected = screen.route == backStackEntry.value?.destination?.route,
                onClick = {
                    navController.navigate(screen.route) {
                        launchSingleTop = true
                        restoreState = true
                        // Pop up backstack to the first destination and save state. This makes going back
                        // to the start destination when pressing back in any other bottom tab.
                        popUpTo(items[0].route) {
                            saveState = true
                        }
                    }
                },
                label = { Text(text = screen.title)},
                icon = {
                        Icon(imageVector = screen.icon, contentDescription = "${screen.title} icon")
                },
                selectedContentColor = MaterialTheme.colors.primary,
                unselectedContentColor = MaterialTheme.colors.onSurface.copy(0.4f),
            )

        }
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
