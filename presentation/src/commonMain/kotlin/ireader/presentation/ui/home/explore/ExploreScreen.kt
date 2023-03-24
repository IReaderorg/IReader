package ireader.presentation.ui.home.explore

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.navigator.currentOrThrow
import ireader.core.source.HttpSource
import ireader.core.source.Source
import ireader.core.source.model.Filter
import ireader.core.source.model.Listing
import ireader.domain.models.DisplayMode
import ireader.domain.models.entities.Book
import ireader.domain.models.entities.BookItem
import ireader.domain.models.entities.toBook
import ireader.domain.models.entities.toBookItem
import ireader.i18n.asString
import ireader.i18n.localize
import ireader.i18n.resources.MR
import ireader.presentation.ui.component.components.ShowLoading
import ireader.presentation.ui.component.isLandscape
import ireader.presentation.ui.component.list.LayoutComposable
import ireader.presentation.ui.component.list.isScrolledToTheEnd
import ireader.presentation.ui.component.reusable_composable.AppIconButton
import ireader.presentation.ui.component.reusable_composable.MidSizeTextComposable
import ireader.presentation.ui.component.reusable_composable.SmallTextComposable
import ireader.presentation.ui.core.theme.ContentAlpha
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.presentation.ui.core.ui.kaomojis
import ireader.presentation.ui.home.explore.viewmodel.ExploreViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

@OptIn(
    ExperimentalMaterial3Api::class
)
@Composable
fun ExploreScreen(
    modifier: Modifier = Modifier,
    vm: ExploreViewModel,
    source: ireader.core.source.CatalogSource,
    onFilterClick: () -> Unit,
    getBooks: (query: String?, listing: Listing?, filters: List<Filter<*>>) -> Unit,
    loadItems: (Boolean) -> Unit,
    onBook: (BookItem) -> Unit,
    onAppbarWebView: (url: String) -> Unit,
    onPopBackStack: () -> Unit,
    snackBarHostState: SnackbarHostState,
    showmodalSheet: () -> Unit,
    onLongClick: (Book) -> Unit = {},
    headers: ((url: String) -> okhttp3.Headers?)? = null,
    getColumnsForOrientation: CoroutineScope.(Boolean) -> StateFlow<Int>,
) {
    val scrollState = rememberLazyListState()
    val localizeHelper = LocalLocalizeHelper.currentOrThrow

    val gridState = rememberLazyGridState()

    val scope = rememberCoroutineScope()

    LaunchedEffect(key1 = true) {
        vm.modifiedFilter = source.getFilters()
    }

    val (showSnackBar, setShowSnackBar) = remember {
        mutableStateOf(false)
    }
    val (snackBarText, setSnackBarText) = remember {
        mutableStateOf("")
    }

    if (showSnackBar) {
        LaunchedEffect(snackBarHostState.currentSnackbarData) {
            val result = snackBarHostState.showSnackbar(
                message = snackBarText,
                actionLabel = "Reload",
                duration = androidx.compose.material3.SnackbarDuration.Indefinite
            )
            when (result) {
                androidx.compose.material3.SnackbarResult.ActionPerformed -> {
                    setShowSnackBar(false)
                    vm.endReached = false
                    loadItems(false)
                }
                else -> {}
            }
        }
    }
    LaunchedEffect(key1 = vm.error != null) {
        val errors = vm.error
        if (errors != null && errors.asString(localizeHelper).isNotBlank() && vm.page > 1) {
            setShowSnackBar(true)
            setSnackBarText(errors.asString(localizeHelper))
        }
    }
    LaunchedEffect(key1 = scrollState.layoutInfo.totalItemsCount > 0, key2 = scrollState.isScrolledToTheEnd(), key3 = !vm.endReached && !vm.isLoading) {
        if (scrollState.layoutInfo.totalItemsCount > 0 && scrollState.isScrolledToTheEnd() && !vm.endReached && !vm.isLoading) {
            loadItems(false)
        }
    }
    LaunchedEffect(key1 = gridState.layoutInfo.totalItemsCount > 0, key2 = gridState.isScrolledToTheEnd(), key3 = !vm.endReached && !vm.isLoading) {
        if (gridState.layoutInfo.totalItemsCount > 0 && gridState.isScrolledToTheEnd() && !vm.endReached && !vm.isLoading) {
            loadItems(false)
        }
    }
    val columns by if (vm.layout != DisplayMode.List) {

        val isLandscape = isLandscape()

        with(rememberCoroutineScope()) {
            remember(isLandscape) { getColumnsForOrientation(isLandscape) }.collectAsState()
        }
    } else {
        remember { mutableStateOf(0) }
    }
    Scaffold(
        modifier = modifier,
        floatingActionButtonPosition = androidx.compose.material3.FabPosition.End,
        floatingActionButton = {
            androidx.compose.material3.ExtendedFloatingActionButton(
                text = {
                    MidSizeTextComposable(
                        text = localize(MR.strings.filter),
                        color = MaterialTheme.colorScheme.onSecondary
                    )
                },
                onClick = {
                    showmodalSheet()
                },
                icon = {
                    Icon(Icons.Filled.Add, "", tint = MaterialTheme.colorScheme.onSecondary)
                },
                contentColor = MaterialTheme.colorScheme.onSecondary,
                containerColor = MaterialTheme.colorScheme.secondary,
                shape = RoundedCornerShape(32.dp)
            )
        },
    ) { paddingValue ->
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {

            when {
                vm.isLoading && vm.page == 1 -> {
                    ShowLoading()
                }
                vm.error != null && vm.page == 1 -> {
                    ExploreScreenErrorComposable(
                        error = vm.error!!.asString(localizeHelper),
                        source = source,
                        onRefresh = { getBooks(null, null, emptyList()) },
                        onWebView = {
                            onAppbarWebView(it.baseUrl)
                        }
                    )
                }
                else -> {
                    LayoutComposable(
                        books = vm.booksState.books
                            .mapIndexed { index, book ->  book.toBookItem()
                                .copy(column= index.toLong())},
                        layout = vm.layout,
                        scrollState = scrollState,
                        source = source,
                        isLocal = false,
                        gridState = gridState,
                        onClick = { book ->
                            onBook(book)
                        },
                        isLoading = vm.isLoading,
                        showInLibraryBadge = true,
                        onLongClick = {
                            onLongClick(it.toBook())
                        },
                        headers = headers,
                        keys = {
                            it.column
                        },
                        columns = columns
                    )
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

        androidx.compose.material3.Text(
            text = kaomoji,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = LocalContentColor.current.copy(alpha = ContentAlpha.medium()),
                fontSize = 48.sp
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
        androidx.compose.material3.Text(
            text = error,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = LocalContentColor.current.copy(alpha = ContentAlpha.medium())
            ),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(top = 16.dp)
                .padding(horizontal = 16.dp),
            color = MaterialTheme.colorScheme.onSurface
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
                    contentDescription = localize(MR.strings.retry),
                    onClick = {
                        onRefresh()
                    }
                )
                SmallTextComposable(text = localize(MR.strings.retry))
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
                        contentDescription = localize(MR.strings.open_in_webView),
                        onClick = {
                            onWebView(source)
                        }
                    )
                }
                SmallTextComposable(text = localize(MR.strings.open_in_webView))
            }
        }
    }
}
