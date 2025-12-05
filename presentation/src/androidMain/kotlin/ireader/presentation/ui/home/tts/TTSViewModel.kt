package ireader.presentation.ui.home.tts

import android.content.ComponentName
import android.content.Context
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
    private val getAllTranslationsForChapterUseCase: ireader.domain.usecases.translation.GetAllTranslationsForChapterUseCase,
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
    
    // Gradio TTS State
    var useGradioTTS by mutableStateOf(false)
        private set
    
    val useGradioTTSState: androidx.compose.runtime.State<Boolean>
        get() = androidx.compose.runtime.derivedStateOf { useGradioTTS }
    
    // Translation State
    var showTranslatedText by mutableStateOf(false)
        private set
    
    var bilingualMode by mutableStateOf(false)
        private set
    
    /**
     * Toggle between showing original and translated text
     * Also updates the TTS service to read the appropriate content
     */
    fun toggleTranslation() {
        showTranslatedText = !showTranslatedText
        // Save preference - this will be picked up by TTS service's getCurrentContent()
        scope.launch {
            readerPreferences.useTTSWithTranslatedText().set(showTranslatedText)
        }
        // Restart current paragraph to use new content source
        updateTTSContent()
    }
    
    /**
     * Toggle bilingual mode (show both original and translated)
     */
    fun toggleBilingualMode() {
        bilingualMode = !bilingualMode
    }
    
    /**
     * Update the TTS service content based on translation toggle
     * Restarts the current paragraph to use the new content source (original or translated)
     */
    private fun updateTTSContent() {
        val wasPlaying = ttsState.isPlaying.value
        if (wasPlaying) {
            // Stop current playback
            controller?.transportControls?.pause()
            // Small delay to ensure preference is saved, then resume
            scope.launch {
                kotlinx.coroutines.delay(100)
                controller?.transportControls?.play()
            }
        }
    }
    
    /**
     * Set translated content for TTS
     * This can be called from external translation services
     */
    fun setTranslatedContent(content: List<String>?) {
        ttsState.setTranslatedTTSContent(content)
    }
    
    /**
     * Check if translation is available
     */
    fun hasTranslation(): Boolean {
        val content = ttsState.translatedTTSContent.value
        return content != null && content.isNotEmpty()
    }
    
    /**
     * Get the content to be read by TTS (respects translation toggle)
     */
    fun getContentForTTS(): List<String> {
        val translatedContent = ttsState.translatedTTSContent.value
        return if (showTranslatedText && translatedContent != null && translatedContent.isNotEmpty()) {
            translatedContent
        } else {
            ttsState.ttsContent.value ?: emptyList()
        }
    }
    
    /**
     * Load translated content for the current chapter
     */
    private fun loadTranslationForChapter(chapterId: Long) {
        scope.launch {
            try {
                val translations = getAllTranslationsForChapterUseCase.execute(chapterId)
                if (translations.isNotEmpty()) {
                    // Use the most recent translation
                    val latestTranslation = translations.maxByOrNull { it.updatedAt }
                    latestTranslation?.let { translation ->
                        // Convert Page list to String list for TTS
                        val translatedStrings = translation.translatedContent
                            .filterIsInstance<ireader.core.source.model.Text>()
                            .map { it.text }
                            .filter { it.isNotBlank() }
                        
                        if (translatedStrings.isNotEmpty()) {
                            ttsState.setTranslatedTTSContent(translatedStrings)
                            ireader.core.log.Log.debug { "Loaded ${translatedStrings.size} translated paragraphs for TTS" }
                        }
                    }
                } else {
                    // No translation available, clear any existing
                    ttsState.setTranslatedTTSContent(null)
                }
            } catch (e: Exception) {
                ireader.core.log.Log.error { "Failed to load translation for TTS: ${e.message}" }
                ttsState.setTranslatedTTSContent(null)
            }
        }
    }
    
    init {
        // Load Gradio TTS preference
        useGradioTTS = androidUiPreferences.useGradioTTS().get()
        
        // Configure Gradio TTS if enabled
        if (useGradioTTS) {
            configurGradioTTS()
        }
        
        // Load translation preference
        showTranslatedText = readerPreferences.useTTSWithTranslatedText().get()
    }
    
    private fun configurGradioTTS() {
        scope.launch {
            try {
                val spaceUrl = androidUiPreferences.activeGradioConfigId().get()
                if (spaceUrl.isNotEmpty()) {
                    aiTTSManager.configureGradio(spaceUrl)
                }
            } catch (e: Exception) {
                ireader.core.log.Log.error { "Failed to configure Gradio TTS: ${e.message}" }
            }
        }
    }
    
    fun toggleTTSEngine() {
        useGradioTTS = !useGradioTTS
        androidUiPreferences.useGradioTTS().set(useGradioTTS)
        
        if (useGradioTTS) {
            configurGradioTTS()
        }
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
    
    private var initialize = false
    
    // Lock object for synchronizing media controller operations
    private val mediaLock = Any()

    init {
        initializeFromParams()
    }

    private fun initializeFromParams() {
        val (sourceId, chapterId, bookId, readingParagraph) = param
        
        readingParagraph?.let { paragraph ->
            val maxIndex = ttsState.ttsContent.value?.lastIndex ?: 0
            // Fixed: Use <= instead of < to allow setting the last paragraph
            // lastIndex is inclusive, so paragraph can equal maxIndex
            if (paragraph <= maxIndex) {
                ttsState.setCurrentReadingParagraph(paragraph)
            }
        }

        if (sourceId != null && chapterId != null && bookId != null) {
            ttsState.setTtsCatalog(getLocalCatalog.get(sourceId))
            
            scope.launch {
                // Load book FIRST before loading chapter
                if (ttsState.ttsBook.value == null || ttsState.ttsBook.value?.id != bookId) {
                    ttsState.setTtsBook(getBookUseCases.findBookById(bookId))
                }
                
                // Now load chapter (this will call runTTSService which needs ttsBook)
                if (ttsState.ttsChapter.value == null || ttsState.ttsChapter.value?.id != chapterId) {
                    getLocalChapter(chapterId)
                }
                
                subscribeChapters(bookId)
                readPreferences()
                
                if (ttsState.ttsChapter.value?.id != chapterId) {
                    runTTSService(Player.PAUSE)
                }
                initialize = true
            }
        }
    }

    private fun readPreferences() {
        scope.launch {
            ttsState.setSpeechSpeed(speechPrefUseCases.readRate())
            ttsState.setPitch(speechPrefUseCases.readPitch())
            ttsState.setCurrentLanguage(speechPrefUseCases.readLanguage())
            ttsState.setAutoNextChapter(speechPrefUseCases.readAutoNext())
            ttsState.setCurrentVoice(speechPrefUseCases.readVoice())
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
                ttsState.setServiceConnected(true)
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
                ttsState.setServiceConnected(false)
            }
        }
    }

    private fun cleanupMediaController() {
        synchronized(mediaLock) {
            ttsState.setServiceConnected(false)
            
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
            
            if (novelId != -1L && ttsState.ttsBook.value?.id != novelId) {
                scope.launch {
                    getBookUseCases.findBookById(novelId)?.let { book ->
                        ttsState.setTtsBook(book)
                    }
                }
            }
            
            if (currentParagraph != -1L && currentParagraph != ttsState.currentReadingParagraph.value.toLong()) {
                ttsState.setCurrentReadingParagraph(currentParagraph.toInt())
            }
            
            if (chapterId != -1L && chapterId != ttsState.ttsChapter.value?.id) {
                scope.launch {
                    getChapterUseCase.findChapterById(chapterId)?.let { chapter ->
                        ttsState.setTtsChapter(chapter)
                    }
                }
            }
        }

        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            super.onPlaybackStateChanged(state)
            ttsState.setPlaying(state?.state == PlaybackStateCompat.STATE_PLAYING)
        }
    }

    fun runTTSService(command: Int = -1) {
        serviceUseCases.startTTSServicesUseCase(
            chapterId = ttsState.ttsChapter.value?.id,
            bookId = ttsState.ttsBook.value?.id,
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
                ttsState.setTtsChapter(chapter)
                if (chapter.isEmpty()) {
                    ttsState.ttsSource.value?.let { getRemoteChapter(chapter) }
                }
                // Load translation for this chapter
                loadTranslationForChapter(chapterId)
                runTTSService(Player.PAUSE)
            }
        }
    }

    private fun subscribeChapters(bookId: Long) {
        scope.launch {
            getChapterUseCase.subscribeChaptersByBookId(bookId).collect { chapters ->
                ttsState.setTtsChapters(chapters)
            }
        }
    }

    private suspend fun getRemoteChapter(chapter: Chapter) {
        ttsState.ttsCatalog.value?.let { catalog ->
            remoteUseCases.getRemoteReadingContent(
                chapter,
                catalog,
                onSuccess = { result ->
                    ttsState.setTtsChapter(result)
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
