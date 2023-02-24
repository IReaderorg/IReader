package ireader.presentation.core

import androidx.compose.animation.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material.BottomNavigation
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.CurrentScreen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.tab.CurrentTab
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabNavigator
import ireader.core.source.SourceFactory
import ireader.presentation.core.MainStarterScreen.showBottomNavEvent
import ireader.presentation.core.theme.AppTheme
import ireader.presentation.core.theme.AppThemeViewModel
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

    @OptIn(ExperimentalAnimationApi::class)
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        TabNavigator(LibraryScreenSpec) { tabNavigator ->
            CompositionLocalProvider(LocalNavigator provides navigator) {
                IScaffold(
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
                                TabNavigationItem(UpdateScreenSpec)
                                TabNavigationItem(HistoryScreenSpec)
                                TabNavigationItem(ExtensionScreenSpec)
                                TabNavigationItem(MoreScreenSpec)
                            }
                        }
                    },

                    ) { contentPadding ->

                    Box(
                        modifier = Modifier,
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
        }
    }

    suspend fun showBottomNav(show: Boolean) {
        showBottomNavEvent.send(show)
    }

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