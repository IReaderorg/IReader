package org.ireader.presentation.ui

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TriStateCheckbox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import org.ireader.app.LibraryScreen
import org.ireader.app.LibraryScreenTopBar
import org.ireader.app.components.BottomTabComposable
import org.ireader.app.components.ScrollableTabs
import org.ireader.app.viewmodel.LibraryViewModel
import org.ireader.common_extensions.launchIO
import org.ireader.common_models.entities.toBookCategory
import org.ireader.common_resources.LAST_CHAPTER
import org.ireader.components.reusable_composable.MidSizeTextComposable
import org.ireader.domain.ui.NavigationArgs
import org.ireader.domain.ui.NavigationArgs.showModalSheet
import org.ireader.presentation.R

@OptIn(ExperimentalMaterial3Api::class)
@ExperimentalMaterialApi
object LibraryScreenSpec : BottomNavScreenSpec {
    override val icon: ImageVector = Icons.Filled.Book
    override val label: Int = R.string.library_screen_label
    override val navHostRoute: String = "library"

    override val arguments: List<NamedNavArgument> = listOf(
        NavigationArgs.showBottomNav,
        showModalSheet
    )

    @OptIn(ExperimentalPagerApi::class)
    @Composable
    override fun BottomModalSheet(
        navController: NavController,
        navBackStackEntry: NavBackStackEntry,
        snackBarHostState: SnackbarHostState,
        sheetState: ModalBottomSheetState,
        drawerState: DrawerState
    ) {
        val vm: LibraryViewModel = hiltViewModel(navBackStackEntry)

        val pagerState = rememberPagerState()
        BottomTabComposable(
            pagerState = pagerState,
            filters = vm.filters,
            toggleFilter = {
                vm.toggleFilter(it.type)
            },
            onSortSelected = {
                vm.toggleSort(it.type)
            },
            sortType = vm.sortType,
            isSortDesc = vm.desc,
            onLayoutSelected = { layout ->
                vm.onLayoutTypeChange(layout)
            },
            layoutType = vm.layout,
            vm = vm
        )
    }

    private const val route = "library"

    @OptIn(ExperimentalPagerApi::class)
    @Composable
    override fun TopBar(
        navController: NavController,
        navBackStackEntry: NavBackStackEntry,
        snackBarHostState: SnackbarHostState,
        sheetState: ModalBottomSheetState,
        drawerState: DrawerState
    ) {
        val vm: LibraryViewModel = hiltViewModel(navBackStackEntry)
        Column {
            LibraryScreenTopBar(
                state = vm,
                bottomSheetState = sheetState,
                onSearch = {
                    vm.getBooks()
                },
                refreshUpdate = {
                    vm.refreshUpdate()
                },
            )

        }
    }

    @OptIn(
        ExperimentalAnimationApi::class,
        ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class, ExperimentalPagerApi::class
    )
    @Composable
    override fun Content(
        navController: NavController,
        navBackStackEntry: NavBackStackEntry,
        snackBarHostState: SnackbarHostState,
        scaffoldPadding: PaddingValues,
        sheetState: ModalBottomSheetState,
        drawerState: DrawerState
    ) {
        val vm: LibraryViewModel = hiltViewModel(navBackStackEntry)
        val pager = rememberPagerState()
        LaunchedEffect(key1 = pager.hashCode()) {
            vm.pager = pager
        }
        LaunchedEffect(key1 = vm.filters, key2 = vm.sorting, key3 = pager.currentPage) {
            vm.getBooks()
//            vm.deleteQueues.clear()
//            vm.addQueues.clear()
        }
        Scaffold(
            modifier = Modifier.padding(scaffoldPadding),
            topBar = {
                if (vm.categories.isNotEmpty()) {
                    ScrollableTabs(
                        modifier = Modifier,
                        libraryTabs = vm.uiCategories.map { it.name },
                        pagerState = pager
                    )
                }
            }
        ) { paddingValues ->
            Box(modifier = Modifier.fillMaxSize()) {
                HorizontalPager(
                    modifier = Modifier.padding(paddingValues),
                    count = vm.uiCategories.size,
                    state = pager
                ) { page ->

                    LibraryScreen(
                        onMarkAsRead = {
                            vm.markAsRead()
                        },
                        onDownload = {
                            vm.downloadChapters()
                        },
                        onMarkAsNotRead = {
                            vm.markAsNotRead()
                        },
                        onDelete = {
                            vm.deleteBooks()
                        },
                        goToLatestChapter = { book ->
                            navController.navigate(
                                ReaderScreenSpec.buildRoute(
                                    bookId = book.id,
                                    sourceId = book.sourceId,
                                    chapterId = LAST_CHAPTER
                                )
                            )
                        },
                        onBook = { book ->
                            if (vm.hasSelection) {
                                if (book.id in vm.selection) {
                                    vm.selection.remove(book.id)
                                } else {
                                    vm.selection.add(book.id)
                                }
                            } else {
                                navController.navigate(
                                    route = BookDetailScreenSpec.buildRoute(
                                        sourceId = book.sourceId,
                                        bookId = book.id
                                    )
                                )
                            }
                        },
                        onLongBook = {
                            vm.selection.add(it.id)
                        },
                        vm = vm,
                        getLibraryBooks = {
                            vm.getBooks()
                        },
                        refreshUpdate = {
                            vm.refreshUpdate()
                        },
                        bottomSheetState = sheetState,
                        onClickChangeCategory = {
                            vm.showDialog = true
                        }
                    )
                }

                EditCategoriesDialog(vm, modifier = Modifier.align(Alignment.BottomCenter))
            }

        }
    }

    @Composable
    private fun EditCategoriesDialog(
        vm: LibraryViewModel,
        modifier: Modifier = Modifier
    ) {
        val showDialog by remember { derivedStateOf { vm.showDialog } }
        if (showDialog) {
            AlertDialog(
                modifier = modifier.heightIn(max = 350.dp, min = 200.dp),
                onDismissRequest = { vm.showDialog = false },
                title = { Text(stringResource(id = R.string.edit_category)) },
                text = {
                    LazyColumn {
                        items(vm.categories) { category ->
                            Row(
                                modifier = Modifier
                                    .requiredHeight(48.dp)
                                    .fillMaxWidth()
                                    .clickable(onClick = {
                                        vm.showDialog = false
                                    }),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val defaultValue by remember {
                                    derivedStateOf { vm.getDefaultValue(category) }
                                }
                                var state: ToggleableState by remember {
                                    mutableStateOf(defaultValue)
                                }
                                TriStateCheckbox(state = state, onClick = {
                                    state = when (defaultValue) {
                                        state -> {
                                            when (state) {
                                                ToggleableState.On -> {
                                                    vm.deleteQueues.addAll(category.toBookCategory(vm.selection))
                                                    ToggleableState.Indeterminate
                                                }
                                                ToggleableState.Indeterminate -> {
                                                  //  vm.addQueues.addAll(category.toBookCategory(vm.selection))
                                                    ToggleableState.On
                                                }
                                                ToggleableState.Off -> {
                                                    vm.addQueues.addAll(category.toBookCategory(vm.selection))
                                                    ToggleableState.On
                                                }
                                            }
                                        }
                                        else -> {
                                            when(state) {
                                                ToggleableState.On -> {
                                                    vm.addQueues.removeIf { it.categoryId == category.id }
                                                }
                                                ToggleableState.Indeterminate -> {
                                                    vm.deleteQueues.removeIf { it.categoryId == category.id }
                                                }
                                                else -> {}
                                            }
                                            defaultValue
                                        }
                                    }
                                })
                                Text(
                                    text = category.name,
                                    modifier = Modifier.padding(start = 24.dp)
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        vm.viewModelScope.launchIO {
                            vm.getCategory.insertBookCategory(vm.addQueues)
                            vm.getCategory.deleteBookCategory(vm.deleteQueues)
                            vm.deleteQueues.clear()
                            vm.addQueues.clear()
                        }
                        vm.showDialog = false

                    }) {
                        MidSizeTextComposable(text = stringResource(id = R.string.add))
                    }
                }
            )
        }
    }
}


