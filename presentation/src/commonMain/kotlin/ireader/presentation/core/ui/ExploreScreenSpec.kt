package ireader.presentation.core.ui

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import ireader.core.source.HttpSource
import ireader.domain.models.entities.toBookItem
import ireader.domain.utils.extensions.launchIO
import ireader.i18n.UiText
import ireader.i18n.resources.MR
import ireader.presentation.core.IModalSheets
import ireader.presentation.core.VoyagerScreen
import ireader.presentation.imageloader.convertToOkHttpRequest
import ireader.presentation.ui.component.IScaffold
import ireader.presentation.ui.component.components.EmptyScreenComposable
import ireader.presentation.ui.component.hideKeyboard
import ireader.presentation.ui.core.ui.SnackBarListener
import ireader.presentation.ui.home.explore.BrowseTopAppBar
import ireader.presentation.ui.home.explore.ExploreScreen
import ireader.presentation.ui.home.explore.FilterBottomSheet
import ireader.presentation.ui.home.explore.viewmodel.ExploreViewModel
import kotlinx.coroutines.launch
import okhttp3.Headers
import org.koin.core.parameter.parametersOf


@OptIn(
        ExperimentalAnimationApi::class,
        ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class
)
data class ExploreScreenSpec(
        val sourceId: Long,
        val query: String?
) : VoyagerScreen() {
    @OptIn(
            ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class,
            ExperimentalComposeUiApi::class
    )
    @Composable
    override fun Content() {
        val vm: ExploreViewModel =
                getIViewModel(parameters = { parametersOf(ExploreViewModel.Param(sourceId, query)) }

                )
        val navigator = LocalNavigator.currentOrThrow
        val source = vm.source
        val scope = rememberCoroutineScope()
        val headers = remember {
            mutableStateOf<Headers?>(null)
        }
        val sheetState = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden)
        val snackBarHostState = SnackBarListener(vm)
        IModalSheets(
                sheetContent = {
                    val source = vm.source
                    val scope = rememberCoroutineScope()
                    val focusManager = LocalFocusManager.current
                    val keyboardController = LocalSoftwareKeyboardController.current
                    FilterBottomSheet(
                        modifier = it,
                        onApply = {
                            val mFilters = vm.modifiedFilter.filterNot { it.isDefaultValue() }
                            vm.stateFilters = mFilters
                            vm.searchQuery = null
                            vm.loadItems(reset = true)
                            hideKeyboard(
                                softwareKeyboardController = keyboardController,
                                focusManager
                            )
                        },
                        filters = vm.modifiedFilter,
                        onReset = {
                            vm.modifiedFilter = source?.getFilters() ?: emptyList()
                            },
                            onUpdate = {
                                vm.modifiedFilter = it
                            }
                    )
                }, bottomSheetState = sheetState
        ) {
            IScaffold(
                    topBar = { scrollBehavior ->
                        val focusManager = LocalFocusManager.current
                        val source = vm.source
                        val scope = rememberCoroutineScope()
                        BrowseTopAppBar(
                                scrollBehavior = scrollBehavior,
                                state = vm,
                                source = source,
                                onValueChange = {
                                    vm.searchQuery = it
                                },
                                onSearch = {
                                    val query = vm.searchQuery
                                    if (query != null && query.isNotBlank()) {
                                        vm.searchQuery = query
                                        vm.loadItems(true)
                                    } else {
                                        vm.stateListing = source?.getListings()?.first()
                                        vm.loadItems()
                                        scope.launch {
                                            vm.showSnackBar(UiText.MStringResource(MR.strings.query_must_not_be_empty))
                                        }
                                    }
                                    focusManager.clearFocus()
                                },
                                onSearchDisable = {
                                    vm.toggleSearchMode(false)
                                    vm.searchQuery = null
                                    vm.loadItems(true)
                                },
                                onSearchEnable = {
                                    vm.toggleSearchMode(true)
                                },
                                onWebView = {
                                    if (source is HttpSource) {
                                        navigator.push(
                                                WebViewScreenSpec(
                                                        url = (source).baseUrl,
                                                        sourceId = source.id,
                                                        chapterId = null,
                                                        bookId = null,
                                                        enableChaptersFetch = true,
                                                        enableBookFetch = true,
                                                        enableChapterFetch = false
                                                )
                                        )
                                    }
                                },
                                onPop = { popBackStack(navigator) },
                                onLayoutTypeSelect = { layout ->
                                    vm.saveLayoutType(layout)
                                },
                                currentLayout = vm.layout
                        )
                    }
            ) { scaffoldPadding ->
                if (source != null) {
                    ExploreScreen(
                            vm = vm,
                            onFilterClick = {
                                vm.toggleFilterMode()
                            },
                        prevPaddingValues = scaffoldPadding,
                            source = source,
                            getBooks = { query, listing, filters ->
                                vm.searchQuery = query
                                vm.stateListing = listing
                                vm.stateFilters = filters
                                vm.loadItems()
                            },
                            loadItems = { reset ->
                                vm.loadItems(reset)
                            },
                            onBook = { book ->

                                vm.scope.launch {
                                    val newBook = vm.booksState.books.getOrNull(book.column.toInt())
                                    vm.booksState.book = null
                                    val bookId = vm.insertUseCases.insertBook(newBook)

                                    if (bookId != 0L) {
                                        vm.booksState.replaceBook(newBook?.copy(id = bookId))
                                        navigator.push(BookDetailScreenSpec(bookId = bookId))
                                    }

                                }

                            },
                            onAppbarWebView = {
                                navigator.push(
                                        WebViewScreenSpec(
                                                url = it,
                                                sourceId = source.id,
                                                enableBookFetch = true,
                                                enableChaptersFetch = true,
                                                bookId = null,
                                                chapterId = null,
                                                enableChapterFetch = false
                                        )
                                )
                            },
                            onPopBackStack = {
                                popBackStack(navigator)
                            },
                            snackBarHostState = snackBarHostState,
                            showmodalSheet = {
                                scope.launchIO {
                                    sheetState.show()
                                }
                            },
                                                        headers = {
                                if (headers.value == null) {
                                    headers.value =
                                            (source as? HttpSource)?.getCoverRequest(it)?.second?.build()
                                                    ?.convertToOkHttpRequest()?.headers
                                    headers.value
                                } else headers.value
                            },
                            onLongClick = {
                                vm.scope.launchIO {
                                    vm.addToFavorite(it.toBookItem()) { book ->
                                        vm.booksState.replaceBook(book)
                                    }
                                }
                            },
                        getColumnsForOrientation = { isLandscape ->
                            vm.getColumnsForOrientation(isLandscape, this)
                        }
                    )
                } else {
                    EmptyScreenComposable(
                            UiText.MStringResource(MR.strings.source_not_available),
                            onPopBackStack = {
                                popBackStack(navigator)
                            }
                    )
                }

            }
        }

    }
}
