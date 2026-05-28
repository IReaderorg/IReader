package ireader.presentation.ui.reader.viewmodel

import ireader.core.log.Log
import ireader.domain.data.repository.ChapterRepository
import ireader.presentation.ui.reader.ReaderConstants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Manages scroll position tracking and persistence for the Reader.
 *
 * Responsibilities:
 * - Tracking current scroll position in memory
 * - Persisting scroll position to database
 * - Restoring scroll position when re-entering a chapter
 * - Handling scroll-to-end on chapter navigation
 */
class ReaderScrollManager(
    private val scope: CoroutineScope,
    private val chapterRepository: ChapterRepository
) {
    private var _currentScrollPosition: Long = ReaderConstants.DEFAULT_SCROLL_POSITION
    val currentScrollPosition: Long get() = _currentScrollPosition

    private var _shouldScrollToEnd: Boolean = false
    val shouldScrollToEnd: Boolean get() = _shouldScrollToEnd

    /**
     * Update the current scroll position in memory.
     * Does NOT persist to database - use [saveScrollPosition] for that.
     */
    fun updatePosition(position: Long) {
        _currentScrollPosition = position
    }

    /**
     * Save the current scroll position to the database.
     * Call this periodically as the user scrolls.
     */
    fun saveScrollPosition(chapterId: Long, position: Long) {
        _currentScrollPosition = position
        scope.launch {
            try {
                chapterRepository.updateLastPageRead(chapterId, position)
