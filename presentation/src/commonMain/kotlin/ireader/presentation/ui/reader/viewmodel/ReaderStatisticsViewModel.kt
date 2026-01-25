package ireader.presentation.ui.reader.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import ireader.core.log.Log
import ireader.domain.models.entities.Chapter
import ireader.domain.preferences.prefs.ReaderPreferences
import ireader.domain.services.ReadingTimerManager
import ireader.domain.usecases.statistics.TrackReadingProgressUseCase
import ireader.domain.usecases.quote.ReadingBuddyUseCases
import ireader.i18n.UiText
import ireader.presentation.ui.core.viewmodel.BaseViewModel
import kotlinx.coroutines.launch
import ireader.domain.utils.extensions.currentTimeToLong

/**
 * ViewModel responsible for reading statistics and tracking
 * 
 * Handles:
 * - Reading time tracking
 * - Progress tracking
 * - Reading break reminders
 * - Statistics collection
 * - Reading speed calculation
 * - Reading Buddy sync (chapters/books completed)
 */
class ReaderStatisticsViewModel(
    private val trackReadingProgressUseCase: TrackReadingProgressUseCase,
    private val readerPreferences: ReaderPreferences,
    private val readingBuddyUseCases: ReadingBuddyUseCases? = null,
) : BaseViewModel() {
    
    // Reading session tracking
    private var chapterOpenTimestamp: Long? = null
    private var sessionStartTime: Long? = null
    private var totalReadingTimeMs: Long = 0
    
    // Public accessor for session start time (for UI display)
    val currentSessionStartTime: Long?
        get() = sessionStartTime
    
    // Current chapter info
    var currentChapterId by mutableStateOf<Long?>(null)
        private set
    
    var currentBookId by mutableStateOf<Long?>(null)
        private set
    
    // Track if current chapter is the last one (for book completion tracking)
    private var isLastChapter: Boolean = false
    
    // Track if book completion has been recorded for this book (to avoid duplicates)
    private val completedBooks = mutableSetOf<Long>()
    
    // Reading progress
    var currentProgress by mutableStateOf(0f)
        private set
    
    var wordsRead by mutableStateOf(0)
        private set
    
    var charactersRead by mutableStateOf(0)
        private set
    
    // Reading speed
    var wordsPerMinute by mutableStateOf(0)
        private set
    
    var estimatedTimeRemaining by mutableStateOf<Long?>(null)
        private set
    
    // Reading break reminder
    val readingBreakReminderEnabled = readerPreferences.readingBreakReminderEnabled().asState()
    val readingBreakInterval = readerPreferences.readingBreakInterval().asState()
    
    var showReadingBreakDialog by mutableStateOf(false)
        private set
    
    private val readingTimerManager = ReadingTimerManager(
        scope = scope,
        onIntervalReached = { onReadingBreakIntervalReached() }
    )
    
    // Session statistics
    private var _sessionStats by mutableStateOf(ReadingSessionStats())
    
    init {
        // Start reading timer if enabled
        if (readingBreakReminderEnabled.value) {
            startReadingTimer()
        }
    }
    
    // ==================== Chapter Tracking ====================
    
    /**
     * Called when a chapter is opened
     * @param chapter The chapter being opened
     * @param isLast Whether this is the last chapter of the book
     */
    fun onChapterOpened(chapter: Chapter, isLast: Boolean = false) {
        chapterOpenTimestamp = currentTimeToLong()
        currentChapterId = chapter.id
        currentBookId = chapter.bookId
        currentProgress = 0f
        isLastChapter = isLast
        
        // Start session if not already started
        if (sessionStartTime == null) {
            sessionStartTime = currentTimeToLong()
        }
        
        // Sync with Reading Buddy - notify reading started
        scope.launch {
            readingBuddyUseCases?.onReadingStarted()
        }
        
        Log.debug("Chapter opened: ${chapter.name}, isLastChapter: $isLast")
    }
    
    /**
     * Called when a chapter is closed
     * Note: Reading time tracking is handled by ReaderScreenSpec's DisposableEffect
     * to avoid double counting. This method only tracks chapter completion and streak.
     */
    fun onChapterClosed() {
        val openTime = chapterOpenTimestamp ?: return
        val closeTime = currentTimeToLong()
        val readingTime = closeTime - openTime
        
        // Track reading time locally for session stats
        totalReadingTimeMs += readingTime
        
        // Update session stats
        _sessionStats = _sessionStats.copy(
            totalReadingTimeMs = totalReadingTimeMs,
            chaptersRead = _sessionStats.chaptersRead + 1
        )
        
        // Save to preferences (local tracking only, not database)
        scope.launch {
            try {
                val currentTotal = readerPreferences.totalReadingTimeMillis().get()
                readerPreferences.totalReadingTimeMillis().set(currentTotal + readingTime)
                
                val currentChapters = readerPreferences.chaptersCompleted().get()
                readerPreferences.chaptersCompleted().set(currentChapters + 1)
                
                // Note: Reading time to database is tracked by ReaderScreenSpec's DisposableEffect
                // to avoid double counting. We only track streak here.
                
                // Track chapter completion if user read at least 80% of the chapter
                if (currentProgress >= 0.8f && wordsRead > 0) {
                    trackReadingProgressUseCase.onChapterProgressUpdate(currentProgress, wordsRead)
                    Log.debug("Chapter progress tracked: ${(currentProgress * 100).toInt()}%, $wordsRead words")
                    
                    // Sync with Reading Buddy - track chapter completion
                    readingBuddyUseCases?.onChapterCompleted()
                    
                    // Track book completion if this is the last chapter and user read 80%+
                    val bookId = currentBookId
                    if (isLastChapter && bookId != null && !completedBooks.contains(bookId)) {
                        trackReadingProgressUseCase.trackBookCompletion()
                        completedBooks.add(bookId)
                        Log.debug("Book completion tracked for bookId: $bookId")
                        
                        // Sync with Reading Buddy - track book completion
                        readingBuddyUseCases?.onBookCompleted()
                    }
                }
            } catch (e: Exception) {
                Log.error("Failed to save reading stats", e)
            }
        }
        
        Log.debug("Chapter closed. Reading time: ${readingTime / 1000}s")
        
        chapterOpenTimestamp = null
    }
    
    /**
     * Update reading progress (0.0 to 1.0)
     */
    fun updateProgress(progress: Float, totalWords: Int = 0) {
        currentProgress = progress.coerceIn(0f, 1f)
        
        // Calculate words read
        if (totalWords > 0) {
            wordsRead = (totalWords * progress).toInt()
            charactersRead = wordsRead * 5 // Estimate 5 chars per word
            
            // Calculate reading speed
            calculateReadingSpeed()
            
            // Estimate time remaining
            estimateTimeRemaining(totalWords)
        }
    }
    
    // ==================== Reading Speed ====================
    
    /**
     * Calculate reading speed (words per minute)
     */
    private fun calculateReadingSpeed() {
        val openTime = chapterOpenTimestamp ?: return
        val currentTime = currentTimeToLong()
        val elapsedMinutes = (currentTime - openTime) / 60000.0
        
        if (elapsedMinutes > 0 && wordsRead > 0) {
            wordsPerMinute = (wordsRead / elapsedMinutes).toInt()
        }
    }
    
    /**
     * Estimate time remaining to finish chapter
     */
    private fun estimateTimeRemaining(totalWords: Int) {
        if (wordsPerMinute > 0 && currentProgress < 1f) {
            val wordsRemaining = totalWords * (1f - currentProgress)
            val minutesRemaining = wordsRemaining / wordsPerMinute
            estimatedTimeRemaining = (minutesRemaining * 60 * 1000).toLong()
        } else {
            estimatedTimeRemaining = null
        }
    }
    
    /**
     * Get formatted time remaining
     */
    fun getFormattedTimeRemaining(): String? {
        val timeMs = estimatedTimeRemaining ?: return null
        
        val minutes = timeMs / 60000
        val seconds = (timeMs % 60000) / 1000
        
        return when {
            minutes > 60 -> "${minutes / 60}h ${minutes % 60}m"
            minutes > 0 -> "${minutes}m ${seconds}s"
            else -> "${seconds}s"
        }
    }
    
    // ==================== Reading Break Reminder ====================
    
    /**
     * Start reading timer for break reminders
     */
    fun startReadingTimer() {
        val intervalMinutes = readingBreakInterval.value
        if (intervalMinutes > 0) {
            readingTimerManager.startTimer(intervalMinutes)
            Log.debug("Reading timer started: $intervalMinutes minutes")
        }
    }
    
    /**
     * Stop reading timer
     */
    fun stopReadingTimer() {
        readingTimerManager.stopTimer()
    }
    
    /**
     * Called when reading break interval is reached
     */
    private fun onReadingBreakIntervalReached() {
        if (readingBreakReminderEnabled.value) {
            showReadingBreakDialog = true
            Log.debug("Reading break reminder triggered")
        }
    }
    
    /**
     * Dismiss reading break dialog
     */
    fun dismissReadingBreakDialog() {
        showReadingBreakDialog = false
        
        // Restart timer
        if (readingBreakReminderEnabled.value) {
            stopReadingTimer()
            startReadingTimer()
        }
    }
    
    /**
     * Toggle reading break reminder
     */
    fun toggleReadingBreakReminder(enabled: Boolean) {
        scope.launch {
            readerPreferences.readingBreakReminderEnabled().set(enabled)
            
            if (enabled) {
                startReadingTimer()
            } else {
                stopReadingTimer()
            }
        }
    }
    
    /**
     * Update reading break interval
     */
    fun updateReadingBreakInterval(minutes: Int) {
        scope.launch {
            readerPreferences.readingBreakInterval().set(minutes)
            
            // Restart timer with new interval
            if (readingBreakReminderEnabled.value) {
                stopReadingTimer()
                startReadingTimer()
            }
        }
    }
    
    // ==================== Session Statistics ====================
    
    /**
     * Get current session statistics
     */
    fun getSessionStats(): ReadingSessionStats {
        val sessionTime = sessionStartTime?.let {
            currentTimeToLong() - it
        } ?: 0
        
        return _sessionStats.copy(
            sessionDurationMs = sessionTime,
            averageWordsPerMinute = wordsPerMinute
        )
    }
    
    /**
     * Reset session statistics
     */
    fun resetSessionStats() {
        sessionStartTime = currentTimeToLong()
        totalReadingTimeMs = 0
        _sessionStats = ReadingSessionStats()
        wordsRead = 0
        charactersRead = 0
        wordsPerMinute = 0
    }
    
    /**
     * Get formatted session duration
     */
    fun getFormattedSessionDuration(): String {
        val sessionTime = sessionStartTime?.let {
            currentTimeToLong() - it
        } ?: 0
        
        val hours = sessionTime / 3600000
        val minutes = (sessionTime % 3600000) / 60000
        val seconds = (sessionTime % 60000) / 1000
        
        return when {
            hours > 0 -> "${hours}h ${minutes}m"
            minutes > 0 -> "${minutes}m ${seconds}s"
            else -> "${seconds}s"
        }
    }
    
    // ==================== Daily Goals ====================
    
    /**
     * Check if daily reading goal is met
     */
    fun isDailyGoalMet(): Boolean {
        val dailyGoalMinutes = readerPreferences.dailyReadingGoal().get()
        val todayReadingMinutes = (totalReadingTimeMs / 60000).toInt()
        
        return todayReadingMinutes >= dailyGoalMinutes
    }
    
    /**
     * Get daily goal progress (0.0 to 1.0)
     */
    fun getDailyGoalProgress(): Float {
        val dailyGoalMinutes = readerPreferences.dailyReadingGoal().get()
        if (dailyGoalMinutes <= 0) return 0f
        
        val todayReadingMinutes = (totalReadingTimeMs / 60000).toFloat()
        return (todayReadingMinutes / dailyGoalMinutes.toFloat()).coerceAtMost(1f)
    }
    
    override fun onCleared() {
        super.onCleared()
        
        // Save final statistics
        onChapterClosed()
        
        // Stop timer
        stopReadingTimer()
    }
}

/**
 * Reading session statistics
 */
data class ReadingSessionStats(
    val sessionDurationMs: Long = 0,
    val totalReadingTimeMs: Long = 0,
    val chaptersRead: Int = 0,
    val wordsRead: Int = 0,
    val averageWordsPerMinute: Int = 0,
    val pagesRead: Int = 0
) {
    val sessionDurationMinutes: Int
        get() = (sessionDurationMs / 60000).toInt()
    
    val totalReadingTimeMinutes: Int
        get() = (totalReadingTimeMs / 60000).toInt()
    
    val formattedSessionDuration: String
        get() {
            val hours = sessionDurationMs / 3600000
            val minutes = (sessionDurationMs % 3600000) / 60000
            return when {
                hours > 0 -> "${hours}h ${minutes}m"
                minutes > 0 -> "${minutes}m"
                else -> "<1m"
            }
        }
}
