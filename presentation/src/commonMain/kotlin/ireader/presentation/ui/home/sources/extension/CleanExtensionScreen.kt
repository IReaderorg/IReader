//package ireader.presentation.ui.home.sources.extension
//
//import androidx.compose.foundation.ExperimentalFoundationApi
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.PaddingValues
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.pager.HorizontalPager
//import androidx.compose.foundation.pager.rememberPagerState
//import androidx.compose.material3.ExperimentalMaterial3Api
//import androidx.compose.material3.MaterialTheme
//import androidx.compose.material3.SnackbarHostState
//import androidx.compose.material3.Tab
//import androidx.compose.material3.TabRow
//import androidx.compose.material3.Text
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.LaunchedEffect
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.rememberCoroutineScope
//import androidx.compose.runtime.snapshotFlow
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.text.font.FontWeight
//import ireader.domain.models.entities.Catalog
//import ireader.i18n.UiEvent
//import ireader.i18n.asString
//import ireader.i18n.resources.*
//import ireader.i18n.resources.extensions
//import ireader.i18n.resources.sources
//import ireader.presentation.ui.core.theme.LocalLocalizeHelper
//import ireader.presentation.ui.home.sources.extension.composables.ModernRemoteSourcesScreen
//import ireader.presentation.ui.home.sources.extension.composables.ModernUserSourcesScreen
//import kotlinx.coroutines.flow.collectLatest
//import kotlinx.coroutines.launch
//
///**
// * Clean and simple extension screen
// * - Material 3 tabs
// * - Simple design
// * - Easy to use
// */
//@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
//@Composable
//fun CleanExtensionScreen(
//    modifier: Modifier = Modifier,
//    vm: ExtensionViewModel,
//    onClickCatalog: (Catalog) -> Unit,
//    onClickInstall: (Catalog) -> Unit,
//    onClickUninstall: (Catalog) -> Unit,
//    onClickTogglePinned: (Catalog) -> Unit,
//    onCancelInstaller: ((Catalog) -> Unit)? = null,
//    snackBarHostState: SnackbarHostState,
//    onShowDetails: ((Catalog) -> Unit)? = null,
//    onMigrateFromSource: ((Long) -> Unit)? = null,
//    onNavigateToBrowseSettings: (() -> Unit)? = null,
//    onNavigateToUserSources: (() -> Unit)? = null,
//    scaffoldPadding: PaddingValues
//) {
//    val localizeHelper = requireNotNull(LocalLocalizeHelper.current)
//
//    LaunchedEffect(Unit) {
//        vm.eventFlow.collectLatest { event ->
//            when (event) {
//                is UiEvent.ShowSnackbar -> {
//                    snackBarHostState.showSnackbar(event.uiText.asString(localizeHelper))
//                }
//                else -> {}
//            }
//        }
//    }
//
//    val pages = remember {
//        listOf(
//            localizeHelper.localize(Res.string.sources),
//            localizeHelper.localize(Res.string.extensions),
//        )
//    }
//
//    val pagerState = rememberPagerState(
//        initialPage = 0,
//        initialPageOffsetFraction = 0f
//    ) { pages.size }
//
//    LaunchedEffect(pagerState) {
//        snapshotFlow { pagerState.currentPage }.collect {
//            vm.setCurrentPagerPage(pagerState.currentPage)
//        }
//    }
//
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(scaffoldPadding)
//    ) {
//        // Simple Material 3 tabs
//        TabRow(
//            selectedTabIndex = pagerState.currentPage,
//            containerColor = MaterialTheme.colorScheme.surface,
//        ) {
//            val scope = rememberCoroutineScope()
//            pages.forEachIndexed { index, title ->
//                Tab(
//                    selected = pagerState.currentPage == index,
//                    onClick = {
//                        scope.launch { pagerState.animateScrollToPage(index) }
//                    },
//                    text = {
//                        Text(
//                            text = title,
//                            fontWeight = if (pagerState.currentPage == index)
//                                FontWeight.SemiBold
//                            else
//                                FontWeight.Normal
//                        )
//                    }
//                )
//            }
//        }
//
//        HorizontalPager(
//            modifier = Modifier.fillMaxSize(),
//            state = pagerState,
//        ) { page ->
//            when (page) {
//                0 -> ModernUserSourcesScreen(
//                    onClickCatalog = onClickCatalog,
//                    onClickTogglePinned = onClickTogglePinned,
//                    vm = vm,
//                    onShowDetails = onShowDetails,
//                    onMigrateFromSource = onMigrateFromSource,
//                    onNavigateToUserSources = onNavigateToUserSources,
//                    onDeleteUserSource = { sourceId -> vm.deleteUserSourceById(sourceId) },
//                )
//                1 -> ModernRemoteSourcesScreen(
//                    vm = vm,
//                    onClickInstall = onClickInstall,
//                    onClickUninstall = onClickUninstall,
//                    onCancelInstaller = onCancelInstaller,
//                )
//            }
//        }
//    }
//}
