package ireader.presentation.core

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import ireader.presentation.core.ui.AppTab
import ireader.presentation.core.ui.GlobalSearchScreenSpec
import ireader.presentation.core.ui.ReaderScreenSpec
import ireader.presentation.core.ui.getIViewModel
import ireader.presentation.ui.component.IBackHandler
import ireader.presentation.ui.component.IScaffold
import ireader.presentation.ui.component.isTableUi
import ireader.presentation.ui.component.navigation.Material3NavigationRail
import ireader.presentation.ui.component.navigation.ModernBottomNavigationBar
import ireader.presentation.ui.component.navigation.ModernNavigationItem
import ireader.presentation.ui.component.navigation.ModernNavigationRailItem
import ireader.presentation.ui.home.library.viewmodel.LibraryViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
object MainStarterScreen {
    private const val TabFadeDuration = 200
    private val showBottomNavEvent = Channel<Boolean>()
    
    // Event channel for library filter sheet (consumed by LibraryScreenSpec)
    private val libraryFilterSheetEvent = Channel<Boolean>()
    
    /**
     * Flow to observe library filter sheet requests
     */
    fun libraryFilterSheetFlow() = libraryFilterSheetEvent.receiveAsFlow()

    @OptIn(ExperimentalLayoutApi::class)
    @Composable
    operator fun invoke() {
        val navController = requireNotNull(LocalNavigator.current) { "LocalNavigator not provided" }
        val vm: ScreenContentViewModel = getIViewModel()
        val libraryVm: LibraryViewModel = getIViewModel(key = "library")
        val scope = rememberCoroutineScope()
        
        // Track current tab using rememberSaveable for state preservation
        var currentTab by rememberSaveable { mutableStateOf(AppTab.Library.route) }
        val selectedTab = AppTab.fromRoute(currentTab)
        
        // Double-tap handlers for each tab
        val onLibraryDoubleTap: () -> Unit = remember {
            {
                // Navigate to Library tab, then request filter sheet
                currentTab = AppTab.Library.route
                scope.launch {
                    libraryFilterSheetEvent.send(true)
                }
            }
        }
        
        val onHistoryDoubleTap: () -> Unit = remember(libraryVm) {
            {
                // Navigate to History tab first, then go to last read chapter
                currentTab = AppTab.History.route
                scope.launch {
                    libraryVm.lastReadInfo?.let { info ->
                        navController.navigateTo(
                            ReaderScreenSpec(
                                bookId = info.novelId,
                                chapterId = info.chapterId
                            )
                        )
                    }
                }
            }
        }
        
        val onExtensionsDoubleTap: () -> Unit = remember {
            {
                // Navigate to Extensions tab first, then go to global search
                currentTab = AppTab.Extensions.route
                navController.navigateTo(GlobalSearchScreenSpec())
            }
        }
        
        val onMoreDoubleTap: () -> Unit = remember {
            {
                // Navigate to More tab first, then go to settings
                currentTab = AppTab.More.route
                navController.navigate(NavigationRoutes.settings)
            }
        }
        
        val onUpdatesDoubleTap: () -> Unit = remember {
            {
                // Navigate to Updates tab first, then go to downloader
                currentTab = AppTab.Updates.route
                navController.navigate(NavigationRoutes.downloader)
            }
        }
        
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
                                    onClick = { currentTab = AppTab.Library.route },
                                    onDoubleClick = onLibraryDoubleTap
                                )
                                if (vm.showUpdate.value) {
                                    NavigationRailTabItem(
                                        tab = AppTab.Updates,
                                        isSelected = selectedTab == AppTab.Updates,
                                        onClick = { currentTab = AppTab.Updates.route },
                                        onDoubleClick = onUpdatesDoubleTap
                                    )
                                }
                                if (vm.showUpdate.value) {
                                    NavigationRailTabItem(
                                        tab = AppTab.History,
                                        isSelected = selectedTab == AppTab.History,
                                        onClick = { currentTab = AppTab.History.route },
                                        onDoubleClick = onHistoryDoubleTap
                                    )
                                }
                                NavigationRailTabItem(
                                    tab = AppTab.Extensions,
                                    isSelected = selectedTab == AppTab.Extensions,
                                    onClick = { currentTab = AppTab.Extensions.route },
                                    onDoubleClick = onExtensionsDoubleTap
                                )
                                NavigationRailTabItem(
                                    tab = AppTab.More,
                                    isSelected = selectedTab == AppTab.More,
                                    onClick = { currentTab = AppTab.More.route },
                                    onDoubleClick = onMoreDoubleTap
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
                                    onClick = { currentTab = AppTab.Library.route },
                                    onDoubleClick = onLibraryDoubleTap
                                )
                                if (vm.showUpdate.value) {
                                    BottomNavTabItem(
                                        tab = AppTab.Updates,
                                        isSelected = selectedTab == AppTab.Updates,
                                        onClick = { currentTab = AppTab.Updates.route },
                                        onDoubleClick = onUpdatesDoubleTap
                                    )
                                }
                                if (vm.showUpdate.value) {
                                    BottomNavTabItem(
                                        tab = AppTab.History,
                                        isSelected = selectedTab == AppTab.History,
                                        onClick = { currentTab = AppTab.History.route },
                                        onDoubleClick = onHistoryDoubleTap
                                    )
                                }
                                BottomNavTabItem(
                                    tab = AppTab.Extensions,
                                    isSelected = selectedTab == AppTab.Extensions,
                                    onClick = { currentTab = AppTab.Extensions.route },
                                    onDoubleClick = onExtensionsDoubleTap
                                )
                                BottomNavTabItem(
                                    tab = AppTab.More,
                                    isSelected = selectedTab == AppTab.More,
                                    onClick = { currentTab = AppTab.More.route },
                                    onDoubleClick = onMoreDoubleTap
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
    onClick: () -> Unit,
    onDoubleClick: (() -> Unit)? = null
) {
    ModernNavigationRailItem(
        selected = isSelected,
        onClick = onClick,
        icon = tab.icon,
        label = tab.title,
        alwaysShowLabel = false,
        onDoubleClick = onDoubleClick
    )
}

@Composable
private fun RowScope.BottomNavTabItem(
    tab: AppTab,
    isSelected: Boolean,
    onClick: () -> Unit,
    onDoubleClick: (() -> Unit)? = null
) {
    ModernNavigationItem(
        selected = isSelected,
        onClick = onClick,
        icon = tab.icon,
        label = tab.title,
        alwaysShowLabel = true,
        onDoubleClick = onDoubleClick
    )
}
