package ireader.presentation.ui.reader

import ireader.domain.preferences.prefs.ReadingMode

/**
 * High-level volume key navigation action.
 */
enum class VolumeKeyAction {
    PAGE_NEXT,
    PAGE_PREV,
    SCROLL_FORWARD,
    SCROLL_BACKWARD,
    NEXT_CHAPTER,
    PREV_CHAPTER,
    NONE
}

/**
 * Event emitted to navigate pages in paged reader mode.
 */
sealed interface PageNavigationEvent {
    data object Next : PageNavigationEvent
    data object Prev : PageNavigationEvent
}

/**
 * Determines the navigation action based on volume key, reading mode, scroll position, and inverted preference.
 */
fun determineVolumeKeyAction(
    isVolumeUp: Boolean,
    volumeKeyInverted: Boolean,
    readingMode: ReadingMode,
    canScrollForward: Boolean = false,
    canScrollBackward: Boolean = false,
): VolumeKeyAction {
    val isNext = if (volumeKeyInverted) isVolumeUp else !isVolumeUp
    return when (readingMode) {
        ReadingMode.Page -> {
            if (isNext) VolumeKeyAction.PAGE_NEXT else VolumeKeyAction.PAGE_PREV
        }
        ReadingMode.Continues -> {
            if (isNext) {
                if (canScrollForward) VolumeKeyAction.SCROLL_FORWARD else VolumeKeyAction.NEXT_CHAPTER
            } else {
                if (canScrollBackward) VolumeKeyAction.SCROLL_BACKWARD else VolumeKeyAction.PREV_CHAPTER
            }
        }
        ReadingMode.InfiniteScroll -> {
            if (isNext) {
                if (canScrollForward) VolumeKeyAction.SCROLL_FORWARD else VolumeKeyAction.NONE
            } else {
                if (canScrollBackward) VolumeKeyAction.SCROLL_BACKWARD else VolumeKeyAction.NONE
            }
        }
    }
}
