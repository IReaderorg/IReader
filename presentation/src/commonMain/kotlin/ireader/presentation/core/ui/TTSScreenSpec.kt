package ireader.presentation.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ireader.domain.preferences.prefs.ReaderPreferences
import ireader.domain.services.tts_service.CommonTTSService
import ireader.domain.usecases.translation.GetAllTranslationsForChapterUseCase
import ireader.presentation.core.LocalNavigator
import ireader.presentation.ui.component.IScaffold
import ireader.presentation.ui.component.isTableUi
import ireader.presentation.ui.component.reusable_composable.TopAppBarBackButton
import ireader.presentation.ui.home.tts.*
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

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
 */
class TTSScreenSpec(
    val bookId: Long,
    val chapterId: Long,
    val sourceId: Long,
    val readingParagraph: Int,
) {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Content() {
        val navController = requireNotNull(LocalNavigator.current) { "LocalNavigator not provided" }
        val ttsService: CommonTTSService = koinInject()
        val readerPreferences: ReaderPreferences = koinInject()
        val getAllTranslationsUseCase: GetAllTranslationsForChapterUseCase = koinInject()
        val scope = rememberCoroutineScope()
        val isTabletOrDesktop = isTableUi()
        
        // Translation state
        var translatedContent by remember { mutableStateOf<List<String>?>(null) }
        
        // Local UI state
        var showTranslation by remember { mutableStateOf(false) }
        var bilingualMode by remember { mutableStateOf(false) }
        var fullScreenMode by remember { mutableStateOf(false) }
        var showSettings by remember { mutableStateOf(false) }
        var showEngineSettings by remember { mutableStateOf(false) }
        
        // Settings state
        var useCustomColors by remember { mutableStateOf(false) }
        var customBackgroundColor by remember { mutableStateOf(Color(0xFF1E1E1E)) }
        var customTextColor by remember { mutableStateOf(Color.White) }
        var fontSize by remember { mutableStateOf(18) }
        var textAlignment by remember { mutableStateOf(TextAlign.Start) }
        var sleepModeEnabled by remember { mutableStateOf(false) }
        var sleepTimeMinutes by remember { mutableStateOf(30) }
        
        // Collect state from service
        val isPlaying by ttsService.state.isPlaying.collectAsState()
        val isLoading by ttsService.state.isLoading.collectAsState()
        val currentBook by ttsService.state.currentBook.collectAsState()
        val currentChapter by ttsService.state.currentChapter.collectAsState()
        val currentParagraph by ttsService.state.currentParagraph.collectAsState()
        val content by ttsService.state.currentContent.collectAsState()
        val speechSpeed by ttsService.state.speechSpeed.collectAsState()
        val autoNextChapter by ttsService.state.autoNextChapter.collectAsState()
        
        // Determine colors
        val backgroundColor = if (useCustomColors) customBackgroundColor else MaterialTheme.colorScheme.background
        val textColor = if (useCustomColors) customTextColor else MaterialTheme.colorScheme.onBackground
        
        val lazyListState = rememberLazyListState()
        
        // Initialize TTS with the chapter
        LaunchedEffect(bookId, chapterId) {
            ttsService.startReading(bookId, chapterId)
            if (readingParagraph > 0) {
                ttsService.jumpToParagraph(readingParagraph)
            }
            
            // Load translation for this chapter
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
                // Translation not available, continue without it
            }
        }
        
        // Auto-scroll to current paragraph when playing
        LaunchedEffect(currentParagraph, isPlaying) {
            if (isPlaying && content.isNotEmpty() && currentParagraph < content.size) {
                lazyListState.animateScrollToItem(currentParagraph)
            }
        }
        
        // Cleanup on dispose
        DisposableEffect(Unit) {
            onDispose {
                scope.launch { ttsService.pause() }
            }
        }
        
        // Create state object for unified components
        val screenState = CommonTTSScreenState(
            currentReadingParagraph = currentParagraph,
            isPlaying = isPlaying,
            isLoading = isLoading,
            content = content,
            translatedContent = translatedContent,
            showTranslation = showTranslation,
            bilingualMode = bilingualMode,
            chapterName = currentChapter?.name ?: "",
            bookTitle = currentBook?.title ?: "",
            speechSpeed = speechSpeed,
            autoNextChapter = autoNextChapter,
            fullScreenMode = fullScreenMode,
            hasDownloadFeature = false, // Platform-specific, can be enabled via expect/actual if needed
            currentEngine = ttsService.getCurrentEngineName(),
            availableEngines = ttsService.getAvailableEngines()
        )
        
        // Create actions
        val actions = object : CommonTTSActions {
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
                scope.launch { readerPreferences.readerAutoNext().set(enabled) }
            }
            override fun onOpenSettings() { showSettings = true }
        }
        
        // Main UI
        IScaffold(
            topBar = {
                if (!fullScreenMode) {
                    TopAppBar(
                        title = {
                            Column {
                                Text(
                                    text = "Text-to-Speech",
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
                            backgroundColor = backgroundColor,
                            textColor = textColor,
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
                            useCoquiTTS = false, // TODO: Add Coqui TTS preference when implemented
                            currentEngineName = ttsService.getCurrentEngineName(),
                            onUseCustomColorsChange = { useCustomColors = it },
                            onBackgroundColorChange = { customBackgroundColor = it },
                            onTextColorChange = { customTextColor = it },
                            onFontSizeChange = { fontSize = it },
                            onTextAlignmentChange = { textAlignment = it },
                            onSleepModeChange = { sleepModeEnabled = it },
                            onSleepTimeChange = { sleepTimeMinutes = it },
                            onSpeedChange = { ttsService.setSpeed(it) },
                            onAutoNextChange = { enabled -> 
                                scope.launch { readerPreferences.readerAutoNext().set(enabled) }
                            },
                            onCoquiTTSChange = { /* TODO: Implement Coqui TTS toggle */ },
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
                        onDismiss = { showEngineSettings = false }
                    )
                }
            }
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
                    contentDescription = "Toggle Translation",
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        
        // Play/Pause
        FloatingActionButton(
            onClick = {
                if (state.isPlaying) actions.onPause() else actions.onPlay()
            },
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f)
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
                contentDescription = "Exit Fullscreen",
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

