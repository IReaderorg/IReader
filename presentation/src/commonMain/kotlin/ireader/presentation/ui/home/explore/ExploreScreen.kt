package ireader.presentation.ui.home.explore

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import ireader.i18n.resources.Res
import ireader.i18n.resources.*
import ireader.presentation.ui.component.ModernLayoutComposable
import ireader.presentation.ui.component.components.BookShimmerLoading
import ireader.presentation.ui.component.isLandscape
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

/**
 * Stable key generator for explore book items
 */
@Stable
private fun stableExploreBookKey(book: BookItem): Any = "${book.id}_${book.column}"

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
    prevPaddingValues: PaddingValues
) {
    val scrollState = rememberLazyListState(
        initialFirstVisibleItemIndex = vm.savedScrollIndex,
        initialFirstVisibleItemScrollOffset = vm.savedScrollOffset
    )
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }

    val gridState = rememberLazyGridState(
        initialFirstVisibleItemIndex = vm.savedScrollIndex,
        initialFirstVisibleItemScrollOffset = vm.savedScrollOffset
    )

    val scope = rememberCoroutineScope()
    
    // Pre-compute stable key function for book items
    val stableKeyFunction = remember { { book: BookItem -> stableExploreBookKey(book) } }
    
    // Memoize click handlers to prevent unnecessary recompositions
    val stableOnBook = remember(onBook) { onBook }
    val stableOnLongClick = remember(onLongClick) { onLongClick }
    
    // Derive screen state for efficient rendering
    val screenState by remember {
        derivedStateOf {
            when {
                vm.isLoading && vm.page == 1 -> ExploreScreenState.Loading
                vm.error != null && vm.page == 1 -> ExploreScreenState.Error
                vm.booksState.books.isEmpty() && !vm.isLoading -> ExploreScreenState.Empty
                else -> ExploreScreenState.Content
            }
        }
    }
    
    // Memoize books with stable keys
    val booksWithKeys by remember(vm.booksState.books) {
        derivedStateOf {
            vm.booksState.books.mapIndexed { index, book -> 
                book.toBookItem().copy(column = index.toLong())
            }
        }
    }

    // Save scroll position when it changes - debounced
    LaunchedEffect(scrollState.firstVisibleItemIndex, scrollState.firstVisibleItemScrollOffset) {
        vm.savedScrollIndex = scrollState.firstVisibleItemIndex
        vm.savedScrollOffset = scrollState.firstVisibleItemScrollOffset
    }
    
    LaunchedEffect(gridState.firstVisibleItemIndex, gridState.firstVisibleItemScrollOffset) {
        vm.savedScrollIndex = gridState.firstVisibleItemIndex
        vm.savedScrollOffset = gridState.firstVisibleItemScrollOffset
    }

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
            remember(isLandscape, vm.layout) {
                getColumnsForOrientation(isLandscape)
            }.collectAsState(
                initial = 2
            )
        }
    } else {
        remember { mutableStateOf(0) }
    }
    Scaffold(
        modifier = modifier,
        floatingActionButtonPosition = androidx.compose.material3.FabPosition.End,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = {
                    Text(
                        text = localize(Res.string.filter),
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                },
                onClick = {
                    showmodalSheet()
                },
                icon = {
                    Icon(Icons.Filled.Add, "", tint = MaterialTheme.colorScheme.onSecondaryContainer)
                },
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                shape = CircleShape
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackBarHostState)
        }
    ) { paddingValue ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(prevPaddingValues)
        ) {
            when {
                vm.isLoading && vm.page == 1 -> {
                    BookShimmerLoading(columns = columns)
                }
                vm.error != null && vm.page == 1 -> {
                    ExploreScreenError(
                        error = vm.error!!.asString(localizeHelper),
                        source = source,
                        onRefresh = { getBooks(null, null, emptyList()) },
                        onWebView = {
                            onAppbarWebView(it.baseUrl)
                        }
                    )
                }
                vm.booksState.books.isEmpty() && !vm.isLoading -> {
                    // Show error screen when no books loaded (could be due to error or truly empty)
                    ExploreScreenError(
                        error = vm.error?.asString(localizeHelper) ?: localize(Res.string.no_results_found),
                        source = source,
                        onRefresh = { getBooks(null, null, emptyList()) },
                        onWebView = {
                            onAppbarWebView(it.baseUrl)
                        }
                    )
                }
                else -> {
                    ModernLayoutComposable(
                        books = booksWithKeys,
                        layout = vm.layout,
                        scrollState = scrollState,
                        gridState = gridState,
                        onClick = stableOnBook,
                        isLoading = vm.isLoading,
                        showInLibraryBadge = true,
                        onLongClick = { book ->
                            stableOnLongClick(book.toBook())
                        },
                        headers = headers,
                        keys = stableKeyFunction,
                        columns = columns
                    )
                }
            }
        }
    }
}

/**
 * Enum representing the different states of the explore screen
 * Used with derivedStateOf for efficient recomposition
 */
private enum class ExploreScreenState {
    Loading,
    Error,
    Empty,
    Content
}
