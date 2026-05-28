package ireader.presentation.ui.reader

/**
 * Constants for the Reader feature.
 * Centralizes all magic numbers and configuration values to make the code
 * self-documenting and easy to tune.
 */
object ReaderConstants {
    // ========== Delays ==========

    /** Delay (ms) after a remote fetch completes before notifying ChapterController.
     *  This ensures the DB insert is fully committed so ChapterController's loadChapter
     *  finds content and doesn't try to re-fetch from remote. */
    const val FETCH_TO_CONTROLLER_DELAY_MS = 300L

    /** Delay (ms) before starting preload of the next chapter after the current
     *  chapter's remote fetch completes. This prevents two remote fetches from
     *  hitting the source server simultaneously. */
    const val PRELOAD_AFTER_FETCH_DELAY_MS = 500L

    /** Delay (ms) for initial chapter load to allow ChapterController to initialize. */
    const val CHAPTER_CONTROLLER_INIT_DELAY_MS = 100L

    /** Debounce delay (ms) for chapter list updates from ChapterNotifier.
     *  Prevents rapid recomposition during batch operations. */
    const val CHAPTER_LIST_DEBOUNCE_MS = 100L

    /** Delay (ms) before resetting the setting-changing flag in settings ViewModel. */
    const val SETTINGS_CHANGE_RESET_DELAY_MS = 100L

    // ========== Scroll ==========

    /** Default scroll position when no saved position exists. */
    const val DEFAULT_SCROLL_POSITION = 0L

    /** Minimum chapter index for validation. */
    const val MIN_CHAPTER_INDEX = 0

    // ========== Preload ==========

    /** Number of chapters to look ahead for preloading. */
    const val PRELOAD_LOOKAHEAD_COUNT = 3
}
