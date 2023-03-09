package ireader.presentation.core

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabNavigator
import ireader.domain.utils.isTabletUi
import ireader.presentation.core.ui.*
import ireader.presentation.ui.component.IScaffold
import ireader.presentation.ui.core.theme.AppColors
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.receiveAsFlow
import soup.compose.material.motion.animation.materialFadeThroughIn
import soup.compose.material.motion.animation.materialFadeThroughOut

@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
object MainStarterScreen : VoyagerScreen() {
    private const val TabFadeDuration = 200
    private val showBottomNavEvent = Channel<Boolean>()

    @OptIn(ExperimentalAnimationApi::class, ExperimentalLayoutApi::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val vm: ScreenContentViewModel = getIViewModel()
        val context = LocalContext.current
        TabNavigator(LibraryScreenSpec) { tabNavigator ->
            CompositionLocalProvider(LocalNavigator provides navigator) {
                IScaffold(
                    startBar = {
                        if (context.isTabletUi()) {
                            NavigationRail {
                                NavigationRailItem(LibraryScreenSpec)
                                if (vm.showUpdate.value) {
                                    NavigationRailItem(UpdateScreenSpec)
                                }
                                if (vm.showUpdate.value) {
                                    NavigationRailItem(HistoryScreenSpec)
                                }
                                NavigationRailItem(ExtensionScreenSpec)
                                NavigationRailItem(MoreScreenSpec)
                            }
                        }
                    },
                    bottomBar = {
                        val bottomNavVisible by produceState(initialValue = true) {
                            showBottomNavEvent.receiveAsFlow().collectLatest { value = it }
                        }
                        AnimatedVisibility(
                            visible = bottomNavVisible,
                            enter = expandVertically(),
                            exit = shrinkVertically(),
                        ) {
                            NavigationBar(
                                modifier = Modifier,
                                containerColor = AppColors.current.bars,
                                contentColor = AppColors.current.onBars,
                                tonalElevation = 0.dp,
                            ) {
                                TabNavigationItem(LibraryScreenSpec)
                                if (vm.showUpdate.value) {
                                    TabNavigationItem(UpdateScreenSpec)
                                }
                                if (vm.showUpdate.value) {
                                    TabNavigationItem(HistoryScreenSpec)
                                }
                                TabNavigationItem(ExtensionScreenSpec)
                                TabNavigationItem(MoreScreenSpec)
                            }
                        }
                    },

                    ) { contentPadding ->

                    Box(
                        modifier = Modifier.padding(contentPadding)
                            .consumedWindowInsets(contentPadding),
                    ) {
                        AnimatedContent(
                            targetState = tabNavigator.current,
                            transitionSpec = {
                                materialFadeThroughIn(
                                    initialScale = 1f,
                                    durationMillis = TabFadeDuration
                                ) with
                                        materialFadeThroughOut(durationMillis = TabFadeDuration)
                            },
                            content = {
                                tabNavigator.saveableState(key = "currentTab", it) {
                                    it.Content()
                                }
                            },
                        )
                    }

                }
            }
            val goToLibraryTab = { tabNavigator.current = LibraryScreenSpec }
            BackHandler(
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
    NavigationRailItem(
        selected = selected,
        onClick = {
            tabNavigator.current = tab
        },
        icon = {
            Icon(
                tab.options.icon!!,
                contentDescription = tab.options.title,
                tint = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onBackground
            )
        },
        label = {
            Text(
                text = tab.options.title,
                style = MaterialTheme.typography.labelLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        alwaysShowLabel = true,
    )
}

@Composable
private fun RowScope.TabNavigationItem(tab: Tab) {
    val tabNavigator = LocalTabNavigator.current
    val navigator = LocalNavigator.currentOrThrow
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