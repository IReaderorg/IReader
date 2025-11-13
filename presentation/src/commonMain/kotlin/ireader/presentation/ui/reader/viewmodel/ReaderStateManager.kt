package ireader.presentation.ui.reader.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import ireader.domain.models.entities.Chapter
import ireader.domain.usecases.reader.ManageChapterNavigationUseCase
import ireader.domain.usecases.reader.ManageReaderUiStateUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * State manager for reader screen
 * Separates state management logic from ViewModel
 * Follows Single Responsibility Principle
 */
class ReaderStateManager(
    private val scope: CoroutineScope,
    private val navigationUseCase: ManageChapterNavigationUseCase,
    private val uiStateUseCase: ManageReaderUiStateUseCase
) {
    // UI State
    var isReaderModeEnabled by mutableStateOf(true)
        private set
    var isMainBottomModeEnabled by mutableStateOf(false)
        private set
    var isSettingModeEnabled by mutableStateOf(false)
        private set
    var isToggleInProgress by mutableStateOf(false)
        private set
    
    // Reading time state
    var showReadingTime by mutableStateOf(true)
    var estimatedReadingMinutes by mutableStateOf(0)
        private set
    var wordsRemaining by mutableStateOf(0)
        private set
    var totalWords by mutableStateOf(0)
        private set
    
    /**
     * Toggle reader mode with debounce
     */
    fun toggleReaderMode(enable: Boolean? = null) {
        if (isToggleInProgress) return
        
        isToggleInProgress = true
        scope.launch {
            val newState = uiStateUseCase.toggleReaderMode(isReaderModeEnabled, enable)
            isReaderModeEnabled = newState.isReaderModeEnabled
            isMainBottomModeEnabled = newState.isMainBottomModeEnabled
            isSettingModeEnabled = newState.isSettingModeEnabled
            isToggleInProgress = false
        }
    }
    
    /**
     * Update reading time estimation
     */
    fun updateReadingTime(text: String, scrollProgress: Float, wordsPerMinute: Int) {
        val estimation = uiStateUseCase.calculateReadingTime(text, scrollProgress, wordsPerMinute)
        totalWords = estimation.totalWords
        wordsRemaining = estimation.wordsRemaining
        estimatedReadingMinutes = estimation.estimatedMinutes
    }
    
    /**
     * Get next chapter using navigation use case
     */
    fun getNextChapter(currentChapter: Chapter?, allChapters: List<Chapter>): Chapter? {
        return navigationUseCase.getNextChapter(currentChapter, allChapters)
    }
    
    /**
     * Get previous chapter using navigation use case
     */
    fun getPreviousChapter(currentChapter: Chapter?, allChapters: List<Chapter>): Chapter? {
        return navigationUseCase.getPreviousChapter(currentChapter, allChapters)
    }
    
    /**
     * Check if next chapter is available
     */
    fun hasNextChapter(currentChapter: Chapter?, allChapters: List<Chapter>): Boolean {
        return navigationUseCase.hasNextChapter(currentChapter, allChapters)
    }
    
    /**
     * Check if previous chapter is available
     */
    fun hasPreviousChapter(currentChapter: Chapter?, allChapters: List<Chapter>): Boolean {
        return navigationUseCase.hasPreviousChapter(currentChapter, allChapters)
    }
}
