package ireader.domain.services.tts_service

import ireader.core.log.Log
import ireader.domain.catalogs.CatalogStore
import ireader.domain.data.repository.BookRepository
import ireader.domain.data.repository.ChapterRepository
import ireader.domain.models.entities.Chapter
import ireader.domain.preferences.prefs.AppPreferences
import ireader.domain.preferences.prefs.ReaderPreferences
import ireader.domain.services.tts_service.piper.PiperException
import ireader.domain.usecases.local.LocalGetChapterUseCase
import ireader.domain.usecases.remote.RemoteUseCases
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.time.Duration.Companion.minutes
import kotlin.time.ExperimentalTime

/**
 * Desktop TTS Service
 * Uses a simple word-by-word simulation for TTS
 * For production, consider integrating with FreeTTS, MaryTTS, or system TTS
 */
class DesktopTTSService : KoinComponent {
    private val bookRepo: BookRepository by inject()
    private val chapterRepo: ChapterRepository by inject()
    private val chapterUseCase: LocalGetChapterUseCase by inject()
    private val remoteUseCases: RemoteUseCases by inject()
    private val extensions: CatalogStore by inject()
    private val readerPreferences: ReaderPreferences by inject()
    val appPrefs: AppPreferences by inject()
    private val getTranslatedChapterUseCase: ireader.domain.usecases.translation.GetTranslatedChapterUseCase by inject()
    
    // Piper TTS components
    val synthesizer: ireader.domain.services.tts_service.piper.PiperSpeechSynthesizer by inject()
    private val audioEngine: ireader.domain.services.tts_service.piper.AudioPlaybackEngine by inject()
    private val modelManager: ireader.domain.services.tts_service.piper.PiperModelManager by inject()
    
    // Kokoro TTS components (optional)
    private val kokoroEngine: ireader.domain.services.tts_service.kokoro.KokoroTTSEngine by lazy {
        val maxProcesses = appPrefs.maxConcurrentTTSProcesses().get()
        ireader.domain.services.tts_service.kokoro.KokoroTTSEngine(
            maxConcurrentProcesses = maxProcesses
        )
    }
    val kokoroAdapter: ireader.domain.services.tts_service.kokoro.KokoroTTSAdapter by lazy {
        ireader.domain.services.tts_service.kokoro.KokoroTTSAdapter(kokoroEngine)
    }
    
    // Maya TTS components (optional)
    private val mayaEngine: ireader.domain.services.tts_service.maya.MayaTTSEngine by lazy {
        ireader.domain.services.tts_service.maya.MayaTTSEngine()
    }
    val mayaAdapter: ireader.domain.services.tts_service.maya.MayaTTSAdapter by lazy {
        ireader.domain.services.tts_service.maya.MayaTTSAdapter(mayaEngine)
    }
    
    // TTS Engine selection
    private var currentEngine: TTSEngine = TTSEngine.PIPER
    var kokoroAvailable = false
    var mayaAvailable = false

    lateinit var state: DesktopTTSState
    private var serviceJob: Job? = null
    private var speechJob: Job? = null
    private var wordBoundaryJob: Job? = null
    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var isSimulationMode = false
    
    // Monitoring and analytics
    private val performanceMonitor = ireader.domain.services.tts_service.piper.PerformanceMonitor()
    private val usageAnalytics = ireader.domain.services.tts_service.piper.UsageAnalytics(
        privacyMode = ireader.domain.services.tts_service.piper.PrivacyMode.BALANCED
    )
    
    // Audio caching for faster playback
    private val audioCache = TTSAudioCache(
        cacheDir = java.io.File(ireader.core.storage.AppDir, "tts_cache"),
        maxCacheSizeMB = 500
    )
    
    // Chapter audio downloader
    val chapterDownloader = ChapterAudioDownloader(
        audioDir = java.io.File(ireader.core.storage.AppDir, "chapter_audio")
    )
    
    // Process tracker for managing child processes
    private val processTracker = ProcessTracker()
    private var cleanupJob: Job? = null
    
    // Track the paragraph index when speechJob started to detect manual navigation
    @Volatile
    private var speechJobStartParagraph: Int = -1

    companion object {
        const val TTS_SERVICE_NAME = "DESKTOP_TTS_SERVICE"
        const val ACTION_STOP = "actionStop"
        const val ACTION_PAUSE = "actionPause"
        const val ACTION_PLAY = "actionPlay"
        const val ACTION_NEXT = "actionNext"
        const val ACTION_PREVIOUS = "actionPrevious"
        const val ACTION_SKIP_NEXT = "actionSkipNext"
        const val ACTION_SKIP_PREV = "actionSkipPrev"
        const val ACTION_NEXT_PAR = "actionNextPar"
        const val ACTION_PREV_PAR = "actionPrevPar"
    }

    fun initialize() {
        state = DesktopTTSState()
        readPrefs()
        
        // Record session start for analytics
        usageAnalytics.recordSessionStart()
        
        // Start periodic cleanup task for zombie processes
        startProcessCleanupTask()
        
        // Initialize TTS engines
        serviceScope.launch {
            // Try to initialize Piper (always attempt)
            try {
                Log.info { "Initializing Piper TTS..." }
                // Load available and downloaded models
                loadAvailableModels()
                loadSelectedVoiceModel()
            } catch (e: UnsatisfiedLinkError) {
                Log.error { "Piper JNI library failed to load: ${e.message}" }
                enableSimulationMode(
                    "Piper TTS library failed to load. " +
                    "Please install Kokoro or Maya TTS from TTS Manager."
                )
            } catch (e: Exception) {
                Log.error { "Failed to load Piper: ${e.message}" }
                enableSimulationMode("Piper TTS not available. Please download a voice model or install Kokoro/Maya from TTS Manager.")
            }
            
            // Check if Kokoro is already installed (don't auto-install)
            try {
                val kokoroDir = java.io.File(ireader.core.storage.AppDir, "kokoro/kokoro-tts")
                
                // Only initialize if FULLY installed (repo exists AND dependencies installed)
                if (kokoroDir.exists() && kokoroDir.listFiles()?.isNotEmpty() == true) {
                    Log.info { "Found Kokoro repository, checking if fully installed..." }
                    
                    // Check if dependencies are installed without triggering installation
                    // Try to run kokoro --help to verify it's working
                    val pythonCheck = ProcessBuilder(
                        "python", "-m", "kokoro", "--help"
                    ).directory(kokoroDir)
                        .redirectErrorStream(true)
                        .start()
                    
                    val checkCompleted = pythonCheck.waitFor(5, java.util.concurrent.TimeUnit.SECONDS)
                    val output = pythonCheck.inputStream.bufferedReader().readText()
                    
                    if (checkCompleted && pythonCheck.exitValue() == 0 && output.contains("--voice")) {
                        kokoroAvailable = true
                        Log.info { "Kokoro TTS available (fully installed)" }
                    } else {
                        Log.info { "Kokoro repository found but not fully installed (user can complete installation from TTS Manager)" }
                    }
                } else {
                    Log.info { "Kokoro not installed (user must install from TTS Manager)" }
                }
            } catch (e: Exception) {
                Log.debug { "Kokoro check: ${e.message}" }
            }
            
            // Check if Maya is already installed (don't auto-install)
            try {
                val mayaDir = java.io.File(ireader.core.storage.AppDir, "maya")
                val mayaScript = java.io.File(mayaDir, "maya_tts.py")
                
                // Only initialize if the script already exists (meaning it was installed before)
                if (mayaScript.exists()) {
                    Log.info { "Found existing Maya installation, verifying..." }
                    
                    // Check if dependencies are installed without triggering installation
                    val pythonCheck = ProcessBuilder(
                        "python", "-c", 
                        "import torch; import transformers; import scipy; print('OK')"
                    ).redirectErrorStream(true).start()
                    
                    val checkCompleted = pythonCheck.waitFor(5, java.util.concurrent.TimeUnit.SECONDS)
                    if (checkCompleted && pythonCheck.exitValue() == 0) {
                        mayaAvailable = true
                        Log.info { "Maya TTS available (dependencies verified)" }
                    } else {
                        Log.info { "Maya script found but dependencies missing (user can reinstall from TTS Manager)" }
                    }
                } else {
                    Log.info { "Maya not installed (user must install from TTS Manager)" }
                }
            } catch (e: Exception) {
                Log.debug { "Maya check: ${e.message}" }
            }
            
            // If Piper failed, enable simulation mode
            if (!synthesizer.isInitialized()) {
                enableSimulationMode("Piper TTS not available. Please download a voice model or install Kokoro/Maya from TTS Manager.")
            }
            
            // Load Gradio TTS configuration from preferences (unified online TTS)
            try {
                val useGradio = appPrefs.useGradioTTS().get()
                val activeConfigId = appPrefs.activeGradioConfigId().get()
                
                Log.info { "Loading Gradio prefs: enabled=$useGradio, configId=$activeConfigId" }
                
                if (useGradio && activeConfigId.isNotEmpty()) {
                    configureGradioFromPreferences()
                    Log.info { "Gradio TTS configured from saved preferences: ${activeGradioConfig?.name}" }
                }
            } catch (e: Exception) {
                Log.error { "Failed to load Gradio preferences: ${e.message}" }
            }
        }
    }
    
    enum class TTSEngine {
        PIPER,
        KOKORO,
        MAYA,
        GRADIO,
        SIMULATION
    }
    
    // Gradio TTS components - using the new GradioTTSPlayer system
    private var gradioPlayer: ireader.domain.services.tts_service.player.GradioTTSPlayer? = null
    private var gradioHttpClient: io.ktor.client.HttpClient? = null
    private var gradioAudioPlayer: DesktopGradioAudioPlayer? = null
    private var gradioAdapter: GradioTTSEngineAdapter? = null  // Shared adapter for caching
    var gradioAvailable = false
    var activeGradioConfig: GradioTTSConfig? = null
    
    private suspend fun loadAvailableModels() {
        Log.info { "DesktopTTSService.loadAvailableModels() - START" }
        try {
            // Get all available models
            Log.info { "Calling modelManager.getAvailableModels()..." }
            val allModels = modelManager.getAvailableModels()
            Log.info { "modelManager returned ${allModels.size} models" }
            
            if (allModels.isEmpty()) {
                Log.warn { "No models returned from modelManager! Voice selection will be empty." }
            } else {
                Log.info { "First 3 models: ${allModels.take(3).map { it.id }}" }
            }
            
            // Get downloaded models from preferences (for quick lookup)
            val downloadedModelIds = appPrefs.downloadedModels().get()
            Log.info { "Downloaded model IDs from prefs: $downloadedModelIds" }
            
            // Mark models as downloaded based on preferences
            state.availableVoiceModels = allModels.map { model ->
                model.copy(isDownloaded = downloadedModelIds.contains(model.id))
            }
            Log.info { "Set state.availableVoiceModels to ${state.availableVoiceModels.size} models" }
            
            // Update selected model in state
            val selectedModelId = appPrefs.selectedPiperModel().get()
            if (selectedModelId.isNotEmpty()) {
                state.selectedVoiceModel = state.availableVoiceModels.find { it.id == selectedModelId }
                Log.info { "Selected model: ${state.selectedVoiceModel?.name ?: "not found"}" }
            }
            
            Log.info { "DesktopTTSService.loadAvailableModels() - END (success)" }
        } catch (e: Exception) {
            Log.error { "Failed to load available models: ${e.message}" }
            Log.error { "Exception type: ${e::class.simpleName}" }
            e.printStackTrace()
            Log.info { "DesktopTTSService.loadAvailableModels() - END (error)" }
        }
    }
    
    private suspend fun loadSelectedVoiceModel() {
        try {
            val selectedModelId = appPrefs.selectedPiperModel().get()
            if (selectedModelId.isNotEmpty()) {
                val paths = modelManager.getModelPaths(selectedModelId)
                if (paths != null) {
                    val result = synthesizer.initialize(paths.modelPath, paths.configPath)
                    
                    result.onSuccess {
                        isSimulationMode = false
                        applySavedTTSSettings()
                    }.onFailure { error ->
                        Log.error { "Failed to load Piper voice model: ${error.message}" }
                        enableSimulationMode("Failed to initialize Piper TTS: ${error.message}")
                    }
                } else {
                    enableSimulationMode("Voice model not found. Please download a voice model.")
                }
            } else {
                enableSimulationMode("No voice model selected. Using simulation mode.")
            }
        } catch (e: Exception) {
            Log.error { "Error loading voice model: ${e.message}" }
            enableSimulationMode("Error loading voice model: ${e.message}")
        }
    }
    
    private fun applySavedTTSSettings() {
        try {
            // Apply speech rate from preferences
            val speechRate = state.speechSpeed.value
            if (speechRate > 0 && speechRate in 0.5f..2.0f) {
                try {
                    synthesizer.setSpeechRate(speechRate)
                } catch (e: Exception) {
                    Log.error { "Failed to apply speech rate: ${e.message}" }
                }
            }
            
            // Note: Noise scale is not supported in subprocess mode
            // Piper's ONNX models have fixed quality characteristics
            
            // Note: Pitch and volume settings would be applied here if Piper supports them
            // Currently, Piper's ONNX models have fixed pitch and volume characteristics
            // These settings are stored in state but may not affect Piper synthesis
            
        } catch (e: Exception) {
            Log.error { "Error applying TTS settings: ${e.message}" }
        }
    }
    
    private fun enableSimulationMode(reason: String? = null) {
        isSimulationMode = true
    }
    
    /**
     * Start periodic cleanup task for zombie processes
     * 
     * This task runs every 5 minutes to detect and clean up zombie processes
     * from Piper, Kokoro, and Maya TTS engines.
     */
    private fun startProcessCleanupTask() {
        cleanupJob = serviceScope.launch {
            Log.info { "Starting process cleanup task (runs every 5 minutes)" }
            
            while (true) {
                try {
                    // Wait 5 minutes before next cleanup
                    delay(5.minutes)
                    
                    // Log current process status from engines
                    logEngineProcessStatus()
                    
                    // Log ProcessTracker status
                    processTracker.logStatus()
                    
                    // Detect and clean up zombie processes
                    val zombieCount = processTracker.cleanupZombieProcesses()
                    
                    if (zombieCount > 0) {
                        Log.warn { "Cleaned up $zombieCount zombie processes during periodic cleanup" }
                    }
                    
                    // Enforce process limits for Maya
                    enforceProcessLimits()
                    
                } catch (e: CancellationException) {
                    Log.info { "Process cleanup task cancelled" }
                    break
                } catch (e: Exception) {
                    Log.error { "Error during process cleanup: ${e.message}" }
                }
            }
        }
    }
    
    /**
     * Log process status from all TTS engines
     */
    private fun logEngineProcessStatus() {
        try {
            val kokoroCount = if (kokoroAvailable) kokoroAdapter.getActiveProcessCount() else 0
            val mayaCount = if (mayaAvailable) mayaAdapter.getActiveProcessCount() else 0
            val totalCount = kokoroCount + mayaCount
            
            if (totalCount > 0) {
                Log.info { "TTS Engine processes: Kokoro=$kokoroCount, Maya=$mayaCount (Total=$totalCount)" }
            }
        } catch (e: Exception) {
            Log.error { "Error logging engine process status: ${e.message}" }
        }
    }
    
    /**
     * Enforce process limits for Maya engine
     * 
     * Maya has a configurable max concurrent process limit from preferences.
     * This ensures the limit is respected.
     */
    private fun enforceProcessLimits() {
        try {
            if (mayaAvailable) {
                val maxProcesses = appPrefs.maxConcurrentTTSProcesses().get()
                val currentCount = mayaAdapter.getActiveProcessCount()
                
                if (currentCount > maxProcesses) {
                    Log.warn { "Maya process count ($currentCount) exceeds limit ($maxProcesses)" }
                    // The Maya engine itself handles queuing, so just log the warning
                }
            }
        } catch (e: Exception) {
            Log.error { "Error enforcing process limits: ${e.message}" }
        }
    }
    
    /**
     * Get total active process count across all TTS engines
     * 
     * @return Total number of active processes
     */
    fun getTotalActiveProcessCount(): Int {
        var total = 0
        
        try {
            if (kokoroAvailable) {
                total += kokoroAdapter.getActiveProcessCount()
            }
            if (mayaAvailable) {
                total += mayaAdapter.getActiveProcessCount()
            }
            // Add ProcessTracker count for any directly tracked processes
            total += processTracker.getActiveProcessCount()
        } catch (e: Exception) {
            Log.error { "Error getting total process count: ${e.message}" }
        }
        
        return total
    }
    
    /**
     * Get detailed process statistics
     * 
     * @return Map of engine name to process count
     */
    fun getProcessStatistics(): Map<String, Int> {
        val stats = mutableMapOf<String, Int>()
        
        try {
            if (kokoroAvailable) {
                stats["Kokoro"] = kokoroAdapter.getActiveProcessCount()
            }
            if (mayaAvailable) {
                stats["Maya"] = mayaAdapter.getActiveProcessCount()
            }
            
            // Add ProcessTracker statistics
            val trackerStats = processTracker.getProcessStatistics()
            trackerStats.forEach { (engine, count) ->
                stats[engine] = stats.getOrDefault(engine, 0) + count
            }
        } catch (e: Exception) {
            Log.error { "Error getting process statistics: ${e.message}" }
        }
        
        return stats
    }
    
    private fun startWordBoundaryTracking(text: String) {
        // Cancel any existing word boundary tracking
        wordBoundaryJob?.cancel()
        
        wordBoundaryJob = serviceScope.launch {
            try {
                // Get word boundaries from synthesizer
                val boundaries = synthesizer.getWordBoundaries(text)
                
                if (boundaries.isEmpty()) {
                    return@launch
                }
                
                // Track the start time for synchronization
                val startTime = System.currentTimeMillis()
                
                // Emit word boundary events based on timing
                for (boundary in boundaries) {
                    // Calculate how long to wait before highlighting this word
                    val elapsed = System.currentTimeMillis() - startTime
                    val delay = boundary.startTimeMs - elapsed
                    
                    if (delay > 0) {
                        delay(delay)
                    }
                    
                    // Check if still playing
                    if (!state.isPlaying.value) {
                        break
                    }
                    
                    // Emit word boundary event for UI highlighting
                    state.currentWordBoundary = boundary
                }
                
                // Clear word boundary when done
                state.currentWordBoundary = null
                
            } catch (e: CancellationException) {
                state.currentWordBoundary = null
            } catch (e: Exception) {
                Log.error { "Error during word boundary tracking: ${e.message}" }
                state.currentWordBoundary = null
            }
        }
    }

    private fun readPrefs() {
        serviceScope.launch {
            state.setAutoNextChapter(readerPreferences.readerAutoNext().get())
            state.setCurrentLanguage(readerPreferences.speechLanguage().get())
            state.setCurrentVoice(appPrefs.speechVoice().get())
            state.setSpeechSpeed(readerPreferences.speechRate().get())
            state.setPitch(readerPreferences.speechPitch().get())
            state.setSleepTime(readerPreferences.sleepTime().get())
            state.setSleepMode(readerPreferences.sleepMode().get())
        }
        
        // Listen to preference changes
        serviceScope.launch {
            readerPreferences.readerAutoNext().changes().collect {
                state.setAutoNextChapter(it)
            }
        }
        serviceScope.launch {
            readerPreferences.speechLanguage().changes().collect {
                state.setCurrentLanguage(it)
            }
        }
        serviceScope.launch {
            appPrefs.speechVoice().changes().collect {
                state.setCurrentVoice(it)
            }
        }
        serviceScope.launch {
            readerPreferences.speechPitch().changes().collect {
                state.setPitch(it)
                // Apply pitch change if not in simulation mode
            }
        }
        serviceScope.launch {
            readerPreferences.speechRate().changes().collect {
                state.setSpeechSpeed(it)
                // Apply speech rate change if not in simulation mode
                if (!isSimulationMode) {
                    try {
                        synthesizer.setSpeechRate(it)
                    } catch (e: Exception) {
                        Log.error { "Failed to apply speech rate: ${e.message}" }
                    }
                }
            }
        }
        serviceScope.launch {
            readerPreferences.sleepTime().changes().collect {
                state.setSleepTime(it)
            }
        }
        serviceScope.launch {
            readerPreferences.sleepMode().changes().collect {
                state.setSleepMode(it)
            }
        }
        
        // Listen to Piper model selection changes
        serviceScope.launch {
            appPrefs.selectedPiperModel().changes().collect { modelId ->
                if (modelId.isNotEmpty()) {
                    loadSelectedVoiceModel()
                }
            }
        }
    }

    suspend fun startReading(bookId: Long, chapterId: Long, autoPlay: Boolean = false) {
        val book = bookRepo.findBookById(bookId)
        val chapter = chapterRepo.findChapterById(chapterId)
        val chapters = chapterRepo.findChaptersByBookId(bookId)
        
        Log.info { "TTS startReading - book: ${book?.title}, chapter: ${chapter?.name}, content size: ${chapter?.content?.size}, sourceId: ${book?.sourceId}" }

        if (chapter != null && book != null) {
            // Try to get the source/catalog
            val source = extensions.get(book.sourceId)
            
            if (source == null) {
                Log.warn { "Source not installed for book: ${book.title}. TTS will work with cached content only." }
            }
            
            state.setTtsBook(book)
            state.setTtsChapters(chapters)
            state.setTtsCatalog(source)
            state.setCurrentReadingParagraph(0)
            
            // Always set the chapter content if available
            if (chapter.isEmpty()) {
                Log.info { "Chapter is empty" }
                if (source != null) {
                    Log.info { "Attempting to load chapter content from source..." }
                    loadChapter(chapterId)
                } else {
                    Log.error { "Cannot load chapter content: source is not installed. Please install the source extension." }
                    // Set error state to show snackbar in UI
                    state.sourceNotInstalledError = true
                }
            } else {
                Log.info { "Chapter has content, setting directly. Content items: ${chapter.content.size}" }
                state.setTtsChapter(chapter)
                
                // Log the extracted content
                val content = state.ttsContent.value
                Log.info { "TTS content after setting chapter: ${content?.size} paragraphs" }
                
                // If source is not installed, set warning state
                if (source == null) {
                    state.sourceNotInstalledError = true
                }
            }
            
            // Load translated content if TTS with translated text is enabled
            loadTranslatedContentForTTS(chapterId)
            
            // Only auto-play if explicitly requested
            if (autoPlay) {
                startService(ACTION_PLAY)
            }
        } else {
            Log.error { "TTS startReading failed - book: ${book?.title}, chapter: ${chapter?.name}" }
        }
    }
    
    private suspend fun loadTranslatedContentForTTS(chapterId: Long) {
        // Check if TTS with translated text is enabled
        val useTTSWithTranslatedText = readerPreferences.useTTSWithTranslatedText().get()
        
        if (!useTTSWithTranslatedText) {
            state.setTranslatedTTSContent(null)
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
                
                state.setTranslatedTTSContent(translatedText)
                Log.info { "Loaded translated content for TTS: ${translatedText.size} paragraphs" }
            } else {
                state.setTranslatedTTSContent(null)
                Log.info { "No translated content available for TTS, will use original text" }
            }
        } catch (e: Exception) {
            Log.error{ "Error loading translated content for TTS" }
            state.setTranslatedTTSContent(null)
        }
    }

    fun startService(action: String) {
        serviceJob = serviceScope.launch {
            try {
                when (action) {
                    ACTION_STOP -> {
                        stopReading()
                    }
                    ACTION_PAUSE -> {
                        pauseReading()
                    }
                    ACTION_PLAY -> {
                        playReading()
                    }
                    ACTION_SKIP_NEXT -> {
                        skipToNextChapter()
                    }
                    ACTION_SKIP_PREV -> {
                        skipToPreviousChapter()
                    }
                    ACTION_NEXT_PAR -> {
                        nextParagraph()
                    }
                    ACTION_PREV_PAR -> {
                        previousParagraph()
                    }
                }
            } catch (e: Exception) {
                Log.error { "Desktop TTS error" }
            }
        }
    }

    @OptIn(ExperimentalTime::class)
    private suspend fun playReading() {
        state.setPlaying(true)
        state.setStartTime(kotlin.time.Clock.System.now())
        readText()
    }

    private fun pauseReading() {
        state.setPlaying(false)
        
        // Pause audio playback (responds within 200ms as per requirements)
        if (!isSimulationMode) {
            audioEngine.pause()
        }
        
        // Pause Gradio audio if active - this will also complete the playAndWait coroutine
        gradioAdapter?.pause()
        gradioAudioPlayer?.pause()
        
        // Cancel speech and word boundary jobs
        speechJob?.cancel()
        wordBoundaryJob?.cancel()
        
        // Record feature usage
        usageAnalytics.recordFeatureUsage("pause_reading")
    }

    private fun stopReading() {
        state.setPlaying(false)
        
        // Stop audio playback and clear buffers
        if (!isSimulationMode) {
            audioEngine.stop()
        }
        
        // Stop Gradio audio if active
        gradioAdapter?.stop()
        gradioAudioPlayer?.stop()
        
        // Clear Gradio audio cache
        clearGradioAudioCache()
        
        // Cancel all jobs
        speechJob?.cancel()
        wordBoundaryJob?.cancel()
        
        // Terminate all tracked processes
        val terminatedCount = processTracker.terminateAllProcesses()
        if (terminatedCount > 0) {
            Log.info { "Terminated $terminatedCount processes during stop" }
        }
        
        // Reset state
        state.setCurrentReadingParagraph(0)
        state.currentWordBoundary = null
        speechJobStartParagraph = -1
    }

    private suspend fun skipToNextChapter() {
        val chapter = state.ttsChapter.value ?: return
        val chapters = state.ttsChapters.value
        val index = getChapterIndex(chapter, chapters)
        
        // Stop audio playback before loading new chapter
        if (!isSimulationMode) {
            audioEngine.stop()
        }
        
        // Stop Gradio audio if active
        gradioAdapter?.stop()
        gradioAudioPlayer?.stop()
        
        // Clear Gradio audio cache for new chapter
        clearGradioAudioCache()
        
        // Cancel ongoing jobs
        speechJob?.cancel()
        wordBoundaryJob?.cancel()
        
        // Clear word boundary state
        state.currentWordBoundary = null
        speechJobStartParagraph = -1
        
        if (index < chapters.lastIndex) {
            val nextChapter = chapters[index + 1]
            loadChapter(nextChapter.id)
            state.setCurrentReadingParagraph(0)
            
            // Load translated content for the new chapter
            loadTranslatedContentForTTS(nextChapter.id)
            
            // Start reading if playing (responds within 500ms as per requirements)
            if (state.isPlaying.value) {
                readText()
            }
        }
        
        // Record feature usage
        usageAnalytics.recordFeatureUsage("skip_next_chapter")
    }

    private suspend fun skipToPreviousChapter() {
        val chapter = state.ttsChapter.value ?: return
        val chapters = state.ttsChapters.value
        val index = getChapterIndex(chapter, chapters)
        
        // Stop audio playback before loading new chapter
        if (!isSimulationMode) {
            audioEngine.stop()
        }
        
        // Stop Gradio audio if active
        gradioAdapter?.stop()
        gradioAudioPlayer?.stop()
        
        // Clear Gradio audio cache for new chapter
        clearGradioAudioCache()
        
        // Cancel ongoing jobs
        speechJob?.cancel()
        wordBoundaryJob?.cancel()
        
        // Clear word boundary state
        state.currentWordBoundary = null
        speechJobStartParagraph = -1
        
        if (index > 0) {
            val prevChapter = chapters[index - 1]
            loadChapter(prevChapter.id)
            state.setCurrentReadingParagraph(0)
            
            // Load translated content for the new chapter
            loadTranslatedContentForTTS(prevChapter.id)
            
            // Start reading if playing (responds within 500ms as per requirements)
            if (state.isPlaying.value) {
                readText()
            }
        }
        
        // Record feature usage
        usageAnalytics.recordFeatureUsage("skip_previous_chapter")
    }

    private fun nextParagraph() {
        // Use translated content if available, otherwise use original content
        val translatedContent = state.translatedTTSContent.value
        val content = if (translatedContent != null && translatedContent.isNotEmpty()) {
            translatedContent
        } else {
            state.ttsContent.value
        }
        
        content?.let { paragraphs ->
            if (state.currentReadingParagraph.value < paragraphs.lastIndex) {
                val wasPlaying = state.isPlaying.value
                val oldParagraph = state.currentReadingParagraph.value
                
                // Cancel ongoing jobs FIRST to prevent race conditions
                val oldSpeechJob = speechJob
                speechJob = null
                oldSpeechJob?.cancel()
                wordBoundaryJob?.cancel()
                
                // Stop current audio playback
                if (!isSimulationMode) {
                    audioEngine.stop()
                }
                
                // Stop Gradio audio if active
                gradioAdapter?.stop()
                gradioAudioPlayer?.stop()
                
                // Clear word boundary state
                state.currentWordBoundary = null
                
                // Move to next paragraph
                val newParagraph = oldParagraph + 1
                state.setCurrentReadingParagraph(newParagraph)
                
                // Start reading if was playing (responds within 500ms as per requirements)
                if (wasPlaying) {
                    serviceScope.launch {
                        // Wait a bit for the old job to fully cancel
                        oldSpeechJob?.join()
                        readText()
                    }
                }
            }
        }
    }

    private fun previousParagraph() {
        // Use translated content if available, otherwise use original content
        val translatedContent = state.translatedTTSContent.value
        val content = if (translatedContent != null && translatedContent.isNotEmpty()) {
            translatedContent
        } else {
            state.ttsContent.value
        }
        
        content?.let { paragraphs ->
            if (state.currentReadingParagraph.value > 0) {
                val wasPlaying = state.isPlaying.value
                val oldParagraph = state.currentReadingParagraph.value
                
                // Cancel ongoing jobs FIRST to prevent race conditions
                val oldSpeechJob = speechJob
                speechJob = null
                oldSpeechJob?.cancel()
                wordBoundaryJob?.cancel()
                
                // Stop current audio playback
                if (!isSimulationMode) {
                    audioEngine.stop()
                }
                
                // Stop Gradio audio if active
                gradioAdapter?.stop()
                gradioAudioPlayer?.stop()
                
                // Clear word boundary state
                state.currentWordBoundary = null
                
                // Move to previous paragraph
                val newParagraph = oldParagraph - 1
                state.setCurrentReadingParagraph(newParagraph)
                
                // Start reading if was playing (responds within 500ms as per requirements)
                if (wasPlaying) {
                    serviceScope.launch {
                        // Wait a bit for the old job to fully cancel
                        oldSpeechJob?.join()
                        readText()
                    }
                }
            }
        }
    }

    private suspend fun readText() {
        // Use translated content if available, otherwise use original content
        val translatedContent = state.translatedTTSContent.value
        val content = if (translatedContent != null && translatedContent.isNotEmpty()) {
            translatedContent
        } else {
            state.ttsContent.value
        }
        
        if (content == null || content.isEmpty()) {
            return
        }
        
        val currentParagraph = state.currentReadingParagraph.value
        
        if (currentParagraph >= content.size) {
            handleEndOfChapter()
            return
        }

        val text = content[currentParagraph]
        
        if (text.isBlank()) {
            advanceToNextParagraph()
            return
        }
        
        state.setUtteranceId(currentParagraph.toString())
        
        // Track the paragraph index when this speechJob starts
        // This is used to detect manual navigation (user tapping next/prev)
        speechJobStartParagraph = currentParagraph
        
        // Pre-generate upcoming paragraphs for Gradio (speeds up playback)
        if (currentEngine == TTSEngine.GRADIO) {
            serviceScope.launch {
                prefetchGradioAudio(currentParagraph, content)
            }
        }
        
        // Pre-generate upcoming paragraphs for Kokoro (speeds up playback)
        if (currentEngine == TTSEngine.KOKORO && currentParagraph % 3 == 0) {
            serviceScope.launch {
                preGenerateParagraphs(5)
            }
        }

        speechJob = serviceScope.launch {
            try {
                when (currentEngine) {
                    TTSEngine.PIPER -> readTextWithPiper(text)
                    TTSEngine.KOKORO -> readTextWithKokoro(text)
                    TTSEngine.MAYA -> readTextWithMaya(text)
                    TTSEngine.GRADIO -> readTextWithGradio(text)
                    TTSEngine.SIMULATION -> readTextSimulation(text)
                }
            } catch (e: CancellationException) {
                audioEngine.stop()
            } catch (e: Exception) {
                Log.error { "Error during speech: ${e.message}" }
                advanceToNextParagraph()
            }
        }
    }
    
    private suspend fun readTextWithPiper(text: String) {
        // Check if synthesizer is initialized
        if (!synthesizer.isInitialized()) {
            Log.error { "Synthesizer is not initialized! Falling back to simulation mode." }
            enableSimulationMode("Synthesizer not initialized")
            readTextSimulation(text)
            return
        }
        
        // Performance monitoring - start timing
        val startTime = System.currentTimeMillis()
        
        // Generate audio using Piper
        val audioResult = synthesizer.synthesize(text)
        
        // Performance monitoring - record metrics
        val synthesisTime = System.currentTimeMillis() - startTime
        
        audioResult.onSuccess { audioData ->
            // Record performance metrics
            val voiceId = state.selectedVoiceModel?.id ?: "unknown"
            performanceMonitor.recordSynthesis(
                textLength = text.length,
                audioSize = audioData.samples.size,
                durationMs = synthesisTime,
                voiceId = voiceId
            )
            
            // Record usage analytics (privacy-preserving)
            val language = state.selectedVoiceModel?.language ?: "unknown"
            // Calculate duration from audio data: samples / (sampleRate * channels * bytesPerSample)
            val bytesPerSample = when (audioData.format) {
                ireader.domain.services.tts_service.piper.AudioData.AudioFormat.PCM_16 -> 2
                ireader.domain.services.tts_service.piper.AudioData.AudioFormat.PCM_24 -> 3
                ireader.domain.services.tts_service.piper.AudioData.AudioFormat.PCM_32 -> 4
            }
            val durationMs = (audioData.samples.size.toLong() * 1000L) / (audioData.sampleRate * audioData.channels * bytesPerSample)
            usageAnalytics.recordVoiceUsage(
                language = language,
                durationMs = durationMs,
                characterCount = text.length
            )
            
            // Start word boundary tracking for text highlighting
            startWordBoundaryTracking(text)
            
            // Play generated audio - this will block until playback completes
            try {
                audioEngine.play(audioData)
            } catch (e: Exception) {
                Log.error { "Audio playback error: ${e.message}" }
            }
            
            // Check sleep time
            checkSleepTime()
            
            // Move to next paragraph if still playing
            if (state.isPlaying.value) {
                advanceToNextParagraph()
            }
        }.onFailure { error ->
            // Record error metrics
            val voiceId = state.selectedVoiceModel?.id
            val errorType = error::class.simpleName ?: "UnknownError"
            
            performanceMonitor.recordError(
                operation = "synthesis",
                errorType = errorType,
                voiceId = voiceId,
                errorMessage = error.message
            )
            
            // Record crash/error in analytics
            usageAnalytics.recordCrash(
                errorType = errorType,
                errorMessage = error.message,
                stackTrace = error.stackTraceToString(),
                context = mapOf(
                    "operation" to "synthesis",
                    "voice_language" to (state.selectedVoiceModel?.language ?: "unknown"),
                    "text_length" to text.length.toString()
                )
            )
            
            Log.error { "Speech synthesis failed: ${error.message}" }
            
            // Handle specific error types
            when (error) {
                is PiperException.SynthesisException -> {
                    Log.error { "Synthesis error: ${error.getUserMessage()}" }
                }
                is PiperException.ResourceException -> {
                    Log.error { "Resource error: ${error.getUserMessage()}" }
                    // May need to reload voice model or free resources
                }
                else -> {
                    Log.error { "Unexpected error during synthesis" }
                }
            }
            
            // Skip to next paragraph on synthesis failure
            advanceToNextParagraph()
        }
    }
    
    /**
     * Format float to specified decimal places
     */
    private fun Float.format(decimals: Int): String {
        return "%.${decimals}f".format(this)
    }
    
    private suspend fun readTextSimulation(text: String) {
        // Calculate reading time based on word count and speech rate
        val words = text.split("\\s+".toRegex())
        val wordsPerMinute = 150 * state.speechSpeed.value // Base reading speed
        val readingTimeMs = (words.size / wordsPerMinute * 60 * 1000).toLong()
        
        delay(readingTimeMs)
        
        // Check sleep time
        checkSleepTime()
        
        if (state.isPlaying.value) {
            advanceToNextParagraph()
        }
    }
    
    private suspend fun readTextWithKokoro(text: String) {
        try {
            Log.debug { "Synthesizing with Kokoro: text length=${text.length}" }
            
            // Use default voice or get from preferences
            val voice = "af_bella" // TODO: Add voice selection to preferences
            
            // Check cache first
            val cachedAudio = audioCache.get(text, voice, state.speechSpeed.value)
            
            val audioData = if (cachedAudio != null) {
                Log.debug { "Using cached audio for Kokoro" }
                cachedAudio
            } else {
                // Synthesize with Kokoro
                val result = kokoroAdapter.synthesize(text, voice, state.speechSpeed.value)
                
                result.onSuccess { audio ->
                    // Cache the generated audio
                    audioCache.put(text, voice, state.speechSpeed.value, audio)
                }.getOrNull()
            }
            
            if (audioData != null) {
                Log.debug { "Kokoro synthesis successful: ${audioData.samples.size} bytes" }
                
                // Play generated audio
                try {
                    audioEngine.play(audioData)
                } catch (e: Exception) {
                    Log.error { "Audio playback error: ${e.message}" }
                }
                
                // Check sleep time
                checkSleepTime()
                
                // Move to next paragraph if still playing
                if (state.isPlaying.value) {
                    advanceToNextParagraph()
                }
            } else {
                Log.error { "Kokoro synthesis failed" }
                advanceToNextParagraph()
            }
        } catch (e: Exception) {
            Log.error { "Kokoro error: ${e.message}" }
            advanceToNextParagraph()
        }
    }
    
    private suspend fun readTextWithMaya(text: String) {
        try {
            Log.debug { "Synthesizing with Maya: text length=${text.length}" }
            
            // Use default language or get from preferences
            val language = "en" // TODO: Add language selection to preferences
            
            // Synthesize with Maya
            val result = mayaAdapter.synthesize(text, language, state.speechSpeed.value)
            
            result.onSuccess { audioData ->
                Log.debug { "Maya synthesis successful: ${audioData.samples.size} bytes" }
                
                // Play generated audio
                try {
                    audioEngine.play(audioData)
                } catch (e: Exception) {
                    Log.error { "Audio playback error: ${e.message}" }
                }
                
                // Check sleep time
                checkSleepTime()
                
                // Move to next paragraph if still playing
                if (state.isPlaying.value) {
                    advanceToNextParagraph()
                }
            }.onFailure { error ->
                Log.error { "Maya synthesis failed: ${error.message}" }
                // Fall back to next paragraph
                advanceToNextParagraph()
            }
        } catch (e: Exception) {
            Log.error { "Maya error: ${e.message}" }
            advanceToNextParagraph()
        }
    }
    
    /**
     * Read text using the new GradioTTSPlayer system
     * Supports any Gradio-based TTS space with caching and prefetching
     */
    private suspend fun readTextWithGradio(text: String) {
        val adapter = gradioAdapter
        if (adapter == null) {
            // Try to configure from preferences
            configureGradioFromPreferences()
            if (gradioAdapter == null) {
                readTextSimulation(text)
                return
            }
        }
        
        val activeAdapter = gradioAdapter ?: return
        val currentParagraph = state.currentReadingParagraph.value
        
        try {
            // Check if cancelled or not playing before starting
            if (!kotlinx.coroutines.currentCoroutineContext().isActive || !state.isPlaying.value) {
                return
            }
            
            // Apply speed from preferences
            val speed = appPrefs.gradioTTSSpeed().get()
            activeAdapter.setSpeed(speed)
            
            // Try to get cached audio first
            var audioData = getCachedGradioAudio(currentParagraph)
            
            if (audioData != null) {
                // Remove from cache after use
                gradioAudioCache.remove(currentParagraph)
            } else {
                // Generate audio (the adapter handles API calls)
                audioData = activeAdapter.generateAudio(text)
            }
            
            // Check if cancelled or paused after generation
            if (!kotlinx.coroutines.currentCoroutineContext().isActive || !state.isPlaying.value) return
            
            if (audioData == null) {
                Log.error { "Failed to generate audio for paragraph $currentParagraph" }
                // Only advance if still playing and not cancelled
                if (state.isPlaying.value && kotlinx.coroutines.currentCoroutineContext().isActive) {
                    advanceToNextParagraph()
                }
                return
            }
            
            // Play and wait for completion
            val success = activeAdapter.playAndWait(audioData)
            
            // Check if cancelled, paused, or playback was interrupted after playback
            // success=false means playback was interrupted (pause/stop)
            if (!kotlinx.coroutines.currentCoroutineContext().isActive || !state.isPlaying.value || !success) return
            
            // Check sleep time
            checkSleepTime()
            
            // Move to next paragraph ONLY if still playing and not cancelled
            if (state.isPlaying.value && kotlinx.coroutines.currentCoroutineContext().isActive) {
                advanceToNextParagraph()
            }
        } catch (e: kotlinx.coroutines.CancellationException) {
            // Coroutine was cancelled, don't advance
            throw e
        } catch (e: Exception) {
            Log.error { "Gradio TTS error: ${e.message}" }
            // Only advance on error if still playing
            if (state.isPlaying.value && kotlinx.coroutines.currentCoroutineContext().isActive) {
                advanceToNextParagraph()
            }
        }
    }
    
    private suspend fun advanceToNextParagraph() {
        // Check if the paragraph was manually changed by user (next/prev button)
        // If so, don't auto-advance because the user already navigated
        val currentParagraph = state.currentReadingParagraph.value
        if (speechJobStartParagraph != -1 && currentParagraph != speechJobStartParagraph) {
            // Paragraph was changed externally (manual navigation), don't advance
            return
        }
        
        // Use translated content if available, otherwise use original content
        val translatedContent = state.translatedTTSContent.value
        val content = if (translatedContent != null && translatedContent.isNotEmpty()) {
            translatedContent
        } else {
            state.ttsContent.value
        } ?: return
        
        if (currentParagraph < content.lastIndex) {
            state.setCurrentReadingParagraph(currentParagraph + 1)
            readText()
        } else {
            handleEndOfChapter()
        }
    }
    
    private suspend fun handleEndOfChapter() {
        if (state.autoNextChapter.value) {
            skipToNextChapter()
        } else {
            stopReading()
        }
    }

    @OptIn(ExperimentalTime::class)
    private fun checkSleepTime() {
        val lastCheckPref = state.startTime.value
        val currentSleepTime = state.sleepTime.value.minutes
        val now = kotlin.time.Clock.System.now()
        if (lastCheckPref != null && now - lastCheckPref > currentSleepTime && state.sleepMode.value) {
            stopReading()
        }
    }
    
    // Cache for pre-generated Gradio audio (thread-safe)
    private val gradioAudioCache = java.util.concurrent.ConcurrentHashMap<Int, ByteArray>()
    // Track paragraphs currently being prefetched to avoid duplicate requests
    private val prefetchingParagraphs = java.util.concurrent.ConcurrentHashMap.newKeySet<Int>()
    
    /**
     * Prefetch audio for the next paragraphs to reduce latency
     * This runs in the background while the current paragraph is playing
     */
    private suspend fun prefetchGradioAudio(currentParagraph: Int, content: List<String>) {
        val adapter = gradioAdapter ?: return
        val prefetchCount = 3 // Prefetch next 3 paragraphs
        
        for (i in 1..prefetchCount) {
            val nextParagraph = currentParagraph + i
            if (nextParagraph >= content.size) break
            
            // Skip if already cached or currently being prefetched
            if (gradioAudioCache.containsKey(nextParagraph)) continue
            if (!prefetchingParagraphs.add(nextParagraph)) continue // Already being prefetched
            
            val text = content[nextParagraph]
            if (text.isBlank()) {
                prefetchingParagraphs.remove(nextParagraph)
                continue
            }
            
            try {
                // Check if still playing and not cancelled
                if (!state.isPlaying.value || !kotlinx.coroutines.currentCoroutineContext().isActive) {
                    prefetchingParagraphs.remove(nextParagraph)
                    break
                }
                
                val audioData = adapter.generateAudio(text)
                
                if (audioData != null) {
                    gradioAudioCache[nextParagraph] = audioData
                }
            } catch (e: Exception) {
                // Prefetch failed, ignore
            } finally {
                prefetchingParagraphs.remove(nextParagraph)
            }
        }
        
        // Clean up old cache entries (keep only nearby paragraphs)
        val keysToRemove = gradioAudioCache.keys().toList().filter { it < currentParagraph - 1 || it > currentParagraph + prefetchCount + 1 }
        keysToRemove.forEach { gradioAudioCache.remove(it) }
    }
    
    /**
     * Get cached audio for a paragraph, or null if not cached
     */
    fun getCachedGradioAudio(paragraph: Int): ByteArray? {
        return gradioAudioCache[paragraph]
    }
    
    /**
     * Clear the Gradio audio cache
     */
    fun clearGradioAudioCache() {
        gradioAudioCache.clear()
        prefetchingParagraphs.clear()
    }

    private suspend fun loadChapter(chapterId: Long) {
        val localChapter = chapterRepo.findChapterById(chapterId)
        val source = state.ttsCatalog.value ?: return

        if (localChapter != null && !localChapter.isEmpty()) {
            state.setTtsChapter(localChapter)
            chapterUseCase.updateLastReadTime(localChapter, updateDateFetched = false)
        } else if (localChapter != null) {
            remoteUseCases.getRemoteReadingContent(
                chapter = localChapter,
                source,
                onSuccess = { result ->
                    if (result.content.joinToString().length > 1) {
                        state.setTtsChapter(result)
                        chapterUseCase.updateLastReadTime(result, updateDateFetched = true)
                        chapterRepo.findChaptersByBookId(result.bookId).let { res ->
                            state.setTtsChapters(res)
                        }
                    }
                },
                onError = {
                    Log.error { "Failed to load chapter: $it" }
                }
            )
        }
    }

    private fun getChapterIndex(chapter: Chapter, chapters: List<Chapter>): Int {
        val chaptersIds = chapters.map { it.name }
        val index = chaptersIds.indexOfFirst { it == chapter.name }
        return if (index != -1) index else throw Exception("Invalid chapter")
    }

    /**
     * Select and persist a Piper voice model
     * This will save the selection to preferences and reload the model
     */
    suspend fun selectVoiceModel(modelId: String) {
        try {
            Log.info { "Selecting voice model: $modelId" }
            
            // Check if model is downloaded - first try state, then model manager
            var model = state.availableVoiceModels.find { it.id == modelId }
            
            if (model == null) {
                // State might be empty, try loading from model manager
                Log.info { "Model not found in state, checking model manager..." }
                val allModels = modelManager.getAvailableModels()
                model = allModels.find { it.id == modelId }
                
                if (model != null) {
                    // Update state with all models
                    val downloadedIds = appPrefs.downloadedModels().get()
                    state.availableVoiceModels = allModels.map { m ->
                        m.copy(isDownloaded = downloadedIds.contains(m.id) || modelManager.getModelPaths(m.id) != null)
                    }
                    // Re-find the model with updated download status
                    model = state.availableVoiceModels.find { it.id == modelId }
                }
            }
            
            if (model == null) {
                Log.error { "Voice model not found in catalog: $modelId" }
                throw IllegalArgumentException("Voice model not found: $modelId")
            }
            
            // Check if model files exist (might be downloaded but not in prefs)
            val modelPaths = modelManager.getModelPaths(modelId)
            val isActuallyDownloaded = model.isDownloaded || modelPaths != null
            
            if (!isActuallyDownloaded) {
                throw IllegalStateException("Voice model not downloaded: $modelId. Please download it first.")
            }
            
            // Persist model selection immediately
            appPrefs.selectedPiperModel().set(modelId)
            
            // Update state
            state.selectedVoiceModel = model.copy(isDownloaded = true)
            
            // Load the voice model
            loadSelectedVoiceModel()
            
            Log.info { "Voice model selection saved: $modelId" }
        } catch (e: Exception) {
            Log.error { "Failed to select voice model: ${e.message}" }
            throw e
        }
    }
    
    /**
     * Switch to a different voice model during playback
     * This will pause playback, reload the model, and resume if requested
     */
    suspend fun switchVoiceModel(modelId: String, resumePlayback: Boolean = false) {
        try {
            Log.info { "Switching voice model to: $modelId" }
            
            // Save current playback state
            val wasPlaying = state.isPlaying.value
            val currentParagraph = state.currentReadingParagraph.value
            
            // Pause playback if active
            if (wasPlaying) {
                pauseReading()
            }
            
            // Select and load new voice model
            selectVoiceModel(modelId)
            
            // Record feature usage
            usageAnalytics.recordFeatureUsage("switch_voice_model")
            
            // Wait for model to load (triggered by preference change)
            // In a real implementation, we might want to wait for a callback or event
            delay(500) // Give time for model to load
            
            // Resume playback if requested and was playing
            if (resumePlayback && wasPlaying) {
                state.setCurrentReadingParagraph(currentParagraph)
                playReading()
            }
            
            Log.info { "Voice model switched successfully to: $modelId" }
        } catch (e: Exception) {
            Log.error { "Failed to switch voice model: ${e.message}" }
            throw e
        }
    }
    
    /**
     * Download a voice model
     */
    suspend fun downloadVoiceModel(modelId: String, onProgress: (Float) -> Unit = {}) {
        try {
            Log.info { "Downloading voice model: $modelId" }
            
            // Check state first, then model manager
            var model = state.availableVoiceModels.find { it.id == modelId }
            
            if (model == null) {
                // State might be empty, try loading from model manager
                Log.info { "Model not found in state for download, checking model manager..." }
                val allModels = modelManager.getAvailableModels()
                model = allModels.find { it.id == modelId }
                
                if (model != null) {
                    // Update state with all models
                    val downloadedIds = appPrefs.downloadedModels().get()
                    state.availableVoiceModels = allModels.map { m ->
                        m.copy(isDownloaded = downloadedIds.contains(m.id) || modelManager.getModelPaths(m.id) != null)
                    }
                }
            }
            
            if (model == null) {
                throw IllegalArgumentException("Voice model not found: $modelId")
            }
            
            // Download the model using model manager
            modelManager.downloadModel(model).collect { progress ->
                val progressPercent = if (progress.total > 0) {
                    progress.downloaded.toFloat() / progress.total.toFloat()
                } else {
                    0f
                }
                onProgress(progressPercent)
                
                // Check if download is complete
                if (progress.status == "Complete") {
                    Log.info { "Voice model downloaded successfully: $modelId" }
                    
                    // Update downloaded models list
                    updateDownloadedModels()
                    
                    // Record feature usage
                    usageAnalytics.recordFeatureUsage("download_voice_model")
                    
                    // If no model is currently selected, select this one
                    if (appPrefs.selectedPiperModel().get().isEmpty()) {
                        selectVoiceModel(modelId)
                    }
                }
            }
        } catch (e: Exception) {
            Log.error { "Error downloading voice model: ${e.message}" }
            throw e
        }
    }
    
    /**
     * Delete a voice model
     */
    suspend fun deleteVoiceModel(modelId: String) {
        try {
            Log.info { "Deleting voice model: $modelId" }
            
            // Check if this is the currently selected model
            val currentModelId = appPrefs.selectedPiperModel().get()
            if (currentModelId == modelId) {
                // Stop playback if active
                if (state.isPlaying.value) {
                    stopReading()
                }
                
                // Shutdown synthesizer
                synthesizer.shutdown()
                
                // Clear selection
                appPrefs.selectedPiperModel().set("")
                state.selectedVoiceModel = null
                
                // Enable simulation mode
                enableSimulationMode("Voice model deleted")
            }
            
            // Delete the model files
            val result = modelManager.deleteModel(modelId)
            
            result.onSuccess {
                Log.info { "Voice model deleted successfully: $modelId" }
                
                // Update downloaded models list
                updateDownloadedModels()
            }.onFailure { error ->
                Log.error { "Failed to delete voice model: ${error.message}" }
                throw error
            }
        } catch (e: Exception) {
            Log.error { "Error deleting voice model: ${e.message}" }
            throw e
        }
    }
    
    /**
     * Update the list of downloaded models in preferences
     * Should be called after downloading or deleting a model
     */
    suspend fun updateDownloadedModels() {
        try {
            val downloadedModels = modelManager.getDownloadedModels()
            val modelIds = downloadedModels.map { it.id }.toSet()
            
            // Persist downloaded models list
            appPrefs.downloadedModels().set(modelIds)
            
            // Update state
            state.availableVoiceModels = modelManager.getAvailableModels().map { model ->
                model.copy(isDownloaded = modelIds.contains(model.id))
            }
            
            Log.debug { "Downloaded models list updated: ${modelIds.size} models" }
        } catch (e: Exception) {
            Log.error { "Failed to update downloaded models list: ${e.message}" }
        }
    }
    
    /**
     * Save TTS settings to preferences
     * Called when user modifies speech rate, pitch, or volume
     */
    fun saveTTSSettings() {
        try {
            readerPreferences.speechRate().set(state.speechSpeed.value)
            readerPreferences.speechPitch().set(state.pitch.value)
        } catch (e: Exception) {
            Log.error { "Failed to save TTS settings: ${e.message}" }
        }
    }
    
    /**
     * Update speech rate and save to preferences
     */
    fun setSpeechRate(rate: Float) {
        state.setSpeechSpeed(rate)
        saveTTSSettings()
        
        // Apply to synthesizer if not in simulation mode
        if (!isSimulationMode) {
            try {
                synthesizer.setSpeechRate(rate)
            } catch (e: Exception) {
                Log.error { "Failed to apply speech rate: ${e.message}" }
            }
        }
        
        // Record feature usage
        usageAnalytics.recordFeatureUsage("adjust_speech_rate")
    }
    
    /**
     * Update pitch and save to preferences
     */
    fun setPitch(pitch: Float) {
        state.setPitch(pitch)
        saveTTSSettings()
        
        // Note: Piper may not support runtime pitch changes
        // This is saved for future use or other TTS engines
    }

    /**
     * Get performance metrics for monitoring
     */
    fun getPerformanceMetrics(): ireader.domain.services.tts_service.piper.PerformanceMetrics {
        return performanceMonitor.getMetrics()
    }
    
    /**
     * Get performance report
     */
    fun getPerformanceReport(): ireader.domain.services.tts_service.piper.PerformanceReport {
        return performanceMonitor.generateReport()
    }
    
    /**
     * Reset performance metrics
     */
    fun resetPerformanceMetrics() {
        performanceMonitor.reset()
        Log.info { "Performance metrics reset" }
    }
    
    /**
     * Get usage analytics summary
     */
    fun getAnalyticsSummary(): ireader.domain.services.tts_service.piper.AnalyticsSummary {
        return usageAnalytics.generateSummary()
    }
    
    /**
     * Export analytics data
     */
    fun exportAnalyticsData(): ireader.domain.services.tts_service.piper.AnalyticsExport {
        return usageAnalytics.exportData()
    }
    
    /**
     * Clear analytics data
     */
    fun clearAnalyticsData() {
        usageAnalytics.clearAllData()
        Log.info { "Analytics data cleared" }
    }
    
    /**
     * Record feature usage for analytics
     */
    fun recordFeatureUsage(featureName: String) {
        usageAnalytics.recordFeatureUsage(featureName)
    }
    
    /**
     * Enable streaming synthesis for long texts
     * This processes text in chunks to improve responsiveness
     */
    suspend fun enableStreamingSynthesis(enabled: Boolean) {
        // Store preference for streaming synthesis
        // This would be used in readTextWithPiper to decide whether to use
        // synthesizer.synthesize() or synthesizer.synthesizeStream()
        Log.info { "Streaming synthesis ${if (enabled) "enabled" else "disabled"}" }
    }
    
    /**
     * Get current voice model information
     */
    fun getCurrentVoiceInfo(): VoiceInfo? {
        val model = state.selectedVoiceModel ?: return null
        
        return VoiceInfo(
            modelId = model.id,
            name = model.name,
            language = model.language,
            isDownloaded = model.isDownloaded,
            isActive = !isSimulationMode
        )
    }
    
    /**
     * Get TTS service status
     */
    fun getServiceStatus(): ServiceStatus {
        return ServiceStatus(
            isInitialized = !isSimulationMode,
            isPlaying = state.isPlaying.value,
            currentVoice = getCurrentVoiceInfo(),
            performanceMetrics = getPerformanceMetrics(),
            simulationMode = isSimulationMode
        )
    }
    
    /**
     * Get current TTS engine
     */
    fun getCurrentEngine(): TTSEngine {
        return currentEngine
    }
    
    /**
     * Set TTS engine
     */
    fun setEngine(engine: TTSEngine) {
        when (engine) {
            TTSEngine.PIPER -> {
                if (synthesizer.isInitialized()) {
                    currentEngine = TTSEngine.PIPER
                    isSimulationMode = false
                    Log.info { "Switched to Piper TTS" }
                } else {
                    Log.warn { "Piper not available" }
                }
            }
            TTSEngine.KOKORO -> {
                if (kokoroAvailable) {
                    currentEngine = TTSEngine.KOKORO
                    isSimulationMode = false
                    Log.info { "Switched to Kokoro TTS" }
                } else {
                    Log.warn { "Kokoro not available. Please install from TTS Manager." }
                }
            }
            TTSEngine.MAYA -> {
                if (mayaAvailable) {
                    currentEngine = TTSEngine.MAYA
                    isSimulationMode = false
                    Log.info { "Switched to Maya TTS" }
                } else {
                    Log.warn { "Maya not available. Please install from TTS Manager." }
                }
            }
            TTSEngine.GRADIO -> {
                // Try to configure from preferences if not already available
                if (!gradioAvailable || gradioPlayer == null) {
                    Log.info { "Gradio not available, trying to configure from preferences..." }
                    configureGradioFromPreferences()
                }
                
                if (gradioAvailable && gradioPlayer != null) {
                    currentEngine = TTSEngine.GRADIO
                    isSimulationMode = false
                    Log.info { "Switched to Gradio TTS: ${activeGradioConfig?.name}" }
                } else {
                    Log.warn { "Gradio TTS not available. Please configure in TTS Settings." }
                }
            }
            TTSEngine.SIMULATION -> {
                currentEngine = TTSEngine.SIMULATION
                isSimulationMode = true
                Log.info { "Switched to Simulation mode" }
            }
        }
    }
    
    /**
     * Configure Generic Gradio TTS with a configuration using the new GradioTTSPlayer
     */
    fun configureGradio(config: GradioTTSConfig?) {
        if (config != null && config.spaceUrl.isNotEmpty() && config.enabled) {
            // Cleanup old player
            gradioPlayer?.release()
            gradioHttpClient?.close()
            gradioAudioPlayer?.release()
            
            // Create new components
            gradioHttpClient = io.ktor.client.HttpClient()
            gradioAudioPlayer = DesktopGradioAudioPlayer()
            
            // Create the adapter that implements both GradioAudioGenerator and GradioAudioPlayback
            // Store it so we can reuse it for caching
            gradioAdapter = GradioTTSEngineAdapter(
                config = config,
                httpClient = gradioHttpClient!!,
                audioPlayer = gradioAudioPlayer!!
            )
            
            // Create the player
            gradioPlayer = ireader.domain.services.tts_service.player.GradioTTSPlayer(
                audioGenerator = gradioAdapter!!,
                audioPlayer = gradioAdapter!!,
                config = config,
                prefetchCount = 3
            )
            
            activeGradioConfig = config
            gradioAvailable = true
            Log.info { "Gradio TTS configured with new player: ${config.name} (${config.spaceUrl})" }
        } else {
            // Cleanup all Gradio components
            gradioPlayer?.release()
            gradioAdapter?.release()
            gradioHttpClient?.close()
            gradioAudioPlayer?.release()
            gradioPlayer = null
            gradioAdapter = null
            gradioHttpClient = null
            gradioAudioPlayer = null
            activeGradioConfig = null
            gradioAvailable = false
            Log.info { "Gradio TTS disabled" }
        }
    }
    
    /**
     * Configure Gradio TTS from preferences
     */
    fun configureGradioFromPreferences() {
        val useGradioTTS = appPrefs.useGradioTTS().get()
        val activeConfigId = appPrefs.activeGradioConfigId().get()
        
        if (useGradioTTS && activeConfigId.isNotEmpty()) {
            val config = loadGradioConfig(activeConfigId)
            configureGradio(config)
        } else {
            configureGradio(null)
        }
    }
    
    /**
     * Load Gradio TTS configuration from preferences
     */
    private fun loadGradioConfig(configId: String): GradioTTSConfig? {
        return try {
            val configsJson = appPrefs.gradioTTSConfigs().get()
            if (configsJson.isEmpty()) {
                GradioTTSPresets.getPresetById(configId)
            } else {
                val state = kotlinx.serialization.json.Json.decodeFromString<GradioTTSManagerState>(configsJson)
                state.configs.find { it.id == configId } ?: GradioTTSPresets.getPresetById(configId)
            }
        } catch (e: Exception) {
            Log.error { "Failed to load Gradio config: ${e.message}" }
            GradioTTSPresets.getPresetById(configId)
        }
    }
    
    /**
     * Get list of available engines
     */
    fun getAvailableEngines(): List<TTSEngine> {
        return buildList {
            if (synthesizer.isInitialized()) add(TTSEngine.PIPER)
            if (kokoroAvailable) add(TTSEngine.KOKORO)
            if (mayaAvailable) add(TTSEngine.MAYA)
            if (gradioAvailable) add(TTSEngine.GRADIO)
            add(TTSEngine.SIMULATION)
        }
    }
    
    /**
     * Check Kokoro availability on-demand (only when needed)
     * This is called when opening TTS settings screen
     */
    suspend fun checkKokoroAvailability(): Boolean = withContext(Dispatchers.IO) {
        try {
            val kokoroDir = java.io.File(ireader.core.storage.AppDir, "kokoro/kokoro-tts")
            
            if (kokoroDir.exists() && kokoroDir.listFiles()?.isNotEmpty() == true) {
                Log.info { "Found Kokoro repository, checking if fully installed..." }
                
                // Check if dependencies are installed
                val pythonCheck = ProcessBuilder(
                    "python", "-m", "kokoro", "--help"
                ).directory(kokoroDir)
                    .redirectErrorStream(true)
                    .start()
                
                val checkCompleted = pythonCheck.waitFor(5, java.util.concurrent.TimeUnit.SECONDS)
                val output = pythonCheck.inputStream.bufferedReader().readText()
                
                if (checkCompleted && pythonCheck.exitValue() == 0 && output.contains("--voice")) {
                    kokoroAvailable = true
                    Log.info { "Kokoro TTS available (fully installed)" }
                    return@withContext true
                } else {
                    Log.info { "Kokoro repository found but not fully installed" }
                    return@withContext false
                }
            } else {
                Log.info { "Kokoro not installed" }
                return@withContext false
            }
        } catch (e: Exception) {
            Log.debug { "Kokoro check: ${e.message}" }
            return@withContext false
        }
    }
    

    
    /**
     * Check Maya availability on-demand (only when needed)
     * This is called when opening TTS settings screen
     */
    suspend fun checkMayaAvailability(): Boolean = withContext(Dispatchers.IO) {
        try {
            val mayaDir = java.io.File(ireader.core.storage.AppDir, "maya")
            val mayaScript = java.io.File(mayaDir, "maya_tts.py")
            
            if (mayaScript.exists()) {
                Log.info { "Found existing Maya installation, verifying..." }
                
                // Check if dependencies are installed
                val pythonCheck = ProcessBuilder(
                    "python", "-c", 
                    "import torch; import transformers; import scipy; print('OK')"
                ).redirectErrorStream(true).start()
                
                val checkCompleted = pythonCheck.waitFor(5, java.util.concurrent.TimeUnit.SECONDS)
                if (checkCompleted && pythonCheck.exitValue() == 0) {
                    mayaAvailable = true
                    Log.info { "Maya TTS available (dependencies verified)" }
                    return@withContext true
                } else {
                    Log.info { "Maya script found but dependencies missing" }
                    return@withContext false
                }
            } else {
                Log.info { "Maya not installed" }
                return@withContext false
            }
        } catch (e: Exception) {
            Log.debug { "Maya check: ${e.message}" }
            return@withContext false
        }
    }
    
    /**
     * Pre-generate audio for upcoming paragraphs
     * This speeds up playback by generating audio in advance
     */
    suspend fun preGenerateParagraphs(count: Int = 5) {
        // Use translated content if available, otherwise use original content
        val translatedContent = state.translatedTTSContent.value
        val content = if (translatedContent != null && translatedContent.isNotEmpty()) {
            translatedContent
        } else {
            state.ttsContent.value
        } ?: return
        
        val currentIndex = state.currentReadingParagraph.value
        
        // Get next N paragraphs
        val upcomingParagraphs = content
            .drop(currentIndex + 1)
            .take(count)
            .filter { it.isNotBlank() }
        
        if (upcomingParagraphs.isEmpty()) return
        
        Log.info { "Pre-generating ${upcomingParagraphs.size} paragraphs..." }
        
        val voice = when (currentEngine) {
            TTSEngine.KOKORO -> "af_bella"
            else -> return // Only Kokoro benefits from pre-generation currently
        }
        
        audioCache.preGenerate(
            paragraphs = upcomingParagraphs,
            voice = voice,
            speed = state.speechSpeed.value,
            synthesizer = { text ->
                when (currentEngine) {
                    TTSEngine.KOKORO -> kokoroAdapter.synthesize(text, voice, state.speechSpeed.value)
                    else -> Result.failure(Exception("Engine not supported"))
                }
            }
        )
    }
    
    /**
     * Download entire chapter audio for offline listening
     * 
     * @param chapterId Chapter to download
     * @param onProgress Progress callback (current, total)
     * @return Path to saved audio file
     */
    suspend fun downloadChapterAudio(
        chapterId: Long,
        onProgress: (Int, Int) -> Unit = { _, _ -> }
    ): Result<java.io.File> {
        val chapter = chapterRepo.findChapterById(chapterId)
            ?: return Result.failure(Exception("Chapter not found"))
        
        // Load chapter content if empty
        if (chapter.isEmpty()) {
            loadChapter(chapterId)
            delay(1000) // Wait for content to load
        }
        
        val updatedChapter = state.ttsChapter.value
            ?: return Result.failure(Exception("Failed to load chapter content"))
        
        Log.info { "Downloading chapter audio: ${updatedChapter.name}" }
        
        // Verify TTS engine is ready
        when (currentEngine) {
            TTSEngine.PIPER -> {
                if (!synthesizer.isInitialized()) {
                    return Result.failure(Exception("Piper TTS not initialized. Please select a voice model first."))
                }
            }
            TTSEngine.KOKORO -> {
                if (!kokoroAvailable) {
                    return Result.failure(Exception("Kokoro TTS not available. Please initialize it first."))
                }
            }
            TTSEngine.MAYA -> {
                if (!mayaAvailable) {
                    return Result.failure(Exception("Maya TTS not available. Please initialize it first."))
                }
            }
            TTSEngine.GRADIO -> {
                if (!gradioAvailable || gradioPlayer == null) {
                    return Result.failure(Exception("Gradio TTS not available. Please configure it first."))
                }
            }
            TTSEngine.SIMULATION -> {
                return Result.failure(Exception("Cannot download in simulation mode"))
            }
        }
        
        // Create synthesizer function based on current engine
        val synthesizerFn: suspend (String) -> Result<ireader.domain.services.tts_service.piper.AudioData> = { text ->
            try {
                when (currentEngine) {
                    TTSEngine.PIPER -> this.synthesizer.synthesize(text)
                    TTSEngine.KOKORO -> {
                        val voice = "af_bella"
                        kokoroAdapter.synthesize(text, voice, state.speechSpeed.value)
                    }
                    TTSEngine.MAYA -> {
                        val language = "en"
                        mayaAdapter.synthesize(text, language, state.speechSpeed.value)
                    }
                    TTSEngine.GRADIO -> {
                        // Gradio TTS doesn't support direct synthesis to AudioData for chapter download
                        Result.failure(Exception("Gradio TTS doesn't support chapter download"))
                    }
                    TTSEngine.SIMULATION -> Result.failure(Exception("Cannot download in simulation mode"))
                }
            } catch (e: Exception) {
                Log.error { "Synthesis error during chapter download: ${e.message}" }
                Result.failure<ireader.domain.services.tts_service.piper.AudioData>(e)
            }
        }
        
        return chapterDownloader.downloadChapter(
            chapter = updatedChapter,
            synthesizer = synthesizerFn,
            onProgress = onProgress
        )
    }
    
    /**
     * Get list of downloaded chapter audio files
     */
    fun getDownloadedChapters(): List<ChapterAudioDownloader.ChapterAudioInfo> {
        return chapterDownloader.getDownloadedChapters()
    }
    
    /**
     * Delete downloaded chapter audio
     */
    fun deleteChapterAudio(chapterId: Long): Boolean {
        return chapterDownloader.deleteChapterAudio(chapterId)
    }
    
    /**
     * Get audio cache statistics
     */
    suspend fun getCacheStats(): TTSAudioCache.CacheStats {
        return audioCache.getStats()
    }
    
    /**
     * Clear audio cache
     */
    suspend fun clearCache() {
        audioCache.clear()
    }
    
    /**
     * Get maximum concurrent TTS processes setting
     */
    fun getMaxConcurrentProcesses(): Int {
        return appPrefs.maxConcurrentTTSProcesses().get()
    }
    
    /**
     * Set maximum concurrent TTS processes (1-8)
     * Applies to Kokoro and Maya engines
     */
    fun setMaxConcurrentProcesses(max: Int) {
        val clamped = max.coerceIn(1, 8)
        appPrefs.maxConcurrentTTSProcesses().set(clamped)
        
        // Update Kokoro engine if initialized
        if (kokoroAvailable) {
            kokoroEngine.setMaxConcurrentProcesses(clamped)
        }
        
        // TODO: Update Maya engine when implemented
        
        Log.info { "Max concurrent TTS processes set to: $clamped" }
    }
    
    fun shutdown() {
        Log.info { "Shutting down DesktopTTSService" }
        
        // Shutdown monitoring and analytics (generates final reports)
        performanceMonitor.shutdown()
        usageAnalytics.shutdown()
        
        // Stop reading and cancel all jobs
        stopReading()
        serviceJob?.cancel()
        speechJob?.cancel()
        wordBoundaryJob?.cancel()
        
        // Cancel cleanup job
        cleanupJob?.cancel()
        
        // Terminate all tracked processes before shutting down engines
        val terminatedCount = processTracker.terminateAllProcesses()
        if (terminatedCount > 0) {
            Log.info { "Terminated $terminatedCount tracked processes during shutdown" }
        }
        
        // Shutdown Piper components to release native resources
        if (!isSimulationMode) {
            try {
                synthesizer.shutdown()
                audioEngine.shutdown()
                Log.info { "Piper components shutdown successfully" }
            } catch (e: Exception) {
                Log.error { "Error shutting down Piper components: ${e.message}" }
            }
        }
        
        // Shutdown Kokoro and kill all Python processes
        if (kokoroAvailable) {
            try {
                Log.info { "Shutting down Kokoro TTS..." }
                kokoroAdapter.shutdown()
                Log.info { "Kokoro TTS shutdown successfully" }
            } catch (e: Exception) {
                Log.error { "Error shutting down Kokoro: ${e.message}" }
            }
        }
        
        // Shutdown Maya
        if (mayaAvailable) {
            try {
                Log.info { "Shutting down Maya TTS..." }
                mayaAdapter.shutdown()
                Log.info { "Maya TTS shutdown successfully" }
            } catch (e: Exception) {
                Log.error { "Error shutting down Maya: ${e.message}" }
            }
        }
        
        // Shutdown Gradio TTS components
        if (gradioAvailable) {
            try {
                Log.info { "Shutting down Gradio TTS..." }
                gradioPlayer?.release()
                gradioAdapter?.release()
                gradioHttpClient?.close()
                gradioAudioPlayer?.release()
                gradioPlayer = null
                gradioAdapter = null
                gradioHttpClient = null
                gradioAudioPlayer = null
                gradioAvailable = false
                Log.info { "Gradio TTS shutdown successfully" }
            } catch (e: Exception) {
                Log.error { "Error shutting down Gradio: ${e.message}" }
            }
        }
        
        Log.info { "DesktopTTSService shutdown complete" }
    }
}

/**
 * Voice information data class
 */
data class VoiceInfo(
    val modelId: String,
    val name: String,
    val language: String,
    val isDownloaded: Boolean,
    val isActive: Boolean
)

/**
 * Service status data class
 */
data class ServiceStatus(
    val isInitialized: Boolean,
    val isPlaying: Boolean,
    val currentVoice: VoiceInfo?,
    val performanceMetrics: ireader.domain.services.tts_service.piper.PerformanceMetrics,
    val simulationMode: Boolean
)
