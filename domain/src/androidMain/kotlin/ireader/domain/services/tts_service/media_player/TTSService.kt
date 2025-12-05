package ireader.domain.services.tts_service.media_player

import android.app.PendingIntent
import android.content.*
import android.media.*
import android.os.Build
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.NotificationManagerCompat
import androidx.media.MediaBrowserServiceCompat
import ireader.core.log.Log
import ireader.domain.catalogs.CatalogStore
import ireader.domain.data.repository.BookRepository
import ireader.domain.data.repository.ChapterRepository
import ireader.domain.models.entities.Book
import ireader.domain.models.entities.CatalogLocal
import ireader.domain.models.entities.Chapter
import ireader.domain.notification.NotificationsIds
import ireader.domain.preferences.prefs.AppPreferences
import ireader.domain.preferences.prefs.ReaderPreferences
import ireader.domain.services.tts_service.*
import ireader.domain.usecases.local.LocalGetChapterUseCase
import ireader.domain.usecases.preferences.TextReaderPrefUseCase
import ireader.domain.usecases.remote.RemoteUseCases
import ireader.i18n.LocalizeHelper
import ireader.i18n.R
import kotlinx.coroutines.*
import org.koin.android.ext.android.inject
import kotlin.time.Duration.Companion.minutes
import kotlin.time.ExperimentalTime

/**
 * Clean TTS Service with Unified Player Architecture
 * 
 * Only ONE TTS engine runs at a time (Native OR Gradio)
 * All legacy code removed and moved to appropriate classes
 */
class TTSService : MediaBrowserServiceCompat(), AudioManager.OnAudioFocusChangeListener {
    
    // Dependencies
    private val bookRepo: BookRepository by inject()
    private val chapterRepo: ChapterRepository by inject()
    private val chapterUseCase: LocalGetChapterUseCase by inject()
    private val remoteUseCases: RemoteUseCases by inject()
    private val extensions: CatalogStore by inject()
    private val textReaderPrefUseCase: TextReaderPrefUseCase by inject()
    private val readerPreferences: ReaderPreferences by inject()
    private val appPrefs: AppPreferences by inject()
    private val localizeHelper: LocalizeHelper by inject()
    private val historyUseCase: ireader.domain.usecases.history.HistoryUseCase by inject()
    
    // Unified TTS Engine - ONLY ONE engine at a time
    private var ttsEngine: TTSEngine? = null
    private var currentEngineType: TTSEngineType = TTSEngineType.NATIVE
    
    // Track the paragraph index when speech started to detect manual navigation
    @Volatile
    private var speechStartParagraph: Int = -1
    
    // Track if playback is paused (for proper resume behavior)
    @Volatile
    private var isPlaybackPaused: Boolean = false
    
    enum class TTSEngineType {
        NATIVE,
        GRADIO
    }
    
    // Service components
    private lateinit var ttsNotification: AndroidTTSNotificationImpl
    lateinit var state: TTSStateImpl
    lateinit var mediaSession: MediaSessionCompat
    lateinit var stateBuilder: PlaybackStateCompat.Builder
    
    private lateinit var mediaCallback: TTSSessionCallback
    private lateinit var notificationController: NotificationController
    private var controller: MediaControllerCompat? = null
    
    private val noisyReceiver = NoisyReceiver()
    private var noisyReceiverHooked = false
    private var resumeOnFocus = true
    private var isPlayerDispose = false
    private var focusRequest: AudioFocusRequest? = null
    
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val scope = CoroutineScope(Dispatchers.Main.immediate + SupervisorJob())
    
    var isNotificationForeground = false
    private var silence: MediaPlayer? = null
    
    companion object {
        const val TTS_SERVICE_NAME = "TTS_SERVICE"
        const val TTS_Chapter_ID = "chapterId"
        const val COMMAND = "command"
        const val TTS_BOOK_ID = "bookId"
        
        const val ACTION_STOP = "actionStop"
        const val ACTION_PAUSE = "actionPause"
        const val ACTION_PLAY_PAUSE = "actionPlayPause"
        const val ACTION_PLAY = "actionPlay"
        const val ACTION_NEXT = "actionNext"
        const val ACTION_PREVIOUS = "actionPrevious"
        const val ACTION_NEXT_PARAGRAPH = "actionNextParagraph"
        const val ACTION_PREVIOUS_PARAGRAPH = "actionPreviousParagraph"
        const val ACTION_JUMP_TO_PARAGRAPH = "actionJumpToParagraph"
        const val PARAGRAPH_INDEX = "paragraphIndex"
        const val UPDATE_PAGER = "update_pager"
        const val PAGE = "page"
        const val ACTION_UPDATE = "actionUpdate"
        const val ACTION_CANCEL = "actionCancel"
        
        const val NOVEL_ID = "novel_id"
        const val SOURCE_ID = "source_id"
        const val FAVORITE = "favorite"
        const val NOVEL_TITLE = "novel_title"
        const val NOVEL_COVER = "novel_cover"
        const val PROGRESS = "progress"
        const val LAST_PARAGRAPH = "last_paragraph"
        const val CHAPTER_TITLE = "chapter_title"
        const val CHAPTER_ID = "chapter_id"
        const val IS_LOADING = "is_loading"
        const val ERROR = "error"
    }
    
    // ========== Lifecycle ==========
    
    // Inject shared state instead of creating new instance
    private val sharedState: TTSStateImpl by inject()
    
    override fun onCreate() {
        super.onCreate()
        state = sharedState
        readPrefs()
        
        // Initialize media session
        mediaSession = MediaSessionCompat(this, TTS_SERVICE_NAME)
        sessionToken = mediaSession.sessionToken
        
        // Include all transport actions - both SKIP and FAST_FORWARD/REWIND do paragraph navigation
        // This ensures the notification shows controls regardless of which icons Android chooses to display
        stateBuilder = PlaybackStateCompat.Builder()
            .setActions(
                PlaybackStateCompat.ACTION_PLAY or
                PlaybackStateCompat.ACTION_STOP or
                PlaybackStateCompat.ACTION_PAUSE or
                PlaybackStateCompat.ACTION_PLAY_PAUSE or
                PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or
                PlaybackStateCompat.ACTION_FAST_FORWARD or
                PlaybackStateCompat.ACTION_REWIND
            )
        mediaSession.setPlaybackState(stateBuilder.build())
        
        // Set callbacks
        mediaCallback = TTSSessionCallback()
        mediaSession.setCallback(mediaCallback)
        
        // Set media button receiver
        val mediaReceiverPendingIntent = PendingIntent.getService(
            this,
            0,
            Intent(this, TTSService::class.java).apply { action = ACTION_PLAY_PAUSE },
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
        )
        mediaSession.setMediaButtonReceiver(mediaReceiverPendingIntent)
        
        // Activate media session
        try {
            mediaSession.isActive = true
        } catch (e: NullPointerException) {
            mediaSession.isActive = false
            mediaSession.setFlags(
                MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or
                MediaSessionCompat.FLAG_HANDLES_QUEUE_COMMANDS or
                MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS
            )
            mediaSession.isActive = true
        }
        
        // Initialize notification using abstraction
        ttsNotification = TTSNotificationFactory.create(object : TTSNotificationCallback {
            override fun onPlay() {
                scope.launch { handlePlay() }
            }
            
            override fun onPause() {
                scope.launch { handlePause() }
            }
            
            override fun onNext() {
                val chapter = state.ttsChapter.value
                val chapters = state.ttsChapters.value
                val source = state.ttsCatalog.value
                if (chapter != null && source != null) {
                    scope.launch { handleSkipNext(chapter, chapters, source) }
                }
            }
            
            override fun onPrevious() {
                val chapter = state.ttsChapter.value
                val chapters = state.ttsChapters.value
                val source = state.ttsCatalog.value
                if (chapter != null && source != null) {
                    scope.launch { handleSkipPrevious(chapter, chapters, source) }
                }
            }
            
            override fun onNextParagraph() {
                scope.launch { handleSkipNextParagraph() }
            }
            
            override fun onPreviousParagraph() {
                scope.launch { handleSkipPreviousParagraph() }
            }
            
            override fun onClose() {
                scope.launch { handleCancel() }
            }
            
            override fun onNotificationClick() {
                // Open TTS screen - handled by notification intent
            }
        }) as AndroidTTSNotificationImpl
        
        // Set media session for notification
        ttsNotification.setMediaSession(mediaSession)
        
        controller = MediaControllerCompat(this, mediaSession.sessionToken)
        
        notificationController = NotificationController()
        notificationController.start()
        
        silence = MediaPlayer.create(this, R.raw.silence).apply {
            isLooping = true
        }
        silence?.start()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when (it.action) {
                ACTION_UPDATE -> {
                    val command = it.getIntExtra(COMMAND, -1)
                    val bookId = it.getLongExtra(TTS_BOOK_ID, -1)
                    val chapterId = it.getLongExtra(TTS_Chapter_ID, -1)
                    
                    // Load book and chapter if provided - WAIT for completion
                    scope.launch {
                        // Only load book if provided and different from current
                        if (bookId != -1L && state.ttsBook.value?.id != bookId) {
                            state.setTtsBook(bookRepo.findBookById(bookId))
                        }
                        
                        // Load chapters list and catalog
                        state.ttsBook.value?.let { book ->
                            if (state.ttsChapters.value.isEmpty()) {
                                state.setTtsChapters(chapterRepo.findChaptersByBookId(book.id))
                            }
                            if (state.ttsCatalog.value == null) {
                                state.setTtsCatalog(extensions.get(book.sourceId))
                            }
                        }
                        
                        // Load chapter if provided and different from current
                        if (chapterId != -1L && state.ttsChapter.value?.id != chapterId) {
                            val source = state.ttsCatalog.value
                            if (source != null) {
                                // Use getRemoteChapter to properly load content (local or remote)
                                getRemoteChapter(chapterId, source, state) {
                                    // Reset paragraph to start of new chapter
                                    state.setCurrentReadingParagraph(0)
                                    state.setPreviousReadingParagraph(0)
                                    updateNotification()
                                    
                                    // Execute command AFTER data is loaded
                                    if (command != -1) {
                                        startService(command)
                                    }
                                }
                            } else {
                                // Fallback: just load from local database
                                val chapter = chapterRepo.findChapterById(chapterId)
                                state.setTtsChapter(chapter)
                                state.setCurrentReadingParagraph(0)
                                state.setPreviousReadingParagraph(0)
                                
                                if (command != -1) {
                                    startService(command)
                                }
                            }
                        } else if (command != -1) {
                            // Same chapter, just execute command
                            startService(command)
                        }
                    }
                }
                ACTION_PLAY -> startService(Player.PLAY)
                ACTION_PAUSE -> startService(Player.PAUSE)
                ACTION_PLAY_PAUSE -> startService(Player.PLAY_PAUSE)
                ACTION_STOP, ACTION_CANCEL -> scope.launch { handleCancel() }
                ACTION_NEXT -> {
                    val chapter = state.ttsChapter.value
                    val chapters = state.ttsChapters.value
                    val source = state.ttsCatalog.value
                    if (chapter != null && source != null) {
                        scope.launch { handleSkipNext(chapter, chapters, source) }
                    }
                }
                ACTION_PREVIOUS -> {
                    val chapter = state.ttsChapter.value
                    val chapters = state.ttsChapters.value
                    val source = state.ttsCatalog.value
                    if (chapter != null && source != null) {
                        scope.launch { handleSkipPrevious(chapter, chapters, source) }
                    }
                }
                ACTION_NEXT_PARAGRAPH -> {
                    scope.launch { handleSkipNextParagraph() }
                }
                ACTION_PREVIOUS_PARAGRAPH -> {
                    scope.launch { handleSkipPreviousParagraph() }
                }
                ACTION_JUMP_TO_PARAGRAPH -> {
                    val paragraphIndex = it.getIntExtra(PARAGRAPH_INDEX, -1)
                    if (paragraphIndex >= 0) {
                        scope.launch { handleJumpToParagraph(paragraphIndex) }
                    }
                }
            }
        }
        
        return START_STICKY
    }
    
    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        // When user swipes away the app, stop TTS and clean up
        scope.launch { handleCancel() }
    }
    
    override fun onDestroy() {
        // Stop playback immediately
        ttsEngine?.stop()
        state.setPlaying(false
)
        
        // Cancel notification when service is destroyed
        NotificationManagerCompat.from(this).cancel(NotificationsIds.ID_TTS)
        
        // Cleanup TTS engine
        ttsEngine?.cleanup()
        ttsEngine = null
        
        // Cleanup service components
        serviceScope.cancel()
        scope.cancel()
        notificationController.stop()
        silence?.release()
        silence = null
        
        if (noisyReceiverHooked) {
            runCatching { unregisterReceiver(noisyReceiver) }
            noisyReceiverHooked = false
        }
        
        unhookNotification()
        
        mediaSession.isActive = false
        mediaSession.release()
        
        super.onDestroy()
    }
    
    // ========== Unified TTS Player Management ==========
    
    /**
     * Initialize the TTS engine based on user preference
     * Only ONE engine is active at a time
     */
    private fun initializeTTSEngine() {
        val useGradioTTS = appPrefs.useGradioTTS().get()
        val activeConfigId = appPrefs.activeGradioConfigId().get()
        
        val desiredEngine = if (useGradioTTS && activeConfigId.isNotEmpty()) {
            TTSEngineType.GRADIO
        } else {
            TTSEngineType.NATIVE
        }
        
        // If engine changed, cleanup old one and create new one
        if (currentEngineType != desiredEngine || ttsEngine == null) {
            ttsEngine?.cleanup()
            ttsEngine = null
            
            ttsEngine = when (desiredEngine) {
                TTSEngineType.GRADIO -> {
                    val config = GradioTTSPresets.getPresetById(activeConfigId)
                    if (config != null) {
                        TTSEngineFactory.createGradioEngine(config)
                    } else {
                        TTSEngineFactory.createNativeEngine()
                    }
                }
                TTSEngineType.NATIVE -> {
                    TTSEngineFactory.createNativeEngine()
                }
            }
            
            currentEngineType = desiredEngine
            
            // Set callback for TTS events
            ttsEngine?.setCallback(object : TTSEngineCallback {
                override fun onStart(utteranceId: String) {
                    state.setLoading(false) // Clear loading state when TTS starts
                    state.setPlaying(true)
                    // BRILLIANT SYNC: Embed actual speaking start timestamp in utteranceId
                    // Format: "originalUtteranceId_actualStartTimestamp"
                    // The UI extracts this timestamp for precise highlighter sync
                    val actualStartTime = System.currentTimeMillis()
                    val syncedUtteranceId = "${utteranceId}_$actualStartTime"
                    state.setUtteranceId(syncedUtteranceId)
                    state.setParagraphSpeakingStartTime(actualStartTime)
                    setPlaybackState(PlaybackStateCompat.STATE_PLAYING)
                    scope.launch { updateNotification() }
                }
                
                override fun onDone(utteranceId: String) {
                    handleParagraphComplete()
                }
                
                override fun onError(utteranceId: String, error: String) {
                    Log.error { "TTS error: $error" }
                    state.setLoading(false) // Clear loading state on error too
                    handleParagraphComplete()
                }
                
                override fun onReady() {
                    Log.info { "TTS engine is ready" }
                    state.setTTSReady(true)
                }
            })
            
            // Apply current settings
            ttsEngine?.setSpeed(readerPreferences.speechRate().get())
            ttsEngine?.setPitch(readerPreferences.speechPitch().get())
        }
    }
    
    /**
     * Read text using unified player
     */
    fun readText(context: Context, mediaSessionCompat: MediaSessionCompat) {
        setBundle()
        
        val chapter = state.ttsChapter.value
        val content = getCurrentContent()
        val book = state.ttsBook.value
        
        if (chapter == null || content == null || book == null) {
            Log.error { "TTS: Missing data - book=${book?.title}, chapter=${chapter?.name}, content=${content?.size}" }
            return
        }
        
        val currentParagraph = state.currentReadingParagraph.value
        
        if (currentParagraph >= content.size) {
            Log.error { "TTS: Paragraph index out of bounds: $currentParagraph >= ${content.size}" }
            return
        }
        
        // Track the paragraph when speech starts to detect manual navigation
        speechStartParagraph = currentParagraph
        
        runCatching {
            scope.launch { updateNotification() }
            
            // Initialize TTS engine if needed
            initializeTTSEngine()
            
            // Speak current paragraph
            val text = content[currentParagraph]
            val utteranceId = currentParagraph.toString()
            
            // Pre-cache next 3 paragraphs for Gradio TTS
            if (currentEngineType == TTSEngineType.GRADIO) {
                // Get the underlying Gradio player for caching
                val androidEngine = ttsEngine as? AndroidGradioTTSEngine
                androidEngine?.let { engine ->
                    val nextParagraphs = mutableListOf<Pair<String, String>>()
                    val loadingSet = mutableSetOf<Int>()
                    
                    for (i in 1..3) {
                        val nextIndex = state.currentReadingParagraph.value + i
                        if (nextIndex < content.size) {
                            nextParagraphs.add(nextIndex.toString() to content[nextIndex])
                            loadingSet.add(nextIndex)
                        }
                    }
                    
                    if (nextParagraphs.isNotEmpty()) {
                        state.loadingParagraphs = loadingSet
                        engine.precacheParagraphs(nextParagraphs)
                        
                        // Update cache status after a delay
                        scope.launch {
                            delay(500) // Give it time to start caching
                            val cached = mutableSetOf<Int>()
                            val loading = mutableSetOf<Int>()
                            
                            for (i in 1..3) {
                                val nextIndex = state.currentReadingParagraph.value + i
                                if (nextIndex < content.size) {
                                    when (engine.getCacheStatus(nextIndex.toString())) {
                                        GenericGradioTTSEngine.CacheStatus.CACHED -> cached.add(nextIndex)
                                        GenericGradioTTSEngine.CacheStatus.LOADING -> loading.add(nextIndex)
                                        else -> {}
                                    }
                                }
                            }
                            
                            state.cachedParagraphs = cached
                            state.loadingParagraphs = loading
                        }
                    }
                }
            }
            
            // Speak the text - NativeTTSPlayer handles queuing if not ready yet
            scope.launch {
                ttsEngine?.speak(text, utteranceId)
            }
            
        }.onFailure { e ->
            Log.error { "TTS: Error reading text - ${e.message}" }
        }
    }
    
    /**
     * Handle paragraph completion and auto-advance
     */
    private fun handleParagraphComplete() {
        checkSleepTime()
        
        val content = getCurrentContent() ?: return
        val currentParagraph = state.currentReadingParagraph.value
        
        // Check if paragraph was manually changed by user (next/prev button)
        // If so, don't auto-advance because the user already navigated
        if (speechStartParagraph != -1 && currentParagraph != speechStartParagraph) {
            Log.info { "TTS: Skipping auto-advance - paragraph changed from $speechStartParagraph to $currentParagraph" }
            return
        }
        
        val isFinished = currentParagraph >= content.lastIndex
        
        // Keep playing state true for auto-advance
        if (!isFinished && currentParagraph < content.size && state.isPlaying.value) {
            // Update previous paragraph before advancing to current
            state.setPreviousReadingParagraph(currentParagraph)
            state.setCurrentReadingParagraph(currentParagraph + 1)
            scope.launch { updateNotification() }
            readText(this, mediaSession)
            return
        }
        
        if (isFinished && state.isPlaying.value) {
            handleChapterFinished()
        }
    }
    
    /**
     * Handle chapter completion and auto-next chapter
     */
    private fun handleChapterFinished() {
        state.setPlaying(false)
        // Reset paragraph tracking
        state.setPreviousReadingParagraph(0)
        state.setCurrentReadingParagraph(0)
        speechStartParagraph = -1
        isPlaybackPaused = false
        ttsEngine?.stop()
        
        if (state.autoNextChapter.value) {
            scope.launch {
                val chapter = state.ttsChapter.value ?: return@launch
                val chapters = state.ttsChapters.value
                val source = state.ttsCatalog.value ?: return@launch
                val index = getChapterIndex(chapter, chapters)
                
                if (index != chapters.lastIndex) {
                    val nextChapterId = chapters[index + 1].id
                    getRemoteChapter(nextChapterId, source, state) {
                        // Reset paragraph tracking for new chapter
                        state.setPreviousReadingParagraph(0)
                        state.setCurrentReadingParagraph(0)
                        state.setPlaying(true)
                        updateNotification()
                        readText(this@TTSService, mediaSession)
                    }
                }
            }
        }
    }
    
    // ========== Playback Controls ==========
    
    private suspend fun handlePlay() {
        setPlaybackState(PlaybackStateCompat.STATE_PLAYING)
        hookNotification()
        state.setPlaying(true)
        
        // Check if we're resuming from pause or starting fresh
        if (isPlaybackPaused) {
            // Resume paused playback - just resume the engine, don't start new request
            isPlaybackPaused = false
            ttsEngine?.resume()
        } else {
            // Start fresh playback
            readText(this@TTSService, mediaSession)
        }
    }
    
    private suspend fun handlePause(dueToFocusLoss: Boolean = false) {
        setPlaybackState(PlaybackStateCompat.STATE_PAUSED)
        state.setPlaying(false)
        isPlaybackPaused = true
        
        // Pause the TTS engine
        ttsEngine?.pause()
        
        // Only allow auto-resume on focus gain if pause was due to focus loss
        // Don't auto-resume when user manually pauses
        if (!dueToFocusLoss) {
            resumeOnFocus = false
        }
        updateNotification()
    }
    
    private suspend fun handlePlayPause() {
        if (state.isPlaying.value) {
            handlePause()
        } else {
            handlePlay()
        }
    }
    
    private suspend fun handleTogglePlayPause() {
        if (state.isPlaying.value) {
            handlePause()
        } else {
            // Use handlePlay which handles pause/resume logic
            handlePlay()
        }
    }
    
    private suspend fun handleCancel() {
        // Stop playback
        ttsEngine?.stop()
        state.setUtteranceId("")
        state.setPlaying(false)
        speechStartParagraph = -1
        isPlaybackPaused = false
        
        abandonAudioFocus()
        unhookNotification()
        
        // Stop foreground and remove notification
        if (isNotificationForeground) {
            stopForeground(true) // true = remove notification
            isNotificationForeground = false
        }
        
        // Cancel notification explicitly
        NotificationManagerCompat.from(this).cancel(NotificationsIds.ID_TTS)
        
        resumeOnFocus = false
        notificationController.stop()
        isPlayerDispose = true
        setPlaybackState(PlaybackStateCompat.STATE_STOPPED)
        
        // Stop the service completely to clear from memory
        stopSelf()
    }
    
    private suspend fun handleSkipNext(chapter: Chapter, chapters: List<Chapter>, source: CatalogLocal) {
        val wasPlaying = state.isPlaying.value
        ttsEngine?.stop()
        state.setPlaying(false)
        speechStartParagraph = -1
        isPlaybackPaused = false
        
        val index = getChapterIndex(chapter, chapters)
        if (index != chapters.lastIndex) {
            val nextChapterId = chapters[index + 1].id
            getRemoteChapter(nextChapterId, source, state) {
                // Reset paragraph tracking for new chapter
                state.setPreviousReadingParagraph(0)
                state.setCurrentReadingParagraph(0)
                updateNotification()
                if (wasPlaying) {
                    state.setPlaying(true)
                    readText(this@TTSService, mediaSession)
                }
            }
        }
    }
    
    private suspend fun handleSkipPrevious(chapter: Chapter, chapters: List<Chapter>, source: CatalogLocal) {
        val wasPlaying = state.isPlaying.value
        ttsEngine?.stop()
        state.setPlaying(false)
        speechStartParagraph = -1
        isPlaybackPaused = false
        
        val index = getChapterIndex(chapter, chapters)
        if (index != 0) {
            val prevChapterId = chapters[index - 1].id
            getRemoteChapter(prevChapterId, source, state) {
                // Reset paragraph tracking for new chapter
                state.setPreviousReadingParagraph(0)
                state.setCurrentReadingParagraph(0)
                updateNotification()
                if (wasPlaying) {
                    state.setPlaying(true)
                    readText(this@TTSService, mediaSession)
                }
            }
        }
    }
    
    private suspend fun handleSkipNextParagraph() {
        val content = getCurrentContent() ?: return
        val currentParagraph = state.currentReadingParagraph.value
        if (currentParagraph < content.lastIndex) {
            val wasPlaying = state.isPlaying.value
            
            // Stop current playback first
            ttsEngine?.stop()
            isPlaybackPaused = false
            
            // Update previous paragraph before advancing
            state.setPreviousReadingParagraph(currentParagraph)
            state.setCurrentReadingParagraph(currentParagraph + 1)
            updateNotification()
            
            // Continue playing if was playing
            if (wasPlaying) {
                readText(this@TTSService, mediaSession)
            }
        }
    }
    
    private suspend fun handleSkipPreviousParagraph() {
        val currentParagraph = state.currentReadingParagraph.value
        if (currentParagraph > 0) {
            val wasPlaying = state.isPlaying.value
            
            // Stop current playback first
            ttsEngine?.stop()
            isPlaybackPaused = false
            
            // Update previous paragraph before going back
            state.setPreviousReadingParagraph(currentParagraph)
            state.setCurrentReadingParagraph(currentParagraph - 1)
            updateNotification()
            
            // Continue playing if was playing
            if (wasPlaying) {
                readText(this@TTSService, mediaSession)
            }
        }
    }
    
    private suspend fun handleJumpToParagraph(index: Int) {
        val content = getCurrentContent() ?: return
        if (index < 0 || index >= content.size) return
        
        val wasPlaying = state.isPlaying.value
        val currentParagraph = state.currentReadingParagraph.value
        
        // Stop current playback first
        ttsEngine?.stop()
        isPlaybackPaused = false
        
        // Update paragraph
        state.setPreviousReadingParagraph(currentParagraph)
        state.setCurrentReadingParagraph(index)
        updateNotification()
        
        // Continue playing if was playing
        if (wasPlaying) {
            readText(this@TTSService, mediaSession)
        }
    }
    
    // ========== Helper Methods ==========
    
    private fun readPrefs() {
        scope.launch {
            with(state) {
                setAutoNextChapter(readerPreferences.readerAutoNext().get())
                setCurrentLanguage(readerPreferences.speechLanguage().get())
                setCurrentVoice(textReaderPrefUseCase.readVoice())
                setSpeechSpeed(readerPreferences.speechRate().get())
                setPitch(readerPreferences.speechPitch().get())
                setSleepTime(readerPreferences.sleepTime().get())
                setSleepMode(readerPreferences.sleepMode().get())
            }
            
            observePreferenceChanges()
        }
    }
    
    private fun CoroutineScope.observePreferenceChanges() {
        launch { readerPreferences.readerAutoNext().changes().collect { state.setAutoNextChapter(it) } }
        launch { readerPreferences.speechLanguage().changes().collect { state.setCurrentLanguage(it) } }
        launch { appPrefs.speechVoice().changes().collect { state.setCurrentVoice(it) } }
        launch { readerPreferences.speechPitch().changes().collect { state.setPitch(it) } }
        launch { 
            readerPreferences.speechRate().changes().collect { 
                state.setSpeechSpeed(it)
                ttsEngine?.setSpeed(it)
            } 
        }
        launch { readerPreferences.sleepTime().changes().collect { state.setSleepTime(it) } }
        launch { readerPreferences.sleepMode().changes().collect { state.setSleepMode(it) } }
    }
    
    @OptIn(ExperimentalTime::class)
    fun checkSleepTime() {
        val lastCheckPref = state.startTime.value
        val currentSleepTime = state.sleepTime.value.minutes
        val now = kotlin.time.Clock.System.now()
        if (lastCheckPref != null && now - lastCheckPref > currentSleepTime && state.sleepMode.value) {
            startService(Player.CANCEL)
        }
    }
    
    private fun getCurrentContent(): List<String>? {
        // Check if we should use translated content
        val useTTSWithTranslatedText = readerPreferences.useTTSWithTranslatedText().get()
        val translatedContent = state.translatedTTSContent.value
        
        return if (useTTSWithTranslatedText && translatedContent != null && translatedContent.isNotEmpty()) {
            translatedContent
        } else {
            state.ttsContent.value
        }
    }
    
    private fun getChapterIndex(chapter: Chapter, chapters: List<Chapter>): Int {
        return chapters.indexOfFirst { it.id == chapter.id }
    }
    
    /**
     * Update reading history when chapter changes in TTS
     * This ensures the novel progress stays in sync with TTS navigation
     */
    private suspend fun updateReadingHistory(chapter: Chapter) {
        try {
            val currentTime = ireader.domain.utils.extensions.currentTimeToLong()
            val existingHistory = historyUseCase.findHistory(chapter.id)
            
            val history = ireader.domain.models.entities.History(
                id = existingHistory?.id ?: 0L,
                chapterId = chapter.id,
                readAt = currentTime,
                readDuration = existingHistory?.readDuration ?: 0L,
                progress = 0f // TTS starts from beginning of chapter
            )
            historyUseCase.insertHistory(history)
            Log.info { "TTS: Updated reading history for chapter ${chapter.name}" }
        } catch (e: Exception) {
            Log.error { "TTS: Failed to update reading history: ${e.message}" }
        }
    }
    
    // ========== Notification Management ==========
    
    private fun hookNotification() {
        if (!isNotificationForeground) {
            runCatching {
                scope.launch {
                    startForegroundService()
                    isNotificationForeground = true
                }
            }.onFailure { e ->
                Log.error { "Failed to start foreground service: ${e.message}" }
            }
        }
        
        if (!noisyReceiverHooked) {
            registerReceiver(noisyReceiver, IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY))
            noisyReceiverHooked = true
        }
        
        initAudioManager()
    }
    
    private suspend fun startForegroundService() {
        // Ensure metadata is set before building notification
        setBundle()
        
        // Show notification using abstraction
        val data = TTSNotificationData(
            title = state.ttsChapter.value?.name ?: "",
            subtitle = state.ttsBook.value?.title ?: "",
            coverUrl = state.ttsBook.value?.cover,
            isPlaying = state.isPlaying.value,
            isLoading = state.isLoading.value,
            currentParagraph = state.currentReadingParagraph.value,
            totalParagraphs = getCurrentContent()?.size ?: 0,
            bookId = state.ttsBook.value?.id ?: -1,
            chapterId = state.ttsChapter.value?.id ?: -1,
            sourceId = state.ttsBook.value?.sourceId ?: -1
        )
        ttsNotification.show(data)
        
        // Get the actual notification for foreground service
        // We need to build it directly for startForeground
        val notificationBuilder = TTSNotificationBuilder(this, localizeHelper)
        val notification = notificationBuilder.buildTTSNotification(mediaSession).build()
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NotificationsIds.ID_TTS,
                notification,
                android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
            )
        } else {
            startForeground(NotificationsIds.ID_TTS, notification)
        }
    }
    
    private fun unhookNotification() {
        stopForeground(true)
        runCatching { unregisterReceiver(noisyReceiver) }
        noisyReceiverHooked = false
    }
    
    private suspend fun updateNotification() {
        setBundle()
        
        // Ensure metadata is set before building notification
        if (state.meta == null) {
            val meta = MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, state.ttsChapter.value?.name ?: "")
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, state.ttsBook.value?.title ?: "")
                .putLong(IS_LOADING, if (state.isLoading.value) 1L else 0L)
                .build()
            state.meta = meta
            mediaSession.setMetadata(meta)
        }
        
        // Update notification using abstraction
        val data = TTSNotificationData(
            title = state.ttsChapter.value?.name ?: "",
            subtitle = state.ttsBook.value?.title ?: "",
            coverUrl = state.ttsBook.value?.cover,
            isPlaying = state.isPlaying.value,
            isLoading = state.isLoading.value,
            currentParagraph = state.currentReadingParagraph.value,
            totalParagraphs = getCurrentContent()?.size ?: 0,
            bookId = state.ttsBook.value?.id ?: -1,
            chapterId = state.ttsChapter.value?.id ?: -1,
            sourceId = state.ttsBook.value?.sourceId ?: -1
        )
        ttsNotification.show(data)
    }
    
    fun setBundle(
        book: Book? = state.ttsBook.value,
        chapter: Chapter? = state.ttsChapter.value
    ) {
        val bundle = Bundle()
        bundle.putLong(NOVEL_ID, book?.id ?: -1)
        bundle.putLong(SOURCE_ID, book?.sourceId ?: -1)
        bundle.putBoolean(FAVORITE, book?.favorite ?: false)
        bundle.putString(NOVEL_TITLE, book?.title ?: "")
        bundle.putString(NOVEL_COVER, book?.cover ?: "")
        bundle.putInt(PROGRESS, state.currentReadingParagraph.value)
        bundle.putInt(LAST_PARAGRAPH, getCurrentContent()?.size ?: 0)
        bundle.putString(CHAPTER_TITLE, chapter?.name ?: "")
        bundle.putLong(CHAPTER_ID, chapter?.id ?: -1)
        bundle.putBoolean(IS_LOADING, state.isLoading.value)
        bundle.putString(ERROR, "")
        
        controller?.sendCommand(UPDATE_PAGER, bundle, null)
        
        // Also update metadata for notification
        val content = getCurrentContent()
        val paragraphText = when {
            state.isLoading.value -> "Loading..."
            else -> "${state.currentReadingParagraph.value + 1}/${content?.size ?: 0}"
        }
        
        val meta = MediaMetadataCompat.Builder()
            // MediaStyle uses these for notification display
            .putString(MediaMetadataCompat.METADATA_KEY_TITLE, chapter?.name ?: "")
            .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, paragraphText)
            // Custom metadata for our notification builder
            .putString(NOVEL_COVER, book?.cover ?: "")
            .putString(NOVEL_TITLE, book?.title ?: "")
            .putLong(NOVEL_ID, book?.id ?: -1)
            .putLong(SOURCE_ID, book?.sourceId ?: -1)
            .putLong(CHAPTER_ID, chapter?.id ?: -1)
            .putString(CHAPTER_TITLE, chapter?.name ?: "")
            .putLong(PROGRESS, state.currentReadingParagraph.value.toLong())
            .putLong(LAST_PARAGRAPH, content?.size?.toLong() ?: 0L)
            .putLong(IS_LOADING, if (state.isLoading.value) 1L else 0L)
            .putLong(FAVORITE, if (book?.favorite == true) 1L else 0L)
            .build()
        state.meta = meta
        mediaSession.setMetadata(meta)
    }
    
    // ========== Audio Focus ==========
    
    private fun initAudioManager() {
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build()
            
            focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(audioAttributes)
                .setAcceptsDelayedFocusGain(true)
                .setOnAudioFocusChangeListener(this)
                .build()
            
            audioManager.requestAudioFocus(focusRequest!!)
        } else {
            @Suppress("DEPRECATION")
            audioManager.requestAudioFocus(
                this,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN
            )
        }
    }
    
    private fun abandonAudioFocus() {
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            focusRequest?.let { audioManager.abandonAudioFocusRequest(it) }
        } else {
            @Suppress("DEPRECATION")
            audioManager.abandonAudioFocus(this)
        }
    }
    
    override fun onAudioFocusChange(focusChange: Int) {
        when (focusChange) {
            AudioManager.AUDIOFOCUS_GAIN -> {
                // Only resume if:
                // 1. resumeOnFocus is true (was playing before losing focus)
                // 2. Not currently playing
                // 3. Notification is in foreground (service is actively being used)
                if (resumeOnFocus && !state.isPlaying.value && isNotificationForeground) {
                    scope.launch { handlePlay() }
                }
            }
            AudioManager.AUDIOFOCUS_LOSS, AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                if (state.isPlaying.value) {
                    // Set resumeOnFocus to true when pausing due to focus loss
                    // This way we'll resume when focus is regained
                    resumeOnFocus = true
                    scope.launch { handlePause(dueToFocusLoss = true) }
                }
            }
        }
    }
    
    // ========== Media Session Callback ==========
    
    inner class TTSSessionCallback : MediaSessionCompat.Callback() {
        override fun onPlay() {
            scope.launch { handlePlay() }
        }
        
        override fun onPause() {
            scope.launch { handlePause() }
        }
        
        override fun onStop() {
            scope.launch { handleCancel() }
        }
        
        // SKIP_TO_NEXT now does paragraph navigation (for devices that show skip icons)
        override fun onSkipToNext() {
            scope.launch { handleSkipNextParagraph() }
        }
        
        // SKIP_TO_PREVIOUS now does paragraph navigation (for devices that show skip icons)
        override fun onSkipToPrevious() {
            scope.launch { handleSkipPreviousParagraph() }
        }
        
        // FAST_FORWARD also does paragraph navigation (for devices that show these icons)
        override fun onFastForward() {
            scope.launch { handleSkipNextParagraph() }
        }
        
        // REWIND also does paragraph navigation (for devices that show these icons)
        override fun onRewind() {
            scope.launch { handleSkipPreviousParagraph() }
        }
    }
    
    // ========== Service Management ==========
    
    fun startService(command: Int) {
        hookNotification()
        
        scope.launch {
            when (command) {
                Player.PLAY -> handlePlay()
                Player.PAUSE -> handlePause()
                Player.PLAY_PAUSE -> handlePlayPause()
                else -> handleTogglePlayPause()
            }
        }
    }
    
    private fun setPlaybackState(state: Int) {
        stateBuilder.setState(state, PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1f)
        mediaSession.setPlaybackState(stateBuilder.build())
    }
    
    private fun setLoadingState(isLoading: Boolean) {
        // Update metadata to reflect loading state
        val meta = MediaMetadataCompat.Builder()
            .putLong(IS_LOADING, if (isLoading) 1L else 0L)
            .build()
        state.meta = meta
        scope.launch { updateNotification() }
    }
    
    // ========== Chapter Loading ==========
    
    suspend fun getRemoteChapter(
        chapterId: Long,
        source: CatalogLocal,
        ttsState: TTSState,
        onSuccess: suspend () -> Unit,
    ) {
        try {
            setLoadingState(true)
            state.setCurrentReadingParagraph(0)
            
            val chapter = chapterRepo.findChapterById(chapterId) ?: return
            
            if (chapter.content.isEmpty()) {
                val catalog = extensions.get(source.sourceId)
                if (catalog != null) {
                    remoteUseCases.getRemoteReadingContent(
                        chapter = chapter,
                        catalog = catalog,
                        onSuccess = { remoteChapter ->
                            scope.launch {
                                chapterRepo.insertChapter(remoteChapter)
                                ttsState.setTtsChapter(remoteChapter)
                                // Update reading history to sync TTS progress with novel progress
                                updateReadingHistory(remoteChapter)
                                setLoadingState(false)
                                onSuccess()
                            }
                        },
                        onError = { error ->
                            Log.error { "Failed to load chapter content: $error" }
                            setLoadingState(false)
                        }
                    )
                } else {
                    Log.error { "Catalog not found for source: ${source.sourceId}" }
                    setLoadingState(false)
                }
            } else {
                ttsState.setTtsChapter(chapter)
                // Update reading history to sync TTS progress with novel progress
                updateReadingHistory(chapter)
                setLoadingState(false)
                onSuccess()
            }
            
        } catch (e: Exception) {
            Log.error { "Error loading chapter: ${e.message}" }
            setLoadingState(false)
        }
    }
    
    // ========== Media Browser Service ==========
    
    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot {
        return BrowserRoot(TTS_SERVICE_NAME, null)
    }
    
    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {
        result.sendResult(mutableListOf())
    }
    
    // ========== Noisy Receiver ==========
    
    inner class NoisyReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == AudioManager.ACTION_AUDIO_BECOMING_NOISY) {
                scope.launch { handlePause() }
            }
        }
    }
    
    // ========== Notification Controller ==========
    
    inner class NotificationController {
        private var job: Job? = null
        
        fun start() {
            job = scope.launch {
                while (isActive) {
                    delay(1000)
                    if (state.isPlaying.value) {
                        updateNotification()
                    }
                }
            }
        }
        
        fun stop() {
            job?.cancel()
        }
    }
}
