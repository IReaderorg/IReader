package ireader.presentation.ui.home.tts.v2

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import ireader.domain.services.tts_service.v2.*
import ireader.presentation.ui.home.tts.CommonTTSActions
import ireader.presentation.ui.home.tts.CommonTTSScreenState
import ireader.presentation.ui.home.tts.TTSContentDisplay
import ireader.presentation.ui.home.tts.TTSMediaControls
import kotlinx.coroutines.flow.collectLatest
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.i18n.resources.*

/**
 * Adapter that bridges TTS v2 state to CommonTTSScreenState
 * 
 * This allows the existing CommonTTSScreen UI to work with the new v2 architecture
 * without requiring a complete UI rewrite.
 */
@Composable
fun rememberTTSV2StateAdapter(
    adapter: TTSViewModelAdapter,
    sleepTimerState: TTSSleepTimerUseCase.SleepTimerState? = null
): CommonTTSScreenState {
    val state by adapter.state.collectAsState()
    val isPlaying by adapter.isPlaying.collectAsState()
    val isLoading by adapter.isLoading.collectAsState()
    val currentParagraph by adapter.currentParagraph.collectAsState()
    val previousParagraph by adapter.previousParagraph.collectAsState()
    val chapterTitle by adapter.chapterTitle.collectAsState()
    val bookTitle by adapter.bookTitle.collectAsState()
    val speed by adapter.speed.collectAsState()
    val chunkModeEnabled by adapter.chunkModeEnabled.collectAsState()
    val currentChunkIndex by adapter.currentChunkIndex.collectAsState()
    val totalChunks by adapter.totalChunks.collectAsState()
    val isEngineReady by adapter.isEngineReady.collectAsState()
    val engineType by adapter.engineType.collectAsState()
    // Translation state
    val showTranslation by adapter.showTranslation.collectAsState()
    val bilingualMode by adapter.bilingualMode.collectAsState()
    val translatedParagraphs by adapter.translatedParagraphs.collectAsState()
    // Sentence highlighting state
    val paragraphStartTime by adapter.paragraphStartTime.collectAsState()
    val sentenceHighlightEnabled by adapter.sentenceHighlightEnabled.collectAsState()
    val calibratedWPM by adapter.calibratedWPM.collectAsState()
    val isCalibrated by adapter.isCalibrated.collectAsState()
    
    return remember(
        state, isPlaying, isLoading, currentParagraph, previousParagraph,
        chapterTitle, bookTitle, speed, chunkModeEnabled, currentChunkIndex,
        totalChunks, isEngineReady, engineType, sleepTimerState,
        showTranslation, bilingualMode, translatedParagraphs,
        paragraphStartTime, sentenceHighlightEnabled, calibratedWPM, isCalibrated
    ) {
        derivedStateOf {
            CommonTTSScreenState(
                currentReadingParagraph = currentParagraph,
                previousReadingParagraph = previousParagraph,
                isPlaying = isPlaying,
                isLoading = isLoading,
                content = state.paragraphs,
                translatedContent = translatedParagraphs,
                showTranslation = showTranslation,
                bilingualMode = bilingualMode,
                chapterName = chapterTitle,
                bookTitle = bookTitle,
                speechSpeed = speed,
                autoNextChapter = state.autoNextChapter,
                fullScreenMode = false,
                cachedParagraphs = state.cachedParagraphs,
                loadingParagraphs = state.loadingParagraphs,
                sleepTimeRemaining = sleepTimerState?.remainingTimeMs ?: 0L,
                sleepModeEnabled = sleepTimerState?.isEnabled == true,
                currentEngine = when (engineType) {
                    EngineType.NATIVE -> "Native TTS"
                    EngineType.GRADIO -> "Gradio TTS"
                },
                availableEngines = listOf("Native TTS", "Gradio TTS"),
                isTTSReady = isEngineReady,
                // Sentence highlighting
                paragraphStartTime = paragraphStartTime,
                sentenceHighlightEnabled = sentenceHighlightEnabled,
                calibratedWPM = calibratedWPM,
                isCalibrated = isCalibrated,
                // Chunk mode
                mergedChunkParagraphs = state.currentChunkParagraphs,
                isMergingEnabled = chunkModeEnabled,
                currentMergedChunkIndex = currentChunkIndex,
                totalMergedChunks = totalChunks,
                usingCachedAudio = state.isUsingCachedAudio
            )
        }.value
    }
}

/**
 * Creates CommonTTSActions that dispatch to TTSViewModelAdapter
 */
@Composable
fun rememberTTSV2Actions(
    adapter: TTSViewModelAdapter,
    onToggleFullScreen: () -> Unit = {},
    onOpenSettings: () -> Unit = {}
): CommonTTSActions {
    return remember(adapter, onToggleFullScreen, onOpenSettings) {
        object : CommonTTSActions {
            override fun onPlay() = adapter.play()
            override fun onPause() = adapter.pause()
            
            override fun onNextParagraph() {
                if (adapter.state.value.chunkModeEnabled) {
                    adapter.nextChunk()
                } else {
                    adapter.nextParagraph()
                }
            }
            
            override fun onPreviousParagraph() {
                if (adapter.state.value.chunkModeEnabled) {
                    adapter.previousChunk()
                } else {
                    adapter.previousParagraph()
                }
            }
            
            override fun onNextChapter() = adapter.nextChapter()
            override fun onPreviousChapter() = adapter.previousChapter()
            override fun onParagraphClick(index: Int) = adapter.jumpToParagraph(index)
            override fun onToggleTranslation() = adapter.toggleTranslation()
            override fun onToggleBilingualMode() = adapter.toggleBilingualMode()
            override fun onToggleFullScreen() = onToggleFullScreen()
            override fun onSpeedChange(speed: Float) = adapter.setSpeed(speed)
            override fun onAutoNextChange(enabled: Boolean) = adapter.setAutoNextChapter(enabled)
            
            override fun onSelectEngine(engine: String) {
                when (engine) {
                    "Native TTS" -> adapter.useNativeTTS()
                    "Gradio TTS" -> adapter.setEngine(EngineType.GRADIO)
                }
            }
            
            override fun onOpenSettings() = onOpenSettings()
        }
    }
}


/**
 * TTS V2 Screen using CommonTTSScreen components
 * 
 * This screen uses the existing CommonTTSScreen UI (TTSContentDisplay, TTSMediaControls)
 * with the new v2 backend architecture.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TTSV2CommonScreen(
    adapter: TTSViewModelAdapter,
    sleepTimerState: TTSSleepTimerUseCase.SleepTimerState? = null,
    onSleepTimerStart: ((Int) -> Unit)? = null,
    onSleepTimerCancel: (() -> Unit)? = null,
    onBack: () -> Unit,
    onOpenSettings: () -> Unit = {},
    // Display customization
    backgroundColor: Color = MaterialTheme.colorScheme.background,
    textColor: Color = MaterialTheme.colorScheme.onBackground,
    highlightColor: Color = MaterialTheme.colorScheme.primaryContainer,
    fontSize: Int = 18,
    textAlignment: TextAlign = TextAlign.Start,
    lineHeight: Int = 24,
    paragraphIndent: Int = 0,
    paragraphDistance: Int = 8,
    fontWeight: Int = 400,
    isTabletOrDesktop: Boolean = false,
    modifier: Modifier = Modifier
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    // Convert v2 state to CommonTTSScreenState
    val commonState = rememberTTSV2StateAdapter(adapter, sleepTimerState)
    
    // Create actions that dispatch to v2
    var fullScreenMode by remember { mutableStateOf(false) }
    val actions = rememberTTSV2Actions(
        adapter = adapter,
        onToggleFullScreen = { fullScreenMode = !fullScreenMode },
        onOpenSettings = onOpenSettings
    )
    
    // LazyList state for content scrolling
    val lazyListState = rememberLazyListState()
    
    // Auto-scroll to current paragraph
    val currentParagraph by adapter.currentParagraph.collectAsState()
    LaunchedEffect(currentParagraph) {
        if (currentParagraph > 0 && commonState.hasContent) {
            lazyListState.animateScrollToItem(currentParagraph)
        }
    }
    
    // Snackbar for errors
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Handle events
    LaunchedEffect(Unit) {
        adapter.events.collectLatest { event ->
            when (event) {
                is TTSEvent.Error -> {
                    val message = when (event.error) {
                        is TTSError.NoContent -> "No content to read"
                        is TTSError.EngineNotReady -> "TTS engine not ready"
                        is TTSError.SpeechFailed -> "Speech failed: ${(event.error as TTSError.SpeechFailed).message}"
                        is TTSError.ContentLoadFailed -> "Failed to load content"
                        is TTSError.NetworkError -> "Network error"
                        is TTSError.EngineInitFailed -> "Engine initialization failed"
                        else -> "An error occurred"
                    }
                    snackbarHostState.showSnackbar(message)
                }
                is TTSEvent.ChapterCompleted -> {
                    snackbarHostState.showSnackbar("Chapter completed")
                }
                else -> {}
            }
        }
    }
    
    // Sleep timer dialog
    var showSleepTimerDialog by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            if (!fullScreenMode) {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = commonState.chapterName.ifEmpty { "TTS Player" },
                                style = MaterialTheme.typography.titleMedium
                            )
                            if (commonState.bookTitle.isNotEmpty()) {
                                Text(
                                    text = commonState.bookTitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = localizeHelper.localize(Res.string.back))
                        }
                    },
                    actions = {
                        // Sleep timer button
                        if (onSleepTimerStart != null) {
                            IconButton(onClick = { showSleepTimerDialog = true }) {
                                BadgedBox(
                                    badge = {
                                        if (sleepTimerState?.isEnabled == true) {
                                            Badge { Text(sleepTimerState.remainingMinutes.toString()) }
                                        }
                                    }
                                ) {
                                    Icon(
                                        Icons.Default.Timer,
                                        contentDescription = localizeHelper.localize(Res.string.sleep_timer),
                                        tint = if (sleepTimerState?.isEnabled == true)
                                            MaterialTheme.colorScheme.primary
                                        else
                                            MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                        
                        // Settings button
                        IconButton(onClick = onOpenSettings) {
                            Icon(Icons.Default.Settings, contentDescription = localizeHelper.localize(Res.string.settings))
                        }
                    }
                )
            }
        },
        bottomBar = {
            if (!fullScreenMode) {
                TTSMediaControls(
                    state = commonState,
                    actions = actions,
                    isTabletOrDesktop = isTabletOrDesktop
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .then(
                    if (!fullScreenMode) Modifier.padding(paddingValues)
                    else Modifier
                )
        ) {
            TTSContentDisplay(
                state = commonState,
                actions = actions,
                lazyListState = lazyListState,
                backgroundColor = backgroundColor,
                textColor = textColor,
                highlightColor = highlightColor,
                fontSize = fontSize,
                textAlignment = textAlignment,
                lineHeight = lineHeight,
                paragraphIndent = paragraphIndent,
                paragraphDistance = paragraphDistance,
                fontWeight = fontWeight,
                isTabletOrDesktop = isTabletOrDesktop,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
    
    // Sleep timer dialog
    if (showSleepTimerDialog && onSleepTimerStart != null) {
        SleepTimerDialog(
            currentState = sleepTimerState,
            onStart = { minutes -> onSleepTimerStart(minutes) },
            onCancel = { onSleepTimerCancel?.invoke() },
            onDismiss = { showSleepTimerDialog = false }
        )
    }
}
