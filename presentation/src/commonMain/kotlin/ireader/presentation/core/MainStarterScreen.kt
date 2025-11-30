package ireader.presentation.core

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import ireader.presentation.core.ui.*
import ireader.presentation.ui.component.IBackHandler
import ireader.presentation.ui.component.IScaffold
import ireader.presentation.ui.component.isTableUi
import ireader.presentation.ui.component.navigation.ModernBottomNavigationBar
import ireader.presentation.ui.component.navigation.ModernNavigationItem
import ireader.presentation.ui.component.navigation.ModernNavigationRailItem
import ireader.presentation.ui.component.navigation.Material3NavigationRail
import ireader.presentation.ui.component.navigation.Material3NavigationRailItem
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
        
        // Track current tab using rememberSaveable for state preservation
        var currentTab by rememberSaveable { mutableStateOf(AppTab.Library.route) }
        val selectedTab = AppTab.fromRoute(currentTab)
        
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
                                NavigationRailTabItem(
                                    tab = AppTab.Library,
                                    isSelected = selectedTab == AppTab.Library,
                                    onClick = { currentTab = AppTab.Library.route }
                                )
                                if (vm.showUpdate.value) {
                                    NavigationRailTabItem(
                                        tab = AppTab.Updates,
                                        isSelected = selectedTab == AppTab.Updates,
                                        onClick = { currentTab = AppTab.Updates.route }
                                    )
                                }
                                if (vm.showUpdate.value) {
                                    NavigationRailTabItem(
                                        tab = AppTab.History,
                                        isSelected = selectedTab == AppTab.History,
                                        onClick = { currentTab = AppTab.History.route }
                                    )
                                }
                                NavigationRailTabItem(
                                    tab = AppTab.Extensions,
                                    isSelected = selectedTab == AppTab.Extensions,
                                    onClick = { currentTab = AppTab.Extensions.route }
                                )
                                NavigationRailTabItem(
                                    tab = AppTab.More,
                                    isSelected = selectedTab == AppTab.More,
                                    onClick = { currentTab = AppTab.More.route }
                                )
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
                                BottomNavTabItem(
                                    tab = AppTab.Library,
                                    isSelected = selectedTab == AppTab.Library,
                                    onClick = { currentTab = AppTab.Library.route }
                                )
                                if (vm.showUpdate.value) {
                                    BottomNavTabItem(
                                        tab = AppTab.Updates,
                                        isSelected = selectedTab == AppTab.Updates,
                                        onClick = { currentTab = AppTab.Updates.route }
                                    )
                                }
                                if (vm.showUpdate.value) {
                                    BottomNavTabItem(
                                        tab = AppTab.History,
                                        isSelected = selectedTab == AppTab.History,
                                        onClick = { currentTab = AppTab.History.route }
                                    )
                                }
                                BottomNavTabItem(
                                    tab = AppTab.Extensions,
                                    isSelected = selectedTab == AppTab.Extensions,
                                    onClick = { currentTab = AppTab.Extensions.route }
                                )
                                BottomNavTabItem(
                                    tab = AppTab.More,
                                    isSelected = selectedTab == AppTab.More,
                                    onClick = { currentTab = AppTab.More.route }
                                )
                            }
                        }
                    }
                }
            ) { contentPadding ->
                Box(
                    modifier = Modifier
                        .padding(contentPadding)
                        .consumeWindowInsets(contentPadding),
                ) {
                    // Use Crossfade for smooth tab transitions
                    Crossfade(
                        targetState = selectedTab,
                        label = "TabContent"
                    ) { tab ->
                        tab.Content()
                    }
                }
            }
            
            // Back handler to return to Library tab
            val goToLibraryTab = { currentTab = AppTab.Library.route }
            IBackHandler(
                enabled = selectedTab != AppTab.Library,
                onBack = goToLibraryTab,
            )
        }
    }

    suspend fun showBottomNav(show: Boolean) {
        showBottomNavEvent.send(show)
    }
}

@Composable
private fun NavigationRailTabItem(
    tab: AppTab,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Material3NavigationRailItem(
        selected = isSelected,
        onClick = onClick,
        icon = tab.icon,
        label = tab.title,
        alwaysShowLabel = false
    )
}

@Composable
private fun RowScope.BottomNavTabItem(
    tab: AppTab,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    ModernNavigationItem(
        selected = isSelected,
        onClick = onClick,
        icon = tab.icon,
        label = tab.title,
        alwaysShowLabel = true
    )
}
