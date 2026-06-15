package ireader.presentation.ui.reader

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ireader.domain.models.entities.Chapter
import ireader.domain.models.prefs.PreferenceValues
import ireader.domain.preferences.prefs.ReadingMode
import ireader.domain.utils.extensions.currentTimeToLong
import ireader.i18n.resources.Res
import ireader.i18n.resources.bounce
import ireader.i18n.resources.bounceoffset
import ireader.i18n.resources.first_chapter
import ireader.i18n.resources.release_for_previous
import ireader.presentation.core.toComposeColor
import ireader.presentation.ui.component.list.scrollbars.ILazyColumnScrollbar
import ireader.presentation.ui.core.modifier.supportDesktopScroll
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.presentation.ui.reader.reverse_swip_refresh.ISwipeRefreshIndicator
import ireader.presentation.ui.reader.reverse_swip_refresh.MultiSwipeRefresh
import ireader.presentation.ui.reader.reverse_swip_refresh.SwipeRefreshState
import ireader.presentation.ui.reader.viewmodel.ReaderScreenViewModel
import kotlinx.coroutines.launch

/**
 * Enum representing tap zones for page mode navigation.
 * LEFT: 30% of screen width - previous page
 * CENTER: 40% of screen width - toggle reader mode
 * RIGHT: 30% of screen width - next page
 */
private enum class TapZone {
    LEFT, CENTER, RIGHT
}

/**
 * Determine the tap zone based on x-coordinate and screen width.
 * LEFT: 30% of screen width - previous page
 * CENTER: 40% of screen width - toggle reader mode
 * RIGHT: 30% of screen width - next page
 */
private fun getTapZone(x: Float, screenWidth: Float): TapZone {
    return when {
        x < screenWidth * 0.3f -> TapZone.LEFT
        x > screenWidth * 0.7f -> TapZone.RIGHT
        else -> TapZone.CENTER
    }
}

@Composable
fun ReaderText(
    modifier: Modifier = Modifier,
    vm: ReaderScreenViewModel,
    uiState: ReaderScreenViewModel, // Using ViewModel directly for state access
    onNext: () -> Unit,
    onPrev: () -> Unit,
    swipeState: SwipeRefreshState,
    scrollState: ScrollState,
    lazyListState: LazyListState,
    toggleReaderMode: () -> Unit,
    onChapterShown: (chapter: Chapter) -> Unit,
    onShowComments: (chapter: Chapter) -> Unit,
    onCopyModeDone: ((ireader.domain.models.quote.QuoteCreationParams) -> Unit)? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val scope = rememberCoroutineScope()

    // ==================== Scroll Position Restoration ====================
    // This logic restores scroll position from lastPageRead when opening a chapter
    // Works for both Page and Continues reading modes

    // Track the last chapter ID to detect chapter changes
    var lastScrolledChapterId by remember { mutableStateOf<Long?>(null) }

    // Track if we've done initial scroll restoration for this screen instance  
    var hasRestoredInitialPosition by remember { mutableStateOf(false) }

    // Collect state from ViewModel's StateFlow for reliable observation
    val readerState by vm.state.collectAsState()
    val successState = readerState as? ireader.presentation.ui.reader.viewmodel.ReaderState.Success
    val currentChapterId = successState?.currentChapter?.id
    val lastPageRead = successState?.currentChapter?.lastPageRead ?: 0L

    // Refresh chapter from database when screen is entered
    // This ensures we have the fresh lastPageRead even if ViewModel is cached
    LaunchedEffect(Unit) {
        // Wait a brief moment for state to stabilize
        kotlinx.coroutines.delay(50)
        vm.refreshCurrentChapterFromDatabase()
    }

    // Single LaunchedEffect to handle initial scroll position restoration
    // This runs for BOTH Page and Continues modes
    // Key: chapter ID change triggers this, scrollToEnd is handled separately
    LaunchedEffect(key1 = currentChapterId) {
        val chapterId = currentChapterId ?: return@LaunchedEffect

        // Wait for database refresh to complete (triggered by the Unit LaunchedEffect above)
        // This ensures we get the fresh lastPageRead from the database
        kotlinx.coroutines.delay(100)

        // Get the fresh state AFTER the delay (not stale composition capture)
        val freshState = vm.state.value as? ireader.presentation.ui.reader.viewmodel.ReaderState.Success
        val currentLastPageRead = freshState?.currentChapter?.lastPageRead ?: 0L
        val contentSize = freshState?.currentChapter?.content?.size ?: 0
        val shouldScrollToEnd = freshState?.scrollToEndOnChapterChange ?: false



        // Check if this is the same chapter we already processed
        if (chapterId == lastScrolledChapterId) {
            return@LaunchedEffect
        }

        // Mark that we're processing this chapter
        val isFirstChapterOfSession = lastScrolledChapterId == null
        lastScrolledChapterId = chapterId



        // Wait for LazyColumn to have items
        var totalItems = 0
        repeat(50) { // Wait up to ~800ms for content
            kotlinx.coroutines.delay(16) // ~1 frame
            totalItems = lazyListState.layoutInfo.totalItemsCount
            if (totalItems > 0) return@repeat
        }



        if (totalItems == 0) {

            hasRestoredInitialPosition = true
            return@LaunchedEffect
        }

        // Decide what scroll action to take
        when {
            shouldScrollToEnd -> {
                // Previous chapter navigation - scroll to end
                val targetIndex = (totalItems - 1).coerceAtLeast(0)

                lazyListState.scrollToItem(targetIndex)
                vm.scrollToEndOnChapterChange = false
            }
            isFirstChapterOfSession && currentLastPageRead > 0 -> {
                // Initial open with saved position - restore from lastPageRead
                val targetIndex = currentLastPageRead.toInt().coerceIn(0, totalItems - 1)

                lazyListState.scrollToItem(targetIndex)
            }
            !isFirstChapterOfSession -> {
                // Next chapter navigation - scroll to top

                lazyListState.scrollToItem(0)
            }
            else -> {
                // Initial open with no saved position - scroll to top

                lazyListState.scrollToItem(0)
            }
        }

        hasRestoredInitialPosition = true

    }

    // Separate LaunchedEffect for scrollToEnd changes (when navigating to previous chapter)
    LaunchedEffect(key1 = successState?.scrollToEndOnChapterChange) {
        val shouldScrollToEnd = successState?.scrollToEndOnChapterChange ?: false
        if (!shouldScrollToEnd) return@LaunchedEffect

        // Only scroll to end if we've already set up this chapter
        if (lastScrolledChapterId == currentChapterId && hasRestoredInitialPosition) {
            repeat(20) {
                kotlinx.coroutines.delay(16)
                val totalItems = lazyListState.layoutInfo.totalItemsCount
                if (totalItems > 0) {
                    val targetIndex = (totalItems - 1).coerceAtLeast(0)

                    lazyListState.scrollToItem(targetIndex)
                    vm.scrollToEndOnChapterChange = false
                    return@LaunchedEffect
                }
            }
        }
    }

    // ==================== Periodic Scroll Position Saving ====================
    // Save scroll position periodically as user scrolls to prevent data loss
    // This ensures progress is saved even if app crashes or screen is disposed abruptly

    var lastSavedScrollPosition by remember { mutableStateOf(-1) }

    // Immediately update ViewModel's tracked position on every scroll
    // This ensures the position is available even if the debounced save hasn't completed
    LaunchedEffect(key1 = lazyListState.firstVisibleItemIndex) {
        val currentPosition = lazyListState.firstVisibleItemIndex
        if (hasRestoredInitialPosition && currentPosition >= 0) {
            // Update ViewModel's in-memory tracking immediately (no DB write yet)
            vm.saveScrollPosition(currentPosition.toLong())
        }
    }

    // Debounced save to database - saves after user stops scrolling
    LaunchedEffect(key1 = lazyListState.firstVisibleItemIndex, key2 = successState?.currentChapter?.id) {
        val chapter = successState?.currentChapter ?: return@LaunchedEffect
        val currentPosition = lazyListState.firstVisibleItemIndex

        // Only save if position has actually changed and we've finished initial restoration
        if (hasRestoredInitialPosition && currentPosition != lastSavedScrollPosition) {
            // Debounce: wait a bit before saving to database to avoid too many writes
            kotlinx.coroutines.delay(500) // 500ms debounce (reduced from 1s for better reliability)

            // Check again if position is still the same (user stopped scrolling)
            if (lazyListState.firstVisibleItemIndex == currentPosition) {

                lastSavedScrollPosition = currentPosition
            }
        }
    }

    // ==================== Toggle Mode Debounce ====================

    // Add debounce mechanism for toggleReaderMode
    var lastToggleTime by remember { mutableStateOf(0L) }
    val debounceInterval = 500L // 500ms debounce

    val debouncedToggleReaderMode = remember {
        {
            val currentTime = currentTimeToLong()
            if (currentTime - lastToggleTime > debounceInterval) {
                lastToggleTime = currentTime
                toggleReaderMode()
            }
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                debouncedToggleReaderMode()
            }
            .supportDesktopScroll(
                scrollState,
                scope,
                enable = vm.readingMode.value == ReadingMode.Page
            )
            .supportDesktopScroll(
                lazyListState,
                scope,
                enable = vm.readingMode.value == ReadingMode.Continues
            )
            .fillMaxSize()
            .background(vm.backgroundColor.value.toComposeColor()),
    ) {

        val maxHeight = remember {
            constraints.maxHeight.toFloat()
        }
        // Check if at boundaries for swipe refresh
        // Both Page and Continues modes now use LazyColumn
        val isAtTop by remember { derivedStateOf {
            val firstIndex = lazyListState.firstVisibleItemIndex
            val firstOffset = lazyListState.firstVisibleItemScrollOffset
            // At top if first item is visible and scroll offset is minimal
            firstIndex == 0 && firstOffset <= 20
        }}

        val isAtBottom by remember { derivedStateOf {
            val layoutInfo = lazyListState.layoutInfo
            val lastVisibleItem = layoutInfo.visibleItemsInfo.lastOrNull()
            val totalItems = layoutInfo.totalItemsCount
            if (totalItems == 0) {
                // When there's no content, consider it at bottom to enable navigation
                true
            } else {
                // At bottom if last item is fully visible
                val isLastItemVisible = lastVisibleItem?.index == totalItems - 1
                val viewportEnd = layoutInfo.viewportEndOffset
                val isLastItemFullyVisible = if (lastVisibleItem != null) {
                    lastVisibleItem.offset + lastVisibleItem.size <= viewportEnd + 20
                } else false
                val result = isLastItemVisible && isLastItemFullyVisible
                // Debug logging
                if (isLastItemVisible) {
                    ireader.core.log.Log.debug { "isAtBottom check: lastIndex=${lastVisibleItem?.index}, totalItems=$totalItems, viewportEnd=$viewportEnd, itemEnd=${lastVisibleItem?.let { it.offset + it.size }}, result=$result" }
                }
                result
            }
        }}

        // Check if content doesn't fill the screen (both at top and bottom)
        // This means nested scroll won't work, so we need direct drag handling
        val contentDoesNotFillScreen by remember { derivedStateOf {
            val layoutInfo = lazyListState.layoutInfo
            val totalItems = layoutInfo.totalItemsCount
            if (totalItems == 0) {
                true // No content at all
            } else {
                // Check if all items fit within viewport
                val firstItem = layoutInfo.visibleItemsInfo.firstOrNull()
                val lastItem = layoutInfo.visibleItemsInfo.lastOrNull()
                val isFirstVisible = firstItem?.index == 0
                val isLastVisible = lastItem?.index == totalItems - 1
                val viewportHeight = layoutInfo.viewportEndOffset - layoutInfo.viewportStartOffset
                val contentHeight = layoutInfo.visibleItemsInfo.sumOf { it.size }
                isFirstVisible && isLastVisible && contentHeight <= viewportHeight
            }
        }}

        // Get chapter names for indicators
        val currentIndex = vm.currentChapterIndex
        val chapters = vm.stateChapters
        val hasPrevChapter = currentIndex > 0
        val hasNextChapter = currentIndex < chapters.lastIndex
        val prevChapterName = if (hasPrevChapter) chapters.getOrNull(currentIndex - 1)?.name else null
        val nextChapterName = if (hasNextChapter) chapters.getOrNull(currentIndex + 1)?.name else null

        // Use lazyValue for immediate UI updates during slider drag
        val refreshTriggerPx = with(androidx.compose.ui.platform.LocalDensity.current) { 80.dp.toPx() }

        Box(
            modifier = Modifier
                .padding(
                    top = vm.topMargin.lazyValue.dp,
                    bottom = vm.bottomMargin.lazyValue.dp,
                    start = vm.leftMargin.lazyValue.dp,
                    end = vm.rightMargin.lazyValue.dp
                )
        ) {
            MultiSwipeRefresh(
                modifier = Modifier.fillMaxSize(),
                state = swipeState,
                indicators = listOf(
                    ISwipeRefreshIndicator(
                        enable = isAtTop && hasPrevChapter,
                        alignment = Alignment.TopCenter,
                        indicator = { state, _ ->
                            // Only render indicator when there's actual drag activity
                            if (state.isSwipeInProgress || state.indicatorOffset > 0f) {
                                ArrowIndicator(
                                    icon = Icons.Default.KeyboardArrowUp,
                                    swipeRefreshState = state,
                                    refreshTriggerDistance = 80.dp,
                                    color = vm.textColor.value.toComposeColor(),
                                    chapterName = prevChapterName,
                                    isTop = true
                                )
                            }
                        },
                        onRefresh = {
                            onPrev()
                        }
                    ),
                    ISwipeRefreshIndicator(
                        enable = isAtBottom && hasNextChapter,
                        alignment = Alignment.BottomCenter,
                        onRefresh = {
                            onNext()
                        },
                        indicator = { state, _ ->
                            // Only render indicator when there's actual drag activity
                            if (state.isSwipeInProgress || state.indicatorOffset > 0f) {
                                ArrowIndicator(
                                    icon = Icons.Default.KeyboardArrowDown,
                                    swipeRefreshState = state,
                                    refreshTriggerDistance = 80.dp,
                                    color = vm.textColor.value.toComposeColor(),
                                    chapterName = nextChapterName,
                                    isTop = false
                                )
                            }
                        }
                    ),
                ),
            ) {
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            // Translate content based on swipe state for drag effect
                            translationY = swipeState.indicatorOffset * 0.3f
                        }
                ) {
                    // Capture constraints for use in tap zone detection
                    val screenWidth = constraints.maxWidth.toFloat()

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(contentDoesNotFillScreen, hasPrevChapter, hasNextChapter) {
                                // Combined gesture handling
                                if (contentDoesNotFillScreen && (hasPrevChapter || hasNextChapter)) {
                                    // When content doesn't fill screen, handle both tap and drag
                                    // But allow horizontal gestures to pass through for drawer
                                    awaitEachGesture {
                                        val down = awaitFirstDown(requireUnconsumed = false)

                                        var dragAmount = 0f
                                        var isDragging = false
                                        var isHorizontalGesture = false // Track if this is a horizontal gesture (drawer)
                                        val touchSlop = viewConfiguration.touchSlop

                                        // Track pointer movement
                                        var currentPosition = down.position
                                        var pointer = down

                                        while (true) {
                                            val event = awaitPointerEvent()
                                            val change = event.changes.firstOrNull { it.id == pointer.id }

                                            if (change == null || !change.pressed) {
                                                // Pointer released
                                                if (isDragging && !isHorizontalGesture) {
                                                    if (dragAmount > refreshTriggerPx && hasPrevChapter) {
                                                        onPrev()
                                                    } else if (dragAmount < -refreshTriggerPx && hasNextChapter) {
                                                        onNext()
                                                    }
                                                    swipeState.isSwipeInProgress = false
                                                    scope.launch {
                                                        swipeState.animateOffsetTo(0f)
                                                    }
                                                 } else if (!isDragging && !isHorizontalGesture) {
                                                     // It was a tap - use zone detection for Page mode
                                                     if (vm.readingMode.value == ReadingMode.Page) {
                                                         val tapZone = getTapZone(down.position.x, screenWidth)
                                                         when (tapZone) {
                                                             TapZone.LEFT -> onPrev()
                                                             TapZone.RIGHT -> onNext()
                                                             TapZone.CENTER -> debouncedToggleReaderMode()
                                                         }
                                                     } else {
                                                         debouncedToggleReaderMode()
                                                     }
                                                 }
                                                break
                                            }

                                            // If already determined to be horizontal gesture, don't process further
                                            if (isHorizontalGesture) {
                                                continue
                                            }

                                            val deltaY = change.position.y - currentPosition.y
                                            val deltaX = change.position.x - currentPosition.x
                                            currentPosition = change.position

                                            val totalDeltaY = change.position.y - down.position.y
                                            val totalDeltaX = change.position.x - down.position.x

                                            if (!isDragging) {
                                                // Check if movement exceeds touch slop
                                                val absX = kotlin.math.abs(totalDeltaX)
                                                val absY = kotlin.math.abs(totalDeltaY)

                                                if (absX > touchSlop || absY > touchSlop) {
                                                    // Determine gesture direction - if horizontal movement is dominant, let it pass through for drawer
                                                    if (absX > absY * 1.5f) {
                                                        // Horizontal gesture - likely drawer swipe, don't consume
                                                        isHorizontalGesture = true
                                                        continue
                                                    } else if (absY > touchSlop) {
                                                        // Vertical gesture - handle chapter navigation
                                                        isDragging = true
                                                        swipeState.isSwipeInProgress = true
                                                    }
                                                }
                                            }

                                            if (isDragging) {
                                                dragAmount += deltaY
                                                val dragMultiplier = 0.5f
                                                scope.launch {
                                                    swipeState.dispatchScrollDelta(deltaY * dragMultiplier)
                                                }
                                                change.consume()
                                            }
                                        }
                                    }
                                 } else {
                                     // Normal case - tap detection with zone support for Page mode
                                     detectTapGestures(
                                         onTap = { offset ->
                                             if (vm.readingMode.value == ReadingMode.Page) {
                                                 val tapZone = getTapZone(offset.x, screenWidth)
                                                 when (tapZone) {
                                                     TapZone.LEFT -> onPrev()
                                                     TapZone.RIGHT -> onNext()
                                                     TapZone.CENTER -> debouncedToggleReaderMode()
                                                 }
                                             } else {
                                                 debouncedToggleReaderMode()
                                             }
                                         }
                                     )
                                 }
                             }
                    ) {
                    // Use copy mode OR selectable mode for text selection
                    TextSelectionContainer(selectable = vm.copyModeActive || vm.selectableMode.value) {
                        when (vm.readingMode.value) {
                            ReadingMode.Page -> {
                                PagedReaderContent(
                                    vm = vm,
                                    lazyListState = lazyListState,
                                    onPrev = onPrev,
                                    onNext = onNext,
                                    toggleReaderMode = debouncedToggleReaderMode,
                                    onShowComments = onShowComments
                                )
                            }

                            ReadingMode.Continues -> {
                                ContinuousReaderContent(
                                    vm = vm,
                                    modifier = Modifier,
                                    lazyListState = lazyListState,
                                    onPrev = onPrev,
                                    onNext = onNext,
                                    toggleReaderMode = debouncedToggleReaderMode,
                                    onChapterShown = onChapterShown,
                                    onShowComments = onShowComments
                                )
                            }
                        }
                    }
                }
                ReaderHorizontalScreen(
                    interactionSource = interactionSource,
                    scrollState = scrollState,
                    vm = vm,
                    maxHeight = maxHeight,
                    onNext = onNext,
                    onPrev = onPrev,
                    toggleReaderMode = debouncedToggleReaderMode
                )
            }

        }

        // Copy Mode Overlay
        ireader.presentation.ui.reader.components.CopyModeOverlay(
            isActive = vm.copyModeActive,
            onDone = {
                val params = vm.finishCopyMode()
                if (params != null && onCopyModeDone != null) {
                    onCopyModeDone(params)
                }
            },
            onCancel = { vm.exitCopyMode() }
        )

    }

}
}

@Composable
private fun TextSelectionContainer(
    modifier: Modifier = Modifier,
    selectable: Boolean,
    content: @Composable () -> Unit,
) {
    when (selectable) {
        true -> SelectionContainer {
            content()
        }

        else -> {
            content()
        }
    }
}


@Composable
private fun ReaderHorizontalScreen(
    modifier: Modifier = Modifier,
    interactionSource: MutableInteractionSource,
    scrollState: ScrollState,
    vm: ReaderScreenViewModel,
    maxHeight: Float,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    toggleReaderMode: () -> Unit
) {
    val scope = rememberCoroutineScope()
    // RTL support: In RTL layouts, swap left/right tap zones for natural reading direction
    val layoutDirection = androidx.compose.ui.platform.LocalLayoutDirection.current
    val isRtl = layoutDirection == androidx.compose.ui.unit.LayoutDirection.Rtl

    if (!vm.verticalScrolling.value) {
        Row(modifier = Modifier.fillMaxSize()) {
            // Left zone: Previous in LTR, Next in RTL
            Box(
                Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null
                    ) {
                        scope.launch {
                            if (isRtl) {
                                // RTL: Left tap goes to next page/chapter
                                if (scrollState.value != scrollState.maxValue) {
                                    scrollState.scrollBy(maxHeight)
                                } else {
                                    onNext()
                                }
                            } else {
                                // LTR: Left tap goes to previous page/chapter
                                if (scrollState.value != 0) {
                                    scrollState.scrollBy(-maxHeight)
                                } else {
                                    onPrev()
                                }
                            }
                        }
                    }
            ) {
            }
            // Center zone: Toggle reader mode (same for both directions)
            Box(
                Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null
                    ) {
                        toggleReaderMode()
                    }
            ) {
            }
            // Right zone: Next in LTR, Previous in RTL
            Box(
                Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null
                    ) {
                        scope.launch {
                            if (isRtl) {
                                // RTL: Right tap goes to previous page/chapter
                                if (scrollState.value != 0) {
                                    scrollState.scrollBy(-maxHeight)
                                } else {
                                    onPrev()
                                }
                            } else {
                                // LTR: Right tap goes to next page/chapter
                                if (scrollState.value != scrollState.maxValue) {
                                    scrollState.scrollBy(maxHeight)
                                } else {
                                    onNext()
                                }
                            }
                        }
                    }
            ) {
            }
        }
    }
}




