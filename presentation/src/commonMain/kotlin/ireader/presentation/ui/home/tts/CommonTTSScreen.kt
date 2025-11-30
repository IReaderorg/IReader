package ireader.presentation.ui.home.tts

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ireader.domain.services.tts_service.TTSState
import ireader.i18n.localize
import ireader.i18n.resources.Res
import ireader.i18n.resources.*
import ireader.presentation.ui.component.reusable_composable.AppIconButton
import ireader.presentation.ui.component.reusable_composable.SuperSmallTextComposable
import kotlinx.coroutines.flow.StateFlow
import ireader.presentation.ui.core.theme.LocalLocalizeHelper

/**
 * Common TTS Screen State that works across platforms
 * This provides a unified interface for TTS functionality
 */
data class CommonTTSScreenState(
    val currentReadingParagraph: Int = 0,
    val previousReadingParagraph: Int = 0,
    val isPlaying: Boolean = false,
    val isLoading: Boolean = false,
    val content: List<String> = emptyList(),
    val translatedContent: List<String>? = null,
    val showTranslation: Boolean = false,
    val bilingualMode: Boolean = false,
    val chapterName: String = "",
    val bookTitle: String = "",
    val speechSpeed: Float = 1.0f,
    val autoNextChapter: Boolean = false,
    val fullScreenMode: Boolean = false,
    // Cache status for Coqui TTS
    val cachedParagraphs: Set<Int> = emptySet(),
    val loadingParagraphs: Set<Int> = emptySet(),
    // Sleep timer
    val sleepTimeRemaining: Long = 0L,
    val sleepModeEnabled: Boolean = false,
    // Desktop-specific features
    val hasDownloadFeature: Boolean = false,
    val isDownloading: Boolean = false,
    val downloadProgress: Float = 0f,
    val selectedVoiceModel: String? = null,
    val availableEngines: List<String> = emptyList(),
    val currentEngine: String = "Default"
)

/**
 * Common TTS Screen Actions interface
 */
interface CommonTTSActions {
    fun onPlay()
    fun onPause()
    fun onNextParagraph()
    fun onPreviousParagraph()
    fun onNextChapter()
    fun onPreviousChapter()
    fun onParagraphClick(index: Int)
    fun onToggleTranslation()
    fun onToggleBilingualMode()
    fun onToggleFullScreen()
    fun onSpeedChange(speed: Float)
    fun onAutoNextChange(enabled: Boolean)
    // Desktop-specific actions
    fun onDownloadChapter() {}
    fun onCancelDownload() {}
    fun onSelectEngine(engine: String) {}
    fun onSelectVoice() {}
    fun onOpenSettings() {}
}


/**
 * Unified TTS Content Display
 * Works for both mobile and desktop with adaptive layout
 */
@Composable
fun TTSContentDisplay(
    state: CommonTTSScreenState,
    actions: CommonTTSActions,
    lazyListState: LazyListState = rememberLazyListState(),
    backgroundColor: Color = MaterialTheme.colorScheme.background,
    textColor: Color = MaterialTheme.colorScheme.onBackground,
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
    val displayContent = if (state.showTranslation && state.translatedContent != null) {
        state.translatedContent
    } else {
        state.content
    }
    
    val hasTranslation = state.translatedContent != null && state.translatedContent.isNotEmpty()
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        if (displayContent.isEmpty()) {
            // Empty state
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator()
                } else {
                    Text(
                        text = localizeHelper.localize(Res.string.no_content_available),
                        color = textColor.copy(alpha = 0.6f),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        } else {
            LazyColumn(
                state = lazyListState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    horizontal = if (isTabletOrDesktop) 32.dp else 16.dp,
                    vertical = 16.dp
                ),
                verticalArrangement = Arrangement.spacedBy(paragraphDistance.dp)
            ) {
                items(displayContent.size) { index ->
                    val isCurrentParagraph = index == state.currentReadingParagraph
                    val isPreviousParagraph = index == state.previousReadingParagraph && index != state.currentReadingParagraph
                    val originalText = state.content.getOrNull(index) ?: ""
                    val translatedText = state.translatedContent?.getOrNull(index)
                    val isCached = state.cachedParagraphs.contains(index)
                    val isLoadingCache = state.loadingParagraphs.contains(index)
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { actions.onParagraphClick(index) }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        // Bilingual mode: show both original and translated
                        if (state.bilingualMode && hasTranslation && translatedText != null) {
                            // Original text
                            Text(
                                text = originalText,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = paragraphIndent.dp),
                                fontSize = fontSize.sp,
                                textAlign = textAlignment,
                                color = textColor.copy(
                                    alpha = if (isCurrentParagraph) 1f else 0.6f
                                ),
                                lineHeight = lineHeight.sp,
                                fontWeight = FontWeight(fontWeight)
                            )
                            
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            // Translated text with different styling
                            Text(
                                text = translatedText,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = paragraphIndent.dp)
                                    .background(textColor.copy(alpha = 0.05f))
                                    .padding(4.dp),
                                fontSize = (fontSize - 2).sp,
                                textAlign = textAlignment,
                                color = textColor.copy(
                                    alpha = if (isCurrentParagraph) 0.85f else 0.5f
                                ),
                                lineHeight = lineHeight.sp,
                                fontWeight = FontWeight(fontWeight - 100)
                            )
                        } else {
                            // Single text display
                            Text(
                                text = displayContent[index],
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = paragraphIndent.dp),
                                fontSize = fontSize.sp,
                                textAlign = textAlignment,
                                color = textColor.copy(
                                    alpha = if (isCurrentParagraph) 1f else 0.6f
                                ),
                                lineHeight = lineHeight.sp,
                                fontWeight = if (isCurrentParagraph) FontWeight.Bold else FontWeight(fontWeight)
                            )
                        }
                        // Cache indicator (only show for upcoming paragraphs)
                        if (index > state.currentReadingParagraph && index <= state.currentReadingParagraph + 3) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .padding(top = 6.dp)
                            ) {
                                when {
                                    isCached -> {
                                        // Green dot for cached
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .background(
                                                    Color(0xFF4CAF50),
                                                    shape = CircleShape
                                                )
                                        )
                                    }

                                    isLoadingCache -> {
                                        // Yellow dot for loading
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .background(
                                                    Color(0xFFFFC107),
                                                    shape = CircleShape
                                                )
                                        )
                                    }

                                    else -> {
                                        // Gray dot for not cached
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .background(
                                                    textColor.copy(alpha = 0.2f),
                                                    shape = CircleShape
                                                )
                                        )
                                    }
                                }
                            }
                        }
                    } // Close Row
                }
            }
        }
        
        // Progress indicator at bottom
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${state.currentReadingParagraph + 1}/${displayContent.size}",
                    style = MaterialTheme.typography.labelSmall,
                    color = textColor.copy(alpha = 0.7f)
                )
                
                if (state.showTranslation && hasTranslation) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.Translate,
                        contentDescription = localizeHelper.localize(Res.string.translated),
                        modifier = Modifier.size(12.dp),
                        tint = textColor.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}


/**
 * Unified Media Controls for TTS
 * Adapts to mobile/tablet/desktop layouts
 */
@Composable
fun TTSMediaControls(
    state: CommonTTSScreenState,
    actions: CommonTTSActions,
    isTabletOrDesktop: Boolean = false,
    modifier: Modifier = Modifier
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = if (isTabletOrDesktop) 8.dp else 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Progress bar
            if (state.content.isNotEmpty()) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Paragraph ${state.currentReadingParagraph + 1} / ${state.content.size}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Sleep timer indicator
                            if (state.sleepModeEnabled && state.sleepTimeRemaining > 0) {
                                val minutes = state.sleepTimeRemaining / 60000
                                val seconds = (state.sleepTimeRemaining % 60000) / 1000
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Schedule,
                                        contentDescription = localizeHelper.localize(Res.string.sleep_timer),
                                        modifier = Modifier.size(14.dp),
                                        tint = MaterialTheme.colorScheme.tertiary
                                    )
                                    Text(
                                        text = "${minutes}:${seconds.toString().padStart(2, '0')}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.tertiary
                                    )
                                }
                            }
                            
                            state.selectedVoiceModel?.let { model ->
                                Text(
                                    text = model,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                    
                    LinearProgressIndicator(
                        progress = { (state.currentReadingParagraph + 1).toFloat() / state.content.size },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            
            // Main playback controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Previous Chapter
                IconButton(
                    onClick = { actions.onPreviousChapter() },
                    enabled = state.chapterName.isNotEmpty()
                ) {
                    Icon(
                        Icons.Default.SkipPrevious,
                        contentDescription = localize(Res.string.previous_chapter),
                        modifier = Modifier.size(if (isTabletOrDesktop) 32.dp else 28.dp)
                    )
                }
                
                // Previous Paragraph
                IconButton(
                    onClick = { actions.onPreviousParagraph() },
                    enabled = state.currentReadingParagraph > 0
                ) {
                    Icon(
                        Icons.Default.FastRewind,
                        contentDescription = localize(Res.string.previous_paragraph),
                        modifier = Modifier.size(if (isTabletOrDesktop) 32.dp else 28.dp)
                    )
                }
                
                // Play/Pause (Large circular button)
                FloatingActionButton(
                    onClick = {
                        if (state.isPlaying) actions.onPause() else actions.onPlay()
                    },
                    modifier = Modifier.size(if (isTabletOrDesktop) 72.dp else 64.dp),
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = CircleShape
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(32.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Icon(
                            imageVector = if (state.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (state.isPlaying) localize(Res.string.pause) else localize(Res.string.play),
                            modifier = Modifier.size(if (isTabletOrDesktop) 40.dp else 32.dp)
                        )
                    }
                }
                
                // Next Paragraph
                IconButton(
                    onClick = { actions.onNextParagraph() },
                    enabled = state.currentReadingParagraph < state.content.lastIndex
                ) {
                    Icon(
                        Icons.Default.FastForward,
                        contentDescription = localize(Res.string.next_paragraph),
                        modifier = Modifier.size(if (isTabletOrDesktop) 32.dp else 28.dp)
                    )
                }
                
                // Next Chapter
                IconButton(
                    onClick = { actions.onNextChapter() },
                    enabled = state.chapterName.isNotEmpty()
                ) {
                    Icon(
                        Icons.Default.SkipNext,
                        contentDescription = localize(Res.string.next_chapter),
                        modifier = Modifier.size(if (isTabletOrDesktop) 32.dp else 28.dp)
                    )
                }
            }
            
            // Speed and additional controls
            if (isTabletOrDesktop) {
                DesktopTTSControls(state, actions)
            } else {
                MobileTTSControls(state, actions)
            }
        }
    }
}

/**
 * Desktop/Tablet specific TTS controls with more options
 */
@Composable
private fun DesktopTTSControls(
    state: CommonTTSScreenState,
    actions: CommonTTSActions
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Speed control
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = localizeHelper.localize(Res.string.speed),
                        style = MaterialTheme.typography.labelMedium
                    )
                    Text(
                        text = String.format("%.1fx", state.speechSpeed),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                Slider(
                    value = state.speechSpeed,
                    onValueChange = { actions.onSpeedChange(it) },
                    valueRange = 0.5f..2.0f,
                    steps = 14,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            
            // Auto-next chapter toggle
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = localizeHelper.localize(Res.string.auto_next),
                    style = MaterialTheme.typography.labelSmall
                )
                Switch(
                    checked = state.autoNextChapter,
                    onCheckedChange = { actions.onAutoNextChange(it) }
                )
            }
        }
        
        // Engine and Voice Selection (Desktop only)
        if (state.availableEngines.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Engine Selector
                OutlinedButton(
                    onClick = { actions.onOpenSettings() },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = localizeHelper.localize(Res.string.select_engine),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = state.currentEngine,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                
                // Voice Selection Button
                OutlinedButton(
                    onClick = { actions.onSelectVoice() },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.RecordVoiceOver,
                        contentDescription = localizeHelper.localize(Res.string.select_voice),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = localizeHelper.localize(Res.string.voice),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                
                // Download button (Desktop only)
                if (state.hasDownloadFeature) {
                    IconButton(
                        onClick = { actions.onDownloadChapter() },
                        enabled = !state.isDownloading
                    ) {
                        if (state.isDownloading) {
                            CircularProgressIndicator(
                                progress = { state.downloadProgress },
                                modifier = Modifier.size(24.dp)
                            )
                        } else {
                            Icon(Icons.Default.Download, "Download Chapter")
                        }
                    }
                }
            }
        }
        
        // Translation controls
        val hasTranslation = state.translatedContent != null && state.translatedContent.isNotEmpty()
        if (hasTranslation) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterChip(
                    selected = state.showTranslation,
                    onClick = { actions.onToggleTranslation() },
                    label = { Text(localizeHelper.localize(Res.string.show_translation)) },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Translate,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )
                
                FilterChip(
                    selected = state.bilingualMode,
                    onClick = { actions.onToggleBilingualMode() },
                    label = { Text(localizeHelper.localize(Res.string.bilingual)) },
                    enabled = state.showTranslation
                )
            }
        }
    }
}


/**
 * Mobile specific TTS controls - more compact
 */
@Composable
private fun MobileTTSControls(
    state: CommonTTSScreenState,
    actions: CommonTTSActions
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    val hasTranslation = state.translatedContent != null && state.translatedContent.isNotEmpty()
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Speed indicator
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                Icons.Default.Speed,
                contentDescription = localizeHelper.localize(Res.string.speed),
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = String.format("%.1fx", state.speechSpeed),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        // Translation toggle (if available)
        if (hasTranslation) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                SmallFloatingActionButton(
                    onClick = { actions.onToggleTranslation() },
                    modifier = Modifier.size(36.dp),
                    containerColor = if (state.showTranslation)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Icon(
                        imageVector = Icons.Default.Translate,
                        contentDescription = if (state.showTranslation) "Show Original" else "Show Translation",
                        modifier = Modifier.size(18.dp)
                    )
                }
                
                if (state.showTranslation) {
                    SmallFloatingActionButton(
                        onClick = { actions.onToggleBilingualMode() },
                        modifier = Modifier.size(36.dp),
                        containerColor = if (state.bilingualMode)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Icon(
                            imageVector = Icons.Default.ViewAgenda,
                            contentDescription = localizeHelper.localize(Res.string.bilingual_mode),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
        
        // Fullscreen toggle
        SmallFloatingActionButton(
            onClick = { actions.onToggleFullScreen() },
            modifier = Modifier.size(36.dp),
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Icon(
                imageVector = if (state.fullScreenMode) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                contentDescription = if (state.fullScreenMode) "Exit Fullscreen" else "Fullscreen",
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

/**
 * TTS Settings Panel - Modern modal bottom sheet with scrolling
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TTSSettingsPanelCommon(
    useCustomColors: Boolean,
    customBackgroundColor: Color,
    customTextColor: Color,
    fontSize: Int,
    textAlignment: TextAlign,
    sleepModeEnabled: Boolean,
    sleepTimeMinutes: Int,
    speechSpeed: Float,
    autoNextChapter: Boolean,
    useGradioTTS: Boolean = false,
    currentEngineName: String = "System TTS",
    readTranslatedText: Boolean = false,
    hasTranslation: Boolean = false,
    onUseCustomColorsChange: (Boolean) -> Unit,
    onBackgroundColorChange: (Color) -> Unit,
    onTextColorChange: (Color) -> Unit,
    onFontSizeChange: (Int) -> Unit,
    onTextAlignmentChange: (TextAlign) -> Unit,
    onSleepModeChange: (Boolean) -> Unit,
    onSleepTimeChange: (Int) -> Unit,
    onSpeedChange: (Float) -> Unit,
    onAutoNextChange: (Boolean) -> Unit,
    onCoquiTTSChange: (Boolean) -> Unit = {},
    onReadTranslatedTextChange: (Boolean) -> Unit = {},
    onOpenEngineSettings: () -> Unit = {},
    onDismiss: () -> Unit,
    isTabletOrDesktop: Boolean = false,
    modifier: Modifier = Modifier
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    val scrollState = rememberScrollState()
    
    Surface(
        modifier = modifier
            .fillMaxWidth(if (isTabletOrDesktop) 0.6f else 0.95f)
            .fillMaxHeight(if (isTabletOrDesktop) 0.85f else 0.92f),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header with drag handle indicator
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Drag handle
                Box(
                    modifier = Modifier
                        .padding(bottom = 12.dp)
                        .size(width = 40.dp, height = 4.dp)
                        .background(
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            shape = MaterialTheme.shapes.extraLarge
                        )
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = localizeHelper.localize(Res.string.tts_settings),
                        style = MaterialTheme.typography.headlineSmall
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, "Close")
                    }
                }
            }
            
            HorizontalDivider()
            
            // Scrollable settings content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // TTS Engine Selection
                SettingSectionCommon(title = localizeHelper.localize(Res.string.tts_engine)) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Current engine info with settings button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = localizeHelper.localize(Res.string.current_engine),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = currentEngineName,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            OutlinedButton(
                                onClick = onOpenEngineSettings
                            ) {
                                Icon(
                                    Icons.Default.Settings,
                                    contentDescription = localizeHelper.localize(Res.string.engine_settings),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(localizeHelper.localize(Res.string.engine_settings))
                            }
                        }
                        
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        
                        // Coqui TTS toggle (for online TTS)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = localizeHelper.localize(Res.string.use_coqui_tts),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = if (useGradioTTS) "High-quality neural TTS (requires server)" else "System TTS engine",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = useGradioTTS,
                                onCheckedChange = onCoquiTTSChange
                            )
                        }
                    }
                }
                
                // Speed Control
                SettingSectionCommon(title = "Speech Speed: ${String.format("%.1fx", speechSpeed)}") {
                    Slider(
                        value = speechSpeed,
                        onValueChange = onSpeedChange,
                        valueRange = 0.5f..2.0f,
                        steps = 14,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                // Auto-next Chapter
                SettingSectionCommon(title = localizeHelper.localize(Res.string.playback)) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = localizeHelper.localize(Res.string.auto_next_chapter_1),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = localizeHelper.localize(Res.string.automatically_play_next_chapter_when_current_ends),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = autoNextChapter,
                                onCheckedChange = onAutoNextChange
                            )
                        }
                        
                        
                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = localizeHelper.localize(Res.string.read_translated_text),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = if (readTranslatedText) "TTS will read translated content" else "TTS will read original content",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Switch(
                                    checked = readTranslatedText,
                                    onCheckedChange = onReadTranslatedTextChange
                                )
                            }

                    }
                }
                
                // Custom Colors Toggle
                SettingSectionCommon(title = localizeHelper.localize(Res.string.color_theme)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = localizeHelper.localize(Res.string.use_custom_colors),
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = if (useCustomColors) "Custom colors enabled" else "Using app theme colors",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = useCustomColors,
                            onCheckedChange = onUseCustomColorsChange
                        )
                    }
                }
                
                // Background Color (only if custom colors enabled)
                if (useCustomColors) {
                    SettingSectionCommon(title = localizeHelper.localize(Res.string.background_color)) {
                        ColorPickerCommon(
                            selectedColor = customBackgroundColor,
                            onColorSelected = onBackgroundColorChange,
                            colors = listOf(
                                Color(0xFF1E1E1E), // Dark
                                Color(0xFF2C2C2C), // Dark Gray
                                Color(0xFF1A1A2E), // Dark Blue
                                Color(0xFF16213E), // Navy
                                Color(0xFF0F3460), // Deep Blue
                                Color(0xFFFFFBF0), // Cream
                                Color(0xFFF5F5DC), // Beige
                                Color(0xFFE8E8E8), // Light Gray
                            )
                        )
                    }
                    
                    // Text Color
                    SettingSectionCommon(title = localizeHelper.localize(Res.string.text_color_1)) {
                        ColorPickerCommon(
                            selectedColor = customTextColor,
                            onColorSelected = onTextColorChange,
                            colors = listOf(
                                Color.White,
                                Color(0xFFE0E0E0),
                                Color(0xFFFFF8DC),
                                Color(0xFFFFE4B5),
                                Color.Black,
                                Color(0xFF333333),
                                Color(0xFF4A4A4A),
                                Color(0xFF2196F3),
                            )
                        )
                    }
                }
                
                // Font Size
                SettingSectionCommon(title = "Font Size: ${fontSize}sp") {
                    Slider(
                        value = fontSize.toFloat(),
                        onValueChange = { onFontSizeChange(it.toInt()) },
                        valueRange = 12f..32f,
                        steps = 19,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                // Text Alignment
                SettingSectionCommon(title = localizeHelper.localize(Res.string.text_alignment)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        AlignmentButtonCommon(
                            icon = Icons.Default.FormatAlignLeft,
                            label = localizeHelper.localize(Res.string.left),
                            isSelected = textAlignment == TextAlign.Start,
                            onClick = { onTextAlignmentChange(TextAlign.Start) }
                        )
                        AlignmentButtonCommon(
                            icon = Icons.Default.FormatAlignCenter,
                            label = localizeHelper.localize(Res.string.center),
                            isSelected = textAlignment == TextAlign.Center,
                            onClick = { onTextAlignmentChange(TextAlign.Center) }
                        )
                        AlignmentButtonCommon(
                            icon = Icons.Default.FormatAlignRight,
                            label = localizeHelper.localize(Res.string.right),
                            isSelected = textAlignment == TextAlign.End,
                            onClick = { onTextAlignmentChange(TextAlign.End) }
                        )
                        AlignmentButtonCommon(
                            icon = Icons.Default.FormatAlignJustify,
                            label = localizeHelper.localize(Res.string.justify),
                            isSelected = textAlignment == TextAlign.Justify,
                            onClick = { onTextAlignmentChange(TextAlign.Justify) }
                        )
                    }
                }
                
                // Sleep Mode
                SettingSectionCommon(title = localizeHelper.localize(Res.string.enable_sleep_timer)) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = localizeHelper.localize(Res.string.enable_sleep_timer_1),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = localizeHelper.localize(Res.string.automatically_stop_playback_after_set_time),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = sleepModeEnabled,
                                onCheckedChange = onSleepModeChange
                            )
                        }
                        
                        if (sleepModeEnabled) {
                            Column {
                                Text(
                                    text = "Sleep after: $sleepTimeMinutes minutes",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Slider(
                                    value = sleepTimeMinutes.toFloat(),
                                    onValueChange = { onSleepTimeChange(it.toInt()) },
                                    valueRange = 5f..120f,
                                    steps = 22,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
                
                // Bottom padding for better scrolling
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun SettingSectionCommon(
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary
        )
        content()
    }
}

@Composable
private fun ColorPickerCommon(
    selectedColor: Color,
    onColorSelected: (Color) -> Unit,
    colors: List<Color>,
    modifier: Modifier = Modifier
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        colors.forEach { color ->
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(color, shape = MaterialTheme.shapes.small)
                    .clickable { onColorSelected(color) }
                    .then(
                        if (color == selectedColor) {
                            Modifier.padding(2.dp)
                        } else Modifier
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (color == selectedColor) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = localizeHelper.localize(Res.string.selected),
                        tint = if (color.luminance() > 0.5f) Color.Black else Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun AlignmentButtonCommon(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        FilledTonalIconButton(
            onClick = onClick,
            colors = IconButtonDefaults.filledTonalIconButtonColors(
                containerColor = if (isSelected)
                    MaterialTheme.colorScheme.primaryContainer
                else
                    MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Icon(icon, label)
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (isSelected)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// Extension function to calculate color luminance
private fun Color.luminance(): Float {
    return (0.299f * red + 0.587f * green + 0.114f * blue)
}

/**
 * Download Progress Dialog - Desktop feature
 */
@Composable
fun DownloadProgressDialog(
    isDownloading: Boolean,
    progress: Float,
    elapsedSeconds: Long,
    onCancel: () -> Unit,
    onDismiss: () -> Unit
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    if (isDownloading) {
        AlertDialog(
            onDismissRequest = { /* Prevent dismissal during download */ },
            title = { Text(localizeHelper.localize(Res.string.downloading_chapter_audio)) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = localizeHelper.localize(Res.string.generating_audio_for_entire_chapter),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${(progress * 100).toInt()}%",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        Text(
                            text = "${elapsedSeconds}s",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Text(
                        text = localizeHelper.localize(Res.string.this_may_take_a_few_minutes_for_long_chapters),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = onCancel) {
                    Text(localizeHelper.localize(Res.string.cancel))
                }
            }
        )
    }
}

/**
 * TTS Engine Settings Screen
 * 
 * Platform-specific behavior:
 * - Android: Shows option to open system TTS settings or IReader TTS manager
 * - Desktop: Shows IReader TTS Engine Manager directly
 */
@Composable
expect fun TTSEngineSettingsScreen(
    isDesktop: Boolean,
    onDismiss: () -> Unit,
    onNavigateToTTSManager: () -> Unit = {}
)

/**
 * TTS Voice Selection Screen
 * 
 * Platform-specific behavior:
 * - Android: Shows available system TTS voices
 * - Desktop: Shows available Piper/Kokoro/Maya voices
 */
@Composable
expect fun TTSVoiceSelectionScreen(
    isDesktop: Boolean,
    onDismiss: () -> Unit
)
