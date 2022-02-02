package ir.kazemcodes.infinity.feature_activity.presentation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import ir.kazemcodes.infinity.core.presentation.components.ISnackBarHost
import ir.kazemcodes.infinity.core.ui.BottomNavScreenSpec
import ir.kazemcodes.infinity.core.ui.ScreenSpec
import ir.kazemcodes.infinity.core.utils.Constants.ARG_HIDE_BOTTOM_BAR

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ScreenContent() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val scaffoldState = rememberScaffoldState()
    val modalBottomSheetState = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden)
    val screenSpec = ScreenSpec.allScreens[currentDestination?.route]

        Scaffold(
            topBar = {
                if (navBackStackEntry != null) {
                    screenSpec?.TopBar(navController, navBackStackEntry!!, scaffoldState = scaffoldState)
                }
            },
            bottomBar = {
                val hideBottomBar = navBackStackEntry?.arguments?.getBoolean(ARG_HIDE_BOTTOM_BAR)
                if (hideBottomBar == null || hideBottomBar == true) {
                    BottomNavigation(
                        backgroundColor = MaterialTheme.colors.background,
                        elevation = 5.dp
                    ) {
                        BottomNavScreenSpec.screens.forEach { bottomNavDestination ->
                            BottomNavigationItem(
                                icon = {
                                    Icon(bottomNavDestination.icon, contentDescription = null)
                                },
                                label = {
                                    Text(stringResource(bottomNavDestination.label))
                                },
                                selectedContentColor = MaterialTheme.colors.primary,
                                unselectedContentColor = MaterialTheme.colors.onSurface.copy(0.4f),
                                alwaysShowLabel = true,
                                selected = currentDestination?.hierarchy?.any { it.route == bottomNavDestination.navHostRoute } == true,
                                onClick = {
                                    navController.navigate(bottomNavDestination.navHostRoute) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                            )
                        }
                    }
                }
            },
            snackbarHost = { ISnackBarHost(snackBarHostState = it)},
            scaffoldState = scaffoldState,
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = BottomNavScreenSpec.screens[0].navHostRoute,
                modifier = Modifier.padding(innerPadding),
            ) {
                ScreenSpec.allScreens.values.forEach { screen ->
                    composable(
                        route = screen.navHostRoute,
                        arguments = screen.arguments,
                        deepLinks = screen.deepLinks,
                    ) { navBackStackEntry ->
                        screen.Content(
                            navController = navController,
                            navBackStackEntry = navBackStackEntry,
                            scaffoldState = scaffoldState,
                        )
                    }
                }
            }
        }


}