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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import kotlinx.coroutines.launch
import org.ireader.common_resources.ARG_HAVE_DRAWER
import org.ireader.common_resources.ARG_HAVE_MODAL_SHEET
import org.ireader.common_resources.ARG_HAVE_VARIANT_BOTTOM_BAR
import org.ireader.common_resources.ARG_HIDE_BOTTOM_BAR
import org.ireader.common_resources.ARG_SYSTEM_BAR_PADDING
import org.ireader.common_resources.ARG_TRANSPARENT_STATUS_BAR
import org.ireader.components.components.ISnackBarHost
import org.ireader.core_ui.theme.AppColors
import org.ireader.core_ui.theme.TransparentStatusBar
import org.ireader.presentation.ui.BottomNavScreenSpec
import org.ireader.presentation.ui.HistoryScreenSpec
import org.ireader.presentation.ui.LibraryScreenSpec
import org.ireader.presentation.ui.ScreenSpec
import org.ireader.presentation.ui.UpdateScreenSpec

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
    val shoeModalSheet = navBackStackEntry?.arguments?.getBoolean(ARG_HAVE_MODAL_SHEET) ?: false
    val transparentStatusBar =
        navBackStackEntry?.arguments?.getBoolean(ARG_TRANSPARENT_STATUS_BAR) ?: false
    val haveDrawer = navBackStackEntry?.arguments?.getBoolean(ARG_HAVE_DRAWER) ?: false
    val haveVariantBottomAppBar =
        navBackStackEntry?.arguments?.getBoolean(ARG_HAVE_VARIANT_BOTTOM_BAR) ?: false
    val systemBarPadding =
        navBackStackEntry?.arguments?.getBoolean(ARG_SYSTEM_BAR_PADDING) ?: false
    val snackBarHostState = remember { SnackbarHostState() }
    val modalBottomSheetState =
        rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    val vm: ScreenContentViewModel = hiltViewModel()
    val (requestedHideBottomNav, requestHideBottomNav) = remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    DisposableEffect(navBackStackEntry) {
        onDispose {
            requestHideBottomNav(false)
            scope.launch {
                modalBottomSheetState.hide()
                drawerState.close()

            }
        }
    }

    IModalSheets(
        bottomSheetState = modalBottomSheetState,
        isEnable = shoeModalSheet,
        sheetContent = {
            if (navStackEntry != null) {
                screenSpec?.BottomModalSheet(
                    ScreenSpec.Controller(
                        navController,
                        navStackEntry,
                        snackBarHostState,
                        modalBottomSheetState,
                        drawerState
                    )
                )
            }
        }
    ) {
        IModalDrawer(
            state = drawerState,
            sheetContent = {
                if (navStackEntry != null) {
                    screenSpec?.ModalDrawer(
                        ScreenSpec.Controller(
                            navController = navController,
                            snackBarHostState = snackBarHostState,
                            sheetState = modalBottomSheetState,
                            drawerState = drawerState,
                            requestHideNavigator = requestHideBottomNav,
                            navBackStackEntry = navStackEntry
                        )
                    )
                }
            },
            isEnable = haveDrawer,
        ) {
            TransparentStatusBar(enable = transparentStatusBar) {
                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .navigationBarsPadding(),
                    topBar = {
                        if (navStackEntry != null) {
                            screenSpec?.TopBar(
                                ScreenSpec.Controller(
                                    navController = navController,
                                    snackBarHostState = snackBarHostState,
                                    sheetState = modalBottomSheetState,
                                    drawerState = drawerState,
                                    requestHideNavigator = requestHideBottomNav,
                                    navBackStackEntry = navStackEntry
                                )
                            )
                        }
                    },
                    bottomBar = {
                        if (navStackEntry != null) {
                            IBottomAppBar(
                                isEnable = haveVariantBottomAppBar,
                                sheetContent = {
                                    screenSpec?.BottomAppBar(
                                        ScreenSpec.Controller(
                                            navController = navController,
                                            snackBarHostState = snackBarHostState,
                                            sheetState = modalBottomSheetState,
                                            drawerState = drawerState,
                                            requestHideNavigator = requestHideBottomNav,
                                            navBackStackEntry = navStackEntry
                                        )
                                    )
                                }
                            )
                        }

                        AnimatedVisibility(
                            visible = hideBottomBar == null || hideBottomBar == true || requestedHideBottomNav,
                            enter = slideInVertically(initialOffsetY = { it }),
                            exit = slideOutVertically(targetOffsetY = { it })
                        ) {
                            NavigationBar(
                                modifier = Modifier.fillMaxWidth(),
                                containerColor = AppColors.current.bars,
                                contentColor = AppColors.current.onBars,
                                tonalElevation = 5.dp,
                            ) {
                                BottomNavScreenSpec.screens.filter {
                                    (if (vm.showHistory.value) true else it != HistoryScreenSpec) && (if (vm.showUpdate.value) true else it != UpdateScreenSpec)
                                }.forEach { bottomNavDestination ->
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
                    AnimatedNavHost(
                        modifier = Modifier.fillMaxSize(),
                        navController = navController,
                        startDestination = LibraryScreenSpec.navHostRoute,
                        enterTransition = {
                            fadeIn(animationSpec = tween(500))
                        },
                        exitTransition = {
                            fadeOut(animationSpec = tween(500))
                        },
                    ) {

                        ScreenSpec.allScreens.values.forEach { screen ->
                            composable(
                                route = screen.navHostRoute,
                                arguments = screen.arguments,
                                deepLinks = screen.deepLinks,
                            ) { navBackStackEntry ->

                                screen.Content(
                                    ScreenSpec.Controller(
                                        navController = navController,
                                        navBackStackEntry = navBackStackEntry,
                                        snackBarHostState = snackBarHostState,
                                        sheetState = modalBottomSheetState,
                                        drawerState = drawerState,
                                        requestHideNavigator = requestHideBottomNav,
                                        scaffoldPadding = padding
                                    )
                                )
                            }
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
    isEnable: Boolean = false,
    bottomSheetState: ModalBottomSheetState,
    sheetContent: @Composable () -> Unit,
    content: @Composable () -> Unit
) {
    if (isEnable) {
        ModalBottomSheetLayout(
            modifier = if (bottomSheetState.currentValue == ModalBottomSheetValue.Expanded) Modifier.statusBarsPadding() else Modifier,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IModalDrawer(
    modifier: Modifier = Modifier,
    isEnable: Boolean = false,
    state: DrawerState,
    sheetContent: @Composable () -> Unit,
    content: @Composable () -> Unit
) {
    if (isEnable) {
        ModalNavigationDrawer(
            modifier = Modifier,
            drawerState = state,
            drawerContent = {
                sheetContent()
            },
            drawerContainerColor = MaterialTheme.colorScheme.surface,
            drawerContentColor = MaterialTheme.colorScheme.onSurface,
            content = content
        )
    } else {
        content()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IBottomAppBar(
    modifier: Modifier = Modifier,
    isEnable: Boolean = false,
    sheetContent: @Composable () -> Unit,
) {
    if (isEnable) {
        BottomAppBar(
            modifier = modifier.fillMaxWidth(),
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
            content = {
                sheetContent()
            }
        )
    }
}






