
package org.ireader.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import org.ireader.common_resources.ARG_HIDE_BOTTOM_BAR
import org.ireader.presentation.ui.BottomNavScreenSpec
import org.ireader.presentation.ui.LibraryScreenSpec
import org.ireader.presentation.ui.ScreenSpec

@OptIn(ExperimentalMaterialApi::class, ExperimentalAnimationApi::class,
    ExperimentalMaterial3Api::class
)
@Composable
fun ScreenContent() {
    val navController = rememberAnimatedNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val screenSpec = ScreenSpec.allScreens[currentDestination?.route]
    val hideBottomBar = navBackStackEntry?.arguments?.getBoolean(ARG_HIDE_BOTTOM_BAR)
    androidx.compose.material3.Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding(),
        bottomBar = {
            AnimatedVisibility(
                visible = hideBottomBar == null || hideBottomBar == true,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it })
            ) {
                BottomNavigation(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = MaterialTheme.colorScheme.surface,
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
                            selectedContentColor = MaterialTheme.colorScheme.primary,
                            unselectedContentColor = MaterialTheme.colorScheme.onSurface.copy(0.4f),
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
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = if (hideBottomBar == null || hideBottomBar == true) 45.dp else 0.dp)
        ) {
            AnimatedNavHost(
                navController = navController,
                startDestination = LibraryScreenSpec.navHostRoute,
                modifier = Modifier
                    .fillMaxSize(),
                enterTransition = { fadeIn(animationSpec = tween(500)) },
                exitTransition = { fadeOut(animationSpec = tween(500)) },
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
                        )
                    }
                }
            }
        }
    }
}
