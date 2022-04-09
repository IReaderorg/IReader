package org.ireader.presentation.feature_library.presentation


import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.DoneOutline
import androidx.compose.material.icons.filled.GetApp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.rememberPagerState
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.SwipeRefreshIndicator
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import org.ireader.core.utils.Constants
import org.ireader.core.utils.UiText
import org.ireader.core_ui.ui.EmptyScreen
import org.ireader.core_ui.ui.LoadingScreen
import org.ireader.presentation.feature_library.presentation.components.BottomTabComposable
import org.ireader.presentation.feature_library.presentation.components.LayoutComposable
import org.ireader.presentation.feature_library.presentation.viewmodel.LibraryViewModel
import org.ireader.presentation.presentation.reusable_composable.AppIconButton
import org.ireader.presentation.ui.BookDetailScreenSpec
import org.ireader.presentation.ui.ReaderScreenSpec


@ExperimentalPagerApi
@ExperimentalAnimationApi
@OptIn(ExperimentalMaterialApi::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun LibraryScreen(
    modifier: Modifier = Modifier,
    navController: NavController = rememberNavController(),
    vm: LibraryViewModel = hiltViewModel(),
) {


    val coroutineScope = rememberCoroutineScope()


    val pagerState = rememberPagerState()
    val bottomSheetState =
        rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)

    val gridState = rememberLazyGridState()
    val lazyListState = rememberLazyListState()
    val context = LocalContext.current
    val swipeState = rememberSwipeRefreshState(isRefreshing = false)
    LaunchedEffect(key1 = true) {
        vm.getLibraryBooks()
    }

    SwipeRefresh(
        state = swipeState, onRefresh = { vm.refreshUpdate(context) },
        indicator = { state, trigger ->
            SwipeRefreshIndicator(
                state = state,
                refreshTriggerDistance = trigger,
                scale = true,
                backgroundColor = MaterialTheme.colors.background,
                contentColor = MaterialTheme.colors.primaryVariant,
                elevation = 8.dp,
            )
        }
    ) {
        ModalBottomSheetLayout(
            modifier = Modifier.systemBarsPadding(),
            sheetContent = {
                Box(modifier.defaultMinSize(minHeight = 1.dp)) {
                    BottomTabComposable(
                        viewModel = vm,
                        pagerState = pagerState,
                        navController = navController,
                        scope = coroutineScope)

                }
            },
            sheetState = bottomSheetState,
            sheetBackgroundColor = MaterialTheme.colors.background,
            sheetContentColor = MaterialTheme.colors.onBackground,
        ) {
            Column(modifier = Modifier
                .fillMaxSize()
            ) {
                LibraryScreenTopBar(
                    navController = navController,
                    state = vm,
                    coroutineScope = coroutineScope,
                    bottomSheetState = bottomSheetState)
                Box(modifier = Modifier
                    .fillMaxSize()) {
                    Crossfade(targetState = Pair(vm.isLoading,
                        vm.isEmpty)) { (isLoading, isEmpty) ->
                        when {
                            isLoading -> LoadingScreen()
                            isEmpty && vm.filters.isEmpty() -> EmptyScreen(UiText.DynamicString(
                                "There is no book is Library, you can add books in the Explore screen."))
                            else -> LayoutComposable(
                                books = vm.books,
                                layout = vm.layout,
                                navController = navController,
                                isLocal = true,
                                gridState = gridState,
                                scrollState = lazyListState,
                                selection = vm.selection,
                                goToLatestChapter = { book ->
                                    navController.navigate(
                                        ReaderScreenSpec.buildRoute(
                                            bookId = book.id,
                                            sourceId = book.sourceId,
                                            chapterId = Constants.LAST_CHAPTER
                                        )
                                    )
                                },
                                onClick = { book ->
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
                                                bookId = book.id)
                                        )
                                    }

                                },
                                onLongClick = {
                                    vm.selection.add(it.id)
                                },
                            )
                        }
                    }
                    when {
                        vm.hasSelection -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(80.dp)
                                    .align(Alignment.BottomCenter)
                                    .padding(8.dp)
                                    .background(MaterialTheme.colors.background)
                                    .border(width = 1.dp,
                                        color = MaterialTheme.colors.onBackground.copy(.1f))
                                    .clickable(enabled = false) {},
                            ) {
                                Row(modifier = Modifier
                                    .fillMaxSize(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    AppIconButton(imageVector = Icons.Default.GetApp,
                                        title = "Download",
                                        onClick = {
                                            vm.downloadChapters(context)
                                            // vm.selection.clear()
                                        })
                                    AppIconButton(imageVector = Icons.Default.Done,
                                        title = "Mark as read",
                                        onClick = {
                                            vm.markAsRead()
                                            // vm.selection.clear()
                                        })
                                    AppIconButton(imageVector = Icons.Default.DoneOutline,
                                        title = "Mark as Not read",
                                        onClick = {
                                            vm.markAsNotRead()
                                        })
                                    AppIconButton(imageVector = Icons.Default.Delete,
                                        title = "Mark Previous as read",
                                        onClick = {
                                            vm.deleteBooks()
                                        })
                                }
                            }

                        }
                    }

                }
            }
        }
    }

}





