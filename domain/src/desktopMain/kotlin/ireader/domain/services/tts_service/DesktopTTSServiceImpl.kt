package ireader.domain.services.tts_service

import ireader.domain.catalogs.CatalogStore
import ireader.domain.data.repository.BookRepository
import ireader.domain.data.repository.ChapterRepository
import ireader.domain.preferences.prefs.AppPreferences
import ireader.domain.preferences.prefs.ReaderPreferences
import ireader.domain.usecases.remote.RemoteUseCases
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent

/**
 * Desktop TTS Service Implementation
 * Extends BaseTTSService with Desktop-specific functionality
 */
class DesktopTTSServiceImpl(
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
    
    private var currentEngineType: EngineType = EngineType.PIPER
    
    enum class EngineType {
        PIPER,
        KOKORO,
        MAYA,
        GRADIO
    }
    
    override suspend fun initializePlatformComponents() {
        // Platform-specific initialization
        // Engine and notification will be created on-demand
    }
    
    override fun createTTSEngine(): TTSEngine {
        // For Desktop, default to Piper (fastest)
        // User can switch engines through preferences
        val selectedEngine = "piper" // Default to Piper for now
        
        return when (selectedEngine) {
            "kokoro" -> {
                currentEngineType = EngineType.KOKORO
                DesktopTTSEngines.createKokoroEngine() ?: createFallbackEngine()
            }
            "maya" -> {
                currentEngineType = EngineType.MAYA
                DesktopTTSEngines.createMayaEngine() ?: createFallbackEngine()
            }
            "gradio" -> {
                currentEngineType = EngineType.GRADIO
                val activeConfigId = appPrefs.activeGradioConfigId().get()
                val config = GradioTTSPresets.getPresetById(activeConfigId) ?: GradioTTSPresets.COQUI_IREADER
                TTSEngineFactory.createGradioEngine(config) ?: createFallbackEngine()
            }
            else -> {
                currentEngineType = EngineType.PIPER
                TTSEngineFactory.createNativeEngine() // Piper is "native" for Desktop
            }
        }
    }
    
    private fun createFallbackEngine(): TTSEngine {
        currentEngineType = EngineType.PIPER
        return TTSEngineFactory.createNativeEngine()
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
                // Bring window to front - handled by notification
            }
        })
    }
    
    override fun getPlatformAvailableEngines(): List<String> {
        val engines = mutableListOf<String>()
        
        engines.add("Piper TTS")
        
        // Check if Kokoro is available
        // This would need actual availability check
        engines.add("Kokoro TTS (optional)")
        
        // Check if Maya is available
        engines.add("Maya TTS (optional)")
        
        // Gradio TTS (Online) - includes Coqui and other Hugging Face Spaces
        engines.add("Gradio TTS (Online)")
        
        return engines
    }
    
    override suspend fun precacheNextParagraphs() {
        // Desktop engines don't support pre-caching yet
        // Could be added for Coqui HTTP TTS in the future
    }
}

/**
 * Extension functions for TTSEngineFactory to create Desktop engines
 */
private fun TTSEngineFactory.createKokoroEngine(): TTSEngine? {
    // This would need to be added to the factory
    // For now, return null to fallback to Piper
    return null
}

private fun TTSEngineFactory.createMayaEngine(): TTSEngine? {
    // This would need to be added to the factory
    // For now, return null to fallback to Piper
    return null
}
