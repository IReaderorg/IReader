package org.ireader.sources.extension

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.TabRowDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.ireader.common_models.entities.Catalog
import org.ireader.common_models.entities.CatalogLocal
import org.ireader.common_resources.UiEvent
import org.ireader.common_resources.UiText
import org.ireader.components.reusable_composable.MidSizeTextComposable
import org.ireader.core_ui.component.pagerTabIndicatorOffset
import org.ireader.core_ui.theme.AppColors
import org.ireader.sources.extension.composables.RemoteSourcesScreen
import org.ireader.sources.extension.composables.UserSourcesScreen
import org.ireader.ui_sources.R

@ExperimentalMaterialApi
@OptIn(ExperimentalPagerApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ExtensionScreen(
    modifier: Modifier = Modifier,
    viewModel: ExtensionViewModel,
    onRefreshCatalogs: () -> Unit,
    onClickCatalog: (Catalog) -> Unit,
    onClickInstall: (Catalog) -> Unit,
    onClickUninstall: (Catalog) -> Unit,
    onClickTogglePinned: (CatalogLocal) -> Unit,
    onSearchNavigate: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState()
    val context = LocalContext.current
    val snackBarHostState = remember { androidx.compose.material3.SnackbarHostState() }
    LaunchedEffect(key1 = true) {
        viewModel.eventFlow.collectLatest { event ->
            when (event) {
                is UiEvent.ShowSnackbar -> {
                    snackBarHostState.showSnackbar(
                        event.uiText.asString(context)
                    )
                }
                else -> {}
            }
        }
    }
    LaunchedEffect(key1 = true) {
        viewModel.clearExploreMode()
    }

    val pages = listOf<UiText>(
        UiText.StringResource(R.string.sources),
        UiText.StringResource(R.string.extensions),
    )

    var searchMode by remember {
        mutableStateOf(false)
    }
    val focusManager = LocalFocusManager.current
    androidx.compose.material3.Scaffold(
        topBar = {
            ExtensionScreenTopAppBar(
                searchMode = searchMode,
                query = viewModel.searchQuery ?: "",
                onValueChange = {
                    viewModel.searchQuery = it
                },
                onConfirm = {
                    focusManager.clearFocus()
                },
                pagerState = pagerState,
                onClose = {
                    searchMode = false
                    viewModel.searchQuery = ""
                },
                onSearchDisable = {
                    searchMode = false
                    viewModel.searchQuery = ""
                },
                onRefresh = {
                    viewModel.refreshCatalogs()
                },
                onSearchEnable = {
                    searchMode = true
                },
                onSearchNavigate = onSearchNavigate
            )
        },
        snackbarHost = {
            androidx.compose.material3.SnackbarHost(hostState = snackBarHostState) { data ->
            androidx.compose.material3.Snackbar(
                actionColor = MaterialTheme.colorScheme.primary,
                snackbarData = data,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
            )
        } },
    ) { padding ->
        // UserSourcesScreen(viewModel, navController)

        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            TabRow(
                selectedTabIndex = pagerState.currentPage,
                containerColor = AppColors.current.bars,
                contentColor = AppColors.current.onBars,
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        Modifier.pagerTabIndicatorOffset(pagerState, tabPositions)
                    )
                },
            ) {
                pages.forEachIndexed { index, title ->
                    Tab(
                        text = {
                            MidSizeTextComposable(text = title, color = Color.Unspecified)
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

            HorizontalPager(
                count = pages.size,
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                when (page) {
                    0 -> {
                        UserSourcesScreen(
                            onClickCatalog = onClickCatalog,
                            onClickTogglePinned = onClickTogglePinned,
                            state = viewModel,
                        )
                    }
                    1 -> {
                        RemoteSourcesScreen(
                            state = viewModel,
                            onRefreshCatalogs = onRefreshCatalogs,
                            onClickInstall = onClickInstall,
                            onClickUninstall = onClickUninstall,
                        )
                    }
                }
            }
            }

    }
}
