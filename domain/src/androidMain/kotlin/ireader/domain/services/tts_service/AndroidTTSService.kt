package ireader.domain.services.tts_service

import android.content.Context
import ireader.domain.catalogs.CatalogStore
import ireader.domain.data.repository.BookRepository
import ireader.domain.data.repository.ChapterRepository
import ireader.domain.preferences.prefs.AppPreferences
import ireader.domain.preferences.prefs.ReaderPreferences
import ireader.domain.usecases.remote.RemoteUseCases
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Android TTS Service Implementation
 * Extends BaseTTSService with Android-specific functionality
 */
class AndroidTTSService(
    private val context: Context,
    bookRepo: BookRepository,
    chapterRepo: ChapterRepository,
    extensions: CatalogStore,
    remoteUseCases: RemoteUseCases,
    readerPreferences: ReaderPreferences,
    appPrefs: AppPreferences
) : BaseTTSService(
    bookRepo,
    chapterRepo,
    extensions,
    remoteUseCases,
    readerPreferences,
    appPrefs
), KoinComponent {
    
    private var currentEngineType: EngineType = EngineType.NATIVE
    
    enum class EngineType {
        NATIVE,
        GRADIO
    }
    
    override suspend fun initializePlatformComponents() {
        // Platform-specific initialization
        // Engine and notification will be created on-demand
    }
    
    override fun createTTSEngine(): TTSEngine {
        // Check for Gradio TTS (unified online TTS system)
        val useGradioTTS = appPrefs.useGradioTTS().get()
        val activeGradioConfigId = appPrefs.activeGradioConfigId().get()
        
        if (useGradioTTS && activeGradioConfigId.isNotEmpty()) {
            // Try to load Gradio config
            val gradioConfig = loadGradioConfig(activeGradioConfigId)
            if (gradioConfig != null && gradioConfig.spaceUrl.isNotEmpty()) {
                currentEngineType = EngineType.GRADIO
                val engine = TTSEngineFactory.createGradioEngine(gradioConfig)
                if (engine != null) {
                    return engine
                }
            }
        }
        
        // Fall back to native TTS
        currentEngineType = EngineType.NATIVE
        return TTSEngineFactory.createNativeEngine()
    }
    
    /**
     * Load Gradio TTS configuration from preferences
     */
    private fun loadGradioConfig(configId: String): GradioTTSConfig? {
        return try {
            val configsJson = appPrefs.gradioTTSConfigs().get()
            if (configsJson.isEmpty()) {
                // Return preset if available
                GradioTTSPresets.getPresetById(configId)
            } else {
                val state = kotlinx.serialization.json.Json.decodeFromString<GradioTTSManagerState>(configsJson)
                state.configs.find { it.id == configId } ?: GradioTTSPresets.getPresetById(configId)
            }
        } catch (e: Exception) {
            ireader.core.log.Log.error { "Failed to load Gradio config: ${e.message}" }
            GradioTTSPresets.getPresetById(configId)
        }
    }
    
    override fun createNotification(): TTSNotification {
        return TTSNotificationFactory.create(object : TTSNotificationCallback {
            override fun onPlay() {
                scope.launch { play() }
            }
            
            override fun onPause() {
                scope.launch { pause() }
            }
            
            override fun onNext() {
                scope.launch { nextChapter() }
            }
            
            override fun onPrevious() {
                scope.launch { previousChapter() }
            }
            
            override fun onNextParagraph() {
                scope.launch { nextParagraph() }
            }
            
            override fun onPreviousParagraph() {
                scope.launch { previousParagraph() }
            }
            
            override fun onClose() {
                scope.launch { stop() }
            }
            
            override fun onNotificationClick() {
                // Open TTS screen - handled by notification intent
            }
        })
    }
    
    override suspend fun precacheNextParagraphs() {
        // Only cache for Gradio TTS
        if (currentEngineType == EngineType.NATIVE) return
        
        val content = state.currentContent.value
        val current = state.currentParagraph.value
        
        val nextParagraphs = mutableListOf<Pair<String, String>>()
        
        // Cache next 3 paragraphs
        for (i in 1..3) {
            val nextIndex = current + i
            if (nextIndex < content.size) {
                nextParagraphs.add(nextIndex.toString() to content[nextIndex])
            }
        }
        
        if (nextParagraphs.isEmpty()) return
        
        // Handle Gradio engine
        val gradioEngine = ttsEngine as? AndroidGradioTTSEngine ?: return
        gradioEngine.precacheParagraphs(nextParagraphs)
        
        // Update cache status after a delay
        scope.launch {
            delay(500)
            for (i in 1..3) {
                val nextIndex = current + i
                if (nextIndex < content.size) {
                    gradioEngine.getCacheStatus(nextIndex.toString())
                }
            }
        }
    }
    
    override fun getPlatformAvailableEngines(): List<String> {
        val engines = mutableListOf("Native Android TTS")
        
        // Check for Gradio TTS
        val useGradioTTS = appPrefs.useGradioTTS().get()
        val activeGradioConfigId = appPrefs.activeGradioConfigId().get()
        if (useGradioTTS && activeGradioConfigId.isNotEmpty()) {
            val config = loadGradioConfig(activeGradioConfigId)
            if (config != null) {
                engines.add("Gradio TTS (${config.name})")
            }
        }
        
        return engines
    }
}
