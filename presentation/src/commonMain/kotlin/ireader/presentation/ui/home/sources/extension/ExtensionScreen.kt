package ireader.presentation.ui.home.sources.extension

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.PagerScope
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ireader.domain.models.entities.Catalog
import ireader.i18n.UiEvent
import ireader.i18n.asString
import ireader.i18n.resources.Res
import ireader.i18n.resources.*
import ireader.presentation.ui.component.reusable_composable.MidSizeTextComposable
import ireader.presentation.ui.core.theme.AppColors
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.presentation.ui.core.utils.horizontalPadding
import ireader.presentation.ui.home.sources.extension.composables.RemoteSourcesScreen
import ireader.presentation.ui.home.sources.extension.composables.UserSourcesScreen
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@ExperimentalMaterialApi
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExtensionScreen(
    modifier: Modifier = Modifier,
    vm: ExtensionViewModel,
    onClickCatalog: (Catalog) -> Unit,
    onClickInstall: (Catalog) -> Unit,
    onClickUninstall: (Catalog) -> Unit,
    onClickTogglePinned: (Catalog) -> Unit,
    onCancelInstaller: ((Catalog) -> Unit)? = null,
    snackBarHostState: androidx.compose.material3.SnackbarHostState,
    onShowDetails: ((Catalog) -> Unit)? = null,
    onMigrateFromSource: ((Long) -> Unit)? = null,
    onNavigateToBrowseSettings: (() -> Unit)? = null,
    scaffoldPadding: PaddingValues
) {
    val localizeHelper =
        requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    LaunchedEffect(key1 = true) {
        vm.eventFlow.collectLatest { event ->
            when (event) {
                is UiEvent.ShowSnackbar -> {
                    snackBarHostState.showSnackbar(
                        event.uiText.asString(localizeHelper)
                    )
                }

                else -> {}
            }
        }
    }
    val pages = remember {
        listOf<String>(
            localizeHelper.localize(Res.string.sources),
            localizeHelper.localize(Res.string.extensions),
        )
    }
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(scaffoldPadding)
    ) {
        ExtensionContent(
            vm = vm,
            state = vm,
            onClickCatalog = onClickCatalog,
            onClickInstall = onClickInstall,
            onClickTogglePinned = onClickTogglePinned,
            onClickUninstall = onClickUninstall,
            pages = pages,
            onCancelInstaller = onCancelInstaller,
            onShowDetails = onShowDetails,
        )

    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SourceHeader(
    modifier: Modifier = Modifier,
    language: String,
) {
    val localizeHelper =
        requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = LocaleHelper.getSourceDisplayName(language, localizeHelper),
            modifier = modifier
                .padding(horizontal = horizontalPadding, vertical = 8.dp),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ExtensionContent(
    pages: List<String>,
    modifier: Modifier = Modifier,
    state: CatalogsState,
    onClickCatalog: (Catalog) -> Unit,
    onClickTogglePinned: (Catalog) -> Unit,
    vm: ExtensionViewModel,
    onClickInstall: (Catalog) -> Unit,
    onClickUninstall: (Catalog) -> Unit,
    onCancelInstaller: ((Catalog) -> Unit)? = null,
    onShowDetails: ((Catalog) -> Unit)? = null,

    ) {
    val pagerState = rememberPagerState(
        initialPage = 0,
        initialPageOffsetFraction = 0f
    ) {
        pages.size
    }
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect {
            vm.currentPagerPage = pagerState.currentPage
        }
    }
    ExtensionTabs(pagerState = pagerState, pages = pages)
    ExtensionPager(
        pagerState = pagerState,
        vm = vm,
        state = vm,
        onClickCatalog = onClickCatalog,
        onClickInstall = onClickInstall,
        onClickTogglePinned = onClickTogglePinned,
        onClickUninstall = onClickUninstall,
        pages = pages,
        onCancelInstaller = onCancelInstaller,
        onShowDetails = onShowDetails
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ExtensionPager(
    pagerState: androidx.compose.foundation.pager.PagerState,
    pages: List<String>,
    modifier: Modifier = Modifier,
    state: CatalogsState,
    onClickCatalog: (Catalog) -> Unit,
    onClickTogglePinned: (Catalog) -> Unit,
    vm: ExtensionViewModel,
    onClickInstall: (Catalog) -> Unit,
    onClickUninstall: (Catalog) -> Unit,
    onCancelInstaller: ((Catalog) -> Unit)? = null,
    onShowDetails: ((Catalog) -> Unit)? = null,
) {
    HorizontalPager(
        modifier = Modifier.fillMaxSize(),
        state = pagerState,
        pageSpacing = 0.dp,
        userScrollEnabled = true,
        reverseLayout = false,
        contentPadding = PaddingValues(0.dp),
        pageSize = PageSize.Fill,
        key = null,
        pageNestedScrollConnection = PagerDefaults.pageNestedScrollConnection(
            state = pagerState,
            Orientation.Horizontal
        ),
        pageContent = { page ->
            when (page) {
                0 -> {
                    UserSourcesScreen(
                        onClickCatalog = onClickCatalog,
                        onClickTogglePinned = onClickTogglePinned,
                        vm = vm,
                        onShowDetails = onShowDetails,
                    )
                }

                1 -> {
                    RemoteSourcesScreen(
                        vm = vm,
                        onClickInstall = onClickInstall,
                        onClickUninstall = onClickUninstall,
                        onCancelInstaller = onCancelInstaller,
                    )
                }
            }
        }
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ExtensionTabs(
    modifier: Modifier = Modifier,
    pagerState: androidx.compose.foundation.pager.PagerState,
    pages: List<String>,

    ) {
    val scope = rememberCoroutineScope()
    TabRow(
        selectedTabIndex = pagerState.currentPage,
        containerColor = AppColors.current.bars,
        contentColor = AppColors.current.onBars,
    ) {
        pages.forEachIndexed { index, title ->
            Tab(
                text = {
                    MidSizeTextComposable(
                        text = title,
                        color = androidx.compose.ui.graphics.Color.Unspecified
                    )
                },
                selected = pagerState.currentPage == index,
                onClick = {
                    scope.launch { pagerState.animateScrollToPage(index) }
                },
                selectedContentColor = MaterialTheme.colorScheme.primary,
                unselectedContentColor = MaterialTheme.colorScheme.onBackground,
            )
        }
    }
}

