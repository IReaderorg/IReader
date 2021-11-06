package ir.kazemcodes.infinity.presentation.screen.home_screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import ir.kazemcodes.infinity.domain.model.model_component.BottomNavItem
import ir.kazemcodes.infinity.presentation.screen.browse_screen.BrowseScreen
import ir.kazemcodes.infinity.presentation.screen.library_screen.LibraryScreen
import ir.kazemcodes.infinity.presentation.screen.setting_screen.SettingScreen

@Composable
fun BottomNavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = BottomBarScreen.Library.route,
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {

        composable(
            BottomBarScreen.Library.route
        ) {
            LibraryScreen()

        }
        composable(
            BottomBarScreen.Browse.route
        ) {
            BrowseScreen()

        }
        composable(
            BottomBarScreen.Setting.route
        ) {
            SettingScreen()

        }
    }
}

@Composable
fun BottomNavigationBar(
    items: List<BottomNavItem>,
    navController: NavController,
    modifier: Modifier = Modifier,
    onItemClick: (BottomNavItem) -> Unit,
) {
    val backStackEntry = navController.currentBackStackEntryAsState()
    BottomNavigation(
        modifier = modifier,
        backgroundColor = MaterialTheme.colors.background,
        elevation = 5.dp,
    ) {
        items.forEach { item ->

            val selected = item.route == backStackEntry.value?.destination?.route

            BottomNavigationItem(
                selected = selected,
                onClick = { onItemClick(item) },
                icon = {
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(imageVector = item.icon, contentDescription = "${item.title} icon")
                        Spacer(modifier = modifier.height(1.dp))
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.body2.copy(fontSize = 10.sp)
                        )

                    }
                },
                selectedContentColor = MaterialTheme.colors.primary,
                unselectedContentColor = MaterialTheme.colors.onSurface.copy(0.4f),
            )

        }
    }

}
