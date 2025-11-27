//package ireader.presentation.ui.home.tts
//
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.lazy.rememberLazyListState
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.*
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.text.style.TextAlign
//import androidx.compose.ui.unit.dp
//import ireader.domain.services.tts_service.TTSState
//import ireader.presentation.ui.component.IScaffold
//import ireader.presentation.ui.component.reusable_composable.TopAppBarBackButton
//import kotlinx.coroutines.flow.StateFlow
//
///**
// * Unified TTS Screen that works across all platforms
// * Adapts UI based on screen size (mobile/tablet/desktop)
// *
// * Features:
// * - Text content display with paragraph highlighting
// * - Translation support (original, translated, bilingual)
// * - Media controls (play/pause, next/prev paragraph, next/prev chapter)
// * - Speed control
// * - Sleep timer
// * - Custom colors and font settings
// * - Download feature (desktop only)
// * - Engine/Voice selection (desktop only)
// */
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun UnifiedTTSScreen(
//    state: CommonTTSScreenState,
//    actions: CommonTTSActions,
//    onNavigateBack: () -> Unit,
//    isTabletOrDesktop: Boolean = false,
//    modifier: Modifier = Modifier
//) {
//    // Settings state
//    var showSettings by remember { mutableStateOf(false) }
//    var useCustomColors by remember { mutableStateOf(false) }
//    var customBackgroundColor by remember { mutableStateOf(Color(0xFF1E1E1E)) }
//    var customTextColor by remember { mutableStateOf(Color.White) }
//    var fontSize by remember { mutableStateOf(18) }
//    var textAlignment by remember { mutableStateOf(TextAlign.Start) }
//    var sleepModeEnabled by remember { mutableStateOf(false) }
//    var sleepTimeMinutes by remember { mutableStateOf(30) }
//
//    // Determine actual colors to use
//    val backgroundColor = if (useCustomColors) customBackgroundColor else MaterialTheme.colorScheme.background
//    val textColor = if (useCustomColors) customTextColor else MaterialTheme.colorScheme.onBackground
//
//    val lazyListState = rememberLazyListState()
//
//    // Auto-scroll to current paragraph when playing
//    LaunchedEffect(state.currentReadingParagraph, state.isPlaying) {
//        if (state.isPlaying && state.content.isNotEmpty()) {
//            lazyListState.animateScrollToItem(
//                index = state.currentReadingParagraph.coerceIn(0, state.content.lastIndex)
//            )
//        }
//    }
//
//    IScaffold(
//        topBar = {
//            if (!state.fullScreenMode) {
//                TopAppBar(
//                    title = {
//                        Column {
//                            Text(
//                                text = "Text-to-Speech",
//                                style = MaterialTheme.typography.titleMedium
//                            )
//                            if (state.chapterName.isNotEmpty()) {
//                                Text(
//                                    text = state.chapterName,
//                                    style = MaterialTheme.typography.bodySmall,
//                                    color = MaterialTheme.colorScheme.onSurfaceVariant
//                                )
//                            }
//                        }
//                    },
//                    navigationIcon = {
//                        TopAppBarBackButton(onClick = onNavigateBack)
//                    },
//                    actions = {
//                        // Download button (Desktop only)
//                        if (state.hasDownloadFeature) {
//                            IconButton(
//                                onClick = { actions.onDownloadChapter() },
//                                enabled = !state.isDownloading && state.content.isNotEmpty()
//                            ) {
//                                if (state.isDownloading) {
//                                    CircularProgressIndicator(
//                                        progress = { state.downloadProgress },
//                                        modifier = Modifier.size(24.dp)
//                                    )
//                                } else {
//                                    Icon(Icons.Default.Download, "Download Chapter")
//                                }
//                            }
//                        }
//
//                        // Settings button
//                        IconButton(onClick = { showSettings = true }) {
//                            Icon(Icons.Default.Settings, "Settings")
//                        }
//                    },
//                    colors = TopAppBarDefaults.topAppBarColors(
//                        containerColor = MaterialTheme.colorScheme.surface
//                    )
//                )
//            }
//        }
//    ) { paddingValues ->
//        Box(
//            modifier = modifier
//                .fillMaxSize()
//                .padding(if (state.fullScreenMode) PaddingValues(0.dp) else paddingValues)
//        ) {
//            Column(
//                modifier = Modifier.fillMaxSize()
//            ) {
//                // Content area
//                Box(
//                    modifier = Modifier
//                        .weight(1f)
//                        .fillMaxWidth()
//                ) {
//                    TTSContentDisplay(
//                        state = state,
//                        actions = actions,
//                        lazyListState = lazyListState,
//                        backgroundColor = backgroundColor,
//                        textColor = textColor,
//                        fontSize = fontSize,
//                        textAlignment = textAlignment,
//                        isTabletOrDesktop = isTabletOrDesktop,
//                        modifier = Modifier.fillMaxSize()
//                    )
//
//                    // Floating controls for fullscreen mode
//                    if (state.fullScreenMode) {
//                        FloatingFullscreenControls(
//                            state = state,
//                            actions = actions,
//                            modifier = Modifier.align(Alignment.BottomEnd)
//                        )
//                    }
//                }
//
//                // Media controls at bottom (hidden in fullscreen)
//                if (!state.fullScreenMode) {
//                    TTSMediaControls(
//                        state = state,
//                        actions = actions,
//                        isTabletOrDesktop = isTabletOrDesktop,
//                        modifier = Modifier.fillMaxWidth()
//                    )
//                }
//            }
//
//            // Settings Panel (overlay)
//            if (showSettings) {
//                Box(
//                    modifier = Modifier
//                        .fillMaxSize()
//                        .padding(16.dp),
//                    contentAlignment = Alignment.Center
//                ) {
//                    TTSSettingsPanelCommon(
//                        useCustomColors = useCustomColors,
//                        customBackgroundColor = customBackgroundColor,
//                        customTextColor = customTextColor,
//                        fontSize = fontSize,
//                        textAlignment = textAlignment,
//                        sleepModeEnabled = sleepModeEnabled,
//                        sleepTimeMinutes = sleepTimeMinutes,
//                        speechSpeed = state.speechSpeed,
//                        autoNextChapter = state.autoNextChapter,
//                        onUseCustomColorsChange = { useCustomColors = it },
//                        onBackgroundColorChange = { customBackgroundColor = it },
//                        onTextColorChange = { customTextColor = it },
//                        onFontSizeChange = { fontSize = it },
//                        onTextAlignmentChange = { textAlignment = it },
//                        onSleepModeChange = { sleepModeEnabled = it },
//                        onSleepTimeChange = { sleepTimeMinutes = it },
//                        onSpeedChange = { actions.onSpeedChange(it) },
//                        onAutoNextChange = { actions.onAutoNextChange(it) },
//                        onDismiss = { showSettings = false },
//                        isTabletOrDesktop = isTabletOrDesktop
//                    )
//                }
//            }
//        }
//    }
//}
//
///**
// * Floating controls for fullscreen mode
// */
//@Composable
//private fun FloatingFullscreenControls(
//    state: CommonTTSScreenState,
//    actions: CommonTTSActions,
//    modifier: Modifier = Modifier
//) {
//    val hasTranslation = state.translatedContent != null && state.translatedContent.isNotEmpty()
//
//    Row(
//        modifier = modifier.padding(16.dp),
//        horizontalArrangement = Arrangement.spacedBy(8.dp),
//        verticalAlignment = Alignment.CenterVertically
//    ) {
//        // Translation toggle
//        if (hasTranslation) {
//            SmallFloatingActionButton(
//                onClick = { actions.onToggleTranslation() },
//                containerColor = if (state.showTranslation)
//                    MaterialTheme.colorScheme.primary
//                else
//                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
//            ) {
//                Icon(
//                    imageVector = Icons.Default.Translate,
//                    contentDescription = "Toggle Translation",
//                    modifier = Modifier.size(20.dp)
//                )
//            }
//        }
//
//        // Play/Pause
//        FloatingActionButton(
//            onClick = {
//                if (state.isPlaying) actions.onPause() else actions.onPlay()
//            },
//            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f)
//        ) {
//            Icon(
//                imageVector = if (state.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
//                contentDescription = if (state.isPlaying) "Pause" else "Play"
//            )
//        }
//
//        // Exit fullscreen
//        SmallFloatingActionButton(
//            onClick = { actions.onToggleFullScreen() },
//            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
//        ) {
//            Icon(
//                imageVector = Icons.Default.FullscreenExit,
//                contentDescription = "Exit Fullscreen",
//                modifier = Modifier.size(20.dp)
//            )
//        }
//    }
//}
//
///**
// * Helper function to create CommonTTSScreenState from TTSState
// */
//@Composable
//fun rememberCommonTTSState(
//    ttsState: TTSState,
//    showTranslation: Boolean = false,
//    bilingualMode: Boolean = false,
//    fullScreenMode: Boolean = false,
//    hasDownloadFeature: Boolean = false,
//    currentEngine: String = "Default",
//    availableEngines: List<String> = emptyList()
//): CommonTTSScreenState {
//    val currentReadingParagraph by ttsState.currentReadingParagraph.collectAsState()
//    val isPlaying by ttsState.isPlaying.collectAsState()
//    val isLoading by ttsState.isLoading.collectAsState()
//    val content by ttsState.ttsContent.collectAsState()
//    val translatedContent by ttsState.translatedTTSContent.collectAsState()
//    val chapter by ttsState.ttsChapter.collectAsState()
//    val book by ttsState.ttsBook.collectAsState()
//    val speechSpeed by ttsState.speechSpeed.collectAsState()
//    val autoNextChapter by ttsState.autoNextChapter.collectAsState()
//
//    return CommonTTSScreenState(
//        currentReadingParagraph = currentReadingParagraph,
//        isPlaying = isPlaying,
//        isLoading = isLoading,
//        content = content ?: emptyList(),
//        translatedContent = translatedContent,
//        showTranslation = showTranslation,
//        bilingualMode = bilingualMode,
//        chapterName = chapter?.name ?: "",
//        bookTitle = book?.title ?: "",
//        speechSpeed = speechSpeed,
//        autoNextChapter = autoNextChapter,
//        fullScreenMode = fullScreenMode,
//        hasDownloadFeature = hasDownloadFeature,
//        currentEngine = currentEngine,
//        availableEngines = availableEngines
//    )
//}
