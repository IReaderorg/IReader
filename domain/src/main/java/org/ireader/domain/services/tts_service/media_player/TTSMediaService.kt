package org.ireader.domain.services.tts_service.media_player

import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.media.MediaMetadata
import android.os.Bundle
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
import kotlinx.coroutines.*
import kotlinx.datetime.Clock
import org.ireader.common_models.entities.Book
import org.ireader.common_models.entities.Chapter
import org.ireader.core_api.log.Log
import org.ireader.core_api.source.Source
import org.ireader.core_catalogs.CatalogStore

import org.ireader.domain.notification.Notifications
import org.ireader.domain.services.tts_service.Player
import org.ireader.domain.use_cases.local.LocalInsertUseCases
import org.ireader.domain.use_cases.remote.RemoteUseCases
import org.ireader.domain.utils.notificationManager
import org.ireader.presentation.feature_ttl.TTSState
import org.ireader.presentation.feature_ttl.TTSStateImpl
import javax.inject.Inject


private const val TTS_SERVICE = "tts_service"

@AndroidEntryPoint
class TTSService : MediaBrowserServiceCompat() {

    @Inject
    lateinit var bookRepoConnection: org.ireader.common_data.repository.LocalBookRepository

    lateinit var bookRepo: org.ireader.common_data.repository.LocalBookRepository

    @Inject
    lateinit var chapterRepoConnection: org.ireader.common_data.repository.LocalChapterRepository
    lateinit var chapterRepo: org.ireader.common_data.repository.LocalChapterRepository

    @Inject
    lateinit var remoteUseCasesConnection: RemoteUseCases
    lateinit var remoteUseCases: RemoteUseCases

    @Inject
    lateinit var extensionsConnection: CatalogStore
    lateinit var extensions: CatalogStore

    lateinit var ttsNotificationBuilder: TTSNotificationBuilder

    @Inject
    lateinit var stateConnection: TTSStateImpl

    lateinit var state: TTSStateImpl

    @Inject
    lateinit var insertUseCasesConnection: LocalInsertUseCases

    lateinit var insertUseCases: LocalInsertUseCases

    lateinit var mediaSession: MediaSessionCompat
    lateinit var stateBuilder: PlaybackStateCompat.Builder
    private lateinit var mediaCallback: TTSSessionCallback
    private lateinit var notificationController: NotificationController

    private lateinit var player: TextToSpeech

    private var serviceJob: Job? = null

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

        const val ACTION_UPDATE = "actionUpdate"


        const val NOVEL_ID = "novel_id"
        const val SOURCE_ID = "source_id"
        const val FAVORITE = "favorite"
        const val NOVEL_TITLE = "novel_title"
        const val NOVEL_COVER = "novel_cover"
        const val PROGRESS = "progress"
        const val LAST_PARAGRAPH = "last_paragraph"
        const val CHAPTER_TITLE = "chapter_title"
        const val CHAPTER_ID = "chapter_id"


    }


    override fun onCreate() {
        super.onCreate()
        Log.error { "OnCreate is Called" }

        state = stateConnection
        insertUseCases = insertUseCasesConnection
        remoteUseCases = remoteUseCasesConnection
        extensions = extensionsConnection
        chapterRepo = chapterRepoConnection
        bookRepo = bookRepoConnection

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
            .setActions(PlaybackStateCompat.ACTION_PLAY or
                    PlaybackStateCompat.ACTION_STOP or
                    PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                    PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or
                    PlaybackStateCompat.ACTION_FAST_FORWARD or
                    PlaybackStateCompat.ACTION_REWIND)
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
            PendingIntent.getService(this@TTSService,
                0,
                Intent(this@TTSService, TTSService::class.java).apply {
                    action = ACTION_PLAY_PAUSE
                },
                pendingIntentFlags)
        mediaSession.setMediaButtonReceiver(mediaReceiverPendingIntent)
        /**
         *  Media session need to be active before using media buttons
         *  https://stackoverflow.com/questions/38247050/mediabuttonreceiver-not-working-with-mediabrowserservicecompat
         */
        try {
            mediaSession.isActive = true
        } catch (e: NullPointerException) {
            mediaSession.isActive = false
            mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or
                    MediaSessionCompat.FLAG_HANDLES_QUEUE_COMMANDS or
                    MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS
            )
            mediaSession.isActive = true
        }
        mediaButtonIntent.setClass(this, MediaButtonReceiver::class.java)

        ttsNotificationBuilder = TTSNotificationBuilder(this, mbrComponent)

        /**
         * Initializing the player
         */
        player = TextToSpeech(this) { status ->
            state.ttsIsLoading = true
            if (status == TextToSpeech.ERROR) {
                Log.error { "Text-to-Speech Not Available" }

                state.ttsIsLoading = false
                return@TextToSpeech
            }
            state.ttsIsLoading = false
        }
        notificationController = NotificationController()


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
                        val source = book?.sourceId?.let { extensions.get(it)?.source }
                        if (chapter != null && source != null) {
                            setBundle(book, chapter)
                            state.ttsBook = book
                            state.ttsChapter = chapter
                            state.ttsChapters = chapters
                            state.ttsSource = source
                            startService(command)
                        }
                    }
                }
            }
            null -> {}
            else -> Log.error { "Unknown Intent $intent" }
        }
        return super.onStartCommand(intent, flags, startId)
    }


    override fun onDestroy() {
        player.shutdown()
        Log.error { "onDestroy" }
        super.onDestroy()
    }

    /**
     * set book info in meta date so thet we could get them directly
     * in notifications
     */
    private fun setBundle(book: Book, chapter: Chapter) {
        val data = MediaMetadataCompat.Builder()
            .apply {
                putText(NOVEL_TITLE, book.title)
                putLong(NOVEL_ID, book.id)
                putLong(FAVORITE, if (book.favorite) 1 else 0)
                putLong(SOURCE_ID, book.sourceId)
                putText(CHAPTER_TITLE, chapter.title)
                putLong(CHAPTER_ID, chapter.id)
                putText(NOVEL_COVER, book.cover)
                putLong(PROGRESS, state.currentReadingParagraph.toLong())
                putLong(LAST_PARAGRAPH, state.ttsContent?.value?.lastIndex?.toLong()?:1L)
                putText(MediaMetadata.METADATA_KEY_TITLE, chapter.title)
                putText(MediaMetadata.METADATA_KEY_AUTHOR, book.author)
            }.build()
        mediaSession.setMetadata(data)
    }


    private inner class TTSSessionCallback : MediaSessionCompat.Callback() {

        override fun onPlay() {
            Log.error { "onPlay" }
            if (state.isPlaying) {
                startService(Player.PAUSE)
            } else {
                startService(Player.PLAY)
            }


        }

        override fun onStop() {
            Log.error { "onStop" }
            startService(Player.PAUSE)
        }


        override fun onRewind() {
            Log.error { "onRewind" }
            startService(Player.PREV_PAR)

        }

        override fun onFastForward() {
            Log.error { "onFastForward" }
            startService(Player.NEXT_PAR)

        }


        override fun onSkipToNext() {
            Log.error { "onSkipToNext" }
            startService(Player.SKIP_NEXT)

        }

        override fun onSkipToPrevious() {
            Log.error { "onSkipToPrevious" }
            startService(Player.SKIP_PREV)

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
                Log.error { "onPlaybackStateChanged $state" }
                scope.launch {
                    notificationManager.notify(Notifications.ID_TTS,
                        ttsNotificationBuilder.buildTTSNotification(mediaSession).build())
                }
            }
        }
    }

    private fun setPlaybackState(state: Int) {
        // Playback speed is set to 0 because our duration is faked one, and we update position manually
        // instead of relying on MediaSession doing it for us.
        mediaSession.setPlaybackState(stateBuilder.setState(state, this.state.currentReadingParagraph.toLong(), 0.0f).build())
    }

    suspend fun updateNotification(
        chapter: Chapter,
        book: Book,
        error: Boolean = false,
        isLoading: Boolean? = null,
    ) {
        mediaSession.let { mediaSession ->
            NotificationManagerCompat.from(this).apply {
                val builder =
                    ttsNotificationBuilder.buildTTSNotification(
                        mediaSession,
                        isLoading = isLoading ?: state.ttsIsLoading,
                        isError = error
                    )

                notify(Notifications.ID_TTS,
                    builder.build())
            }
        }
    }


    fun startService(command: Int) {
        serviceJob = scope.launch {
            try {
                player.let { tts ->
                    mediaSession.let { mediaSession ->
                        state.ttsChapter?.let { chapter ->
                            state.ttsSource?.let { source ->
                                state.ttsChapters.let { chapters ->
                                    state.ttsBook?.let { book ->
                                        setBundle(book, chapter)
                                        val notification =
                                            ttsNotificationBuilder.buildTTSNotification(
                                                mediaSession,
                                                isLoading = state.ttsIsLoading
                                            ).build()

                                        NotificationManagerCompat.from(this@TTSService)
                                            .notify(Notifications.ID_TTS,
                                                notification)


                                        when (command) {
                                            Player.CANCEL -> {
                                                NotificationManagerCompat.from(this@TTSService)
                                                    .cancel(Notifications.ID_TTS)
                                            }
                                            Player.SKIP_PREV -> {
                                                tts.stop()
                                                updateNotification(chapter, book)
                                                val index = getChapterIndex(chapter, chapters)
                                                if (index > 0) {
                                                    val id = chapters[index - 1].id
                                                    getRemoteChapter(chapterId = id,
                                                        source,
                                                        state) {
                                                        if (state.isPlaying) {
                                                            readText(this@TTSService, mediaSession)
                                                        }
                                                    }
                                                }
                                                state.currentReadingParagraph = 0
                                                updateNotification(chapter, book)
                                                if (state.isPlaying) {
                                                    readText(this@TTSService, mediaSession)
                                                } else {

                                                }
                                            }
                                            Player.PREV_PAR -> {
                                                state.ttsContent?.value?.let { content ->
                                                    if (state.currentReadingParagraph > 0 && state.currentReadingParagraph in 0..content.lastIndex) {
                                                        state.currentReadingParagraph -= 1
                                                        updateNotification(chapter, book)
                                                        if (state.isPlaying) {
                                                            readText(this@TTSService, mediaSession)
                                                        }
                                                    }
                                                }
                                            }
                                            Player.SKIP_NEXT -> {
                                                tts.stop()
                                                updateNotification(chapter, book)
                                                val index = getChapterIndex(chapter, chapters)
                                                if (index != chapters.lastIndex) {
                                                    val id = chapters[index + 1].id
                                                    getRemoteChapter(chapterId = id,
                                                        source,
                                                        state) {
                                                        if (state.isPlaying) {
                                                            readText(this@TTSService, mediaSession)
                                                        }
                                                    }
                                                }
                                                state.currentReadingParagraph = 0
                                                updateNotification(chapter, book)
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
                                                            updateNotification(chapter, book)
                                                        }
                                                    }
                                                }


                                            }
                                            Player.PLAY -> {
                                                setPlaybackState(PlaybackStateCompat.STATE_PLAYING)
                                                state.isPlaying = true
                                                readText(this@TTSService, mediaSession)
                                            }
                                            Player.PAUSE -> {
                                                setPlaybackState(PlaybackStateCompat.STATE_PAUSED)
                                                state.isPlaying = false
                                                tts.stop()
                                                updateNotification(chapter, book)
                                            }
                                            Player.PLAY_PAUSE -> {
                                                if (state.isPlaying) {
                                                    setPlaybackState(PlaybackStateCompat.STATE_PAUSED)
                                                    state.isPlaying = false
                                                    tts.stop()
                                                    updateNotification(chapter, book)
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
                                                    updateNotification(chapter, book)
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
                                updateNotification(chapter, book, error = true)
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
                player.voices?.firstOrNull { it.name == currentVoice }?.let {
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

                            NotificationManagerCompat.from(context).apply {
                                scope.launch {
                                    val notification = ttsNotificationBuilder.buildTTSNotification(
                                        mediaSessionCompat,
                                        isLoading = state.ttsIsLoading
                                    )

                                    notify(Notifications.ID_TTS,
                                        notification.build())
                                }
                                if (state.utteranceId != (currentReadingParagraph).toString()) {
                                    player.speak(content[currentReadingParagraph],
                                        TextToSpeech.QUEUE_FLUSH,
                                        null,
                                        currentReadingParagraph.toString())
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


                                            if (currentReadingParagraph == content.lastIndex && !ttsIsLoading && isFinished) {
                                                isPlaying = false
                                                currentReadingParagraph = 0

                                                player.stop()
                                                if (autoNextChapter) {
                                                    scope.launch {
                                                        state.ttsChapters.let { chapters ->
                                                            state.ttsSource?.let { source ->
                                                                val index =
                                                                    getChapterIndex(chapter,
                                                                        chapters)
                                                                if (index != chapters.lastIndex) {
                                                                    val id = chapters[index + 1].id
                                                                    getRemoteChapter(chapterId = id,
                                                                        source,
                                                                        state) {
                                                                        state.currentReadingParagraph =
                                                                            0
                                                                        updateNotification(chapter,
                                                                            book)

                                                                        mediaSession.let { mediaSession ->
                                                                            readText(context,
                                                                                mediaSession)
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
                                        super.onBeginSynthesis(utteranceId,
                                            sampleRateInHz,
                                            audioFormat,
                                            channelCount)
                                    }
                                })
                            }
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
        source: Source,
        ttsState: TTSState,
        onSuccess: suspend () -> Unit,
    ) {
        try {


            ttsState.ttsChapter?.let { chapter ->
                ttsState.ttsBook?.let { book ->
                    updateNotification(chapter = chapter, book = book, isLoading = true)
                }
            }

            ttsState.ttsIsLoading = true
            state.currentReadingParagraph = 0
            val localChapter = chapterRepo.findChapterById(chapterId)

            if (localChapter?.isChapterNotEmpty() == true) {
                state.ttsChapter = localChapter
                state.currentReadingParagraph = 0
                ttsState.ttsIsLoading = false
                ttsState.ttsBook?.let { book ->
                    updateNotification(chapter = localChapter, book = book)

                }
                insertUseCases.insertChapter(localChapter.copy(
                    read = true,
                    readAt = Clock.System.now()
                        .toEpochMilliseconds(),
                ))
                onSuccess()
            } else {
                if (localChapter != null) {
                    remoteUseCases.getRemoteReadingContent(
                        chapter = localChapter,
                        source,
                        onSuccess = { result ->
                            if (result.content.joinToString().length > 1) {
                                state.ttsChapter = result
                                insertUseCases.insertChapter(result.copy(
                                    dateFetch = Clock.System.now()
                                        .toEpochMilliseconds(),
                                    read = true,
                                    readAt = Clock.System.now()
                                        .toEpochMilliseconds(),
                                ))
                                chapterRepo.findChaptersByBookId(result.bookId).let { res ->
                                    state.ttsChapters = res
                                }
                                state.currentReadingParagraph = 0
                                ttsState.ttsIsLoading = false
                                ttsState.ttsBook?.let { book ->
                                    updateNotification(chapter = localChapter, book = book)
                                }
                                onSuccess()
                            }
                        }, onError = {
                            ttsState.ttsIsLoading = false

                        }
                    )
                }
            }
            ttsState.ttsIsLoading = false
        } catch (e: java.lang.Exception) {
            Log.error { e.stackTrace.toString() }
            ttsState.ttsChapter?.let { chapter ->
                ttsState.ttsBook?.let { book ->
                    updateNotification(chapter = chapter,
                        book = book,
                        isLoading = false,
                        error = true)
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

}