package org.ireader.chapterDetails

import android.content.Context
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.DoneOutline
import androidx.compose.material.icons.filled.GetApp
import androidx.compose.material.icons.filled.PlaylistAddCheck
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.ireader.chapterDetails.viewmodel.ChapterDetailViewModel
import org.ireader.components.components.ChapterListItemComposable
import org.ireader.components.list.scrollbars.LazyColumnScrollbar
import org.ireader.components.reusable_composable.AppIconButton
import org.ireader.core_ui.ui.EmptyScreen
import org.ireader.core_ui.ui.LoadingScreen
import org.ireader.explore.webview.CustomTextField
import org.ireader.ui_chapter_detail.R

@ExperimentalAnimationApi
@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ChapterDetailScreen(
    modifier: Modifier = Modifier,
    vm: ChapterDetailViewModel,
    onItemClick: (index: Int) -> Unit,
    onLongItemClick: (index: Int) -> Unit,
    scaffoldPadding : PaddingValues
) {
    val context = LocalContext.current
    val scrollState = rememberLazyListState()
    LaunchedEffect(key1 = scrollState.hashCode() ) {
        vm.scrollState = scrollState
    }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(key1 = true) {
        vm.book?.let { book ->
            vm.getLastReadChapter(book)
        }
    }
    val scope = rememberCoroutineScope()
    Box(modifier = Modifier
        .padding(scaffoldPadding)
        .fillMaxSize()) {
        Column {
            CustomTextField(
                modifier = Modifier
                    .padding(horizontal = 8.dp, vertical = 8.dp)
                    .height(35.dp)
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.onBackground.copy(.1f),
                        shape = CircleShape
                    ),
                hint = stringResource(R.string.search_hint),
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
                        AppIconButton(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.exit_search_mode),
                            onClick = {
                                vm.query = ""
                                vm.getLocalChaptersByPaging(vm.isAsc)
                            }
                        )
                    }
                }
            )
            Box(modifier.fillMaxSize()) {
                Crossfade(
                    targetState = Pair(
                        vm.isLoading,
                        vm.isEmpty
                    )
                ) { (isLoading, isEmpty) ->
                    when {
                        isLoading -> LoadingScreen()
                        isEmpty -> EmptyScreen(text = stringResource(R.string.there_is_no_chapter))
                        else -> {
                            LazyColumnScrollbar(listState = scrollState) {
                                LazyColumn(
                                    modifier = Modifier
                                        .fillMaxSize(),
                                    state = scrollState
                                ) {
                                    items(vm.chapters.size) { index ->
                                        ChapterListItemComposable(
                                            modifier = modifier,
                                            chapter = vm.chapters[index],
                                            onItemClick = {
                                                onItemClick(index)
                                            },
                                            isLastRead = vm.chapters[index].id == vm.lastRead,
                                            isSelected = vm.chapters[index].id in vm.selection,
                                            onLongClick = {
                                                onLongItemClick(index)
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        when {
            vm.hasSelection -> {
                ChapterDetailBottomBar(
                    vm,
                    context,
                    onDownload = {
                    },
                    onBookmark = {
                    },
                    onMarkAsRead = {
                    }
                )
            }
        }
    }

    @Composable
    fun Modifier.simpleVerticalScrollbar(
        state: LazyListState,
        width: Dp = 8.dp,
        color: Color = MaterialTheme.colorScheme.primary,
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
private fun BoxScope.ChapterDetailBottomBar(
    vm: ChapterDetailViewModel,
    context: Context,
    onDownload: () -> Unit,
    onBookmark: () -> Unit,
    onMarkAsRead: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .align(Alignment.BottomCenter)
            .padding(8.dp)
            .background(MaterialTheme.colorScheme.background)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.onBackground.copy(.1f)
            )
            .clickable(enabled = false) {},
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppIconButton(
                imageVector = Icons.Default.GetApp,
                contentDescription = stringResource(R.string.download),
                onClick = {
                    vm.downloadChapters()
                    vm.selection.clear()
                }
            )
            AppIconButton(
                imageVector = Icons.Default.BookmarkBorder,
                contentDescription = stringResource(R.string.bookmark),
                onClick = {
                    vm.insertChapters(
                        vm.chapters.filter { it.id in vm.selection }
                            .map { it.copy(bookmark = !it.bookmark) }
                    )
                    vm.selection.clear()
                }
            )

            AppIconButton(
                imageVector = if (vm.chapters.filter { it.read }
                        .map { it.id }
                        .containsAll(vm.selection)
                ) Icons.Default.DoneOutline else Icons.Default.Done,
                contentDescription = stringResource(R.string.mark_as_read),
                onClick = {
                    vm.insertChapters(
                        vm.chapters.filter { it.id in vm.selection }
                            .map { it.copy(read = !it.read) }
                    )
                    vm.selection.clear()
                }
            )
            AppIconButton(
                imageVector = Icons.Default.PlaylistAddCheck,
                contentDescription = stringResource(R.string.mark_previous_as_read),
                onClick = {
                    vm.insertChapters(
                        vm.chapters.filter { it.id <= (vm.selection.maxOrNull() ?: 0) }
                            .map { it.copy(read = true) }
                    )
                    vm.selection.clear()
                }
            )
            AppIconButton(
                imageVector = Icons.Default.Delete,
                contentDescription = stringResource(R.string.delete),
                onClick = {
                    vm.deleteChapters(vm.chapters.filter { it.id in vm.selection })
                    vm.selection.clear()
                }
            )
        }
    }
}
