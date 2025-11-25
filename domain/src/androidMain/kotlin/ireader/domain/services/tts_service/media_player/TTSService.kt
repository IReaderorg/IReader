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
 * Only ONE TTS engine runs at a time (Native OR Coqui)
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
    
    // Unified TTS Player - ONLY ONE engine at a time
    private var unifiedPlayer: UnifiedTTSPlayer? = null
    private var currentEngine: TTSEngine = TTSEngine.NATIVE
    
    // Service components
    lateinit var ttsNotificationBuilder: TTSNotificationBuilder
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
    
    enum class TTSEngine {
        NATIVE,
        COQUI
    }
    
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
        
        // Initialize notification builder
        ttsNotificationBuilder = TTSNotificationBuilder(this, localizeHelper)
        
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
                        if (bookId != -1L && state.ttsBook?.id != bookId) {
                            state.ttsBook = bookRepo.findBookById(bookId)
                        }
                        
                        // Only load chapter if provided and different from current
                        if (chapterId != -1L && state.ttsChapter?.id != chapterId) {
                            val chapter = chapterRepo.findChapterById(chapterId)
                            state.ttsChapter = chapter
                            
                            // Load chapters list
                            state.ttsBook?.let { book ->
                                state.ttsChapters = chapterRepo.findChaptersByBookId(book.id)
                                state.ttsCatalog = extensions.get(book.sourceId)
                            }
                        }
                        
                        // Execute command AFTER data is loaded
                        if (command != -1) {
                            startService(command)
                        }
                    }
                }
                ACTION_PLAY -> startService(Player.PLAY)
                ACTION_PAUSE -> startService(Player.PAUSE)
                ACTION_PLAY_PAUSE -> startService(Player.PLAY_PAUSE)
                ACTION_STOP, ACTION_CANCEL -> scope.launch { handleCancel() }
                ACTION_NEXT -> {
                    val chapter = state.ttsChapter
                    val chapters = state.ttsChapters
                    val source = state.ttsCatalog
                    if (chapter != null && source != null) {
                        scope.launch { handleSkipNext(chapter, chapters, source) }
                    }
                }
                ACTION_PREVIOUS -> {
                    val chapter = state.ttsChapter
                    val chapters = state.ttsChapters
                    val source = state.ttsCatalog
                    if (chapter != null && source != null) {
                        scope.launch { handleSkipPrevious(chapter, chapters, source) }
                    }
                }
            }
        }
        
        return START_STICKY
    }
    
    override fun onDestroy() {
        // Cancel notification when service is destroyed
        NotificationManagerCompat.from(this).cancel(NotificationsIds.ID_TTS)
        
        // Cleanup unified player
        unifiedPlayer?.cleanup()
        unifiedPlayer = null
        
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
     * Initialize the unified TTS player based on user preference
     * Only ONE engine is active at a time
     */
    private fun initializeUnifiedPlayer() {
        val useCoquiTTS = appPrefs.useCoquiTTS().get()
        val coquiSpaceUrl = appPrefs.coquiSpaceUrl().get()
        
        val desiredEngine = if (useCoquiTTS && coquiSpaceUrl.isNotEmpty()) {
            TTSEngine.COQUI
        } else {
            TTSEngine.NATIVE
        }
        
        // If engine changed, cleanup old one and create new one
        if (currentEngine != desiredEngine || unifiedPlayer == null) {
            unifiedPlayer?.cleanup()
            unifiedPlayer = null
            
            unifiedPlayer = when (desiredEngine) {
                TTSEngine.COQUI -> {
                    CoquiTTSPlayer(
                        context = this,
                        spaceUrl = coquiSpaceUrl,
                        apiKey = appPrefs.coquiApiKey().get().takeIf { it.isNotEmpty() }
                    )
                }
                TTSEngine.NATIVE -> {
                    NativeTTSPlayer(this)
                }
            }
            
            currentEngine = desiredEngine
            
            // Set callback for TTS events
            unifiedPlayer?.setCallback(object : TTSCallback {
                override fun onStart(utteranceId: String) {
                    Log.error { "TTS_CALLBACK: onStart - utteranceId=$utteranceId, engine=$currentEngine" }
                    state.isPlaying = true
                    state.utteranceId = utteranceId
                    setPlaybackState(PlaybackStateCompat.STATE_PLAYING)
                    scope.launch { updateNotification() }
                }
                
                override fun onDone(utteranceId: String) {
                    Log.error { "TTS_CALLBACK: onDone - utteranceId=$utteranceId, engine=$currentEngine" }
                    // Don't change isPlaying state here - let handleParagraphComplete manage it
                    handleParagraphComplete()
                }
                
                override fun onError(utteranceId: String, error: String) {
                    Log.error { "TTS_CALLBACK: onError - $error, engine=$currentEngine" }
                    handleParagraphComplete()
                }
            })
            
            // Apply current settings
            unifiedPlayer?.setSpeed(readerPreferences.speechRate().get())
            unifiedPlayer?.setPitch(readerPreferences.speechPitch().get())
        }
    }
    
    /**
     * Read text using unified player
     */
    fun readText(context: Context, mediaSessionCompat: MediaSessionCompat) {
        setBundle()
        
        val chapter = state.ttsChapter
        val content = getCurrentContent()
        val book = state.ttsBook
        
        if (chapter == null || content == null || book == null) {
            Log.error { "TTS: Missing data - book=${book?.title}, chapter=${chapter?.name}, content=${content?.size}" }
            return
        }
        
        if (state.currentReadingParagraph >= content.size) {
            Log.error { "TTS: Paragraph index out of bounds: ${state.currentReadingParagraph} >= ${content.size}" }
            return
        }
        
        runCatching {
            scope.launch { updateNotification() }
            
            // Initialize unified player if needed
            initializeUnifiedPlayer()
            
            // Speak current paragraph
            val text = content[state.currentReadingParagraph]
            val utteranceId = state.currentReadingParagraph.toString()
            
            // Pre-cache next 3 paragraphs for Coqui TTS
            if (currentEngine == TTSEngine.COQUI && unifiedPlayer is CoquiTTSPlayer) {
                val coquiPlayer = unifiedPlayer as CoquiTTSPlayer
                val nextParagraphs = mutableListOf<Pair<String, String>>()
                val loadingSet = mutableSetOf<Int>()
                
                for (i in 1..3) {
                    val nextIndex = state.currentReadingParagraph + i
                    if (nextIndex < content.size) {
                        nextParagraphs.add(nextIndex.toString() to content[nextIndex])
                        loadingSet.add(nextIndex)
                    }
                }
                
                if (nextParagraphs.isNotEmpty()) {
                    state.loadingParagraphs = loadingSet
                    coquiPlayer.precacheParagraphs(nextParagraphs)
                    
                    // Update cache status after a delay
                    scope.launch {
                        delay(500) // Give it time to start caching
                        val cached = mutableSetOf<Int>()
                        val loading = mutableSetOf<Int>()
                        
                        for (i in 1..3) {
                            val nextIndex = state.currentReadingParagraph + i
                            if (nextIndex < content.size) {
                                when (coquiPlayer.getCacheStatus(nextIndex.toString())) {
                                    CoquiTTSPlayer.CacheStatus.CACHED -> cached.add(nextIndex)
                                    CoquiTTSPlayer.CacheStatus.LOADING -> loading.add(nextIndex)
                                    else -> {}
                                }
                            }
                        }
                        
                        state.cachedParagraphs = cached
                        state.loadingParagraphs = loading
                    }
                }
            }
            
            // For Native TTS, retry a few times if not ready yet
            if (currentEngine == TTSEngine.NATIVE && !unifiedPlayer!!.isReady()) {
                scope.launch {
                    repeat(30) { attempt ->
                        delay(100)
                        if (unifiedPlayer?.isReady() == true) {
                            unifiedPlayer?.speak(text, utteranceId)
                            return@launch
                        }
                    }
                    Log.error { "TTS: Native TTS failed to initialize" }
                }
            } else {
                unifiedPlayer?.speak(text, utteranceId)
            }
            
        }.onFailure { e ->
            Log.error { "TTS: Error reading text - ${e.message}" }
        }
    }
    
    /**
     * Handle paragraph completion and auto-advance
     */
    private fun handleParagraphComplete() {
        Log.error { "TTS_COMPLETE: Paragraph ${state.currentReadingParagraph} complete, isPlaying=${state.isPlaying}" }
        checkSleepTime()
        
        val content = getCurrentContent() ?: return
        val isFinished = state.currentReadingParagraph >= content.lastIndex
        
        Log.error { "TTS_COMPLETE: isFinished=$isFinished, current=${state.currentReadingParagraph}, lastIndex=${content.lastIndex}, size=${content.size}" }
        
        // Keep playing state true for auto-advance
        if (!isFinished && state.currentReadingParagraph < content.size && state.isPlaying) {
            state.currentReadingParagraph += 1
            Log.error { "TTS_COMPLETE: Advancing to paragraph ${state.currentReadingParagraph}" }
            readText(this, mediaSession)
            return
        }
        
        if (isFinished && state.isPlaying) {
            Log.error { "TTS_COMPLETE: Chapter finished, handling auto-next" }
            handleChapterFinished()
        }
    }
    
    /**
     * Handle chapter completion and auto-next chapter
     */
    private fun handleChapterFinished() {
        state.isPlaying = false
        state.currentReadingParagraph = 0
        unifiedPlayer?.stop()
        
        if (state.autoNextChapter) {
            scope.launch {
                val chapter = state.ttsChapter ?: return@launch
                val chapters = state.ttsChapters
                val source = state.ttsCatalog ?: return@launch
                val index = getChapterIndex(chapter, chapters)
                
                if (index != chapters.lastIndex) {
                    val nextChapterId = chapters[index + 1].id
                    getRemoteChapter(nextChapterId, source, state) {
                        state.currentReadingParagraph = 0
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
        state.isPlaying = true
        readText(this@TTSService, mediaSession)
    }
    
    private suspend fun handlePause() {
        setPlaybackState(PlaybackStateCompat.STATE_PAUSED)
        state.isPlaying = false
        unifiedPlayer?.pause()
        updateNotification()
    }
    
    private suspend fun handlePlayPause() {
        if (state.isPlaying) {
            handlePause()
        } else {
            handlePlay()
        }
    }
    
    private suspend fun handleTogglePlayPause() {
        if (state.isPlaying) {
            handlePause()
        } else {
            state.isPlaying = true
            readText(this@TTSService, mediaSession)
        }
    }
    
    private suspend fun handleCancel() {
        // Stop playback
        unifiedPlayer?.stop()
        state.utteranceId = ""
        state.isPlaying = false
        
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
        unifiedPlayer?.stop()
        state.isPlaying = false
        
        val index = getChapterIndex(chapter, chapters)
        if (index != chapters.lastIndex) {
            val nextChapterId = chapters[index + 1].id
            getRemoteChapter(nextChapterId, source, state) {
                if (state.isPlaying) {
                    readText(this@TTSService, mediaSession)
                }
            }
        }
    }
    
    private suspend fun handleSkipPrevious(chapter: Chapter, chapters: List<Chapter>, source: CatalogLocal) {
        unifiedPlayer?.stop()
        state.isPlaying = false
        
        val index = getChapterIndex(chapter, chapters)
        if (index != 0) {
            val prevChapterId = chapters[index - 1].id
            getRemoteChapter(prevChapterId, source, state) {
                updateNotification()
                if (state.isPlaying) {
                    readText(this@TTSService, mediaSession)
                }
            }
        }
    }
    
    private suspend fun handleSkipNextParagraph() {
        val content = getCurrentContent() ?: return
        if (state.currentReadingParagraph < content.lastIndex) {
            unifiedPlayer?.stop()
            state.currentReadingParagraph += 1
            if (state.isPlaying) {
                readText(this@TTSService, mediaSession)
                updateNotification()
            }
        }
    }
    
    private suspend fun handleSkipPreviousParagraph() {
        if (state.currentReadingParagraph > 0) {
            unifiedPlayer?.stop()
            state.currentReadingParagraph -= 1
            if (state.isPlaying) {
                readText(this@TTSService, mediaSession)
                updateNotification()
            }
        }
    }
    
    // ========== Helper Methods ==========
    
    private fun readPrefs() {
        scope.launch {
            with(state) {
                autoNextChapter = readerPreferences.readerAutoNext().get()
                currentLanguage = readerPreferences.speechLanguage().get()
                currentVoice = textReaderPrefUseCase.readVoice()
                speechSpeed = readerPreferences.speechRate().get()
                pitch = readerPreferences.speechPitch().get()
                sleepTime = readerPreferences.sleepTime().get()
                sleepMode = readerPreferences.sleepMode().get()
            }
            
            observePreferenceChanges()
        }
    }
    
    private fun CoroutineScope.observePreferenceChanges() {
        launch { readerPreferences.readerAutoNext().changes().collect { state.autoNextChapter = it } }
        launch { readerPreferences.speechLanguage().changes().collect { state.currentLanguage = it } }
        launch { appPrefs.speechVoice().changes().collect { state.currentVoice = it } }
        launch { readerPreferences.speechPitch().changes().collect { state.pitch = it } }
        launch { 
            readerPreferences.speechRate().changes().collect { 
                state.speechSpeed = it
                unifiedPlayer?.setSpeed(it)
            } 
        }
        launch { readerPreferences.sleepTime().changes().collect { state.sleepTime = it } }
        launch { readerPreferences.sleepMode().changes().collect { state.sleepMode = it } }
    }
    
    @OptIn(ExperimentalTime::class)
    fun checkSleepTime() {
        val lastCheckPref = state.startTime
        val currentSleepTime = state.sleepTime.minutes
        val now = kotlin.time.Clock.System.now()
        if (lastCheckPref != null && now - lastCheckPref > currentSleepTime && state.sleepMode) {
            startService(Player.CANCEL)
        }
    }
    
    private fun getCurrentContent(): List<String>? {
        return state.ttsContent?.value
    }
    
    private fun getChapterIndex(chapter: Chapter, chapters: List<Chapter>): Int {
        return chapters.indexOfFirst { it.id == chapter.id }
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
        val notification = ttsNotificationBuilder.buildNotification(mediaSession.sessionToken)
        
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
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, state.ttsChapter?.name ?: "")
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, state.ttsBook?.title ?: "")
                .putLong(IS_LOADING, if (state.isLoading.value) 1L else 0L)
                .build()
            state.meta = meta
            mediaSession.setMetadata(meta)
        }
        
        val notification = ttsNotificationBuilder.buildTTSNotification(mediaSession).build()
        NotificationManagerCompat.from(this).notify(NotificationsIds.ID_TTS, notification)
    }
    
    fun setBundle(
        book: Book? = state.ttsBook,
        chapter: Chapter? = state.ttsChapter
    ) {
        val bundle = Bundle()
        bundle.putLong(NOVEL_ID, book?.id ?: -1)
        bundle.putLong(SOURCE_ID, book?.sourceId ?: -1)
        bundle.putBoolean(FAVORITE, book?.favorite ?: false)
        bundle.putString(NOVEL_TITLE, book?.title ?: "")
        bundle.putString(NOVEL_COVER, book?.cover ?: "")
        bundle.putInt(PROGRESS, state.currentReadingParagraph)
        bundle.putInt(LAST_PARAGRAPH, getCurrentContent()?.size ?: 0)
        bundle.putString(CHAPTER_TITLE, chapter?.name ?: "")
        bundle.putLong(CHAPTER_ID, chapter?.id ?: -1)
        bundle.putBoolean(IS_LOADING, state.isLoading.value)
        bundle.putString(ERROR, "")
        
        controller?.sendCommand(UPDATE_PAGER, bundle, null)
        
        // Also update metadata for notification
        val meta = MediaMetadataCompat.Builder()
            .putString(MediaMetadataCompat.METADATA_KEY_TITLE, chapter?.name ?: "")
            .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, book?.title ?: "")
            .putString(NOVEL_COVER, book?.cover ?: "")
            .putString(NOVEL_TITLE, book?.title ?: "")
            .putLong(NOVEL_ID, book?.id ?: -1)
            .putLong(SOURCE_ID, book?.sourceId ?: -1)
            .putLong(CHAPTER_ID, chapter?.id ?: -1)
            .putString(CHAPTER_TITLE, chapter?.name ?: "")
            .putLong(PROGRESS, state.currentReadingParagraph.toLong())
            .putLong(LAST_PARAGRAPH, getCurrentContent()?.size?.toLong() ?: 0L)
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
                if (resumeOnFocus && !state.isPlaying) {
                    scope.launch { handlePlay() }
                }
            }
            AudioManager.AUDIOFOCUS_LOSS, AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                if (state.isPlaying) {
                    scope.launch { handlePause() }
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
        
        override fun onSkipToNext() {
            val chapter = state.ttsChapter ?: return
            val chapters = state.ttsChapters
            val source = state.ttsCatalog ?: return
            scope.launch { handleSkipNext(chapter, chapters, source) }
        }
        
        override fun onSkipToPrevious() {
            val chapter = state.ttsChapter ?: return
            val chapters = state.ttsChapters
            val source = state.ttsCatalog ?: return
            scope.launch { handleSkipPrevious(chapter, chapters, source) }
        }
        
        override fun onFastForward() {
            scope.launch { handleSkipNextParagraph() }
        }
        
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
            state.currentReadingParagraph = 0
            
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
                                ttsState.ttsChapter = remoteChapter
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
                ttsState.ttsChapter = chapter
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
                    if (state.isPlaying) {
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
