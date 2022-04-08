package org.ireader.presentation.feature_detail.presentation.chapter_detail

import android.content.Context
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import org.ireader.core.utils.UiText
import org.ireader.core_ui.ui.EmptyScreen
import org.ireader.core_ui.ui.LoadingScreen
import org.ireader.presentation.feature_detail.presentation.chapter_detail.viewmodel.ChapterDetailEvent
import org.ireader.presentation.feature_detail.presentation.chapter_detail.viewmodel.ChapterDetailViewModel
import org.ireader.presentation.feature_settings.presentation.webview.CustomTextField
import org.ireader.presentation.presentation.components.ChapterListItemComposable
import org.ireader.presentation.presentation.reusable_composable.AppIconButton
import org.ireader.presentation.presentation.reusable_composable.BigSizeTextComposable
import org.ireader.presentation.presentation.reusable_composable.MidSizeTextComposable
import org.ireader.presentation.ui.ReaderScreenSpec


@ExperimentalAnimationApi
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ChapterDetailScreen(
    modifier: Modifier = Modifier,
    vm: ChapterDetailViewModel = hiltViewModel(),
    navController: NavController = rememberNavController(),
) {
    val book = vm.book
    val context = LocalContext.current
    val scrollState = rememberLazyListState()
    val focusManager = LocalFocusManager.current
//    LaunchedEffect(key1 = true) {
//        vm.book?.let { vm.getLocalBookById(it.id) }
//    }
    LaunchedEffect(key1 = true) {
        vm.book?.let { book ->
            vm.getLastReadChapter(book)
        }
    }
    val scope = rememberCoroutineScope()
    Scaffold(
        modifier = Modifier.systemBarsPadding(),
        topBar = {
            ChapterDetailTopAppBar(
                state = vm,
                onClickCancelSelection = { vm.selection.clear() },
                onClickSelectAll = {
                    vm.selection.clear()
                    vm.selection.addAll(vm.chapters.map { it.id })
                    vm.selection.distinct()
                },
                onClickFlipSelection = {
                    val ids: List<Long> =
                        vm.chapters.map { it.id }
                            .filterNot { it in vm.selection }.distinct()
                    vm.selection.clear()
                    vm.selection.addAll(ids)
                },
                onReverseClick = {
                    vm.onEvent(ChapterDetailEvent.ToggleOrder)
                },
                onPopBackStack = { navController.popBackStack() }
            )
        },
        drawerGesturesEnabled = true,
        drawerBackgroundColor = MaterialTheme.colors.background,
        drawerContent = {
            Column(modifier = modifier
                .fillMaxSize()
                .systemBarsPadding(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top) {
                Spacer(modifier = modifier.height(5.dp))
                BigSizeTextComposable(text = "Advance Setting")

                Spacer(modifier = modifier.height(5.dp))
                Divider(modifier = modifier.fillMaxWidth(), thickness = 1.dp)
                TextButton(modifier = Modifier.fillMaxWidth(),
                    onClick = { vm.reverseChapterInDB() }) {
                    MidSizeTextComposable(text = "Reverse Chapters in DB")
                }
            }
        },
        floatingActionButtonPosition = FabPosition.End,
        floatingActionButton = {
            FloatingActionButton(onClick = {
                scope.launch {
                    try {

                        scrollState.scrollToItem(vm.getLastChapterIndex(),
                            -scrollState.layoutInfo.viewportEndOffset / 2)

                    } catch (e: Exception) {

                    }
                }
            }) {
                Icon(Icons.Filled.Map, "", tint = MaterialTheme.colors.onSecondary)
            }
        },
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column {
                CustomTextField(modifier = Modifier
                    .padding(horizontal = 8.dp, vertical = 8.dp)
                    .height(35.dp)
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colors.onBackground.copy(.1f),
                        shape = CircleShape
                    ),
                    hint = "Search...",
                    value = vm.query,
                    onValueChange = {
                        vm.query = it
                        vm.getLocalChaptersByPaging(vm.isAsc)
                    },
                    onValueConfirm = {
                        focusManager.clearFocus()
                    },
                    paddingTrailingIconStart = 8.dp,
                    paddingLeadingIconEnd = 8.dp,
                    trailingIcon = {
                        if (vm.query.isNotBlank()) {
                            AppIconButton(imageVector = Icons.Default.Close,
                                title = "Exit search",
                                onClick = {
                                    vm.query = ""
                                    vm.getLocalChaptersByPaging(vm.isAsc)
                                })
                        }
                    }
                )
                Box(modifier.fillMaxSize()) {
                    Crossfade(targetState = Pair(vm.isLoading,
                        vm.isEmpty)) { (isLoading, isEmpty) ->
                        when {
                            isLoading -> LoadingScreen()
                            isEmpty -> EmptyScreen(UiText.DynamicString("There is no chapter."))
                            else -> LazyColumn(modifier = Modifier
                                .fillMaxSize(), state = scrollState) {
                                items(vm.chapters.size) { index ->
                                    ChapterListItemComposable(modifier = modifier,
                                        chapter = vm.chapters[index],
                                        onItemClick = {
                                            if (vm.selection.isEmpty()) {
                                                if (book != null) {
                                                    navController.navigate(ReaderScreenSpec.buildRoute(
                                                        bookId = book.id,
                                                        sourceId = book.sourceId,
                                                        chapterId = vm.chapters[index].id,
                                                    ))
                                                }
                                            } else {
                                                when (vm.chapters[index].id) {
                                                    in vm.selection -> {
                                                        vm.selection.remove(vm.chapters[index].id)
                                                    }
                                                    else -> {
                                                        vm.selection.add(vm.chapters[index].id)
                                                    }
                                                }

                                            }

                                        },
                                        isLastRead = vm.chapters[index].id == vm.lastRead,
                                        isSelected = vm.chapters[index].id in vm.selection,
                                        onLongClick = { vm.selection.add(vm.chapters[index].id) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
            when {
                vm.hasSelection -> {
                    ChapterDetailBottomBar(vm, context)
                }
            }
        }


    }

    @Composable
    fun Modifier.simpleVerticalScrollbar(
        state: LazyListState,
        width: Dp = 8.dp,
        color: Color = MaterialTheme.colors.primary,
    ): Modifier {
        val targetAlpha = if (state.isScrollInProgress) 1f else 0f
        val duration = if (state.isScrollInProgress) 150 else 500

        val alpha by animateFloatAsState(
            targetValue = targetAlpha,
            animationSpec = tween(durationMillis = duration)
        )

        return drawWithContent {
            drawContent()

            val firstVisibleElementIndex = state.layoutInfo.visibleItemsInfo.firstOrNull()?.index
            val needDrawScrollbar = state.isScrollInProgress || alpha > 0.0f

            // Draw scrollbar if scrolling or if the animation is still running and lazy column has content
            if (needDrawScrollbar && firstVisibleElementIndex != null) {
                val elementHeight = this.size.height / state.layoutInfo.totalItemsCount
                val scrollbarOffsetY = firstVisibleElementIndex * elementHeight
                val scrollbarHeight = state.layoutInfo.visibleItemsInfo.size * elementHeight

                drawRect(
                    color = color,
                    topLeft = Offset(this.size.width - width.toPx(), scrollbarOffsetY),
                    size = Size(width.toPx(), scrollbarHeight),
                    alpha = alpha
                )
            }
        }
    }
}

@Composable
private fun BoxScope.ChapterDetailBottomBar(vm: ChapterDetailViewModel, context: Context) {
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
                    vm.downloadChapters(context = context)
                    vm.selection.clear()
                })
            AppIconButton(imageVector = Icons.Default.BookmarkBorder,
                title = "Bookmark",
                onClick = {
                    vm.insertChapters(vm.chapters.filter { it.id in vm.selection }
                        .map { it.copy(bookmark = !it.bookmark) })
                    vm.selection.clear()
                })

            AppIconButton(imageVector = if (vm.chapters.filter { it.read }
                    .map { it.id }
                    .containsAll(vm.selection)) Icons.Default.DoneOutline else Icons.Default.Done,
                title = "Mark as read",
                onClick = {
                    vm.insertChapters(vm.chapters.filter { it.id in vm.selection }
                        .map { it.copy(read = !it.read) })
                    vm.selection.clear()
                })
            AppIconButton(imageVector = Icons.Default.PlaylistAddCheck,
                title = "Mark Previous as read",
                onClick = {
                    vm.insertChapters(vm.chapters.filter { it.id <= vm.selection.maxOrNull() ?: 0 }
                        .map { it.copy(read = true) })
                    vm.selection.clear()
                })
            AppIconButton(imageVector = Icons.Default.Delete,
                title = "Delete",
                onClick = {
                    vm.deleteChapters(vm.chapters.filter { it.id in vm.selection })
                    vm.selection.clear()
                })
        }
    }
}