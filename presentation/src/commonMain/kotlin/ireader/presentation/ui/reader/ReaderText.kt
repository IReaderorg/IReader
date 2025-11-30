package ireader.presentation.ui.reader

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.LocalPlatformContext
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.toUri
import ireader.core.source.model.ImageUrl
import ireader.presentation.core.toComposeColor
import ireader.presentation.core.toDomainColor
import ireader.presentation.core.toComposeColor
import ireader.presentation.core.toComposeFontFamily
import ireader.presentation.core.toComposeTextAlign
import ireader.core.source.model.Page
import ireader.core.source.model.Text
import ireader.domain.models.entities.Chapter
import ireader.domain.models.prefs.PreferenceValues
import ireader.domain.models.prefs.mapTextAlign
import ireader.domain.preferences.prefs.ReadingMode
import ireader.presentation.imageloader.IImageLoader
import ireader.presentation.ui.component.list.scrollbars.IColumnScrollbar
import ireader.presentation.ui.component.list.scrollbars.ILazyColumnScrollbar
import ireader.presentation.ui.core.modifier.supportDesktopScroll
import ireader.presentation.ui.reader.components.SelectableTranslatableText
import ireader.presentation.ui.reader.reverse_swip_refresh.ISwipeRefreshIndicator
import ireader.presentation.ui.reader.reverse_swip_refresh.MultiSwipeRefresh
import ireader.presentation.ui.reader.reverse_swip_refresh.SwipeRefreshState
import ireader.presentation.ui.reader.viewmodel.ReaderScreenState
import ireader.presentation.ui.reader.viewmodel.ReaderScreenViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ReaderText(
    modifier: Modifier = Modifier,
    vm: ReaderScreenViewModel,
    uiState: ReaderScreenState,
    onNext: () -> Unit,
    onPrev: () -> Unit,
    swipeState: SwipeRefreshState,
    scrollState: ScrollState,
    lazyListState: LazyListState,
    modalState: ModalBottomSheetState,
    toggleReaderMode: () -> Unit,
    onChapterShown: (chapter: Chapter) -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val scope = rememberCoroutineScope()
    
    // Add debounce mechanism for toggleReaderMode
    var lastToggleTime by remember { mutableStateOf(0L) }
    val debounceInterval = 500L // 500ms debounce
    
    val debouncedToggleReaderMode = remember {
        {
            val currentTime = System.currentTimeMillis()
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
        val firstVisibleValue =
            remember { derivedStateOf { lazyListState.firstVisibleItemScrollOffset } }


        Box(
            modifier = Modifier.padding(
                top = vm.topMargin.value.dp,
                bottom = vm.bottomMargin.value.dp,
                start = vm.leftMargin.value.dp,
                end = vm.rightMargin.value.dp
            )
        ) {
            MultiSwipeRefresh(
                modifier = Modifier.fillMaxSize(),
                state = swipeState,
                indicators = listOf(
                    ISwipeRefreshIndicator(
                        (scrollState.value == 0 && vm.readingMode.value == ReadingMode.Page) || (vm.readingMode.value == ReadingMode.Continues && firstVisibleValue.value == 0),
                        alignment = Alignment.TopCenter,
                        indicator = { _, _ ->
                            ArrowIndicator(
                                icon = Icons.Default.KeyboardArrowUp,
                                swipeRefreshState = swipeState,
                                refreshTriggerDistance = 80.dp,
                                color = vm.textColor.value.toComposeColor()
                            )
                        }, onRefresh = {
                            onPrev()
                        }
                    ),
                    ISwipeRefreshIndicator(
                        (scrollState.value != 0 && vm.readingMode.value == ReadingMode.Page) || (vm.readingMode.value == ReadingMode.Continues && firstVisibleValue.value != 0),
                        alignment = Alignment.BottomCenter,
                        onRefresh = {
                            onNext()
                        },
                        indicator = { _, _ ->
                            ArrowIndicator(
                                icon = Icons.Default.KeyboardArrowDown,
                                swipeRefreshState = swipeState,
                                refreshTriggerDistance = 80.dp,
                                color = vm.textColor.value.toComposeColor()
                            )
                        }
                    ),
                ),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
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
                                    vm = vm,
                                    maxHeight = maxHeight,
                                    onNext = onNext,
                                    onPrev = onPrev,
                                    toggleReaderMode = debouncedToggleReaderMode
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
                                    onChapterShown = onChapterShown
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

        IColumnScrollbar(
            state = scrollState,
            padding = if (vm.scrollIndicatorPadding.value < 0) 0.dp else vm.scrollIndicatorPadding.value.dp,
            thickness = if (vm.scrollIndicatorWith.value < 0) 0.dp else vm.scrollIndicatorWith.value.dp,
            enabled = vm.showScrollIndicator.value,
            thumbColor = vm.unselectedScrollBarColor.value.toComposeColor(),
            thumbSelectedColor = vm.selectedScrollBarColor.value.toComposeColor(),
            selectionMode = vm.isScrollIndicatorDraggable.value,
            rightSide = vm.scrollIndicatorAlignment.value == PreferenceValues.PreferenceTextAlignment.Right
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(top = 32.dp)

            ) {
                vm.getCurrentContent().forEachIndexed { index, text ->
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

@Composable
private fun MainText(
    modifier: Modifier,
    index: Int,
    page: Page,
    vm: ReaderScreenViewModel
) {

    val context = LocalPlatformContext.current
    when (page) {
        is Text -> {
            StyleText(modifier, vm, index, page, vm.bionicReadingMode.value)
        }
        is ImageUrl -> {
            val isLoading = remember {
                mutableStateOf(false)

            }
            Box(contentAlignment = Alignment.Center) {
                IImageLoader(
                    modifier = Modifier
                        .fillMaxWidth()
                        .requiredHeight(500.dp),
                    model = ImageRequest.Builder(context=context).data(page.url.toUri()).diskCachePolicy(CachePolicy.DISABLED).build(),
                    contentDescription = "image",
                    contentScale = ContentScale.FillWidth,
                    onLoading = {
                        isLoading.value = true
                    },
                    onError = {
                        isLoading.value = false
                    },
                    onSuccess = {
                        isLoading.value = false
                    },
                )
                if (isLoading.value) {
                    androidx.compose.material3.CircularProgressIndicator()
                }
            }
        }

        else -> {
        }
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
    val currentContent = remember(vm.stateChapter?.id, vm.translationViewModel.translationState.hasTranslation) {
        vm.getCurrentContent()
    }
    val isLastIndex = index == currentContent.lastIndex
    
    val originalText = setText(
        text = page.text,
        index = index,
        isLast = isLastIndex,
        topContentPadding = vm.topContentPadding.value,
        contentPadding = vm.distanceBetweenParagraphs.value,
        bottomContentPadding = vm.bottomContentPadding.value
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
                .padding(horizontal = vm.paragraphsIndent.value.dp),
            fontSize = vm.fontSize.value.sp,
            fontFamily = remember(vm.font?.value, vm.fontVersion) { 
                vm.font?.value?.fontFamily?.toComposeFontFamily() 
            },
            textAlign = mapTextAlign(vm.textAlignment.value).toComposeTextAlign(),
            originalColor = vm.textColor.value.toComposeColor(),
            translatedColor = vm.textColor.value.toComposeColor().copy(alpha = 0.9f),
            lineHeight = vm.lineHeight.value.sp,
            letterSpacing = vm.betweenLetterSpaces.value.sp,
            fontWeight = FontWeight(vm.textWeight.value)
        )
    } else if (enableBioReading) {
        Text(
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
                .padding(horizontal = vm.paragraphsIndent.value.dp),
            fontSize = vm.fontSize.value.sp,
            fontFamily = remember(vm.font?.value, vm.fontVersion) { 
                vm.font?.value?.fontFamily?.toComposeFontFamily() 
            },
            textAlign = mapTextAlign(vm.textAlignment.value).toComposeTextAlign(),
            color = vm.textColor.value.toComposeColor(),
            lineHeight = vm.lineHeight.value.sp,
            letterSpacing = vm.betweenLetterSpaces.value.sp,
            fontWeight = FontWeight(vm.textWeight.value),
        )
    } else {
        // Normal text rendering
        SelectableTranslatableText(
            text = originalText,
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = vm.paragraphsIndent.value.dp),
            fontSize = vm.fontSize.value.sp,
            fontFamily = remember(vm.font?.value, vm.fontVersion) { 
                vm.font?.value?.fontFamily?.toComposeFontFamily() 
            },
            textAlign = mapTextAlign(vm.textAlignment.value).toComposeTextAlign(),
            color = vm.textColor.value.toComposeColor(),
            lineHeight = vm.lineHeight.value.sp,
            letterSpacing = vm.betweenLetterSpaces.value.sp,
            fontWeight = FontWeight(vm.textWeight.value),
            selectable = vm.selectableMode.value,
            paragraphTranslationEnabled = vm.paragraphTranslationEnabled.value,
            onTranslateRequest = { selectedText ->
                vm.showParagraphTranslation(selectedText)
            }
        )
    }

}
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
) {
    val scope = rememberCoroutineScope()
    
    var lastChapterId: Chapter? by remember {
        mutableStateOf(null)
    }
    LaunchedEffect(key1 = lastChapterId) {
        lastChapterId?.let { onChapterShown(it) }
    }
    val visibleItemInfo = remember { derivedStateOf { scrollState.layoutInfo } }
    LaunchedEffect(key1 = visibleItemInfo.value.visibleItemsInfo.firstOrNull()?.key) {
        runCatching {
            val visibleKey = scrollState.layoutInfo.visibleItemsInfo.firstOrNull()?.key?.toString()
            if (visibleKey != null) {
                val chapterId = visibleKey.substringAfter("-").toLongOrNull()
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
    
    val items by remember {
        derivedStateOf {
            // Safely map chapter content, handling potential null or empty content
            vm.chapterShell.flatMap { chapter ->
                val content = chapter.content
                if (content.isNullOrEmpty()) {
                    emptyList()
                } else {
                    content.map { page -> chapter.id to page }
                }
            }
        }
    }

    ILazyColumnScrollbar(
        listState = scrollState,
        padding = if (vm.scrollIndicatorPadding.value < 0) 0.dp else vm.scrollIndicatorPadding.value.dp,
        thickness = if (vm.scrollIndicatorWith.value < 0) 0.dp else vm.scrollIndicatorWith.value.dp,
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
            items(
                count = items.size,
                key = { index ->
                    "$index-${items[index].first}"
                }
            ) { index ->
                MainText(
                    modifier = modifier,
                    index = index,
                    page = items[index].second,
                    vm = vm
                )
            }
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
    if (!vm.verticalScrolling.value) {
        Row(modifier = Modifier.fillMaxSize()) {

            Box(
                Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null
                    ) {
                        scope.launch {
                            if (scrollState.value != 0) {
                                scrollState.scrollBy(-maxHeight)
                            } else {
                                onPrev()
                            }
                        }
                    }
            ) {
            }
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
            Box(
                Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null
                    ) {
                        scope.launch {
                            if (scrollState.value != scrollState.maxValue) {
                                scrollState.scrollBy(maxHeight)
                            } else {
                                onNext()
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
