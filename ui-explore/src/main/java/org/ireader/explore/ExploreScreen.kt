package org.ireader.explore

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ContentAlpha
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ExtendedFloatingActionButton
import androidx.compose.material.FabPosition
import androidx.compose.material.Icon
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.Scaffold
import androidx.compose.material.Snackbar
import androidx.compose.material.SnackbarDuration
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarResult
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.ireader.common_models.DisplayMode
import org.ireader.common_models.LayoutType
import org.ireader.common_models.entities.BookItem
import org.ireader.components.components.ShowLoading
import org.ireader.components.list.LayoutComposable
import org.ireader.components.reusable_composable.AppIconButton
import org.ireader.components.reusable_composable.MidSizeTextComposable
import org.ireader.components.reusable_composable.SmallTextComposable
import org.ireader.core_api.source.CatalogSource
import org.ireader.core_api.source.HttpSource
import org.ireader.core_api.source.Source
import org.ireader.core_api.source.model.Filter
import org.ireader.core_api.source.model.Listing
import org.ireader.core_ui.ui.kaomojis
import org.ireader.explore.viewmodel.ExploreState
import org.ireader.ui_explore.R

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterialApi::class)
@Composable
fun ExploreScreen(
    modifier: Modifier = Modifier,
    vm: ExploreState,
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
    loadItems: (Boolean) -> Unit,
    onBook: (BookItem) -> Unit,
    onAppbarWebView: (url: String) -> Unit,
    onPopBackStack: () -> Unit,
) {
    val scrollState = rememberLazyListState()
    val context = LocalContext.current

    // val books = vm.books.collectAsLazyPagingItems()

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
                    vm.endReached = false
                    loadItems(false)
                    // books.retry()
                }
            }
        }
    }
    LaunchedEffect(key1 = vm.error != null) {
        val errors = vm.error
        if (errors != null && errors.asString(context).isNotBlank() && vm.page > 1) {
            setShowSnackBar(true)
            setSnackBarText(errors.asString(context))
        }
    }
    ModalBottomSheetLayout(
        modifier = Modifier,
        sheetState = bottomSheetState,
        sheetContent = {
            FilterBottomSheet(
                onApply = {
                    val mFilters = vm.modifiedFilter.filterNot { it.isDefaultValue() }
                    vm.stateFilters = mFilters
                    vm.searchQuery = null
                    loadItems(true)
                    // vm.getBooks(filters = mFilters, source = source)
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
                            text = stringResource(R.string.filter),
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
        ) { paddingValue ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValue)
            ) {
                when {
                    vm.isLoading && vm.page == 1 -> {
                        ShowLoading()
                    }
                    vm.error != null && vm.page == 1 -> {
                        ExploreScreenErrorComposable(
                            error = vm.error!!.asString(context),
                            source = source,
                            onRefresh = { getBooks(null, null, emptyList()) },
                            onWebView = {
                                onAppbarWebView(it.baseUrl)
                            }
                        )
                    }
                    else -> {
                        LayoutComposable(
                            books = vm.stateItems,
                            layout = vm.layout,
                            scrollState = scrollState,
                            source = source,
                            isLocal = false,
                            gridState = gridState,
                            onClick = { book ->
                                onBook(book)
                            },
                            isLoading = vm.isLoading,
                            onEndReachValidator = { index ->
                                if (index >= vm.stateItems.lastIndex && !vm.endReached && !vm.isLoading) {
                                    loadItems(false)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BoxScope.ExploreScreenErrorComposable(
    modifier: Modifier = Modifier,
    error: String,
    onRefresh: () -> Unit,
    source: Source,
    onWebView: (HttpSource) -> Unit,
) {
    val kaomoji = remember { kaomojis.random() }
    Column(
        modifier = modifier
            .fillMaxSize()
            .align(Alignment.Center)
            .padding(bottom = 30.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        Text(
            text = kaomoji,
            style = MaterialTheme.typography.body2.copy(
                color = LocalContentColor.current.copy(alpha = ContentAlpha.medium),
                fontSize = 48.sp
            ),
        )
        Text(
            text = error,
            style = MaterialTheme.typography.body2.copy(
                color = LocalContentColor.current.copy(alpha = ContentAlpha.medium)
            ),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(top = 16.dp)
                .padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(32.dp))

        Row(
            Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                Modifier
                    .weight(.5f)
                    .wrapContentSize(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AppIconButton(
                    imageVector = Icons.Default.Refresh,
                    title = "Retry",
                    onClick = {
                        onRefresh()
                    }
                )
                SmallTextComposable(text = "Retry")
            }
            Column(
                Modifier
                    .weight(.5f)
                    .wrapContentSize(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (source is HttpSource) {
                    AppIconButton(
                        imageVector = Icons.Default.Public,
                        title = "Open in WebView",
                        onClick = {
                            onWebView(source)
                        }
                    )
                }
                SmallTextComposable(text = "Open in WebView")
            }
        }
    }
}
