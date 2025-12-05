package ireader.domain.services.common

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Desktop implementation of TranslationService.
 * Uses coroutines for background translation tasks.
 */
class DesktopTranslationService : TranslationService, KoinComponent {
    
    private val _state = MutableStateFlow(ServiceState.IDLE)
    override val state: StateFlow<ServiceState> = _state
    
    private val _translationProgress = MutableStateFlow<Map<Long, TranslationProgress>>(emptyMap())
    override val translationProgress: StateFlow<Map<Long, TranslationProgress>> = _translationProgress
    
    private val _currentBookId = MutableStateFlow<Long?>(null)
    override val currentBookId: StateFlow<Long?> = _currentBookId
    
    // Lazy inject dependencies to avoid circular dependency issues
    private val translationServiceImpl: ireader.domain.services.translationService.TranslationServiceImpl by inject()
    
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
        bypassWarning: Boolean
    ): ServiceResult<TranslationQueueResult> {
        return translationServiceImpl.queueChapters(
            bookId = bookId,
            chapterIds = chapterIds,
            sourceLanguage = sourceLanguage,
            targetLanguage = targetLanguage,
            engineId = engineId,
            bypassWarning = bypassWarning
        )
    }
    
    override suspend fun pause() {
        translationServiceImpl.pause()
        _state.value = ServiceState.PAUSED
    }
    
    override suspend fun resume() {
        translationServiceImpl.resume()
        _state.value = ServiceState.RUNNING
    }
    
    override suspend fun cancelTranslation(chapterId: Long): ServiceResult<Unit> {
        return translationServiceImpl.cancelTranslation(chapterId)
    }
    
    override suspend fun cancelAll(): ServiceResult<Unit> {
        val result = translationServiceImpl.cancelAll()
        _state.value = ServiceState.IDLE
        return result
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
        _state.value = ServiceState.RUNNING
    }
    
    override suspend fun stop() {
        _state.value = ServiceState.STOPPED
    }
    
    override fun isRunning(): Boolean {
        return _state.value == ServiceState.RUNNING
    }
}
