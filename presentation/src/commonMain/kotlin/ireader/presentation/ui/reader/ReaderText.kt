package ireader.presentation.ui.reader

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.LocalPlatformContext
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.toUri
import ireader.core.source.model.ImageUrl
import ireader.core.source.model.Page
import ireader.core.source.model.Text
import ireader.domain.models.entities.Chapter
import ireader.domain.models.prefs.PreferenceValues
import ireader.domain.models.prefs.mapTextAlign
import ireader.domain.preferences.prefs.ReadingMode
import ireader.i18n.resources.Res
import ireader.i18n.resources.image
import ireader.presentation.core.toComposeColor
import ireader.presentation.core.toComposeFontFamily
import ireader.presentation.core.toComposeTextAlign
import ireader.presentation.imageloader.IImageLoader
import ireader.presentation.ui.component.list.scrollbars.IColumnScrollbar
import ireader.presentation.ui.component.list.scrollbars.ILazyColumnScrollbar
import ireader.presentation.ui.core.modifier.supportDesktopScroll
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.presentation.ui.reader.components.SelectableTranslatableText
import ireader.presentation.ui.reader.reverse_swip_refresh.ISwipeRefreshIndicator
import ireader.presentation.ui.reader.reverse_swip_refresh.MultiSwipeRefresh
import ireader.presentation.ui.reader.reverse_swip_refresh.SwipeRefreshState
import ireader.presentation.ui.reader.viewmodel.ReaderScreenViewModel
import kotlinx.coroutines.launch
import ireader.domain.utils.extensions.currentTimeToLong

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
) {
    val interactionSource = remember { MutableInteractionSource() }
    val scope = rememberCoroutineScope()
    
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
                false
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
        
        // Get chapter names for indicators
        val currentIndex = vm.currentChapterIndex
        val chapters = vm.stateChapters
        val hasPrevChapter = currentIndex > 0
        val hasNextChapter = currentIndex < chapters.lastIndex
        val prevChapterName = if (hasPrevChapter) chapters.getOrNull(currentIndex - 1)?.name else null
        val nextChapterName = if (hasNextChapter) chapters.getOrNull(currentIndex + 1)?.name else null

        // Use lazyValue for immediate UI updates during slider drag
        Box(
            modifier = Modifier.padding(
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
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            // Translate content based on swipe state for drag effect
                            translationY = swipeState.indicatorOffset * 0.3f
                        }
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onTap = {
                                    debouncedToggleReaderMode()
                                }
                            )
                        }
                ) {
                    TextSelectionContainer(selectable = vm.selectableMode.value) {
                        when (vm.readingMode.value) {
                            ReadingMode.Page -> {
                                PagedReaderText(
                                    interactionSource = interactionSource,
                                    scrollState = scrollState,
                                    lazyListState = lazyListState,
                                    vm = vm,
                                    maxHeight = maxHeight,
                                    onNext = onNext,
                                    onPrev = onPrev,
                                    toggleReaderMode = debouncedToggleReaderMode,
                                    onShowComments = onShowComments
                                )
                            }

                            ReadingMode.Continues -> {
                                ContinuesReaderPage(
                                    interactionSource = interactionSource,
                                    scrollState = lazyListState,
                                    vm = vm,
                                    maxHeight = maxHeight,
                                    onNext = onNext,
                                    onPrev = onPrev,
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
private fun PagedReaderText(
    modifier: Modifier = Modifier,
    interactionSource: MutableInteractionSource,
    scrollState: ScrollState,
    lazyListState: LazyListState,
    vm: ReaderScreenViewModel,
    maxHeight: Float,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    toggleReaderMode: () -> Unit,
    onShowComments: (chapter: Chapter) -> Unit,
) {
    // Use optimized LazyColumn-based implementation for better performance
    OptimizedPagedReaderText(
        modifier = modifier,
        interactionSource = interactionSource,
        lazyListState = lazyListState,
        vm = vm,
        maxHeight = maxHeight,
        onPrev = onPrev,
        onNext = onNext,
        toggleReaderMode = toggleReaderMode,
        onShowComments = onShowComments
    )
}

/**
 * Optimized paged reader that uses LazyColumn instead of Column with verticalScroll.
 * This provides significant performance improvements for long chapters by:
 * 1. Only composing visible paragraphs (lazy loading)
 * 2. Recycling paragraph composables as user scrolls
 * 3. Reducing memory usage for chapters with many paragraphs
 * 4. Chapter end UI with navigation and comments section
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OptimizedPagedReaderText(
    modifier: Modifier = Modifier,
    interactionSource: MutableInteractionSource,
    lazyListState: LazyListState,
    vm: ReaderScreenViewModel,
    maxHeight: Float,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    toggleReaderMode: () -> Unit,
    onShowComments: (chapter: Chapter) -> Unit,
) {
    val scope = rememberCoroutineScope()
    
    // Track the last chapter ID to detect chapter changes
    var lastScrolledChapterId by remember { mutableStateOf<Long?>(null) }
    
    // Collect state from ViewModel's StateFlow for reliable observation
    val readerState by vm.state.collectAsState()
    val successState = readerState as? ireader.presentation.ui.reader.viewmodel.ReaderState.Success
    val currentChapterId = successState?.currentChapter?.id
    
    // Single LaunchedEffect to handle all chapter navigation scrolling
    // Key on both chapter ID and scrollToEnd flag to catch all state changes
    LaunchedEffect(key1 = currentChapterId, key2 = successState?.scrollToEndOnChapterChange) {
        val chapterId = currentChapterId ?: return@LaunchedEffect
        val shouldScrollToEnd = successState?.scrollToEndOnChapterChange ?: false
        val contentSize = successState?.currentChapter?.content?.size ?: 0
        
        // Skip if same chapter and not a scroll-to-end request
        if (chapterId == lastScrolledChapterId && !shouldScrollToEnd) return@LaunchedEffect
        
        if (shouldScrollToEnd && contentSize > 0) {
            // Previous chapter navigation - scroll to end (void space)
            // Structure: header (index 0) + content items (indices 1..N) + void (index N+1)
            val voidIndex = contentSize + 1
            
            // Quick check - try to scroll immediately if items are ready
            val immediateTotal = lazyListState.layoutInfo.totalItemsCount
            if (immediateTotal > voidIndex) {
                lazyListState.scrollToItem(voidIndex)
                lastScrolledChapterId = chapterId
                vm.scrollToEndOnChapterChange = false
                return@LaunchedEffect
            }
            
            // Wait briefly for LazyColumn to be populated, then scroll
            repeat(10) {
                kotlinx.coroutines.delay(16) // ~1 frame
                val totalItems = lazyListState.layoutInfo.totalItemsCount
                if (totalItems > voidIndex) {
                    lazyListState.scrollToItem(voidIndex)
                    lastScrolledChapterId = chapterId
                    vm.scrollToEndOnChapterChange = false
                    return@LaunchedEffect
                }
            }
            // Fallback: scroll to last available item
            val totalItems = lazyListState.layoutInfo.totalItemsCount
            if (totalItems > 0) {
                lazyListState.scrollToItem(totalItems - 1)
            }
            vm.scrollToEndOnChapterChange = false
            lastScrolledChapterId = chapterId
        } else if (chapterId != lastScrolledChapterId) {
            // Next chapter navigation - scroll to start (top)
            // Only scroll to top if it's a NEW chapter (not same chapter)
            lazyListState.scrollToItem(0)
            lastScrolledChapterId = chapterId
        }
    }
    
    // Track scroll progress for reading time estimation
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
    
    // Auto-scroll logic for optimized Page mode
    LaunchedEffect(key1 = vm.autoScrollMode, key2 = vm.autoScrollOffset.value, key3 = vm.stateChapter?.id) {
        if (vm.autoScrollMode) {
            while (vm.autoScrollMode) {
                val scrollAmount = vm.autoScrollOffset.value.toFloat()
                
                // Check if we've reached the end before scrolling
                val layoutInfo = lazyListState.layoutInfo
                val lastVisibleItem = layoutInfo.visibleItemsInfo.lastOrNull()
                val isAtEnd = lastVisibleItem?.index == layoutInfo.totalItemsCount - 1
                
                if (isAtEnd) {
                    // Stop auto-scroll before advancing to prevent double-advance
                    vm.autoScrollMode = false
                    // Auto-advance to next chapter
                    onNext()
                    break
                }
                
                lazyListState.scrollBy(scrollAmount)
                
                // Delay based on interval (smooth scrolling)
                kotlinx.coroutines.delay(16L) // ~60fps
            }
        }
    }
    
    // Memoize content to prevent unnecessary recomposition
    // Include showTranslatedContent in key so content updates when translation is toggled
    val content = remember(
        vm.stateChapter?.id, 
        vm.translationViewModel.translationState.hasTranslation,
        vm.showTranslatedContent.value
    ) {
        vm.getCurrentContent()
    }
    
    // Check chapter navigation availability
    val currentIndex = vm.currentChapterIndex
    val chapters = vm.stateChapters
    val hasPrevChapter = currentIndex > 0
    val hasNextChapter = currentIndex < chapters.lastIndex
    val prevChapterName = if (hasPrevChapter) chapters.getOrNull(currentIndex - 1)?.name else null
    val nextChapterName = if (hasNextChapter) chapters.getOrNull(currentIndex + 1)?.name else null

    val prevChapter = if (hasPrevChapter) chapters.getOrNull(currentIndex - 1) else null
    
    Box(modifier = Modifier.fillMaxSize()) {
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

                
                // Chapter content items
                items(
                    count = content.size,
                    key = { index ->
                        // Stable key combining chapter ID and paragraph index for better item reuse
                        "${vm.stateChapter?.id ?: 0}-content-$index"
                    }
                ) { index ->
                    // Use remember to cache the page reference
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
                
                // Void space at end of chapter with comments (only shown when content exists)
                // Users must read the chapter before seeing the "chapter complete" section
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

/**
 * Legacy paged reader using Column with verticalScroll.
 * Kept for compatibility - use OptimizedPagedReaderText for better performance.
 */
@Composable
private fun LegacyPagedReaderText(
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
    
    // Track scroll progress for reading time estimation in Page mode
    LaunchedEffect(key1 = scrollState.value, key2 = scrollState.maxValue, key3 = vm.stateChapter?.id) {
        if (scrollState.maxValue > 0 && !vm.isLoading) {
            val scrollProgress = scrollState.value.toFloat() / scrollState.maxValue.toFloat()
            vm.updateReadingTimeEstimation(scrollProgress)
        }
    }
    
    // Auto-scroll logic for Page mode
    LaunchedEffect(key1 = vm.autoScrollMode, key2 = vm.autoScrollOffset.value, key3 = vm.stateChapter?.id) {
        if (vm.autoScrollMode) {
            while (vm.autoScrollMode) {
                val scrollAmount = vm.autoScrollOffset.value.toFloat()
                
                // Check if we've reached the end before scrolling
                if (scrollState.value >= scrollState.maxValue) {
                    // Stop auto-scroll before advancing to prevent double-advance
                    vm.autoScrollMode = false
                    // Auto-advance to next chapter
                    onNext()
                    break
                }
                
                scrollState.scrollBy(scrollAmount)
                
                // Delay based on interval (smooth scrolling)
                kotlinx.coroutines.delay(16L) // ~60fps
            }
        }
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        // Use lazyValue for immediate UI updates during slider drag
        IColumnScrollbar(
            state = scrollState,
            padding = if (vm.scrollIndicatorPadding.lazyValue < 0) 0.dp else vm.scrollIndicatorPadding.lazyValue.dp,
            thickness = if (vm.scrollIndicatorWith.lazyValue < 0) 0.dp else vm.scrollIndicatorWith.lazyValue.dp,
            enabled = vm.showScrollIndicator.value,
            thumbColor = vm.unselectedScrollBarColor.value.toComposeColor(),
            thumbSelectedColor = vm.selectedScrollBarColor.value.toComposeColor(),
            selectionMode = vm.isScrollIndicatorDraggable.value,
            rightSide = vm.scrollIndicatorAlignment.value == PreferenceValues.PreferenceTextAlignment.Right
        ) {
            // Memoize content to prevent unnecessary recomposition
            // Include showTranslatedContent in key so content updates when translation is toggled
            val content = remember(
                vm.stateChapter?.id, 
                vm.translationViewModel.translationState.hasTranslation,
                vm.showTranslatedContent.value
            ) {
                vm.getCurrentContent()
            }
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(top = 32.dp)
            ) {
                content.forEachIndexed { index, text ->
                    // Use key to help Compose identify items and skip unchanged ones
                    androidx.compose.runtime.key(index, text) {
                        MainText(
                            modifier = modifier,
                            index = index,
                            page = text,
                            vm = vm
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MainText(
    modifier: Modifier,
    index: Int,
    page: Page,
    vm: ReaderScreenViewModel
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    val context = LocalPlatformContext.current
    
    // Use key to prevent unnecessary recomposition when only index changes
    androidx.compose.runtime.key(page) {
        when (page) {
            is Text -> {
                // Memoize text styling parameters to prevent recomposition on unrelated changes
                val textStyleParams = remember(
                    vm.fontSize.lazyValue,
                    vm.lineHeight.lazyValue,
                    vm.betweenLetterSpaces.lazyValue,
                    vm.textWeight.lazyValue,
                    vm.paragraphsIndent.lazyValue,
                    vm.textAlignment.value,
                    vm.textColor.value,
                    vm.font?.value,
                    vm.fontVersion
                ) {
                    TextStyleParams(
                        fontSize = vm.fontSize.lazyValue,
                        lineHeight = vm.lineHeight.lazyValue,
                        letterSpacing = vm.betweenLetterSpaces.lazyValue,
                        fontWeight = vm.textWeight.lazyValue,
                        paragraphIndent = vm.paragraphsIndent.lazyValue,
                        textAlignment = vm.textAlignment.value,
                        textColor = vm.textColor.value.toComposeColor(),
                        fontFamily = vm.font?.value?.fontFamily?.toComposeFontFamily()
                    )
                }
                StyleTextOptimized(modifier, vm, index, page, vm.bionicReadingMode.value, textStyleParams)
            }
            is ImageUrl -> {
                val isLoading = remember { mutableStateOf(false) }
                Box(contentAlignment = Alignment.Center) {
                    IImageLoader(
                        modifier = Modifier
                            .fillMaxWidth()
                            .requiredHeight(500.dp),
                        model = ImageRequest.Builder(context=context).data(page.url.toUri()).diskCachePolicy(CachePolicy.DISABLED).build(),
                        contentDescription = localizeHelper.localize(Res.string.image),
                        contentScale = ContentScale.FillWidth,
                        onLoading = { isLoading.value = true },
                        onError = { isLoading.value = false },
                        onSuccess = { isLoading.value = false },
                    )
                    if (isLoading.value) {
                        androidx.compose.material3.CircularProgressIndicator()
                    }
                }
            }
            else -> {}
        }
    }
}

/**
 * Data class to hold text styling parameters for memoization.
 * This prevents recomposition when unrelated ViewModel properties change.
 * Marked as @Stable to help Compose skip recomposition when values are equal.
 */
@androidx.compose.runtime.Stable
private data class TextStyleParams(
    val fontSize: Int,
    val lineHeight: Int,
    val letterSpacing: Int,
    val fontWeight: Int,
    val paragraphIndent: Int,
    val textAlignment: PreferenceValues.PreferenceTextAlignment,
    val textColor: Color,
    val fontFamily: androidx.compose.ui.text.font.FontFamily?
) {
    // Pre-compute TextStyle to avoid creating new objects during recomposition
    val textStyle: androidx.compose.ui.text.TextStyle by lazy {
        androidx.compose.ui.text.TextStyle(
            fontSize = fontSize.sp,
            lineHeight = lineHeight.sp,
            letterSpacing = letterSpacing.sp,
            fontWeight = FontWeight(fontWeight),
            fontFamily = fontFamily,
            color = textColor,
            textAlign = mapTextAlign(textAlignment).toComposeTextAlign()
        )
    }
}

@Composable
private fun StyleText(
    modifier: Modifier,
    vm: ReaderScreenViewModel,
    index: Int,
    page: Text,
    enableBioReading: Boolean
) {
    // Cache the content to avoid race conditions from multiple getCurrentContent() calls
    // Include showTranslatedContent in key so content updates when translation is toggled
    val currentContent = remember(
        vm.stateChapter?.id, 
        vm.translationViewModel.translationState.hasTranslation,
        vm.showTranslatedContent.value
    ) {
        vm.getCurrentContent()
    }
    val isLastIndex = index == currentContent.lastIndex
    
    val originalText = setText(
        text = page.text,
        index = index,
        isLast = isLastIndex,
        topContentPadding = vm.topContentPadding.lazyValue,
        contentPadding = vm.distanceBetweenParagraphs.lazyValue,
        bottomContentPadding = vm.bottomContentPadding.lazyValue
    )
    
    // Check if bilingual mode is enabled and we have a translation for this paragraph
    val bilingualModeEnabled = vm.bilingualModeEnabled.value
    val translatedText = vm.getTranslationForParagraph(index)
    
    if (bilingualModeEnabled && translatedText != null) {
        // Display bilingual text
        val bilingualMode = if (vm.bilingualModeLayout.value == 0) {
            ireader.presentation.ui.reader.components.BilingualMode.SIDE_BY_SIDE
        } else {
            ireader.presentation.ui.reader.components.BilingualMode.PARAGRAPH_BY_PARAGRAPH
        }
        
        ireader.presentation.ui.reader.components.BilingualText(
            originalText = originalText,
            translatedText = translatedText,
            mode = bilingualMode,
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = vm.paragraphsIndent.lazyValue.dp),
            fontSize = vm.fontSize.lazyValue.sp,
            fontFamily = remember(vm.font?.value, vm.fontVersion) { 
                vm.font?.value?.fontFamily?.toComposeFontFamily() 
            },
            textAlign = mapTextAlign(vm.textAlignment.value).toComposeTextAlign(),
            originalColor = vm.textColor.value.toComposeColor(),
            translatedColor = vm.textColor.value.toComposeColor().copy(alpha = 0.9f),
            lineHeight = vm.lineHeight.lazyValue.sp,
            letterSpacing = vm.betweenLetterSpaces.lazyValue.sp,
            fontWeight = FontWeight(vm.textWeight.lazyValue)
        )
    } else if (enableBioReading) {
        androidx.compose.material3.Text(
            text = buildAnnotatedString {
                var currentCursorIndex = 0
                originalText.split(" ")
                    .forEach { s ->
                        s.forEachIndexed { charIndex, c ->
                            if (charIndex <= (s.length / 2)) {
                                withStyle(
                                    style = SpanStyle(
                                        fontWeight = FontWeight.SemiBold
                                    )
                                ) {

                                    append(c)
                                }
                            } else {
                                withStyle(
                                    style = SpanStyle(
                                        fontWeight = FontWeight.Light
                                    )
                                ) {

                                    append(c)
                                }
                            }

                        }
                        append(" ")

                    }

            },
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = vm.paragraphsIndent.lazyValue.dp),
            fontSize = vm.fontSize.lazyValue.sp,
            fontFamily = remember(vm.font?.value, vm.fontVersion) { 
                vm.font?.value?.fontFamily?.toComposeFontFamily() 
            },
            textAlign = mapTextAlign(vm.textAlignment.value).toComposeTextAlign(),
            color = vm.textColor.value.toComposeColor(),
            lineHeight = vm.lineHeight.lazyValue.sp,
            letterSpacing = vm.betweenLetterSpaces.lazyValue.sp,
            fontWeight = FontWeight(vm.textWeight.lazyValue),
        )
    } else {
        // Normal text rendering
        SelectableTranslatableText(
            text = originalText,
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = vm.paragraphsIndent.lazyValue.dp),
            fontSize = vm.fontSize.lazyValue.sp,
            fontFamily = remember(vm.font?.value, vm.fontVersion) { 
                vm.font?.value?.fontFamily?.toComposeFontFamily() 
            },
            textAlign = mapTextAlign(vm.textAlignment.value).toComposeTextAlign(),
            color = vm.textColor.value.toComposeColor(),
            lineHeight = vm.lineHeight.lazyValue.sp,
            letterSpacing = vm.betweenLetterSpaces.lazyValue.sp,
            fontWeight = FontWeight(vm.textWeight.lazyValue),
            selectable = vm.selectableMode.value,
            paragraphTranslationEnabled = vm.paragraphTranslationEnabled.value,
            onTranslateRequest = { selectedText ->
                vm.showParagraphTranslation(selectedText)
            }
        )
    }

}

/**
 * Optimized StyleText that uses pre-computed TextStyleParams to minimize recomposition.
 */
@Composable
private fun StyleTextOptimized(
    modifier: Modifier,
    vm: ReaderScreenViewModel,
    index: Int,
    page: Text,
    enableBioReading: Boolean,
    styleParams: TextStyleParams
) {
    // Cache the content to avoid race conditions from multiple getCurrentContent() calls
    // Include showTranslatedContent in key so content updates when translation is toggled
    val currentContent = remember(
        vm.stateChapter?.id, 
        vm.translationViewModel.translationState.hasTranslation,
        vm.showTranslatedContent.value
    ) {
        vm.getCurrentContent()
    }
    val isLastIndex = index == currentContent.lastIndex
    
    // Use lazyValue for immediate UI updates during slider drag
    val originalText = remember(
        page.text, 
        index, 
        isLastIndex, 
        vm.topContentPadding.lazyValue,
        vm.distanceBetweenParagraphs.lazyValue,
        vm.bottomContentPadding.lazyValue
    ) {
        setText(
            text = page.text,
            index = index,
            isLast = isLastIndex,
            topContentPadding = vm.topContentPadding.lazyValue,
            contentPadding = vm.distanceBetweenParagraphs.lazyValue,
            bottomContentPadding = vm.bottomContentPadding.lazyValue
        )
    }
    
    // Check if bilingual mode is enabled and we have a translation for this paragraph
    val bilingualModeEnabled = vm.bilingualModeEnabled.value
    val translatedText = vm.getTranslationForParagraph(index)
    
    if (bilingualModeEnabled && translatedText != null) {
        // Display bilingual text
        val bilingualMode = if (vm.bilingualModeLayout.value == 0) {
            ireader.presentation.ui.reader.components.BilingualMode.SIDE_BY_SIDE
        } else {
            ireader.presentation.ui.reader.components.BilingualMode.PARAGRAPH_BY_PARAGRAPH
        }
        
        ireader.presentation.ui.reader.components.BilingualText(
            originalText = originalText,
            translatedText = translatedText,
            mode = bilingualMode,
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = styleParams.paragraphIndent.dp),
            fontSize = styleParams.fontSize.sp,
            fontFamily = styleParams.fontFamily,
            textAlign = mapTextAlign(styleParams.textAlignment).toComposeTextAlign(),
            originalColor = styleParams.textColor,
            translatedColor = styleParams.textColor.copy(alpha = 0.9f),
            lineHeight = styleParams.lineHeight.sp,
            letterSpacing = styleParams.letterSpacing.sp,
            fontWeight = FontWeight(styleParams.fontWeight)
        )
    } else if (enableBioReading) {
        // Memoize the annotated string to avoid rebuilding on every recomposition
        val bionicText = remember(originalText, styleParams.fontWeight) {
            buildAnnotatedString {
                originalText.split(" ").forEach { s ->
                    s.forEachIndexed { charIndex, c ->
                        if (charIndex <= (s.length / 2)) {
                            withStyle(style = SpanStyle(fontWeight = FontWeight.SemiBold)) {
                                append(c)
                            }
                        } else {
                            withStyle(style = SpanStyle(fontWeight = FontWeight.Light)) {
                                append(c)
                            }
                        }
                    }
                    append(" ")
                }
            }
        }
        
        Text(
            text = bionicText,
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = styleParams.paragraphIndent.dp),
            fontSize = styleParams.fontSize.sp,
            fontFamily = styleParams.fontFamily,
            textAlign = mapTextAlign(styleParams.textAlignment).toComposeTextAlign(),
            color = styleParams.textColor,
            lineHeight = styleParams.lineHeight.sp,
            letterSpacing = styleParams.letterSpacing.sp,
            fontWeight = FontWeight(styleParams.fontWeight),
        )
    } else {
        // Normal text rendering with optimized parameters
        SelectableTranslatableText(
            text = originalText,
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = styleParams.paragraphIndent.dp),
            fontSize = styleParams.fontSize.sp,
            fontFamily = styleParams.fontFamily,
            textAlign = mapTextAlign(styleParams.textAlignment).toComposeTextAlign(),
            color = styleParams.textColor,
            lineHeight = styleParams.lineHeight.sp,
            letterSpacing = styleParams.letterSpacing.sp,
            fontWeight = FontWeight(styleParams.fontWeight),
            selectable = vm.selectableMode.value,
            paragraphTranslationEnabled = vm.paragraphTranslationEnabled.value,
            onTranslateRequest = { selectedText ->
                vm.showParagraphTranslation(selectedText)
            }
        )
    }
}
/**
 * Continues reading mode with infinite scroll and void space between chapters.
 * Features:
 * - Seamless chapter transitions with dark void space
 * - Bouncing effect at chapter boundaries
 * - Automatic chapter state updates
 * - Optimized for performance on old devices
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ContinuesReaderPage(
    modifier: Modifier = Modifier,
    interactionSource: MutableInteractionSource,
    scrollState: LazyListState,
    vm: ReaderScreenViewModel,
    maxHeight: Float,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    toggleReaderMode: () -> Unit,
    onChapterShown: (chapter: Chapter) -> Unit,
    onShowComments: (chapter: Chapter) -> Unit,
) {
    val scope = rememberCoroutineScope()
    
    // Track current chapter for state updates
    var lastChapterId: Chapter? by remember { mutableStateOf(null) }
    
    LaunchedEffect(key1 = lastChapterId) {
        lastChapterId?.let { chapter ->
            onChapterShown(chapter)
            // Chapter index is managed by the ViewModel state
        }
    }
    
    // Scroll to appropriate position when navigating to previous chapter
    LaunchedEffect(key1 = vm.stateChapter?.id, key2 = vm.scrollToEndOnChapterChange) {
        if (vm.scrollToEndOnChapterChange) {
            // For previous chapter navigation - scroll to end (void space)
            val contentSize = vm.stateChapter?.content?.size ?: 0
            if (contentSize > 0) {
                // Structure: header (1) + content items (N) + void (1)
                val voidIndex = contentSize + 1
                
                // Wait a bit for the LazyColumn to be populated
                kotlinx.coroutines.delay(150)
                
                // Scroll to the void (end of chapter)
                val totalItems = scrollState.layoutInfo.totalItemsCount
                if (totalItems > voidIndex) {
                    scrollState.scrollToItem(voidIndex)
                } else if (totalItems > 0) {
                    scrollState.scrollToItem(totalItems - 1)
                }
            }
            // Reset the flag
            vm.scrollToEndOnChapterChange = false
        }
    }
    
    // Build items for infinite scroll with void spaces
    val items = remember(vm.chapterShell, vm.stateChapters, vm.currentChapterIndex) {
        buildInfiniteReaderItems(
            chapterShell = vm.chapterShell,
            allChapters = vm.stateChapters,
            currentIndex = vm.currentChapterIndex
        )
    }
    
    // Track visible item to update chapter state
    val visibleItemInfo = remember { derivedStateOf { scrollState.layoutInfo } }
    
    LaunchedEffect(key1 = visibleItemInfo.value.visibleItemsInfo.firstOrNull()?.key) {
        runCatching {
            val visibleKey = scrollState.layoutInfo.visibleItemsInfo.firstOrNull()?.key?.toString()
            if (visibleKey != null) {
                // Extract chapter ID from key (format: "content-{chapterId}-{index}" or "void-{chapterId}")
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
    
    // Track scroll progress for reading time estimation
    LaunchedEffect(key1 = visibleItemInfo.value, key2 = vm.stateChapter?.id) {
        val layoutInfo = scrollState.layoutInfo
        val totalItems = layoutInfo.totalItemsCount
        val firstVisibleItem = layoutInfo.visibleItemsInfo.firstOrNull()?.index ?: 0
        
        if (totalItems > 0 && !vm.isLoading) {
            val scrollProgress = firstVisibleItem.toFloat() / totalItems.toFloat()
            vm.updateReadingTimeEstimation(scrollProgress)
        }
    }
    
    // Auto-scroll logic for Continues mode
    LaunchedEffect(key1 = vm.autoScrollMode, key2 = vm.autoScrollOffset.value, key3 = vm.stateChapter?.id) {
        if (vm.autoScrollMode) {
            while (vm.autoScrollMode) {
                val scrollAmount = vm.autoScrollOffset.value.toFloat()
                
                // Check if we've reached the end before scrolling
                val layoutInfo = scrollState.layoutInfo
                val lastVisibleItem = layoutInfo.visibleItemsInfo.lastOrNull()
                val isAtEnd = lastVisibleItem?.index == layoutInfo.totalItemsCount - 1
                
                if (isAtEnd) {
                    // Stop auto-scroll before advancing to prevent double-advance
                    vm.autoScrollMode = false
                    // Auto-advance to next chapter
                    onNext()
                    break
                }
                
                scrollState.scrollBy(scrollAmount)
                
                // Delay based on interval (smooth scrolling)
                kotlinx.coroutines.delay(16L) // ~60fps
            }
        }
    }
    
    // Check chapter navigation availability
    val currentIndex = vm.currentChapterIndex
    val chapters = vm.stateChapters
    val hasPrevChapter = currentIndex > 0
    val hasNextChapter = currentIndex < chapters.lastIndex
    val prevChapter = if (hasPrevChapter) chapters.getOrNull(currentIndex - 1) else null

    // Use lazyValue for immediate UI updates during slider drag
    ILazyColumnScrollbar(
        listState = scrollState,
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
            state = scrollState,
        ) {
            // Render each chapter with void space between them
            // The pull-to-previous is handled by MultiSwipeRefresh in the parent
            vm.chapterShell.forEachIndexed { shellIndex, chapter ->
                val isFirstChapter = shellIndex == 0
                val isLastChapter = shellIndex == vm.chapterShell.lastIndex
                // Chapter content
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
                
                // Void space at end of chapter (only shown when content exists)
                // Users must read the chapter before seeing the "chapter complete" section
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

/**
 * Build items for infinite scroll with void spaces between chapters.
 */
private fun buildInfiniteReaderItems(
    chapterShell: List<Chapter>,
    allChapters: List<Chapter>,
    currentIndex: Int
): List<Any> {
    if (chapterShell.isEmpty()) return emptyList()
    
    val items = mutableListOf<Any>()
    val prevChapter = if (currentIndex > 0) allChapters.getOrNull(currentIndex - 1) else null
    
    // Add prev indicator
    items.add("prev-indicator" to prevChapter)
    
    chapterShell.forEachIndexed { shellIndex, chapter ->
        items.add("header" to chapter)
        chapter.content?.forEachIndexed { index, page ->
            items.add(Triple(chapter.id, index, page))
        }
        items.add("void" to chapter)
    }
    
    return items
}

/**
 * Overscroll indicator that appears when user drags at the top.
 * This is shown as an overlay, not as a list item.
 * @param dragOffset The current drag offset (0 = no drag, positive = dragging down)
 */
@Composable
private fun OverscrollPrevIndicator(
    prevChapter: Chapter?,
    textColor: Color,
    dragOffset: Float,
    modifier: Modifier = Modifier
) {
    // Only show when dragging (offset > threshold)
    val showThreshold = 20f
    val maxOffset = 120f
    val progress = ((dragOffset - showThreshold) / (maxOffset - showThreshold)).coerceIn(0f, 1f)
    
    if (progress > 0f) {
        val infiniteTransition = rememberInfiniteTransition(label = "bounce")
        val bounceOffset by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 6f,
            animationSpec = infiniteRepeatable(
                animation = tween(600, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "bounceOffset"
        )
        
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height((progress * 100).dp)
                .alpha(progress)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF0A0A0A),
                            Color.Transparent
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .graphicsLayer { translationY = bounceOffset }
                    .padding(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowUp,
                    contentDescription = null,
                    tint = textColor.copy(alpha = 0.6f * progress),
                    modifier = Modifier.size(24.dp)
                )
                
                if (prevChapter != null && progress > 0.5f) {
                    Text(
                        text = "Release for previous",
                        color = textColor.copy(alpha = 0.5f * progress),
                        fontSize = 11.sp
                    )
                    Text(
                        text = prevChapter.name,
                        color = textColor.copy(alpha = 0.7f * progress),
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                } else if (prevChapter == null && progress > 0.3f) {
                    Text(
                        text = "First chapter",
                        color = textColor.copy(alpha = 0.4f * progress),
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}


/**
 * Clean void space between chapters with smooth animations.
 * Colors are fully based on reader text/background colors for consistency.
 */
@Composable
private fun ChapterVoidSpace(
    chapter: Chapter,
    isLast: Boolean,
    textColor: Color,
    backgroundColor: Color = Color.Black,
    onShowComments: () -> Unit,
    onNextChapter: () -> Unit,
    isLoading: Boolean = false,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "void")
    
    // Smooth floating animation
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float"
    )
    
    // Breathing glow effect
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )
    
    // Pulse for the continue button
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    
    // Use reader colors directly - no hardcoded colors
    val contentTextColor = textColor
    val contentTextColorMuted = textColor.copy(alpha = 0.6f)
    val contentTextColorSubtle = textColor.copy(alpha = 0.35f)
    val accentColor = textColor.copy(alpha = 0.8f)
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(380.dp)
            .background(backgroundColor)
    ) {
        // Subtle glow orb using text color
        Box(
            modifier = Modifier
                .size(150.dp)
                .align(Alignment.Center)
                .graphicsLayer { translationY = floatOffset }
                .alpha(glowAlpha * 0.3f)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            textColor.copy(alpha = 0.15f),
                            textColor.copy(alpha = 0.05f),
                            Color.Transparent
                        )
                    )
                )
        )
        
        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Top decorative line
            Box(
                modifier = Modifier
                    .width(50.dp)
                    .height(1.dp)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color.Transparent,
                                contentTextColorSubtle,
                                Color.Transparent
                            )
                        )
                    )
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Floating chapter completion content (no card, just text)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .graphicsLayer { translationY = floatOffset }
                    .padding(horizontal = 16.dp)
            ) {
                // Checkmark icon
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(28.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "CHAPTER COMPLETE",
                    color = contentTextColorMuted,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 3.sp
                )
                
                Spacer(modifier = Modifier.height(10.dp))
                
                Text(
                    text = chapter.name,
                    color = contentTextColor.copy(alpha = 0.9f),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            Spacer(modifier = Modifier.height(28.dp))
            
            // Comment button - uses reader text color
            OutlinedButton(
                onClick = onShowComments,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = contentTextColor
                ),
                border = BorderStroke(1.dp, contentTextColorSubtle),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .height(44.dp)
                    .widthIn(min = 150.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.RateReview,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = contentTextColorSubtle,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "View Comments",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = contentTextColorSubtle,
                )
            }
            
            // Loading indicator when fetching next chapter
            if (isLoading) {
                Spacer(modifier = Modifier.height(24.dp))
                androidx.compose.material3.CircularProgressIndicator(
                    modifier = Modifier.size(28.dp),
                    color = accentColor,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Loading...",
                    color = contentTextColorMuted,
                    fontSize = 12.sp
                )
            }
            
            // Next chapter section
            if (!isLast && !isLoading) {
                Spacer(modifier = Modifier.height(32.dp))
                
                // Decorative dots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    repeat(3) { index ->
                        val dotAlpha by infiniteTransition.animateFloat(
                            initialValue = 0.3f,
                            targetValue = 0.8f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(600, delayMillis = index * 150, easing = FastOutSlowInEasing),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "dot$index"
                        )
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .alpha(dotAlpha)
                                .background(contentTextColorMuted, RoundedCornerShape(2.5.dp))
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Continue button with pulse
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable { onNextChapter() }
                        .graphicsLayer { 
                            scaleX = pulseScale
                            scaleY = pulseScale
                        }
                        .padding(12.dp)
                ) {
                    Text(
                        text = "Continue Reading",
                        color = contentTextColorMuted,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Next chapter",
                        tint = accentColor,
                        modifier = Modifier
                            .size(28.dp)
                            .graphicsLayer { translationY = floatOffset * 0.3f }
                    )
                }
            } else if (isLast) {
                Spacer(modifier = Modifier.height(28.dp))
                
                // End of book indicator
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.graphicsLayer { translationY = floatOffset }
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "THE END",
                        color = contentTextColorMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 3.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "You've completed this story",
                        color = contentTextColorSubtle,
                        fontSize = 12.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Bottom decorative line
            Box(
                modifier = Modifier
                    .width(50.dp)
                    .height(1.dp)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color.Transparent,
                                contentTextColorSubtle,
                                Color.Transparent
                            )
                        )
                    )
            )
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



/**
 * Get highlight color based on background color
 * Returns a color that is visible in both light and dark themes
 * 
 * Requirements: 5.2 - Highlight must be visible in light and dark themes
 */
private fun getHighlightColor(backgroundColor: Color): Color {
    // Calculate relative luminance using the standard formula
    // https://www.w3.org/TR/WCAG20/#relativeluminancedef
    val luminance = (0.299 * backgroundColor.red + 
                    0.587 * backgroundColor.green + 
                    0.114 * backgroundColor.blue)
    
    // Return appropriate highlight color based on background luminance
    return if (luminance > 0.5) {
        // Light background (e.g., white, light gray, beige)
        // Use a warm yellow/amber highlight with moderate transparency
        Color(0xFFFFC107).copy(alpha = 0.45f) // Amber 500 with 45% opacity
    } else {
        // Dark background (e.g., black, dark gray, dark blue)
        // Use a brighter yellow highlight with less transparency for better visibility
        Color(0xFFFFEB3B).copy(alpha = 0.35f) // Yellow 500 with 35% opacity
    }
}

private fun setText(
    text: String,
    index: Int,
    isLast: Boolean,
    topContentPadding: Int,
    bottomContentPadding: Int,
    contentPadding: Int,
): String {
    // Handle null or empty text safely
    if (text.isEmpty()) {
        return ""
    }
    
    val stringBuilder = StringBuilder()
    
    // Add top padding only if this is the first chunk (index 0)
    if (index == 0 && topContentPadding > 0) {
        stringBuilder.append("\n".repeat(topContentPadding.coerceAtLeast(0)))
    }
    
    // Add the text content without any leading newlines for the first paragraph
    val cleanedText = if (index == 0) {
        // For the first paragraph, remove any leading newlines
        text.trimStart()
    } else {
        // For other paragraphs, keep as is
        text
    }
    
    stringBuilder.append(cleanedText)
    
    // Add bottom padding if this is the last chunk
    if (isLast && bottomContentPadding > 0) {
        stringBuilder.append("\n".repeat(bottomContentPadding.coerceAtLeast(0)))
    }
    
    // Add spacing between paragraphs only for non-first paragraphs
    if (index > 0 && contentPadding > 0) {
        stringBuilder.append("\n".repeat(contentPadding.coerceAtLeast(0)))
    }
    
    return stringBuilder.toString()
}
