package ireader.presentation.ui.reader.viewmodel

import ireader.domain.preferences.prefs.ReaderPreferences
import ireader.domain.usecases.reader.ReaderUseCasesAggregate
import ireader.presentation.ui.core.ui.asStateIn
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * ViewModel responsible for scroll state management, auto-scroll logic,
 * reading time estimation, and reading break reminder.
 *
 * Extracted from ReaderScreenViewModel to separate scroll-related concerns.
 */
class ReaderScrollViewModel(
    private val scope: CoroutineScope,
    private val readerUseCasesAggregate: ReaderUseCasesAggregate,
    private val statisticsViewModel: ReaderStatisticsViewModel,
    private val readerPreferences: ReaderPreferences,
    private val settingsViewModel: ReaderSettingsViewModel,
    private val getState: () -> ReaderState,
    private val updateSuccessState: ((ReaderState.Success) -> ReaderState.Success) -> Unit,
) {
    // ==================== Scroll State ====================

    private val scrollManager = ReaderScrollManager(
        scope = scope,
        chapterRepository = readerUseCasesAggregate.chapterRepository
    )

    val scrollPosition: MutableStateFlow<Long> = MutableStateFlow(scrollManager.currentScrollPosition)
    val autoScrollEnabled: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val autoScrollSpeed: MutableStateFlow<Float> = MutableStateFlow(0f)
    val readingTimeMs: MutableStateFlow<Long> = MutableStateFlow(0L)

    /** Current scroll position (delegated to scrollManager) */
    val currentScrollPosition: Long get() = scrollManager.currentScrollPosition

    // ==================== Scroll Position Management ====================

    fun saveScrollPosition(position: Long) {
        val chapter = (getState() as? ReaderState.Success)?.currentChapter ?: return

        scrollManager.updatePosition(position)
        scrollPosition.value = position

        updateSuccessState { state ->
            state.copy(
                currentChapter = state.currentChapter.copy(lastPageRead = position)
            )
        }

        scrollManager.saveScrollPosition(chapter.id, position)
    }

    fun saveCurrentScrollPositionToDatabase() {
        val chapter = (getState() as? ReaderState.Success)?.currentChapter ?: return
        scrollManager.forceSaveScrollPosition(chapter.id)
    }

    // ==================== Scroll-to-Position ====================

    var scrollToEndOnChapterChange: Boolean
        get() = (getState() as? ReaderState.Success)?.scrollToEndOnChapterChange ?: false
        set(value) { updateSuccessState { it.copy(scrollToEndOnChapterChange = value) } }

    // ==================== Auto-Scroll ====================

    var autoScrollMode: Boolean
        get() = settingsViewModel.autoScrollMode
        set(value) { settingsViewModel.autoScrollMode = value }

    val autoScrollOffset get() = settingsViewModel.autoScrollOffset
    val autoScrollInterval get() = settingsViewModel.autoScrollInterval

    fun increaseAutoScrollSpeed() {
        settingsViewModel.increaseAutoScrollSpeed()
    }

    fun decreaseAutoScrollSpeed() {
        settingsViewModel.decreaseAutoScrollSpeed()
    }

    fun toggleAutoScroll() {
        settingsViewModel.toggleAutoScroll()
    }

    // ==================== Reading Time Estimation ====================

    var showReadingTime: Boolean
        get() = (getState() as? ReaderState.Success)?.showReadingTime ?: false
        set(value) { updateSuccessState { it.copy(showReadingTime = value) } }

    val estimatedReadingMinutes: Int
        get() = (getState() as? ReaderState.Success)?.estimatedReadingMinutes ?: 0

    val wordsRemaining: Int
        get() = (getState() as? ReaderState.Success)?.wordsRemaining ?: 0

    val showReadingTimeIndicator = readerPreferences.showReadingTimeIndicator().asStateIn(scope)

    fun updateReadingTimeEstimation(scrollProgress: Float) {
        val successState = getState() as? ReaderState.Success ?: return
        val totalWords = successState.totalWords
        statisticsViewModel.updateProgress(scrollProgress, totalWords)

        val estimatedMinutes = (statisticsViewModel.estimatedTimeRemaining ?: 0L) / 60000
        val wordsRemaining = if (totalWords > 0) ((1f - scrollProgress) * totalWords).toInt() else 0

        val estimatedMs = statisticsViewModel.estimatedTimeRemaining ?: 0L
        readingTimeMs.value = estimatedMs

        updateSuccessState {
            it.copy(
                estimatedReadingMinutes = estimatedMinutes.toInt(),
                wordsRemaining = wordsRemaining
            )
        }
    }

    // ==================== Reading Break Reminder ====================

    val readingBreakInterval = readerPreferences.readingBreakInterval().asStateIn(scope)

    val showReadingBreakDialog: Boolean
        get() = (getState() as? ReaderState.Success)?.showReadingBreakDialog ?: false

    fun onTakeBreak() {
        updateSuccessState { it.copy(showReadingBreakDialog = false) }
    }

    fun onContinueReading() {
        updateSuccessState { it.copy(showReadingBreakDialog = false) }
    }

    fun onSnoozeReadingBreak(minutes: Int) {
        updateSuccessState { it.copy(showReadingBreakDialog = false) }
    }

    fun dismissReadingBreakDialog() {
        updateSuccessState { it.copy(showReadingBreakDialog = false) }
    }
}
