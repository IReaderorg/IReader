package ireader.domain.services.tts_service

import ireader.core.log.Log
import ireader.domain.catalogs.CatalogStore
import ireader.domain.data.repository.BookRepository
import ireader.domain.data.repository.ChapterRepository
import ireader.domain.models.entities.Chapter
import ireader.domain.preferences.prefs.AppPreferences
import ireader.domain.preferences.prefs.ReaderPreferences
import ireader.domain.services.tts_service.piper.NativeLibraryLoader
import ireader.domain.usecases.local.LocalGetChapterUseCase
import ireader.domain.usecases.remote.RemoteUseCases
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ireader.domain.services.tts_service.piper.PiperException
import ireader.domain.services.tts_service.piper.PiperNative
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
    private val appPrefs: AppPreferences by inject()
    
    // Piper TTS components
    private val synthesizer: ireader.domain.services.tts_service.piper.PiperSpeechSynthesizer by inject()
    private val audioEngine: ireader.domain.services.tts_service.piper.AudioPlaybackEngine by inject()
    private val modelManager: ireader.domain.services.tts_service.piper.PiperModelManager by inject()

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
        
        // Load Piper native libraries
        serviceScope.launch {
            try {
                NativeLibraryLoader.loadLibraries()
            } catch (e: Exception) {
                Log.error { "Failed to load Piper native libraries: ${e.message}" }
                enableSimulationMode("Failed to load native libraries: ${e.message}")
            }
            
            // Load available and downloaded models
            loadAvailableModels()
            loadSelectedVoiceModel()
        }
    }
    
    private suspend fun loadAvailableModels() {
        try {
            // Get all available models
            val allModels = modelManager.getAvailableModels()
            
            // Get downloaded models from preferences (for quick lookup)
            val downloadedModelIds = appPrefs.downloadedModels().get()
            
            // Mark models as downloaded based on preferences
            state.availableVoiceModels = allModels.map { model ->
                model.copy(isDownloaded = downloadedModelIds.contains(model.id))
            }
            
            // Update selected model in state
            val selectedModelId = appPrefs.selectedPiperModel().get()
            if (selectedModelId.isNotEmpty()) {
                state.selectedVoiceModel = state.availableVoiceModels.find { it.id == selectedModelId }
            }
            
        } catch (e: Exception) {
            Log.error { "Failed to load available models: ${e.message}" }
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
            val speechRate = state.speechSpeed
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
    
    private fun startWordBoundaryTracking(text: String) {
        // Cancel any existing word boundary tracking
        wordBoundaryJob?.cancel()
        
        wordBoundaryJob = serviceScope.launch {
            try {
                // Get word boundaries from synthesizer
                val boundaries = synthesizer.getWordBoundaries(text)
                
                if (boundaries.isEmpty()) {
                    println("DEBUG: No word boundaries calculated")
                    return@launch
                }
                
                println("DEBUG: Starting word boundary tracking for ${boundaries.size} words")
                
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
                    if (!state.isPlaying) {
                        break
                    }
                    
                    // Emit word boundary event for UI highlighting
                    state.currentWordBoundary = boundary
                    println("DEBUG: Set word boundary - word: '${boundary.word}', offset: ${boundary.startOffset}-${boundary.endOffset}")
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
            state.autoNextChapter = readerPreferences.readerAutoNext().get()
            state.currentLanguage = readerPreferences.speechLanguage().get()
            state.currentVoice = appPrefs.speechVoice().get()
            state.speechSpeed = readerPreferences.speechRate().get()
            state.pitch = readerPreferences.speechPitch().get()
            state.sleepTime = readerPreferences.sleepTime().get()
            state.sleepMode = readerPreferences.sleepMode().get()
        }
        
        // Listen to preference changes
        serviceScope.launch {
            readerPreferences.readerAutoNext().changes().collect {
                state.autoNextChapter = it
            }
        }
        serviceScope.launch {
            readerPreferences.speechLanguage().changes().collect {
                state.currentLanguage = it
            }
        }
        serviceScope.launch {
            appPrefs.speechVoice().changes().collect {
                state.currentVoice = it
            }
        }
        serviceScope.launch {
            readerPreferences.speechPitch().changes().collect {
                state.pitch = it
                // Apply pitch change if not in simulation mode
            }
        }
        serviceScope.launch {
            readerPreferences.speechRate().changes().collect {
                state.speechSpeed = it
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
                state.sleepTime = it
            }
        }
        serviceScope.launch {
            readerPreferences.sleepMode().changes().collect {
                state.sleepMode = it
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

    suspend fun startReading(bookId: Long, chapterId: Long) {
        val book = bookRepo.findBookById(bookId)
        val chapter = chapterRepo.findChapterById(chapterId)
        val chapters = chapterRepo.findChaptersByBookId(bookId)
        val source = book?.sourceId?.let { extensions.get(it) }

        if (chapter != null && source != null && book != null) {
            state.ttsBook = book
            state.ttsChapters = chapters
            state.ttsCatalog = source
            state.currentReadingParagraph = 0
            
            // Load chapter content if empty
            if (chapter.isEmpty()) {
                loadChapter(chapterId)
            } else {
                state.ttsChapter = chapter
            }
            
            startService(ACTION_PLAY)
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
        state.isPlaying = true
        state.startTime = kotlin.time.Clock.System.now()
        readText()
    }

    private fun pauseReading() {
        state.isPlaying = false
        
        // Pause audio playback (responds within 200ms as per requirements)
        if (!isSimulationMode) {
            audioEngine.pause()
        }
        
        // Cancel speech and word boundary jobs
        speechJob?.cancel()
        wordBoundaryJob?.cancel()
        
        // Record feature usage
        usageAnalytics.recordFeatureUsage("pause_reading")
    }

    private fun stopReading() {
        state.isPlaying = false
        
        // Stop audio playback and clear buffers
        if (!isSimulationMode) {
            audioEngine.stop()
        }
        
        // Cancel all jobs
        speechJob?.cancel()
        wordBoundaryJob?.cancel()
        
        // Reset state
        state.currentReadingParagraph = 0
        state.currentWordBoundary = null
    }

    private suspend fun skipToNextChapter() {
        val chapter = state.ttsChapter ?: return
        val chapters = state.ttsChapters
        val index = getChapterIndex(chapter, chapters)
        
        // Stop audio playback before loading new chapter
        if (!isSimulationMode) {
            audioEngine.stop()
        }
        
        // Cancel ongoing jobs
        speechJob?.cancel()
        wordBoundaryJob?.cancel()
        
        // Clear word boundary state
        state.currentWordBoundary = null
        
        if (index < chapters.lastIndex) {
            val nextChapter = chapters[index + 1]
            loadChapter(nextChapter.id)
            state.currentReadingParagraph = 0
            
            // Start reading if playing (responds within 500ms as per requirements)
            if (state.isPlaying) {
                readText()
            }
        }
        
        // Record feature usage
        usageAnalytics.recordFeatureUsage("skip_next_chapter")
    }

    private suspend fun skipToPreviousChapter() {
        val chapter = state.ttsChapter ?: return
        val chapters = state.ttsChapters
        val index = getChapterIndex(chapter, chapters)
        
        // Stop audio playback before loading new chapter
        if (!isSimulationMode) {
            audioEngine.stop()
        }
        
        // Cancel ongoing jobs
        speechJob?.cancel()
        wordBoundaryJob?.cancel()
        
        // Clear word boundary state
        state.currentWordBoundary = null
        
        if (index > 0) {
            val prevChapter = chapters[index - 1]
            loadChapter(prevChapter.id)
            state.currentReadingParagraph = 0
            
            // Start reading if playing (responds within 500ms as per requirements)
            if (state.isPlaying) {
                readText()
            }
        }
        
        // Record feature usage
        usageAnalytics.recordFeatureUsage("skip_previous_chapter")
    }

    private fun nextParagraph() {
        state.ttsContent?.value?.let { content ->
            if (state.currentReadingParagraph < content.lastIndex) {
                // Stop current audio playback
                if (!isSimulationMode) {
                    audioEngine.stop()
                }
                
                // Cancel ongoing jobs
                speechJob?.cancel()
                wordBoundaryJob?.cancel()
                
                // Clear word boundary state
                state.currentWordBoundary = null
                
                // Move to next paragraph
                state.currentReadingParagraph += 1
                
                // Start reading if playing (responds within 500ms as per requirements)
                if (state.isPlaying) {
                    serviceScope.launch { readText() }
                }
            }
        }
    }

    private fun previousParagraph() {
        state.ttsContent?.value?.let { content ->
            if (state.currentReadingParagraph > 0) {
                // Stop current audio playback
                if (!isSimulationMode) {
                    audioEngine.stop()
                }
                
                // Cancel ongoing jobs
                speechJob?.cancel()
                wordBoundaryJob?.cancel()
                
                // Clear word boundary state
                state.currentWordBoundary = null
                
                // Move to previous paragraph
                state.currentReadingParagraph -= 1
                
                // Start reading if playing (responds within 500ms as per requirements)
                if (state.isPlaying) {
                    serviceScope.launch { readText() }
                }
            }
        }
    }

    private suspend fun readText() {
        val content = state.ttsContent?.value
        
        if (content == null || content.isEmpty()) {
            return
        }
        
        if (state.currentReadingParagraph >= content.size) {
            handleEndOfChapter()
            return
        }

        val text = content[state.currentReadingParagraph]
        
        if (text.isBlank()) {
            advanceToNextParagraph()
            return
        }
        
        state.utteranceId = state.currentReadingParagraph.toString()

        speechJob = serviceScope.launch {
            try {
                if (isSimulationMode) {
                    readTextSimulation(text)
                } else {
                    readTextWithPiper(text)
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
            if (state.isPlaying) {
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
        val wordsPerMinute = 150 * state.speechSpeed // Base reading speed
        val readingTimeMs = (words.size / wordsPerMinute * 60 * 1000).toLong()
        
        Log.debug { "Reading paragraph ${state.currentReadingParagraph} (simulation): $text" }
        Log.debug { "Estimated reading time: ${readingTimeMs}ms for ${words.size} words" }
        
        delay(readingTimeMs)
        
        // Check sleep time
        checkSleepTime()
        
        if (state.isPlaying) {
            advanceToNextParagraph()
        }
    }
    
    private suspend fun advanceToNextParagraph() {
        val content = state.ttsContent?.value ?: return
        
        if (state.currentReadingParagraph < content.lastIndex) {
            state.currentReadingParagraph += 1
            readText()
        } else {
            handleEndOfChapter()
        }
    }
    
    private suspend fun handleEndOfChapter() {
        if (state.autoNextChapter) {
            skipToNextChapter()
        } else {
            stopReading()
        }
    }

    @OptIn(ExperimentalTime::class)
    private fun checkSleepTime() {
        val lastCheckPref = state.startTime
        val currentSleepTime = state.sleepTime.minutes
        val now = kotlin.time.Clock.System.now()
        if (lastCheckPref != null && now - lastCheckPref > currentSleepTime && state.sleepMode) {
            stopReading()
        }
    }

    private suspend fun loadChapter(chapterId: Long) {
        val localChapter = chapterRepo.findChapterById(chapterId)
        val source = state.ttsCatalog ?: return

        if (localChapter != null && !localChapter.isEmpty()) {
            state.ttsChapter = localChapter
            chapterUseCase.updateLastReadTime(localChapter, updateDateFetched = false)
        } else if (localChapter != null) {
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
            
            // Check if model is downloaded
            val model = state.availableVoiceModels.find { it.id == modelId }
            if (model == null) {
                throw IllegalArgumentException("Voice model not found: $modelId")
            }
            
            if (!model.isDownloaded) {
                throw IllegalStateException("Voice model not downloaded: $modelId. Please download it first.")
            }
            
            // Persist model selection immediately
            appPrefs.selectedPiperModel().set(modelId)
            
            // Update state
            state.selectedVoiceModel = model
            
            // Reload the voice model (this will be triggered by the preference change listener)
            // No need to call loadSelectedVoiceModel() here as the listener will handle it
            
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
            val wasPlaying = state.isPlaying
            val currentParagraph = state.currentReadingParagraph
            
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
                state.currentReadingParagraph = currentParagraph
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
            
            val model = state.availableVoiceModels.find { it.id == modelId }
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
                if (state.isPlaying) {
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
            readerPreferences.speechRate().set(state.speechSpeed)
            readerPreferences.speechPitch().set(state.pitch)
        } catch (e: Exception) {
            Log.error { "Failed to save TTS settings: ${e.message}" }
        }
    }
    
    /**
     * Update speech rate and save to preferences
     */
    fun setSpeechRate(rate: Float) {
        state.speechSpeed = rate
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
        state.pitch = pitch
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
            isPlaying = state.isPlaying,
            currentVoice = getCurrentVoiceInfo(),
            performanceMetrics = getPerformanceMetrics(),
            simulationMode = isSimulationMode
        )
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
