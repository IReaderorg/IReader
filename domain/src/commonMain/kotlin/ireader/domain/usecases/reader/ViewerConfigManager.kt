package ireader.domain.usecases.reader

import ireader.domain.models.reader.ImageScaleType
import ireader.domain.models.reader.NavigationConfig
import ireader.domain.models.reader.ReaderMode
import ireader.domain.models.reader.ReaderOrientation
import ireader.domain.models.reader.TapZoneMode
import ireader.domain.models.reader.TappingInvertMode
import ireader.domain.models.reader.ViewerConfig
import ireader.domain.models.reader.ZoomStart
import ireader.domain.preferences.prefs.ReaderPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/**
 * Manager for viewer configuration in reader
 * Requirements: 5.1, 5.2, 8.1
 */
class ViewerConfigManager(
    private val readerPreferences: ReaderPreferences,
) {
    /**
     * Get current viewer configuration
     */
    suspend fun getViewerConfig(): ViewerConfig {
        val readerModeValue = readerPreferences.defaultReaderMode().get()
        val orientationValue = readerPreferences.defaultOrientationType().get()
        
        return ViewerConfig(
            readerMode = convertToReaderMode(readerModeValue),
            orientation = convertToReaderOrientation(orientationValue),
            imageScaleType = ImageScaleType.fromValue(readerPreferences.imageScaleType().get()),
            zoomStart = ZoomStart.fromValue(readerPreferences.zoomStart().get()),
            cropBorders = readerPreferences.cropBorders().get(),
            dualPageSplit = readerPreferences.dualPageSplitPaged().get(),
            dualPageInvert = readerPreferences.dualPageInvertPaged().get(),
            dualPageRotateToFit = readerPreferences.dualPageRotateToFit().get(),
            webtoonSidePadding = readerPreferences.webtoonSidePadding().get(),
            webtoonDisableZoomOut = readerPreferences.webtoonDisableZoomOut().get(),
            landscapeZoom = readerPreferences.landscapeZoom().get(),
            navigateToPan = readerPreferences.navigateToPan().get()
        )
    }

    /**
     * Get navigation configuration
     */
    suspend fun getNavigationConfig(): NavigationConfig {
        return NavigationConfig(
            tapZoneMode = convertToTapZoneMode(readerPreferences.navigationModePager().get()),
            volumeKeyNavigation = readerPreferences.readWithVolumeKeys().get(),
            volumeKeyInverted = readerPreferences.readWithVolumeKeysInverted().get(),
            longTapEnabled = readerPreferences.readWithLongTap().get(),
            tappingInvertMode = readerPreferences.pagerNavInverted().get(),
            showNavigationOverlay = readerPreferences.showNavigationOverlayOnStart().get()
        )
    }

    /**
     * Observe viewer configuration changes
     */
    fun observeViewerConfig(): Flow<ViewerConfig> {
        return combine(
            readerPreferences.defaultReaderMode().changes(),
            readerPreferences.defaultOrientationType().changes(),
            readerPreferences.imageScaleType().changes(),
            readerPreferences.zoomStart().changes(),
            readerPreferences.cropBorders().changes(),
            readerPreferences.dualPageSplitPaged().changes(),
            readerPreferences.landscapeZoom().changes()
        ) { readerMode, orientation, scaleType, zoomStart, cropBorders, dualPage, landscapeZoom ->
            ViewerConfig(
                readerMode = convertToReaderMode(readerMode),
                orientation = convertToReaderOrientation(orientation),
                imageScaleType = ImageScaleType.fromValue(scaleType),
                zoomStart = ZoomStart.fromValue(zoomStart),
                cropBorders = cropBorders,
                dualPageSplit = dualPage,
                landscapeZoom = landscapeZoom
            )
        }
    }

    /**
     * Update viewer configuration
     */
    suspend fun updateViewerConfig(config: ViewerConfig) {
        readerPreferences.defaultReaderMode().set(convertFromReaderMode(config.readerMode))
        readerPreferences.defaultOrientationType().set(convertFromReaderOrientation(config.orientation))
        readerPreferences.imageScaleType().set(config.imageScaleType.ordinal)
        readerPreferences.zoomStart().set(config.zoomStart.ordinal)
        readerPreferences.cropBorders().set(config.cropBorders)
        readerPreferences.dualPageSplitPaged().set(config.dualPageSplit)
        readerPreferences.dualPageInvertPaged().set(config.dualPageInvert)
        readerPreferences.dualPageRotateToFit().set(config.dualPageRotateToFit)
        readerPreferences.webtoonSidePadding().set(config.webtoonSidePadding)
        readerPreferences.webtoonDisableZoomOut().set(config.webtoonDisableZoomOut)
        readerPreferences.landscapeZoom().set(config.landscapeZoom)
        readerPreferences.navigateToPan().set(config.navigateToPan)
    }

    /**
     * Update navigation configuration
     */
    suspend fun updateNavigationConfig(config: NavigationConfig) {
        readerPreferences.navigationModePager().set(convertFromTapZoneMode(config.tapZoneMode))
        readerPreferences.readWithVolumeKeys().set(config.volumeKeyNavigation)
        readerPreferences.readWithVolumeKeysInverted().set(config.volumeKeyInverted)
        readerPreferences.readWithLongTap().set(config.longTapEnabled)
        readerPreferences.pagerNavInverted().set(config.tappingInvertMode)
        readerPreferences.showNavigationOverlayOnStart().set(config.showNavigationOverlay)
    }

    /**
     * Toggle dual page mode
     */
    suspend fun toggleDualPageMode() {
        val current = readerPreferences.dualPageSplitPaged().get()
        readerPreferences.dualPageSplitPaged().set(!current)
    }

    /**
     * Toggle crop borders
     */
    suspend fun toggleCropBorders() {
        val current = readerPreferences.cropBorders().get()
        readerPreferences.cropBorders().set(!current)
    }

    /**
     * Set image scale type
     */
    suspend fun setImageScaleType(scaleType: ImageScaleType) {
        readerPreferences.imageScaleType().set(scaleType.ordinal)
    }

    /**
     * Enable volume key navigation
     */
    suspend fun enableVolumeKeyNavigation(inverted: Boolean = false) {
        readerPreferences.readWithVolumeKeys().set(true)
        readerPreferences.readWithVolumeKeysInverted().set(inverted)
    }

    /**
     * Disable volume key navigation
     */
    suspend fun disableVolumeKeyNavigation() {
        readerPreferences.readWithVolumeKeys().set(false)
    }

    // Helper conversion functions
    private fun convertToReaderMode(value: Int): ReaderMode {
        return when (value) {
            1 -> ReaderMode.LEFT_TO_RIGHT
            2 -> ReaderMode.RIGHT_TO_LEFT
            3 -> ReaderMode.VERTICAL
            4 -> ReaderMode.WEBTOON
            5 -> ReaderMode.CONTINUOUS_VERTICAL
            else -> ReaderMode.PAGED
        }
    }

    private fun convertFromReaderMode(mode: ReaderMode): Int {
        return when (mode) {
            ReaderMode.LEFT_TO_RIGHT -> 1
            ReaderMode.RIGHT_TO_LEFT -> 2
            ReaderMode.VERTICAL -> 3
            ReaderMode.WEBTOON -> 4
            ReaderMode.CONTINUOUS_VERTICAL -> 5
            else -> 0
        }
    }

    private fun convertToReaderOrientation(value: Int): ReaderOrientation {
        return when (value) {
            1 -> ReaderOrientation.PORTRAIT
            2 -> ReaderOrientation.LANDSCAPE
            3 -> ReaderOrientation.LOCKED_PORTRAIT
            4 -> ReaderOrientation.LOCKED_LANDSCAPE
            else -> ReaderOrientation.FREE
        }
    }

    private fun convertFromReaderOrientation(orientation: ReaderOrientation): Int {
        return when (orientation) {
            ReaderOrientation.PORTRAIT -> 1
            ReaderOrientation.LANDSCAPE -> 2
            ReaderOrientation.LOCKED_PORTRAIT -> 3
            ReaderOrientation.LOCKED_LANDSCAPE -> 4
            else -> 0
        }
    }

    private fun convertToTapZoneMode(value: Int): TapZoneMode {
        return when (value) {
            1 -> TapZoneMode.L_SHAPED
            2 -> TapZoneMode.KINDLE
            3 -> TapZoneMode.EDGE
            4 -> TapZoneMode.RIGHT_AND_LEFT
            5 -> TapZoneMode.DISABLED
            else -> TapZoneMode.DEFAULT
        }
    }

    private fun convertFromTapZoneMode(mode: TapZoneMode): Int {
        return when (mode) {
            TapZoneMode.L_SHAPED -> 1
            TapZoneMode.KINDLE -> 2
            TapZoneMode.EDGE -> 3
            TapZoneMode.RIGHT_AND_LEFT -> 4
            TapZoneMode.DISABLED -> 5
            else -> 0
        }
    }
}
