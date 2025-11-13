package ireader.domain.usecases.reader

import kotlinx.coroutines.delay

/**
 * Use case for managing reader UI state transitions
 * Extracts UI state management logic from ViewModel
 */
class ManageReaderUiStateUseCase {
    
    /**
     * Toggle reader mode with debounce to prevent rapid toggling
     * @param currentState Current reader mode state
     * @param enable Explicit state to set, or null to toggle
     * @return New reader mode state
     */
    suspend fun toggleReaderMode(
        currentState: Boolean,
        enable: Boolean?
    ): ReaderModeState {
        // Debounce delay to prevent rapid toggling
        delay(100)
        
        val newReaderMode = enable ?: !currentState
        return ReaderModeState(
            isReaderModeEnabled = newReaderMode,
            isMainBottomModeEnabled = !newReaderMode,
            isSettingModeEnabled = false
        )
    }
    
    /**
     * Calculate reading time estimation
     * @param text Full text content
     * @param scrollProgress Progress through the chapter (0.0 to 1.0)
     * @param wordsPerMinute Reading speed in words per minute
     * @return Reading time estimation
     */
    fun calculateReadingTime(
        text: String,
        scrollProgress: Float,
        wordsPerMinute: Int
    ): ReadingTimeEstimation {
        val totalWords = countWords(text)
        val progressClamped = scrollProgress.coerceIn(0f, 1f)
        val wordsRemaining = (totalWords * (1f - progressClamped)).toInt().coerceAtLeast(0)
        val estimatedMinutes = if (wordsRemaining > 0) {
            (wordsRemaining.toFloat() / wordsPerMinute).toInt().coerceAtLeast(1)
        } else {
            0
        }
        
        return ReadingTimeEstimation(
            totalWords = totalWords,
            wordsRemaining = wordsRemaining,
            estimatedMinutes = estimatedMinutes
        )
    }
    
    /**
     * Count words in text
     */
    private fun countWords(text: String): Int {
        if (text.isBlank()) return 0
        return text.trim().split(Regex("\\s+")).size
    }
}

/**
 * Data class representing reader mode state
 */
data class ReaderModeState(
    val isReaderModeEnabled: Boolean,
    val isMainBottomModeEnabled: Boolean,
    val isSettingModeEnabled: Boolean
)

/**
 * Data class representing reading time estimation
 */
data class ReadingTimeEstimation(
    val totalWords: Int,
    val wordsRemaining: Int,
    val estimatedMinutes: Int
)
