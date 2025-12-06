package ireader.presentation.core.ui

import androidx.compose.animation.AnimatedVisibility

import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.*
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.style.TextAlign

import androidx.compose.ui.unit.dp
import ireader.domain.data.repository.ChapterRepository
import ireader.domain.models.entities.Chapter
import ireader.domain.preferences.prefs.ReaderPreferences
import ireader.domain.services.tts_service.CommonTTSService
import ireader.domain.usecases.translation.GetAllTranslationsForChapterUseCase
import ireader.domain.services.common.TranslationService
import ireader.domain.services.common.ServiceResult
import ireader.domain.services.common.TranslationQueueResult
import ireader.domain.usecases.translate.TranslationEnginesManager
import ireader.presentation.core.IModalDrawer
import ireader.presentation.core.LocalNavigator
import ireader.presentation.core.NavigationRoutes

import ireader.presentation.ui.component.IScaffold
import ireader.presentation.ui.component.components.ChapterRow
import ireader.presentation.ui.component.isTableUi
import ireader.presentation.ui.component.list.scrollbars.IVerticalFastScroller
import ireader.presentation.ui.component.reusable_composable.AppIconButton
import ireader.presentation.ui.component.reusable_composable.BigSizeTextComposable
import ireader.presentation.ui.component.reusable_composable.TopAppBarBackButton
import ireader.presentation.ui.home.tts.*
import ireader.i18n.localize
import ireader.i18n.resources.Res
import ireader.i18n.resources.*
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import ireader.presentation.ui.core.theme.LocalLocalizeHelper

/**
 * Unified TTS Screen Spec - Works across all platforms
 * 
 * Uses CommonTTSService interface which is implemented by:
 * - Android: AndroidTTSService
 * - Desktop: DesktopTTSServiceAdapter
 * 
 * Features:
 * - Paragraph highlighting during playback
 * - Translation support (original, translated, bilingual)
 * - Speed control, sleep timer
 * - Adaptive UI for mobile/tablet/desktop
 * 
 * Optimizations for low-end devices:
 * - Debounced scroll animations to reduce frame drops
 * - Memoized callbacks and derived state to minimize recompositions
 * - Lazy state collection with throttling
 * - Stable keys for LazyColumn items
 */
class TTSScreenSpec(
    val bookId: Long,
    val chapterId: Long,
    val sourceId: Long,
    val readingParagraph: Int,
) {
    @OptIn(ExperimentalMaterial3Api::class, FlowPreview::class)
    @Composable
    fun Content() {
        val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
        val navController = requireNotNull(LocalNavigator.current) { "LocalNavigator not provided" }
        val ttsService: CommonTTSService = koinInject()
        val readerPreferences: ReaderPreferences = koinInject()
        val appPreferences: ireader.domain.preferences.prefs.AppPreferences = koinInject()
        val chapterRepository: ChapterRepository = koinInject()
        val getAllTranslationsUseCase: GetAllTranslationsForChapterUseCase = koinInject()
        val translationService: TranslationService = koinInject()
        val translationEnginesManager: TranslationEnginesManager = koinInject()
        val scope = rememberCoroutineScope()
        val isTabletOrDesktop = isTableUi()
        
        // Translation in progress state
        var isTranslatingChapter by remember { mutableStateOf(false) }
        
        // Translation state
        var translatedContent by remember { mutableStateOf<List<String>?>(null) }
        
        // Chapter drawer state
        val drawerState = rememberDrawerState(DrawerValue.Closed)
        var chapters by remember { mutableStateOf<List<Chapter>>(emptyList()) }
        var chaptersAscending by remember { mutableStateOf(true) }
        
        // Local UI state - grouped to reduce state objects
        var showTranslation by remember { mutableStateOf(false) }
        var bilingualMode by remember { mutableStateOf(false) }
        var fullScreenMode by remember { mutableStateOf(false) }
        var showSettings by remember { mutableStateOf(false) }
        var showEngineSettings by remember { mutableStateOf(false) }
        var showVoiceSelection by remember { mutableStateOf(false) }
        
        // Settings state - load from TTS-specific preferences with lazy initialization
        var useCustomColors by remember { mutableStateOf(readerPreferences.ttsUseCustomColors().get()) }
        var customBackgroundColor by remember { 
            val savedColor = readerPreferences.ttsBackgroundColor().get()
            mutableStateOf(Color(savedColor.toInt()))
        }
        var customTextColor by remember { 
            val savedColor = readerPreferences.ttsTextColor().get()
            mutableStateOf(Color(savedColor.toInt()))
        }
        var fontSize by remember { mutableStateOf(readerPreferences.ttsFontSize().get()) }
        var textAlignment by remember { 
            val savedAlignment = readerPreferences.ttsTextAlignment().get()
            mutableStateOf(
                when (savedAlignment) {
                    ireader.domain.models.prefs.PreferenceValues.PreferenceTextAlignment.Left -> TextAlign.Start
                    ireader.domain.models.prefs.PreferenceValues.PreferenceTextAlignment.Center -> TextAlign.Center
                    ireader.domain.models.prefs.PreferenceValues.PreferenceTextAlignment.Right -> TextAlign.End
                    ireader.domain.models.prefs.PreferenceValues.PreferenceTextAlignment.Justify -> TextAlign.Justify
                    else -> TextAlign.Start
                }
            )
        }
        var sleepModeEnabled by remember { mutableStateOf(readerPreferences.sleepMode().get()) }
        var sleepTimeMinutes by remember { mutableStateOf(readerPreferences.sleepTime().get().toInt()) }
        
        // Load readTranslatedText preference
        var readTranslatedText by remember { mutableStateOf(readerPreferences.useTTSWithTranslatedText().get()) }
        
        // Gradio TTS state
        var useGradioTTS by remember { mutableStateOf(appPreferences.useGradioTTS().get()) }
        val activeGradioConfigId = remember { appPreferences.activeGradioConfigId().get() }
        val isGradioConfigured = remember(activeGradioConfigId) { activeGradioConfigId.isNotEmpty() }
        
        // Collect state from service with debouncing for low-end device optimization
        val isPlaying by ttsService.state.isPlaying.collectAsState()
        val isLoading by ttsService.state.isLoading.collectAsState()
        val isTTSReady by ttsService.state.isTTSReady.collectAsState()
        val currentBook by ttsService.state.currentBook.collectAsState()
        val currentChapter by ttsService.state.currentChapter.collectAsState()
        val currentParagraph by remember(ttsService) {
            ttsService.state.currentParagraph.debounce(50) // Debounce rapid paragraph changes
        }.collectAsState(initial = 0)
        val content by ttsService.state.currentContent.collectAsState()
        val speechSpeed by ttsService.state.speechSpeed.collectAsState()
        val autoNextChapter by ttsService.state.autoNextChapter.collectAsState()
        
        // Collect paragraph speaking start time from service for highlighter sync
        // This is updated when TTS actually starts speaking (in TTSService.onStart callback)
        // Used as a SIGNAL to reset the sentence highlighter timer in CommonTTSScreen
        val paragraphSpeakingStartTime by ttsService.state.paragraphSpeakingStartTime.collectAsState()
        
        // Memoize colors to prevent unnecessary recompositions
        val backgroundColor by remember(useCustomColors, customBackgroundColor) {
            derivedStateOf { if (useCustomColors) customBackgroundColor else Color.Unspecified }
        }
        val resolvedBackgroundColor = if (backgroundColor == Color.Unspecified) 
            MaterialTheme.colorScheme.background else backgroundColor
        
        val textColor by remember(useCustomColors, customTextColor) {
            derivedStateOf { if (useCustomColors) customTextColor else Color.Unspecified }
        }
        val resolvedTextColor = if (textColor == Color.Unspecified) 
            MaterialTheme.colorScheme.onBackground else textColor
        
        val lazyListState = rememberLazyListState()
        
        // Initialize TTS with the chapter and load chapters list
        LaunchedEffect(bookId, chapterId) {
            // IMPORTANT: Clear existing translation state FIRST to prevent sync bug
            // This ensures we don't show chapter 200's translation for chapter 220
            translatedContent = null
            showTranslation = false
            ttsService.setCustomContent(null) // Clear TTS custom content too
            
            // Load chapters list for drawer first
            try {
                chapters = chapterRepository.findChaptersByBookId(bookId)
            } catch (e: Exception) {
                // Failed to load chapters
            }
            
            // Load translation BEFORE starting TTS to ensure correct language from the start
            var translatedStringsToUse: List<String>? = null
            if (readTranslatedText) {
                try {
                    val translations = getAllTranslationsUseCase.execute(chapterId)
                    if (translations.isNotEmpty()) {
                        val latestTranslation = translations.maxByOrNull { it.updatedAt }
                        latestTranslation?.let { translation ->
                            val translatedStrings = translation.translatedContent
                                .filterIsInstance<ireader.core.source.model.Text>()
                                .map { it.text }
                                .filter { it.isNotBlank() }
                            if (translatedStrings.isNotEmpty()) {
                                translatedContent = translatedStrings
                                showTranslation = true
                                translatedStringsToUse = translatedStrings
                            }
                        }
                    }
                } catch (e: Exception) {
                    // Translation not available, continue without it
                }
            }
            
            // Set translated content BEFORE starting TTS so it reads in correct language
            if (translatedStringsToUse != null) {
                ttsService.setCustomContent(translatedStringsToUse)
            }
            
            // Now start TTS - it will use translated content if set
            ttsService.startReading(bookId, chapterId)
            if (readingParagraph > 0) {
                ttsService.jumpToParagraph(readingParagraph)
            }
            
            // If translation wasn't loaded yet (readTranslatedText was false), load it for display
            if (translatedContent == null) {
                try {
                    val translations = getAllTranslationsUseCase.execute(chapterId)
                    if (translations.isNotEmpty()) {
                        val latestTranslation = translations.maxByOrNull { it.updatedAt }
                        latestTranslation?.let { translation ->
                            val translatedStrings = translation.translatedContent
                                .filterIsInstance<ireader.core.source.model.Text>()
                                .map { it.text }
                                .filter { it.isNotBlank() }
                            if (translatedStrings.isNotEmpty()) {
                                translatedContent = translatedStrings
                            }
                        }
                    }
                } catch (e: Exception) {
                    // Translation not available
                }
            }
        }
        
        // CRITICAL: Watch for chapter changes WITHIN the TTS screen (e.g., auto-next chapter)
        // The initial LaunchedEffect only triggers on screen entry, not when chapter changes internally
        LaunchedEffect(currentChapter?.id) {
            val chapter = currentChapter ?: return@LaunchedEffect
            // Skip if this is the initial chapter (already handled by the LaunchedEffect above)
            if (chapter.id == chapterId) return@LaunchedEffect
            
            // Clear existing translation state FIRST to prevent sync bug
            // This ensures we don't show chapter 200's translation for chapter 220
            translatedContent = null
            showTranslation = false
            ttsService.setCustomContent(null)
            
            // Load translation for the new chapter
            try {
                val translations = getAllTranslationsUseCase.execute(chapter.id)
                if (translations.isNotEmpty()) {
                    val latestTranslation = translations.maxByOrNull { it.updatedAt }
                    latestTranslation?.let { translation ->
                        val translatedStrings = translation.translatedContent
                            .filterIsInstance<ireader.core.source.model.Text>()
                            .map { it.text }
                            .filter { it.isNotBlank() }
                        if (translatedStrings.isNotEmpty()) {
                            translatedContent = translatedStrings
                            // Only show translation if user preference is enabled
                            if (readTranslatedText) {
                                showTranslation = true
                                ttsService.setCustomContent(translatedStrings)
                            }
                        }
                    }
                }
                // If no translation found, translatedContent stays null (cleared above)
            } catch (e: Exception) {
                // Translation not available for this chapter
            }
        }
        
        // Auto-scroll to current paragraph when playing - optimized with debounce for low-end devices
        LaunchedEffect(currentParagraph, isPlaying) {
            if (isPlaying && content.isNotEmpty() && currentParagraph < content.size) {
                // Small delay to batch rapid paragraph changes and reduce scroll jank
                delay(100)
                try {
                    lazyListState.animateScrollToItem(
                        index = currentParagraph,
                        scrollOffset = 0
                    )
                } catch (e: Exception) {
                    // Fallback to non-animated scroll if animation fails on low-end device
                    lazyListState.scrollToItem(currentParagraph)
                }
            }
        }
        
        // Auto-translate next chapter preference
        var autoTranslateNextChapter by remember { mutableStateOf(readerPreferences.autoTranslateNextChapter().get()) }
        
        // Observe translation progress and reload when complete
        val translationProgress by translationService.translationProgress.collectAsState()
        LaunchedEffect(translationProgress) {
            val chapter = currentChapter ?: return@LaunchedEffect
            val progress = translationProgress[chapter.id] ?: return@LaunchedEffect
            
            // When translation completes, reload the translated content
            if (progress.status == ireader.domain.services.common.TranslationStatus.COMPLETED) {
                try {
                    val translations = getAllTranslationsUseCase.execute(chapter.id)
                    if (translations.isNotEmpty()) {
                        val latestTranslation = translations.maxByOrNull { it.updatedAt }
                        latestTranslation?.let { translation ->
                            val translatedStrings = translation.translatedContent
                                .filterIsInstance<ireader.core.source.model.Text>()
                                .map { it.text }
                                .filter { it.isNotBlank() }
                            if (translatedStrings.isNotEmpty()) {
                                translatedContent = translatedStrings
                                if (readTranslatedText) {
                                    showTranslation = true
                                    ttsService.setCustomContent(translatedStrings)
                                }
                            }
                        }
                    }
                    
                    // Auto-translate next chapter if enabled
                    if (autoTranslateNextChapter) {
                        val currentIndex = chapters.indexOfFirst { it.id == chapter.id }
                        if (currentIndex != -1 && currentIndex < chapters.size - 1) {
                            val nextChapter = chapters[currentIndex + 1]
                            
                            // Check if next chapter already has translation
                            val nextTranslations = getAllTranslationsUseCase.execute(nextChapter.id)
                            if (nextTranslations.isEmpty()) {
                                // Queue next chapter for translation
                                val engineId = translationEnginesManager.get().id
                                val sourceLang = readerPreferences.translatorOriginLanguage().get()
                                val targetLang = readerPreferences.translatorTargetLanguage().get()
                                
                                translationService.queueChapters(
                                    bookId = bookId,
                                    chapterIds = listOf(nextChapter.id),
                                    sourceLanguage = sourceLang,
                                    targetLanguage = targetLang,
                                    engineId = engineId,
                                    bypassWarning = true,
                                    priority = false // Background translation
                                )
                            }
                        }
                    }
                } catch (e: Exception) {
                    // Failed to load translation or queue next chapter
                }
            }
        }
        
        // Cleanup on dispose - stop TTS when leaving screen
        DisposableEffect(Unit) {
            onDispose {
                // Use runBlocking to ensure stop completes before screen is destroyed
                kotlinx.coroutines.runBlocking {
                    ttsService.stop()
                }
            }
        }
        
        // Collect cache status and sleep timer - throttled for low-end devices
        val cachedParagraphs by remember(ttsService) {
            ttsService.state.cachedParagraphs.debounce(200) // Reduce cache indicator updates
        }.collectAsState(initial = emptySet())
        val loadingParagraphs by remember(ttsService) {
            ttsService.state.loadingParagraphs.debounce(100)
        }.collectAsState(initial = emptySet())
        val sleepTimeRemaining by remember(ttsService) {
            ttsService.state.sleepTimeRemaining.debounce(1000) // Update once per second max
        }.collectAsState(initial = 0L)
        val sleepModeEnabledState by ttsService.state.sleepModeEnabled.collectAsState()
        val previousParagraph by remember(ttsService) {
            ttsService.state.previousParagraph.debounce(50)
        }.collectAsState(initial = 0)
        
        // Collect merged chunk state for text merging feature
        val mergedChunkParagraphs by ttsService.state.currentMergedChunkParagraphs.collectAsState()
        val isMergingEnabled by ttsService.state.isMergingEnabled.collectAsState()
        
        // Sync sleep mode UI with service state
        LaunchedEffect(sleepModeEnabledState, sleepTimeRemaining) {
            sleepModeEnabled = sleepModeEnabledState
            if (sleepTimeRemaining > 0) {
                sleepTimeMinutes = (sleepTimeRemaining / 60000).toInt().coerceAtLeast(1)
            }
        }
        
        // TTS Speed Calibration - ROLLING AVERAGE approach
        // Instead of calibrating once, we continuously improve accuracy with each paragraph
        var calibratedWPM by remember { mutableStateOf<Float?>(null) }
        var isCalibrated by remember { mutableStateOf(false) }
        var lastParagraphStartTime by remember { mutableStateOf(0L) }
        var lastParagraphWordCount by remember { mutableStateOf(0) }
        var lastParagraphIndex by remember { mutableStateOf(-1) }
        // Rolling average: keep last 3 WPM measurements for stability
        var wpmHistory by remember { mutableStateOf(listOf<Float>()) }
        
        // Track when each paragraph starts for calibration
        LaunchedEffect(paragraphSpeakingStartTime, currentParagraph, isPlaying) {
            if (content.isNotEmpty() && isPlaying && paragraphSpeakingStartTime > 0) {
                // When a new paragraph starts, calculate WPM from the previous one
                if (lastParagraphIndex >= 0 && lastParagraphIndex != currentParagraph && 
                    lastParagraphStartTime > 0 && lastParagraphWordCount > 0) {
                    
                    val paragraphDuration = paragraphSpeakingStartTime - lastParagraphStartTime
                    // Only use measurements from paragraphs that took reasonable time
                    if (paragraphDuration > 300 && paragraphDuration < 60000) {
                        val measuredWPM = SentenceHighlighter.calculateCalibratedWPM(
                            lastParagraphWordCount,
                            paragraphDuration,
                            speechSpeed
                        )
                        // Add to history, keep last 3 measurements
                        wpmHistory = (wpmHistory + measuredWPM).takeLast(3)
                        // Use average of measurements for stability
                        calibratedWPM = wpmHistory.average().toFloat()
                        isCalibrated = true
                    }
                }
                
                // Record current paragraph info for next calibration
                lastParagraphIndex = currentParagraph
                lastParagraphStartTime = paragraphSpeakingStartTime
                lastParagraphWordCount = if (currentParagraph < content.size) {
                    SentenceHighlighter.countTotalWords(content[currentParagraph])
                } else 0
            }
        }
        
        // Reset calibration when chapter changes
        LaunchedEffect(chapterId) {
            calibratedWPM = null
            isCalibrated = false
            lastParagraphStartTime = 0L
            lastParagraphWordCount = 0
            lastParagraphIndex = -1
            wpmHistory = emptyList()
        }
        
        // Sentence highlighting preference
        var sentenceHighlightEnabled by remember { 
            mutableStateOf(readerPreferences.ttsSentenceHighlight().get()) 
        }
        
        // Memoize chapter and book names to avoid string allocations
        val chapterName by remember(currentChapter) {
            derivedStateOf { currentChapter?.name ?: "" }
        }
        val bookTitle by remember(currentBook) {
            derivedStateOf { currentBook?.title ?: "" }
        }
        
        // Memoize engine info to avoid repeated calls
        val currentEngineName = remember(ttsService) { ttsService.getCurrentEngineName() }
        val availableEngines = remember(ttsService) { ttsService.getAvailableEngines() }
        
        // Create state object for unified components - memoized to reduce object allocations
        val screenState by remember(
            currentParagraph, previousParagraph, isPlaying, isLoading, isTTSReady, content,
            translatedContent, showTranslation, bilingualMode, chapterName, bookTitle,
            speechSpeed, autoNextChapter, fullScreenMode, cachedParagraphs, loadingParagraphs,
            sleepTimeRemaining, sleepModeEnabledState, paragraphSpeakingStartTime, sentenceHighlightEnabled,
            calibratedWPM, isCalibrated, mergedChunkParagraphs, isMergingEnabled
        ) {
            derivedStateOf {
                CommonTTSScreenState(
                    currentReadingParagraph = currentParagraph,
                    previousReadingParagraph = previousParagraph,
                    isPlaying = isPlaying,
                    isLoading = isLoading,
                    content = content,
                    translatedContent = translatedContent,
                    showTranslation = showTranslation,
                    bilingualMode = bilingualMode,
                    chapterName = chapterName,
                    bookTitle = bookTitle,
                    speechSpeed = speechSpeed,
                    autoNextChapter = autoNextChapter,
                    fullScreenMode = fullScreenMode,
                    cachedParagraphs = cachedParagraphs,
                    loadingParagraphs = loadingParagraphs,
                    sleepTimeRemaining = sleepTimeRemaining,
                    sleepModeEnabled = sleepModeEnabledState,
                    hasDownloadFeature = false,
                    currentEngine = currentEngineName,
                    availableEngines = availableEngines,
                    paragraphStartTime = paragraphSpeakingStartTime,
                    sentenceHighlightEnabled = sentenceHighlightEnabled,
                    calibratedWPM = calibratedWPM,
                    isCalibrated = isCalibrated,
                    isTTSReady = isTTSReady,
                    mergedChunkParagraphs = mergedChunkParagraphs,
                    isMergingEnabled = isMergingEnabled
                )
            }
        }
        
        // Create actions - memoized to prevent recreation on every recomposition
        val actions = remember(scope, ttsService) {
            object : CommonTTSActions {
                override fun onPlay() { scope.launch { ttsService.play() } }
                override fun onPause() { scope.launch { ttsService.pause() } }
                override fun onNextParagraph() { scope.launch { ttsService.nextParagraph() } }
                override fun onPreviousParagraph() { scope.launch { ttsService.previousParagraph() } }
                override fun onNextChapter() { scope.launch { ttsService.nextChapter() } }
                override fun onPreviousChapter() { scope.launch { ttsService.previousChapter() } }
                override fun onParagraphClick(index: Int) { scope.launch { ttsService.jumpToParagraph(index) } }
                override fun onToggleTranslation() { showTranslation = !showTranslation }
                override fun onToggleBilingualMode() { bilingualMode = !bilingualMode }
                override fun onToggleFullScreen() { fullScreenMode = !fullScreenMode }
                override fun onSpeedChange(speed: Float) { ttsService.setSpeed(speed) }
                override fun onAutoNextChange(enabled: Boolean) { 
                    ttsService.setAutoNextChapter(enabled)
                }
                override fun onOpenSettings() { showSettings = true }
                override fun onSelectVoice() { showVoiceSelection = true }
                override fun onSelectEngine(engine: String) { 
                    showEngineSettings = true 
                }
            }
        }
        
        // Sorted chapters for drawer - memoized to avoid repeated sorting
        val sortedChapters by remember(chapters, chaptersAscending) {
            derivedStateOf {
                if (chaptersAscending) chapters else chapters.asReversed()
            }
        }
        
        // Drawer scroll state
        val drawerScrollState = rememberLazyListState()
        
        // Scroll to current chapter when drawer opens
        LaunchedEffect(drawerState.targetValue) {
            if (currentChapter != null && drawerState.targetValue == DrawerValue.Open && chapters.isNotEmpty()) {
                val index = sortedChapters.indexOfFirst { it.id == currentChapter?.id }
                if (index != -1) {
                    scope.launch {
                        drawerScrollState.scrollToItem(
                            index,
                            -drawerScrollState.layoutInfo.viewportEndOffset / 2
                        )
                    }
                }
            }
        }
        
        // Main UI with Chapter Drawer (using IModalDrawer like ReaderScreenSpec)
        IModalDrawer(
            state = drawerState,
            sheetContent = {
                AnimatedVisibility(
                    visible = true,
                    enter = slideInVertically(initialOffsetY = { -it }),
                    exit = slideOutVertically(targetOffsetY = { -it })
                ) {
                    TTSScreenDrawer(
                        onReverseIcon = {
                            chaptersAscending = !chaptersAscending
                        },
                        onChapter = { ch ->
                            // Capture playing state BEFORE any changes
                            val wasPlaying = isPlaying
                            
                            scope.launch {
                                drawerState.close()
                                
                                // CRITICAL: Clear translation state IMMEDIATELY when user selects a new chapter
                                // Don't wait for LaunchedEffect - clear it here to prevent showing wrong translation
                                translatedContent = null
                                showTranslation = false
                                
                                // Load translation for the new chapter FIRST (before starting TTS)
                                var translatedStringsToUse: List<String>? = null
                                try {
                                    val translations = getAllTranslationsUseCase.execute(ch.id)
                                    if (translations.isNotEmpty()) {
                                        val latestTranslation = translations.maxByOrNull { it.updatedAt }
                                        latestTranslation?.let { translation ->
                                            val translatedStrings = translation.translatedContent
                                                .filterIsInstance<ireader.core.source.model.Text>()
                                                .map { it.text }
                                                .filter { it.isNotBlank() }
                                            if (translatedStrings.isNotEmpty()) {
                                                translatedContent = translatedStrings
                                                if (readTranslatedText) {
                                                    showTranslation = true
                                                    translatedStringsToUse = translatedStrings
                                                }
                                            }
                                        }
                                    }
                                } catch (e: Exception) {
                                    // Translation not available for this chapter
                                }
                                
                                // Set translated content BEFORE starting TTS
                                ttsService.setCustomContent(translatedStringsToUse)
                                
                                // Start reading the new chapter (this will stop current playback internally)
                                ttsService.startReading(bookId, ch.id, autoPlay = wasPlaying)
                            }
                        },
                        chapter = currentChapter,
                        chapters = sortedChapters,
                        drawerScrollState = drawerScrollState,
                        onMap = { drawer ->
                            scope.launch {
                                try {
                                    val index = sortedChapters.indexOfFirst { it.id == currentChapter?.id }
                                    if (index != -1) {
                                        drawer.scrollToItem(
                                            index,
                                            -drawer.layoutInfo.viewportEndOffset / 2
                                        )
                                    }
                                } catch (e: Throwable) {
                                    // Ignore scroll errors
                                }
                            }
                        }
                    )
                }
            }
        ) {
        IScaffold(
            topBar = {
                if (!fullScreenMode) {
                    TopAppBar(
                        title = {
                            Column {
                                Text(
                                    text = localizeHelper.localize(Res.string.text_to_speech),
                                    style = MaterialTheme.typography.titleMedium
                                )
                                if (currentChapter != null) {
                                    Text(
                                        text = currentChapter?.name ?: "",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        },
                        navigationIcon = {
                            TopAppBarBackButton(onClick = { navController.popBackStack() })
                        },
                        actions = {
                            // Translate current chapter button
                            IconButton(
                                onClick = {
                                    val chapter = currentChapter ?: return@IconButton
                                    if (isTranslatingChapter) return@IconButton
                                    
                                    scope.launch {
                                        isTranslatingChapter = true
                                        try {
                                            val engineId = translationEnginesManager.get().id
                                            val sourceLang = readerPreferences.translatorOriginLanguage().get()
                                            val targetLang = readerPreferences.translatorTargetLanguage().get()
                                            
                                            val result = translationService.queueChapters(
                                                bookId = bookId,
                                                chapterIds = listOf(chapter.id),
                                                sourceLanguage = sourceLang,
                                                targetLanguage = targetLang,
                                                engineId = engineId,
                                                bypassWarning = true,
                                                priority = true
                                            )
                                            
                                            when (result) {
                                                is ServiceResult.Success -> {
                                                    // Translation started - will show notification
                                                }
                                                is ServiceResult.Error -> {
                                                    // Show error (could add snackbar here)
                                                }
                                                else -> {}
                                            }
                                        } finally {
                                            isTranslatingChapter = false
                                        }
                                    }
                                },
                                enabled = currentChapter != null && !isTranslatingChapter
                            ) {
                                if (isTranslatingChapter) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(
                                        Icons.Default.Translate,
                                        contentDescription = "Translate Chapter",
                                        tint = if (translatedContent != null) 
                                            MaterialTheme.colorScheme.primary 
                                        else 
                                            LocalContentColor.current
                                    )
                                }
                            }
                            // Download chapter audio button (only for Gradio engines when caching is enabled)
                            if (useGradioTTS && readerPreferences.ttsChapterCacheEnabled().get()) {
                                IconButton(
                                    onClick = {
                                        // TODO: Implement chapter audio download
                                        // This would call TTSChapterCache to download and cache the entire chapter
                                    },
                                    enabled = currentChapter != null && !isLoading
                                ) {
                                    Icon(
                                        Icons.Default.Download,
                                        contentDescription = "Download Chapter Audio",
                                        tint = LocalContentColor.current
                                    )
                                }
                            }
                            // Chapter drawer button
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(Icons.Default.List, "Chapters")
                            }
                            IconButton(onClick = { showSettings = true }) {
                                Icon(Icons.Default.Settings, "Settings")
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(if (fullScreenMode) PaddingValues(0.dp) else paddingValues)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Content area
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        TTSContentDisplay(
                            state = screenState,
                            actions = actions,
                            lazyListState = lazyListState,
                            backgroundColor = resolvedBackgroundColor,
                            textColor = resolvedTextColor,
                            highlightColor = MaterialTheme.colorScheme.primaryContainer,
                            fontSize = fontSize,
                            textAlignment = textAlignment,
                            isTabletOrDesktop = isTabletOrDesktop,
                            modifier = Modifier.fillMaxSize()
                        )
                        
                        // Floating controls for fullscreen mode
                        if (fullScreenMode) {
                            FloatingFullscreenControlsCommon(
                                state = screenState,
                                actions = actions,
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(16.dp)
                            )
                        }
                    }
                    
                    // Media controls at bottom (hidden in fullscreen)
                    if (!fullScreenMode) {
                        TTSMediaControls(
                            state = screenState,
                            actions = actions,
                            isTabletOrDesktop = isTabletOrDesktop,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                
                // Settings Panel (overlay)
                if (showSettings) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.5f))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { showSettings = false }
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        TTSSettingsPanelCommon(
                            useCustomColors = useCustomColors,
                            customBackgroundColor = customBackgroundColor,
                            customTextColor = customTextColor,
                            fontSize = fontSize,
                            textAlignment = textAlignment,
                            sleepModeEnabled = sleepModeEnabled,
                            sleepTimeMinutes = sleepTimeMinutes,
                            speechSpeed = speechSpeed,
                            autoNextChapter = autoNextChapter,
                            useGradioTTS = useGradioTTS,
                            currentEngineName = ttsService.getCurrentEngineName(),
                            readTranslatedText = readTranslatedText,
                            hasTranslation = translatedContent != null && translatedContent!!.isNotEmpty(),
                            onUseCustomColorsChange = { 
                                useCustomColors = it
                                scope.launch { readerPreferences.ttsUseCustomColors().set(it) }
                            },
                            onBackgroundColorChange = { color ->
                                customBackgroundColor = color
                                // Use toArgb() to get proper ARGB Int value for storage
                                scope.launch { readerPreferences.ttsBackgroundColor().set(color.toArgb().toLong()) }
                            },
                            onTextColorChange = { color ->
                                customTextColor = color
                                // Use toArgb() to get proper ARGB Int value for storage
                                scope.launch { readerPreferences.ttsTextColor().set(color.toArgb().toLong()) }
                            },
                            onFontSizeChange = { 
                                fontSize = it
                                scope.launch { readerPreferences.ttsFontSize().set(it) }
                            },
                            onTextAlignmentChange = { alignment ->
                                textAlignment = alignment
                                val prefAlignment = when (alignment) {
                                    TextAlign.Start -> ireader.domain.models.prefs.PreferenceValues.PreferenceTextAlignment.Left
                                    TextAlign.Center -> ireader.domain.models.prefs.PreferenceValues.PreferenceTextAlignment.Center
                                    TextAlign.End -> ireader.domain.models.prefs.PreferenceValues.PreferenceTextAlignment.Right
                                    TextAlign.Justify -> ireader.domain.models.prefs.PreferenceValues.PreferenceTextAlignment.Justify
                                    else -> ireader.domain.models.prefs.PreferenceValues.PreferenceTextAlignment.Left
                                }
                                scope.launch { readerPreferences.ttsTextAlignment().set(prefAlignment) }
                            },
                            onSleepModeChange = { enabled ->
                                sleepModeEnabled = enabled
                                scope.launch { readerPreferences.sleepMode().set(enabled) }
                                if (enabled) {
                                    ttsService.setSleepTimer(sleepTimeMinutes)
                                } else {
                                    ttsService.cancelSleepTimer()
                                }
                            },
                            onSleepTimeChange = { minutes ->
                                sleepTimeMinutes = minutes
                                scope.launch { readerPreferences.sleepTime().set(minutes.toLong()) }
                                if (sleepModeEnabled) {
                                    ttsService.setSleepTimer(minutes)
                                }
                            },
                            onSpeedChange = { ttsService.setSpeed(it) },
                            onAutoNextChange = { enabled -> 
                                ttsService.setAutoNextChapter(enabled)
                            },
                            onCoquiTTSChange = { enabled ->
                                if (isGradioConfigured) {
                                    useGradioTTS = enabled
                                    scope.launch { appPreferences.useGradioTTS().set(enabled) }
                                }
                            },
                            onReadTranslatedTextChange = { enabled ->
                                readTranslatedText = enabled
                                // Also update the showTranslation state to sync with TTS
                                showTranslation = enabled
                                // Update TTS service to read translated or original content
                                if (enabled && translatedContent != null && translatedContent!!.isNotEmpty()) {
                                    ttsService.setCustomContent(translatedContent)
                                } else {
                                    ttsService.setCustomContent(null) // Restore original content
                                }
                                // Save preference
                                scope.launch { readerPreferences.useTTSWithTranslatedText().set(enabled) }
                            },
                            sentenceHighlightEnabled = sentenceHighlightEnabled,
                            onSentenceHighlightChange = { enabled ->
                                sentenceHighlightEnabled = enabled
                                scope.launch { readerPreferences.ttsSentenceHighlight().set(enabled) }
                            },
                            onOpenEngineSettings = {
                                showEngineSettings = true
                                showSettings = false
                            },
                            onDismiss = { showSettings = false },
                            isTabletOrDesktop = isTabletOrDesktop,
                            modifier = Modifier.clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { /* Prevent click through */ }
                        )
                    }
                }
                
                // Engine Settings Screen (platform-specific)
                if (showEngineSettings) {
                    TTSEngineSettingsScreen(
                        isDesktop = isTabletOrDesktop,
                        onDismiss = { showEngineSettings = false },
                        onNavigateToTTSManager = {
                            // Navigate to TTS Manager screen
                            showEngineSettings = false
                            navController.navigate(NavigationRoutes.ttsEngineManager)
                        }
                    )
                }
                
                // Voice Selection Screen (platform-specific)
                if (showVoiceSelection) {
                    TTSVoiceSelectionScreen(
                        isDesktop = isTabletOrDesktop,
                        onDismiss = { showVoiceSelection = false }
                    )
                }
            }
        }
        } // Close IModalDrawer
    }
}

/**
 * TTS Screen Drawer - Chapter selection drawer similar to ReaderScreenDrawer
 */
@Composable
private fun TTSScreenDrawer(
    modifier: Modifier = Modifier,
    chapter: Chapter?,
    onChapter: (chapter: Chapter) -> Unit,
    chapters: List<Chapter>,
    onReverseIcon: () -> Unit,
    drawerScrollState: LazyListState,
    onMap: (LazyListState) -> Unit,
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    Column(
        modifier = modifier.fillMaxWidth(0.9f),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(5.dp))
        Box(modifier = Modifier.fillMaxWidth()) {
            BigSizeTextComposable(
                text = localize(Res.string.content),
                modifier = Modifier.align(Alignment.Center)
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth()
            ) {
                AppIconButton(
                    imageVector = Icons.Filled.Place,
                    contentDescription = localize(Res.string.find_current_chapter),
                    onClick = { onMap(drawerScrollState) }
                )
                AppIconButton(
                    imageVector = Icons.Default.Sort,
                    contentDescription = localize(Res.string.reverse),
                    onClick = { onReverseIcon() }
                )
            }
        }

        Spacer(modifier = Modifier.height(5.dp))
        HorizontalDivider(modifier = Modifier.fillMaxWidth(), thickness = 1.dp)
        
        IVerticalFastScroller(listState = drawerScrollState) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = drawerScrollState
            ) {
                items(count = chapters.size) { index ->
                    ChapterRow(
                        modifier = Modifier,
                        chapter = chapters[index],
                        onItemClick = { onChapter(chapters[index]) },
                        isLastRead = chapter?.id == chapters[index].id,
                    )
                }
            }
        }
        
        if (chapters.isEmpty()) {
            Text(
                text = localizeHelper.localize(Res.string.no_chapters_available),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Floating controls for fullscreen mode
 */
@Composable
private fun FloatingFullscreenControlsCommon(
    state: CommonTTSScreenState,
    actions: CommonTTSActions,
    modifier: Modifier = Modifier
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    val hasTranslation = state.translatedContent != null && state.translatedContent.isNotEmpty()
    
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Translation toggle
        if (hasTranslation) {
            SmallFloatingActionButton(
                onClick = { actions.onToggleTranslation() },
                containerColor = if (state.showTranslation)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
            ) {
                Icon(
                    imageVector = Icons.Default.Translate,
                    contentDescription = localizeHelper.localize(Res.string.toggle_translation),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        
        // Play/Pause
        FloatingActionButton(
            onClick = {
                if (state.isPlaying) actions.onPause() else actions.onPlay()
            },
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f),
            shape = CircleShape
        ) {
            Icon(
                imageVector = if (state.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (state.isPlaying) "Pause" else "Play"
            )
        }
        
        // Exit fullscreen
        SmallFloatingActionButton(
            onClick = { actions.onToggleFullScreen() },
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
        ) {
            Icon(
                imageVector = Icons.Default.FullscreenExit,
                contentDescription = localizeHelper.localize(Res.string.exit_fullscreen),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

