package ireader.presentation.ui.reader

import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import ireader.core.source.model.Page
import ireader.presentation.core.toComposeColor
import ireader.presentation.ui.reader.viewmodel.ReaderScreenViewModel
import ireader.domain.models.entities.Chapter

/**
 * Paged reader mode — all content rendered in a single Column,
 * paged by viewport height. Swipe left/right to change pages.
 *
 * This approach:
 * - Never cuts off text (content wraps naturally)
 * - Accurate page count based on measured layout height
 * - Smooth horizontal swipe to change pages
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
    val scope = rememberCoroutineScope()

    val density = LocalDensity.current
    val scrollState = rememberScrollState()

    // Measured dimensions
    var contentHeightPx by remember { mutableIntStateOf(0) }
    var viewportHeightPx by remember { mutableIntStateOf(0) }

    // Current page derived from scroll position
    val currentPage = if (viewportHeightPx > 0) {
        (scrollState.value / viewportHeightPx) + 1
    } else {
        1
    }

    val totalPages = if (viewportHeightPx > 0 && contentHeightPx > viewportHeightPx) {
        (contentHeightPx + viewportHeightPx - 1) / viewportHeightPx
    } else {
        1
    }

    // Update VM page info
    LaunchedEffect(currentPage, totalPages) {
        vm.updatePagedPageInfo(currentPage, totalPages)
    }

    // Reading time estimation
    LaunchedEffect(currentPage, totalPages, successState?.currentChapter?.id) {
        if (totalPages > 0 && !vm.isLoading) {
            vm.updateReadingTimeEstimation((currentPage - 1).toFloat() / totalPages.toFloat())
        }
    }

    // Navigate to correct scroll position on chapter load
    LaunchedEffect(successState?.currentChapter?.id) {
        if (vm.scrollToEndOnChapterChange && contentHeightPx > viewportHeightPx) {
            // Start at last page
            val lastPageOffset = ((totalPages - 1) * viewportHeightPx).coerceAtMost(contentHeightPx - viewportHeightPx)
            scrollState.scrollTo(lastPageOffset)
        }
    }

    val chapter = successState?.currentChapter
    val hasNextChapter = vm.currentChapterIndex < vm.stateChapters.lastIndex

    // Track horizontal drag for page swiping
    var dragAccumulator by remember { mutableFloatStateOf(0f) }
    val swipeThreshold = with(density) { 80.dp.toPx() }

    Box(
        modifier = modifier
            .fillMaxSize()
            .onGloballyPositioned { coords ->
                if (viewportHeightPx == 0) {
                    viewportHeightPx = coords.size.height
                }
            }
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragStart = { dragAccumulator = 0f },
                    onDragEnd = {
                        when {
                            dragAccumulator > swipeThreshold -> {
                                // Swipe right → previous page
                                if (currentPage <= 1) {
                                    // At first page, go to prev chapter
                                    onPrev()
                                } else {
                                    val target = ((currentPage - 2) * viewportHeightPx).coerceAtLeast(0)
                                    scope.launch { scrollState.animateScrollTo(target) }
                                }
                            }
                            dragAccumulator < -swipeThreshold -> {
                                // Swipe left → next page
                                if (currentPage >= totalPages) {
                                    // At last page, go to next chapter
                                    onNext()
                                } else {
                                    val target = (currentPage * viewportHeightPx).coerceAtMost(
                                        (contentHeightPx - viewportHeightPx).coerceAtLeast(0)
                                    )
                                    scope.launch { scrollState.animateScrollTo(target) }
                                }
                            }
                        }
                        dragAccumulator = 0f
                    },
                    onHorizontalDrag = { _, dragAmount ->
                        dragAccumulator += dragAmount
                    }
                )
            }
    ) {
            // Render all content in a single scrollable column
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .onGloballyPositioned { coordinates ->
                        contentHeightPx = coordinates.size.height
                    }
                    .verticalScroll(scrollState, enabled = false) // disabled — we control scroll programmatically
            ) {
                content.forEachIndexed { index, page ->
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
