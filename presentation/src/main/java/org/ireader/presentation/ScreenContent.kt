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
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
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
import org.ireader.components.reusable_composable.SuperSmallTextComposable
import org.ireader.core_ui.theme.AppColors
import org.ireader.presentation.ui.BottomNavScreenSpec
import org.ireader.presentation.ui.LibraryScreenSpec
import org.ireader.presentation.ui.ScreenSpec

@OptIn(
    ExperimentalMaterialApi::class, ExperimentalAnimationApi::class,
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
                NavigationBar(
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = AppColors.current.bars,
                    contentColor = AppColors.current.onBars,
                    tonalElevation = 5.dp,
                ) {
                    BottomNavScreenSpec.screens.forEach { bottomNavDestination ->
                        val isSelected: Boolean by derivedStateOf {
                            currentDestination?.hierarchy?.any {
                                it.route == bottomNavDestination.navHostRoute
                            } == true
                        }
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    bottomNavDestination.icon,
                                    contentDescription = null,
                                    tint = AppColors.current.onBars
                                )
                            },
                            label = {
                                SuperSmallTextComposable(
                                    text = stringResource(bottomNavDestination.label),
                                    color = AppColors.current.onBars
                                )
                            },
//                            colors = NavigationBarItemDefaults.colors(
//                                indicatorColor = Color.Transparent,
//                            ),
                            alwaysShowLabel = true,
                            selected = isSelected,
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
                enterTransition = {
                    fadeIn(animationSpec = tween(300))
                },
                exitTransition = {
                    fadeOut(animationSpec = tween(300))
                },
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
