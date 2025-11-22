package ireader.presentation.ui.home.sources.extension

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ireader.domain.models.entities.Catalog
import ireader.i18n.UiEvent
import ireader.i18n.asString
import ireader.i18n.resources.Res
import ireader.i18n.resources.*
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.presentation.ui.home.sources.extension.composables.ModernRemoteSourcesScreen
import ireader.presentation.ui.home.sources.extension.composables.ModernUserSourcesScreen
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Clean and simple extension screen
 * - Material 3 tabs
 * - Simple design
 * - Easy to use
 */
@ExperimentalMaterialApi
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun CleanExtensionScreen(
    modifier: Modifier = Modifier,
    vm: ExtensionViewModel,
    onClickCatalog: (Catalog) -> Unit,
    onClickInstall: (Catalog) -> Unit,
    onClickUninstall: (Catalog) -> Unit,
    onClickTogglePinned: (Catalog) -> Unit,
    onCancelInstaller: ((Catalog) -> Unit)? = null,
    snackBarHostState: SnackbarHostState,
    onShowDetails: ((Catalog) -> Unit)? = null,
    onMigrateFromSource: ((Long) -> Unit)? = null,
    onNavigateToBrowseSettings: (() -> Unit)? = null,
    scaffoldPadding: PaddingValues
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current)
    
    LaunchedEffect(key1 = true) {
        vm.eventFlow.collectLatest { event ->
            when (event) {
                is UiEvent.ShowSnackbar -> {
                    snackBarHostState.showSnackbar(event.uiText.asString(localizeHelper))
                }
                else -> {}
            }
        }
    }

    val pages = remember {
        listOf(
            localizeHelper.localize(Res.string.sources),
            localizeHelper.localize(Res.string.extensions),
        )
    }

    val pagerState = rememberPagerState(
        initialPage = 0,
        initialPageOffsetFraction = 0f
    ) { pages.size }

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect {
            vm.currentPagerPage = pagerState.currentPage
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(scaffoldPadding)
    ) {
        // Simple Material 3 tabs
        TabRow(
            selectedTabIndex = pagerState.currentPage,
            containerColor = MaterialTheme.colorScheme.surface,
        ) {
            val scope = rememberCoroutineScope()
            pages.forEachIndexed { index, title ->
                Tab(
                    selected = pagerState.currentPage == index,
                    onClick = {
                        scope.launch { pagerState.animateScrollToPage(index) }
                    },
                    text = {
                        Text(
                            text = title,
                            fontWeight = if (pagerState.currentPage == index) 
                                FontWeight.SemiBold 
                            else 
                                FontWeight.Normal
                        )
                    }
                )
            }
        }

        HorizontalPager(
            modifier = Modifier.fillMaxSize(),
            state = pagerState,
        ) { page ->
            when (page) {
                0 -> ModernUserSourcesScreen(
                    onClickCatalog = onClickCatalog,
                    onClickTogglePinned = onClickTogglePinned,
                    vm = vm,
                    onShowDetails = onShowDetails,
                    onMigrateFromSource = onMigrateFromSource,
                )
                1 -> ModernRemoteSourcesScreen(
                    vm = vm,
                    onClickInstall = onClickInstall,
                    onClickUninstall = onClickUninstall,
                    onCancelInstaller = onCancelInstaller,
                )
            }
        }
    }
}
