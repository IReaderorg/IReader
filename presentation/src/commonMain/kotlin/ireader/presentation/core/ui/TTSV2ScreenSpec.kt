package ireader.presentation.core.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ireader.core.log.Log
import ireader.core.source.model.Text
import ireader.domain.data.repository.ChapterRepository
import ireader.domain.models.entities.Chapter
import ireader.domain.preferences.prefs.AppPreferences
import ireader.domain.preferences.prefs.ReaderPreferences
import ireader.domain.services.common.TranslationService
import ireader.domain.services.common.TranslationStatus
import ireader.domain.services.processstate.ProcessStateManager
import ireader.domain.services.processstate.TTSProcessState
import ireader.domain.services.tts_service.GradioTTSManager
import ireader.domain.services.tts_service.TTSChapterCache
import ireader.domain.services.tts_service.TTSChapterDownloadManager
import ireader.domain.services.tts_service.TTSTextMerger
import ireader.domain.services.tts_service.createTTSDownloadIntentProvider
import ireader.domain.services.tts_service.v2.EngineType
import ireader.domain.services.tts_service.v2.GradioConfig
import ireader.domain.services.tts_service.v2.TTSController
import ireader.domain.services.tts_service.v2.TTSNotificationUseCase
import ireader.domain.services.tts_service.v2.TTSPreferencesUseCase
import ireader.domain.services.tts_service.v2.TTSSleepTimerUseCase
import ireader.domain.services.tts_service.v2.TTSV2ServiceStarter
import ireader.domain.usecases.translate.TranslationEnginesManager
import ireader.domain.usecases.translation.GetAllTranslationsForChapterUseCase
import ireader.domain.utils.extensions.currentTimeToLong
import ireader.i18n.localize
import ireader.i18n.resources.*
import ireader.i18n.resources.content
import ireader.i18n.resources.exit_fullscreen
import ireader.i18n.resources.find_current_chapter
import ireader.i18n.resources.no_chapters_available
import ireader.i18n.resources.reverse
import ireader.i18n.resources.text_to_speech
import ireader.i18n.resources.toggle_translation
import ireader.presentation.core.IModalDrawer
import ireader.presentation.core.LocalNavigator
import ireader.presentation.core.NavigationRoutes
import ireader.presentation.core.safePopBackStack
import ireader.presentation.ui.component.IScaffold
import ireader.presentation.ui.component.components.ChapterRow
import ireader.presentation.ui.component.isTableUi
import ireader.presentation.ui.component.list.scrollbars.IVerticalFastScroller
import ireader.presentation.ui.component.reusable_composable.AppIconButton
import ireader.presentation.ui.component.reusable_composable.BigSizeTextComposable
import ireader.presentation.ui.component.reusable_composable.TopAppBarBackButton
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import ireader.presentation.ui.home.tts.CommonTTSActions
import ireader.presentation.ui.home.tts.CommonTTSScreenState
import ireader.presentation.ui.home.tts.SentenceHighlighter
import ireader.presentation.ui.home.tts.TTSContentDisplay
import ireader.presentation.ui.home.tts.TTSEngineSettingsScreen
import ireader.presentation.ui.home.tts.TTSMediaControls
import ireader.presentation.ui.home.tts.TTSSettingsPanelCommon
import ireader.presentation.ui.home.tts.TTSVoiceSelectionScreen
import ireader.presentation.ui.home.tts.v2.SleepTimerDialog
import ireader.presentation.ui.home.tts.v2.TTSV2ViewModelFactory
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

/**
 * TTS V2 Screen Spec - Full-featured TTS screen using v2 architecture
 * 
 * Features:
 * - Clean architecture with single source of truth (TTSState)
 * - Command pattern for all interactions
 * - CommonTTSScreen UI components
 * - Sleep timer, notifications, preferences integration
 * - Chapter drawer for navigation
 * - Settings panel with all TTS options
 * - Translation support with auto-translate next chapter
 * - Sentence highlighting with WPM calibration
 * - Download chapter audio (for Gradio TTS)
 * - Fullscreen mode with floating controls
 */
class TTSV2ScreenSpec(
    val bookId: Long,
    val chapterId: Long,
    val sourceId: Long,
    val readingParagraph: Int = 0
) {
    @OptIn(ExperimentalMaterial3Api::class, FlowPreview::class)
    @Composable
    fun Content() {
        val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
        val navController = requireNotNull(LocalNavigator.current) { "LocalNavigator not provided" }
        val scope = rememberCoroutineScope()
        val isTabletOrDesktop = isTableUi()
        
        // Inject dependencies
        val controller: TTSController = koinInject()
        val sleepTimerUseCase: TTSSleepTimerUseCase = koinInject()
        val notificationUseCase: TTSNotificationUseCase = koinInject()
        val preferencesUseCase: TTSPreferencesUseCase = koinInject()
        val readerPreferences: ReaderPreferences = koinInject()
        val appPreferences: AppPreferences = koinInject()
        val chapterRepository: ChapterRepository = koinInject()
        val getAllTranslationsUseCase: GetAllTranslationsForChapterUseCase = koinInject()
        val translationService: TranslationService = koinInject()
        val translationEnginesManager: TranslationEnginesManager = koinInject()
        val downloadManager: TTSChapterDownloadManager = koinInject()
        val chapterCache: TTSChapterCache = koinInject()
        val serviceStarter: TTSV2ServiceStarter = koinInject()
        val gradioTTSManager: GradioTTSManager = koinInject()
        val processStateManager: ProcessStateManager = koinInject()
        
        // Set up platform-specific intents for notification actions
        LaunchedEffect(Unit) {
            val intentProvider = createTTSDownloadIntentProvider()
            downloadManager.pauseIntent = intentProvider.getPauseIntent()
            downloadManager.cancelIntent = intentProvider.getCancelIntent()
        }
        
        // Download state
        val downloadProgress by downloadManager.progress.collectAsState()
        val downloadState by downloadManager.state.collectAsState()
        
        // Create ViewModel
        val viewModel = remember {
            TTSV2ViewModelFactory(
                controller = controller,
                sleepTimerUseCase = sleepTimerUseCase,
                notificationUseCase = notificationUseCase,
                preferencesUseCase = preferencesUseCase
            ).create()
        }
        
        // Collect v2 state
        val state by viewModel.adapter.state.collectAsState()
        val isPlaying by viewModel.adapter.isPlaying.collectAsState()
        val isLoading by viewModel.adapter.isLoading.collectAsState()
        val currentParagraph by remember(viewModel.adapter) {
            viewModel.adapter.currentParagraph.debounce(50)
        }.collectAsState(initial = 0)
        val previousParagraph by remember(viewModel.adapter) {
            viewModel.adapter.previousParagraph.debounce(50)
        }.collectAsState(initial = 0)
        
        // Chapter drawer state
        val drawerState = rememberDrawerState(DrawerValue.Closed)
        var chapters by remember { mutableStateOf<List<Chapter>>(emptyList()) }
        var chaptersAscending by remember { mutableStateOf(true) }
        
        // Local UI state
        var fullScreenMode by remember { mutableStateOf(false) }
        var showSettings by remember { mutableStateOf(false) }
        var showEngineSettings by remember { mutableStateOf(false) }
        var showVoiceSelection by remember { mutableStateOf(false) }
        var showSleepTimerDialog by remember { mutableStateOf(false) }
        
        // Translation state
        var isTranslatingChapter by remember { mutableStateOf(false) }
        var autoTranslateNextChapter by remember { mutableStateOf(readerPreferences.autoTranslateNextChapter().get()) }
        val readTranslatedText by readerPreferences.useTTSWithTranslatedText().changes().collectAsState(
            initial = readerPreferences.useTTSWithTranslatedText().get()
        )
        
        // Settings state from preferences
        var useCustomColors by remember { mutableStateOf(readerPreferences.ttsUseCustomColors().get()) }
        var customBackgroundColor by remember { 
            mutableStateOf(Color(readerPreferences.ttsBackgroundColor().get().toInt()))
        }
        var customTextColor by remember { 
            mutableStateOf(Color(readerPreferences.ttsTextColor().get().toInt()))
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
        var sentenceHighlightEnabled by remember { mutableStateOf(readerPreferences.ttsSentenceHighlight().get()) }
        
        // Content filter state
        var contentFilterEnabled by remember { mutableStateOf(readerPreferences.contentFilterEnabled().get()) }
        var contentFilterPatterns by remember { mutableStateOf(readerPreferences.contentFilterPatterns().get()) }
        
        // Gradio TTS state - observe changes to react when user changes config in settings
        val useGradioTTS by appPreferences.useGradioTTS().changes().collectAsState(
            initial = appPreferences.useGradioTTS().get()
        )
        val savedGradioConfigId by appPreferences.activeGradioConfigId().changes().collectAsState(
            initial = appPreferences.activeGradioConfigId().get()
        )
        // Use default preset if no config is saved - derivedStateOf for efficient updates
        val activeGradioConfigId by remember {
            derivedStateOf { savedGradioConfigId.ifEmpty { "coqui_ireader" } }
        }
        
        // Selected Piper voice model - observe changes to display in UI
        val selectedPiperModel by appPreferences.selectedPiperModel().changes().collectAsState(
            initial = appPreferences.selectedPiperModel().get()
        )
        // Always consider configured since we have a default preset - derivedStateOf for efficiency
        val isGradioConfigured by remember {
            derivedStateOf { activeGradioConfigId.isNotEmpty() }
        }
        
        // Chunk mode settings - observe changes to re-chunk when user changes word count
        val mergeWordsRemote by readerPreferences.ttsMergeWordsRemote().changes().collectAsState(
            initial = readerPreferences.ttsMergeWordsRemote().get()
        )
        
        // Cache state
        val isChapterCached by remember(state.chapter?.id, state.paragraphs) {
            derivedStateOf { 
                val chId = state.chapter?.id ?: return@derivedStateOf false
                if (chapterCache.isCached(chId)) return@derivedStateOf true
                val cachedChunks = chapterCache.getCachedChunkIndices(chId)
                cachedChunks.isNotEmpty()
            }
        }
        
        val isChapterFullyCached by remember(state.chapter?.id, state.paragraphs) {
            derivedStateOf {
                val chId = state.chapter?.id ?: return@derivedStateOf false
                if (chapterCache.isCached(chId)) return@derivedStateOf true
                val totalChunks = if (mergeWordsRemote > 0 && state.paragraphs.isNotEmpty()) {
                    TTSTextMerger.mergeParagraphs(state.paragraphs, mergeWordsRemote).size
                } else {
                    state.paragraphs.size
                }
                if (totalChunks > 0) {
                    chapterCache.areAllChunksCached(chId, totalChunks)
                } else {
                    false
                }
            }
        }
        
        // Use derivedStateOf to avoid recomposition when downloadState changes to unrelated values
        val isDownloading by remember {
            derivedStateOf {
                downloadState == TTSChapterDownloadManager.DownloadState.DOWNLOADING ||
                    downloadState == TTSChapterDownloadManager.DownloadState.PAUSED
            }
        }
        
        // WPM Calibration state (rolling average)
        var calibratedWPM by remember { mutableStateOf<Float?>(null) }
        var isCalibrated by remember { mutableStateOf(false) }
        var lastParagraphStartTime by remember { mutableStateOf(0L) }
        var lastParagraphWordCount by remember { mutableStateOf(0) }
        var lastParagraphIndex by remember { mutableStateOf(-1) }
        var wpmHistory by remember { mutableStateOf(listOf<Float>()) }
        
        // Memoize colors
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
        
        // Collect sleep timer state
        val sleepTimerState by viewModel.sleepTimerState?.collectAsState()
            ?: remember { mutableStateOf(null) }

        
        // Initialize TTS and load content
        LaunchedEffect(bookId, chapterId) {
            Log.warn { "TTSV2ScreenSpec: Loading chapter bookId=$bookId, chapterId=$chapterId" }
            
            // Load chapters list for drawer
            try {
                chapters = chapterRepository.findChaptersByBookId(bookId)
            } catch (e: Exception) {
                Log.error { "TTSV2ScreenSpec: Failed to load chapters: ${e.message}" }
            }
            
            // Check if controller already has this EXACT book/chapter loaded (e.g., coming from notification)
            // IMPORTANT: Must check BOTH bookId AND chapterId to avoid showing stale data from different book
            val currentState = controller.state.value
            val sameBook = currentState.book?.id == bookId
            val sameChapter = currentState.chapter?.id == chapterId
            val hasContent = currentState.paragraphs.isNotEmpty()
            val alreadyLoaded = sameBook && sameChapter && hasContent
            
            if (alreadyLoaded) {
                Log.warn { "TTSV2ScreenSpec: Same book/chapter already loaded in controller, skipping reload" }
                // Just load translations if needed
                if (readTranslatedText && currentState.translatedParagraphs.isNullOrEmpty()) {
                    try {
                        val translations = getAllTranslationsUseCase.execute(chapterId)
                        if (translations.isNotEmpty()) {
                            val latestTranslation = translations.maxByOrNull { it.updatedAt }
                            latestTranslation?.let { translation ->
                                val translatedStrings = translation.translatedContent
                                    .filterIsInstance<Text>()
                                    .map { it.text }
                                    .filter { it.isNotBlank() }
                                if (translatedStrings.isNotEmpty()) {
                                    viewModel.adapter.setTranslatedContent(translatedStrings)
                                    viewModel.adapter.setShowTranslation(true)
                                }
                            }
                        }
                    } catch (e: Exception) {
                        Log.warn { "TTSV2ScreenSpec: Translation not available: ${e.message}" }
                    }
                }
                
                // Ensure chunk mode is enabled if Gradio TTS is active and not already enabled
                if (useGradioTTS && mergeWordsRemote > 0 && !currentState.chunkModeEnabled) {
                    Log.warn { "TTSV2ScreenSpec: Re-enabling chunk mode with $mergeWordsRemote words (was disabled)" }
                    viewModel.adapter.enableChunkMode(mergeWordsRemote)
                }
            } else {
                // Clear existing translation state
                viewModel.adapter.setTranslatedContent(null)
                viewModel.adapter.setShowTranslation(false)
                
                // Load translation BEFORE starting TTS
                var translatedStringsToUse: List<String>? = null
                if (readTranslatedText) {
                    try {
                        val translations = getAllTranslationsUseCase.execute(chapterId)
                        if (translations.isNotEmpty()) {
                            val latestTranslation = translations.maxByOrNull { it.updatedAt }
                            latestTranslation?.let { translation ->
                                val translatedStrings = translation.translatedContent
                                    .filterIsInstance<Text>()
                                    .map { it.text }
                                    .filter { it.isNotBlank() }
                                if (translatedStrings.isNotEmpty()) {
                                    translatedStringsToUse = translatedStrings
                                    viewModel.adapter.setTranslatedContent(translatedStrings)
                                    viewModel.adapter.setShowTranslation(true)
                                }
                            }
                        }
                    } catch (e: Exception) {
                        Log.warn { "TTSV2ScreenSpec: Translation not available: ${e.message}" }
                    }
                }
                
                // Load chapter content
                viewModel.loadChapter(bookId, chapterId, readingParagraph)
            }
            
            // Configure Gradio TTS if enabled (only if not already configured)
            if (useGradioTTS && activeGradioConfigId.isNotEmpty() && !alreadyLoaded) {
                Log.warn { "TTSV2ScreenSpec: Configuring Gradio TTS with config: $activeGradioConfigId" }
                val gradioTTSConfig = gradioTTSManager.getConfigByIdOrPreset(activeGradioConfigId)
                if (gradioTTSConfig != null) {
                    // Convert to v2 GradioConfig with original config for full functionality
                    val v2Config = GradioConfig(
                        id = gradioTTSConfig.id,
                        name = gradioTTSConfig.name,
                        spaceUrl = gradioTTSConfig.spaceUrl,
                        apiName = gradioTTSConfig.apiName,
                        enabled = gradioTTSConfig.enabled,
                        originalConfig = gradioTTSConfig  // Pass full config for parameters, apiType, etc.
                    )
                    // Use useGradioTTS which sets config and engine type together
                    viewModel.adapter.useGradioTTS(v2Config)
                    
                    // Enable chunk mode for Gradio TTS (merges paragraphs for better performance)
                    if (mergeWordsRemote > 0) {
                        viewModel.adapter.enableChunkMode(mergeWordsRemote)
                        Log.warn { "TTSV2ScreenSpec: Chunk mode enabled with $mergeWordsRemote words" }
                    }
                    
                    Log.warn { "TTSV2ScreenSpec: Gradio engine configured: ${gradioTTSConfig.name}" }
                } else {
                    Log.warn { "TTSV2ScreenSpec: Gradio config not found: $activeGradioConfigId" }
                }
            }
            
            // Start background service for notification (Android only)
            serviceStarter.startService(bookId, chapterId, readingParagraph)
            
            // Load sentence highlight preference
            viewModel.adapter.setSentenceHighlight(sentenceHighlightEnabled)
        }
        
        // Watch for chapter changes (auto-next chapter)
        LaunchedEffect(state.chapter?.id) {
            val chapter = state.chapter ?: return@LaunchedEffect
            if (chapter.id == chapterId) return@LaunchedEffect // Skip initial chapter
            
            Log.warn { "TTSV2ScreenSpec: Chapter changed to ${chapter.id}" }
            
            // Clear and reload translation for new chapter
            viewModel.adapter.setTranslatedContent(null)
            viewModel.adapter.setShowTranslation(false)
            
            try {
                val translations = getAllTranslationsUseCase.execute(chapter.id)
                if (translations.isNotEmpty()) {
                    val latestTranslation = translations.maxByOrNull { it.updatedAt }
                    latestTranslation?.let { translation ->
                        val translatedStrings = translation.translatedContent
                            .filterIsInstance<Text>()
                            .map { it.text }
                            .filter { it.isNotBlank() }
                        if (translatedStrings.isNotEmpty()) {
                            viewModel.adapter.setTranslatedContent(translatedStrings)
                            if (readTranslatedText) {
                                viewModel.adapter.setShowTranslation(true)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.warn { "TTSV2ScreenSpec: Translation not available for chapter ${chapter.id}" }
            }
            
            // Reset calibration for new chapter
            calibratedWPM = null
            isCalibrated = false
            lastParagraphStartTime = 0L
            lastParagraphWordCount = 0
            lastParagraphIndex = -1
            wpmHistory = emptyList()
        }
        
        // Watch for Gradio config changes (when user changes config in settings)
        var previousGradioConfigId by remember { mutableStateOf(activeGradioConfigId) }
        var previousUseGradioTTS by remember { mutableStateOf(useGradioTTS) }
        LaunchedEffect(activeGradioConfigId, useGradioTTS) {
            // Skip initial composition (both values unchanged)
            val configChanged = previousGradioConfigId != activeGradioConfigId
            val useGradioChanged = previousUseGradioTTS != useGradioTTS
            
            if (!configChanged && !useGradioChanged) return@LaunchedEffect
            
            previousGradioConfigId = activeGradioConfigId
            previousUseGradioTTS = useGradioTTS
            
            Log.warn { "TTSV2ScreenSpec: Gradio settings changed - config=$activeGradioConfigId, useGradio=$useGradioTTS" }
            
            if (useGradioTTS && activeGradioConfigId.isNotEmpty()) {
                val gradioTTSConfig = gradioTTSManager.getConfigByIdOrPreset(activeGradioConfigId)
                if (gradioTTSConfig != null) {
                    val v2Config = GradioConfig(
                        id = gradioTTSConfig.id,
                        name = gradioTTSConfig.name,
                        spaceUrl = gradioTTSConfig.spaceUrl,
                        apiName = gradioTTSConfig.apiName,
                        enabled = gradioTTSConfig.enabled,
                        originalConfig = gradioTTSConfig
                    )
                    // Use useGradioTTS which sets both config AND engine type
                    viewModel.adapter.useGradioTTS(v2Config)
                    
                    // Re-enable chunk mode if needed
                    if (mergeWordsRemote > 0) {
                        viewModel.adapter.enableChunkMode(mergeWordsRemote)
                    }
                    
                    Log.warn { "TTSV2ScreenSpec: Gradio engine updated to: ${gradioTTSConfig.name}" }
                }
            } else if (!useGradioTTS) {
                // Switch back to native TTS
                viewModel.adapter.useNativeTTS()
                viewModel.adapter.disableChunkMode()
                Log.warn { "TTSV2ScreenSpec: Switched to native TTS" }
            }
        }
        
        // Watch for merge words changes (when user changes chunk word count in settings)
        var previousMergeWords by remember { mutableStateOf(mergeWordsRemote) }
        LaunchedEffect(mergeWordsRemote) {
            // Skip initial composition
            if (previousMergeWords == mergeWordsRemote) return@LaunchedEffect
            previousMergeWords = mergeWordsRemote
            
            Log.warn { "TTSV2ScreenSpec: Merge words changed to $mergeWordsRemote" }
            
            if (useGradioTTS && state.hasContent) {
                if (mergeWordsRemote > 0) {
                    // Re-enable chunk mode with new word count
                    viewModel.adapter.enableChunkMode(mergeWordsRemote)
                    Log.warn { "TTSV2ScreenSpec: Chunk mode re-enabled with $mergeWordsRemote words" }
                } else {
                    // Disable chunk mode
                    viewModel.adapter.disableChunkMode()
                    Log.warn { "TTSV2ScreenSpec: Chunk mode disabled" }
                }
            }
        }
        
        // Auto-scroll to current paragraph
        LaunchedEffect(currentParagraph, isPlaying) {
            if (isPlaying && state.paragraphs.isNotEmpty() && currentParagraph < state.paragraphs.size) {
                delay(100)
                try {
                    lazyListState.animateScrollToItem(currentParagraph)
                } catch (e: Exception) {
                    lazyListState.scrollToItem(currentParagraph)
                }
            }
        }
        
        // WPM Calibration - rolling average
        val paragraphStartTime by viewModel.adapter.paragraphStartTime.collectAsState()
        LaunchedEffect(paragraphStartTime, currentParagraph, isPlaying) {
            if (state.paragraphs.isNotEmpty() && isPlaying && paragraphStartTime > 0) {
                if (lastParagraphIndex >= 0 && lastParagraphIndex != currentParagraph && 
                    lastParagraphStartTime > 0 && lastParagraphWordCount > 0) {
                    
                    val paragraphDuration = paragraphStartTime - lastParagraphStartTime
                    if (paragraphDuration > 300 && paragraphDuration < 60000) {
                        val measuredWPM = SentenceHighlighter.calculateCalibratedWPM(
                            lastParagraphWordCount,
                            paragraphDuration,
                            state.speed
                        )
                        wpmHistory = (wpmHistory + measuredWPM).takeLast(3)
                        calibratedWPM = wpmHistory.average().toFloat()
                        isCalibrated = true
                        viewModel.adapter.setCalibration(calibratedWPM, true)
                    }
                }
                
                lastParagraphIndex = currentParagraph
                lastParagraphStartTime = paragraphStartTime
                lastParagraphWordCount = if (currentParagraph < state.paragraphs.size) {
                    SentenceHighlighter.countTotalWords(state.paragraphs[currentParagraph])
                } else 0
            }
        }
        
        // Observe translation progress
        val translationProgress by translationService.translationProgress.collectAsState()
        LaunchedEffect(translationProgress) {
            val chapter = state.chapter ?: return@LaunchedEffect
            val progress = translationProgress[chapter.id] ?: return@LaunchedEffect
            
            if (progress.status == TranslationStatus.COMPLETED) {
                try {
                    val translations = getAllTranslationsUseCase.execute(chapter.id)
                    if (translations.isNotEmpty()) {
                        val latestTranslation = translations.maxByOrNull { it.updatedAt }
                        latestTranslation?.let { translation ->
                            val translatedStrings = translation.translatedContent
                                .filterIsInstance<Text>()
                                .map { it.text }
                                .filter { it.isNotBlank() }
                            if (translatedStrings.isNotEmpty()) {
                                viewModel.adapter.setTranslatedContent(translatedStrings)
                                if (readTranslatedText) {
                                    viewModel.adapter.setShowTranslation(true)
                                }
                            }
                        }
                    }
                    
                    // Auto-translate next chapter if enabled
                    if (autoTranslateNextChapter) {
                        val currentIndex = chapters.indexOfFirst { it.id == chapter.id }
                        if (currentIndex != -1 && currentIndex < chapters.size - 1) {
                            val nextChapter = chapters[currentIndex + 1]
                            val nextTranslations = getAllTranslationsUseCase.execute(nextChapter.id)
                            if (nextTranslations.isEmpty()) {
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
                                    priority = false
                                )
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.error { "TTSV2ScreenSpec: Failed to load translation: ${e.message}" }
                }
            }
        }
        
        // Sync sleep mode UI with timer state
        LaunchedEffect(sleepTimerState) {
            sleepTimerState?.let { timerState ->
                sleepModeEnabled = timerState.isEnabled
                if (timerState.remainingTimeMs > 0) {
                    sleepTimeMinutes = (timerState.remainingTimeMs / 60000).toInt().coerceAtLeast(1)
                }
            }
        }
        
        // Content filter changes are applied on next chapter load
        // No need to watch for changes during TTS playback as it would interrupt reading
        
        // Save TTS state periodically for process death recovery
        LaunchedEffect(state.chapter?.id, currentParagraph, isPlaying) {
            if (state.chapter != null) {
                // Debounce state saving to avoid excessive writes
                delay(500)
                processStateManager.saveTTSState(
                    TTSProcessState(
                        bookId = bookId,
                        chapterId = state.chapter?.id ?: chapterId,
                        sourceId = sourceId,
                        readingParagraph = currentParagraph,
                        wasPlaying = isPlaying,
                        timestamp = currentTimeToLong()
                    )
                )
            }
        }
        
        // Cleanup on dispose - only cleanup ViewModel resources, NOT the controller
        // The controller state should persist while the service is running
        DisposableEffect(Unit) {
            onDispose {
                // Don't call viewModel.onCleared() as it would reset the controller
                // The controller is managed by the service lifecycle
                Log.warn { "TTSV2ScreenSpec: Screen disposed, keeping controller state" }
                // Clear process state when user intentionally leaves the screen
                processStateManager.clearTTSState()
            }
        }

        
        // Create CommonTTSScreenState from v2 state
        val screenState by remember(
            state, currentParagraph, previousParagraph, isPlaying, isLoading,
            sleepTimerState, calibratedWPM, isCalibrated, paragraphStartTime, sentenceHighlightEnabled,
            selectedPiperModel
        ) {
            derivedStateOf {
                CommonTTSScreenState(
                    currentReadingParagraph = currentParagraph,
                    previousReadingParagraph = previousParagraph,
                    isPlaying = isPlaying,
                    isLoading = isLoading,
                    content = state.paragraphs,
                    translatedContent = state.translatedParagraphs,
                    showTranslation = state.showTranslation,
                    bilingualMode = state.bilingualMode,
                    chapterName = state.chapter?.name ?: "",
                    bookTitle = state.book?.title ?: "",
                    speechSpeed = state.speed,
                    autoNextChapter = state.autoNextChapter,
                    fullScreenMode = fullScreenMode,
                    cachedParagraphs = emptySet(),
                    loadingParagraphs = emptySet(),
                    showCacheIndicators = false, // Don't show cache indicators (green checkmarks) in v2
                    sleepTimeRemaining = sleepTimerState?.remainingTimeMs ?: 0L,
                    sleepModeEnabled = sleepTimerState?.isEnabled == true,
                    selectedVoiceModel = selectedPiperModel.takeIf { it.isNotEmpty() },
                    currentEngine = when (state.engineType) {
                        EngineType.NATIVE -> "Native TTS"
                        EngineType.GRADIO -> "Gradio TTS"
                    },
                    availableEngines = listOf("Native TTS", "Gradio TTS"),
                    isTTSReady = state.isEngineReady,
                    paragraphStartTime = paragraphStartTime,
                    sentenceHighlightEnabled = sentenceHighlightEnabled,
                    calibratedWPM = calibratedWPM,
                    isCalibrated = isCalibrated,
                    mergedChunkParagraphs = state.currentChunkParagraphs,
                    isMergingEnabled = state.chunkModeEnabled,
                    currentMergedChunkIndex = state.currentChunkIndex,
                    totalMergedChunks = state.totalChunks,
                    usingCachedAudio = state.isUsingCachedAudio
                )
            }
        }
        
        // Create actions - use derivedStateOf for state-dependent actions
        val actions = object : CommonTTSActions {
            override fun onPlay() { 
                // Restart service for notification when playing
                val chId = state.chapter?.id ?: chapterId
                serviceStarter.startService(bookId, chId, state.currentParagraphIndex)
                viewModel.adapter.play() 
            }
            override fun onPause() { viewModel.adapter.pause() }
            override fun onNextParagraph() {
                if (state.chunkModeEnabled) {
                    viewModel.adapter.nextChunk()
                } else {
                    viewModel.adapter.nextParagraph()
                }
            }
            override fun onPreviousParagraph() {
                if (state.chunkModeEnabled) {
                    viewModel.adapter.previousChunk()
                } else {
                    viewModel.adapter.previousParagraph()
                }
            }
            override fun onNextChapter() { viewModel.adapter.nextChapter() }
            override fun onPreviousChapter() { viewModel.adapter.previousChapter() }
            override fun onParagraphClick(index: Int) { viewModel.adapter.jumpToParagraph(index) }
            override fun onToggleTranslation() { viewModel.adapter.toggleTranslation() }
            override fun onToggleBilingualMode() { viewModel.adapter.toggleBilingualMode() }
            override fun onToggleFullScreen() { fullScreenMode = !fullScreenMode }
            override fun onSpeedChange(speed: Float) { viewModel.adapter.setSpeed(speed) }
            override fun onAutoNextChange(enabled: Boolean) { viewModel.adapter.setAutoNextChapter(enabled) }
            override fun onOpenSettings() { showSettings = true }
            override fun onSelectVoice() { showVoiceSelection = true }
            override fun onSelectEngine(engine: String) { showEngineSettings = true }
        }
        
        // Sorted chapters for drawer
        val sortedChapters by remember(chapters, chaptersAscending) {
            derivedStateOf { if (chaptersAscending) chapters else chapters.asReversed() }
        }
        
        val drawerScrollState = rememberLazyListState()
        
        // Scroll to current chapter when drawer opens
        LaunchedEffect(drawerState.targetValue) {
            if (state.chapter != null && drawerState.targetValue == DrawerValue.Open && chapters.isNotEmpty()) {
                val index = sortedChapters.indexOfFirst { it.id == state.chapter?.id }
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
        
        // Main UI with Chapter Drawer
        IModalDrawer(
            state = drawerState,
            sheetContent = {
                AnimatedVisibility(
                    visible = true,
                    enter = slideInVertically(initialOffsetY = { -it }),
                    exit = slideOutVertically(targetOffsetY = { -it })
                ) {
                    TTSV2ScreenDrawer(
                        onReverseIcon = { chaptersAscending = !chaptersAscending },
                        onChapter = { ch ->
                            val wasPlaying = isPlaying
                            scope.launch {
                                drawerState.close()
                                
                                // Clear translation state
                                viewModel.adapter.setTranslatedContent(null)
                                viewModel.adapter.setShowTranslation(false)
                                
                                // Load translation for new chapter
                                var translatedStringsToUse: List<String>? = null
                                try {
                                    val translations = getAllTranslationsUseCase.execute(ch.id)
                                    if (translations.isNotEmpty()) {
                                        val latestTranslation = translations.maxByOrNull { it.updatedAt }
                                        latestTranslation?.let { translation ->
                                            val translatedStrings = translation.translatedContent
                                                .filterIsInstance<Text>()
                                                .map { it.text }
                                                .filter { it.isNotBlank() }
                                            if (translatedStrings.isNotEmpty()) {
                                                translatedStringsToUse = translatedStrings
                                                viewModel.adapter.setTranslatedContent(translatedStrings)
                                                if (readTranslatedText) {
                                                    viewModel.adapter.setShowTranslation(true)
                                                }
                                            }
                                        }
                                    }
                                } catch (e: Exception) {
                                    // Translation not available
                                }
                                
                                // Load new chapter
                                viewModel.loadChapter(bookId, ch.id, 0)
                                
                                // Resume playback if was playing
                                if (wasPlaying) {
                                    delay(500) // Wait for content to load
                                    viewModel.adapter.play()
                                }
                            }
                        },
                        chapter = state.chapter,
                        chapters = sortedChapters,
                        drawerScrollState = drawerScrollState,
                        onMap = { drawer ->
                            scope.launch {
                                try {
                                    val index = sortedChapters.indexOfFirst { it.id == state.chapter?.id }
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
                                if (state.chapter != null) {
                                    Text(
                                        text = state.chapter?.name ?: "",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        },
                        navigationIcon = {
                            TopAppBarBackButton(onClick = { navController.safePopBackStack() })
                        },
                        actions = {
                            // Translate current chapter button
                            IconButton(
                                onClick = {
                                    val chapter = state.chapter ?: return@IconButton
                                    if (isTranslatingChapter) return@IconButton
                                    
                                    scope.launch {
                                        isTranslatingChapter = true
                                        try {
                                            val engineId = translationEnginesManager.get().id
                                            val sourceLang = readerPreferences.translatorOriginLanguage().get()
                                            val targetLang = readerPreferences.translatorTargetLanguage().get()
                                            
                                            translationService.queueChapters(
                                                bookId = bookId,
                                                chapterIds = listOf(chapter.id),
                                                sourceLanguage = sourceLang,
                                                targetLanguage = targetLang,
                                                engineId = engineId,
                                                bypassWarning = true,
                                                priority = true
                                            )
                                        } finally {
                                            isTranslatingChapter = false
                                        }
                                    }
                                },
                                enabled = state.chapter != null && !isTranslatingChapter
                            ) {
                                if (isTranslatingChapter) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(
                                        Icons.Default.Translate,
                                        contentDescription = localizeHelper.localize(Res.string.translate_chapter),
                                        tint = if (state.hasTranslation) 
                                            MaterialTheme.colorScheme.primary 
                                        else 
                                            LocalContentColor.current
                                    )
                                }
                            }
                            
                            // Download chapter audio button (only for Gradio TTS when caching is enabled)
                            if (useGradioTTS && readerPreferences.ttsChapterCacheEnabled().get()) {
                                val cacheDays = readerPreferences.ttsChapterCacheDays().get()
                                
                                IconButton(
                                    onClick = {
                                        val chapter = state.chapter ?: return@IconButton
                                        val book = state.book ?: return@IconButton
                                        
                                        if (isDownloading) {
                                            // If already downloading, cancel it
                                            downloadManager.cancel()
                                        } else if (isChapterCached) {
                                            // If already cached, remove from cache
                                            chapterCache.removeEntry(chapter.id)
                                            chapterCache.removeAllChunksForChapter(chapter.id)
                                        } else {
                                            // Start download using chunks
                                            val paragraphs = if (state.showTranslation && state.translatedParagraphs != null) {
                                                state.translatedParagraphs!!
                                            } else {
                                                state.paragraphs
                                            }
                                            
                                            if (paragraphs.isNotEmpty()) {
                                                val mergeWordCount = readerPreferences.ttsMergeWordsRemote().get()
                                                val mergedChunks = if (mergeWordCount > 0) {
                                                    TTSTextMerger.mergeParagraphs(paragraphs, mergeWordCount)
                                                } else {
                                                    paragraphs.mapIndexed { index, text ->
                                                        TTSTextMerger.MergedChunk(
                                                            mergedText = text,
                                                            originalParagraphIndices = listOf(index),
                                                            wordCount = text.split("\\s+".toRegex()).size
                                                        )
                                                    }
                                                }
                                                
                                                val chunksToDownload = mergedChunks.mapIndexed { index, chunk ->
                                                    TTSChapterDownloadManager.ChunkInfo(
                                                        index = index,
                                                        text = chunk.mergedText,
                                                        paragraphIndices = chunk.originalParagraphIndices
                                                    )
                                                }
                                                
                                                // Start chunk download
                                                downloadManager.startChunkDownload(
                                                    chapterId = chapter.id,
                                                    chapterName = chapter.name,
                                                    bookTitle = book.title,
                                                    chunks = chunksToDownload,
                                                    generateAudio = { text, index ->
                                                        viewModel.adapter.generateAudioForText(text)
                                                    },
                                                    onChunkComplete = { chunkIndex, audioData, paragraphIndices ->
                                                        chapterCache.cacheChunkAudio(
                                                            chapterId = chapter.id,
                                                            chunkIndex = chunkIndex,
                                                            audioData = audioData,
                                                            engineId = activeGradioConfigId,
                                                            cacheDays = cacheDays,
                                                            paragraphIndices = paragraphIndices
                                                        )
                                                    },
                                                    onComplete = {
                                                        Log.warn { "TTSV2: All ${chunksToDownload.size} chunks cached for chapter ${chapter.id}" }
                                                    },
                                                    onError = { error ->
                                                        Log.error { "TTSV2: Download failed: $error" }
                                                    }
                                                )
                                            }
                                        }
                                    },
                                    enabled = state.chapter != null && !isLoading
                                ) {
                                    when {
                                        isDownloading -> {
                                            if (downloadState == TTSChapterDownloadManager.DownloadState.PAUSED) {
                                                Icon(
                                                    Icons.Default.PlayArrow,
                                                    contentDescription = localizeHelper.localize(Res.string.resume_download),
                                                    tint = MaterialTheme.colorScheme.primary
                                                )
                                            } else {
                                                CircularProgressIndicator(
                                                    modifier = Modifier.size(24.dp),
                                                    strokeWidth = 2.dp,
                                                    progress = { downloadProgress?.progressFraction ?: 0f }
                                                )
                                            }
                                        }
                                        isChapterCached -> {
                                            Icon(
                                                Icons.Default.DownloadDone,
                                                contentDescription = if (isChapterFullyCached) 
                                                    "Chapter Fully Cached (tap to remove)" 
                                                else 
                                                    "Chapter Partially Cached (tap to remove)",
                                                tint = if (isChapterFullyCached) 
                                                    MaterialTheme.colorScheme.primary 
                                                else 
                                                    MaterialTheme.colorScheme.tertiary
                                            )
                                        }
                                        else -> {
                                            Icon(
                                                Icons.Default.Download,
                                                contentDescription = localizeHelper.localize(Res.string.download_chapter_audio),
                                                tint = LocalContentColor.current
                                            )
                                        }
                                    }
                                }
                            }
                            
                            // Sleep timer button
                            IconButton(onClick = { showSleepTimerDialog = true }) {
                                BadgedBox(
                                    badge = {
                                        if (sleepTimerState?.isEnabled == true) {
                                            Badge { Text(sleepTimerState?.remainingMinutes?.toString() ?: "") }
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
                            
                            // Settings button
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
                            TTSV2FloatingFullscreenControls(
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
                            speechSpeed = state.speed,
                            autoNextChapter = state.autoNextChapter,
                            useGradioTTS = useGradioTTS,
                            currentEngineName = when (state.engineType) {
                                EngineType.NATIVE -> "Native TTS"
                                EngineType.GRADIO -> "Gradio TTS"
                            },
                            readTranslatedText = readTranslatedText,
                            hasTranslation = state.hasTranslation,
                            onUseCustomColorsChange = { 
                                useCustomColors = it
                                scope.launch { readerPreferences.ttsUseCustomColors().set(it) }
                            },
                            onBackgroundColorChange = { color ->
                                customBackgroundColor = color
                                scope.launch { readerPreferences.ttsBackgroundColor().set(color.toArgb().toLong()) }
                            },
                            onTextColorChange = { color ->
                                customTextColor = color
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
                                    viewModel.startSleepTimer(sleepTimeMinutes)
                                } else {
                                    viewModel.cancelSleepTimer()
                                }
                            },
                            onSleepTimeChange = { minutes ->
                                sleepTimeMinutes = minutes
                                scope.launch { readerPreferences.sleepTime().set(minutes.toLong()) }
                                if (sleepModeEnabled) {
                                    viewModel.startSleepTimer(minutes)
                                }
                            },
                            onSpeedChange = { viewModel.adapter.setSpeed(it) },
                            onAutoNextChange = { enabled -> viewModel.adapter.setAutoNextChapter(enabled) },
                            onCoquiTTSChange = { enabled ->
                                if (enabled && !isGradioConfigured) {
                                    // No Gradio config set up - open engine settings
                                    showEngineSettings = true
                                    showSettings = false
                                } else if (isGradioConfigured) {
                                    // Save to preferences - the flow will update useGradioTTS automatically
                                    scope.launch { appPreferences.useGradioTTS().set(enabled) }
                                    if (enabled) {
                                        // Set Gradio config before switching engine
                                        val gradioTTSConfig = gradioTTSManager.getConfigByIdOrPreset(activeGradioConfigId)
                                        if (gradioTTSConfig != null) {
                                            val v2Config = GradioConfig(
                                                id = gradioTTSConfig.id,
                                                name = gradioTTSConfig.name,
                                                spaceUrl = gradioTTSConfig.spaceUrl,
                                                apiName = gradioTTSConfig.apiName,
                                                enabled = gradioTTSConfig.enabled,
                                                originalConfig = gradioTTSConfig
                                            )
                                            viewModel.adapter.useGradioTTS(v2Config)
                                            
                                            // Enable chunk mode for Gradio TTS
                                            val mergeWordCount = readerPreferences.ttsMergeWordsRemote().get()
                                            if (mergeWordCount > 0) {
                                                viewModel.adapter.enableChunkMode(mergeWordCount)
                                            }
                                        }
                                    } else {
                                        viewModel.adapter.useNativeTTS()
                                    }
                                }
                            },
                            onReadTranslatedTextChange = { enabled ->
                                // Save to preferences - the flow will update readTranslatedText automatically
                                viewModel.adapter.setShowTranslation(enabled)
                                scope.launch { readerPreferences.useTTSWithTranslatedText().set(enabled) }
                            },
                            sentenceHighlightEnabled = sentenceHighlightEnabled,
                            onSentenceHighlightChange = { enabled ->
                                sentenceHighlightEnabled = enabled
                                viewModel.adapter.setSentenceHighlight(enabled)
                                scope.launch { readerPreferences.ttsSentenceHighlight().set(enabled) }
                            },
                            // Content filter settings
                            contentFilterEnabled = contentFilterEnabled,
                            contentFilterPatterns = contentFilterPatterns,
                            onContentFilterEnabledChange = { enabled ->
                                contentFilterEnabled = enabled
                                scope.launch { readerPreferences.contentFilterEnabled().set(enabled) }
                            },
                            onContentFilterPatternsChange = { patterns ->
                                contentFilterPatterns = patterns
                                scope.launch { readerPreferences.contentFilterPatterns().set(patterns) }
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
                
                // Engine Settings Screen
                if (showEngineSettings) {
                    TTSEngineSettingsScreen(
                        isDesktop = isTabletOrDesktop,
                        onDismiss = { showEngineSettings = false },
                        onNavigateToTTSManager = {
                            showEngineSettings = false
                            navController.navigate(NavigationRoutes.ttsEngineManager)
                        }
                    )
                }
                
                // Voice Selection Screen
                if (showVoiceSelection) {
                    TTSVoiceSelectionScreen(
                        isDesktop = isTabletOrDesktop,
                        onDismiss = { showVoiceSelection = false }
                    )
                }
            }
        }
        } // Close IModalDrawer
        
        // Sleep timer dialog
        if (showSleepTimerDialog) {
            SleepTimerDialog(
                currentState = sleepTimerState,
                onStart = { minutes -> viewModel.startSleepTimer(minutes) },
                onCancel = { viewModel.cancelSleepTimer() },
                onDismiss = { showSleepTimerDialog = false }
            )
        }
    }
}


/**
 * TTS V2 Screen Drawer - Chapter selection drawer
 */
@Composable
private fun TTSV2ScreenDrawer(
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
                items(
                    count = chapters.size,
                    key = { index -> chapters[index].id }  // Stable key for better recomposition
                ) { index ->
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
private fun TTSV2FloatingFullscreenControls(
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
