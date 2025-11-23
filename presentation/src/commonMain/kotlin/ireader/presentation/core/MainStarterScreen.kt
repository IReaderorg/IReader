package ireader.presentation.core

import ireader.presentation.core.LocalNavigator

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabNavigator
import ireader.presentation.core.ui.*
import ireader.presentation.ui.component.IBackHandler
import ireader.presentation.ui.component.IScaffold
import ireader.presentation.ui.component.isTableUi
import ireader.presentation.ui.component.navigation.ModernBottomNavigationBar
import ireader.presentation.ui.component.navigation.ModernNavigationItem
import ireader.presentation.ui.component.navigation.ModernNavigationRailItem
import ireader.presentation.ui.component.navigation.Material3NavigationRail
import ireader.presentation.ui.component.navigation.Material3NavigationRailItem
import ireader.presentation.ui.core.theme.AppColors
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.receiveAsFlow

@OptIn(ExperimentalMaterial3Api::class)
object MainStarterScreen {
    private const val TabFadeDuration = 200
    private val showBottomNavEvent = Channel<Boolean>()

    @OptIn(ExperimentalLayoutApi::class)
    @Composable
    operator fun invoke() {
        val navController = requireNotNull(LocalNavigator.current) { "LocalNavigator not provided" }
        val vm: ScreenContentViewModel = getIViewModel()
        TabNavigator(LibraryScreenSpec) { tabNavigator ->
            CompositionLocalProvider(LocalNavigator provides navController) {
                IScaffold(
                        startBar = {
                            if (isTableUi()) {
                                val bottomNavVisible by produceState(initialValue = true) {
                                    showBottomNavEvent.receiveAsFlow().collectLatest { value = it }
                                }
                                AnimatedVisibility(
                                    visible = bottomNavVisible,
                                    enter = expandVertically(),
                                    exit = shrinkVertically(),
                                ) {
                                    Material3NavigationRail(
                                        containerColor = MaterialTheme.colorScheme.surface,
                                        contentColor = MaterialTheme.colorScheme.onSurface
                                    ) {
                                        Material3NavigationRailTabItem(LibraryScreenSpec)
                                        if (vm.showUpdate.value) {
                                            Material3NavigationRailTabItem(UpdateScreenSpec)
                                        }
                                        if (vm.showUpdate.value) {
                                            Material3NavigationRailTabItem(HistoryScreenSpec)
                                        }
                                        Material3NavigationRailTabItem(ExtensionScreenSpec)
                                        Material3NavigationRailTabItem(MoreScreenSpec)
                                    }
                                }
                            }
                        },
                        bottomBar = {
                            val bottomNavVisible by produceState(initialValue = true) {
                                showBottomNavEvent.receiveAsFlow().collectLatest { value = it }
                            }
                            if (!isTableUi()) {
                                AnimatedVisibility(
                                    visible = bottomNavVisible,
                                    enter = expandVertically(),
                                    exit = shrinkVertically(),
                                ) {
                                    ModernBottomNavigationBar(
                                        containerColor = MaterialTheme.colorScheme.surface,
                                        contentColor = MaterialTheme.colorScheme.onSurface
                                    ) {
                                        ModernTabNavigationItem(LibraryScreenSpec)
                                        if (vm.showUpdate.value) {
                                            ModernTabNavigationItem(UpdateScreenSpec)
                                        }
                                        if (vm.showUpdate.value) {
                                            ModernTabNavigationItem(HistoryScreenSpec)
                                        }
                                        ModernTabNavigationItem(ExtensionScreenSpec)
                                        ModernTabNavigationItem(MoreScreenSpec)
                                    }
                                }
                            }
                        }
                        ) { contentPadding ->

                    Box(
                            modifier = Modifier.padding(contentPadding)
                                    .consumeWindowInsets(contentPadding),
                    ) {

                        tabNavigator.saveableState(key = "currentTab", tabNavigator.current) {
                            tabNavigator.current.Content()
                        }
                    }

                }
            }
            val goToLibraryTab = { tabNavigator.current = LibraryScreenSpec }
            IBackHandler(
                    enabled = tabNavigator.current != LibraryScreenSpec,
                    onBack = goToLibraryTab,
            )

        }
    }

    suspend fun showBottomNav(show: Boolean) {
        showBottomNavEvent.send(show)
    }

}

@Composable
fun NavigationRailItem(tab: Tab) {
    val tabNavigator = LocalTabNavigator.current
    val selected = tabNavigator.current::class == tab::class
    val icon = tab.options.icon
    if (icon != null) {
        ModernNavigationRailItem(
            selected = selected,
            onClick = {
                tabNavigator.current = tab
            },
            icon = icon,
            label = tab.options.title,
            alwaysShowLabel = true
        )
    }
}

@Composable
fun Material3NavigationRailTabItem(tab: Tab) {
    val tabNavigator = LocalTabNavigator.current
    val selected = tabNavigator.current::class == tab::class
    val icon = tab.options.icon
    if (icon != null) {
        Material3NavigationRailItem(
            selected = selected,
            onClick = {
                tabNavigator.current = tab
            },
            icon = icon,
            label = tab.options.title,
            alwaysShowLabel = false
        )
    }
}

@Composable
private fun RowScope.TabNavigationItem(tab: Tab) {
    val tabNavigator = LocalTabNavigator.current
    val navController = requireNotNull(LocalNavigator.current) { "LocalNavigator not provided" }
    val isSelected = tabNavigator.current::class == tab::class
    NavigationBarItem(
            selected = isSelected,
            onClick = { tabNavigator.current = tab },
            icon = {
                Icon(
                        tab.options.icon!!,
                        contentDescription = tab.options.title,
                        tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onBackground
                )
            },
            label = {
                Text(
                        tab.options.title,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onBackground
                )
            },
            colors = NavigationBarItemDefaults.colors(
                    indicatorColor = androidx.compose.material3.MaterialTheme.colorScheme.primaryContainer,
                    unselectedTextColor = MaterialTheme.colorScheme.onBackground,
                    selectedTextColor = MaterialTheme.colorScheme.onBackground,
                    selectedIconColor = MaterialTheme.colorScheme.onBackground,
                    unselectedIconColor = MaterialTheme.colorScheme.onBackground
            ),
            alwaysShowLabel = true,
    )
}

@Composable
private fun RowScope.ModernTabNavigationItem(tab: Tab) {
    val tabNavigator = LocalTabNavigator.current
    val isSelected = tabNavigator.current::class == tab::class
    val icon = tab.options.icon
    if (icon != null) {
        ModernNavigationItem(
            selected = isSelected,
            onClick = { tabNavigator.current = tab },
            icon = icon,
            label = tab.options.title,
            alwaysShowLabel = true
        )
    }
}