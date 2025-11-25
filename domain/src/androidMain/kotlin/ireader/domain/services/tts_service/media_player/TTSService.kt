package ireader.domain.services.tts_service.media_player

import android.app.PendingIntent
import android.content.*
import android.media.*
import android.os.Build
import android.os.Bundle
import android.os.ResultReceiver
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.NotificationManagerCompat
import androidx.media.MediaBrowserServiceCompat
import androidx.media.session.MediaButtonReceiver
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
import ireader.domain.services.tts_service.Player
import ireader.domain.services.tts_service.TTSState
import ireader.domain.services.tts_service.TTSStateImpl
import ireader.domain.services.tts_service.isSame
import ireader.domain.usecases.local.LocalGetChapterUseCase
import ireader.domain.usecases.preferences.TextReaderPrefUseCase
import ireader.domain.usecases.remote.RemoteUseCases
import ireader.i18n.LocalizeHelper
import ireader.i18n.R
import kotlinx.coroutines.*
import  kotlin.time.Clock
import org.koin.android.ext.android.inject
import kotlin.time.Duration.Companion.minutes
import kotlin.time.ExperimentalTime

class TTSService(
) : MediaBrowserServiceCompat(), AudioManager.OnAudioFocusChangeListener {
    private val bookRepo: BookRepository by inject()


    private val chapterRepo: ChapterRepository by inject()


    private val chapterUseCase: LocalGetChapterUseCase by inject()


    private val remoteUseCases: RemoteUseCases by inject()


    private val extensions: CatalogStore by inject()

    private val textReaderPrefUseCase: TextReaderPrefUseCase by inject()

    private val readerPreferences: ReaderPreferences by inject()
    private val appPrefs: AppPreferences by inject()
    private val localizeHelper: LocalizeHelper by inject()
    private val getTranslatedChapterUseCase: ireader.domain.usecases.translation.GetTranslatedChapterUseCase by inject()
    
    // Unified TTS Core - Production-ready TTS management
    private lateinit var ttsCore: ireader.domain.usecases.tts.AndroidTTSCore
    
    // Legacy support - maintained for backward compatibility
    private val aiTTSPlayer: ireader.domain.services.tts_service.media_player.AITTSPlayer by inject()
    private val aiTTSManager: ireader.domain.services.tts.AITTSManager by inject()
    private var player: TextToSpeech? = null
    
    lateinit var ttsNotificationBuilder: TTSNotificationBuilder
    lateinit var state: TTSStateImpl
    private val noisyReceiver = NoisyReceiver()
    private var noisyReceiverHooked = false
    private val focusLock = Any()
    private var resumeOnFocus = true

    lateinit var mediaSession: MediaSessionCompat
    lateinit var stateBuilder: PlaybackStateCompat.Builder
    private val metadata = MediaMetadataCompat.Builder()

    private lateinit var mediaCallback: TTSSessionCallback
    private lateinit var notificationController: NotificationController
    private var controller: MediaControllerCompat? = null

    private var serviceJob: Job? = null
    private var isPlayerDispose = false
    private var isHooked = false
    private var focusRequest: AudioFocusRequest? = null
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

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

    private fun readPrefs() {
        scope.launch {
            // Load initial preferences
            with(state) {
                autoNextChapter = readerPreferences.readerAutoNext().get()
                currentLanguage = readerPreferences.speechLanguage().get()
                currentVoice = textReaderPrefUseCase.readVoice()
                speechSpeed = readerPreferences.speechRate().get()
                pitch = readerPreferences.speechPitch().get()
                sleepTime = readerPreferences.sleepTime().get()
                sleepMode = readerPreferences.sleepMode().get()
            }
            
            // Observe preference changes
            observePreferenceChanges()
        }
    }

    private fun CoroutineScope.observePreferenceChanges() {
        launch { readerPreferences.readerAutoNext().changes().collect { state.autoNextChapter = it } }
        launch { readerPreferences.speechLanguage().changes().collect { state.currentLanguage = it } }
        launch { appPrefs.speechVoice().changes().collect { state.currentVoice = it } }
        launch { readerPreferences.speechPitch().changes().collect { state.pitch = it } }
        launch { readerPreferences.speechRate().changes().collect { state.speechSpeed = it } }
        launch { readerPreferences.sleepTime().changes().collect { state.sleepTime = it } }
        launch { readerPreferences.sleepMode().changes().collect { state.sleepMode = it } }
    }

    var silence: MediaPlayer? = null
    override fun onCreate() {
        super.onCreate()
        state = TTSStateImpl()

        val pendingIntentFlags: Int =
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        val mediaButtonIntent = Intent(Intent.ACTION_MEDIA_BUTTON)
        val mbrIntent = PendingIntent.getBroadcast(this, 0, mediaButtonIntent, pendingIntentFlags)
        val mbrComponent = ComponentName(this, MediaButtonReceiver::class.java)

        /**
         * Initializing the MediaSession
         */
        mediaSession = MediaSessionCompat(this, TTS_SERVICE_NAME, mbrComponent, mbrIntent)
        /**
         * setting a session token
         */
        mediaSession.apply {
            setSessionToken(sessionToken)
        }
        /**
         * setting the possible actions for mediabuttons
         */
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
        /**
         * setting a callbacks
         */
        mediaCallback = TTSSessionCallback()
        mediaSession.setCallback(mediaCallback)
        /**
         * setting MediaButtonReceiver
         */
        val mediaReceiverPendingIntent =
            PendingIntent.getService(
                this@TTSService,
                0,
                Intent(this@TTSService, TTSService::class.java).apply {
                    action = ACTION_PLAY_PAUSE
                },
                pendingIntentFlags
            )
        mediaSession.setMediaButtonReceiver(mediaReceiverPendingIntent)
        /**
         *  Media session need to be active before using media buttons
         *  https://stackoverflow.com/questions/38247050/mediabuttonreceiver-not-working-with-mediabrowserservicecompat
         */
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
        mediaButtonIntent.setClass(this, MediaButtonReceiver::class.java)

        // Initialize old notification builder (for backward compatibility)
        ttsNotificationBuilder = TTSNotificationBuilder(this, localizeHelper)
        
        // Initialize unified TTS Core
        ttsCore = ireader.domain.usecases.tts.AndroidTTSCore(
            context = this,
            mediaSession = mediaSession,
            appPreferences = appPrefs,
            readerPreferences = readerPreferences
        )
        
        // Initialize TTS Core asynchronously
        serviceScope.launch {
            ttsCore.initialize().onSuccess {
                Log.info { "TTS Core initialized: ${ttsCore.getCurrentProviderName()}" }
            }.onFailure { error ->
                Log.error { "Failed to initialize TTS Core: ${error.message}" }
            }
        }
        
        controller = MediaControllerCompat(this, mediaSession.sessionToken)

        /**
         * Initializing the player
         */
        initPlayer()

        notificationController = NotificationController()
        notificationController.start()
        silence = MediaPlayer.create(this, R.raw.silence).apply {
            isLooping = true
        }

        silence?.start()
    }

    var isNotificationForeground = false

    private fun hookNotification() {
        if (!isNotificationForeground) {
            runCatching {
                scope.launch {
                    startForegroundService()
                    isNotificationForeground = true
                }
            }
        }
        
        if (isPlayerDispose) {
            initPlayer()
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

    private fun initAudioManager() {
        val am = baseContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val result = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN).run {
                setOnAudioFocusChangeListener(this@TTSService)
                setAudioAttributes(
                    AudioAttributes.Builder().run {
                        setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        setUsage(AudioAttributes.USAGE_MEDIA)
                        build()
                    }
                )
                build()
            }
            focusRequest?.let { focusRequest ->
                am.requestAudioFocus(focusRequest)
            }
        } else {
            @Suppress("DEPRECATION")
            am.requestAudioFocus(
                this@TTSService,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN
            )
        }
    }

    private fun unhookNotification() {
        stopForeground(true)
        runCatching { unregisterReceiver(noisyReceiver) }
        isNotificationForeground = false
        noisyReceiverHooked = false
    }

    private fun initPlayer() {
        player = TextToSpeech(this) { status ->
            when (status) {
                TextToSpeech.ERROR -> {
                    Log.error { "Text-to-Speech Not Available" }
                    setBundle(isLoading = false)
                }
                TextToSpeech.SUCCESS -> {
                    runCatching {
                        state.voices = player?.voices?.toList() ?: emptyList()
                        state.languages = player?.availableLanguages?.toList() ?: emptyList()
                    }
                    readPrefs()
                }
            }
        }
        isPlayerDispose = false
    }

    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?,
    ): BrowserRoot {
        return BrowserRoot("NONE", null)
    }

    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>,
    ) {

        result.sendResult(mutableListOf())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Start foreground immediately to comply with Android 12+ requirements
        if (!isNotificationForeground) {
            serviceScope.launch {
                startForegroundService()
                isNotificationForeground = true
            }
        }

        when (intent?.action) {
            Intent.ACTION_MEDIA_BUTTON -> MediaButtonReceiver.handleIntent(mediaSession, intent)
            ACTION_UPDATE -> {
                val chapterId = intent.getLongExtra(TTS_Chapter_ID, -1)
                val bookId = intent.getLongExtra(TTS_BOOK_ID, -1)
                val command = intent.getIntExtra(COMMAND, -1)
                serviceScope.launch {
                    if (chapterId != -1L && bookId != -1L) {
                        val book = bookRepo.findBookById(bookId)
                        val chapter = chapterRepo.findChapterById(chapterId)
                        val chapters = chapterRepo.findChaptersByBookId(bookId)
                        val source = book?.sourceId?.let { extensions.get(it) }
                        if (chapter != null && source != null) {
                            state.ttsBook = book
                            state.ttsChapter = chapter
                            state.ttsChapters = chapters
                            state.ttsCatalog = source
                            state.currentReadingParagraph = 0
                            
                            // Load translated content for TTS if enabled
                            loadTranslatedContentForTTS(chapterId)
                            
                            setBundle(book, chapter)
                            startService(command)
                        }
                    }
                }
            }
            ACTION_CANCEL -> {
                startService(Player.CANCEL)
            }
            null -> {}
            else -> Log.error { "Unknown Intent $intent" }
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        // Shutdown unified TTS Core
        ttsCore.shutdown()
        
        // Cleanup old players
        player?.shutdown()
        
        notificationController.stop()
        unhookNotification()
        
        mediaSession.isActive = false
        mediaSession.release()
        isPlayerDispose = true

        super.onDestroy()
    }

    private fun setBundle(
        book: Book? = state.ttsBook,
        chapter: Chapter? = state.ttsChapter,
        isLoading: Boolean = false,
        error: Boolean = false
    ) {
        val data = metadata.apply {
            val lastTrackNumber = state.ttsContent?.value?.lastIndex?.toLong()
            
            book?.let {
                putText(NOVEL_TITLE, it.title)
                putLong(NOVEL_ID, it.id)
                putLong(FAVORITE, if (it.favorite) 1 else 0)
                putLong(SOURCE_ID, it.sourceId)
                putText(NOVEL_COVER, it.cover)
                putText(MediaMetadata.METADATA_KEY_AUTHOR, it.author)
            }
            
            chapter?.let {
                putText(CHAPTER_TITLE, it.name)
                putLong(CHAPTER_ID, it.id)
                putText(MediaMetadata.METADATA_KEY_DISPLAY_TITLE, it.name)
            }
            
            putLong(IS_LOADING, if (isLoading) 1L else 0L)
            putLong(ERROR, if (error) 1L else 0L)
            putLong(PROGRESS, state.currentReadingParagraph.toLong())
            putLong(LAST_PARAGRAPH, lastTrackNumber ?: 1L)
        }.build()
        
        mediaSession.setMetadata(data)
    }

    private inner class TTSSessionCallback : MediaSessionCompat.Callback() {

        @OptIn(ExperimentalTime::class)
        override fun onPlay() {
            if (isPlayerDispose) initPlayer()
            state.startTime = Clock.System.now()
            startService(Player.PLAY)
        }

        override fun onPause() {
            player?.stop()
            startService(Player.PAUSE)
        }

        override fun onStop() {
            player?.stop()
            startService(Player.PAUSE)
        }

        override fun onRewind() = startService(Player.PREV_PAR)

        override fun onFastForward() = startService(Player.NEXT_PAR)

        override fun onSkipToNext() = startService(Player.SKIP_NEXT)

        override fun onSkipToPrevious() = startService(Player.SKIP_PREV)

        override fun onSeekTo(pos: Long) {
            state.currentReadingParagraph = pos.toInt()
            setBundle()
            if (state.isPlaying) {
                startService(Player.PLAY)
            }
        }
    }

    private inner class NotificationController : MediaControllerCompat.Callback() {

        private val controller = MediaControllerCompat(this@TTSService, mediaSession.sessionToken)

        fun start() {
            controller.registerCallback(this)
        }

        fun stop() {
            controller.unregisterCallback(this)
        }

        @android.annotation.SuppressLint("MissingPermission")
        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            if (state != null && mediaSession.controller.metadata != null) {
                scope.launch {
                    // Update using new notification manager
                    updateNotificationState()
                    
                    // Keep old notification for backward compatibility
                    NotificationManagerCompat.from(applicationContext).notify(
                        NotificationsIds.ID_TTS,
                        ttsNotificationBuilder.buildTTSNotification(mediaSession).build()
                    )
                }
            }
        }
    }

    private fun setPlaybackState(state: Int) {
        mediaSession.setPlaybackState(
            stateBuilder.setState(
                state,
                this.state.currentReadingParagraph.toLong() * 1000L,
                0.0f
            ).build()
        )
    }

    @android.annotation.SuppressLint("MissingPermission")
    private suspend fun updateNotification() {
        // Update using new notification manager
        updateNotificationState()
        
        // Keep old notification for backward compatibility
        val notification = ttsNotificationBuilder.buildTTSNotification(mediaSession).build()
        NotificationManagerCompat.from(this).notify(NotificationsIds.ID_TTS, notification)
    }
    
    /**
     * Update notification using TTS Core (handled automatically)
     */
    private fun updateNotificationState() {
        // Notification updates are now handled automatically by AndroidTTSCore
        // This method kept for backward compatibility
    }

    fun startService(command: Int) {
        if (isPlayerDispose) initPlayer()
        hookNotification()
        
        serviceJob = scope.launch {
            runCatching {
                executeCommand(command)
            }.onFailure { e ->
                Log.error { "Service error: ${e.message}" }
                setBundle()
                updateNotification()
            }
        }
    }

    private suspend fun executeCommand(command: Int) {
        val chapter = state.ttsChapter ?: return
        val source = state.ttsCatalog ?: return
        val chapters = state.ttsChapters
        val book = state.ttsBook ?: return
        
        setBundle(book, chapter)
        hookNotification()
        
        when (command) {
            Player.CANCEL -> handleCancel()
            Player.SKIP_PREV -> handleSkipPrevious(chapter, chapters, source)
            Player.PREV_PAR -> handlePreviousParagraph()
            Player.SKIP_NEXT -> handleSkipNext(chapter, chapters, source)
            Player.NEXT_PAR -> handleNextParagraph()
            Player.PLAY -> handlePlay()
            Player.PAUSE -> handlePause()
            Player.PLAY_PAUSE -> handlePlayPause()
            else -> handleTogglePlayPause()
        }
    }

    private suspend fun handleCancel() {
        NotificationManagerCompat.from(this).cancel(NotificationsIds.ID_TTS)
        
        // Use unified TTS Core
        ttsCore.stop()
        state.utteranceId = ""
        state.isPlaying = false
        
        abandonAudioFocus()
        unhookNotification()
        
        if (isNotificationForeground) {
            stopForeground(true)
            isNotificationForeground = false
        }
        
        resumeOnFocus = false
        notificationController.stop()
        isPlayerDispose = true
        setPlaybackState(PlaybackStateCompat.STATE_NONE)
        stopSelf()
    }

    private fun abandonAudioFocus() {
        val am = baseContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            focusRequest?.let { am.abandonAudioFocusRequest(it) }
        } else {
            @Suppress("DEPRECATION")
            am.abandonAudioFocus(this)
        }
    }

    private suspend fun handleSkipPrevious(chapter: Chapter, chapters: List<Chapter>, source: CatalogLocal) {
        // Use unified TTS Core
        ttsCore.stop()
        state.isPlaying = false
        
        updateNotification()
        
        val index = getChapterIndex(chapter, chapters)
        if (index > 0) {
            val id = chapters[index - 1].id
            getRemoteChapter(id, source, state) {
                if (state.isPlaying) {
                    readText(this@TTSService, mediaSession)
                }
            }
        }
        
        state.currentReadingParagraph = 0
        updateNotification()
    }

    private suspend fun handlePreviousParagraph() {
        val content = getCurrentContent()
        content?.let { paragraphs ->
            if (state.currentReadingParagraph > 0 && state.currentReadingParagraph <= paragraphs.lastIndex) {
                state.currentReadingParagraph -= 1
                updateNotification()
                if (state.isPlaying) {
                    readText(this@TTSService, mediaSession)
                }
            }
        }
    }

    private suspend fun handleSkipNext(chapter: Chapter, chapters: List<Chapter>, source: CatalogLocal) {
        player?.stop()
        aiTTSPlayer.stop()
        updateNotification()
        
        val index = getChapterIndex(chapter, chapters)
        if (index != chapters.lastIndex) {
            val id = chapters[index + 1].id
            getRemoteChapter(id, source, state) {
                if (state.isPlaying) {
                    readText(this@TTSService, mediaSession)
                }
            }
        }
        
        state.currentReadingParagraph = 0
        updateNotification()
    }

    private suspend fun handleNextParagraph() {
        val content = getCurrentContent()
        content?.let { paragraphs ->
            if (state.currentReadingParagraph in 0 until paragraphs.lastIndex) {
                state.currentReadingParagraph += 1
                if (state.isPlaying) {
                    readText(this@TTSService, mediaSession)
                    updateNotification()
                }
            }
        }
    }

    private suspend fun handlePlay() {
        setPlaybackState(PlaybackStateCompat.STATE_PLAYING)
        hookNotification()
        state.isPlaying = true
        
        val book = state.ttsBook
        val chapter = state.ttsChapter
        val content = getCurrentContent()
        
        // Use ttsCore for reading
        if (book != null && chapter != null && content != null) {
            Log.info { "Using ttsCore for playback" }
            ttsCore.startReading(
                book = book,
                chapter = chapter,
                paragraphs = content,
                startIndex = state.currentReadingParagraph,
                onParagraphComplete = { index ->
                    state.currentReadingParagraph = index
                    state.utteranceId = index.toString()
                    serviceScope.launch {
                        setBundle()
                        updateNotification()
                    }
                },
                onChapterComplete = {
                    serviceScope.launch {
                        if (readerPreferences.readerAutoNext().get()) {
                            // Auto-load next chapter
                            mediaCallback.onSkipToNext()
                        } else {
                            handlePause()
                        }
                    }
                }
            )
        } else {
            // Fallback to old system
            Log.warn { "Missing book/chapter/content, using legacy readText" }
            readText(this@TTSService, mediaSession)
        }
    }

    private suspend fun handlePause() {
        setPlaybackState(PlaybackStateCompat.STATE_PAUSED)
        state.isPlaying = false
        
        // Use unified TTS Core
        ttsCore.pause()
        
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
            state.isPlaying = false
            player?.stop()
            aiTTSPlayer.stop()
            updateNotification()
        } else {
            state.isPlaying = true
            readText(this@TTSService, mediaSession)
        }
    }

    private fun getCurrentContent(): List<String>? {
        return if (state.translatedTTSContent?.isNotEmpty() == true) {
            state.translatedTTSContent
        } else {
            state.ttsContent?.value
        }
    }

    private val ttsJob = Job()
    val scope = CoroutineScope(Dispatchers.Main.immediate + ttsJob)
    
    fun readText(context: Context, mediaSessionCompat: MediaSessionCompat) {
        setBundle()
        
        val chapter = state.ttsChapter ?: return
        val content = getCurrentContent() ?: return
        val book = state.ttsBook ?: return
        
        runCatching {
            scope.launch { updateNotification() }
            
            // Check if Coqui TTS is enabled
            val useCoquiTTS = appPrefs.useCoquiTTS().get()
            val coquiSpaceUrl = appPrefs.coquiSpaceUrl().get()
            
            if (useCoquiTTS && coquiSpaceUrl.isNotEmpty()) {
                Log.info { "Using Coqui TTS" }
                readTextWithCoquiTTS(context, mediaSessionCompat, chapter, content)
            } else {
                // Use native Android TTS
                readTextWithNativeTTS(context, mediaSessionCompat, chapter, content)
            }
        }.onFailure { e ->
            Log.error { "Error reading text: ${e.message}" }
        }
    }
    
    private fun readTextWithNativeTTS(
        context: Context,
        mediaSessionCompat: MediaSessionCompat,
        chapter: Chapter,
        content: List<String>
    ) {
        if (player == null) {
            Log.error { "Native TTS player is null! Reinitializing..." }
            initPlayer()
            return
        }
        
        updatePlayerSettings()
        
        if (state.utteranceId != state.currentReadingParagraph.toString()) {
            player?.speak(
                content[state.currentReadingParagraph],
                TextToSpeech.QUEUE_FLUSH,
                null,
                state.currentReadingParagraph.toString()
            )
        }
        
        player?.setOnUtteranceProgressListener(createUtteranceListener(context, mediaSessionCompat, chapter, content))
    }
    
    private fun readTextWithAITTS(
        context: Context,
        mediaSessionCompat: MediaSessionCompat,
        chapter: Chapter,
        content: List<String>
    ) {
        scope.launch {
            try {
                // Check if Coqui TTS is enabled and configured
                val useCoquiTTS = appPrefs.useCoquiTTS().get()
                val coquiSpaceUrl = appPrefs.coquiSpaceUrl().get()
                
                if (useCoquiTTS && coquiSpaceUrl.isNotEmpty()) {
                    Log.info { "Using Coqui TTS for playback" }
                    readTextWithCoquiTTS(context, mediaSessionCompat, chapter, content)
                    return@launch
                }
                
                // Check if AI TTS is actually available
                if (!aiTTSManager.hasAvailableProvider()) {
                    Log.warn { "No AI TTS provider available, falling back to native TTS" }
                    readTextWithNativeTTS(context, mediaSessionCompat, chapter, content)
                    return@launch
                }
                
                val selectedProvider = try {
                    ireader.domain.services.tts.AITTSProvider.valueOf(
                        appPrefs.selectedAITTSProvider().get()
                    )
                } catch (e: Exception) {
                    ireader.domain.services.tts.AITTSProvider.NATIVE_ANDROID
                }
                
                val selectedVoiceId = appPrefs.selectedAIVoiceId().get()
                
                // Check if voice is selected and downloaded
                if (selectedVoiceId.isEmpty() || !aiTTSManager.isVoiceDownloaded(selectedVoiceId)) {
                    Log.warn { "AI TTS voice not selected or not downloaded, falling back to native TTS" }
                    readTextWithNativeTTS(context, mediaSessionCompat, chapter, content)
                    return@launch
                }
                
                val speechSpeed = readerPreferences.speechRate().get()
                val pitch = readerPreferences.speechPitch().get()
                
                state.utteranceId = state.currentReadingParagraph.toString()
                
                aiTTSPlayer.speak(
                    text = content[state.currentReadingParagraph],
                    provider = selectedProvider,
                    voiceId = selectedVoiceId,
                    speed = speechSpeed,
                    pitch = pitch,
                    onStart = {
                        state.isPlaying = true
                        state.utteranceId = state.currentReadingParagraph.toString()
                    },
                    onComplete = {
                        handleAITTSComplete(context, mediaSessionCompat, chapter, content)
                    },
                    onError = { error ->
                        Log.error { "AI TTS error: ${error.message}, falling back to native TTS" }
                        // Fall back to native TTS on error
                        readTextWithNativeTTS(context, mediaSessionCompat, chapter, content)
                    }
                )
            } catch (e: Exception) {
                Log.error { "AI TTS error: ${e.message}, falling back to native TTS" }
                readTextWithNativeTTS(context, mediaSessionCompat, chapter, content)
            }
        }
    }
    
    /**
     * Read text using Coqui TTS with preloading and auto-next features
     */
    private fun readTextWithCoquiTTS(
        context: Context,
        mediaSessionCompat: MediaSessionCompat,
        chapter: Chapter,
        content: List<String>
    ) {
        scope.launch {
            try {
                val coquiSpaceUrl = appPrefs.coquiSpaceUrl().get()
                val coquiApiKey = appPrefs.coquiApiKey().get().takeIf { it.isNotEmpty() }
                val speechSpeed = readerPreferences.speechRate().get()
                
                // Create Coqui TTS service
                val coquiService = ireader.domain.services.tts.CoquiTTSService(context, coquiSpaceUrl, coquiApiKey)
                
                // Start reading with auto-advance and preloading
                coquiService.startReading(
                    paragraphs = content,
                    startIndex = state.currentReadingParagraph,
                    speed = speechSpeed,
                    autoNext = true,
                    onParagraphComplete = { paragraphIndex ->
                        // Update state and notification
                        state.currentReadingParagraph = paragraphIndex
                        state.utteranceId = paragraphIndex.toString()
                        scope.launch {
                            setBundle()
                            updateNotification()
                        }
                    },
                    onChapterComplete = {
                        // Chapter finished
                        handleChapterFinished(context, mediaSessionCompat, chapter)
                    }
                )
                
                Log.info { "Started Coqui TTS playback with preloading" }
                
            } catch (e: Exception) {
                Log.error { "Coqui TTS error: ${e.message}, falling back to native TTS" }
                readTextWithNativeTTS(context, mediaSessionCompat, chapter, content)
            }
        }
    }
    
    private fun handleAITTSComplete(
        context: Context,
        mediaSessionCompat: MediaSessionCompat,
        chapter: Chapter,
        content: List<String>
    ) {
        checkSleepTime()
        runCatching {
            handleUtteranceDone(context, mediaSessionCompat, chapter, content)
        }.onFailure { e ->
            Log.error { "Error in AI TTS completion: ${e.message}" }
        }
    }

    private fun updatePlayerSettings() {
        with(state) {
            if (languages.isEmpty()) {
                player?.availableLanguages?.let { languages = it.toList() }
            }
            if (voices.isEmpty()) {
                player?.voices?.let { voices = it.toList() }
            }
            if (currentVoice != prevVoice) {
                prevVoice = currentVoice
                player?.voices?.firstOrNull { it.isSame(currentVoice) }?.let {
                    player?.voice = it
                }
            }
            if (currentLanguage != prevLanguage) {
                prevLanguage = currentLanguage
                player?.availableLanguages?.firstOrNull { it.displayName == currentLanguage }?.let {
                    player?.language = it
                }
            }
            if (pitch != prevPitch) {
                prevPitch = pitch
                player?.setPitch(pitch)
            }
            if (speechSpeed != prevSpeechSpeed) {
                prevSpeechSpeed = speechSpeed
                player?.setSpeechRate(speechSpeed)
            }
        }
    }

    private fun createUtteranceListener(
        context: Context,
        mediaSessionCompat: MediaSessionCompat,
        chapter: Chapter,
        content: List<String>
    ) = object : UtteranceProgressListener() {
        
        override fun onStop(utteranceId: String?, interrupted: Boolean) {
            super.onStop(utteranceId, interrupted)
            state.utteranceId = ""
        }

        override fun onStart(utteranceId: String) {
            state.isPlaying = true
            state.utteranceId = utteranceId
        }

        override fun onDone(utteranceId: String) {
            checkSleepTime()
            runCatching {
                handleUtteranceDone(context, mediaSessionCompat, chapter, content)
            }.onFailure { e ->
                Log.error { "Error in onDone: ${e.message}" }
            }
        }

        override fun onError(utteranceId: String) {
            state.isPlaying = false
        }
    }

    private fun handleUtteranceDone(
        context: Context,
        mediaSessionCompat: MediaSessionCompat,
        chapter: Chapter,
        content: List<String>
    ) {
        val isFinished = state.currentReadingParagraph >= content.lastIndex
        
        if (!isFinished && state.currentReadingParagraph < content.size) {
            state.currentReadingParagraph += 1
            readText(context, mediaSessionCompat)
            return
        }
        
        if (isFinished && controller?.playbackState?.isPlaying == true) {
            handleChapterFinished(context, mediaSessionCompat, chapter)
        }
    }

    private fun handleChapterFinished(context: Context, mediaSessionCompat: MediaSessionCompat, chapter: Chapter) {
        state.isPlaying = false
        state.currentReadingParagraph = 0
        player?.stop()
        aiTTSPlayer.stop()
        
        if (state.autoNextChapter) {
            scope.launch {
                val chapters = state.ttsChapters
                val source = state.ttsCatalog ?: return@launch
                val index = getChapterIndex(chapter, chapters)
                
                if (index != chapters.lastIndex) {
                    val nextChapterId = chapters[index + 1].id
                    getRemoteChapter(nextChapterId, source, state) {
                        state.currentReadingParagraph = 0
                        updateNotification()
                        readText(context, mediaSessionCompat)
                    }
                }
            }
        }
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

    suspend fun getRemoteChapter(
        chapterId: Long,
        source: CatalogLocal,
        ttsState: TTSState,
        onSuccess: suspend () -> Unit,
    ) {
        try {
            setLoadingState(true)
            state.currentReadingParagraph = 0
            
            val localChapter = chapterRepo.findChapterById(chapterId) ?: return
            
            if (!localChapter.isEmpty()) {
                handleLocalChapter(localChapter, chapterId, onSuccess)
            } else {
                handleRemoteChapter(localChapter, source, chapterId, onSuccess)
            }
        } catch (e: Exception) {
            Log.error { "Error loading chapter: ${e.message}" }
            setBundle(error = true)
            updateNotification()
        }
    }

    private suspend fun setLoadingState(isLoading: Boolean) {
        setBundle(isLoading = isLoading)
        updateNotification()
    }

    private suspend fun handleLocalChapter(chapter: Chapter, chapterId: Long, onSuccess: suspend () -> Unit) {
        state.ttsChapter = chapter
        state.currentReadingParagraph = 0
        
        loadTranslatedContentForTTS(chapterId)
        
        setLoadingState(false)
        chapterUseCase.updateLastReadTime(chapter, updateDateFetched = false)
        onSuccess()
    }

    private suspend fun handleRemoteChapter(
        chapter: Chapter,
        source: CatalogLocal,
        chapterId: Long,
        onSuccess: suspend () -> Unit
    ) {
        remoteUseCases.getRemoteReadingContent(
            chapter = chapter,
            source,
            onSuccess = { result ->
                if (result.content.joinToString().isNotEmpty()) {
                    updateStateWithRemoteChapter(result, chapterId, onSuccess)
                }
            },
            onError = {
                setLoadingState(false)
            }
        )
    }

    private suspend fun updateStateWithRemoteChapter(chapter: Chapter, chapterId: Long, onSuccess: suspend () -> Unit) {
        state.ttsChapter = chapter
        state.currentReadingParagraph = 0
        state.ttsChapters = chapterRepo.findChaptersByBookId(chapter.bookId)
        
        chapterUseCase.updateLastReadTime(chapter, updateDateFetched = true)
        
        loadTranslatedContentForTTS(chapterId)
        setLoadingState(false)
        onSuccess()
    }

    private suspend fun loadTranslatedContentForTTS(chapterId: Long) {
        // Check if TTS with translated text is enabled
        val useTTSWithTranslatedText = readerPreferences.useTTSWithTranslatedText().get()
        
        if (!useTTSWithTranslatedText) {
            state.translatedTTSContent = null
            return
        }
        
        // Get translation preferences
        val targetLanguage = readerPreferences.translatorTargetLanguage().get()
        val engineId = readerPreferences.translatorEngine().get()
        
        try {
            // Try to get translated chapter
            val translatedChapter = getTranslatedChapterUseCase.execute(
                chapterId = chapterId,
                targetLanguage = targetLanguage,
                engineId = engineId
            )
            
            if (translatedChapter != null) {
                // Extract text from translated content
                val translatedText = translatedChapter.translatedContent
                    .filterIsInstance<ireader.core.source.model.Text>()
                    .map { it.text }
                    .filter { it.isNotBlank() }
                    .map { it.trim() }
                
                state.translatedTTSContent = translatedText
                Log.info { "Loaded translated content for TTS: ${translatedText.size} paragraphs" }
            } else {
                state.translatedTTSContent = null
                Log.info { "No translated content available for TTS, will use original text" }
            }
        } catch (e: Exception) {
            Log.error(e, "Error loading translated content for TTS")
            state.translatedTTSContent = null
        }
    }
    
    fun getChapterIndex(chapter: Chapter, chapters: List<Chapter>): Int {
        val chaptersIds = chapters.map { it.name }
        val index = chaptersIds.indexOfFirst { it == chapter.name }

        return if (index != -1) {
            index
        } else {
            Log.error("TTSService", "Chapter '${chapter.name}' not found in chapters list. Returning 0 as fallback.")
            0 // Return first chapter as fallback instead of crashing
        }
    }

    private inner class NoisyReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == AudioManager.ACTION_AUDIO_BECOMING_NOISY) {
                resumeOnFocus = false
                player?.stop()
                aiTTSPlayer.stop()
            }
        }
    }

    override fun onAudioFocusChange(focusChange: Int) {
        Log.debug("TAG", "Focus change $focusChange")
        when (focusChange) {
            AudioManager.AUDIOFOCUS_GAIN -> if (resumeOnFocus) {
                synchronized(focusLock) { resumeOnFocus = false }
                startService(Player.PLAY)
            }
            AudioManager.AUDIOFOCUS_LOSS -> {
                synchronized(focusLock) { resumeOnFocus = false }
                player?.stop()
                aiTTSPlayer.stop()
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT, AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                synchronized(focusLock) { resumeOnFocus = state.isPlaying }
                player?.stop()
                aiTTSPlayer.stop()
            }
        }
    }

}
