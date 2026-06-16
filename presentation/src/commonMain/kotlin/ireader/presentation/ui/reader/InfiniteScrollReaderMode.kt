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
import ireader.presentation.core.toComposeColor
import ireader.presentation.ui.reader.viewmodel.ReaderScreenViewModel

/**
 * Infinite scroll reading mode — concatenates multiple chapters into one stream.
 *
 * Chapter tracking: A pre-computed array maps each flat content index to a chapter index.
 * When the first visible item changes, we look up which chapter it belongs to.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InfiniteScrollReaderContent(
    vm: ReaderScreenViewModel,
    modifier: Modifier = Modifier,
    lazyListState: LazyListState,
    onNext: () -> Unit,
    onShowComments: (chapter: Chapter) -> Unit,
) {
    val readerState by vm.state.collectAsState()
    val successState = readerState as? ireader.presentation.ui.reader.viewmodel.ReaderState.Success
        ?: return

    val loadedChapters = remember { mutableStateListOf<Pair<Chapter, List<Page>>>() }
    var isLoadingNextChapter by remember { mutableStateOf(false) }
    var hasMoreChapters by remember { mutableStateOf(true) }
    var lastTrackedChapterIndex by remember { mutableIntStateOf(0) }

    // Initialize with current chapter, then preload next 2
    LaunchedEffect(successState.currentChapter.id) {
        val currentPages = successState.currentContent
        loadedChapters.clear()
        loadedChapters.add(successState.currentChapter to currentPages)
        hasMoreChapters = true
        isLoadingNextChapter = false
        lastTrackedChapterIndex = 0

        // Set initial chapter for UI
        vm.contentVM.updateCurrentChapterForInfiniteScroll(successState.currentChapter)

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

    // Build flat content
    val flatContent = remember(loadedChapters.toList()) {
        val result = mutableListOf<InfiniteScrollItem>()
        loadedChapters.forEachIndexed { idx, (chapter, pages) ->
            if (idx > 0) {
                result.add(ChapterVoidItem(loadedChapters[idx - 1].first))
            }
            result.add(ChapterHeadingItem(chapter.name))
            pages.forEach { result.add(PageItem(it)) }
        }
        if (loadedChapters.isNotEmpty()) {
            result.add(ChapterVoidItem(loadedChapters.last().first))
        }
        result
    }

    // Pre-compute: for each flatContent index, which chapter index in loadedChapters does it belong to?
    val flatContentChapterMap = remember(flatContent) {
        IntArray(flatContent.size).also { map ->
            var chIdx = 0
            flatContent.forEachIndexed { i, item ->
                when (item) {
                    is ChapterHeadingItem -> chIdx++
                    else -> {}
                }
                map[i] = (chIdx - 1).coerceAtLeast(0)
            }
        }
    }

    // Track chapter from first visible item
    val firstVisible by remember {
        derivedStateOf { lazyListState.firstVisibleItemIndex }
    }
    LaunchedEffect(firstVisible) {
        if (firstVisible < flatContentChapterMap.size) {
            val chIdx = flatContentChapterMap[firstVisible]
            if (chIdx != lastTrackedChapterIndex && chIdx < loadedChapters.size) {
                lastTrackedChapterIndex = chIdx
                vm.contentVM.updateCurrentChapterForInfiniteScroll(loadedChapters[chIdx].first)
            }
        }
    }

    // Keep loading ahead
    val scrollProgress by remember {
        derivedStateOf {
            val layoutInfo = lazyListState.layoutInfo
            val totalItems = layoutInfo.totalItemsCount
            if (totalItems == 0) return@derivedStateOf 0f
            val lastVisible = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            lastVisible.toFloat() / totalItems.toFloat()
        }
    }

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

    val chapterItems = remember(flatContent) {
        flatContent.mapIndexed { index, item ->
            val key = when (item) {
                is ChapterHeadingItem -> "heading_${item.title}"
                is ChapterVoidItem -> "void_${item.chapter.id}"
                is PageItem -> "page_${item.page.hashCode()}_$index"
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
                is ChapterVoidItem -> {
                    ChapterVoidSpace(
                        chapter = item.chapter,
                        isLast = item.chapter.id == loadedChapters.lastOrNull()?.first?.id && !hasMoreChapters,
                        textColor = vm.textColorCompose.value,
                        backgroundColor = vm.backgroundColor.value.toComposeColor(),
                        onShowComments = { onShowComments(item.chapter) },
                        onNextChapter = onNext,
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
private data class ChapterVoidItem(val chapter: Chapter) : InfiniteScrollItem()
private data class PageItem(val page: Page) : InfiniteScrollItem()
