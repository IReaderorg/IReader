package ireader.presentation.ui.reader

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.toUri
import ireader.core.source.model.ImageUrl
import ireader.core.source.model.Page
import ireader.core.source.model.Text
import ireader.domain.models.entities.Chapter
import ireader.domain.models.prefs.mapTextAlign
import ireader.domain.utils.extensions.currentTimeToLong
import ireader.i18n.resources.Res
import ireader.i18n.resources.chapter_complete
import ireader.i18n.resources.continue_reading
import ireader.i18n.resources.image
import ireader.i18n.resources.loading_1
import ireader.i18n.resources.next_chapter
import ireader.i18n.resources.the_end
import ireader.i18n.resources.view_comments
import ireader.presentation.core.toComposeColor
import ireader.presentation.core.toComposeTextAlign
import ireader.presentation.imageloader.IImageLoader
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import coil3.compose.LocalPlatformContext
import ireader.presentation.ui.reader.components.BilingualMode
import ireader.presentation.ui.reader.components.BilingualText
import ireader.presentation.ui.reader.components.SelectableTranslatableText
import ireader.presentation.ui.reader.viewmodel.ReaderScreenViewModel
import ireader.presentation.ui.reader.viewmodel.ReaderState
import kotlinx.coroutines.launch

/**
 * Model representing an indexed paragraph on a page with clean pre-formatted display text.
 */
private data class IndexedParagraph(
    val originalIndex: Int,
    val page: Page,
    val displayText: String = if (page is Text) page.text else ""
)

/**
 * Modern Horizontal Paged Reader Mode.
 *
 * Features:
 * - Real HorizontalPager with 60/120fps hardware-accelerated swipe physics.
 * - TextMeasurer pixel-perfect chunking matching exact viewport dimensions with NO vertical scrolling.
 * - Reliable Chapter Navigation: Lands on First Page on forward chapter navigation, and Last Page on backwards navigation.
 * - Start Boundary Page: Pull/tap to Previous Chapter with live title.
 * - End Boundary Page: Chapter Completion, View Comments/Reviews modal button, and Next Chapter action.
 * - 3-Zone Tap Navigation (Left: Prev, Center: Toggle UI, Right: Next).
 * - Race-condition-free navigation debouncing.
 */
@Composable
internal fun PagedReaderContent(
    vm: ReaderScreenViewModel,
    modifier: Modifier = Modifier,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onShowComments: (chapter: Chapter) -> Unit,
) {
    val readerState by vm.state.collectAsState()
    val successState = readerState as? ReaderState.Success
    val content = successState?.currentContent ?: emptyList()
    val chapter = successState?.currentChapter
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    val textMeasurer = rememberTextMeasurer()

    val currentIndex = vm.currentChapterIndex
    val chapters = vm.stateChapters
    val hasPrevChapter = currentIndex > 0
    val hasNextChapter = currentIndex < chapters.lastIndex
    val prevChapter = if (hasPrevChapter) chapters.getOrNull(currentIndex - 1) else null
    val nextChapter = if (hasNextChapter) chapters.getOrNull(currentIndex + 1) else null

    val preferences = rememberReaderTextPreferencesState(vm)

    val topMarginVal = vm.topMargin.lazyValue
    val bottomMarginVal = vm.bottomMargin.lazyValue
    val leftMarginVal = vm.leftMargin.lazyValue
    val rightMarginVal = vm.rightMargin.lazyValue

    // Navigation guard to prevent race conditions during rapid tapping/swiping
    var isNavigatingChapter by remember { mutableStateOf(false) }
    var lastNavTime by remember { mutableStateOf(0L) }
    val navDebounceMs = 400L

    fun safeNavigatePrev() {
        val now = currentTimeToLong()
        if (!isNavigatingChapter && (now - lastNavTime) > navDebounceMs && hasPrevChapter) {
            isNavigatingChapter = true
            lastNavTime = now
            onPrev()
        }
    }

    fun safeNavigateNext() {
        val now = currentTimeToLong()
        if (!isNavigatingChapter && (now - lastNavTime) > navDebounceMs && hasNextChapter) {
            isNavigatingChapter = true
            lastNavTime = now
            onNext()
        }
    }

    // Reset navigation flag when chapter changes
    LaunchedEffect(chapter?.id) {
        isNavigatingChapter = false
    }

    val styleParams = remember(
        preferences.fontSize,
        preferences.lineHeight,
        preferences.letterSpacing,
        preferences.fontWeight,
        preferences.paragraphIndent,
        preferences.textAlignment,
        preferences.textColor,
        preferences.fontFamily
    ) {
        TextStyleParams(
            fontSize = preferences.fontSize,
            lineHeight = preferences.lineHeight,
            letterSpacing = preferences.letterSpacing,
            fontWeight = preferences.fontWeight,
            paragraphIndent = preferences.paragraphIndent,
            textAlignment = preferences.textAlignment,
            textColor = preferences.textColor,
            fontFamily = preferences.fontFamily
        )
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val screenWidthPx = constraints.maxWidth
        val screenHeightPx = constraints.maxHeight

        // Partition content into pages using TextMeasurer for exact pixel fitting
        val contentPages: List<List<IndexedParagraph>> = remember(
            content,
            styleParams.textStyle,
            preferences.fontSize,
            preferences.paragraphIndent,
            preferences.distanceBetweenParagraphs,
            topMarginVal,
            bottomMarginVal,
            leftMarginVal,
            rightMarginVal,
            screenHeightPx,
            screenWidthPx,
            density.density
        ) {
            partitionContentWithMeasurer(
                content = content,
                textMeasurer = textMeasurer,
                textStyle = styleParams.textStyle,
                fontSize = preferences.fontSize,
                paragraphIndent = preferences.paragraphIndent,
                distanceBetweenParagraphsDp = preferences.distanceBetweenParagraphs,
                topMarginDp = topMarginVal,
                bottomMarginDp = bottomMarginVal,
                leftMarginDp = leftMarginVal,
                rightMarginDp = rightMarginVal,
                viewportHeightPx = screenHeightPx,
                viewportWidthPx = screenWidthPx,
                densityDpi = density.density
            )
        }

        val totalContentPages = contentPages.size.coerceAtLeast(1)
        val hasStartPage = hasPrevChapter
        val startPageOffset = if (hasStartPage) 1 else 0
        // Total pager items = (1 if prev chapter exists) + contentPages + (1 for end/comments page)
        val totalPagerCount = startPageOffset + totalContentPages + 1

        val pagerState = rememberPagerState(
            initialPage = if (vm.scrollToEndOnChapterChange) {
                (startPageOffset + totalContentPages - 1).coerceIn(0, totalPagerCount - 1)
            } else {
                startPageOffset.coerceIn(0, totalPagerCount - 1)
            }
        ) { totalPagerCount }

        // Chapter navigation positioning logic:
        // When navigating to next chapter -> jump to first content page (startPageOffset)
        // When navigating to previous chapter -> jump to last content page (startPageOffset + totalContentPages - 1)
        val currentChapterId = chapter?.id
        var prevChapterId by remember { mutableStateOf<Long?>(null) }
        var pendingScrollToEnd by remember { mutableStateOf(false) }

        LaunchedEffect(currentChapterId) {
            if (currentChapterId != null && currentChapterId != prevChapterId) {
                val isGoingBack = vm.scrollToEndOnChapterChange
                if (isGoingBack) {
                    pendingScrollToEnd = true
                } else {
                    pendingScrollToEnd = false
                    pagerState.scrollToPage(startPageOffset)
                }
                prevChapterId = currentChapterId
            }
        }

        LaunchedEffect(contentPages, pendingScrollToEnd) {
            if (pendingScrollToEnd && contentPages.isNotEmpty()) {
                val lastContentPage = (startPageOffset + totalContentPages - 1).coerceIn(0, totalPagerCount - 1)
                pagerState.scrollToPage(lastContentPage)
                pendingScrollToEnd = false
                vm.scrollToEndOnChapterChange = false
            }
        }

        // Track current page & reading time
        val currentContentPageIndex by remember {
            derivedStateOf {
                val rawPage = pagerState.currentPage
                if (hasStartPage && rawPage == 0) {
                    1
                } else if (rawPage >= startPageOffset + totalContentPages) {
                    totalContentPages
                } else {
                    (rawPage - startPageOffset + 1).coerceIn(1, totalContentPages)
                }
            }
        }

        LaunchedEffect(currentContentPageIndex, totalContentPages) {
            vm.updatePagedPageInfo(currentContentPageIndex, totalContentPages)
            if (totalContentPages > 0 && !vm.isLoading) {
                val progress = (currentContentPageIndex - 1).toFloat() / totalContentPages.toFloat()
                vm.updateReadingTimeEstimation(progress)
            }
        }

        // Debounced toggle reader mode for center tap
        var lastToggleTime by remember { mutableStateOf(0L) }
        fun debouncedToggle() {
            val now = currentTimeToLong()
            if (now - lastToggleTime > 250L) {
                lastToggleTime = now
                vm.toggleReaderMode()
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(totalPagerCount, hasPrevChapter, hasNextChapter) {
                    detectTapGestures(
                        onTap = { offset ->
                            val xRatio = offset.x / size.width
                            when {
                                xRatio < 0.25f -> {
                                    // Tap Left: Previous page
                                    if (pagerState.currentPage > 0) {
                                        scope.launch {
                                            pagerState.animateScrollToPage(pagerState.currentPage - 1)
                                        }
                                    } else if (hasPrevChapter) {
                                        safeNavigatePrev()
                                    }
                                }
                                xRatio > 0.75f -> {
                                    // Tap Right: Next page
                                    if (pagerState.currentPage < totalPagerCount - 1) {
                                        scope.launch {
                                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                        }
                                    } else if (hasNextChapter) {
                                        safeNavigateNext()
                                    }
                                }
                                else -> {
                                    // Tap Center (25% - 75%): Toggle reader controls
                                    debouncedToggle()
                                }
                            }
                        }
                    )
                }
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                beyondViewportPageCount = 1
            ) { pageIndex ->
                when {
                    hasStartPage && pageIndex == 0 -> {
                        // Previous Chapter Transition Page
                        StartChapterTransitionPage(
                            prevChapter = prevChapter,
                            textColor = vm.textColorCompose.value,
                            backgroundColor = vm.backgroundColor.value.toComposeColor(),
                            onNavigatePrev = { safeNavigatePrev() }
                        )
                    }
                    pageIndex >= (startPageOffset + totalContentPages) -> {
                        // End of Chapter & Comments Void Page
                        EndChapterCompletionPage(
                            chapter = chapter,
                            nextChapter = nextChapter,
                            hasNextChapter = hasNextChapter,
                            textColor = vm.textColorCompose.value,
                            backgroundColor = vm.backgroundColor.value.toComposeColor(),
                            isLoading = vm.isLoading,
                            onShowComments = { chapter?.let { onShowComments(it) } },
                            onNextChapter = { safeNavigateNext() },
                            onReRead = {
                                scope.launch {
                                    pagerState.animateScrollToPage(startPageOffset)
                                }
                            }
                        )
                    }
                    else -> {
                        // Content Page
                        val contentIndex = pageIndex - startPageOffset
                        val pageParagraphs = contentPages.getOrNull(contentIndex) ?: emptyList()

                        ContentPageView(
                            paragraphs = pageParagraphs,
                            vm = vm,
                            preferences = preferences,
                            styleParams = styleParams,
                            currentPage = contentIndex + 1,
                            totalPages = totalContentPages,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}

/**
 * Renders the content paragraphs for a single page with pure zero-scroll layout.
 */
@Composable
private fun ContentPageView(
    paragraphs: List<IndexedParagraph>,
    vm: ReaderScreenViewModel,
    preferences: ReaderTextPreferencesState,
    styleParams: TextStyleParams,
    currentPage: Int,
    totalPages: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(
                top = vm.topMargin.lazyValue.dp,
                bottom = vm.bottomMargin.lazyValue.dp,
                start = vm.leftMargin.lazyValue.dp,
                end = vm.rightMargin.lazyValue.dp
            ),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            paragraphs.forEachIndexed { idx, item ->
                if (idx > 0 && preferences.distanceBetweenParagraphs > 0) {
                    Spacer(modifier = Modifier.height(preferences.distanceBetweenParagraphs.dp))
                }
                PagedParagraphView(
                    item = item,
                    vm = vm,
                    preferences = preferences,
                    styleParams = styleParams,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Bottom Page Indicator
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp, bottom = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$currentPage / $totalPages",
                color = vm.textColorCompose.value.copy(alpha = 0.5f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
            val progressPercent = ((currentPage.toFloat() / totalPages.toFloat()) * 100).toInt()
            Text(
                text = "$progressPercent%",
                color = vm.textColorCompose.value.copy(alpha = 0.5f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/**
 * Renders an individual paragraph on a paged view without extra unpredictable newlines.
 */
@Composable
private fun PagedParagraphView(
    item: IndexedParagraph,
    vm: ReaderScreenViewModel,
    preferences: ReaderTextPreferencesState,
    styleParams: TextStyleParams,
    modifier: Modifier = Modifier
) {
    when (val page = item.page) {
        is ImageUrl -> {
            val localizeHelper = LocalLocalizeHelper.current
            val context = LocalPlatformContext.current
            val isLoading = remember { mutableStateOf(false) }
            Box(contentAlignment = Alignment.Center, modifier = modifier.fillMaxWidth()) {
                IImageLoader(
                    modifier = Modifier
                        .fillMaxWidth()
                        .requiredHeight(400.dp),
                    model = ImageRequest.Builder(context = context).data(page.url.toUri()).diskCachePolicy(CachePolicy.DISABLED).build(),
                    contentDescription = localizeHelper?.localize(Res.string.image),
                    contentScale = ContentScale.Fit,
                    onLoading = { isLoading.value = true },
                    onError = { isLoading.value = false },
                    onSuccess = { isLoading.value = false },
                )
                if (isLoading.value) {
                    CircularProgressIndicator()
                }
            }
        }
        is Text -> {
            val textToDisplay = item.displayText
            val translatedText = if (preferences.bilingualModeEnabled) {
                vm.getTranslationForParagraph(item.originalIndex)
            } else null

            if (preferences.bilingualModeEnabled && translatedText != null) {
                val bilingualMode = if (preferences.bilingualModeLayout == 0) {
                    BilingualMode.SIDE_BY_SIDE
                } else {
                    BilingualMode.PARAGRAPH_BY_PARAGRAPH
                }
                BilingualText(
                    originalText = textToDisplay,
                    translatedText = translatedText,
                    mode = bilingualMode,
                    modifier = modifier.fillMaxWidth(),
                    fontSize = styleParams.fontSize.sp,
                    fontFamily = styleParams.fontFamily,
                    textAlign = mapTextAlign(styleParams.textAlignment).toComposeTextAlign(),
                    originalColor = styleParams.textColor,
                    translatedColor = styleParams.textColor.copy(alpha = 0.9f),
                    lineHeight = styleParams.lineHeight.sp,
                    letterSpacing = styleParams.letterSpacing.sp,
                    fontWeight = FontWeight(styleParams.fontWeight)
                )
            } else if (preferences.bionicReadingMode) {
                val bionicText = remember(textToDisplay, styleParams.fontWeight) {
                    buildAnnotatedString {
                        textToDisplay.split(" ").forEach { s ->
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
                    modifier = modifier.fillMaxWidth(),
                    fontSize = styleParams.fontSize.sp,
                    fontFamily = styleParams.fontFamily,
                    textAlign = mapTextAlign(styleParams.textAlignment).toComposeTextAlign(),
                    color = styleParams.textColor,
                    lineHeight = styleParams.lineHeight.sp,
                    letterSpacing = styleParams.letterSpacing.sp,
                    fontWeight = FontWeight(styleParams.fontWeight),
                )
            } else {
                SelectableTranslatableText(
                    text = textToDisplay,
                    modifier = modifier.fillMaxWidth(),
                    fontSize = styleParams.fontSize.sp,
                    fontFamily = styleParams.fontFamily,
                    textAlign = mapTextAlign(styleParams.textAlignment).toComposeTextAlign(),
                    color = styleParams.textColor,
                    lineHeight = styleParams.lineHeight.sp,
                    letterSpacing = styleParams.letterSpacing.sp,
                    fontWeight = FontWeight(styleParams.fontWeight),
                    selectable = preferences.selectable,
                    paragraphTranslationEnabled = preferences.paragraphTranslationEnabled,
                    onTranslateRequest = { selectedText ->
                        vm.showParagraphTranslation(selectedText)
                    }
                )
            }
        }
        else -> {}
    }
}

/**
 * Start Transition Page: Drag or tap to go to Previous Chapter.
 */
@Composable
private fun StartChapterTransitionPage(
    prevChapter: Chapter?,
    textColor: Color,
    backgroundColor: Color,
    onNavigatePrev: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "prevTransition")
    val arrowOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -10f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "arrowFloat"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .clickable { onNavigatePrev() }
                .padding(16.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = textColor.copy(alpha = 0.08f),
                modifier = Modifier.size(64.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Previous Chapter",
                        tint = textColor.copy(alpha = 0.8f),
                        modifier = Modifier
                            .size(28.dp)
                            .graphicsLayer { translationX = arrowOffset }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "PREVIOUS CHAPTER",
                color = textColor.copy(alpha = 0.5f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = prevChapter?.name ?: "Previous Chapter",
                color = textColor,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Tap or swipe left to read",
                color = textColor.copy(alpha = 0.4f),
                fontSize = 12.sp
            )
        }
    }
}

/**
 * End Completion Page: Shows completion badge, comments/reviews button, and next chapter action.
 */
@Composable
private fun EndChapterCompletionPage(
    chapter: Chapter?,
    nextChapter: Chapter?,
    hasNextChapter: Boolean,
    textColor: Color,
    backgroundColor: Color,
    isLoading: Boolean,
    onShowComments: () -> Unit,
    onNextChapter: () -> Unit,
    onReRead: () -> Unit
) {
    val localizeHelper = LocalLocalizeHelper.current
    val infiniteTransition = rememberInfiniteTransition(label = "nextPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Chapter complete icon
            Surface(
                shape = CircleShape,
                color = textColor.copy(alpha = 0.08f),
                modifier = Modifier.size(56.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = textColor.copy(alpha = 0.7f),
                        modifier = Modifier.size(30.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = localizeHelper?.localize(Res.string.chapter_complete) ?: "Chapter Complete",
                color = textColor.copy(alpha = 0.5f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = chapter?.name ?: "",
                color = textColor,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(24.dp))

            // View Comments & Reviews Button
            OutlinedButton(
                onClick = onShowComments,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = textColor),
                border = BorderStroke(1.dp, textColor.copy(alpha = 0.25f)),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .height(44.dp)
                    .widthIn(min = 160.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.RateReview,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = textColor.copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = localizeHelper?.localize(Res.string.view_comments) ?: "Comments & Reviews",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = textColor.copy(alpha = 0.9f)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(28.dp),
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = localizeHelper?.localize(Res.string.loading_1) ?: "Loading...",
                    color = textColor.copy(alpha = 0.5f),
                    fontSize = 12.sp
                )
            } else if (hasNextChapter) {
                // Next Chapter CTA
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = localizeHelper?.localize(Res.string.next_chapter) ?: "Next Chapter",
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = nextChapter?.name ?: "",
                        color = textColor.copy(alpha = 0.6f),
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            } else {
                // End of Book / Caught up
                Text(
                    text = localizeHelper?.localize(Res.string.the_end) ?: "The End",
                    color = textColor.copy(alpha = 0.5f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Re-read Button
            OutlinedButton(
                onClick = onReRead,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = textColor),
                border = BorderStroke(0.5.dp, textColor.copy(alpha = 0.15f)),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.height(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Replay,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = textColor.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "First Page",
                    fontSize = 12.sp,
                    color = textColor.copy(alpha = 0.7f)
                )
            }
        }
    }
}

/**
 * Splits chapter content into pages using exact TextMeasurer layout calculations.
 * Measures text using the exact font, size, line height, and pixel constraints,
 * cleanly breaking paragraphs at word/sentence boundaries so that every page
 * fits 100% within the viewport without requiring vertical scrolling or cutting text.
 */
private fun partitionContentWithMeasurer(
    content: List<Page>,
    textMeasurer: TextMeasurer,
    textStyle: TextStyle,
    fontSize: Int,
    paragraphIndent: Int,
    distanceBetweenParagraphsDp: Int,
    topMarginDp: Int,
    bottomMarginDp: Int,
    leftMarginDp: Int,
    rightMarginDp: Int,
    viewportHeightPx: Int,
    viewportWidthPx: Int,
    densityDpi: Float
): List<List<IndexedParagraph>> {
    if (content.isEmpty()) return emptyList()

    val effectiveWidth = if (viewportWidthPx > 0) viewportWidthPx else 1080
    val effectiveHeight = if (viewportHeightPx > 0) viewportHeightPx else 1920

    val topMarginPx = (topMarginDp * densityDpi).toInt()
    val bottomMarginPx = (bottomMarginDp * densityDpi).toInt()
    val leftMarginPx = (leftMarginDp * densityDpi).toInt()
    val rightMarginPx = (rightMarginDp * densityDpi).toInt()
    val bottomIndicatorPx = (24 * densityDpi).toInt()
    val indicatorPaddingPx = (6 * densityDpi).toInt()
    val safetyPaddingPx = (24 * densityDpi).toInt() // Headroom to ensure zero bottom overflow

    val usableWidthPx = (effectiveWidth - leftMarginPx - rightMarginPx).coerceAtLeast(200)
    val usableHeightPx = (effectiveHeight - topMarginPx - bottomMarginPx - bottomIndicatorPx - indicatorPaddingPx - safetyPaddingPx).coerceAtLeast(200)
    val constraints = Constraints(maxWidth = usableWidthPx)
    val paragraphSpacingPx = (distanceBetweenParagraphsDp * densityDpi).coerceAtLeast(0f)

    val pages = mutableListOf<List<IndexedParagraph>>()
    var currentPageItems = mutableListOf<IndexedParagraph>()
    var currentHeightOnPage = 0f

    fun flushPage() {
        if (currentPageItems.isNotEmpty()) {
            pages.add(currentPageItems)
            currentPageItems = mutableListOf()
            currentHeightOnPage = 0f
        }
    }

    content.forEachIndexed { originalIndex, page ->
        when (page) {
            is ImageUrl -> {
                flushPage()
                pages.add(listOf(IndexedParagraph(originalIndex, page)))
            }
            is Text -> {
                var remainingText = page.text
                var isFirstChunkOfParagraph = true

                while (remainingText.isNotEmpty()) {
                    val spacingBefore = if (currentPageItems.isNotEmpty()) paragraphSpacingPx else 0f
                    val availableHeight = usableHeightPx - currentHeightOnPage - spacingBefore

                    val indentStr = if (isFirstChunkOfParagraph && paragraphIndent > 0) {
                        " ".repeat((paragraphIndent / 2).coerceAtLeast(0))
                    } else ""
                    val textToMeasure = indentStr + remainingText

                    val measureResult = textMeasurer.measure(
                        text = AnnotatedString(textToMeasure),
                        style = textStyle,
                        constraints = constraints
                    )

                    val singleLineHeight = if (measureResult.lineCount > 0) {
                        measureResult.getLineBottom(0)
                    } else {
                        (fontSize * 1.5f * densityDpi)
                    }

                    // If remaining space cannot fit even one single line, flush to a fresh page
                    if (currentPageItems.isNotEmpty() && availableHeight < singleLineHeight) {
                        flushPage()
                        continue
                    }

                    val currentAvailableHeight = usableHeightPx - currentHeightOnPage - (if (currentPageItems.isNotEmpty()) paragraphSpacingPx else 0f)

                    if (measureResult.size.height <= currentAvailableHeight) {
                        // Entire remaining text fits completely on current page!
                        currentPageItems.add(
                            IndexedParagraph(
                                originalIndex = originalIndex,
                                page = page,
                                displayText = textToMeasure
                            )
                        )
                        currentHeightOnPage += (if (currentPageItems.size > 1) paragraphSpacingPx else 0f) + measureResult.size.height
                        remainingText = ""
                    } else {
                        // Overflow: find the exact line that fits within currentAvailableHeight
                        var lastFittingLine = -1
                        for (lineIdx in 0 until measureResult.lineCount) {
                            if (measureResult.getLineBottom(lineIdx) <= currentAvailableHeight) {
                                lastFittingLine = lineIdx
                            } else {
                                break
                            }
                        }

                        if (lastFittingLine < 0) {
                            if (currentPageItems.isNotEmpty()) {
                                flushPage()
                                continue
                            } else {
                                // If even 1 line exceeds on empty page, allow at least 1 line
                                lastFittingLine = 0
                            }
                        }

                        // Measure character offset at end of lastFittingLine
                        val rawCutInMeasured = measureResult.getLineEnd(lastFittingLine, visibleEnd = true)
                        val rawCutInRemaining = (rawCutInMeasured - indentStr.length).coerceIn(1, remainingText.length)
                        val cleanCut = findCleanBreak(remainingText, rawCutInRemaining)

                        val chunkText = remainingText.substring(0, cleanCut).trimEnd()
                        val nextRemaining = remainingText.substring(cleanCut).trimStart()

                        if (chunkText.isNotEmpty()) {
                            val chunkDisplay = indentStr + chunkText
                            val chunkMeasure = textMeasurer.measure(
                                text = AnnotatedString(chunkDisplay),
                                style = textStyle,
                                constraints = constraints
                            )
                            currentPageItems.add(
                                IndexedParagraph(
                                    originalIndex = originalIndex,
                                    page = page,
                                    displayText = chunkDisplay
                                )
                            )
                            currentHeightOnPage += (if (currentPageItems.size > 1) paragraphSpacingPx else 0f) + chunkMeasure.size.height
                        }

                        remainingText = nextRemaining
                        isFirstChunkOfParagraph = false
                        flushPage()
                    }
                }
            }
            else -> {
                currentPageItems.add(IndexedParagraph(originalIndex, page))
            }
        }
    }

    flushPage()

    return pages.ifEmpty { listOf(emptyList()) }
}

/**
 * Finds a natural break point (sentence, punctuation, or space) near rawCutOffset
 * so words are not cut in half across pages.
 */
private fun findCleanBreak(text: String, rawCutOffset: Int): Int {
    if (rawCutOffset >= text.length) return text.length
    if (rawCutOffset <= 0) return text.length.coerceAtMost(1)

    val targetLimit = rawCutOffset.coerceIn(1, text.length)
    val minLimit = (targetLimit * 0.7f).toInt().coerceAtLeast(1)

    // 1. Sentence ending (. ! ? 。 ！？) followed by space/quote/newline
    val sentenceRegex = Regex("[.!?。！？][\\s\"'”’)\n]")
    val sentenceMatch = sentenceRegex.findAll(text.substring(0, targetLimit))
        .lastOrNull { it.range.first >= minLimit }
    if (sentenceMatch != null) {
        return sentenceMatch.range.last + 1
    }

    // 2. Newline '\n'
    val newlineIdx = text.lastIndexOf('\n', targetLimit - 1)
    if (newlineIdx >= minLimit) {
        return newlineIdx + 1
    }

    // 3. Space ' '
    val spaceIdx = text.lastIndexOf(' ', targetLimit - 1)
    if (spaceIdx >= minLimit) {
        return spaceIdx + 1
    }

    // 4. Punctuation mark (, ; : ， ； ：)
    val punctIdx = text.lastIndexOfAny(charArrayOf(',', ';', ':', '，', '；', '：'), targetLimit - 1)
    if (punctIdx >= minLimit) {
        return punctIdx + 1
    }

    // 5. Fallback to targetLimit
    return targetLimit
}

