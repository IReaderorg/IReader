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
        COQUI
    }
    
    override suspend fun initializePlatformComponents() {
        // Platform-specific initialization
        // Engine and notification will be created on-demand
    }
    
    override fun createTTSEngine(): TTSEngine {
        val useCoquiTTS = appPrefs.useCoquiTTS().get()
        val coquiSpaceUrl = appPrefs.coquiSpaceUrl().get()
        
        return if (useCoquiTTS && coquiSpaceUrl.isNotEmpty()) {
            currentEngineType = EngineType.COQUI
            TTSEngineFactory.createCoquiEngine(
                spaceUrl = coquiSpaceUrl,
                apiKey = appPrefs.coquiApiKey().get().takeIf { it.isNotEmpty() }
            ) ?: run {
                currentEngineType = EngineType.NATIVE
                TTSEngineFactory.createNativeEngine()
            }
        } else {
            currentEngineType = EngineType.NATIVE
            TTSEngineFactory.createNativeEngine()
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
        // Only cache for Coqui TTS
        if (currentEngineType != EngineType.COQUI) return
        
        val androidEngine = ttsEngine as? AndroidCoquiTTSEngine ?: return
        val content = state.currentContent.value
        val current = state.currentParagraph.value
        
        val nextParagraphs = mutableListOf<Pair<String, String>>()
        val loadingSet = mutableSetOf<Int>()
        
        // Cache next 3 paragraphs
        for (i in 1..3) {
            val nextIndex = current + i
            if (nextIndex < content.size) {
                nextParagraphs.add(nextIndex.toString() to content[nextIndex])
                loadingSet.add(nextIndex)
            }
        }
        
        if (nextParagraphs.isNotEmpty()) {
            // Update loading state
            (state as? BaseTTSService)?.let { baseState ->
                // Access protected state through reflection or make it internal
                // For now, we'll use the public state flows
            }
            
            androidEngine.precacheParagraphs(nextParagraphs)
            
            // Update cache status after a delay
            scope.launch {
                delay(500)
                val cached = mutableSetOf<Int>()
                val loading = mutableSetOf<Int>()
                
                for (i in 1..3) {
                    val nextIndex = current + i
                    if (nextIndex < content.size) {
                        when (androidEngine.getCacheStatus(nextIndex.toString())) {
                            CoquiTTSPlayer.CacheStatus.CACHED -> cached.add(nextIndex)
                            CoquiTTSPlayer.CacheStatus.LOADING -> loading.add(nextIndex)
                            else -> {}
                        }
                    }
                }
                
                // Update state through protected methods
                // This would need to be exposed in BaseTTSService
            }
        }
    }
}
