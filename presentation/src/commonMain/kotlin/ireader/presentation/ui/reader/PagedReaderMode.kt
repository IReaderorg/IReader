package ireader.presentation.ui.reader

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ireader.domain.models.prefs.PreferenceValues
import ireader.presentation.core.toComposeColor
import ireader.presentation.ui.component.list.scrollbars.ILazyColumnScrollbar
import ireader.presentation.ui.reader.viewmodel.ReaderScreenViewModel
import ireader.domain.models.entities.Chapter
import kotlinx.coroutines.flow.distinctUntilChanged

/**
 * Paged reader mode — uses HorizontalPager for left/right page swiping.
 * Each page shows a portion of the chapter content.
 * User can only swipe horizontally — no vertical scrolling.
 *
 * Pages are split from the chapter content, roughly one screenful per page.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PagedReaderContent(
    vm: ReaderScreenViewModel,
    modifier: Modifier = Modifier,
    lazyListState: androidx.compose.foundation.lazy.LazyListState,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    toggleReaderMode: () -> Unit,
    onShowComments: (chapter: Chapter) -> Unit,
) {
    val readerState by vm.state.collectAsState()
    val successState = readerState as? ireader.presentation.ui.reader.viewmodel.ReaderState.Success

    val content = successState?.currentContent ?: emptyList()
    val totalWords = content.sumOf { page ->
        when (page) {
            is ireader.core.source.model.Text -> page.text.split("\\s+".toRegex()).size
            else -> 0
        }
    }

    // Split content into pages: each page has ~500 words
    val wordsPerPage = 500
    val pages = remember(content, totalWords) {
        if (content.isEmpty()) return@remember emptyList()
        
        val result = mutableListOf<List<ireader.core.source.model.Page>>()
        var currentPageWords = 0
        var currentPageItems = mutableListOf<ireader.core.source.model.Page>()
        
        for (page in content) {
            val wordCount = when (page) {
                is ireader.core.source.model.Text -> page.text.split("\\s+".toRegex()).size
                else -> 1
            }
            currentPageItems.add(page)
            currentPageWords += wordCount
            
            if (currentPageWords >= wordsPerPage) {
                result.add(currentPageItems)
                currentPageItems = mutableListOf()
                currentPageWords = 0
            }
        }
        if (currentPageItems.isNotEmpty()) {
            result.add(currentPageItems)
        }
        result
    }

    val pageCount = pages.size.coerceAtLeast(1)

    val pagerState = rememberPagerState(pageCount = { pageCount })

    // Update reading time estimation based on scroll position
    LaunchedEffect(pagerState.currentPage, successState?.currentChapter?.id) {
        if (pageCount > 0 && !vm.isLoading) {
            val scrollProgress = pagerState.currentPage.toFloat() / pageCount.toFloat()
            vm.updateReadingTimeEstimation(scrollProgress)
        }
    }

    // When user swipes to the last page, trigger next chapter
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }
            .distinctUntilChanged()
            .collect { page ->
                if (page == pageCount - 1 && pageCount > 1) {
                    // At the last page — show chapter end card
                }
            }
    }

    val chapter = successState?.currentChapter
    val hasNextChapter = vm.currentChapterIndex < vm.stateChapters.lastIndex

    HorizontalPager(
        state = pagerState,
        modifier = modifier.fillMaxSize(),
    ) { pageIndex ->
        val pageItems = pages.getOrElse(pageIndex) { emptyList() }

        if (pageIndex < pages.size) {
            // Render the page content
            androidx.compose.foundation.layout.Column(
                modifier = Modifier.fillMaxSize()
            ) {
                pageItems.forEachIndexed { index, page ->
                    MainText(
                        modifier = Modifier.fillMaxWidth(),
                        index = index,
                        page = page,
                        vm = vm
                    )
                }

                // Show ChapterVoidSpace on the last page
                if (pageIndex == pageCount - 1 && chapter != null) {
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
