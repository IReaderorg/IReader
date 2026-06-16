package ireader.presentation.ui.reader

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import ireader.core.source.model.Page
import ireader.presentation.core.toComposeColor
import ireader.presentation.ui.reader.viewmodel.ReaderScreenViewModel
import ireader.domain.models.entities.Chapter

/**
 * Paged reader mode — HorizontalPager with virtual navigation pages.
 *
 * Layout: [prev_nav] [content_page_1] [content_page_2] ... [content_page_N] [next_nav]
 * - Swipe right at first page → triggers onPrev (goes to prev chapter, starts at last page)
 * - Swipe left at last page → triggers onNext (goes to next chapter, starts at first page)
 * - Vertical drag is disabled
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PagedReaderContent(
    vm: ReaderScreenViewModel,
    modifier: Modifier = Modifier,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onShowComments: (chapter: Chapter) -> Unit,
) {
    val readerState by vm.state.collectAsState()
    val successState = readerState as? ireader.presentation.ui.reader.viewmodel.ReaderState.Success

    val content = successState?.currentContent ?: emptyList()

    // Split content into pages
    val wordsPerPage = 500
    val pages = remember(content) {
        if (content.isEmpty()) return@remember emptyList()
        val result = mutableListOf<List<ireader.core.source.model.Page>>()
        var words = 0
        var items = mutableListOf<ireader.core.source.model.Page>()
        for (page in content) {
            val wc = when (page) {
                is ireader.core.source.model.Text -> page.text.split("\\s+".toRegex()).size
                else -> 1
            }
            items.add(page)
            words += wc
            if (words >= wordsPerPage) {
                result.add(items)
                items = mutableListOf()
                words = 0
            }
        }
        if (items.isNotEmpty()) result.add(items)
        result
    }

    val contentPageCount = pages.size.coerceAtLeast(1)
    // +2: virtual prev nav at start, virtual next nav at end
    val totalPageCount = contentPageCount + 2
    val prevNavPage = 0
    val nextNavPage = totalPageCount - 1
    val firstContentPage = 1

    val pagerState = rememberPagerState(pageCount = { totalPageCount })

    // Navigate to first content page when chapter loads, or last page if going prev
    LaunchedEffect(successState?.currentChapter?.id) {
        if (vm.scrollToEndOnChapterChange) {
            // Coming from prev chapter → start at last content page
            pagerState.scrollToPage(nextNavPage - 1)
        } else {
            pagerState.scrollToPage(firstContentPage)
        }
    }

    // Detect when user swipes to virtual navigation pages
    LaunchedEffect(pagerState.currentPage) {
        when (pagerState.currentPage) {
            prevNavPage -> {
                // Swipe right past first page → previous chapter
                pagerState.scrollToPage(firstContentPage)
                onPrev()
            }
            // nextNavPage (void page) is NOT auto-navigated — user sees ChapterVoidSpace
            // and can tap "Next Chapter" or swipe left past it to go to next chapter
        }
    }

    // Reading time
    LaunchedEffect(pagerState.currentPage, successState?.currentChapter?.id) {
        if (pages.isNotEmpty() && !vm.isLoading) {
            val idx = (pagerState.currentPage - firstContentPage).coerceIn(0, pages.size - 1)
            vm.updateReadingTimeEstimation(idx.toFloat() / pages.size.toFloat())
        }
    }

    val chapter = successState?.currentChapter
    val hasNextChapter = vm.currentChapterIndex < vm.stateChapters.lastIndex

    // Disable vertical scrolling — only horizontal page swiping allowed
    HorizontalPager(
        state = pagerState,
        modifier = modifier.fillMaxSize(),
        userScrollEnabled = true,
    ) { pageIndex ->
        when {
            pageIndex == prevNavPage -> {
                // Empty trigger page for prev chapter
            }
            pageIndex == nextNavPage -> {
                // ChapterVoidSpace with comments and next chapter
                androidx.compose.foundation.layout.Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    if (chapter != null) {
                        ChapterVoidSpace(
                            chapter = chapter,
                            isLast = !hasNextChapter,
                            textColor = vm.textColorCompose.value,
                            backgroundColor = vm.backgroundColor.value.toComposeColor(),
                            onShowComments = { onShowComments(chapter) },
                            onNextChapter = { onNext() },
                            isLoading = vm.isLoading
                        )
                    }
                }
            }
            else -> {
                val contentIndex = pageIndex - firstContentPage
                val pageItems = pages.getOrElse(contentIndex) { emptyList() }
                androidx.compose.foundation.layout.Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    pageItems.forEachIndexed { index: Int, page: Page ->
                        MainText(
                            modifier = Modifier.fillMaxWidth(),
                            index = index,
                            page = page,
                            vm = vm
                        )
                    }
                }
            }
        }
    }
}
