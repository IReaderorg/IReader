package ireader.domain.services.tts_service

import android.content.Context
import android.content.Intent
import ireader.core.log.Log
import ireader.domain.models.entities.Book
import ireader.domain.models.entities.Chapter
import ireader.domain.services.tts_service.media_player.TTSService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.time.ExperimentalTime

/**
 * Android TTS Service Adapter
 * 
 * This adapter bridges the CommonTTSService interface with the existing
 * Android TTSService (MediaBrowserServiceCompat) which handles notifications
 * and media session properly.
 * 
 * It delegates all operations to the TTSService via Intents and observes
 * the shared TTSStateImpl for state updates.
 */
class AndroidTTSServiceAdapter(
    private val context: Context,
    private val sharedState: TTSStateImpl
) : CommonTTSService {
    
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    // Adapter state that wraps the shared state
    override val state: TTSServiceState = AndroidTTSStateAdapter(sharedState)
    
    override fun initialize() {
        Log.info { "AndroidTTSServiceAdapter initialized" }
    }
    
    override suspend fun startReading(bookId: Long, chapterId: Long, autoPlay: Boolean) {
        Log.info { "Starting TTS reading: bookId=$bookId, chapterId=$chapterId, autoPlay=$autoPlay" }
        
        // Start the TTSService with the book and chapter
        val intent = Intent(context, TTSService::class.java).apply {
            action = TTSService.ACTION_UPDATE
            putExtra(TTSService.TTS_BOOK_ID, bookId)
            putExtra(TTSService.TTS_Chapter_ID, chapterId)
            if (autoPlay) {
                putExtra(TTSService.COMMAND, Player.PLAY)
            }
        }
        context.startService(intent)
    }
    
    override suspend fun play() {
        Log.info { "TTS play" }
        // Set loading state if TTS is not ready yet
        if (!sharedState.isTTSReady.value) {
            sharedState.setLoading(true)
        }
        val intent = Intent(context, TTSService::class.java).apply {
            action = TTSService.ACTION_PLAY
        }
        context.startService(intent)
    }
    
    override suspend fun pause() {
        Log.info { "TTS pause" }
        val intent = Intent(context, TTSService::class.java).apply {
            action = TTSService.ACTION_PAUSE
        }
        context.startService(intent)
    }
    
    override suspend fun stop() {
        Log.info { "TTS stop" }
        val intent = Intent(context, TTSService::class.java).apply {
            action = TTSService.ACTION_STOP
        }
        context.startService(intent)
    }
    
    override suspend fun nextChapter() {
        Log.info { "TTS next chapter" }
        val intent = Intent(context, TTSService::class.java).apply {
            action = TTSService.ACTION_NEXT
        }
        context.startService(intent)
    }
    
    override suspend fun previousChapter() {
        Log.info { "TTS previous chapter" }
        val intent = Intent(context, TTSService::class.java).apply {
            action = TTSService.ACTION_PREVIOUS
        }
        context.startService(intent)
    }
    
    override suspend fun nextParagraph() {
        Log.info { "TTS next paragraph" }
        val intent = Intent(context, TTSService::class.java).apply {
            action = TTSService.ACTION_NEXT_PARAGRAPH
        }
        context.startService(intent)
    }
    
    override suspend fun previousParagraph() {
        Log.info { "TTS previous paragraph" }
        val intent = Intent(context, TTSService::class.java).apply {
            action = TTSService.ACTION_PREVIOUS_PARAGRAPH
        }
        context.startService(intent)
    }
    
    override fun setSpeed(speed: Float) {
        Log.info { "TTS set speed: $speed" }
        sharedState.setSpeechRate(speed)
    }
    
    override fun setPitch(pitch: Float) {
        Log.info { "TTS set pitch: $pitch" }
        sharedState.setPitch(pitch)
    }
    
    override suspend fun jumpToParagraph(index: Int) {
        Log.info { "TTS jump to paragraph: $index" }
        val intent = Intent(context, TTSService::class.java).apply {
            action = TTSService.ACTION_JUMP_TO_PARAGRAPH
            putExtra(TTSService.PARAGRAPH_INDEX, index)
        }
        context.startService(intent)
    }
    
    override fun setCustomContent(content: List<String>?) {
        Log.info { "TTS set custom content: ${content?.size ?: 0} paragraphs" }
        if (content != null && content.isNotEmpty()) {
            sharedState.setTtsContent(content)
        }
        // Note: To restore original content, the caller should reload the chapter
    }
    
    override fun getAvailableEngines(): List<String> {
        return listOf("Native Android TTS", "Gradio TTS (Online)")
    }
    
    override fun getCurrentEngineName(): String {
        return if (sharedState.useGradioTTS) "Gradio TTS" else "Native Android TTS"
    }
    
    override fun isReady(): Boolean {
        return true // Service is always ready to receive commands
    }
    
    override fun cleanup() {
        Log.info { "TTS cleanup" }
        scope.launch {
            stop()
        }
    }
    
    @OptIn(ExperimentalTime::class)
    override fun setSleepTimer(minutes: Int) {
        Log.info { "TTS set sleep timer: $minutes minutes" }
        // Update shared state
        sharedState.setSleepTime(minutes.toLong())
        sharedState.setSleepMode(minutes > 0)
        if (minutes > 0) {
            sharedState.setStartTime(kotlin.time.Clock.System.now())
        }
    }
    
    override fun cancelSleepTimer() {
        Log.info { "TTS cancel sleep timer" }
        sharedState.setSleepMode(false)
        sharedState.setSleepTime(0)
    }
    
    override fun setAutoNextChapter(enabled: Boolean) {
        Log.info { "TTS set auto next chapter: $enabled" }
        sharedState.setAutoNextChapter(enabled)
    }
}

/**
 * Adapter that wraps TTSStateImpl to implement TTSServiceState
 */
@OptIn(ExperimentalTime::class)
class AndroidTTSStateAdapter(
    private val sharedState: TTSStateImpl
) : TTSServiceState {
    
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    override val isPlaying: StateFlow<Boolean> = sharedState.isPlaying
    override val isLoading: StateFlow<Boolean> = sharedState.isLoading
    override val currentBook: StateFlow<Book?> = sharedState.ttsBook
    override val currentChapter: StateFlow<Chapter?> = sharedState.ttsChapter
    override val currentParagraph: StateFlow<Int> = sharedState.currentReadingParagraph
    override val previousParagraph: StateFlow<Int> = sharedState.previousReadingParagraph
    
    // Extract timestamp from utteranceId (format: "paragraphIndex_timestamp")
    private val _paragraphSpeakingStartTime = MutableStateFlow(0L)
    override val paragraphSpeakingStartTime: StateFlow<Long> = _paragraphSpeakingStartTime.asStateFlow()
    
    private val _totalParagraphs = MutableStateFlow(0)
    override val totalParagraphs: StateFlow<Int> = _totalParagraphs.asStateFlow()
    
    private val _currentContent = MutableStateFlow<List<String>>(emptyList())
    override val currentContent: StateFlow<List<String>> = _currentContent.asStateFlow()
    
    override val speechSpeed: StateFlow<Float> = sharedState.speechSpeed
    override val speechPitch: StateFlow<Float> = sharedState.pitch
    override val autoNextChapter: StateFlow<Boolean> = sharedState.autoNextChapter
    override val error: StateFlow<String?> = MutableStateFlow(null)
    
    private val _cachedParagraphs = MutableStateFlow<Set<Int>>(emptySet())
    override val cachedParagraphs: StateFlow<Set<Int>> = _cachedParagraphs.asStateFlow()
    
    private val _loadingParagraphs = MutableStateFlow<Set<Int>>(emptySet())
    override val loadingParagraphs: StateFlow<Set<Int>> = _loadingParagraphs.asStateFlow()
    
    // Sleep timer state
    private val _sleepTimeRemaining = MutableStateFlow(0L)
    override val sleepTimeRemaining: StateFlow<Long> = _sleepTimeRemaining.asStateFlow()
    
    override val sleepModeEnabled: StateFlow<Boolean> = sharedState.sleepMode
    
    // Audio focus state
    private val _hasAudioFocus = MutableStateFlow(true)
    override val hasAudioFocus: StateFlow<Boolean> = _hasAudioFocus.asStateFlow()
    
    // TTS engine ready state
    override val isTTSReady: StateFlow<Boolean> = sharedState.isTTSReady
    
    init {
        // Observe ttsContent and update currentContent
        scope.launch {
            sharedState.ttsContent.collect { content ->
                val nonNullContent = content ?: emptyList()
                _currentContent.value = nonNullContent
                _totalParagraphs.value = nonNullContent.size
            }
        }
        
        // Extract timestamp from utteranceId (format: "paragraphIndex_actualStartTimestamp")
        // BRILLIANT SYNC: The timestamp is embedded in utteranceId by TTSService.onStart()
        // This gives us the EXACT moment TTS actually started speaking, not when speak() was called
        scope.launch {
            sharedState.utteranceId.collect { utteranceId ->
                if (utteranceId.contains("_")) {
                    // Get the last part which is the actual start timestamp
                    val parts = utteranceId.split("_")
                    parts.lastOrNull()?.toLongOrNull()?.let { timestamp ->
                        _paragraphSpeakingStartTime.value = timestamp
                    }
                }
            }
        }
        
        // Periodically sync cache status and sleep timer from shared state
        scope.launch {
            while (true) {
                _cachedParagraphs.value = sharedState.cachedParagraphs
                _loadingParagraphs.value = sharedState.loadingParagraphs
                
                // Calculate sleep timer remaining
                if (sharedState.sleepMode.value) {
                    val startTime = sharedState.startTime.value
                    val sleepMinutes = sharedState.sleepTime.value
                    if (startTime != null && sleepMinutes > 0) {
                        val now = kotlin.time.Clock.System.now()
                        val elapsed = now - startTime
                        val totalMs = sleepMinutes * 60 * 1000L
                        val remaining = (totalMs - elapsed.inWholeMilliseconds).coerceAtLeast(0)
                        _sleepTimeRemaining.value = remaining
                    }
                } else {
                    _sleepTimeRemaining.value = 0L
                }
                
                kotlinx.coroutines.delay(500) // Update every 500ms
            }
        }
    }
    
    fun updateCacheStatus(cached: Set<Int>, loading: Set<Int>) {
        _cachedParagraphs.value = cached
        _loadingParagraphs.value = loading
    }
}
