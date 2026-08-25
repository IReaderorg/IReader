package ireader.presentation.core.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import ireader.domain.models.prefs.PreferenceValues
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Self-checks for the hand-rolled HSL palette math: hue rotation wrap-around,
 * light/dark mode separation, and style distinctness.
 */
class Material3PaletteGeneratorTest {

    private fun hueOf(color: Color): Float {
        val max = maxOf(color.red, color.green, color.blue)
        val min = minOf(color.red, color.green, color.blue)
        val d = max - min
        if (d == 0f) return 0f
        val h = when (max) {
            color.red -> (((color.green - color.blue) / d) % 6f + 6f) % 6f * 60f
            color.green -> ((color.blue - color.red) / d + 2f) * 60f
            else -> ((color.red - color.green) / d + 4f) * 60f
        }
        return if (h < 0f) h + 360f else h
    }

    @Test
    fun `hue survives wrap-around for every style`() {
        // Red seed (hue ~0) with rotations near 360 must not produce negative/garbage hues
        val red = Color(0xFFE53935)
        for (style in PreferenceValues.CoverBasedThemeStyle.entries) {
            for (isDark in listOf(false, true)) {
                val scheme = Material3PaletteGenerator.generate(red, style, isDark)
                val primaryHue = hueOf(scheme.primary.toComposeColorForTest())
                assertTrue(primaryHue in 0f..360f, "$style dark=$isDark primary hue $primaryHue out of range")
            }
        }
    }

    @Test
    fun `dark scheme background is darker than light scheme background`() {
        val teal = Color(0xFF00897B)
        for (style in PreferenceValues.CoverBasedThemeStyle.entries) {
            val light = Material3PaletteGenerator.generate(teal, style, isDark = false)
            val dark = Material3PaletteGenerator.generate(teal, style, isDark = true)
            assertTrue(
                dark.background.toComposeColorForTest().luminanceForTest() <
                    light.background.toComposeColorForTest().luminanceForTest(),
                "$style: dark bg should be darker than light bg"
            )
        }
    }

    @Test
    fun `monochrome is less saturated than vibrant`() {
        val seed = Color(0xFF1565C0)
        val mono = Material3PaletteGenerator.generate(seed, PreferenceValues.CoverBasedThemeStyle.Monochrome, false)
        val vibrant = Material3PaletteGenerator.generate(seed, PreferenceValues.CoverBasedThemeStyle.Vibrant, false)
        assertTrue(
            mono.primary.toComposeColorForTest().saturationForTest() <
                vibrant.primary.toComposeColorForTest().saturationForTest()
        )
    }

    @Test
    fun `onPrimary contrasts with primary`() {
        val seed = Color.hsv(0.75f, 0.8f, 0.6f)
        for (style in PreferenceValues.CoverBasedThemeStyle.entries) {
            val scheme = Material3PaletteGenerator.generate(seed, style, isDark = true)
            val onPrimaryLum = scheme.onPrimary.luminance()
            val primaryLum = scheme.primary.luminance()
            assertEquals(
                onPrimaryLum > primaryLum,
                primaryLum < 0.5f,
                "$style: onPrimary luminance relationship wrong"
            )
        }
    }
}

// Local helpers so the test stays inside presentation's existing Compose deps.
private fun ireader.domain.models.common.DomainColor.toComposeColorForTest() =
    Color(red, green, blue, alpha)

private fun Color.saturationForTest(): Float {
    val max = maxOf(red, green, blue)
    val min = minOf(red, green, blue)
    return if (max == 0f) 0f else (max - min) / max
}

private fun Color.luminanceForTest(): Float = luminance()
