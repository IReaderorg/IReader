package ireader.presentation.ui.reader

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ireader.core.source.model.Page
import ireader.domain.models.entities.Chapter
import ireader.presentation.ui.reader.viewmodel.ReaderScreenViewModel

/**
 * Infinite scroll reading mode — loads and concatenates multiple chapters
 * into a single continuous scrollable stream.
 *
 * Differences from Continuous mode:
 * - Continuous: scrolls within ONE chapter, shows "next chapter" card at the end
 * - InfiniteScroll: concatenates MULTIPLE chapters into one seamless stream,
 *   auto-loads next chapters as user scrolls, chapter headings as separators
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InfiniteScrollReaderContent(
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
        ?: return

    val loadedChapters = remember(successState.currentChapter.id) {
        mutableStateListOf(successState.currentChapter to successState.currentContent)
    }
    var isLoadingNextChapter by remember { mutableStateOf(false) }
    var hasMoreChapters by remember { mutableStateOf(true) }
    var lastTrackedChapterIndex by remember { mutableIntStateOf(0) }

    // After initialization, preload next 2 chapters
    LaunchedEffect(successState.currentChapter.id) {
        hasMoreChapters = true
        isLoadingNextChapter = false
        lastTrackedChapterIndex = 0

        val chapters = successState.chapters
        val currentIndex = chapters.indexOfFirst { it.id == successState.currentChapter.id }
        if (currentIndex != -1) {
            for (offset in 1..2) {
                val nextChapter = chapters.getOrNull(currentIndex + offset) ?: break
                isLoadingNextChapter = true
                try {
                    val pages = vm.contentVM.fetchChapterContentForInfiniteScroll(nextChapter)
                    if (pages.isNotEmpty()) {
                        loadedChapters.add(nextChapter to pages)
                    } else {
                        hasMoreChapters = false
                        break
                    }
                } catch (_: Exception) {
                    hasMoreChapters = false
                    break
                }
            }
            isLoadingNextChapter = false
        }
    }

    // Build flat content with chapter headings and end dividers
    val flatContent = remember(loadedChapters.toList()) {
        val result = mutableListOf<InfiniteScrollItem>()
        loadedChapters.forEachIndexed { idx, (chapter, pages) ->
            if (idx > 0) {
                result.add(ChapterEndItem(loadedChapters[idx - 1].first))
            }
            result.add(ChapterHeadingItem(chapter.name))
            pages.forEach { result.add(PageItem(it)) }
        }
        if (loadedChapters.isNotEmpty()) {
            result.add(ChapterEndItem(loadedChapters.last().first))
        }
        result
    }

    val itemToChapterIndex = remember(flatContent) {
        val map = IntArray(flatContent.size)
        var chapterIdx = -1
        flatContent.forEachIndexed { i, item ->
            when (item) {
                is ChapterHeadingItem -> chapterIdx++
                else -> {}
            }
            map[i] = chapterIdx.coerceAtLeast(0)
        }
        map
    }

    // Scroll progress for triggering loads
    val scrollProgress by remember {
        derivedStateOf {
            val layoutInfo = lazyListState.layoutInfo
            val totalItems = layoutInfo.totalItemsCount
            if (totalItems == 0) return@derivedStateOf 0f
            val lastVisible = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            lastVisible.toFloat() / totalItems.toFloat()
        }
    }

    // Keep loading ahead when scrolling near bottom
    LaunchedEffect(scrollProgress) {
        if (scrollProgress > 0.75f && !isLoadingNextChapter && hasMoreChapters) {
            val chapters = successState.chapters
            val currentId = loadedChapters.lastOrNull()?.first?.id ?: successState.currentChapter.id
            val currentIdx = chapters.indexOfFirst { it.id == currentId }
            val loadedCount = loadedChapters.size

            for (offset in loadedCount..<(loadedCount + 3)) {
                val nextChapter = chapters.getOrNull(currentIdx + offset) ?: break
                isLoadingNextChapter = true
                try {
                    val pages = vm.contentVM.fetchChapterContentForInfiniteScroll(nextChapter)
                    if (pages.isNotEmpty()) {
                        loadedChapters.add(nextChapter to pages)
                    } else {
                        hasMoreChapters = false
                        break
                    }
                } catch (_: Exception) {
                    hasMoreChapters = false
                    break
                }
            }
            isLoadingNextChapter = false
        }
    }

    // Track visible chapter and update VM
    val firstVisibleItemIndex by remember {
        derivedStateOf { lazyListState.firstVisibleItemIndex }
    }
    LaunchedEffect(firstVisibleItemIndex) {
        if (firstVisibleItemIndex < itemToChapterIndex.size && loadedChapters.size > 1) {
            val chapterIdx = itemToChapterIndex[firstVisibleItemIndex]
            if (chapterIdx != lastTrackedChapterIndex && chapterIdx < loadedChapters.size) {
                lastTrackedChapterIndex = chapterIdx
                vm.contentVM.updateCurrentChapterForInfiniteScroll(loadedChapters[chapterIdx].first)
            }
        }
    }

    val chapterItems = remember(flatContent) {
        var pageIdx = 0
        flatContent.map { item ->
            val key = when (item) {
                is ChapterHeadingItem -> "heading_${item.title}"
                is ChapterEndItem -> "end_${item.chapter.id}"
                is PageItem -> {
                    val idx = pageIdx++
                    "page_${item.page.hashCode()}_$idx"
                }
            }
            key to item
        }
    }

    LazyColumn(
        state = lazyListState,
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        items(
            count = chapterItems.size,
            key = { chapterItems[it].first },
        ) { index ->
            val (_, item) = chapterItems[index]
            when (item) {
                is ChapterHeadingItem -> {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(vertical = 16.dp),
                    )
                }
                is ChapterEndItem -> {
                    ChapterVoidSpace(
                        chapter = item.chapter,
                        isLast = item.chapter.id == loadedChapters.lastOrNull()?.first?.id && !hasMoreChapters,
                        textColor = vm.textColorCompose.value,
                        onShowComments = { onShowComments(item.chapter) },
                        onNextChapter = {},
                        isLoading = isLoadingNextChapter,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                    )
                }
                is PageItem -> {
                    MainText(
                        modifier = Modifier,
                        index = index,
                        page = item.page,
                        vm = vm,
                    )
                }
            }
        }

        if (isLoadingNextChapter) {
            item(key = "loading") {
                Text(
                    text = "Loading next chapter...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    textAlign = TextAlign.Center,
                )
            }
        }

        if (!hasMoreChapters && loadedChapters.size > 1) {
            item(key = "end") {
                Text(
                    text = "End of all loaded chapters",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

private sealed class InfiniteScrollItem
private data class ChapterHeadingItem(val title: String) : InfiniteScrollItem()
private data class ChapterEndItem(val chapter: Chapter) : InfiniteScrollItem()
private data class PageItem(val page: Page) : InfiniteScrollItem()
