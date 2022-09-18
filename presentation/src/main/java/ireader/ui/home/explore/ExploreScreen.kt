package ireader.ui.home.explore

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.LocalContentColor
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ireader.common.models.entities.Book
import ireader.common.models.entities.BookItem
import ireader.common.models.entities.toBook
import ireader.common.models.entities.toBookItem
import ireader.i18n.asString
import ireader.core.source.HttpSource
import ireader.core.source.Source
import ireader.core.source.model.Filter
import ireader.core.source.model.Listing
import ireader.ui.core.theme.ContentAlpha
import ireader.ui.core.ui.kaomojis
import ireader.ui.component.components.ShowLoading
import ireader.ui.component.list.LayoutComposable
import ireader.ui.component.list.isScrolledToTheEnd
import ireader.ui.component.reusable_composable.AppIconButton
import ireader.ui.component.reusable_composable.MidSizeTextComposable
import ireader.ui.component.reusable_composable.SmallTextComposable
import ireader.ui.home.explore.viewmodel.ExploreViewModel
import kotlinx.coroutines.launch

@OptIn(
    ExperimentalFoundationApi::class, ExperimentalMaterialApi::class,
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
    modalState: ModalBottomSheetState,
    onLongClick: (Book) -> Unit = {},
    headers: ((url: String) -> okhttp3.Headers?)? = null,
    scaffoldPadding: PaddingValues
) {
    val scrollState = rememberLazyListState()
    val context = LocalContext.current

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
        if (errors != null && errors.asString(context).isNotBlank() && vm.page > 1) {
            setShowSnackBar(true)
            setSnackBarText(errors.asString(context))
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
    Scaffold(
        modifier = modifier,
        floatingActionButtonPosition = androidx.compose.material3.FabPosition.End,
        floatingActionButton = {
            androidx.compose.material3.ExtendedFloatingActionButton(
                text = {
                    MidSizeTextComposable(
                        text = stringResource(ireader.i18n.R.string.filter),
                        color = MaterialTheme.colorScheme.onSecondary
                    )
                },
                onClick = {
                    scope.launch {
                        modalState.show()
                    }
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
                        }
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
                color = LocalContentColor.current.copy(alpha = androidx.compose.material.ContentAlpha.medium)
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
                    contentDescription = stringResource(ireader.i18n.R.string.retry),
                    onClick = {
                        onRefresh()
                    }
                )
                SmallTextComposable(text = stringResource(ireader.i18n.R.string.retry))
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
                        contentDescription = stringResource(ireader.i18n.R.string.open_in_webView),
                        onClick = {
                            onWebView(source)
                        }
                    )
                }
                SmallTextComposable(text = stringResource(ireader.i18n.R.string.open_in_webView))
            }
        }
    }
}
