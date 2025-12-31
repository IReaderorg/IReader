package ireader.presentation.ui.reader

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import coil3.compose.LocalPlatformContext
import ireader.domain.models.entities.Chapter
import ireader.domain.preferences.prefs.ReadingMode
import ireader.presentation.core.toComposeColor
import ireader.presentation.ui.reader.components.AutoScrollSpeedControl
import ireader.presentation.ui.reader.components.FindInChapterBar
import ireader.presentation.ui.reader.components.FindInChapterState
import ireader.presentation.ui.reader.components.GlossaryDialogWithFilePickers
import ireader.presentation.ui.reader.components.MainBottomSettingComposable
import ireader.presentation.ui.reader.components.PreloadIndicator
import ireader.presentation.ui.reader.components.ReaderSettingsBottomSheet
import ireader.presentation.ui.reader.components.ReadingTimeEstimator
import ireader.presentation.ui.reader.components.ReportBrokenChapterDialog
import ireader.presentation.ui.reader.components.ChapterArtFocusDialog
import ireader.presentation.ui.reader.components.ChapterArtGeneratingDialog
import ireader.presentation.ui.reader.components.ChapterArtPromptResultDialog
import ireader.presentation.ui.reader.components.ChapterArtErrorDialog
import ireader.presentation.ui.reader.components.TranslationProgressIndicator
import ireader.presentation.ui.reader.components.TranslationToggleButton
import ireader.presentation.ui.reader.reverse_swip_refresh.SwipeRefreshState
import ireader.presentation.ui.reader.viewmodel.ReaderScreenViewModel
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.i18n.resources.*

/**
 * Pre-computed modifiers for ReaderScreen to avoid recreation on each recomposition
 */
private object ReaderScreenModifiers {
    val fillMaxSize = Modifier.fillMaxSize()
    val loadingIndicator = Modifier.size(48.dp)
    val bottomSheetHandle = Modifier
        .width(40.dp)
        .height(4.dp)
        .clip(RoundedCornerShape(2.dp))
}

@ExperimentalAnimationApi
@OptIn(

    ExperimentalMaterial3Api::class
)
@Composable
fun ReadingScreen(
        vm: ReaderScreenViewModel,
        scrollState: ScrollState,
        lazyListState: LazyListState,
        swipeState: SwipeRefreshState,
        onNext: (reset: Boolean) -> Unit,
        onPrev: (reset: Boolean) -> Unit,
        readerScreenPreferencesState: ReaderScreenViewModel,
        toggleReaderMode: () -> Unit,
        onBackgroundColorAndTextColorApply: (bgColor: String, txtColor: String) -> Unit,
        snackBarHostState: SnackbarHostState,
        drawerState: DrawerState,
        onReaderBottomOnSetting: () -> Unit,
        onSliderFinished: () -> Unit,
        onSliderChange: (index: Float) -> Unit,
        onReaderPlay: () -> Unit,
        onChapterShown: (chapter: Chapter) -> Unit,
        paddingValues: PaddingValues,
        onNavigateToTranslationSettings: () -> Unit,
        onNavigateToCharacterArtUpload: (bookTitle: String, chapterTitle: String, prompt: String) -> Unit = { _, _, _ -> },
        onChangeBrightness: (Float) -> Unit = {},
        onToggleAutoBrightness: () -> Unit = {},
        onNavigateToQuoteCreation: ((ireader.domain.models.quote.QuoteCreationParams) -> Unit)? = null
) {

    val scope = rememberCoroutineScope()
    val context = LocalPlatformContext.current
    val chapter = vm.stateChapter
    var showChapterReviews = androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    
    // Handle status bar for reading screen with proper colors based on background
    ireader.presentation.ui.core.theme.CustomSystemColor(
        enable = true,
        statusBar = vm.backgroundColor.value.toComposeColor(),
        navigationBar = vm.backgroundColor.value.toComposeColor()
    ) {
        ReadingScreenContent(
            vm = vm,
            scrollState = scrollState,
            lazyListState = lazyListState,
            swipeState = swipeState,
            onNext = onNext,
            onPrev = onPrev,
            readerScreenPreferencesState = readerScreenPreferencesState,
            toggleReaderMode = toggleReaderMode,
            snackBarHostState = snackBarHostState,
            drawerState = drawerState,
            onSliderFinished = onSliderFinished,
            onSliderChange = onSliderChange,
            onReaderPlay = onReaderPlay,
            onChapterShown = onChapterShown,
            paddingValues = paddingValues,
            onNavigateToTranslationSettings = onNavigateToTranslationSettings,
            onNavigateToCharacterArtUpload = onNavigateToCharacterArtUpload,
            scope = scope,
            chapter = chapter,
            showChapterReviews = showChapterReviews,
            onChangeBrightness = onChangeBrightness,
            onToggleAutoBrightness = onToggleAutoBrightness,
            onNavigateToQuoteCreation = onNavigateToQuoteCreation
        )
    }
}

@ExperimentalAnimationApi
@OptIn(

    ExperimentalMaterial3Api::class
)
@Composable
private fun ReadingScreenContent(
        vm: ReaderScreenViewModel,
        scrollState: ScrollState,
        lazyListState: LazyListState,
        swipeState: SwipeRefreshState,
        onNext: (reset: Boolean) -> Unit,
        onPrev: (reset: Boolean) -> Unit,
        readerScreenPreferencesState: ReaderScreenViewModel,
        toggleReaderMode: () -> Unit,
        snackBarHostState: SnackbarHostState,
        drawerState: DrawerState,
        onSliderFinished: () -> Unit,
        onSliderChange: (index: Float) -> Unit,
        onReaderPlay: () -> Unit,
        onChapterShown: (chapter: Chapter) -> Unit,
        paddingValues: PaddingValues,
        onNavigateToTranslationSettings: () -> Unit,
        onNavigateToCharacterArtUpload: (bookTitle: String, chapterTitle: String, prompt: String) -> Unit,
        scope: kotlinx.coroutines.CoroutineScope,
        chapter: Chapter?,
        showChapterReviews: androidx.compose.runtime.MutableState<Boolean>,
        onChangeBrightness: (Float) -> Unit,
        onToggleAutoBrightness: () -> Unit,
        onNavigateToQuoteCreation: ((ireader.domain.models.quote.QuoteCreationParams) -> Unit)? = null
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    // Pre-compute background color to avoid repeated conversions
    val backgroundColor = remember(vm.backgroundColor.value) { 
        vm.backgroundColor.value.toComposeColor() 
    }
    
    // Derive loading state for efficient Crossfade
    val isContentLoading by remember(vm.isLoading, vm.readingMode.value, vm.chapterShell) {
        derivedStateOf {
            vm.isLoading && if (vm.readingMode.value == ReadingMode.Continues) vm.chapterShell.isEmpty() else true
        }
    }
    
    // Use rememberUpdatedState to always have the latest callback references
    // This ensures the callbacks use the current state values, not stale captured values
    val currentOnNext by rememberUpdatedState(onNext)
    val currentOnPrev by rememberUpdatedState(onPrev)
    
    // Create stable callback wrappers that always call the latest callback
    val onNextWithReset = remember { { currentOnNext(true) } }
    val onPrevWithReset = remember { { currentOnPrev(true) } }
    val onNextWithoutReset = remember { { currentOnNext(false) } }
    val onPrevWithoutReset = remember { { currentOnPrev(false) } }
    
    // Calculate reading time when chapter changes
    LaunchedEffect(key1 = chapter?.id, key2 = vm.isLoading) {
        if (chapter != null && !vm.isLoading && vm.initialized) {
            vm.updateReadingTimeEstimation(0f)
        }
    }

    // No complex syncing needed - Material3 ModalBottomSheet is controlled by boolean condition
    // The sheet visibility is controlled by !vm.isReaderModeEnable
    
    Box(
        modifier = ReaderScreenModifiers.fillMaxSize
            .background(backgroundColor)
            .volumeKeyHandler(
                enabled = vm.volumeKeyNavigation.value,
                onVolumeUp = onPrevWithReset,
                onVolumeDown = onNextWithReset
            ),
        contentAlignment = Alignment.Center,
    ) {
        
        Crossfade(
            modifier = ReaderScreenModifiers.fillMaxSize,
            targetState = isContentLoading,
            animationSpec = tween(durationMillis = 300)
        ) { isLoading ->

            when (isLoading) {
                true -> {
                    Surface(
                        modifier = ReaderScreenModifiers.fillMaxSize,
                        color = backgroundColor
                    ) {
                        Box(
                            modifier = ReaderScreenModifiers.fillMaxSize,
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = ReaderScreenModifiers.loadingIndicator
                                    .align(Alignment.Center),
                                color = MaterialTheme.colorScheme.primary,
                                strokeWidth = 3.dp
                            )
                        }
                    }
                }
                else -> {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(if (vm.immersiveMode.value) PaddingValues() else paddingValues)
                        ) {
                            ReaderText(
                                vm = readerScreenPreferencesState,
                                onNext = onNextWithoutReset,
                                swipeState = swipeState,
                                onPrev = onPrevWithoutReset,
                                scrollState = scrollState,
                                toggleReaderMode = toggleReaderMode,
                                uiState = vm,
                                lazyListState = lazyListState,
                                onChapterShown = onChapterShown,
                                onShowComments = {
                                    if (vm.book != null && vm.stateChapter != null) {
                                        showChapterReviews.value = true
                                    }
                                },
                                onCopyModeDone = onNavigateToQuoteCreation
                            )
                            
                            // Settings Bottom Sheet
                            if (vm.showSettingsBottomSheet) {
                                ReaderSettingsBottomSheet(
                                    vm = vm,
                                    onDismiss = { vm.showSettingsBottomSheet = false },
                                    onFontSelected = { /* Handle font selection */ },
                                    onToggleAutoBrightness = onToggleAutoBrightness,
                                    onChangeBrightness = onChangeBrightness,
                                    onBackgroundChange = { themeId ->
                                        vm.changeBackgroundColor(themeId)
                                    },
                                    onTextAlign = { alignment ->
                                        vm.saveTextAlignment(alignment)
                                    }
                                )
                            }
                            
                            // Chapter Reviews Modal
                            if (showChapterReviews.value) {
                                val currentBook = vm.book
                                val currentChapter = vm.stateChapter
                                if (currentBook != null && currentChapter != null) {
                                    androidx.compose.material3.ModalBottomSheet(
                                        onDismissRequest = { showChapterReviews.value = false },
                                        sheetState = androidx.compose.material3.rememberModalBottomSheetState()
                                    ) {
                                        ireader.presentation.ui.reader.components.ChapterReviewsFullSheet(
                                            bookTitle = currentBook.title,
                                            chapterName = currentChapter.name,
                                            onDismiss = { showChapterReviews.value = false }
                                        )
                                    }
                                }
                            }
                            
                            // Translation toggle button
                            // Directly observe translationViewModel.translationState.hasTranslation for reactivity
                            // This ensures the button shows/hides immediately when translation is loaded
                            TranslationToggleButton(
                                isTranslated = vm.showTranslatedContent.value,
                                hasTranslation = vm.translationViewModel.translationState.hasTranslation,
                                onToggle = { vm.toggleTranslation() },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .statusBarsPadding()
                                    .padding(top = 56.dp, end = 16.dp) // Below top bar
                            )
                            
                            // Translation badge
//                            if (vm.showTranslatedContent.value) {
//                                TranslationBadge(
//                                    isTranslated = true,
//                                    textColor = vm.textColor.value.toComposeColor(),
//                                    modifier = Modifier
//                                        .align(Alignment.TopEnd)
//                                        .padding(paddingValues)
//
//                                )
//                            }
                            
                            // Translation progress indicator
                            TranslationProgressIndicator(
                                isVisible = vm.translationViewModel.isTranslating,
                                progress = vm.translationViewModel.translationProgress,
                                completedItems = (vm.translationViewModel.translationProgress * 100).toInt(),
                                totalItems = 100,
                                engine = vm.translationEnginesManager.get(),
                                textColor = vm.textColor.value.toComposeColor(),
                                onCancel = { 
                                    vm.scope.launch {
                                        vm.translationViewModel.cancelTranslation()
                                    }
                                },
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .padding(horizontal = 16.dp)
                            )
                            
                            // Preload indicator
                            PreloadIndicator(
                                isVisible = vm.isPreloading,
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(bottom = 16.dp)
                            )
                            
                            // Chapter repair banner
                            ireader.presentation.ui.reader.components.ChapterRepairBanner(
                                visible = vm.showRepairBanner,
                                message = vm.chapterBreakReason ?: "This chapter appears to be broken",
                                isRepairing = vm.isRepairing,
                                onRepairClick = { vm.repairChapter() },
                                onDismiss = { vm.dismissRepairBanner() },
                                modifier = Modifier
                                    .align(Alignment.TopCenter)
                                    .padding(top = 16.dp)
                            )
                            
                            // Chapter repair success banner
                            ireader.presentation.ui.reader.components.ChapterRepairSuccessBanner(
                                visible = vm.showRepairSuccess,
                                sourceName = vm.repairSuccessSourceName ?: "alternative source",
                                onDismiss = { vm.dismissRepairSuccessBanner() },
                                modifier = Modifier
                                    .align(Alignment.TopCenter)
                                    .padding(top = 16.dp)
                            )
                            
                            // Chapter Art Generation progress indicator
                            if (vm.isGeneratingArtPrompt) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .background(
                                            MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                                            RoundedCornerShape(16.dp)
                                        )
                                        .padding(24.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(48.dp),
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        androidx.compose.material3.Text(
                                            text = localizeHelper.localize(Res.string.generating_art_prompt),
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        androidx.compose.material3.Text(
                                            text = localizeHelper.localize(Res.string.analyzing_chapter_with_gemini_ai),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                            
                            // Autoscroll speed control
                            AutoScrollSpeedControl(
                                visible = vm.autoScrollMode,
                                isScrolling = vm.autoScrollMode,
                                scrollSpeed = vm.autoScrollOffset.value,
                                onSpeedIncrease = { vm.increaseAutoScrollSpeed() },
                                onSpeedDecrease = { vm.decreaseAutoScrollSpeed() },
                                onToggleScroll = { vm.toggleAutoScroll() },
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(bottom = 80.dp)
                            )
                            
                            // Find in chapter bar
                            if (vm.showFindInChapter) {
                                FindInChapterBar(
                                    state = FindInChapterState(
                                        query = vm.findQuery,
                                        matches = vm.findMatches,
                                        currentMatchIndex = vm.currentFindMatchIndex
                                    ),
                                    onQueryChange = { vm.updateFindQuery(it) },
                                    onNext = { vm.findNext() },
                                    onPrevious = { vm.findPrevious() },
                                    onClose = { vm.toggleFindInChapter() },
                                    modifier = Modifier
                                        .align(Alignment.TopCenter)
                                        .padding(top = 56.dp)
                                )
                            }
                            
                            // Reading time estimator
                            ReadingTimeEstimator(
                                visible = vm.showReadingTime && !vm.isLoading,
                                estimatedMinutes = vm.estimatedReadingMinutes,
                                wordsRemaining = vm.wordsRemaining,
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(bottom = 16.dp)
                            )
                        }
                        
                        // Glossary dialog
                        if (vm.translationViewModel.translationState.showGlossaryDialog) {
                            GlossaryDialogWithFilePickers(
                                glossaryEntries = vm.translationViewModel.glossaryEntries.toImmutableList(),
                                bookTitle = vm.book?.title,
                                onDismiss = { vm.translationViewModel.translationState.showGlossaryDialog = false },
                                onAddEntry = { source, target, type, notes ->
                                    vm.addGlossaryEntry(source, target, type, notes)
                                },
                                onEditEntry = { entry ->
                                    vm.updateGlossaryEntry(entry)
                                },
                                onDeleteEntry = { id ->
                                    vm.deleteGlossaryEntry(id)
                                },
                                onExportGlossary = { onSuccess ->
                                    vm.exportGlossary(onSuccess)
                                },
                                onImportGlossary = { json ->
                                    vm.importGlossary(json)
                                },
                                onShowSnackBar = { message ->
                                    vm.showSnackBar(message)
                                }
                            )
                        }
                        
                        // Report broken chapter dialog
                        if (vm.showReportDialog) {
                            ReportBrokenChapterDialog(
                                chapterName = vm.stateChapter?.name ?: "Unknown Chapter",
                                onDismiss = { vm.toggleReportDialog() },
                                onReport = { category, description ->
                                    vm.reportBrokenChapter(category, description)
                                }
                            )
                        }
                        
                        // Chapter Art Generation - Focus selection dialog
                        if (vm.showChapterArtDialog) {
                            ChapterArtFocusDialog(
                                bookTitle = vm.book?.title ?: "Unknown Book",
                                chapterTitle = vm.stateChapter?.name ?: "Unknown Chapter",
                                onDismiss = { vm.dismissChapterArtDialog() },
                                onGenerate = { focus ->
                                    vm.generateChapterArtPrompt(focus)
                                }
                            )
                        }
                        
                        // Chapter Art Generation - Loading dialog
                        if (vm.isGeneratingArtPrompt) {
                            ChapterArtGeneratingDialog(
                                onDismiss = { vm.dismissChapterArtDialog() }
                            )
                        }
                        
                        // Chapter Art Generation - Result dialog
                        vm.generatedArtPrompt?.let { prompt ->
                            val bookTitle = vm.book?.title ?: "Unknown Book"
                            val chapterTitle = vm.stateChapter?.name ?: "Unknown Chapter"
                            ChapterArtPromptResultDialog(
                                prompt = prompt,
                                bookTitle = bookTitle,
                                chapterTitle = chapterTitle,
                                onDismiss = { vm.clearGeneratedArtPrompt() },
                                onProceedToUpload = {
                                    // Navigate to upload screen with pre-filled data
                                    vm.clearGeneratedArtPrompt()
                                    onNavigateToCharacterArtUpload(bookTitle, chapterTitle, prompt)
                                },
                                onCopyPrompt = {
                                    // Show success message when copied
                                    scope.launch {
                                        snackBarHostState.showSnackbar(
                                            message = "Prompt copied to clipboard!",
                                            duration = androidx.compose.material3.SnackbarDuration.Short
                                        )
                                    }
                                }
                            )
                        }
                        
                        // Chapter Art Generation - Error dialog
                        vm.chapterArtError?.let { error ->
                            ChapterArtErrorDialog(
                                error = error,
                                onDismiss = { vm.clearGeneratedArtPrompt() },
                                onRetry = { vm.showChapterArtDialog() }
                            )
                        }
                        
                        // Paragraph translation dialog
                        if (vm.showParagraphTranslationDialog) {
                            ireader.presentation.ui.reader.components.TranslationResultDialog(
                                originalText = vm.paragraphToTranslate,
                                translatedText = vm.translatedParagraph,
                                isLoading = vm.isParagraphTranslating,
                                error = vm.paragraphTranslationError,
                                onDismiss = { vm.hideParagraphTranslation() },
                                onRetry = { vm.retryParagraphTranslation() }
                            )
                        }
                        
                        // Translation API key prompt dialog
                        if (vm.showTranslationApiKeyPrompt) {
                            ireader.presentation.ui.reader.components.TranslationApiKeyPromptDialog(
                                engineName = vm.getCurrentEngineName(),
                                onDismiss = { vm.dismissTranslationApiKeyPrompt() },
                                onGoToSettings = {
                                    vm.navigateToTranslationSettings()
                                    onNavigateToTranslationSettings()
                                }
                            )
                        }
                        
                        // Reading break reminder dialog
                        if (vm.showReadingBreakDialog) {
                            ireader.presentation.ui.reader.components.ReadingBreakReminderDialog(
                                intervalMinutes = vm.readingBreakInterval.value,
                                onTakeBreak = { vm.onTakeBreak() },
                                onContinueReading = { vm.onContinueReading() },
                                onSnooze = { minutes -> vm.onSnoozeReadingBreak(minutes) },
                                onDismiss = { vm.dismissReadingBreakDialog() }
                            )
                        }
                        
                        // Reader controls bottom bar (non-modal) with fast animation
                        AnimatedVisibility(
                            visible = !vm.isReaderModeEnable,
                            enter = slideInVertically(
                                initialOffsetY = { fullHeight -> fullHeight },
                                animationSpec = tween(
                                    durationMillis = 150,
                                    easing = androidx.compose.animation.core.FastOutSlowInEasing
                                )
                            ),
                            exit = slideOutVertically(
                                targetOffsetY = { fullHeight -> fullHeight },
                                animationSpec = tween(
                                    durationMillis = 100,
                                    easing = androidx.compose.animation.core.FastOutLinearInEasing
                                )
                            ),
                            modifier = Modifier.align(Alignment.BottomCenter)
                        ) {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                                contentColor = MaterialTheme.colorScheme.onSurface,
                                shadowElevation = 16.dp,
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .navigationBarsPadding()
                                        .padding(horizontal = 16.dp)
                                ) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .width(40.dp)
                                            .height(4.dp)
                                            .clip(RoundedCornerShape(2.dp))
                                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                                            .align(Alignment.CenterHorizontally)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    MainBottomSettingComposable(
                                        scope = scope,
                                        drawerState = drawerState,
                                        chapter = chapter,
                                        chapters = vm.stateChapters,
                                        currentChapterIndex = vm.currentChapterIndex,
                                        onSetting = { vm.showSettingsBottomSheet = true },
                                        onNext = onNextWithReset,
                                        onPrev = onPrevWithReset,
                                        onSliderChange = onSliderChange,
                                        onSliderFinished = onSliderFinished,
                                        onPlay = onReaderPlay,
                                        onAutoScrollToggle = { vm.toggleAutoScroll() }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }}

