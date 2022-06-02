package org.ireader.chapterDetails

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.ireader.chapterDetails.viewmodel.ChapterDetailViewModel
import org.ireader.common_models.entities.Chapter
import org.ireader.components.CustomTextField
import org.ireader.components.components.ChapterRow
import org.ireader.components.list.scrollbars.LazyColumnScrollbar
import org.ireader.components.reusable_composable.AppIconButton
import org.ireader.core_ui.theme.AppColors
import org.ireader.core_ui.ui.EmptyScreen
import org.ireader.core_ui.ui.LoadingScreen
import org.ireader.ui_chapter_detail.R

@ExperimentalAnimationApi
@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ChapterDetailScreen(
    modifier: Modifier = Modifier,
    vm: ChapterDetailViewModel,
    onItemClick: (Chapter) -> Unit,
    onLongItemClick: (Chapter) -> Unit,
    scaffoldPadding: PaddingValues
) {
    val context = LocalContext.current
    val scrollState = rememberLazyListState()
    LaunchedEffect(key1 = scrollState.hashCode()) {
        vm.scrollState = scrollState
    }
    val focusManager = LocalFocusManager.current

    val scope = rememberCoroutineScope()
    Box(
        modifier = Modifier
            .padding(scaffoldPadding)
            .fillMaxSize()
    ) {
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
                value = vm.query?:"",
                onValueChange = {
                    vm.query = it
                   // vm.getLocalChaptersByPaging(vm.isAsc)
                },
                onValueConfirm = {
                    focusManager.clearFocus()
                },
                paddingTrailingIconStart = 8.dp,
                paddingLeadingIconEnd = 8.dp,
                trailingIcon = {
                    if (vm.query.isNullOrBlank()) {
                        AppIconButton(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.exit_search_mode),
                            onClick = {
                                vm.query = ""
                                //vm.getLocalChaptersByPaging(vm.isAsc)
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
                    }
                }
                ChaptersContent(
                    scrollState = scrollState,
                    onLongClick = {
                        onLongItemClick(it)
                    },
                    vm = vm,
                    isLastRead = { chapter ->
                        chapter.id == vm.lastRead
                    },
                    isSelected = { chapter ->
                        chapter.id in vm.selection
                    },
                    onItemClick = {
                        onItemClick(it)
                    },
                )
                ChapterDetailBottomBar(
                    vm,
                    context,
                    onDownload = {
                    },
                    onBookmark = {
                    },
                    onMarkAsRead = {
                    },
                    visible = vm.hasSelection,
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }

        }
    }
}

@Composable
private fun ChaptersContent(
    vm: ChapterDetailViewModel,
    scrollState: LazyListState,
    isLastRead: (Chapter) -> Boolean,
    isSelected: (Chapter) -> Boolean,
    onItemClick: (Chapter) -> Unit,
    onLongClick: (Chapter) -> Unit = {},
) {
    val book = vm.book ?: return
    val chapters = vm.getChapters(book)
    LazyColumnScrollbar(listState = scrollState) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize(),
            state = scrollState
        ) {
            items(chapters.value.size) { index ->
                ChapterRow(
                    modifier = Modifier,
                    chapter = chapters.value[index],
                    onItemClick = {
                        onItemClick(chapters.value[index])
                    },
                    isLastRead = isLastRead(chapters.value[index]),
                    isSelected = isSelected(chapters.value[index]),
                    onLongClick = {
                        onLongClick(chapters.value[index])
                    }
                )
//                ChapterListItemComposable(
//                    modifier = Modifier,
//                    chapter = chapters.value[index],
//                    onItemClick = {
//                        onItemClick(chapters.value[index])
//                    },
//                    isLastRead = isLastRead(chapters.value[index]),
//                    isSelected = isSelected(chapters.value[index]),
//                    onLongClick = {
//                        onLongClick(chapters.value[index])
//                    }
//                )
            }
        }
    }
}

@Composable
private fun ChapterDetailBottomBar(
    vm: ChapterDetailViewModel,
    context: Context,
    onDownload: () -> Unit,
    onBookmark: () -> Unit,
    onMarkAsRead: () -> Unit,
    visible: Boolean,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = expandVertically(),
        exit = shrinkVertically()
    ) {
        Surface(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp)
                .fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            color = AppColors.current.bars,
            contentColor = AppColors.current.onBars,
            tonalElevation = 4.dp
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
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
}
