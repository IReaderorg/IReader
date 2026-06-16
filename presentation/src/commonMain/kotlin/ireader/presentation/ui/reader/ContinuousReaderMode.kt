package ireader.presentation.ui.reader

import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ireader.domain.models.prefs.PreferenceValues
import ireader.presentation.core.toComposeColor
import ireader.presentation.ui.component.list.scrollbars.ILazyColumnScrollbar
import ireader.presentation.ui.reader.viewmodel.ReaderScreenViewModel

/**
 * Continuous reader mode — shows ONE chapter as scrollable vertical content.
 * At the end of the chapter, the user can scroll/go to next chapter.
 * No multi-chapter concatenation (that's InfiniteScroll).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ContinuousReaderContent(
    vm: ReaderScreenViewModel,
    modifier: Modifier = Modifier,
    lazyListState: LazyListState,
    onNext: () -> Unit,
    onShowComments: (ireader.domain.models.entities.Chapter) -> Unit,
) {
    val readerState by vm.state.collectAsState()
    val successState = readerState as? ireader.presentation.ui.reader.viewmodel.ReaderState.Success

    val content = successState?.currentContent ?: emptyList()
    val chapter = successState?.currentChapter
    val hasNextChapter = vm.currentChapterIndex < vm.stateChapters.lastIndex

    // Scroll to end when entering a new chapter (from "previous chapter" navigation)
    LaunchedEffect(vm.scrollToEndOnChapterChange) {
        if (vm.scrollToEndOnChapterChange) {
            kotlinx.coroutines.delay(150)
            val totalItems = lazyListState.layoutInfo.totalItemsCount
            if (totalItems > 0) {
                lazyListState.scrollToItem(totalItems - 1)
            }
            vm.scrollToEndOnChapterChange = false
        }
    }

    // Update reading time estimation
    val visibleItemInfo = remember { derivedStateOf { lazyListState.layoutInfo } }
    LaunchedEffect(visibleItemInfo.value, vm.stateChapter?.id) {
        val layoutInfo = lazyListState.layoutInfo
        val totalItems = layoutInfo.totalItemsCount
        val firstVisibleItem = layoutInfo.visibleItemsInfo.firstOrNull()?.index ?: 0
        if (totalItems > 0 && !vm.isLoading) {
            val scrollProgress = firstVisibleItem.toFloat() / totalItems.toFloat()
            vm.updateReadingTimeEstimation(scrollProgress)
        }
    }

    // Auto-scroll
    LaunchedEffect(vm.autoScrollMode, vm.autoScrollOffset.value, vm.stateChapter?.id) {
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
            // Render ONLY the current chapter's content
            items(
                count = content.size,
                key = { index -> "content-${chapter?.id ?: 0}-$index" }
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

            // ChapterVoidSpace at the end
            if (content.isNotEmpty() && chapter != null) {
                item(key = "void-${chapter.id}") {
                    ChapterVoidSpace(
                        chapter = chapter,
                        isLast = !hasNextChapter,
                        textColor = vm.textColorCompose.value,
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
