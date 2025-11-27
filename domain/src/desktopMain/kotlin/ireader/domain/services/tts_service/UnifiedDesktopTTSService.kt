package ireader.domain.services.tts_service

import ireader.core.log.Log
import ireader.domain.catalogs.CatalogStore
import ireader.domain.data.repository.BookRepository
import ireader.domain.data.repository.ChapterRepository
import ireader.domain.models.entities.Book
import ireader.domain.models.entities.Chapter
import ireader.domain.preferences.prefs.AppPreferences
import ireader.domain.preferences.prefs.ReaderPreferences
import ireader.domain.usecases.remote.RemoteUseCases
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Unified Desktop TTS Service
 * 
 * This is the new unified implementation that implements CommonTTSService
 * and delegates to the existing DesktopTTSService for platform-specific functionality.
 * 
 * This allows gradual migration while maintaining backward compatibility.
 */
class UnifiedDesktopTTSService(
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
) {
    
    // Delegate to existing DesktopTTSService for platform-specific features
    private val legacyService = DesktopTTSService().also {
        // Initialize immediately so state is available
        it.initialize()
    }
    
    // State adapter to bridge legacy state to new state
    private val stateAdapter by lazy { DesktopTTSStateAdapter(legacyService.state) }
    
    override val state: TTSServiceState
        get() = stateAdapter
    
    override suspend fun initializePlatformComponents() {
        // Legacy service already initialized in constructor
        // Create TTS engine wrapper
        ttsEngine = DesktopTTSEngineAdapter(legacyService)
        
        // No notification on desktop (for now)
        ttsNotification = null
    }
    
    override fun createTTSEngine(): TTSEngine {
        return DesktopTTSEngineAdapter(legacyService)
    }
    
    override fun createNotification(): TTSNotification {
        // Desktop doesn't use notifications (yet)
        return object : TTSNotification {
            override fun show(data: TTSNotificationData) {}
            override fun hide() {}
            override fun updatePlaybackState(isPlaying: Boolean) {}
            override fun updateProgress(current: Int, total: Int) {}
        }
    }
    
    override fun getPlatformAvailableEngines(): List<String> {
        return legacyService.getAvailableEngines().map { it.name }
    }
    
    override suspend fun startReading(bookId: Long, chapterId: Long, autoPlay: Boolean) {
        // Use legacy service for reading
        legacyService.startReading(bookId, chapterId, autoPlay)
        
        // Sync state
        syncStateFromLegacy()
    }
    
    override suspend fun play() {
        legacyService.startService(DesktopTTSService.ACTION_PLAY)
        syncStateFromLegacy()
    }
    
    override suspend fun pause() {
        legacyService.startService(DesktopTTSService.ACTION_PAUSE)
        syncStateFromLegacy()
    }
    
    override suspend fun stop() {
        legacyService.startService(DesktopTTSService.ACTION_STOP)
        syncStateFromLegacy()
    }
    
    override suspend fun nextChapter() {
        legacyService.startService(DesktopTTSService.ACTION_SKIP_NEXT)
        syncStateFromLegacy()
    }
    
    override suspend fun previousChapter() {
        legacyService.startService(DesktopTTSService.ACTION_SKIP_PREV)
        syncStateFromLegacy()
    }
    
    override suspend fun nextParagraph() {
        legacyService.startService(DesktopTTSService.ACTION_NEXT_PAR)
        syncStateFromLegacy()
    }
    
    override suspend fun previousParagraph() {
        legacyService.startService(DesktopTTSService.ACTION_PREV_PAR)
        syncStateFromLegacy()
    }
    
    override suspend fun jumpToParagraph(index: Int) {
        // Set the paragraph index directly in the legacy state
        legacyService.state.setCurrentReadingParagraph(index)
        syncStateFromLegacy()
    }
    
    override fun setSpeed(speed: Float) {
        legacyService.setSpeechRate(speed)
    }
    
    override fun setPitch(pitch: Float) {
        legacyService.setPitch(pitch)
    }
    
    override fun getCurrentEngineName(): String {
        return legacyService.getCurrentEngine().name
    }
    
    override fun isReady(): Boolean {
        return legacyService.synthesizer.isInitialized() || 
               legacyService.kokoroAvailable || 
               legacyService.mayaAvailable
    }
    
    override fun cleanup() {
        legacyService.shutdown()
        super.cleanup()
    }
    
    /**
     * Sync state from legacy service to new state flows
     */
    private fun syncStateFromLegacy() {
        scope.launch {
            stateAdapter.updateStateFlows()
        }
    }
    
    /**
     * Get the legacy service for direct access to desktop-specific features
     */
    fun getLegacyService(): DesktopTTSService = legacyService
}
