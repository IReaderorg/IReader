package ireader.domain.services.translationService

import ireader.domain.services.common.TranslationProgress
import ireader.domain.services.common.TranslationStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Manages the state of the translation service using StateFlow.
 * This class is used by both Android and Desktop implementations
 * to share translation state across the application.
 */
interface TranslationServiceState {
    val isRunning: StateFlow<Boolean>
    val isPaused: StateFlow<Boolean>
    val currentBookId: StateFlow<Long?>
    val translationProgress: StateFlow<Map<Long, TranslationProgress>>
    val totalChapters: StateFlow<Int>
    val completedChapters: StateFlow<Int>
}

/**
 * Shared state holder for translation service state management.
 */
class TranslationStateHolder : TranslationServiceState {
    private val _isRunning = MutableStateFlow(false)
    override val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()
    
    private val _isPaused = MutableStateFlow(false)
    override val isPaused: StateFlow<Boolean> = _isPaused.asStateFlow()
    
    private val _currentBookId = MutableStateFlow<Long?>(null)
    override val currentBookId: StateFlow<Long?> = _currentBookId.asStateFlow()
    
    private val _translationProgress = MutableStateFlow<Map<Long, TranslationProgress>>(emptyMap())
    override val translationProgress: StateFlow<Map<Long, TranslationProgress>> = _translationProgress.asStateFlow()
    
    private val _totalChapters = MutableStateFlow(0)
    override val totalChapters: StateFlow<Int> = _totalChapters.asStateFlow()
    
    private val _completedChapters = MutableStateFlow(0)
    override val completedChapters: StateFlow<Int> = _completedChapters.asStateFlow()
    
    fun setRunning(value: Boolean) {
        _isRunning.value = value
    }
    
    fun setPaused(value: Boolean) {
        _isPaused.value = value
    }
    
    fun setCurrentBookId(value: Long?) {
        _currentBookId.value = value
    }
    
    fun setTranslationProgress(value: Map<Long, TranslationProgress>) {
        _translationProgress.value = value
    }
    
    fun updateChapterProgress(chapterId: Long, progress: TranslationProgress) {
        _translationProgress.value = _translationProgress.value + (chapterId to progress)
    }
    
    fun setTotalChapters(value: Int) {
        _totalChapters.value = value
    }
    
    fun setCompletedChapters(value: Int) {
        _completedChapters.value = value
    }
    
    fun incrementCompleted() {
        _completedChapters.value++
    }
    
    /**
     * Reset all state to initial values
     */
    fun reset() {
        _isRunning.value = false
        _isPaused.value = false
        _currentBookId.value = null
        _translationProgress.value = emptyMap()
        _totalChapters.value = 0
        _completedChapters.value = 0
    }
}
