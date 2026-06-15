package ireader.presentation.ui.reader

import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ireader.domain.models.entities.Chapter
import ireader.domain.models.prefs.PreferenceValues
import ireader.presentation.core.toComposeColor
import ireader.presentation.ui.component.list.scrollbars.ILazyColumnScrollbar
import ireader.presentation.ui.reader.viewmodel.ReaderScreenViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ContinuousReaderContent(
    vm: ReaderScreenViewModel,
    modifier: Modifier = Modifier,
    lazyListState: LazyListState,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    toggleReaderMode: () -> Unit,
    onChapterShown: (chapter: Chapter) -> Unit,
    onShowComments: (chapter: Chapter) -> Unit,
) {
    var lastChapterId: Chapter? by remember { mutableStateOf(null) }

    LaunchedEffect(key1 = lastChapterId) {
        lastChapterId?.let { chapter ->
            onChapterShown(chapter)
        }
    }

    LaunchedEffect(key1 = vm.stateChapter?.id, key2 = vm.scrollToEndOnChapterChange) {
        if (vm.scrollToEndOnChapterChange) {
            val contentSize = vm.stateChapter?.content?.size ?: 0
            if (contentSize > 0) {
                val voidIndex = contentSize + 1
                kotlinx.coroutines.delay(150)
                val totalItems = lazyListState.layoutInfo.totalItemsCount
                if (totalItems > voidIndex) {
                    lazyListState.scrollToItem(voidIndex)
                } else if (totalItems > 0) {
                    lazyListState.scrollToItem(totalItems - 1)
                }
            }
            vm.scrollToEndOnChapterChange = false
        }
    }

    val visibleItemInfo = remember { derivedStateOf { lazyListState.layoutInfo } }

    LaunchedEffect(key1 = visibleItemInfo.value.visibleItemsInfo.firstOrNull()?.key) {
        runCatching {
            val visibleKey = lazyListState.layoutInfo.visibleItemsInfo.firstOrNull()?.key?.toString()
            if (visibleKey != null) {
                val chapterId = when {
                    visibleKey.startsWith("content-") -> visibleKey.split("-").getOrNull(1)?.toLongOrNull()
                    visibleKey.startsWith("void-") -> visibleKey.substringAfter("void-").toLongOrNull()
                    visibleKey.startsWith("header-") -> visibleKey.substringAfter("header-").toLongOrNull()
                    else -> visibleKey.substringAfter("-").toLongOrNull()
                }
                if (chapterId != null) {
                    vm.chapterShell.firstOrNull { it.id == chapterId }?.let { chapter ->
                        if (chapter.id != lastChapterId?.id) {
                            lastChapterId = chapter
                        }
                    }
                }
            }
        }.onFailure { e ->
            ireader.core.log.Log.error("Error tracking visible chapter", e)
        }
    }

    LaunchedEffect(key1 = visibleItemInfo.value, key2 = vm.stateChapter?.id) {
        val layoutInfo = lazyListState.layoutInfo
        val totalItems = layoutInfo.totalItemsCount
        val firstVisibleItem = layoutInfo.visibleItemsInfo.firstOrNull()?.index ?: 0

        if (totalItems > 0 && !vm.isLoading) {
            val scrollProgress = firstVisibleItem.toFloat() / totalItems.toFloat()
            vm.updateReadingTimeEstimation(scrollProgress)
        }
    }

    LaunchedEffect(key1 = vm.autoScrollMode, key2 = vm.autoScrollOffset.value, key3 = vm.stateChapter?.id) {
        if (vm.autoScrollMode) {
            while (vm.autoScrollMode) {
                val scrollAmount = vm.autoScrollOffset.value.toFloat()
                val layoutInfo = lazyListState.layoutInfo
                val lastVisibleItem = layoutInfo.visibleItemsInfo.lastOrNull()
                val isAtEnd = lastVisibleItem?.index == layoutInfo.totalItemsCount - 1

                if (isAtEnd) {
                    vm.autoScrollMode = false
                    onNext()
                    break
                }

                lazyListState.scrollBy(scrollAmount)
                kotlinx.coroutines.delay(16L)
            }
        }
    }

    val currentIndex = vm.currentChapterIndex
    val chapters = vm.stateChapters
    val hasNextChapter = currentIndex < chapters.lastIndex

    ILazyColumnScrollbar(
        listState = lazyListState,
        padding = if (vm.scrollIndicatorPadding.lazyValue < 0) 0.dp else vm.scrollIndicatorPadding.lazyValue.dp,
        thickness = if (vm.scrollIndicatorWith.lazyValue < 0) 0.dp else vm.scrollIndicatorWith.lazyValue.dp,
        enable = vm.showScrollIndicator.value,
        thumbColor = vm.unselectedScrollBarColor.value.toComposeColor(),
        thumbSelectedColor = vm.selectedScrollBarColor.value.toComposeColor(),
        selectionMode = vm.isScrollIndicatorDraggable.value,
        rightSide = vm.scrollIndicatorAlignment.value == PreferenceValues.PreferenceTextAlignment.Right,
    ) {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            state = lazyListState,
        ) {
            vm.chapterShell.forEachIndexed { shellIndex, chapter ->
                val isLastChapter = shellIndex == vm.chapterShell.lastIndex
                val content = chapter.content ?: emptyList()
                items(
                    count = content.size,
                    key = { index -> "content-${chapter.id}-$index" }
                ) { index ->
                    val page = content.getOrNull(index)
                    if (page != null) {
                        MainText(
                            modifier = modifier,
                            index = index,
                            page = page,
                            vm = vm
                        )
                    }
                }

                if (content.isNotEmpty()) {
                    item(key = "void-${chapter.id}") {
                        ChapterVoidSpace(
                            chapter = chapter,
                            isLast = isLastChapter && !hasNextChapter,
                            textColor = vm.textColor.value.toComposeColor(),
                            backgroundColor = vm.backgroundColor.value.toComposeColor(),
                            onShowComments = { onShowComments(chapter) },
                            onNextChapter = onNext,
                            isLoading = vm.isLoading
                        )
                    }
                }
            }
        }
    }
}

