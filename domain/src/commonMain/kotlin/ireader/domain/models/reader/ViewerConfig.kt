package ireader.domain.models.reader

/**
 * Viewer configuration for different reading modes
 * Requirements: 5.1, 5.2, 8.1
 */
data class ViewerConfig(
    val readerMode: ReaderMode = ReaderMode.PAGED,
    val orientation: ReaderOrientation = ReaderOrientation.FREE,
    val imageScaleType: ImageScaleType = ImageScaleType.FIT_SCREEN,
    val zoomStart: ZoomStart = ZoomStart.AUTOMATIC,
    val cropBorders: Boolean = false,
    val dualPageSplit: Boolean = false,
    val dualPageInvert: Boolean = false,
    val dualPageRotateToFit: Boolean = false,
    val webtoonSidePadding: Int = 0,
    val webtoonDisableZoomOut: Boolean = false,
    val landscapeZoom: Boolean = true,
    val navigateToPan: Boolean = true,
) {
    /**
     * Check if dual page mode is enabled
     */
    fun isDualPageMode(): Boolean {
        return dualPageSplit
    }

    /**
     * Check if webtoon mode
     */
    fun isWebtoonMode(): Boolean {
        return readerMode == ReaderMode.WEBTOON || readerMode == ReaderMode.CONTINUOUS_VERTICAL
    }

    /**
     * Check if paged mode
     */
    fun isPagedMode(): Boolean {
        return !isWebtoonMode()
    }
}

/**
 * Reader mode types
 */
enum class ReaderMode {
    PAGED,
    LEFT_TO_RIGHT,
    RIGHT_TO_LEFT,
    VERTICAL,
    WEBTOON,
    CONTINUOUS_VERTICAL
}

/**
 * Reader orientation types
 */
enum class ReaderOrientation {
    FREE,
    PORTRAIT,
    LANDSCAPE,
    LOCKED_PORTRAIT,
    LOCKED_LANDSCAPE
}

/**
 * Image scale types for reader
 */
enum class ImageScaleType {
    FIT_SCREEN,
    STRETCH,
    FIT_WIDTH,
    FIT_HEIGHT,
    ORIGINAL_SIZE,
    SMART_FIT;

    companion object {
        fun fromValue(value: Int): ImageScaleType {
            return entries.getOrNull(value) ?: FIT_SCREEN
        }
    }
}

/**
 * Zoom start position
 */
enum class ZoomStart {
    AUTOMATIC,
    LEFT,
    RIGHT,
    CENTER;

    companion object {
        fun fromValue(value: Int): ZoomStart {
            return entries.getOrNull(value) ?: AUTOMATIC
        }
    }
}

/**
 * Navigation mode configuration
 */
data class NavigationConfig(
    val tapZoneMode: TapZoneMode = TapZoneMode.DEFAULT,
    val volumeKeyNavigation: Boolean = false,
    val volumeKeyInverted: Boolean = false,
    val longTapEnabled: Boolean = true,
    val tappingInvertMode: TappingInvertMode = TappingInvertMode.NONE,
    val showNavigationOverlay: Boolean = false,
) {
    /**
     * Check if volume key navigation is enabled
     */
    fun isVolumeKeyEnabled(): Boolean {
        return volumeKeyNavigation
    }

    /**
     * Check if tapping is inverted horizontally
     */
    fun isHorizontallyInverted(): Boolean {
        return tappingInvertMode.shouldInvertHorizontal
    }

    /**
     * Check if tapping is inverted vertically
     */
    fun isVerticallyInverted(): Boolean {
        return tappingInvertMode.shouldInvertVertical
    }
}

/**
 * Tap zone modes for navigation
 */
enum class TapZoneMode {
    DEFAULT,
    L_SHAPED,
    KINDLE,
    EDGE,
    RIGHT_AND_LEFT,
    DISABLED
}

/**
 * Tapping invert modes
 */
enum class TappingInvertMode {
    NONE,
    HORIZONTAL,
    VERTICAL,
    BOTH;

    val shouldInvertHorizontal: Boolean
        get() = this == HORIZONTAL || this == BOTH

    val shouldInvertVertical: Boolean
        get() = this == VERTICAL || this == BOTH
}
