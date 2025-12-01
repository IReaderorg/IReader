package ireader.presentation.core

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import kotlinx.coroutines.delay
import ireader.presentation.core.ui.AppTab
import ireader.presentation.ui.component.ExtensionsShimmerLoading
import ireader.presentation.ui.component.HistoryShimmerLoading
import ireader.presentation.ui.component.LibraryShimmerLoading
import ireader.presentation.ui.component.SettingsShimmerLoading
import ireader.presentation.ui.component.UpdatesShimmerLoading
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
    private val showBottomNavEvent = Channel<Boolean>()
    private val libraryFilterSheetEvent = Channel<Boolean>()
    
    fun libraryFilterSheetFlow() = libraryFilterSheetEvent.receiveAsFlow()

    @OptIn(ExperimentalLayoutApi::class)
    @Composable
    operator fun invoke() {
        val navController = requireNotNull(LocalNavigator.current) { "LocalNavigator not provided" }
        val vm: ScreenContentViewModel = getIViewModel()
        val libraryVm: LibraryViewModel = getIViewModel(key = "library")
        val scope = rememberCoroutineScope()
        
        // Use Int index for faster comparison
        var currentTabIndex by rememberSaveable { mutableIntStateOf(0) }
        
        // Track visited tabs - once visited, tab stays in memory
        var visitedTabs by rememberSaveable { mutableStateOf(setOf(0)) }
        
        // Mark current tab as visited
        if (currentTabIndex !in visitedTabs) {
            visitedTabs = visitedTabs + currentTabIndex
        }
        
        // Pre-initialize all tabs in background after first frame
        // This makes subsequent tab switches instant
        LaunchedEffect(Unit) {
            // Wait for first frame to render
            delay(100)
            // Pre-initialize remaining tabs one by one with small delays
            // to avoid blocking the main thread
            listOf(3, 4, 1, 2).forEach { tabIndex ->
                if (tabIndex !in visitedTabs) {
                    delay(50) // Small delay between each tab
                    visitedTabs = visitedTabs + tabIndex
                }
            }
        }
        
        // Stable click handlers
        val tabClickHandlers = remember {
            TabClickHandlers(
                onLibraryClick = { currentTabIndex = 0 },
                onUpdatesClick = { currentTabIndex = 1 },
                onHistoryClick = { currentTabIndex = 2 },
                onExtensionsClick = { currentTabIndex = 3 },
                onMoreClick = { currentTabIndex = 4 }
            )
        }
        
        // Double-tap handlers - must return Unit, not Job
        val onLibraryDoubleTap: () -> Unit = remember {
            {
                currentTabIndex = 0
                scope.launch { libraryFilterSheetEvent.send(true) }
                Unit
            }
        }
        
        val onHistoryDoubleTap: () -> Unit = remember(libraryVm) {
            {
                currentTabIndex = 2
                scope.launch {
                    libraryVm.lastReadInfo?.let { info ->
                        navController.navigateTo(ReaderScreenSpec(info.novelId, info.chapterId))
                    }
                }
                Unit
            }
        }
        
        val onExtensionsDoubleTap: () -> Unit = remember {
            {
                currentTabIndex = 3
                navController.navigateTo(GlobalSearchScreenSpec())
            }
        }
        
        val onMoreDoubleTap: () -> Unit = remember {
            {
                currentTabIndex = 4
                navController.navigate(NavigationRoutes.settings)
            }
        }
        
        val onUpdatesDoubleTap: () -> Unit = remember {
            {
                currentTabIndex = 1
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
                                    isSelected = currentTabIndex == 0,
                                    onClick = tabClickHandlers.onLibraryClick,
                                    onDoubleClick = onLibraryDoubleTap
                                )
                                if (vm.showUpdate.value) {
                                    NavigationRailTabItem(
                                        tab = AppTab.Updates,
                                        isSelected = currentTabIndex == 1,
                                        onClick = tabClickHandlers.onUpdatesClick,
                                        onDoubleClick = onUpdatesDoubleTap
                                    )
                                    NavigationRailTabItem(
                                        tab = AppTab.History,
                                        isSelected = currentTabIndex == 2,
                                        onClick = tabClickHandlers.onHistoryClick,
                                        onDoubleClick = onHistoryDoubleTap
                                    )
                                }
                                NavigationRailTabItem(
                                    tab = AppTab.Extensions,
                                    isSelected = currentTabIndex == 3,
                                    onClick = tabClickHandlers.onExtensionsClick,
                                    onDoubleClick = onExtensionsDoubleTap
                                )
                                NavigationRailTabItem(
                                    tab = AppTab.More,
                                    isSelected = currentTabIndex == 4,
                                    onClick = tabClickHandlers.onMoreClick,
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
                                    isSelected = currentTabIndex == 0,
                                    onClick = tabClickHandlers.onLibraryClick,
                                    onDoubleClick = onLibraryDoubleTap
                                )
                                if (vm.showUpdate.value) {
                                    BottomNavTabItem(
                                        tab = AppTab.Updates,
                                        isSelected = currentTabIndex == 1,
                                        onClick = tabClickHandlers.onUpdatesClick,
                                        onDoubleClick = onUpdatesDoubleTap
                                    )
                                    BottomNavTabItem(
                                        tab = AppTab.History,
                                        isSelected = currentTabIndex == 2,
                                        onClick = tabClickHandlers.onHistoryClick,
                                        onDoubleClick = onHistoryDoubleTap
                                    )
                                }
                                BottomNavTabItem(
                                    tab = AppTab.Extensions,
                                    isSelected = currentTabIndex == 3,
                                    onClick = tabClickHandlers.onExtensionsClick,
                                    onDoubleClick = onExtensionsDoubleTap
                                )
                                BottomNavTabItem(
                                    tab = AppTab.More,
                                    isSelected = currentTabIndex == 4,
                                    onClick = tabClickHandlers.onMoreClick,
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
                        .consumeWindowInsets(contentPadding)
                        .fillMaxSize(),
                ) {
                    // Optimized tab container with lazy init + memory retention
                    PersistentTabContainer(
                        currentTabIndex = currentTabIndex,
                        visitedTabs = visitedTabs,
                        showUpdates = vm.showUpdate.value
                    )
                }
            }
            
            IBackHandler(
                enabled = currentTabIndex != 0,
                onBack = { currentTabIndex = 0 },
            )
        }
    }

    suspend fun showBottomNav(show: Boolean) {
        showBottomNavEvent.send(show)
    }
}

private class TabClickHandlers(
    val onLibraryClick: () -> Unit,
    val onUpdatesClick: () -> Unit,
    val onHistoryClick: () -> Unit,
    val onExtensionsClick: () -> Unit,
    val onMoreClick: () -> Unit
)

/**
 * Persistent tab container - tabs are initialized on first visit and kept in memory.
 * Shows shimmer loading while tab content is being initialized.
 */
@Composable
private fun PersistentTabContainer(
    currentTabIndex: Int,
    visitedTabs: Set<Int>,
    showUpdates: Boolean
) {
    // Library tab (index 0) - always initialized
    TabSlot(
        tabIndex = 0,
        isVisible = currentTabIndex == 0,
        isInitialized = 0 in visitedTabs,
        shimmerContent = { LibraryShimmerLoading() }
    ) {
        AppTab.Library.Content()
    }
    
    // Updates tab (index 1)
    if (showUpdates) {
        TabSlot(
            tabIndex = 1,
            isVisible = currentTabIndex == 1,
            isInitialized = 1 in visitedTabs,
            shimmerContent = { UpdatesShimmerLoading() }
        ) {
            AppTab.Updates.Content()
        }
    }
    
    // History tab (index 2)
    if (showUpdates) {
        TabSlot(
            tabIndex = 2,
            isVisible = currentTabIndex == 2,
            isInitialized = 2 in visitedTabs,
            shimmerContent = { HistoryShimmerLoading() }
        ) {
            AppTab.History.Content()
        }
    }
    
    // Extensions tab (index 3)
    TabSlot(
        tabIndex = 3,
        isVisible = currentTabIndex == 3,
        isInitialized = 3 in visitedTabs,
        shimmerContent = { ExtensionsShimmerLoading() }
    ) {
        AppTab.Extensions.Content()
    }
    
    // More tab (index 4)
    TabSlot(
        tabIndex = 4,
        isVisible = currentTabIndex == 4,
        isInitialized = 4 in visitedTabs,
        shimmerContent = { SettingsShimmerLoading() }
    ) {
        AppTab.More.Content()
    }
}

/**
 * Individual tab slot - lazy initialization with shimmer loading.
 * Shows shimmer while content is being initialized, then fades to actual content.
 * Hidden tabs are moved off-screen to prevent touch interception.
 */
@Composable
private inline fun TabSlot(
    @Suppress("UNUSED_PARAMETER") tabIndex: Int, // Used for debugging/logging if needed
    isVisible: Boolean,
    isInitialized: Boolean,
    crossinline shimmerContent: @Composable () -> Unit,
    crossinline content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                // Visible: normal rendering
                // Hidden: move off-screen to prevent touch interception
                alpha = if (isVisible) 1f else 0f
                translationX = if (isVisible) 0f else 10000f
            }
    ) {
        if (isVisible && !isInitialized) {
            // Show shimmer while waiting for initialization
            shimmerContent()
        } else if (isInitialized) {
            // Show actual content
            content()
        }
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
