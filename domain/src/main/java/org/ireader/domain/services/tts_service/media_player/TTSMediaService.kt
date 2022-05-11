package org.ireader.domain.services.tts_service.media_player

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaMetadata
import android.media.MediaPlayer
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
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.ireader.common_models.entities.Book
import org.ireader.common_models.entities.CatalogLocal
import org.ireader.common_models.entities.Chapter
import org.ireader.core_api.log.Log
import org.ireader.core_catalogs.CatalogStore
import org.ireader.core_ui.preferences.ReaderPreferences
import org.ireader.domain.R
import org.ireader.domain.notification.Notifications
import org.ireader.domain.services.tts_service.IReaderVoice
import org.ireader.domain.services.tts_service.Player
import org.ireader.domain.services.tts_service.TTSState
import org.ireader.domain.services.tts_service.TTSStateImpl
import org.ireader.domain.services.tts_service.isSame
import org.ireader.domain.use_cases.local.LocalGetChapterUseCase
import org.ireader.domain.use_cases.local.LocalInsertUseCases
import org.ireader.domain.use_cases.preferences.reader_preferences.TextReaderPrefUseCase
import org.ireader.domain.use_cases.remote.RemoteUseCases
import org.ireader.domain.utils.notificationManager
import javax.inject.Inject

@AndroidEntryPoint
class TTSService : MediaBrowserServiceCompat(), AudioManager.OnAudioFocusChangeListener {

    @Inject
    lateinit var bookRepo: org.ireader.common_data.repository.LocalBookRepository

    @Inject
    lateinit var chapterRepo: org.ireader.common_data.repository.LocalChapterRepository

    @Inject
    lateinit var chapterUseCase: LocalGetChapterUseCase

    @Inject
    lateinit var remoteUseCases: RemoteUseCases

    @Inject
    lateinit var extensions: CatalogStore

    lateinit var ttsNotificationBuilder: TTSNotificationBuilder

    lateinit var state: TTSStateImpl

    @Inject
    lateinit var insertUseCases: LocalInsertUseCases

    @Inject
    lateinit var textReaderPrefUseCase: TextReaderPrefUseCase

    private val noisyReceiver = NoisyReceiver()
    private var noisyReceiverHooked = false
    private val focusLock = Any()
    private var resumeOnFocus = true

    @Inject
    lateinit var readerPreferences: ReaderPreferences

    lateinit var mediaSession: MediaSessionCompat
    lateinit var stateBuilder: PlaybackStateCompat.Builder
    private val metadata = MediaMetadataCompat.Builder()

    private lateinit var mediaCallback: TTSSessionCallback
    private lateinit var notificationController: NotificationController
    private lateinit var controller: MediaControllerCompat

    private lateinit var player: TextToSpeech

    private var serviceJob: Job? = null
    private var isPlayerDispose = false
    private var isHooked = false
    private lateinit var focusRequest: AudioFocusRequest
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

    fun readPrefs() {
        scope.launch {
            state.autoNextChapter = readerPreferences.readerAutoNext().get()
            state.currentLanguage = readerPreferences.speechLanguage().get()
            state.currentVoice = textReaderPrefUseCase.readVoice()
            state.speechSpeed = readerPreferences.speechRate().get()
            state.pitch = readerPreferences.speechPitch().get()
        }
        scope.launch {
            readerPreferences.readerAutoNext().changes().collect {
                state.autoNextChapter = it
            }
        }
        scope.launch {
            readerPreferences.speechLanguage().changes().collect {
                state.currentLanguage = it
            }
        }
        scope.launch {
            readerPreferences.speechVoice().changes().collect {
                state.currentVoice = kotlin.runCatching {
                     return@runCatching Json.decodeFromString<IReaderVoice?>(it)
                }.getOrNull()
            }
        }
        scope.launch {
            readerPreferences.speechPitch().changes().collect {
                state.pitch = it
            }
        }
        scope.launch {
            readerPreferences.speechRate().changes().collect {
                state.speechSpeed = it
            }
        }
    }

    lateinit var silence: MediaPlayer
    override fun onCreate() {
        super.onCreate()
        Log.debug { "TTS SERVICE: OnCreate is Called" }

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

        ttsNotificationBuilder = TTSNotificationBuilder(this, mbrComponent)
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

        silence.start()
    }

    var isNotificationForeground = false

    fun hookNotification() {
        if (!isNotificationForeground) {
            startService(Intent(applicationContext, TTSService::class.java))
            scope.launch {
                startForeground(
                    Notifications.ID_TTS,
                    ttsNotificationBuilder.buildNotification(mediaSession.sessionToken)
                )
                isNotificationForeground = true
            }
        }
        if (isPlayerDispose) {
            initPlayer()
        }
        if (!noisyReceiverHooked) {
            registerReceiver(noisyReceiver, IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY))
            noisyReceiverHooked = true
        }
        val am = baseContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val result = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN).run {
                setOnAudioFocusChangeListener(this@TTSService)
                setAudioAttributes(AudioAttributes.Builder().run {
                    setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    setUsage(AudioAttributes.USAGE_MEDIA)
                    build()
                })
                build()
            }
            am.requestAudioFocus(focusRequest)
        } else {
            @Suppress("DEPRECATION")
            am.requestAudioFocus(
                this@TTSService,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN
            )
        }
    }

    fun unhookNotification() {
        stopForeground(true)
        try {

            unregisterReceiver(noisyReceiver)
        } catch (e: Exception) {
        }
        isNotificationForeground = false
    }

    private fun initPlayer() {
        player = TextToSpeech(this) { status ->
            if (status == TextToSpeech.ERROR) {
                Log.error { "Text-to-Speech Not Available" }
                setBundle(isLoading = false)
                return@TextToSpeech
            }
            if (status == TextToSpeech.SUCCESS) {
                state.voices = player.voices?.toList() ?: emptyList()
                state.languages = player.availableLanguages?.toList() ?: emptyList()
                readPrefs()
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
        Log.error { "Start Command" }
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
                            setBundle(book, chapter)
                            startService(command)
                        }
                    }
                }
            }
            ACTION_CANCEL -> {
                player.stop()
                state.utteranceId = ""
                val am = baseContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    am.abandonAudioFocusRequest(focusRequest)
                } else {
                    @Suppress("DEPRECATION")
                    am.abandonAudioFocus(this@TTSService)
                }

                Log.error { "Service was cancelled" }
                unhookNotification()
                if (isNotificationForeground) {
                    stopForeground(true)
                    isNotificationForeground = false
                }
                if (noisyReceiverHooked) {
                    kotlin.runCatching {
                        unregisterReceiver(noisyReceiver)
                        noisyReceiverHooked = false

                    }
                }
                resumeOnFocus = false
                notificationController.stop()
                isPlayerDispose = true
                setPlaybackState(PlaybackStateCompat.STATE_NONE)
                stopSelf()

                return START_STICKY
            }
            null -> {}
            else -> Log.error { "Unknown Intent $intent" }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        player.shutdown()
        notificationController.stop()
        unhookNotification()
        mediaSession.isActive = false
        mediaSession.release()
        isPlayerDispose = true
        Log.debug { "TTS SERVICE was Destroyed" }
        super.onDestroy()
    }

    /**
     * set book info in meta date so thet we could get them directly
     * in notifications
     */
    private fun setBundle(
        book: Book? = state.ttsBook,
        chapter: Chapter? = state.ttsChapter,
        isLoading: Boolean = false,
        error: Boolean = false
    ) {
        val data = metadata
            .apply {
                val lastTrackNumber = state.ttsContent?.value?.lastIndex?.toLong()
                if (book != null) {
                    putText(NOVEL_TITLE, book.title)
                    putLong(NOVEL_ID, book.id)
                    putLong(FAVORITE, if (book.favorite) 1 else 0)
                    putLong(SOURCE_ID, book.sourceId)
                    putText(NOVEL_COVER, book.cover)
                    putText(MediaMetadata.METADATA_KEY_AUTHOR, book.author)
                }
                if (chapter != null) {
                    putText(CHAPTER_TITLE, chapter.title)
                    putLong(CHAPTER_ID, chapter.id)
                    putText(MediaMetadata.METADATA_KEY_DISPLAY_TITLE, chapter.title)
                }
                putLong(IS_LOADING, if (isLoading) 1L else 0L)
                putLong(ERROR, if (error) 1L else 0L)
                putLong(PROGRESS, state.currentReadingParagraph.toLong())
                putLong(LAST_PARAGRAPH, lastTrackNumber ?: 1L)
            }.build()
        mediaSession.setMetadata(data)
    }

    private inner class TTSSessionCallback : MediaSessionCompat.Callback() {

        override fun onPlay() {
            Log.debug { "TTS SERVICE: onPlay" }
            if (isPlayerDispose) {
                initPlayer()
            }
            startService(Player.PLAY)
        }

        override fun onPause() {
            Log.debug { "TTS SERVICE: onPause" }
            startService(Player.PAUSE)
            player.stop()
        }

        override fun onStop() {
            Log.debug { "TTS SERVICE: onStop" }
            startService(Player.PAUSE)
            player.stop()
        }

        override fun onRewind() {
            Log.debug { "TTS SERVICE: onRewind" }
            startService(Player.PREV_PAR)
        }

        override fun onFastForward() {
            Log.debug { "TTS SERVICE: onFastForward" }
            startService(Player.NEXT_PAR)
        }

        override fun onSkipToNext() {
            Log.debug { "TTS SERVICE: TTS SERVICE: onSkipToNext" }
            startService(Player.SKIP_NEXT)
        }

        override fun onSkipToPrevious() {
            Log.debug { "TTS SERVICE: onSkipToPrevious" }
            startService(Player.SKIP_PREV)
        }

        override fun onSeekTo(pos: Long) {
            state.currentReadingParagraph = pos.toInt()
            setBundle()
            if (state.isPlaying) {
                startService(Player.PLAY)
            }
        }

        override fun onCommand(command: String?, extras: Bundle?, cb: ResultReceiver?) {
            super.onCommand(command, extras, cb)
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

        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            if (state != null && mediaSession.controller.metadata != null) {
                scope.launch {
                    notificationManager.notify(
                        Notifications.ID_TTS,
                        ttsNotificationBuilder.buildTTSNotification(mediaSession).build()
                    )
                }
            }
        }
    }

    private fun setPlaybackState(state: Int) {
        // Playback speed is set to 0 because our duration is faked one, and we update position manually
        // instead of relying on MediaSession doing it for us.
        mediaSession.setPlaybackState(
            stateBuilder.setState(
                state,
                this.state.currentReadingParagraph.toLong() * 1000L,
                0.0f
            ).build()
        )
    }

    suspend fun updateNotification() {
        mediaSession.let { mediaSession ->
            NotificationManagerCompat.from(this).apply {
                val builder =
                    ttsNotificationBuilder.buildTTSNotification(
                        mediaSession
                    )

                notify(
                    Notifications.ID_TTS,
                    builder.build()
                )
            }
        }
    }

    fun startService(command: Int) {
        if (isPlayerDispose) {
            initPlayer()
        }
        hookNotification()
        serviceJob = scope.launch {
            try {
                player.let { tts ->
                    mediaSession.let { mediaSession ->
                        state.ttsChapter?.let { chapter ->
                            state.ttsCatalog?.let { source ->
                                state.ttsChapters.let { chapters ->
                                    state.ttsBook?.let { book ->
                                        setBundle(book, chapter)
//                                        val notification =
//                                            ttsNotificationBuilder.buildTTSNotification(
//                                                mediaSession,
//                                            ).build()
                                        hookNotification()
//                                        NotificationManagerCompat.from(this@TTSService)
//                                            .notify(
//                                                Notifications.ID_TTS,
//                                                notification
//                                            )

                                        when (command) {
                                            Player.CANCEL -> {
                                                NotificationManagerCompat.from(this@TTSService)
                                                    .cancel(Notifications.ID_TTS)
                                            }
                                            Player.SKIP_PREV -> {
                                                tts.stop()
                                                updateNotification()
                                                val index = getChapterIndex(chapter, chapters)
                                                if (index > 0) {
                                                    val id = chapters[index - 1].id
                                                    getRemoteChapter(
                                                        chapterId = id,
                                                        source,
                                                        state
                                                    ) {
                                                        if (state.isPlaying) {
                                                            readText(this@TTSService, mediaSession)
                                                        }
                                                    }
                                                }
                                                state.currentReadingParagraph = 0
                                                updateNotification()
                                                if (state.isPlaying) {
                                                    readText(this@TTSService, mediaSession)
                                                } else {
                                                }
                                            }
                                            Player.PREV_PAR -> {
                                                state.ttsContent?.value?.let { content ->
                                                    if (state.currentReadingParagraph > 0 && state.currentReadingParagraph in 0..content.lastIndex) {
                                                        state.currentReadingParagraph -= 1
                                                        updateNotification()
                                                        if (state.isPlaying) {
                                                            readText(this@TTSService, mediaSession)
                                                        }
                                                    }
                                                }
                                            }
                                            Player.SKIP_NEXT -> {
                                                tts.stop()
                                                updateNotification()
                                                val index = getChapterIndex(chapter, chapters)
                                                if (index != chapters.lastIndex) {
                                                    val id = chapters[index + 1].id
                                                    getRemoteChapter(
                                                        chapterId = id,
                                                        source,
                                                        state
                                                    ) {
                                                        if (state.isPlaying) {
                                                            readText(this@TTSService, mediaSession)
                                                        }
                                                    }
                                                }
                                                state.currentReadingParagraph = 0
                                                updateNotification()
                                                if (state.isPlaying) {
                                                    readText(this@TTSService, mediaSession)
                                                } else {
                                                }
                                            }
                                            Player.NEXT_PAR -> {
                                                state.ttsContent?.value?.let { content ->
                                                    if (state.currentReadingParagraph >= 0 && state.currentReadingParagraph < content.size) {
                                                        if (state.currentReadingParagraph < content.lastIndex) {
                                                            state.currentReadingParagraph += 1
                                                        }
                                                        if (state.isPlaying) {
                                                            readText(this@TTSService, mediaSession)
                                                            updateNotification()
                                                        }
                                                    }
                                                }
                                            }
                                            Player.PLAY -> {
                                                setPlaybackState(PlaybackStateCompat.STATE_PLAYING)
                                                hookNotification()
                                                state.isPlaying = true
                                                readText(this@TTSService, mediaSession)
                                            }
                                            Player.PAUSE -> {
                                                setPlaybackState(PlaybackStateCompat.STATE_PAUSED)
                                                state.isPlaying = false
                                                tts.stop()
                                                updateNotification()
                                            }
                                            Player.PLAY_PAUSE -> {
                                                if (state.isPlaying) {
                                                    setPlaybackState(PlaybackStateCompat.STATE_PAUSED)
                                                    state.isPlaying = false
                                                    tts.stop()
                                                    updateNotification()
                                                } else {
                                                    setPlaybackState(PlaybackStateCompat.STATE_PLAYING)
                                                    state.isPlaying = true
                                                    readText(this@TTSService, mediaSession)
                                                }
                                            }
                                            else -> {
                                                if (state.isPlaying) {
                                                    state.isPlaying = false
                                                    tts.stop()
                                                    updateNotification()
                                                } else {
                                                    state.isPlaying = true
                                                    readText(this@TTSService, mediaSession)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (e: java.lang.Exception) {
                Log.error { e.stackTrace.toString() }

                state.ttsChapter?.let { chapter ->
                    state.ttsSource?.let { source ->
                        state.ttsChapters.let { chapters ->
                            state.ttsBook?.let { book ->
                                setBundle()
                                updateNotification()
                            }
                        }
                    }
                }
            }
        }
    }

    private val ttsJob = Job()
    val scope = CoroutineScope(Dispatchers.Main.immediate + ttsJob)
    fun readText(context: Context, mediaSessionCompat: MediaSessionCompat) {
        setBundle()
        state.apply {
            if (state.languages.isEmpty()) {
                player.availableLanguages?.let {
                    state.languages = it.toList()
                }
            }
            if (state.voices.isEmpty()) {
                player.voices?.toList()?.let {
                    state.voices = it
                }
            }
            if (currentVoice != prevVoice) {
                prevVoice = currentVoice
                player.voices?.firstOrNull { it.isSame(currentVoice) }?.let {
                    player.voice = it
                }
            }


            if (currentLanguage != prevLanguage) {
                prevLanguage = currentLanguage
                player.availableLanguages?.firstOrNull { it.displayName == currentLanguage }
                    ?.let {
                        player.language = it
                    }
            }

            if (pitch != prevPitch) {
                prevPitch = pitch
                player.setPitch(pitch)
            }
            if (speechSpeed != prevSpeechSpeed) {
                prevSpeechSpeed = speechSpeed
                player.setSpeechRate(speechSpeed)
            }

            ttsChapter?.let { chapter ->
                ttsContent?.value?.let { content ->
                    state.ttsBook?.let { book ->
                        try {

                            scope.launch {
                                updateNotification()
                            }
                            if (state.utteranceId != (currentReadingParagraph).toString()) {
                                player.speak(
                                    content[currentReadingParagraph],
                                    TextToSpeech.QUEUE_FLUSH,
                                    null,
                                    currentReadingParagraph.toString()
                                )
                            }

                            player.setOnUtteranceProgressListener(object :
                                UtteranceProgressListener() {
                                override fun onStop(
                                    utteranceId: String?,
                                    interrupted: Boolean,
                                ) {
                                    super.onStop(utteranceId, interrupted)
                                    state.utteranceId = ""
                                }

                                override fun onStart(p0: String) {
                                    // showTextReaderNotification(context)
                                    isPlaying = true
                                    utteranceId = p0
                                }

                                override fun onDone(p0: String) {
                                    try {
                                        var isFinished = false

                                        if (currentReadingParagraph < content.size) {
                                            if (currentReadingParagraph < content.lastIndex) {
                                                currentReadingParagraph += 1
                                            } else {
                                                isFinished = true
                                            }
                                            readText(context, mediaSessionCompat)
                                        }

                                        if (currentReadingParagraph == content.lastIndex && controller.playbackState.isPlaying && isFinished) {
                                            isPlaying = false
                                            currentReadingParagraph = 0

                                            player.stop()
                                            if (autoNextChapter) {
                                                scope.launch {
                                                    state.ttsChapters.let { chapters ->
                                                        state.ttsCatalog?.let { source ->
                                                            val index =
                                                                getChapterIndex(
                                                                    chapter,
                                                                    chapters
                                                                )
                                                            if (index != chapters.lastIndex) {
                                                                val id = chapters[index + 1].id
                                                                getRemoteChapter(
                                                                    chapterId = id,
                                                                    source,
                                                                    state
                                                                ) {
                                                                    state.currentReadingParagraph =
                                                                        0
                                                                    updateNotification(
                                                                    )

                                                                    mediaSession.let { mediaSession ->
                                                                        readText(
                                                                            context,
                                                                            mediaSession
                                                                        )
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    } catch (e: Throwable) {
                                        Log.error { e.stackTrace.toString() }
                                    }
                                }

                                override fun onError(p0: String) {
                                    isPlaying = false
                                }

                                override fun onBeginSynthesis(
                                    utteranceId: String?,
                                    sampleRateInHz: Int,
                                    audioFormat: Int,
                                    channelCount: Int,
                                ) {
                                    super.onBeginSynthesis(
                                        utteranceId,
                                        sampleRateInHz,
                                        audioFormat,
                                        channelCount
                                    )
                                }
                            })
                        } catch (e: Throwable) {
                            Log.error { e.stackTrace.toString() }
                        }
                    }
                }
            }
        }
    }

    suspend fun getRemoteChapter(
        chapterId: Long,
        source: CatalogLocal,
        ttsState: TTSState,
        onSuccess: suspend () -> Unit,
    ) {
        try {

            ttsState.ttsChapter?.let { chapter ->
                ttsState.ttsBook?.let { book ->
                    setBundle(isLoading = true)
                    updateNotification()
                }
            }
            setBundle(isLoading = true)
            state.currentReadingParagraph = 0
            val localChapter = chapterRepo.findChapterById(chapterId)

            if (localChapter?.isEmpty() == false) {
                state.ttsChapter = localChapter
                state.currentReadingParagraph = 0
                setBundle(isLoading = false)
                ttsState.ttsBook?.let { book ->
                    updateNotification()
                }
                chapterUseCase.updateLastReadTime(localChapter, updateDateFetched = false)
                onSuccess()
            } else {
                if (localChapter != null) {
                    remoteUseCases.getRemoteReadingContent(
                        chapter = localChapter,
                        source,
                        onSuccess = { result ->
                            if (result.content.joinToString().length > 1) {
                                state.ttsChapter = result
                                chapterUseCase.updateLastReadTime(result, updateDateFetched = true)
                                chapterRepo.findChaptersByBookId(result.bookId).let { res ->
                                    state.ttsChapters = res
                                }
                                state.currentReadingParagraph = 0
                                setBundle(isLoading = false)

                                ttsState.ttsBook?.let { book ->
                                    updateNotification()
                                }
                                onSuccess()
                            }
                        }, onError = {
                            setBundle(isLoading = false)
                        }
                    )
                }
            }
            setBundle(isLoading = false)
        } catch (e: java.lang.Exception) {
            Log.error { e.stackTrace.toString() }
            ttsState.ttsChapter?.let { chapter ->
                ttsState.ttsBook?.let { book ->
                    setBundle(error = true)
                    updateNotification()
                }
            }
        }
    }

    fun getChapterIndex(chapter: Chapter, chapters: List<Chapter>): Int {
        val chaptersIds = chapters.map { it.title }
        val index = chaptersIds.indexOfFirst { it == chapter.title }

        return if (index != -1) {
            index
        } else {
            throw Exception("Invalid Id")
        }
    }

    private inner class NoisyReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == AudioManager.ACTION_AUDIO_BECOMING_NOISY) {
                resumeOnFocus = false
                player.stop()
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
                player.stop()
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT, AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                synchronized(focusLock) { resumeOnFocus = state.isPlaying }
                player.stop()
            }
        }
    }
}
