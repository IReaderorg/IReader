package ireader.domain.usecases.tts

import android.content.Context
import android.support.v4.media.session.MediaSessionCompat
import ireader.core.log.Log
import ireader.domain.models.entities.Book
import ireader.domain.models.entities.Chapter
import ireader.domain.preferences.prefs.AppPreferences
import ireader.domain.preferences.prefs.ReaderPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Unified TTS Core for Android
 * 
 * This class manages all TTS functionality for Android, providing a single
 * interface for both Native TTS and Coqui TTS with automatic switching,
 * notification management, and state synchronization.
 * 
 * Features:
 * - Automatic provider switching (Native ↔ Coqui)
 * - Unified notification management
 * - State synchronization
 * - Paragraph tracking
 * - Auto-next chapter support
 * - Speed/pitch control
 */
class AndroidTTSCore(
    private val context: Context,
    private val mediaSession: MediaSessionCompat,
    private val appPreferences: AppPreferences,
    private val readerPreferences: ReaderPreferences
) {
    
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    // TTS Players
    private var nativePlayer: NativeTTSPlayer? = null
    private var coquiPlayer: CoquiTTSPlayerAdapter? = null
    private var currentPlayer: TTSPlayer? = null
    
    // Notification Manager
    val notificationManager = AndroidTTSNotificationManager(context, mediaSession)
    private val manageTTSNotification = ManageTTSNotification(notificationManager)
    
    // State
    private var currentBook: Book? = null
    private var currentChapter: Chapter? = null
    private var currentParagraphs: List<String> = emptyList()
    private var currentParagraphIndex = 0
    private var isPlaying = false
    
    // Callbacks
    private var onParagraphComplete: ((Int) -> Unit)? = null
    private var onChapterComplete: (() -> Unit)? = null
    
    init {
        setupNotificationCallbacks()
    }
    
    /**
     * Initialize TTS core
     */
    suspend fun initialize(): Result<Unit> {
        return try {
            // Initialize native player
            nativePlayer = NativeTTSPlayer(context)
            nativePlayer?.initialize()
            
            // Initialize Coqui player if enabled
            if (appPreferences.useCoquiTTS().get()) {
                val spaceUrl = appPreferences.coquiSpaceUrl().get()
                if (spaceUrl.isNotEmpty()) {
                    coquiPlayer = CoquiTTSPlayerAdapter(
                        context = context,
                        spaceUrl = spaceUrl,
                        apiKey = appPreferences.coquiApiKey().get()
                    )
                    coquiPlayer?.initialize()
                }
            }
            
            // Set current player
            switchToPreferredPlayer()
            
            // Setup callbacks
            setupPlayerCallbacks()
            
            Log.info { "AndroidTTSCore initialized with ${currentPlayer?.getProviderName()}" }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.error { "Failed to initialize AndroidTTSCore: ${e.message}" }
            Result.failure(e)
        }
    }
    
    /**
     * Start reading chapter
     */
    fun startReading(
        book: Book,
        chapter: Chapter,
        paragraphs: List<String>,
        startIndex: Int = 0,
        onParagraphComplete: ((Int) -> Unit)? = null,
        onChapterComplete: (() -> Unit)? = null
    ) {
        this.currentBook = book
        this.currentChapter = chapter
        this.currentParagraphs = paragraphs
        this.currentParagraphIndex = startIndex
        this.onParagraphComplete = onParagraphComplete
        this.onChapterComplete = onChapterComplete
        this.isPlaying = true
        
        // Show notification FIRST (this creates the notification object)
        showNotification()
        
        // Start reading
        readParagraph(startIndex)
    }
    
    /**
     * Read specific paragraph
     */
    private fun readParagraph(index: Int) {
        if (index >= currentParagraphs.size) {
            handleChapterComplete()
            return
        }
        
        if (!isPlaying) {
            return
        }
        
        currentParagraphIndex = index
        val text = currentParagraphs[index]
        val utteranceId = "paragraph_$index"
        
        // Update state to playing
        isPlaying = true
        
        // Update notification on Main thread
        scope.launch(Dispatchers.Main) {
            updateNotification()
        }
        
        // Speak on IO dispatcher to avoid blocking UI
        scope.launch(Dispatchers.IO) {
            currentPlayer?.speak(text, utteranceId)
        }
        
        // Preload next paragraphs for Coqui TTS (already on IO dispatcher)
        preloadNextParagraphs(index)
    }
    
    /**
     * Preload next paragraphs for smooth playback
     */
    private fun preloadNextParagraphs(currentIndex: Int) {
        if (currentPlayer?.getProviderName() != "Coqui TTS") {
            return
        }
        
        val coquiAdapter = currentPlayer as? CoquiTTSPlayerAdapter ?: return
        val coquiService = coquiAdapter.getCoquiService() ?: return
        
        // Preload next 3 paragraphs
        scope.launch(Dispatchers.IO) {
            for (i in 1..3) {
                val nextIndex = currentIndex + i
                if (nextIndex < currentParagraphs.size && isPlaying) {
                    try {
                        val result = coquiService.synthesize(
                            text = currentParagraphs[nextIndex],
                            voiceId = "default",
                            speed = currentPlayer?.getSpeechRate() ?: 1.0f,
                            pitch = currentPlayer?.getPitch() ?: 1.0f
                        )
                        result.onFailure { error ->
                            Log.error { "Failed to preload paragraph $nextIndex: ${error.message}" }
                        }
                    } catch (e: Exception) {
                        Log.error { "Exception preloading paragraph $nextIndex: ${e.message}" }
                    }
                }
            }
        }
    }
    
    /**
     * Pause reading
     */
    fun pause() {
        isPlaying = false
        currentPlayer?.pause()
        updateNotification()
    }
    
    /**
     * Resume reading
     */
    fun resume() {
        isPlaying = true
        
        // Try to resume if supported
        currentPlayer?.resume()
        
        // If resume not supported or not speaking, re-read current paragraph
        scope.launch {
            kotlinx.coroutines.delay(100) // Small delay to check if resume worked
            if (!currentPlayer?.isSpeaking()!!) {
                readParagraph(currentParagraphIndex)
            } else {
                updateNotification()
            }
        }
    }
    
    /**
     * Stop reading
     */
    fun stop() {
        isPlaying = false
        currentPlayer?.stop()
        manageTTSNotification.hide()
    }
    
    /**
     * Skip to next paragraph
     */
    fun nextParagraph() {
        if (currentParagraphIndex + 1 < currentParagraphs.size) {
            currentPlayer?.stop()
            readParagraph(currentParagraphIndex + 1)
        }
    }
    
    /**
     * Skip to previous paragraph
     */
    fun previousParagraph() {
        if (currentParagraphIndex > 0) {
            currentPlayer?.stop()
            readParagraph(currentParagraphIndex - 1)
        }
    }
    
    /**
     * Set speech rate
     */
    fun setSpeechRate(rate: Float) {
        currentPlayer?.setSpeechRate(rate)
        updateNotification()
    }
    
    /**
     * Set pitch
     */
    fun setPitch(pitch: Float) {
        currentPlayer?.setPitch(pitch)
    }
    
    /**
     * Switch TTS provider
     */
    fun switchProvider(useCoqui: Boolean) {
        val wasPlaying = isPlaying
        val currentIndex = currentParagraphIndex
        
        // Stop current player
        currentPlayer?.stop()
        
        // Switch player
        currentPlayer = if (useCoqui && coquiPlayer?.isReady() == true) {
            coquiPlayer
        } else {
            nativePlayer
        }
        
        setupPlayerCallbacks()
        
        // Resume if was playing
        if (wasPlaying) {
            readParagraph(currentIndex)
        }
        
        updateNotification()
        
        Log.info { "Switched to ${currentPlayer?.getProviderName()}" }
    }
    
    /**
     * Check if currently playing
     */
    fun isPlaying(): Boolean = isPlaying
    
    /**
     * Get current paragraph index
     */
    fun getCurrentParagraphIndex(): Int = currentParagraphIndex
    
    /**
     * Get current provider name
     */
    fun getCurrentProviderName(): String = currentPlayer?.getProviderName() ?: "None"
    
    /**
     * Cleanup resources
     */
    fun shutdown() {
        stop()
        nativePlayer?.shutdown()
        coquiPlayer?.shutdown()
        manageTTSNotification.cleanup()
        Log.info { "AndroidTTSCore shut down" }
    }
    
    // Private helper methods
    
    private fun switchToPreferredPlayer() {
        val useCoqui = appPreferences.useCoquiTTS().get()
        currentPlayer = if (useCoqui && coquiPlayer?.isReady() == true) {
            coquiPlayer
        } else {
            nativePlayer
        }
    }
    
    private fun setupPlayerCallbacks() {
        currentPlayer?.setCallback(object : TTSPlayerCallback {
            override fun onStart(utteranceId: String) {
                Log.info { "TTS started: $utteranceId (${currentPlayer?.getProviderName()})" }
                // Update notification to show playing state
                scope.launch(Dispatchers.Main) {
                    updateNotification()
                }
            }
            
            override fun onDone(utteranceId: String) {
                Log.info { "TTS done: $utteranceId (${currentPlayer?.getProviderName()})" }
                // Handle on Main dispatcher
                scope.launch(Dispatchers.Main) {
                    handleParagraphComplete()
                }
            }
            
            override fun onError(utteranceId: String, error: String) {
                Log.error { "TTS error: $error (${currentPlayer?.getProviderName()})" }
                // Try next paragraph on error
                scope.launch(Dispatchers.Main) {
                    handleParagraphComplete()
                }
            }
            
            override fun onStopped() {
                Log.info { "TTS stopped (${currentPlayer?.getProviderName()})" }
            }
        })
    }
    
    private fun setupNotificationCallbacks() {
        manageTTSNotification.setCallback(
            onPlayPause = {
                if (isPlaying) pause() else resume()
            },
            onStop = { stop() },
            onNext = { nextParagraph() },
            onPrevious = { previousParagraph() }
        )
    }
    
    private fun handleParagraphComplete() {
        Log.info { "✅ handleParagraphComplete: paragraph $currentParagraphIndex done, isPlaying=$isPlaying, total=${currentParagraphs.size}" }
        
        // Invoke callback BEFORE advancing
        onParagraphComplete?.invoke(currentParagraphIndex)
        
        // Auto-advance to next paragraph
        val nextIndex = currentParagraphIndex + 1
        Log.info { "📊 Next index would be: $nextIndex (current=$currentParagraphIndex, total=${currentParagraphs.size})" }
        
        if (isPlaying && nextIndex < currentParagraphs.size) {
            Log.info { "⏭️ Auto-advancing to paragraph $nextIndex" }
            readParagraph(nextIndex)
        } else {
            Log.info { "🏁 No more paragraphs (nextIndex=$nextIndex >= total=${currentParagraphs.size}) or stopped (isPlaying=$isPlaying)" }
            handleChapterComplete()
        }
    }
    
    private fun handleChapterComplete() {
        isPlaying = false
        onChapterComplete?.invoke()
        
        // Check if should auto-advance to next chapter
        val autoNext = readerPreferences.readerAutoNext().get()
        if (!autoNext) {
            manageTTSNotification.hide()
        }
    }
    
    private fun showNotification() {
        val book = currentBook ?: return
        val chapter = currentChapter ?: return
        
        val state = ttsNotificationState {
            playing(isPlaying)
            paragraph(currentParagraphIndex, currentParagraphs.size)
            bookTitle(book.title)
            chapterTitle(chapter.name)
            coverUrl(book.cover)
            speed(currentPlayer?.getSpeechRate() ?: 1.0f)
            provider(currentPlayer?.getProviderName() ?: "TTS")
        }
        
        manageTTSNotification.show(book, chapter, state)
    }
    
    /**
     * Get current notification for foreground service
     */
    fun getCurrentNotification(): android.app.Notification? {
        return notificationManager.getCurrentNotification()
    }
    
    /**
     * Get notification ID
     */
    fun getNotificationId(): Int {
        return notificationManager.getNotificationId()
    }
    
    private fun updateNotification() {
        val book = currentBook ?: return
        val chapter = currentChapter ?: return
        
        val state = ttsNotificationState {
            playing(isPlaying)
            paused(!isPlaying && currentParagraphIndex > 0)
            paragraph(currentParagraphIndex, currentParagraphs.size)
            bookTitle(book.title)
            chapterTitle(chapter.name)
            coverUrl(book.cover)
            speed(currentPlayer?.getSpeechRate() ?: 1.0f)
            provider(currentPlayer?.getProviderName() ?: "TTS")
        }
        
        manageTTSNotification.update(state)
    }
}
