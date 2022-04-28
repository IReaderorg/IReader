package org.ireader.sources.extension


import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.navigation.NavController
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.pagerTabIndicatorOffset
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.flow.collectLatest
import org.ireader.common_models.entities.Catalog
import org.ireader.common_models.entities.CatalogLocal
import org.ireader.core.utils.UiEvent
import org.ireader.core_ui.theme.AppColors
import org.ireader.core_ui.ui_components.components.ISnackBarHost
import org.ireader.core_ui.ui_components.reusable_composable.AppIconButton
import org.ireader.core_ui.ui_components.reusable_composable.AppTextField
import org.ireader.core_ui.ui_components.reusable_composable.BigSizeTextComposable
import org.ireader.core_ui.ui_components.reusable_composable.MidSizeTextComposable
import org.ireader.presentation.presentation.Toolbar
import org.ireader.sources.extension.composables.RemoteSourcesScreen
import org.ireader.sources.extension.composables.UserSourcesScreen


@ExperimentalMaterialApi
@OptIn(ExperimentalPagerApi::class)
@Composable
fun ExtensionScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    viewModel: ExtensionViewModel,
    onRefreshCatalogs: () -> Unit,
    onClickCatalog: (Catalog) -> Unit,
    onClickInstall: (Catalog) -> Unit,
    onClickUninstall: (Catalog) -> Unit,
    onClickTogglePinned: (CatalogLocal) -> Unit,
    onSearchNavigate:()-> Unit
) {
    val pagerState = rememberPagerState()
    val scope = rememberCoroutineScope()
    val scaffoldState = rememberScaffoldState()
    val context = LocalContext.current


    LaunchedEffect(key1 = true) {
        viewModel.eventFlow.collectLatest { event ->
            when (event) {
                is UiEvent.ShowSnackbar -> {
                    scaffoldState.snackbarHostState.showSnackbar(
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


    val pages = listOf<String>(
        "Sources",
        "Extensions"
    )

    var searchMode by remember {
        mutableStateOf(false)
    }
    val focusManager = LocalFocusManager.current
    Scaffold(
        topBar = {
            Toolbar(
                title = {
                    if (!searchMode) {
                        BigSizeTextComposable(text = "Extensions")
                    } else {
                        AppTextField(
                            query = viewModel.searchQuery ?: "",
                            onValueChange = {
                                viewModel.searchQuery = it
                            },
                            onConfirm = {
                                focusManager.clearFocus()
                            },
                        )
                    }
                },
                actions = {
                    if (pagerState.currentPage == 1) {
                        if (searchMode) {
                            AppIconButton(
                                imageVector = Icons.Default.Close,
                                title = "Close",
                                onClick = {
                                    searchMode = false
                                    viewModel.searchQuery = ""
                                },
                            )
                        } else {
                            AppIconButton(
                                imageVector = Icons.Default.Search,
                                title = "Search",
                                onClick = {
                                    searchMode = true
                                },
                            )
                        }
                        AppIconButton(
                            imageVector = Icons.Default.Refresh,
                            title = "Refresh",
                            onClick = {
                                viewModel.refreshCatalogs()
                            },
                        )
                    } else {
                        if (searchMode) {
                            AppIconButton(
                                imageVector = Icons.Default.Close,
                                title = "Close",
                                onClick = {
                                    searchMode = false
                                    viewModel.searchQuery = ""
                                },
                            )
                        } else {
                            AppIconButton(
                                imageVector = Icons.Default.Search,
                                title = "Search",
                                onClick = {
                                    searchMode = true
                                },
                            )
                            AppIconButton(
                                imageVector = Icons.Default.TravelExplore,
                                title = "Search",
                                onClick = {
                                    onSearchNavigate()
                                },
                            )
                        }
                    }
                },
                navigationIcon = if (searchMode) {
                    {
                        AppIconButton(imageVector = Icons.Default.ArrowBack,
                            title = "Disable Search",
                            onClick = {
                                searchMode = false
                                viewModel.searchQuery = ""
                            })

                    }
                } else null
            )
        },
        scaffoldState = scaffoldState,
        snackbarHost = { ISnackBarHost(snackBarHostState = it) },
    ) { padding ->
        // UserSourcesScreen(viewModel, navController)

        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            TabRow(
                selectedTabIndex = pagerState.currentPage,
                backgroundColor = AppColors.current.bars,
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
                            /**
                             * TODO need to wait for this issue to be close before using this line
                             * https://issuetracker.google.com/issues/229752147
                             */
                            //scope.launch { pagerState.animateScrollToPage(index) }
                        },
                        selectedContentColor = MaterialTheme.colors.primary,
                        unselectedContentColor = MaterialTheme.colors.onBackground,
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


