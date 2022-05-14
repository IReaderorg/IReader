package org.ireader.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import org.ireader.common_resources.ARG_HAVE_MODAL_SHEET
import org.ireader.common_resources.ARG_HIDE_BOTTOM_BAR
import org.ireader.components.components.ISnackBarHost
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
    val navStackEntry = navBackStackEntry
    val currentDestination = navBackStackEntry?.destination
    val screenSpec = ScreenSpec.allScreens[currentDestination?.route]
    val hideBottomBar = navBackStackEntry?.arguments?.getBoolean(ARG_HIDE_BOTTOM_BAR)
    val shoeModalSheet = navBackStackEntry?.arguments?.getBoolean(ARG_HAVE_MODAL_SHEET)?:false
    val snackBarHostState = remember { SnackbarHostState() }
    val modalBottomSheetState =
        rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)
    IModalSheets(
        bottomSheetState = modalBottomSheetState,
        isEnable = shoeModalSheet,
        sheetContent = {
            if (navStackEntry != null) {
                screenSpec?.BottomModalSheet(navController, navStackEntry, snackBarHostState,modalBottomSheetState)
            }
        }
    ) {
        androidx.compose.material3.Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding(),
            topBar = {

                if (navStackEntry != null) {
                    screenSpec?.TopBar(navController, navStackEntry, snackBarHostState,modalBottomSheetState)
                }
            },
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
                                        tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onBackground
                                    )
                                },
                                label = {
                                    Text(
                                        stringResource(bottomNavDestination.label),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                    unselectedTextColor = MaterialTheme.colorScheme.onBackground,
                                    selectedTextColor = MaterialTheme.colorScheme.onBackground,
                                    selectedIconColor = MaterialTheme.colorScheme.onBackground,
                                    unselectedIconColor = MaterialTheme.colorScheme.onBackground
                                ),
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
            },
            snackbarHost = { ISnackBarHost(snackBarHostState = snackBarHostState) },
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
                                snackBarHostState,
                                padding,
                                sheetState = modalBottomSheetState
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun IModalSheets(
    modifier: Modifier = Modifier,
    isEnable:Boolean = false,
    bottomSheetState: ModalBottomSheetState,
    sheetContent:@Composable () -> Unit,
    content: @Composable () -> Unit
) {
    if (isEnable) {
        ModalBottomSheetLayout(
            modifier = if (bottomSheetState.targetValue == ModalBottomSheetValue.Expanded) Modifier.statusBarsPadding() else Modifier,
            sheetContent = {
                Box(modifier.defaultMinSize(minHeight = 1.dp)) {
                    sheetContent()
                }
            },
            sheetState = bottomSheetState,
            sheetBackgroundColor = MaterialTheme.colorScheme.background,
            sheetContentColor = MaterialTheme.colorScheme.onBackground,
            content = content
        )
    } else {
        content()
    }

}

