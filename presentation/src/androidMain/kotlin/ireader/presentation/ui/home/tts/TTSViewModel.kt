package ireader.presentation.ui.home.tts

import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import ireader.domain.catalogs.interactor.GetLocalCatalog
import ireader.domain.models.entities.Chapter
import ireader.domain.preferences.prefs.AppPreferences
import ireader.domain.preferences.prefs.PlatformUiPreferences
import ireader.domain.preferences.prefs.ReaderPreferences
import ireader.domain.services.tts_service.Player
import ireader.domain.services.tts_service.TTSStateImpl
import ireader.domain.services.tts_service.media_player.TTSService
import ireader.domain.services.tts_service.media_player.isPlaying
import ireader.domain.usecases.preferences.TextReaderPrefUseCase
import ireader.domain.usecases.remote.RemoteUseCases
import ireader.domain.usecases.services.ServiceUseCases
import ireader.domain.utils.findComponentActivity
import kotlinx.coroutines.launch

class TTSViewModel(
    val ttsState: TTSStateImpl,
    private val param: Param,
    private val serviceUseCases: ServiceUseCases,
    private val getBookUseCases: ireader.domain.usecases.local.LocalGetBookUseCases,
    private val getChapterUseCase: ireader.domain.usecases.local.LocalGetChapterUseCase,
    private val remoteUseCases: RemoteUseCases,
    private val getLocalCatalog: GetLocalCatalog,
    val speechPrefUseCases: TextReaderPrefUseCase,
    private val readerPreferences: ReaderPreferences,
    private val androidUiPreferences: AppPreferences,
    private val insertUseCases: ireader.domain.usecases.local.LocalInsertUseCases,
    private val platformUiPreferences: PlatformUiPreferences,
    private val aiTTSManager: ireader.domain.services.tts.AITTSManager,
) : ireader.presentation.ui.core.viewmodel.BaseViewModel(),
    ireader.domain.services.tts_service.AndroidTTSState by ttsState {
    
    data class Param(
        val sourceId: Long?,
        val chapterId: Long?,
        val bookId: Long?,
        val readingParagraph: Int?
    )


    // UI State
    var fullScreenMode by mutableStateOf(false)

    // TTS Preferences
    val speechRate = readerPreferences.speechRate().asState()
    val speechPitch = readerPreferences.speechPitch().asState()
    val sleepModeUi = readerPreferences.sleepMode().asState()
    val sleepTimeUi = readerPreferences.sleepTime().asState()
    
    // Coqui TTS State
    var useCoquiTTS by mutableStateOf(false)
        private set
    
    init {
        // Load Coqui TTS preference
        useCoquiTTS = androidUiPreferences.useCoquiTTS().get()
        
        // Configure Coqui TTS if enabled
        if (useCoquiTTS) {
            configureCoquiTTS()
        }
    }
    
    private fun configureCoquiTTS() {
        scope.launch {
            try {
                val spaceUrl = androidUiPreferences.coquiSpaceUrl().get()
                if (spaceUrl.isNotEmpty()) {
                    aiTTSManager.configureCoqui(spaceUrl)
                    ireader.core.log.Log.info { "✅ Coqui TTS configured: $spaceUrl" }
                }
            } catch (e: Exception) {
                ireader.core.log.Log.error { "❌ Failed to configure Coqui TTS: ${e.message}" }
            }
        }
    }
    
    fun toggleTTSEngine() {
        useCoquiTTS = !useCoquiTTS
        androidUiPreferences.useCoquiTTS().set(useCoquiTTS)
        
        if (useCoquiTTS) {
            configureCoquiTTS()
        }
        
        ireader.core.log.Log.info { "TTS Engine switched to: ${if (useCoquiTTS) "Coqui TTS" else "Native TTS"}" }
    }
    
    var isCoquiLoading by mutableStateOf(false)
        private set
    
    fun speakWithCoqui(text: String) {
        // Launch in IO dispatcher to avoid blocking UI
        scope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                // Update loading state on Main dispatcher
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    isCoquiLoading = true
                }
                
                ireader.core.log.Log.info { "🎙️ Starting Coqui TTS synthesis..." }
                
                val speed = androidUiPreferences.coquiSpeed().get()
                
                aiTTSManager.synthesizeAndPlay(
                    text = text,
                    provider = ireader.domain.services.tts.AITTSProvider.COQUI_TTS,
                    voiceId = "default",
                    speed = speed
                ).onSuccess {
                    ireader.core.log.Log.info { "✅ Coqui TTS playing" }
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                        isCoquiLoading = false
                    }
                }.onFailure { error ->
                    ireader.core.log.Log.error { "❌ Coqui TTS failed: ${error.message}" }
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                        isCoquiLoading = false
                    }
                }
            } catch (e: Exception) {
                ireader.core.log.Log.error { "❌ Coqui TTS error: ${e.message}" }
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                    isCoquiLoading = false
                }
            }
        }
    }
    
    private fun advanceToNextParagraph() {
        scope.launch {
            try {
                val content = ttsContent?.value ?: return@launch
                val currentParagraph = ttsState.currentReadingParagraph
                val nextParagraph = currentParagraph + 1
                
                if (nextParagraph < content.size && isCoquiLoading) {
                    // Move to next paragraph
                    ttsState.currentReadingParagraph = nextParagraph
                    
                    // Speak next paragraph
                    content.getOrNull(nextParagraph)?.let { nextText ->
                        speakWithCoqui(nextText)
                    }
                } else if (isCoquiLoading) {
                    // Reached end of chapter - check if auto-next is enabled
                    val autoNext = readerPreferences.readerAutoNext().get()
                    
                    if (autoNext) {
                        ireader.core.log.Log.info { "End of chapter - loading next chapter..." }
                        // Move to next chapter
                        controller?.transportControls?.skipToNext()
                        // Wait a bit for chapter to load, then continue
                        kotlinx.coroutines.delay(1000)
                        // Start reading from beginning of new chapter
                        ttsContent?.value?.getOrNull(0)?.let { firstParagraph ->
                            speakWithCoqui(firstParagraph)
                        }
                    } else {
                        // Stop at end of chapter
                        isCoquiLoading = false
                        ireader.core.log.Log.info { "Coqui TTS finished reading chapter" }
                    }
                } else {
                    // Stopped by user
                    isCoquiLoading = false
                }
            } catch (e: Exception) {
                ireader.core.log.Log.error { "Failed to advance paragraph: ${e.message}" }
                isCoquiLoading = false
            }
        }
    }
    
    fun stopCoquiTTS() {
        isCoquiLoading = false
        aiTTSManager.stopPlayback()
    }
    val autoNext = readerPreferences.readerAutoNext().asState()
    val voice = androidUiPreferences.speechVoice().asState()
    val language = readerPreferences.speechLanguage().asState()
    val isTtsTrackerEnable = readerPreferences.followTTSSpeaker().asState()
    val theme = androidUiPreferences.backgroundColorTTS().asState()

    // Text Display Preferences
    val lineHeight = readerPreferences.lineHeight().asState()
    val betweenLetterSpaces = readerPreferences.betweenLetterSpaces().asState()
    val textWeight = readerPreferences.textWeight().asState()
    val paragraphsIndent = readerPreferences.paragraphIndent().asState()
    val paragraphDistance = readerPreferences.paragraphDistance().asState()
    val textAlignment = readerPreferences.textAlign().asState()
    val font = platformUiPreferences.font()?.asState()
    val fontSize = readerPreferences.fontSize().asState()
    val ttsIconAlignments = readerPreferences.ttsIconAlignments().asState()

    // Media Controller - using @Volatile for thread-safe visibility
    @Volatile
    var controller: MediaControllerCompat? = null
    
    @Volatile
    private var browser: MediaBrowserCompat? = null
    
    @Volatile
    private var ctrlCallback: TTSController? = null
    
    @Volatile
    private var textReader: TextToSpeech? = null
    
    override var isServiceConnected = false
    private var initialize = false
    
    // Lock object for synchronizing media controller operations
    private val mediaLock = Any()

    init {
        initializeFromParams()
    }

    private fun initializeFromParams() {
        val (sourceId, chapterId, bookId, readingParagraph) = param
        
        readingParagraph?.let { paragraph ->
            val maxIndex = ttsState.ttsContent?.value?.lastIndex ?: 0
            // Fixed: Use <= instead of < to allow setting the last paragraph
            // lastIndex is inclusive, so paragraph can equal maxIndex
            if (paragraph <= maxIndex) {
                ttsState.currentReadingParagraph = paragraph
            }
        }

        if (sourceId != null && chapterId != null && bookId != null) {
            ttsCatalog = getLocalCatalog.get(sourceId)
            
            scope.launch {
                ttsBook = getBookUseCases.findBookById(bookId)
                getLocalChapter(chapterId)
                subscribeChapters(bookId)
                readPreferences()
                
                if (ttsChapter?.id != chapterId) {
                    runTTSService(Player.PAUSE)
                }
                initialize = true
            }
        }
    }

    private fun readPreferences() {
        scope.launch {
            speechSpeed = speechPrefUseCases.readRate()
            pitch = speechPrefUseCases.readPitch()
            currentLanguage = speechPrefUseCases.readLanguage()
            autoNextChapter = speechPrefUseCases.readAutoNext()
            currentVoice = speechPrefUseCases.readVoice()
        }
    }

    fun initMedia(context: Context) {
        initMediaBrowser(context)
        initTextToSpeech(context)
    }

    private fun initMediaBrowser(context: Context) {
        synchronized(mediaLock) {
            val currentBrowser = browser
            if (currentBrowser == null) {
                browser = MediaBrowserCompat(
                    context,
                    ComponentName(context, TTSService::class.java),
                    createConnectionCallback(context),
                    null
                ).apply {
                    connect()
                }
            } else if (!currentBrowser.isConnected) {
                // Only connect if not already connected or connecting
                currentBrowser.connect()
            }
        }
    }

    private fun initTextToSpeech(context: Context) {
        synchronized(mediaLock) {
            if (textReader == null) {
                textReader = TextToSpeech(context) { status ->
                    if (status == TextToSpeech.SUCCESS) {
                        runCatching {
                            // Use local reference to avoid race condition
                            val reader = textReader
                            ttsState.languages = reader?.availableLanguages?.toList() ?: emptyList()
                            ttsState.voices = reader?.voices?.toList() ?: emptyList()
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        synchronized(mediaLock) {
            runCatching {
                // Clean up in reverse order of initialization
                cleanupMediaController()
                
                // Additional cleanup for resources not in cleanupMediaController
                browser?.disconnect()
                browser = null
                
                textReader?.shutdown()
                textReader = null
            }
        }
        super.onDestroy()
    }

    private fun createConnectionCallback(context: Context) = object : MediaBrowserCompat.ConnectionCallback() {
        override fun onConnected() {
            synchronized(mediaLock) {
                isServiceConnected = true
                browser?.sessionToken?.let { token ->
                    controller = MediaControllerCompat(context, token)
                    context.findComponentActivity()?.let { activity ->
                        MediaControllerCompat.setMediaController(activity, controller)
                    }
                    initController()
                }
            }
        }

        override fun onConnectionSuspended() {
            cleanupMediaController()
        }

        override fun onConnectionFailed() {
            synchronized(mediaLock) {
                isServiceConnected = false
            }
        }
    }

    private fun cleanupMediaController() {
        synchronized(mediaLock) {
            isServiceConnected = false
            
            // Fixed: Check controller is not null before unregistering callback
            val currentController = controller
            val currentCallback = ctrlCallback
            
            if (currentController != null && currentCallback != null) {
                runCatching {
                    currentController.unregisterCallback(currentCallback)
                }
            }
            
            // Clear references
            ctrlCallback = null
            controller = null
        }
    }

    private fun initController() {
        synchronized(mediaLock) {
            // Use local reference to avoid race condition
            val currentController = controller
            
            if (currentController != null) {
                ctrlCallback = TTSController().apply {
                    currentController.registerCallback(this)
                    onMetadataChanged(currentController.metadata)
                    onPlaybackStateChanged(currentController.playbackState)
                }
            }
        }
    }

    private inner class TTSController : MediaControllerCompat.Callback() {

        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            super.onMetadataChanged(metadata)
            meta = metadata
            if (metadata == null || !initialize) return
            
            updateFromMetadata(metadata)
        }

        private fun updateFromMetadata(metadata: MediaMetadataCompat) {
            val novelId = metadata.getLong(TTSService.NOVEL_ID)
            val currentParagraph = metadata.getLong(TTSService.PROGRESS)
            val chapterId = metadata.getLong(TTSService.CHAPTER_ID)
            
            if (novelId != -1L && ttsBook?.id != novelId) {
                scope.launch {
                    ttsBook = getBookUseCases.findBookById(novelId)
                }
            }
            
            if (currentParagraph != -1L && currentParagraph != currentReadingParagraph.toLong()) {
                currentReadingParagraph = currentParagraph.toInt()
            }
            
            if (chapterId != -1L && chapterId != ttsChapter?.id) {
                scope.launch {
                    ttsChapter = getChapterUseCase.findChapterById(chapterId)
                }
            }
        }

        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            super.onPlaybackStateChanged(state)
            isPlaying = state?.state == PlaybackStateCompat.STATE_PLAYING
        }
    }

    fun runTTSService(command: Int = -1) {
        serviceUseCases.startTTSServicesUseCase(
            chapterId = ttsChapter?.id,
            bookId = ttsBook?.id,
            command = command
        )
    }

    // Public method to load a chapter
    fun loadChapter(chapterId: Long) {
        getLocalChapter(chapterId)
    }

    private fun getLocalChapter(chapterId: Long) {
        scope.launch {
            getChapterUseCase.findChapterById(chapterId)?.let { chapter ->
                ttsChapter = chapter
                if (chapter.isEmpty()) {
                    ttsSource?.let { getRemoteChapter(chapter) }
                }
                runTTSService(Player.PAUSE)
            }
        }
    }

    private fun subscribeChapters(bookId: Long) {
        scope.launch {
            getChapterUseCase.subscribeChaptersByBookId(bookId).collect { chapters ->
                ttsChapters = chapters
            }
        }
    }

    private suspend fun getRemoteChapter(chapter: Chapter) {
        ttsState.ttsCatalog?.let { catalog ->
            remoteUseCases.getRemoteReadingContent(
                chapter,
                catalog,
                onSuccess = { result ->
                    ttsChapter = result
                    insertUseCases.insertChapter(result)
                },
                onError = { error ->
                    // Log error for debugging instead of silently swallowing it
                    ireader.core.log.Log.error { 
                        "Failed to fetch remote chapter content for chapter ${chapter.id}: ${error}" 
                    }
                }
            )
        }
    }

    fun play(context: Context) {
        // Use local reference to avoid race condition
        val currentController = controller
        
        when {
            currentController?.playbackState?.state == PlaybackStateCompat.STATE_NONE -> {
                initMedia(context)
                initController()
                runTTSService(Player.PLAY)
            }
            currentController?.playbackState?.isPlaying == true -> {
                currentController.transportControls?.pause()
            }
            else -> {
                currentController?.transportControls?.play()
            }
        }
    }
}
