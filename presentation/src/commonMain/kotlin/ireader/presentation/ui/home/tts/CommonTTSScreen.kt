package ireader.presentation.ui.home.tts

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ireader.i18n.localize
import ireader.i18n.resources.Res
import ireader.i18n.resources.*
import ireader.presentation.ui.core.theme.LocalLocalizeHelper
import androidx.compose.runtime.Stable
import kotlinx.coroutines.delay

/**
 * Sentence highlighting data for TTS
 * Tracks which sentence within a paragraph is currently being read
 */
@Stable
data class SentenceHighlightState(
    val currentSentenceIndex: Int = 0,
    val sentences: List<String> = emptyList(),
    val sentenceStartTime: Long = 0L
) {
    val currentSentence: String? get() = sentences.getOrNull(currentSentenceIndex)
    val hasSentences: Boolean get() = sentences.isNotEmpty()
    val totalSentences: Int get() = sentences.size
}

/**
 * Utility object for sentence-level TTS highlighting
 * Uses time-based estimation to highlight sentences without additional TTS API calls
 * 
 * Splits text into sentences (not smaller chunks) for natural reading flow
 * 
 * CALIBRATION: The first paragraph is used to measure actual TTS speed.
 * After the first paragraph completes, we calculate the real words-per-minute
 * and use that for accurate highlighting of subsequent paragraphs.
 */
object SentenceHighlighter {
    // Default words per minute for TTS at 1.0x speed
    private const val DEFAULT_WORDS_PER_MINUTE = 170f
    
    // Maximum words per sentence before splitting (keep sentences reasonably sized)
    private const val MAX_WORDS_PER_SENTENCE = 50
    
    // Sentence endings pattern
    private val SENTENCE_END_PATTERN = Regex("(?<=[.!?。！？;；])")
    
    /**
     * Split text into sentences for highlighting
     * Only splits on sentence endings to keep chunks larger and more natural
     */
    fun splitIntoSentences(text: String): List<String> {
        if (text.isBlank()) return emptyList()
        
        // Split by sentence endings
        val sentences = text.split(SENTENCE_END_PATTERN)
            .map { it.trim() }
            .filter { it.isNotBlank() }
        
        // Only split very long sentences (50+ words) by commas
        val result = sentences.flatMap { sentence ->
            if (countWords(sentence) > MAX_WORDS_PER_SENTENCE) {
                // Split long sentences by commas
                val parts = sentence.split(Regex("(?<=[,，])"))
                    .map { it.trim() }
                    .filter { it.isNotBlank() }
                if (parts.size > 1) parts else listOf(sentence)
            } else {
                listOf(sentence)
            }
        }
        
        return result.ifEmpty { listOf(text.trim()) }
    }
    
    private fun countWords(text: String): Int {
        return text.split(Regex("\\s+")).filter { it.isNotBlank() }.size
    }
    
    private fun splitByWordCount(text: String, maxWords: Int): List<String> {
        val words = text.split(Regex("\\s+"))
        val chunks = mutableListOf<String>()
        var currentChunk = StringBuilder()
        var wordCount = 0
        
        for (word in words) {
            if (wordCount >= maxWords && currentChunk.isNotEmpty()) {
                chunks.add(currentChunk.toString().trim())
                currentChunk = StringBuilder()
                wordCount = 0
            }
            if (currentChunk.isNotEmpty()) currentChunk.append(" ")
            currentChunk.append(word)
            wordCount++
        }
        
        if (currentChunk.isNotEmpty()) {
            chunks.add(currentChunk.toString().trim())
        }
        
        return chunks
    }
    
    /**
     * Count total words in a text
     */
    fun countTotalWords(text: String): Int = countWords(text)
    
    /**
     * Calculate calibrated words per minute based on first paragraph timing
     * @param wordCount Number of words in the calibration paragraph
     * @param durationMs How long it took TTS to read the paragraph
     * @param speechSpeed Current speech speed setting
     * @return Calibrated WPM for this TTS engine at 1.0x speed
     */
    fun calculateCalibratedWPM(wordCount: Int, durationMs: Long, speechSpeed: Float): Float {
        if (durationMs <= 0 || wordCount <= 0) return DEFAULT_WORDS_PER_MINUTE
        val actualWPM = (wordCount.toFloat() / durationMs) * 60000f
        // Normalize to 1.0x speed
        return actualWPM / speechSpeed
    }
    
    /**
     * Estimate reading duration for a text chunk in milliseconds
     * @param chunk The text chunk to estimate
     * @param speechSpeed Current speech speed setting (e.g., 1.0, 1.5, 2.0)
     * @param calibratedWPM Calibrated words per minute (null = use default)
     */
    fun estimateSentenceDuration(chunk: String, speechSpeed: Float, calibratedWPM: Float? = null): Long {
        val wordCount = countWords(chunk).coerceAtLeast(1)
        // Use calibrated WPM if available, otherwise use default
        val baseWPM = calibratedWPM ?: DEFAULT_WORDS_PER_MINUTE
        val wordsPerMinute = baseWPM * speechSpeed
        val durationMinutes = wordCount / wordsPerMinute
        // Minimal pause between chunks
        val pauseMs = if (chunk.endsWith(".") || chunk.endsWith("!") || chunk.endsWith("?") ||
            chunk.endsWith("。") || chunk.endsWith("！") || chunk.endsWith("？")) 30 else 10
        return (durationMinutes * 60 * 1000).toLong() + pauseMs
    }
    
    /**
     * Calculate cumulative durations for all chunks
     * @param sentences List of text chunks
     * @param speechSpeed Current speech speed setting
     * @param calibratedWPM Calibrated words per minute (null = use default)
     * @return List of cumulative end times in milliseconds
     */
    fun calculateSentenceTimings(sentences: List<String>, speechSpeed: Float, calibratedWPM: Float? = null): List<Long> {
        var cumulativeTime = 0L
        return sentences.map { sentence ->
            cumulativeTime += estimateSentenceDuration(sentence, speechSpeed, calibratedWPM)
            cumulativeTime
        }
    }
    
    /**
     * Get current sentence index based on elapsed time
     */
    fun getCurrentSentenceIndex(
        elapsedTimeMs: Long,
        sentenceTimings: List<Long>
    ): Int {
        if (sentenceTimings.isEmpty()) return 0
        
        for ((index, endTime) in sentenceTimings.withIndex()) {
            if (elapsedTimeMs < endTime) {
                return index
            }
        }
        return sentenceTimings.lastIndex
    }
}

/**
 * Common TTS Screen State that works across platforms
 * This provides a unified interface for TTS functionality
 * 
 * Marked as @Stable to help Compose skip recomposition when state hasn't changed
 */
@Stable
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
    val currentEngine: String = "Default",
    // Sentence-level highlighting - timestamp when current paragraph started playing
    val paragraphStartTime: Long = 0L,
    // Enable/disable sentence highlighting (can be toggled in settings)
    val sentenceHighlightEnabled: Boolean = true,
    // TTS Speed Calibration - calculated from first paragraph timing
    // null = not yet calibrated (first paragraph), value = calibrated WPM at 1.0x speed
    val calibratedWPM: Float? = null,
    // Whether calibration is complete (first paragraph has finished)
    val isCalibrated: Boolean = false
) {
    // Pre-computed properties to avoid repeated calculations during recomposition
    val contentSize: Int get() = content.size
    val hasContent: Boolean get() = content.isNotEmpty()
    val hasTranslation: Boolean get() = translatedContent != null && translatedContent.isNotEmpty()
    val progressFraction: Float get() = if (contentSize > 0) (currentReadingParagraph + 1).toFloat() / contentSize else 0f
}

/**
 * Common TTS Screen Actions interface
 * Marked as @Stable to help Compose understand this interface is stable
 */
@Stable
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
 * 
 * Optimizations for low-end devices:
 * - Memoized display content and padding values
 * - Stable keys for LazyColumn items
 * - Reduced recomposition scope with derivedStateOf
 * - Lightweight paragraph items with minimal allocations
 * - Time-based sentence highlighting (no extra TTS API calls)
 */
@Composable
fun TTSContentDisplay(
    state: CommonTTSScreenState,
    actions: CommonTTSActions,
    lazyListState: LazyListState = rememberLazyListState(),
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
    
    // Use derivedStateOf for display content to minimize recompositions
    val displayContent by remember(state.showTranslation, state.translatedContent, state.content) {
        derivedStateOf {
            if (state.showTranslation && state.translatedContent != null) {
                state.translatedContent
            } else {
                state.content
            }
        }
    }
    
    // Use pre-computed property from state
    val hasTranslation = state.hasTranslation
    
    // Pre-compute content padding - only recalculate when dependencies change
    val contentPadding = remember(isTabletOrDesktop) {
        PaddingValues(
            horizontal = if (isTabletOrDesktop) 32.dp else 16.dp,
            vertical = 16.dp
        )
    }
    
    // Memoize paragraph click handler to prevent lambda recreation
    val onParagraphClick = remember(actions) { { index: Int -> actions.onParagraphClick(index) } }
    
    // Pre-compute font sizes to avoid repeated dp/sp conversions
    val fontSizeSp = remember(fontSize) { fontSize.sp }
    val smallerFontSizeSp = remember(fontSize) { (fontSize - 2).sp }
    val lineHeightSp = remember(lineHeight) { lineHeight.sp }
    val paragraphIndentDp = remember(paragraphIndent) { paragraphIndent.dp }
    val paragraphDistanceDp = remember(paragraphDistance) { paragraphDistance.dp }
    
    // Sentence highlighting state - only active for current paragraph when playing
    // Use derivedStateOf to ensure proper recomputation when paragraph changes
    val currentParagraphText by remember(displayContent, state.currentReadingParagraph) {
        derivedStateOf { displayContent.getOrNull(state.currentReadingParagraph) ?: "" }
    }
    
    // Memoize sentences for current paragraph to avoid re-splitting
    val currentSentences = remember(currentParagraphText) {
        SentenceHighlighter.splitIntoSentences(currentParagraphText)
    }
    
    // Calculate word counts for each chunk (for proportional timing)
    val chunkWordCounts = remember(currentSentences) {
        currentSentences.map { SentenceHighlighter.countTotalWords(it) }
    }
    val totalWords = remember(chunkWordCounts) { chunkWordCounts.sum().coerceAtLeast(1) }
    
    // Time-based sentence index
    var currentSentenceIndex by remember { mutableStateOf(0) }
    var highlightStartTime by remember { mutableStateOf(System.currentTimeMillis()) }
    
    // Store isPlaying in a ref that can be read in coroutine
    val isPlayingRef = rememberUpdatedState(state.isPlaying)
    val sentencesRef = rememberUpdatedState(currentSentences)
    val totalWordsRef = rememberUpdatedState(totalWords)
    val calibratedWPMRef = rememberUpdatedState(state.calibratedWPM)
    val speechSpeedRef = rememberUpdatedState(state.speechSpeed)
    
    // Reset when paragraph changes
    LaunchedEffect(state.currentReadingParagraph) {
        currentSentenceIndex = 0
        highlightStartTime = System.currentTimeMillis()
    }
    
    // Reset start time when playback starts
    LaunchedEffect(state.isPlaying) {
        if (state.isPlaying) {
            highlightStartTime = System.currentTimeMillis()
        }
    }
    
    // Main highlighting loop
    LaunchedEffect(Unit) {
        while (true) {
            if (isPlayingRef.value) {
                val sentences = sentencesRef.value
                if (sentences.isNotEmpty()) {
                    val words = totalWordsRef.value.coerceAtLeast(1)
                    val wpm = ((calibratedWPMRef.value ?: 170f) * speechSpeedRef.value).coerceAtLeast(50f)
                    val durationMs = ((words.toFloat() / wpm) * 60000).toLong().coerceIn(2000, 120000)
                    
                    val elapsed = System.currentTimeMillis() - highlightStartTime
                    val progress = (elapsed.toFloat() / durationMs).coerceIn(0f, 0.99f)
                    val newIndex = (progress * sentences.size).toInt().coerceIn(0, sentences.lastIndex)
                    
                    if (newIndex != currentSentenceIndex) {
                        currentSentenceIndex = newIndex
                    }
                }
            }
            delay(100)
        }
    }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        if (displayContent.isEmpty()) {
            // Empty state - lightweight composable
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
                contentPadding = contentPadding,
                verticalArrangement = Arrangement.spacedBy(paragraphDistanceDp)
            ) {
                items(
                    count = displayContent.size,
                    key = { index -> index }, // Use simple Int key for better performance
                    contentType = { "tts_paragraph" }
                ) { index ->
                    val isCurrentParagraph = index == state.currentReadingParagraph
                    
                    // Extract item-specific state to minimize recomposition scope
                    TTSParagraphItemWithSentenceHighlight(
                        index = index,
                        text = displayContent[index],
                        originalText = state.content.getOrNull(index) ?: "",
                        translatedText = state.translatedContent?.getOrNull(index),
                        isCurrentParagraph = isCurrentParagraph,
                        isPlaying = state.isPlaying,
                        currentSentenceIndex = if (isCurrentParagraph) 
                            currentSentenceIndex.coerceAtLeast(0) else 0,
                        sentences = if (isCurrentParagraph) 
                            currentSentences else emptyList(),
                        isBilingualMode = state.bilingualMode,
                        hasTranslation = hasTranslation,
                        showCacheIndicator = index > state.currentReadingParagraph && index <= state.currentReadingParagraph + 3,
                        isCached = state.cachedParagraphs.contains(index),
                        isLoadingCache = state.loadingParagraphs.contains(index),
                        textColor = textColor,
                        highlightColor = highlightColor,
                        fontSize = fontSizeSp,
                        smallerFontSize = smallerFontSizeSp,
                        lineHeight = lineHeightSp,
                        textAlignment = textAlignment,
                        paragraphIndent = paragraphIndentDp,
                        fontWeight = fontWeight,
                        onParagraphClick = onParagraphClick
                    )
                }
            }
        }
        
        // Progress indicator at bottom - lightweight
        TTSProgressIndicator(
            currentParagraph = state.currentReadingParagraph,
            totalParagraphs = displayContent.size,
            showTranslation = state.showTranslation && hasTranslation,
            textColor = textColor,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 8.dp)
        )
    }
}

/**
 * Extracted paragraph item with sentence-level highlighting support.
 * Each paragraph only recomposes when its specific state changes.
 * 
 * Sentence highlighting is time-based and doesn't require additional TTS API calls.
 * The current sentence is estimated based on speech speed and word count.
 */
@Composable
private fun TTSParagraphItemWithSentenceHighlight(
    index: Int,
    text: String,
    originalText: String,
    translatedText: String?,
    isCurrentParagraph: Boolean,
    isPlaying: Boolean,
    currentSentenceIndex: Int,
    sentences: List<String>,
    isBilingualMode: Boolean,
    hasTranslation: Boolean,
    showCacheIndicator: Boolean,
    isCached: Boolean,
    isLoadingCache: Boolean,
    textColor: Color,
    highlightColor: Color,
    fontSize: androidx.compose.ui.unit.TextUnit,
    smallerFontSize: androidx.compose.ui.unit.TextUnit,
    lineHeight: androidx.compose.ui.unit.TextUnit,
    textAlignment: TextAlign,
    paragraphIndent: androidx.compose.ui.unit.Dp,
    fontWeight: Int,
    onParagraphClick: (Int) -> Unit
) {
    // Memoize alpha values to avoid repeated calculations
    val textAlpha = if (isCurrentParagraph) 1f else 0.6f
    val translatedAlpha = if (isCurrentParagraph) 0.85f else 0.5f
    
    // Determine if we should show sentence highlighting
    // Show for current paragraph when playing and sentences are available
    // Also show when paused on current paragraph to maintain visual continuity
    val showSentenceHighlight = isCurrentParagraph && sentences.isNotEmpty()
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onParagraphClick(index) }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Column(modifier = Modifier.weight(1f)) {
            // Bilingual mode: show both original and translated
            if (isBilingualMode && hasTranslation && translatedText != null) {
                // Original text with sentence highlighting
                if (showSentenceHighlight) {
                    SentenceHighlightedText(
                        sentences = sentences,
                        currentSentenceIndex = currentSentenceIndex,
                        textColor = textColor,
                        highlightColor = highlightColor,
                        fontSize = fontSize,
                        lineHeight = lineHeight,
                        textAlignment = textAlignment,
                        fontWeight = fontWeight,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = paragraphIndent)
                    )
                } else {
                    Text(
                        text = originalText,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = paragraphIndent),
                        fontSize = fontSize,
                        textAlign = textAlignment,
                        color = textColor.copy(alpha = textAlpha),
                        lineHeight = lineHeight,
                        fontWeight = FontWeight(fontWeight)
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Translated text with different styling
                Text(
                    text = translatedText,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = paragraphIndent)
                        .background(textColor.copy(alpha = 0.05f))
                        .padding(4.dp),
                    fontSize = smallerFontSize,
                    textAlign = textAlignment,
                    color = textColor.copy(alpha = translatedAlpha),
                    lineHeight = lineHeight,
                    fontWeight = FontWeight((fontWeight - 100).coerceAtLeast(100))
                )
            } else {
                // Single text display with sentence highlighting
                if (showSentenceHighlight) {
                    SentenceHighlightedText(
                        sentences = sentences,
                        currentSentenceIndex = currentSentenceIndex,
                        textColor = textColor,
                        highlightColor = highlightColor,
                        fontSize = fontSize,
                        lineHeight = lineHeight,
                        textAlignment = textAlignment,
                        fontWeight = fontWeight,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = paragraphIndent)
                    )
                } else {
                    Text(
                        text = text,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = paragraphIndent),
                        fontSize = fontSize,
                        textAlign = textAlignment,
                        color = textColor.copy(alpha = textAlpha),
                        lineHeight = lineHeight,
                        fontWeight = if (isCurrentParagraph) FontWeight.Bold else FontWeight(fontWeight)
                    )
                }
            }
        }
        
        // Cache indicator (only show for upcoming paragraphs)
        if (showCacheIndicator) {
            CacheIndicator(
                isCached = isCached,
                isLoadingCache = isLoadingCache,
                textColor = textColor
            )
        }
    }
}

/**
 * Text component with sentence-level highlighting using AnnotatedString.
 * Highlights the current sentence being read while dimming others.
 * 
 * This is optimized to minimize recompositions:
 * - AnnotatedString is built only when currentSentenceIndex changes
 * - Uses SpanStyle for efficient text styling
 */
@Composable
private fun SentenceHighlightedText(
    sentences: List<String>,
    currentSentenceIndex: Int,
    textColor: Color,
    highlightColor: Color,
    fontSize: androidx.compose.ui.unit.TextUnit,
    lineHeight: androidx.compose.ui.unit.TextUnit,
    textAlignment: TextAlign,
    fontWeight: Int,
    modifier: Modifier = Modifier
) {
    // Ensure currentSentenceIndex is valid
    val safeIndex = currentSentenceIndex.coerceIn(0, sentences.lastIndex.coerceAtLeast(0))
    
    // Build annotated string with highlighted current chunk
    val annotatedText = remember(sentences, safeIndex, textColor, highlightColor) {
        buildAnnotatedString {
            sentences.forEachIndexed { index, chunk ->
                val isCurrentChunk = index == safeIndex
                
                if (isCurrentChunk) {
                    // Current chunk - full opacity, bold, with bright yellow background
                    withStyle(
                        SpanStyle(
                            color = textColor,
                            fontWeight = FontWeight.Bold,
                            background = Color(0xFFFFEB3B).copy(alpha = 0.5f) // Bright yellow highlight
                        )
                    ) {
                        append(chunk)
                    }
                } else if (index < safeIndex) {
                    // Already read chunks - more dimmed to make current stand out
                    withStyle(
                        SpanStyle(
                            color = textColor.copy(alpha = 0.4f),
                            fontWeight = FontWeight(fontWeight)
                        )
                    ) {
                        append(chunk)
                    }
                } else {
                    // Upcoming chunks - even more dimmed
                    withStyle(
                        SpanStyle(
                            color = textColor.copy(alpha = 0.35f),
                            fontWeight = FontWeight(fontWeight)
                        )
                    ) {
                        append(chunk)
                    }
                }
                
                // Add space between chunks (except for last one)
                if (index < sentences.lastIndex) {
                    append(" ")
                }
            }
        }
    }
    
    Text(
        text = annotatedText,
        modifier = modifier,
        fontSize = fontSize,
        textAlign = textAlignment,
        lineHeight = lineHeight
    )
}

/**
 * Lightweight cache indicator component
 */
@Composable
private fun CacheIndicator(
    isCached: Boolean,
    isLoadingCache: Boolean,
    textColor: Color
) {
    Box(
        modifier = Modifier
            .size(8.dp)
            .padding(top = 6.dp)
    ) {
        val indicatorColor = when {
            isCached -> Color(0xFF4CAF50) // Green for cached
            isLoadingCache -> Color(0xFFFFC107) // Yellow for loading
            else -> textColor.copy(alpha = 0.2f) // Gray for not cached
        }
        Box(
            modifier = Modifier
                .size(6.dp)
                .background(indicatorColor, shape = CircleShape)
        )
    }
}

/**
 * Lightweight progress indicator component
 */
@Composable
private fun TTSProgressIndicator(
    currentParagraph: Int,
    totalParagraphs: Int,
    showTranslation: Boolean,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${currentParagraph + 1}/$totalParagraphs",
                style = MaterialTheme.typography.labelSmall,
                color = textColor.copy(alpha = 0.7f)
            )
            
            if (showTranslation) {
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


/**
 * Unified Media Controls for TTS
 * Adapts to mobile/tablet/desktop layouts
 * 
 * Optimizations for low-end devices:
 * - Memoized progress text to avoid string allocations
 * - Pre-computed icon sizes
 * - Extracted sub-components for better recomposition control
 */
@Composable
fun TTSMediaControls(
    state: CommonTTSScreenState,
    actions: CommonTTSActions,
    isTabletOrDesktop: Boolean = false,
    modifier: Modifier = Modifier
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    
    // Pre-compute sizes to avoid repeated calculations
    val tonalElevation = remember(isTabletOrDesktop) { if (isTabletOrDesktop) 8.dp else 4.dp }
    val iconSize = remember(isTabletOrDesktop) { if (isTabletOrDesktop) 32.dp else 28.dp }
    val fabSize = remember(isTabletOrDesktop) { if (isTabletOrDesktop) 72.dp else 64.dp }
    val fabIconSize = remember(isTabletOrDesktop) { if (isTabletOrDesktop) 40.dp else 32.dp }
    
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = tonalElevation
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Progress bar - only render if content exists
            if (state.hasContent) {
                TTSProgressBar(
                    currentParagraph = state.currentReadingParagraph,
                    totalParagraphs = state.contentSize,
                    progressFraction = state.progressFraction,
                    sleepModeEnabled = state.sleepModeEnabled,
                    sleepTimeRemaining = state.sleepTimeRemaining,
                    selectedVoiceModel = state.selectedVoiceModel
                )
            }
            
            // Main playback controls - extracted for better recomposition
            TTSPlaybackControls(
                isPlaying = state.isPlaying,
                isLoading = state.isLoading,
                currentParagraph = state.currentReadingParagraph,
                contentLastIndex = state.content.lastIndex,
                hasChapter = state.chapterName.isNotEmpty(),
                iconSize = iconSize,
                fabSize = fabSize,
                fabIconSize = fabIconSize,
                onPreviousChapter = actions::onPreviousChapter,
                onPreviousParagraph = actions::onPreviousParagraph,
                onPlayPause = { if (state.isPlaying) actions.onPause() else actions.onPlay() },
                onNextParagraph = actions::onNextParagraph,
                onNextChapter = actions::onNextChapter
            )
            
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
 * Extracted progress bar component for better recomposition control
 */
@Composable
private fun TTSProgressBar(
    currentParagraph: Int,
    totalParagraphs: Int,
    progressFraction: Float,
    sleepModeEnabled: Boolean,
    sleepTimeRemaining: Long,
    selectedVoiceModel: String?
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    
    // Memoize progress text to avoid string allocation on every recomposition
    val progressText = remember(currentParagraph, totalParagraphs) {
        "Paragraph ${currentParagraph + 1} / $totalParagraphs"
    }
    
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = progressText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Sleep timer indicator - only render when active
                if (sleepModeEnabled && sleepTimeRemaining > 0) {
                    val timerText = remember(sleepTimeRemaining) {
                        val minutes = sleepTimeRemaining / 60000
                        val seconds = (sleepTimeRemaining % 60000) / 1000
                        "${minutes}:${seconds.toString().padStart(2, '0')}"
                    }
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
                            text = timerText,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }
                
                selectedVoiceModel?.let { model ->
                    Text(
                        text = model,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
        
        LinearProgressIndicator(
            progress = { progressFraction },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/**
 * Extracted playback controls for better recomposition control
 */
@Composable
private fun TTSPlaybackControls(
    isPlaying: Boolean,
    isLoading: Boolean,
    currentParagraph: Int,
    contentLastIndex: Int,
    hasChapter: Boolean,
    iconSize: androidx.compose.ui.unit.Dp,
    fabSize: androidx.compose.ui.unit.Dp,
    fabIconSize: androidx.compose.ui.unit.Dp,
    onPreviousChapter: () -> Unit,
    onPreviousParagraph: () -> Unit,
    onPlayPause: () -> Unit,
    onNextParagraph: () -> Unit,
    onNextChapter: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Previous Chapter
        IconButton(
            onClick = onPreviousChapter,
            enabled = hasChapter
        ) {
            Icon(
                Icons.Default.SkipPrevious,
                contentDescription = localize(Res.string.previous_chapter),
                modifier = Modifier.size(iconSize)
            )
        }
        
        // Previous Paragraph
        IconButton(
            onClick = onPreviousParagraph,
            enabled = currentParagraph > 0
        ) {
            Icon(
                Icons.Default.FastRewind,
                contentDescription = localize(Res.string.previous_paragraph),
                modifier = Modifier.size(iconSize)
            )
        }
        
        // Play/Pause (Large circular button)
        FloatingActionButton(
            onClick = onPlayPause,
            modifier = Modifier.size(fabSize),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            shape = CircleShape
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(32.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) localize(Res.string.pause) else localize(Res.string.play),
                    modifier = Modifier.size(fabIconSize)
                )
            }
        }
        
        // Next Paragraph
        IconButton(
            onClick = onNextParagraph,
            enabled = currentParagraph < contentLastIndex
        ) {
            Icon(
                Icons.Default.FastForward,
                contentDescription = localize(Res.string.next_paragraph),
                modifier = Modifier.size(iconSize)
            )
        }
        
        // Next Chapter
        IconButton(
            onClick = onNextChapter,
            enabled = hasChapter
        ) {
            Icon(
                Icons.Default.SkipNext,
                contentDescription = localize(Res.string.next_chapter),
                modifier = Modifier.size(iconSize)
            )
        }
    }
}

/**
 * Desktop/Tablet specific TTS controls with more options
 * Optimized with memoized values for low-end devices
 */
@Composable
private fun DesktopTTSControls(
    state: CommonTTSScreenState,
    actions: CommonTTSActions
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    
    // Memoize speed text to avoid String.format on every recomposition
    val speedText = remember(state.speechSpeed) {
        String.format("%.1fx", state.speechSpeed)
    }
    
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
                        text = speedText,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                Slider(
                    value = state.speechSpeed,
                    onValueChange = actions::onSpeedChange,
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
                    onCheckedChange = actions::onAutoNextChange
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
                    onClick = actions::onOpenSettings,
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
        
        // Translation controls - use pre-computed property
        if (state.hasTranslation) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterChip(
                    selected = state.showTranslation,
                    onClick = actions::onToggleTranslation,
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
                    onClick = actions::onToggleBilingualMode,
                    label = { Text(localizeHelper.localize(Res.string.bilingual)) },
                    enabled = state.showTranslation
                )
            }
        }
    }
}


/**
 * Mobile specific TTS controls - more compact
 * Optimized for low-end devices with memoized values
 */
@Composable
private fun MobileTTSControls(
    state: CommonTTSScreenState,
    actions: CommonTTSActions
) {
    val localizeHelper = requireNotNull(LocalLocalizeHelper.current) { "LocalLocalizeHelper not provided" }
    
    // Use pre-computed property from state
    val hasTranslation = state.hasTranslation
    
    // Memoize speed text to avoid String.format on every recomposition
    val speedText = remember(state.speechSpeed) {
        String.format("%.1fx", state.speechSpeed)
    }
    
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
                text = speedText,
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
                    onClick = actions::onToggleTranslation,
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
                        onClick = actions::onToggleBilingualMode,
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
            onClick = actions::onToggleFullScreen,
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
    sentenceHighlightEnabled: Boolean = true,
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
    onSentenceHighlightChange: (Boolean) -> Unit = {},
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
                            
                            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                            
                            // Sentence-level highlighting toggle
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Sentence Highlighting",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = if (sentenceHighlightEnabled) 
                                            "Highlights current sentence being read" 
                                        else 
                                            "Highlights entire paragraph",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Switch(
                                    checked = sentenceHighlightEnabled,
                                    onCheckedChange = onSentenceHighlightChange
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
