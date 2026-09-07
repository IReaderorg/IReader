package ireader.presentation.ui.settings.audio

import androidx.compose.runtime.Stable
import io.ktor.client.statement.*
import ireader.core.log.Log
import ireader.domain.models.tts.PiperVoice
import ireader.domain.preferences.prefs.AppPreferences
import ireader.domain.preferences.prefs.ReaderPreferences
import ireader.domain.services.platform.PlatformCapabilities
import ireader.domain.services.platform.PlatformType
import ireader.domain.services.tts_service.GenericGradioTTSEngine
import ireader.domain.services.tts_service.GradioTTSConfig
import ireader.domain.services.tts_service.GradioTTSManager
import ireader.domain.services.tts_service.GradioTTSPresets
import ireader.domain.services.tts_service.PiperVoiceDownloader
import ireader.domain.services.tts_service.PiperVoiceService
import ireader.domain.services.tts_service.TTSChapterCache
import ireader.domain.services.tts_service.TTSEngineCallback
import ireader.domain.services.tts_service.local.LocalTTSApiFormat
import ireader.domain.services.tts_service.local.LocalTTSConfig
import ireader.domain.services.tts_service.local.LocalTTSEngine
import ireader.domain.services.tts_service.local.LocalTTSHealthResponse
import ireader.domain.services.tts_service.local.LocalTTSManager
import ireader.domain.services.tts_service.local.LocalTTSVoice
import ireader.domain.services.tts_service.v2.EngineType
import ireader.domain.services.tts_service.v2.GradioConfig
import ireader.domain.services.tts_service.v2.TTSCommand
import ireader.domain.services.tts_service.v2.TTSController
import ireader.presentation.ui.core.viewmodel.StateViewModel
import ireader.presentation.ui.settings.viewmodels.TestResult
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

enum class AudioEngineType {
    DEVICE_TTS,
    PIPER_NEURAL,
    KOKORO_NEURAL,
    GRADIO_AI,
    LOCAL_SERVER
}

@Stable
data class AudioStudioState(
    val platformType: PlatformType = PlatformType.DESKTOP,
    val availableEngines: List<AudioEngineType> = listOf(AudioEngineType.PIPER_NEURAL, AudioEngineType.KOKORO_NEURAL, AudioEngineType.GRADIO_AI, AudioEngineType.LOCAL_SERVER),
    val selectedEngine: AudioEngineType = AudioEngineType.PIPER_NEURAL,
    val speechRate: Float = 1.0f,
    val speechPitch: Float = 1.0f,
    val selectedVoiceName: String = "Default Voice",
    val availableVoices: List<String> = emptyList(),

    // Playback settings
    val autoNextChapter: Boolean = true,
    val autoScrollWithSpeech: Boolean = true,
    val skipBlankLines: Boolean = true,
    val sleepTimerMinutes: Int = 0,
    val isPlayingSample: Boolean = false,
    val sampleText: String = "The quick brown fox jumps over the lazy dog.",
    val highlightColorHex: Long = 0xFF6200EE,

    // Cloud / Gradio Model Management
    val cloudConfigs: List<GradioTTSConfig> = emptyList(),
    val activeCloudConfigId: String? = null,
    val isTestingCloudConfig: Boolean = false,
    val cloudTestResult: TestResult? = null,
    val editingCloudConfig: GradioTTSConfig? = null,
    val isEditCloudDialogOpen: Boolean = false,

    // Local Server TTS (Chatterbox / Local AI)
    val localServerUrl: String = "http://127.0.0.1:8000",
    val localServerVoice: String = "default",
    val localServerApiFormat: LocalTTSApiFormat = LocalTTSApiFormat.SIMPLE_REST,
    val localServerApiKey: String = "",
    val localServerVoices: List<LocalTTSVoice> = LocalTTSManager.DEFAULT_VOICES,
    val isTestingLocalServer: Boolean = false,
    val localServerHealth: LocalTTSHealthResponse? = null,
    val localServerError: String? = null,

    // Piper Voice Management (Desktop)
    val piperVoices: List<PiperVoice> = emptyList(),
    val availablePiperLanguages: List<String> = emptyList(),
    val selectedPiperLanguage: String? = null,
    val downloadingPiperVoiceId: String? = null,
    val piperDownloadProgress: Float = 0f,
    val isRefreshingPiperVoices: Boolean = false,
    val piperErrorMessage: String? = null,

    // Text Merging & Caching Settings
    val mergeWordsRemote: Int = 0,
    val mergeWordsNative: Int = 0,
    val chapterCacheEnabled: Boolean = false,
    val chapterCacheDays: Int = 7,
    val cacheEntryCount: Int = 0,
    val cacheSizeMB: Float = 0f,

    // Installed TTS Engine Plugins (from Feature Store)
    val installedTTSPlugins: List<ireader.domain.plugins.PluginInfo> = emptyList()
)

class AudioStudioViewModel(
    private val readerPreferences: ReaderPreferences,
    private val appPreferences: AppPreferences,
    private val platformCapabilities: PlatformCapabilities? = null,
    private val gradioTTSManager: GradioTTSManager? = null,
    private val localTTSManager: LocalTTSManager? = null,
    private val piperVoiceService: PiperVoiceService? = null,
    private val piperVoiceDownloader: PiperVoiceDownloader? = null,
    private val chapterCache: TTSChapterCache? = null,
    private val ttsController: TTSController? = null,
    private val pluginManager: ireader.domain.plugins.PluginManager? = null,
    private val pluginTTSManager: ireader.domain.services.tts.PluginTTSManager? = null
) : StateViewModel<AudioStudioState>(AudioStudioState()) {

    private var samplePlaybackJob: Job? = null
    private var sampleGradioEngine: GenericGradioTTSEngine? = null
    private var sampleLocalEngine: LocalTTSEngine? = null

    init {
        val platform = platformCapabilities?.platformType ?: PlatformType.DESKTOP
        val availableEngines = when (platform) {
            PlatformType.ANDROID -> listOf(AudioEngineType.DEVICE_TTS, AudioEngineType.GRADIO_AI, AudioEngineType.LOCAL_SERVER)
            PlatformType.DESKTOP -> listOf(AudioEngineType.PIPER_NEURAL, AudioEngineType.KOKORO_NEURAL, AudioEngineType.GRADIO_AI, AudioEngineType.LOCAL_SERVER)
            PlatformType.IOS -> listOf(AudioEngineType.DEVICE_TTS, AudioEngineType.GRADIO_AI, AudioEngineType.LOCAL_SERVER)
            else -> listOf(AudioEngineType.DEVICE_TTS, AudioEngineType.GRADIO_AI, AudioEngineType.LOCAL_SERVER)
        }

        updateState {
            it.copy(
                platformType = platform,
                availableEngines = availableEngines
            )
        }

        loadSettings()
        observeCloudConfigs()
        observeLocalTTSConfig()
        observePiperVoices()
        observeTTSPlugins()
        refreshCacheStats()
    }

    private fun observeTTSPlugins() {
        pluginManager?.let { pm ->
            scope.launch {
                pm.pluginsFlow.collectLatest { plugins ->
                    val ttsPlugins = plugins.filter {
                        it.manifest.type == ireader.plugin.api.PluginType.TTS || 
                        it.manifest.type == ireader.plugin.api.PluginType.GRADIO_TTS
                    }
                    updateState { it.copy(installedTTSPlugins = ttsPlugins) }
                    gradioTTSManager?.reloadPluginConfigs()
                    loadPluginVoices()
                }
            }
        }
    }

    private fun loadPluginVoices() {
        val pluginVoices = pluginTTSManager?.getPluginVoices()?.map { it.voice.name } ?: emptyList()
        if (pluginVoices.isNotEmpty()) {
            updateState {
                val combined = (it.availableVoices + pluginVoices).distinct()
                it.copy(availableVoices = combined)
            }
        }
    }

    private fun loadSettings() {
        val rate = readerPreferences.speechRate().get()
        val pitch = readerPreferences.speechPitch().get()
        val autoNext = readerPreferences.autoNextChapter().get()
        val sleepTimer = readerPreferences.sleepTime().get().toInt()
        val voice = readerPreferences.speechVoice().get()

        val useLocal = appPreferences.useLocalTTS().get()
        val useAI = appPreferences.useAITTS().get() || appPreferences.useGradioTTS().get()
        val platform = state.value.platformType

        val defaultEngine = when {
            useLocal -> AudioEngineType.LOCAL_SERVER
            useAI -> AudioEngineType.GRADIO_AI
            platform == PlatformType.DESKTOP -> AudioEngineType.PIPER_NEURAL
            else -> AudioEngineType.DEVICE_TTS
        }

        val mergeRemote = readerPreferences.ttsMergeWordsRemote().get()
        val mergeNative = readerPreferences.ttsMergeWordsNative().get()
        val cacheEnabled = readerPreferences.ttsChapterCacheEnabled().get()
        val cacheDays = readerPreferences.ttsChapterCacheDays().get()

        updateState {
            it.copy(
                selectedEngine = defaultEngine,
                speechRate = if (rate > 0f) rate else 1.0f,
                speechPitch = if (pitch > 0f) pitch else 1.0f,
                selectedVoiceName = if (voice.isNotBlank()) voice else "Default Voice",
                autoNextChapter = autoNext,
                sleepTimerMinutes = sleepTimer,
                mergeWordsRemote = mergeRemote,
                mergeWordsNative = mergeNative,
                chapterCacheEnabled = cacheEnabled,
                chapterCacheDays = cacheDays
            )
        }

        syncEngineToController(defaultEngine)
    }

    private fun syncEngineToController(engine: AudioEngineType) {
        ttsController?.let { controller ->
            when (engine) {
                AudioEngineType.LOCAL_SERVER -> {
                    val config = localTTSManager?.config?.value ?: LocalTTSConfig(
                        serverUrl = state.value.localServerUrl,
                        voice = state.value.localServerVoice,
                        speed = state.value.speechRate,
                        pitch = state.value.speechPitch,
                        apiKey = state.value.localServerApiKey.ifBlank { null },
                        apiFormat = state.value.localServerApiFormat
                    )
                    controller.dispatch(TTSCommand.SetLocalConfig(config))
                    controller.dispatch(TTSCommand.SetEngine(EngineType.LOCAL))
                }
                AudioEngineType.GRADIO_AI -> {
                    val activeConfig = state.value.activeCloudConfigId?.let { gradioTTSManager?.getConfigById(it) }
                        ?: gradioTTSManager?.getActiveConfig()
                        ?: state.value.cloudConfigs.firstOrNull()

                    if (activeConfig != null) {
                        val gradioConfig = GradioConfig(
                            id = activeConfig.id,
                            name = activeConfig.name,
                            spaceUrl = activeConfig.spaceUrl,
                            apiName = activeConfig.apiName,
                            enabled = activeConfig.enabled,
                            originalConfig = activeConfig
                        )
                        controller.dispatch(TTSCommand.SetGradioConfig(gradioConfig))
                    }
                    controller.dispatch(TTSCommand.SetEngine(EngineType.GRADIO))
                }
                else -> {
                    controller.dispatch(TTSCommand.SetEngine(EngineType.NATIVE))
                }
            }
        }
    }

    private fun observeLocalTTSConfig() {
        localTTSManager?.let { manager ->
            scope.launch {
                manager.config.collectLatest { config ->
                    updateState {
                        it.copy(
                            localServerUrl = config.serverUrl,
                            localServerVoice = config.voice,
                            localServerApiFormat = config.apiFormat,
                            localServerApiKey = config.apiKey ?: ""
                        )
                    }
                }
            }
        }
    }

    private fun observeCloudConfigs() {
        gradioTTSManager?.let { manager ->
            scope.launch {
                manager.configs.collectLatest { configs ->
                    updateState { it.copy(cloudConfigs = configs) }
                }
            }
            scope.launch {
                manager.activeConfigId.collectLatest { activeId ->
                    updateState { it.copy(activeCloudConfigId = activeId) }
                    if (state.value.selectedEngine == AudioEngineType.GRADIO_AI) {
                        syncEngineToController(AudioEngineType.GRADIO_AI)
                    }
                }
            }
        }
    }

    private fun observePiperVoices() {
        piperVoiceService?.let { service ->
            scope.launch {
                try {
                    service.initialize()
                } catch (e: Exception) {
                    Log.warn { "PiperVoiceService initialization warning: ${e.message}" }
                }
            }
            scope.launch {
                service.subscribeAll().collectLatest { voices ->
                    val languages = voices.map { it.language }.distinct().sorted()
                    updateState {
                        it.copy(
                            piperVoices = voices,
                            availablePiperLanguages = languages
                        )
                    }
                }
            }
        }
    }

    fun setEngine(engine: AudioEngineType) {
        val isAI = engine == AudioEngineType.GRADIO_AI
        val isLocal = engine == AudioEngineType.LOCAL_SERVER
        appPreferences.useAITTS().set(isAI)
        appPreferences.useGradioTTS().set(isAI)
        appPreferences.useLocalTTS().set(isLocal)
        updateState { it.copy(selectedEngine = engine) }
        syncEngineToController(engine)
    }

    fun setSpeechRate(rate: Float) {
        val clamped = rate.coerceIn(0.5f, 3.0f)
        readerPreferences.speechRate().set(clamped)
        updateState { it.copy(speechRate = clamped) }
    }

    fun setSpeechPitch(pitch: Float) {
        val clamped = pitch.coerceIn(0.5f, 2.0f)
        readerPreferences.speechPitch().set(clamped)
        updateState { it.copy(speechPitch = clamped) }
    }

    fun setVoice(voice: String) {
        readerPreferences.speechVoice().set(voice)
        updateState { it.copy(selectedVoiceName = voice) }
    }

    fun toggleAutoNext(enabled: Boolean) {
        readerPreferences.autoNextChapter().set(enabled)
        updateState { it.copy(autoNextChapter = enabled) }
    }

    fun toggleAutoScroll(enabled: Boolean) {
        updateState { it.copy(autoScrollWithSpeech = enabled) }
    }

    fun toggleSkipBlankLines(enabled: Boolean) {
        updateState { it.copy(skipBlankLines = enabled) }
    }

    fun setSleepTimer(minutes: Int) {
        readerPreferences.sleepTime().set(minutes.toLong())
        updateState { it.copy(sleepTimerMinutes = minutes) }
    }

    fun setSampleText(text: String) {
        updateState { it.copy(sampleText = text) }
    }

    fun togglePlaySample() {
        if (state.value.isPlayingSample) {
            samplePlaybackJob?.cancel()
            samplePlaybackJob = null
            sampleGradioEngine?.stop()
            sampleGradioEngine?.cleanup()
            sampleGradioEngine = null
            sampleLocalEngine?.stop()
            sampleLocalEngine?.cleanup()
            sampleLocalEngine = null
            ttsController?.dispatch(TTSCommand.Stop)
            updateState { it.copy(isPlayingSample = false) }
        } else {
            samplePlaybackJob?.cancel()
            updateState { it.copy(isPlayingSample = true) }
            samplePlaybackJob = scope.launch {
                try {
                    val sampleText = state.value.sampleText.ifBlank { "The quick brown fox jumps over the lazy dog." }
                    
                    if (state.value.selectedEngine == AudioEngineType.LOCAL_SERVER) {
                        // 1. Local Server TTS (Chatterbox / Local AI)
                        if (localTTSManager != null) {
                            val currentCfg = localTTSManager.config.value.copy(
                                serverUrl = state.value.localServerUrl,
                                voice = state.value.localServerVoice,
                                speed = state.value.speechRate,
                                pitch = state.value.speechPitch,
                                apiKey = state.value.localServerApiKey.ifBlank { null },
                                apiFormat = state.value.localServerApiFormat
                            )
                            val engine = localTTSManager.createEngine(currentCfg)
                            sampleLocalEngine = engine
                            engine.setSpeed(state.value.speechRate)
                            engine.setPitch(state.value.speechPitch)

                            val completer = CompletableDeferred<Unit>()
                            engine.setCallback(object : TTSEngineCallback {
                                override fun onStart(utteranceId: String) {}
                                override fun onDone(utteranceId: String) {
                                    completer.complete(Unit)
                                }
                                override fun onError(utteranceId: String, error: String) {
                                    Log.error { "Local TTS sample playback error: $error" }
                                    completer.complete(Unit)
                                }
                            })
                            engine.speak(sampleText, "sample_preview")

                            withTimeoutOrNull(25_000L) {
                                completer.await()
                            }
                        } else if (ttsController != null) {
                            syncEngineToController(AudioEngineType.LOCAL_SERVER)
                            ttsController.dispatch(TTSCommand.SetSpeed(state.value.speechRate))
                            ttsController.dispatch(TTSCommand.SetPitch(state.value.speechPitch))
                            ttsController.dispatch(TTSCommand.SetContent(listOf(sampleText)))
                            ttsController.dispatch(TTSCommand.Play)

                            val wordCount = sampleText.split("\\s+".toRegex()).size.coerceAtLeast(1)
                            val estimatedDurationMs = ((wordCount * 60_000f / (150f * state.value.speechRate)).toLong()).coerceIn(2000L, 15000L)
                            delay(estimatedDurationMs)
                        } else {
                            val durationMs = ((3000f / state.value.speechRate).toLong()).coerceAtLeast(500L)
                            delay(durationMs)
                        }
                    } else if (state.value.selectedEngine == AudioEngineType.GRADIO_AI) {
                        // 2. Cloud AI Audio Synthesis
                        val activeConfig = state.value.activeCloudConfigId?.let { gradioTTSManager?.getConfigById(it) }
                            ?: gradioTTSManager?.getActiveConfig()
                            ?: state.value.cloudConfigs.firstOrNull()

                        if (activeConfig != null && gradioTTSManager != null) {
                            val engine = gradioTTSManager.createEngine(activeConfig)
                            sampleGradioEngine = engine
                            engine.setSpeed(state.value.speechRate)
                            engine.setPitch(state.value.speechPitch)

                            val completer = CompletableDeferred<Unit>()
                            engine.setCallback(object : TTSEngineCallback {
                                override fun onStart(utteranceId: String) {}
                                override fun onDone(utteranceId: String) {
                                    completer.complete(Unit)
                                }
                                override fun onError(utteranceId: String, error: String) {
                                    Log.error { "Gradio sample playback error: $error" }
                                    completer.complete(Unit)
                                }
                            })
                            engine.speak(sampleText, "sample_preview")

                            withTimeoutOrNull(25_000L) {
                                completer.await()
                            }
                        } else if (ttsController != null) {
                            syncEngineToController(AudioEngineType.GRADIO_AI)
                            ttsController.dispatch(TTSCommand.SetSpeed(state.value.speechRate))
                            ttsController.dispatch(TTSCommand.SetPitch(state.value.speechPitch))
                            ttsController.dispatch(TTSCommand.SetContent(listOf(sampleText)))
                            ttsController.dispatch(TTSCommand.Play)

                            val wordCount = sampleText.split("\\s+".toRegex()).size.coerceAtLeast(1)
                            val estimatedDurationMs = ((wordCount * 60_000f / (150f * state.value.speechRate)).toLong()).coerceIn(2000L, 15000L)
                            delay(estimatedDurationMs)
                        } else {
                            val durationMs = ((3000f / state.value.speechRate).toLong()).coerceAtLeast(500L)
                            delay(durationMs)
                        }
                    } else {
                        // 3. Native System TTS or Desktop Neural Piper
                        if (ttsController != null) {
                            syncEngineToController(state.value.selectedEngine)
                            ttsController.dispatch(TTSCommand.SetSpeed(state.value.speechRate))
                            ttsController.dispatch(TTSCommand.SetPitch(state.value.speechPitch))
                            ttsController.dispatch(TTSCommand.SetContent(listOf(sampleText)))
                            ttsController.dispatch(TTSCommand.Play)

                            val wordCount = sampleText.split("\\s+".toRegex()).size.coerceAtLeast(1)
                            val estimatedDurationMs = ((wordCount * 60_000f / (150f * state.value.speechRate)).toLong()).coerceIn(2000L, 8000L)
                            delay(estimatedDurationMs)
                        } else {
                            val durationMs = ((3000f / state.value.speechRate).toLong()).coerceAtLeast(500L)
                            delay(durationMs)
                        }
                    }
                } catch (e: Exception) {
                    Log.error { "Sample playback failed: ${e.message}" }
                } finally {
                    sampleGradioEngine?.stop()
                    sampleGradioEngine?.cleanup()
                    sampleGradioEngine = null
                    sampleLocalEngine?.stop()
                    sampleLocalEngine?.cleanup()
                    sampleLocalEngine = null
                    ttsController?.dispatch(TTSCommand.Stop)
                    updateState { it.copy(isPlayingSample = false) }
                }
            }
        }
    }

    fun resetRateAndPitch() {
        setSpeechRate(1.0f)
        setSpeechPitch(1.0f)
    }

    // ==================== Cloud / Gradio TTS Methods ====================

    fun selectCloudConfig(configId: String) {
        scope.launch {
            gradioTTSManager?.setActiveConfig(configId)
            appPreferences.activeGradioConfigId().set(configId)
            val config = gradioTTSManager?.getConfigById(configId)
            val voiceName = config?.name ?: "Cloud Model: $configId"
            readerPreferences.speechVoice().set(voiceName)
            updateState { it.copy(activeCloudConfigId = configId, selectedVoiceName = voiceName) }

            if (config != null && state.value.selectedEngine == AudioEngineType.GRADIO_AI) {
                val gradioConfig = GradioConfig(
                    id = config.id,
                    name = config.name,
                    spaceUrl = config.spaceUrl,
                    apiName = config.apiName,
                    enabled = config.enabled,
                    originalConfig = config
                )
                ttsController?.dispatch(TTSCommand.SetGradioConfig(gradioConfig))
            }
        }
    }

    fun testCloudConfig(configId: String) {
        scope.launch {
            updateState { it.copy(isTestingCloudConfig = true, cloudTestResult = null) }
            try {
                val result = gradioTTSManager?.testConfig(configId)
                val testResult = if (result != null && result.isSuccess) {
                    TestResult.Success
                } else {
                    TestResult.Error(result?.exceptionOrNull()?.message ?: "Failed to connect to Gradio space")
                }
                updateState { it.copy(isTestingCloudConfig = false, cloudTestResult = testResult) }
            } catch (e: Exception) {
                updateState { it.copy(isTestingCloudConfig = false, cloudTestResult = TestResult.Error(e.message ?: "Unknown error")) }
            }
        }
    }

    fun clearCloudTestResult() {
        updateState { it.copy(cloudTestResult = null) }
    }

    fun openEditCloudDialog(config: GradioTTSConfig? = null) {
        val toEdit = config ?: GradioTTSPresets.createCustomTemplate()
        updateState { it.copy(editingCloudConfig = toEdit, isEditCloudDialogOpen = true) }
    }

    fun dismissEditCloudDialog() {
        updateState { it.copy(editingCloudConfig = null, isEditCloudDialogOpen = false) }
    }

    fun saveCloudConfig(config: GradioTTSConfig) {
        scope.launch {
            if (gradioTTSManager?.getConfigById(config.id) != null) {
                gradioTTSManager.updateConfig(config)
            } else {
                gradioTTSManager?.addCustomConfig(config)
            }
            updateState { it.copy(editingCloudConfig = null, isEditCloudDialogOpen = false) }
        }
    }

    fun autoDetectCloudSpace(rawUrl: String, apiKey: String? = null, onResult: (Result<GradioTTSConfig>) -> Unit) {
        scope.launch {
            val result = gradioTTSManager?.autoDetectSpace(rawUrl, apiKey)
                ?: Result.failure(Exception("Gradio manager not initialized"))
            onResult(result)
        }
    }

    fun testCustomCloudConfig(config: GradioTTSConfig, onResult: (Result<ByteArray>) -> Unit) {
        scope.launch {
            val result = gradioTTSManager?.testCustomConfig(config)
                ?: Result.failure(Exception("Gradio manager not initialized"))
            onResult(result)
        }
    }

    fun deleteCloudConfig(configId: String) {
        scope.launch {
            gradioTTSManager?.deleteConfig(configId)
        }
    }

    // ==================== Piper Voice Methods (Desktop) ====================

    fun filterPiperLanguage(language: String?) {
        updateState { it.copy(selectedPiperLanguage = language) }
    }

    fun selectPiperVoice(voice: PiperVoice) {
        scope.launch {
            appPreferences.selectedAIVoiceId().set(voice.id)
            readerPreferences.speechVoice().set(voice.name)
            updateState { it.copy(selectedVoiceName = voice.name) }
        }
    }

    fun downloadPiperVoice(voice: PiperVoice) {
        val downloader = piperVoiceDownloader ?: return
        scope.launch {
            updateState { it.copy(downloadingPiperVoiceId = voice.id, piperDownloadProgress = 0f, piperErrorMessage = null) }
            try {
                downloader.downloadVoice(voice) { progress ->
                    updateState { it.copy(piperDownloadProgress = progress) }
                }
                piperVoiceService?.refresh()
            } catch (e: Exception) {
                Log.error { "Download voice failed: ${e.message}" }
                updateState { it.copy(piperErrorMessage = e.message) }
            } finally {
                updateState { it.copy(downloadingPiperVoiceId = null, piperDownloadProgress = 0f) }
            }
        }
    }

    fun deletePiperVoice(voice: PiperVoice) {
        scope.launch {
            piperVoiceDownloader?.deleteVoice(voice.id)
            piperVoiceService?.refresh()
        }
    }

    fun refreshPiperVoices() {
        scope.launch {
            updateState { it.copy(isRefreshingPiperVoices = true) }
            try {
                piperVoiceService?.refresh()
            } finally {
                updateState { it.copy(isRefreshingPiperVoices = false) }
            }
        }
    }


    // ==================== Text Merging & Caching Methods ====================

    fun setMergeWordsRemote(value: Int) {
        val clamped = value.coerceIn(0, 500)
        readerPreferences.ttsMergeWordsRemote().set(clamped)
        updateState { it.copy(mergeWordsRemote = clamped) }
        scope.launch {
            if (clamped > 0) {
                ttsController?.dispatch(TTSCommand.EnableChunkMode(clamped))
            } else {
                ttsController?.dispatch(TTSCommand.DisableChunkMode)
            }
        }
    }

    fun setMergeWordsNative(value: Int) {
        val clamped = value.coerceIn(0, 500)
        readerPreferences.ttsMergeWordsNative().set(clamped)
        updateState { it.copy(mergeWordsNative = clamped) }
    }

    fun setChapterCacheEnabled(enabled: Boolean) {
        readerPreferences.ttsChapterCacheEnabled().set(enabled)
        updateState { it.copy(chapterCacheEnabled = enabled) }
    }

    fun setChapterCacheDays(days: Int) {
        val clamped = days.coerceIn(1, 30)
        readerPreferences.ttsChapterCacheDays().set(clamped)
        updateState { it.copy(chapterCacheDays = clamped) }
    }

    fun clearChapterCache() {
        chapterCache?.clearAll()
        refreshCacheStats()
    }

    fun refreshCacheStats() {
        val stats = chapterCache?.getCacheStats()
        if (stats != null) {
            updateState { it.copy(cacheEntryCount = stats.entryCount, cacheSizeMB = stats.totalSizeMB) }
        }
    }

    // ==================== Local Server TTS Methods ====================

    fun setLocalServerUrl(url: String) {
        updateState { it.copy(localServerUrl = url, localServerError = null) }
        persistLocalConfig()
    }

    fun setLocalServerVoice(voiceId: String) {
        val voiceName = state.value.localServerVoices.find { it.id == voiceId }?.name ?: voiceId
        readerPreferences.speechVoice().set(voiceName)
        updateState { it.copy(localServerVoice = voiceId, selectedVoiceName = voiceName) }
        persistLocalConfig()
    }

    fun setLocalServerApiFormat(format: LocalTTSApiFormat) {
        updateState { it.copy(localServerApiFormat = format) }
        persistLocalConfig()
    }

    fun setLocalServerApiKey(key: String) {
        updateState { it.copy(localServerApiKey = key) }
        persistLocalConfig()
    }

    private fun persistLocalConfig() {
        val current = localTTSManager?.config?.value ?: LocalTTSConfig()
        val updated = current.copy(
            serverUrl = state.value.localServerUrl,
            voice = state.value.localServerVoice,
            apiFormat = state.value.localServerApiFormat,
            apiKey = state.value.localServerApiKey.ifBlank { null },
            speed = state.value.speechRate,
            pitch = state.value.speechPitch
        )
        localTTSManager?.updateConfig(updated)
        if (state.value.selectedEngine == AudioEngineType.LOCAL_SERVER) {
            syncEngineToController(AudioEngineType.LOCAL_SERVER)
        }
    }

    fun testLocalServerConnection() {
        scope.launch {
            updateState { it.copy(isTestingLocalServer = true, localServerError = null, localServerHealth = null) }
            try {
                val result = localTTSManager?.testConnection(
                    url = state.value.localServerUrl,
                    apiKey = state.value.localServerApiKey.ifBlank { null }
                )
                if (result != null && result.isSuccess) {
                    val health = result.getOrNull()
                    updateState { it.copy(isTestingLocalServer = false, localServerHealth = health, localServerError = null) }
                    fetchLocalServerVoices()
                } else {
                    val err = result?.exceptionOrNull()?.message ?: "Failed to connect to Local TTS server"
                    updateState { it.copy(isTestingLocalServer = false, localServerError = err, localServerHealth = null) }
                }
            } catch (e: Exception) {
                updateState { it.copy(isTestingLocalServer = false, localServerError = e.message ?: "Connection error", localServerHealth = null) }
            }
        }
    }

    fun fetchLocalServerVoices() {
        scope.launch {
            try {
                val voices = localTTSManager?.fetchVoices(
                    url = state.value.localServerUrl,
                    apiKey = state.value.localServerApiKey.ifBlank { null }
                ) ?: LocalTTSManager.DEFAULT_VOICES
                updateState { it.copy(localServerVoices = voices) }
            } catch (e: Exception) {
                Log.warn { "Failed to fetch local server voices: ${e.message}" }
            }
        }
    }

    fun clearLocalServerError() {
        updateState { it.copy(localServerError = null) }
    }
}

