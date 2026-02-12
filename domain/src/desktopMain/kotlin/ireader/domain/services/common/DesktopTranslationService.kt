package ireader.domain.services.common

import kotlinx.coroutines.flow.StateFlow
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Desktop implementation of TranslationService.
 * Uses coroutines for background translation tasks.
 * 
 * IMPORTANT: This class delegates all state flows to TranslationServiceImpl
 * to ensure progress updates are properly propagated to observers.
 */
class DesktopTranslationService : TranslationService, KoinComponent {
    
    // Lazy inject dependencies to avoid circular dependency issues
    private val translationServiceImpl: ireader.domain.services.translationService.TranslationServiceImpl by inject()
    
    // Delegate state to the impl (same pattern as AndroidTranslationService)
    override val state: StateFlow<ServiceState>
        get() = translationServiceImpl.state
    
    override val translationProgress: StateFlow<Map<Long, TranslationProgress>>
        get() = translationServiceImpl.translationProgress
    
    override val currentBookId: StateFlow<Long?>
        get() = translationServiceImpl.currentBookId
    
    override suspend fun initialize() {
        translationServiceImpl.initialize()
    }
    
    override suspend fun cleanup() {
        translationServiceImpl.cleanup()
    }
    
    override suspend fun queueChapters(
        bookId: Long,
        chapterIds: List<Long>,
        sourceLanguage: String,
        targetLanguage: String,
        engineId: Long,
        bypassWarning: Boolean,
        priority: Boolean
    ): ServiceResult<TranslationQueueResult> {
        return translationServiceImpl.queueChapters(
            bookId = bookId,
            chapterIds = chapterIds,
            sourceLanguage = sourceLanguage,
            targetLanguage = targetLanguage,
            engineId = engineId,
            bypassWarning = bypassWarning,
            priority = priority
        )
    }
    
    override suspend fun pause() {
        translationServiceImpl.pause()
    }
    
    override suspend fun resume() {
        translationServiceImpl.resume()
    }
    
    override suspend fun cancelTranslation(chapterId: Long): ServiceResult<Unit> {
        return translationServiceImpl.cancelTranslation(chapterId)
    }
    
    override suspend fun cancelAll(): ServiceResult<Unit> {
        return translationServiceImpl.cancelAll()
    }
    
    override suspend fun retryTranslation(chapterId: Long): ServiceResult<Unit> {
        return translationServiceImpl.retryTranslation(chapterId)
    }
    
    override fun getTranslationStatus(chapterId: Long): TranslationStatus? {
        return translationServiceImpl.getTranslationStatus(chapterId)
    }
    
    override fun requiresRateLimiting(engineId: Long): Boolean {
        return translationServiceImpl.requiresRateLimiting(engineId)
    }
    
    override fun isOfflineEngine(engineId: Long): Boolean {
        return translationServiceImpl.isOfflineEngine(engineId)
    }
    
    override suspend fun start() {
        translationServiceImpl.start()
    }
    
    override suspend fun stop() {
        translationServiceImpl.stop()
    }
    
    override fun isRunning(): Boolean {
        return translationServiceImpl.isRunning()
    }
}
