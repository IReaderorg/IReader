package org.ireader.presentation.feature_explore.presentation.browse

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import kotlinx.coroutines.launch
import org.ireader.domain.FetchType
import org.ireader.domain.models.DisplayMode
import org.ireader.domain.models.LayoutType
import org.ireader.presentation.feature_explore.presentation.browse.viewmodel.ExploreViewModel
import org.ireader.presentation.feature_library.presentation.components.LayoutComposable
import org.ireader.presentation.presentation.components.handlePagingResult
import org.ireader.presentation.presentation.reusable_composable.AppIconButton
import org.ireader.presentation.presentation.reusable_composable.ErrorTextWithEmojis
import org.ireader.presentation.presentation.reusable_composable.MidSizeTextComposable
import org.ireader.presentation.presentation.reusable_composable.SmallTextComposable
import org.ireader.presentation.ui.BookDetailScreenSpec
import org.ireader.presentation.ui.WebViewScreenSpec
import tachiyomi.source.CatalogSource
import tachiyomi.source.HttpSource
import tachiyomi.source.model.Filter
import tachiyomi.source.model.Listing


@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterialApi::class)
@ExperimentalPagingApi
@Composable
fun ExploreScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    vm: ExploreViewModel,
    source: CatalogSource,
    onFilterClick: () -> Unit,
    onValueChange: (String) -> Unit,
    onSearch: () -> Unit,
    onSearchDisable: () -> Unit,
    onSearchEnable: () -> Unit,
    onWebView: () -> Unit,
    onPop: () -> Unit,
    onLayoutTypeSelect: (DisplayMode) -> Unit,
    currentLayout: LayoutType,
    getBooks: (query: String?, listing: Listing?, filters: List<Filter<*>>) -> Unit,
) {
    val scrollState = rememberLazyListState()
    val context = LocalContext.current

    val books = vm.books.collectAsLazyPagingItems()

    val gridState = rememberLazyGridState()
    val bottomSheetState =
        rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)
    val scope = rememberCoroutineScope()

    LaunchedEffect(key1 = true) {
        vm.modifiedFilter = source.getFilters()
    }

    val scaffoldState = rememberScaffoldState()
    val (showSnackBar, setShowSnackBar) = remember {
        mutableStateOf(false)
    }
    val (snackBarText, setSnackBarText) = remember {
        mutableStateOf("")
    }
    if (showSnackBar) {
        LaunchedEffect(scaffoldState.snackbarHostState) {
            val result = scaffoldState.snackbarHostState.showSnackbar(
                snackBarText,
                actionLabel = "Reload",
                duration = SnackbarDuration.Indefinite
            )
            when (result) {
                SnackbarResult.ActionPerformed -> {
                    setShowSnackBar(false)
                    books.retry()
                }
            }
        }
    }
    val error = when {
        books.loadState.refresh is LoadState.Error -> books.loadState.refresh as LoadState.Error
        books.loadState.prepend is LoadState.Error -> books.loadState.prepend as LoadState.Error
        books.loadState.append is LoadState.Error -> books.loadState.append as LoadState.Error
        else -> null
    }
    LaunchedEffect(key1 = error?.error != null) {
        val error = error?.error?.localizedMessage
        if (error != null && error.isNotBlank() && books.itemCount > 0) {
            setShowSnackBar(true)
            setSnackBarText(error)

        }

    }
    ModalBottomSheetLayout(
        modifier = Modifier.statusBarsPadding(),
        sheetState = bottomSheetState,
        sheetContent = {
            FilterBottomSheet(
                onApply = {
                    val mFilters = vm.modifiedFilter.filterNot { it.isDefaultValue() }
                    vm.getBooks(filters = mFilters, source = source)
                },
                filters = vm.modifiedFilter,
                onReset = {
                    vm.modifiedFilter = source.getFilters()
                },
                onUpdate = {
                    vm.modifiedFilter = it
                }
            )
        },
        sheetBackgroundColor = MaterialTheme.colors.background,

        ) {
        Scaffold(
            topBar = {
                BrowseTopAppBar(
                    state = vm,
                    source = source,
                    onValueChange = onValueChange,
                    onSearch = onSearch,
                    onSearchDisable = onSearchDisable,
                    onSearchEnable = onSearchEnable,
                    onWebView = onWebView,
                    onPop = onPop,
                    onLayoutTypeSelect = onLayoutTypeSelect,
                    currentLayout = currentLayout
                )
            },
            scaffoldState = scaffoldState,
            snackbarHost = {
                SnackbarHost(hostState = it) { data ->
                    Snackbar(
                        actionColor = MaterialTheme.colors.primary,
                        snackbarData = data,
                        backgroundColor = MaterialTheme.colors.background,
                        contentColor = MaterialTheme.colors.onBackground,
                    )
                }
            },
            floatingActionButtonPosition = FabPosition.End,
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    text = {
                        MidSizeTextComposable(
                            text = stringResource(org.ireader.presentation.R.string.filter),
                            color = Color.White
                        )
                    },
                    onClick = {
                        scope.launch {
                            bottomSheetState.show()
                        }
                    },
                    icon = {
                        Icon(Icons.Filled.Add, "", tint = MaterialTheme.colors.onSecondary)
                    }
                )
            },
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                val result = handlePagingResult(books = books,
                    onEmptyResult = {},
                    onErrorResult = { error ->
                        Column(
                            modifier = modifier
                                .fillMaxSize()
                                .align(Alignment.Center)
                                .padding(bottom = 30.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            ErrorTextWithEmojis(
                                error = error,
                                modifier = Modifier
                                    .padding(20.dp)
                            )
                            Row(Modifier
                                .fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(Modifier
                                    .weight(.5f)
                                    .wrapContentSize(Alignment.Center),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    AppIconButton(imageVector = Icons.Default.Refresh,
                                        title = "Retry",
                                        onClick = { getBooks(null, null, emptyList()) })
                                    SmallTextComposable(text = "Retry")
                                }
                                Column(Modifier
                                    .weight(.5f)
                                    .wrapContentSize(Alignment.Center),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    if (source is HttpSource) {
                                        AppIconButton(imageVector = Icons.Default.Public,
                                            title = "Open in WebView",
                                            onClick = {
                                                navController.navigate(WebViewScreenSpec.buildRoute(
                                                    sourceId = source.id,
                                                    fetchType = FetchType.LatestFetchType.index,
                                                    url = source.baseUrl
                                                )
                                                )
                                            })
                                    }
                                    SmallTextComposable(text = "Open in WebView")
                                }

                            }

                        }

                    })
                if (result) {
                    LayoutComposable(
                        lazyBook = books,
                        layout = vm.layout,
                        scrollState = scrollState,
                        source = source,
                        navController = navController,
                        isLocal = false,
                        gridState = gridState,
                        onClick = { book ->
                            navController.navigate(
                                route = BookDetailScreenSpec.buildRoute(sourceId = book.sourceId,
                                    bookId = book.id)
                            )
                        },
                        isLoading = books.loadState.refresh is LoadState.Loading,
                        error = error?.error?.message ?: "Unknown Error Happened"
                    )
                }
            }


        }
    }

}

