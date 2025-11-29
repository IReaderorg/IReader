package ireader.domain.services.tts_service

import ireader.core.log.Log
import ireader.domain.catalogs.CatalogStore
import ireader.domain.data.repository.BookRepository
import ireader.domain.data.repository.ChapterRepository
import ireader.domain.models.entities.Book
import ireader.domain.models.entities.CatalogLocal
import ireader.domain.models.entities.Chapter
import ireader.domain.preferences.prefs.AppPreferences
import ireader.domain.preferences.prefs.ReaderPreferences
import ireader.domain.usecases.remote.RemoteUseCases
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.coroutines.coroutineContext
import kotlin.time.Duration.Companion.minutes

/**
 * Base TTS Service Implementation
 * Contains shared logic for both Android and Desktop
 */
@OptIn(kotlin.time.ExperimentalTime::class)
abstract class BaseTTSService(
    protected val bookRepo: BookRepository,
    protected val chapterRepo: ChapterRepository,
    protected val extensions: CatalogStore,
    protected val remoteUseCases: RemoteUseCases,
    protected val readerPreferences: ReaderPreferences,
    protected val appPrefs: AppPreferences
) : CommonTTSService {
    
    protected val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    // State flows
    private val _isPlaying = MutableStateFlow(false)
    private val _isLoading = MutableStateFlow(false)
    private val _currentBook = MutableStateFlow<Book?>(null)
    private val _currentChapter = MutableStateFlow<Chapter?>(null)
    private val _currentParagraph = MutableStateFlow(0)
    private val _previousParagraph = MutableStateFlow(0)
    private val _totalParagraphs = MutableStateFlow(0)
    private val _currentContent = MutableStateFlow<List<String>>(emptyList())
    private val _speechSpeed = MutableStateFlow(1.0f)
    private val _speechPitch = MutableStateFlow(1.0f)
    private val _autoNextChapter = MutableStateFlow(true)
    private val _error = MutableStateFlow<String?>(null)
    private val _cachedParagraphs = MutableStateFlow<Set<Int>>(emptySet())
    private val _loadingParagraphs = MutableStateFlow<Set<Int>>(emptySet())
    
    // Sleep timer state
    private val _sleepTimeRemaining = MutableStateFlow(0L)
    private val _sleepModeEnabled = MutableStateFlow(false)
    private var sleepTimerJob: kotlinx.coroutines.Job? = null
    
    // Audio focus state
    private val _hasAudioFocus = MutableStateFlow(false)
    
    // TTS Engine
    protected var ttsEngine: TTSEngine? = null
    protected var ttsNotification: TTSNotification? = null
    
    // Chapter list
    protected var chapters: List<Chapter> = emptyList()
    protected var catalog: CatalogLocal? = null
    
    // Sleep timer
    protected var sleepTime: Long = 0
    protected var sleepMode: Boolean = false
    protected var startTime: kotlin.time.Instant? = null
    
    override val state: TTSServiceState = object : TTSServiceState {
        override val isPlaying = _isPlaying.asStateFlow()
        override val isLoading = _isLoading.asStateFlow()
        override val currentBook = _currentBook.asStateFlow()
        override val currentChapter = _currentChapter.asStateFlow()
        override val currentParagraph = _currentParagraph.asStateFlow()
        override val previousParagraph = _previousParagraph.asStateFlow()
        override val totalParagraphs = _totalParagraphs.asStateFlow()
        override val currentContent = _currentContent.asStateFlow()
        override val speechSpeed = _speechSpeed.asStateFlow()
        override val speechPitch = _speechPitch.asStateFlow()
        override val autoNextChapter = _autoNextChapter.asStateFlow()
        override val error = _error.asStateFlow()
        override val cachedParagraphs = _cachedParagraphs.asStateFlow()
        override val loadingParagraphs = _loadingParagraphs.asStateFlow()
        override val sleepTimeRemaining = _sleepTimeRemaining.asStateFlow()
        override val sleepModeEnabled = _sleepModeEnabled.asStateFlow()
        override val hasAudioFocus = _hasAudioFocus.asStateFlow()
    }
    
    override fun initialize() {
        scope.launch {
            // Load preferences
            _speechSpeed.value = readerPreferences.speechRate().get()
            _speechPitch.value = readerPreferences.speechPitch().get()
            _autoNextChapter.value = readerPreferences.readerAutoNext().get()
            sleepTime = readerPreferences.sleepTime().get()
            sleepMode = readerPreferences.sleepMode().get()
            
            // Observe preference changes
            observePreferences()
            
            // Initialize platform-specific components
            initializePlatformComponents()
        }
    }
    
    /**
     * Platform-specific initialization
     * Override in Android/Desktop implementations
     */
    protected abstract suspend fun initializePlatformComponents()
    
    /**
     * Create TTS engine for the platform
     * Override in Android/Desktop implementations
     */
    protected abstract fun createTTSEngine(): TTSEngine
    
    /**
     * Create notification for the platform
     * Override in Android/Desktop implementations
     */
    protected abstract fun createNotification(): TTSNotification
    
    override suspend fun startReading(bookId: Long, chapterId: Long, autoPlay: Boolean) {
        try {
            _isLoading.value = true
            _error.value = null
            
            // Load book
            val book = bookRepo.findBookById(bookId)
            if (book == null) {
                _error.value = "Book not found"
                _isLoading.value = false
                return
            }
            _currentBook.value = book
            
            // Load chapters
            chapters = chapterRepo.findChaptersByBookId(bookId)
            catalog = extensions.get(book.sourceId)
            
            // Load chapter
            var chapter = chapterRepo.findChapterById(chapterId)
            if (chapter == null) {
                _error.value = "Chapter not found"
                _isLoading.value = false
                return
            }
            
            // Load chapter content if empty
            if (chapter.content.isEmpty()) {
                chapter = loadChapterContent(chapter) ?: run {
                    _error.value = "Failed to load chapter content"
                    _isLoading.value = false
                    return
                }
            }
            
            _currentChapter.value = chapter
            
            // Extract paragraphs
            val content = extractParagraphs(chapter)
            _currentContent.value = content
            _totalParagraphs.value = content.size
            _currentParagraph.value = 0
            
            _isLoading.value = false
            
            // Initialize TTS engine if needed
            if (ttsEngine == null) {
                ttsEngine = createTTSEngine()
                setupEngineCallbacks()
            }
            
            // Initialize notification if needed
            if (ttsNotification == null) {
                ttsNotification = createNotification()
            }
            
            // Show notification
            updateNotification()
            
            // Auto-play if requested
            if (autoPlay) {
                play()
            }
            
        } catch (e: Exception) {
            Log.error { "Error starting reading: ${e.message}" }
            _error.value = e.message
            _isLoading.value = false
        }
    }
    
    override suspend fun play() {
        if (_currentContent.value.isEmpty()) {
            _error.value = "No content to read"
            return
        }
        
        _isPlaying.value = true
        startTime = kotlin.time.Clock.System.now()
        
        // Update notification
        updateNotification()
        
        // Read current paragraph
        readCurrentParagraph()
    }
    
    override suspend fun pause() {
        _isPlaying.value = false
        ttsEngine?.pause()
        
        // Update notification
        updateNotification()
    }
    
    override suspend fun stop() {
        _isPlaying.value = false
        ttsEngine?.stop()
        
        // Hide notification
        ttsNotification?.hide()
        
        // Reset state
        _currentParagraph.value = 0
        startTime = null
    }
    
    override suspend fun nextChapter() {
        val chapter = _currentChapter.value ?: return
        val index = chapters.indexOfFirst { it.id == chapter.id }
        
        if (index < chapters.lastIndex) {
            val nextChapter = chapters[index + 1]
            loadAndPlayChapter(nextChapter.id)
        }
    }
    
    override suspend fun previousChapter() {
        val chapter = _currentChapter.value ?: return
        val index = chapters.indexOfFirst { it.id == chapter.id }
        
        if (index > 0) {
            val prevChapter = chapters[index - 1]
            loadAndPlayChapter(prevChapter.id)
        }
    }
    
    override suspend fun nextParagraph() {
        val content = _currentContent.value
        val current = _currentParagraph.value
        
        if (current < content.lastIndex) {
            _currentParagraph.value = current + 1
            
            if (_isPlaying.value) {
                ttsEngine?.stop()
                readCurrentParagraph()
            }
            
            updateNotification()
        }
    }
    
    override suspend fun previousParagraph() {
        val current = _currentParagraph.value
        
        if (current > 0) {
            _currentParagraph.value = current - 1
            
            if (_isPlaying.value) {
                ttsEngine?.stop()
                readCurrentParagraph()
            }
            
            updateNotification()
        }
    }
    
    override fun setSpeed(speed: Float) {
        _speechSpeed.value = speed
        ttsEngine?.setSpeed(speed)
        readerPreferences.speechRate().set(speed)
    }
    
    override fun setPitch(pitch: Float) {
        _speechPitch.value = pitch
        ttsEngine?.setPitch(pitch)
        readerPreferences.speechPitch().set(pitch)
    }
    
    override suspend fun jumpToParagraph(index: Int) {
        val content = _currentContent.value
        
        if (index in content.indices) {
            _currentParagraph.value = index
            
            if (_isPlaying.value) {
                ttsEngine?.stop()
                readCurrentParagraph()
            }
            
            updateNotification()
        }
    }
    
    // Store original content for restoration
    private var originalContent: List<String>? = null
    
    override fun setCustomContent(content: List<String>?) {
        if (content != null && content.isNotEmpty()) {
            // Save original content if not already saved
            if (originalContent == null) {
                originalContent = _currentContent.value
            }
            // Set custom content (e.g., translated content)
            _currentContent.value = content
            _totalParagraphs.value = content.size
            // Reset paragraph if out of bounds
            if (_currentParagraph.value >= content.size) {
                _currentParagraph.value = 0
            }
        } else {
            // Restore original content
            originalContent?.let {
                _currentContent.value = it
                _totalParagraphs.value = it.size
                if (_currentParagraph.value >= it.size) {
                    _currentParagraph.value = 0
                }
            }
            originalContent = null
        }
    }
    
    override fun getAvailableEngines(): List<String> {
        return getPlatformAvailableEngines()
    }
    
    override fun getCurrentEngineName(): String {
        return ttsEngine?.getEngineName() ?: "None"
    }
    
    override fun isReady(): Boolean {
        return ttsEngine?.isReady() ?: false
    }
    
    /**
     * Get platform-specific available engines
     * Override in Android/Desktop implementations
     */
    protected abstract fun getPlatformAvailableEngines(): List<String>
    
    override fun cleanup() {
        cancelSleepTimer()
        scope.launch {
            stop()
        }
        ttsEngine?.cleanup()
        ttsEngine = null
        ttsNotification = null
    }
    
    override fun setSleepTimer(minutes: Int) {
        cancelSleepTimer()
        
        if (minutes > 0) {
            _sleepModeEnabled.value = true
            val totalMs = minutes * 60 * 1000L
            _sleepTimeRemaining.value = totalMs
            
            sleepTimerJob = scope.launch {
                var remaining = totalMs
                while (remaining > 0 && coroutineContext.isActive) {
                    delay(1000)
                    remaining -= 1000
                    _sleepTimeRemaining.value = remaining.coerceAtLeast(0)
                }
                
                if (remaining <= 0) {
                    // Sleep timer expired - stop TTS
                    stop()
                    _sleepModeEnabled.value = false
                    _sleepTimeRemaining.value = 0L
                }
            }
        }
    }
    
    override fun cancelSleepTimer() {
        sleepTimerJob?.cancel()
        sleepTimerJob = null
        _sleepModeEnabled.value = false
        _sleepTimeRemaining.value = 0L
    }
    
    override fun setAutoNextChapter(enabled: Boolean) {
        _autoNextChapter.value = enabled
        readerPreferences.readerAutoNext().set(enabled)
    }
    
    /**
     * Set audio focus state (called by platform implementations)
     */
    protected fun setAudioFocus(hasFocus: Boolean) {
        _hasAudioFocus.value = hasFocus
    }
    
    // ========== Protected Helper Methods ==========
    
    protected suspend fun readCurrentParagraph() {
        val content = _currentContent.value
        val index = _currentParagraph.value
        
        if (index >= content.size) {
            Log.error { "Paragraph index out of bounds: $index >= ${content.size}" }
            return
        }
        
        val text = content[index]
        val utteranceId = index.toString()
        
        // Pre-cache next paragraphs (platform-specific)
        precacheNextParagraphs()
        
        // Speak
        ttsEngine?.speak(text, utteranceId)
    }
    
    /**
     * Pre-cache next paragraphs for smooth playback
     * Override in platform implementations for engine-specific caching
     */
    protected open suspend fun precacheNextParagraphs() {
        // Default: no caching
        // Override in Android implementation for Coqui TTS caching
    }
    
    protected fun setupEngineCallbacks() {
        ttsEngine?.setCallback(object : TTSEngineCallback {
            override fun onStart(utteranceId: String) {
                scope.launch {
                    updateNotification()
                }
            }
            
            override fun onDone(utteranceId: String) {
                scope.launch {
                    handleParagraphComplete()
                }
            }
            
            override fun onError(utteranceId: String, error: String) {
                Log.error { "TTS error: $error" }
                scope.launch {
                    _error.value = error
                    handleParagraphComplete()
                }
            }
        })
    }
    
    protected suspend fun handleParagraphComplete() {
        checkSleepTime()
        
        val content = _currentContent.value
        val current = _currentParagraph.value
        val isFinished = current >= content.lastIndex
        
        if (!isFinished && _isPlaying.value) {
            // Track previous paragraph for UI highlighting
            _previousParagraph.value = current
            _currentParagraph.value = current + 1
            readCurrentParagraph()
            updateNotification()
        } else if (isFinished && _isPlaying.value && _autoNextChapter.value) {
            // Auto-advance to next chapter
            _previousParagraph.value = 0
            nextChapter()
        } else {
            _isPlaying.value = false
            updateNotification()
        }
    }
    
    protected fun checkSleepTime() {
        val start = startTime ?: return
        val sleepDuration = sleepTime.minutes
        val now = kotlin.time.Clock.System.now()
        
        if (sleepMode && now - start > sleepDuration) {
            scope.launch {
                stop()
            }
        }
    }
    
    protected suspend fun loadAndPlayChapter(chapterId: Long) {
        val wasPlaying = _isPlaying.value
        
        // Stop current playback
        ttsEngine?.stop()
        _isPlaying.value = false
        
        // Load new chapter
        _isLoading.value = true
        
        var chapter = chapterRepo.findChapterById(chapterId)
        if (chapter == null) {
            _error.value = "Chapter not found"
            _isLoading.value = false
            return
        }
        
        // Load content if empty
        if (chapter.content.isEmpty()) {
            chapter = loadChapterContent(chapter) ?: run {
                _error.value = "Failed to load chapter content"
                _isLoading.value = false
                return
            }
        }
        
        _currentChapter.value = chapter
        
        // Extract paragraphs
        val content = extractParagraphs(chapter)
        _currentContent.value = content
        _totalParagraphs.value = content.size
        _currentParagraph.value = 0
        
        _isLoading.value = false
        
        // Update notification
        updateNotification()
        
        // Resume playing if was playing
        if (wasPlaying) {
            play()
        }
    }
    
    protected suspend fun loadChapterContent(chapter: Chapter): Chapter? {
        val source = catalog ?: return null
        
        return try {
            var loadedChapter: Chapter? = null
            var loadError: String? = null
            
            remoteUseCases.getRemoteReadingContent(
                chapter = chapter,
                catalog = source,
                onSuccess = { remoteChapter ->
                    loadedChapter = remoteChapter
                },
                onError = { error ->
                    Log.error { "Failed to load chapter: $error" }
                    loadError = error.toString()
                }
            )
            
            // Save the chapter to repository after loading (outside the callback)
            loadedChapter?.let { remoteChapter ->
                try {
                    chapterRepo.insertChapter(remoteChapter)
                    Log.debug { "Chapter content saved to repository: ${remoteChapter.id}" }
                } catch (e: Exception) {
                    Log.error { "Failed to save chapter to repository: ${e.message}" }
                }
            }
            
            loadedChapter
        } catch (e: Exception) {
            Log.error { "Error loading chapter: ${e.message}" }
            null
        }
    }
    
    protected fun extractParagraphs(chapter: Chapter): List<String> {
        // Handle different content types
        val content = chapter.content
        
        return when {
            // If content is empty, return empty list
            content.isEmpty() -> emptyList()
            
            // If content is a list of Text objects
            content.firstOrNull() is ireader.core.source.model.Text -> {
                content
                    .filterIsInstance<ireader.core.source.model.Text>()
                    .map { it.text }
                    .filter { it.isNotBlank() }
                    .map { it.trim() }
            }
            
            // Otherwise, treat as string content and split by paragraphs
            else -> {
                content.joinToString("\n") { 
                    when (it) {
                        is ireader.core.source.model.Text -> it.text
                        else -> it.toString()
                    }
                }
                .split(Regex("\n\n+")) // Split by double newlines
                .filter { it.isNotBlank() }
                .map { it.trim() }
            }
        }
    }
    
    protected fun updateNotification() {
        val book = _currentBook.value
        val chapter = _currentChapter.value
        val current = _currentParagraph.value
        val total = _totalParagraphs.value
        val isPlaying = _isPlaying.value
        val isLoading = _isLoading.value
        
        if (book != null && chapter != null) {
            ttsNotification?.show(
                TTSNotificationData(
                    title = chapter.name,
                    subtitle = book.title,
                    coverUrl = book.cover,
                    isPlaying = isPlaying,
                    isLoading = isLoading,
                    currentParagraph = current,
                    totalParagraphs = total,
                    bookId = book.id,
                    chapterId = chapter.id,
                    sourceId = book.sourceId
                )
            )
        }
    }
    
    private fun observePreferences() {
        scope.launch {
            readerPreferences.speechRate().changes().collect { speed ->
                _speechSpeed.value = speed
                ttsEngine?.setSpeed(speed)
            }
        }
        
        scope.launch {
            readerPreferences.speechPitch().changes().collect { pitch ->
                _speechPitch.value = pitch
                ttsEngine?.setPitch(pitch)
            }
        }
        
        scope.launch {
            readerPreferences.readerAutoNext().changes().collect { autoNext ->
                _autoNextChapter.value = autoNext
            }
        }
        
        scope.launch {
            readerPreferences.sleepTime().changes().collect { time ->
                sleepTime = time
            }
        }
        
        scope.launch {
            readerPreferences.sleepMode().changes().collect { mode ->
                sleepMode = mode
            }
        }
    }
}
