package ireader.presentation.core

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material3.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import ireader.i18n.*
import ireader.presentation.core.ui.*
import ireader.presentation.ui.component.Controller
import ireader.presentation.ui.component.components.ConfirmExitBackHandler
import ireader.presentation.ui.component.components.ISnackBarHost
import ireader.presentation.ui.core.theme.AppColors
import ireader.presentation.ui.core.theme.CustomSystemColor
import ireader.presentation.ui.core.theme.LocalHideNavigator
import ireader.presentation.ui.core.theme.TransparentStatusBar
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonNull.content


//@OptIn(
//    ExperimentalMaterialApi::class, ExperimentalAnimationApi::class,
//    ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class
//)
//@Composable
//fun ScreenContent() {
//    val navController = rememberAnimatedNavController()
//    val navBackStackEntry by navController.currentBackStackEntryAsState()
//    val currentDestination = navBackStackEntry?.destination
//    val hideBottomBar = navBackStackEntry?.arguments?.getBoolean(ARG_HIDE_BOTTOM_BAR)
//    val vm: ScreenContentViewModel = getViewModel<ScreenContentViewModel>()
//    val hideNavigator = LocalHideNavigator.current
//
//    ConfirmExitBackHandler(vm.confirmExit.value)
//    Scaffold(
//        modifier = Modifier
//            .fillMaxSize(),
//        bottomBar = {
//            AnimatedVisibility(
//                visible = (hideBottomBar == null || hideBottomBar == true && !hideNavigator.value),
//                enter = slideInVertically(initialOffsetY = { it }),
//                exit = slideOutVertically(targetOffsetY = { it })
//            ) {
//                NavigationBar(
//                    modifier = Modifier,
//                    containerColor = AppColors.current.bars,
//                    contentColor = AppColors.current.onBars,
//                    tonalElevation = 0.dp,
//                ) {
//                    BottomNavScreenSpec.screens.filter {
//                        (if (vm.showHistory.value) true else it != HistoryScreenSpec) && (if (vm.showUpdate.value) true else it != UpdateScreenSpec)
//                    }.forEach { bottomNavDestination ->
//                        val isSelected: Boolean by derivedStateOf {
//                            currentDestination?.hierarchy?.any {
//                                it.route == bottomNavDestination.navHostRoute
//                            } == true
//                        }
//                        NavigationBarItem(
//                            icon = {
//                                Icon(
//                                    bottomNavDestination.icon,
//                                    contentDescription = null,
//                                    tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onBackground
//                                )
//                            },
//                            label = {
//                                Text(
//                                    stringResource(bottomNavDestination.label),
//                                    maxLines = 1,
//                                    overflow = TextOverflow.Ellipsis,
//                                    color = MaterialTheme.colorScheme.onBackground
//                                )
//                            },
//                            colors = NavigationBarItemDefaults.colors(
//                                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
//                                unselectedTextColor = MaterialTheme.colorScheme.onBackground,
//                                selectedTextColor = MaterialTheme.colorScheme.onBackground,
//                                selectedIconColor = MaterialTheme.colorScheme.onBackground,
//                                unselectedIconColor = MaterialTheme.colorScheme.onBackground
//                            ),
//                            alwaysShowLabel = true,
//                            selected = isSelected,
//                            onClick = {
//                                navController.navigate(bottomNavDestination.navHostRoute) {
//                                    popUpTo(navController.graph.findStartDestination().id) {
//                                        saveState = true
//                                    }
//                                    launchSingleTop = true
//                                    restoreState = true
//                                }
//                            },
//                        )
//                    }
//                }
//            }
//        },
//    ) { padding ->
//        AnimatedNavHost(
//            modifier = Modifier.fillMaxSize(),
//            navController = navController,
//            startDestination = LibraryScreenSpec.navHostRoute,
//            enterTransition = {
//                slideIn(animationSpec = tween(500), initialOffset = { IntOffset(0, 0) })
//            },
//            exitTransition = {
//                slideOut(animationSpec = tween(500), targetOffset = { IntOffset(0, 0) })
//            },
//        ) {
//            ScreenSpec.allScreens.values.forEach { screen ->
//                composable(
//                    route = screen.navHostRoute,
//                    arguments = screen.arguments,
//                    deepLinks = screen.deepLinks,
//                ) { navBackStackEntry ->
//                    screen.Content(
//                        Controller(
//                            navController = navController,
//                            navBackStackEntry = navBackStackEntry,
//                        )
//                    )
//                }
//            }
//        }
//    }
//}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun IModalSheets(
    modifier: Modifier = Modifier,
    bottomSheetState: ModalBottomSheetState,
    sheetContent: @Composable () -> Unit,
    content: @Composable () -> Unit
) {

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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IModalDrawer(
    modifier: Modifier = Modifier,
    state: DrawerState,
    sheetContent: @Composable () -> Unit,
    content: @Composable () -> Unit
) {


    ModalNavigationDrawer(
        modifier = Modifier,
        drawerState = state,
        drawerContent = {
            DismissibleDrawerSheet(
                drawerContentColor = MaterialTheme.colorScheme.onSurface,
                drawerContainerColor = MaterialTheme.colorScheme.surface,

                ) {
                sheetContent()
            }
        },
        scrimColor = Color.Transparent,
        content = content,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IBottomAppBar(
    modifier: Modifier = Modifier,
    sheetContent: @Composable () -> Unit,
) {
    BottomAppBar(
        modifier = modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        content = {
            sheetContent()
        }
    )
}
