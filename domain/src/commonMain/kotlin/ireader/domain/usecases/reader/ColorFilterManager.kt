package ireader.domain.usecases.reader

import ireader.domain.models.reader.ColorFilter
import ireader.domain.models.reader.ColorFilterBlendMode
import ireader.domain.preferences.prefs.ReaderPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/**
 * Manager for color filter settings in reader
 * Requirements: 5.1, 5.2
 */
class ColorFilterManager(
    private val readerPreferences: ReaderPreferences,
) {
    /**
     * Get current color filter configuration
     */
    suspend fun getColorFilter(): ColorFilter {
        return ColorFilter(
            enabled = readerPreferences.colorFilter().get(),
            colorValue = readerPreferences.colorFilterValue().get(),
            blendMode = ColorFilterBlendMode.fromValue(readerPreferences.colorFilterMode().get()),
            customBrightness = readerPreferences.customBrightness().get(),
            brightnessValue = readerPreferences.customBrightnessValue().get(),
            grayscale = readerPreferences.grayscale().get(),
            invertedColors = readerPreferences.invertedColors().get()
        )
    }

    /**
     * Observe color filter changes
     */
    fun observeColorFilter(): Flow<ColorFilter> {
        return combine(
            readerPreferences.colorFilter().changes(),
            readerPreferences.colorFilterValue().changes(),
            readerPreferences.colorFilterMode().changes(),
            readerPreferences.customBrightness().changes(),
            readerPreferences.customBrightnessValue().changes(),
            readerPreferences.grayscale().changes(),
            readerPreferences.invertedColors().changes()
        ) { values ->
            val enabled = values[0] as Boolean
            val colorValue = values[1] as Int
            val mode = values[2] as Int
            val customBrightness = values[3] as Boolean
            val brightnessValue = values[4] as Int
            val grayscale = values[5] as Boolean
            val inverted = values[6] as Boolean
            
            ColorFilter(
                enabled = enabled,
                colorValue = colorValue,
                blendMode = ColorFilterBlendMode.fromValue(mode),
                customBrightness = customBrightness,
                brightnessValue = brightnessValue,
                grayscale = grayscale,
                invertedColors = inverted
            )
        }
    }

    /**
     * Update color filter settings
     */
    suspend fun updateColorFilter(filter: ColorFilter) {
        readerPreferences.colorFilter().set(filter.enabled)
        readerPreferences.colorFilterValue().set(filter.colorValue)
        readerPreferences.colorFilterMode().set(filter.blendMode.value)
        readerPreferences.customBrightness().set(filter.customBrightness)
        readerPreferences.customBrightnessValue().set(filter.brightnessValue)
        readerPreferences.grayscale().set(filter.grayscale)
        readerPreferences.invertedColors().set(filter.invertedColors)
    }

    /**
     * Enable color filter
     */
    suspend fun enableColorFilter() {
        readerPreferences.colorFilter().set(true)
    }

    /**
     * Disable color filter
     */
    suspend fun disableColorFilter() {
        readerPreferences.colorFilter().set(false)
    }

    /**
     * Set color filter value (ARGB)
     */
    suspend fun setColorFilterValue(color: Int) {
        readerPreferences.colorFilterValue().set(color)
    }

    /**
     * Set color filter blend mode
     */
    suspend fun setBlendMode(mode: ColorFilterBlendMode) {
        readerPreferences.colorFilterMode().set(mode.value)
    }

    /**
     * Enable custom brightness
     */
    suspend fun enableCustomBrightness(value: Int) {
        readerPreferences.customBrightness().set(true)
        readerPreferences.customBrightnessValue().set(value)
    }

    /**
     * Disable custom brightness
     */
    suspend fun disableCustomBrightness() {
        readerPreferences.customBrightness().set(false)
    }

    /**
     * Toggle grayscale mode
     */
    suspend fun toggleGrayscale() {
        val current = readerPreferences.grayscale().get()
        readerPreferences.grayscale().set(!current)
    }

    /**
     * Toggle inverted colors
     */
    suspend fun toggleInvertedColors() {
        val current = readerPreferences.invertedColors().get()
        readerPreferences.invertedColors().set(!current)
    }

    /**
     * Reset color filter to defaults
     */
    suspend fun resetColorFilter() {
        readerPreferences.colorFilter().set(false)
        readerPreferences.colorFilterValue().set(0)
        readerPreferences.colorFilterMode().set(0)
        readerPreferences.customBrightness().set(false)
        readerPreferences.customBrightnessValue().set(0)
        readerPreferences.grayscale().set(false)
        readerPreferences.invertedColors().set(false)
    }
}
