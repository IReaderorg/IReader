package ireader.presentation.ui.reader

import androidx.compose.foundation.layout.Box
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
import ireader.domain.models.entities.Chapter

/**
 * Single entry point for paged reader mode.
 * Consolidates the optimized LazyColumn-based paged reader into one public composable.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PagedReaderContent(
    vm: ReaderScreenViewModel,
    modifier: Modifier = Modifier,
    lazyListState: LazyListState,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    toggleReaderMode: () -> Unit,
    onShowComments: (chapter: Chapter) -> Unit,
) {
    val readerState by vm.state.collectAsState()
    val successState = readerState as? ireader.presentation.ui.reader.viewmodel.ReaderState.Success

    val visibleItemInfo = remember { derivedStateOf { lazyListState.layoutInfo } }
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

    val content = successState?.currentContent ?: emptyList()

    val currentIndex = vm.currentChapterIndex
    val chapters = vm.stateChapters
    val hasNextChapter = currentIndex < chapters.lastIndex

    Box(modifier = modifier.fillMaxSize()) {
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
                modifier = Modifier.fillMaxSize(),
                state = lazyListState,
            ) {
                items(
                    count = content.size,
                    key = { index ->
                        "${vm.stateChapter?.id ?: 0}-content-$index"
                    }
                ) { index ->
                    val page = remember(content, index) { content.getOrNull(index) }
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
                    item(key = "${vm.stateChapter?.id ?: 0}-chapter-void") {
                        ChapterVoidSpace(
                            chapter = vm.stateChapter ?: return@item,
                            isLast = !hasNextChapter,
                            textColor = vm.textColor.value.toComposeColor(),
                            backgroundColor = vm.backgroundColor.value.toComposeColor(),
                            onShowComments = { vm.stateChapter?.let { onShowComments(it) } },
                            onNextChapter = onNext,
                            isLoading = vm.isLoading
                        )
                    }
                }
            }
        }
    }
}
