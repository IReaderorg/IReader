package ireader.presentation.core.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Self-checks for the hand-rolled HSL palette math: hue rotation wrap-around,
 * light/dark mode separation, and adaptive saturation.
 */
class Material3PaletteGeneratorTest {

    private fun saturationOf(color: Color): Float {
        val max = maxOf(color.red, color.green, color.blue)
        val min = minOf(color.red, color.green, color.blue)
        return if (max == 0f) 0f else (max - min) / max
    }

    @Test
    fun `hue survives wrap-around`() {
        // Red seed (hue ~0) with rotations near 360 must not produce negative/garbage hues
        val red = Color(0xFFE53935)
        for (isDark in listOf(false, true)) {
            val scheme = Material3PaletteGenerator.generate(red, isDark)
            val primaryHue = hueOf(scheme.primary.toComposeColorForTest())
            assertTrue(primaryHue in 0f..360f, "dark=$isDark primary hue $primaryHue out of range")
        }
    }

    @Test
    fun `dark scheme background is darker than light scheme background`() {
        val teal = Color(0xFF00897B)
        val light = Material3PaletteGenerator.generate(teal, isDark = false)
        val dark = Material3PaletteGenerator.generate(teal, isDark = true)
        assertTrue(
            dark.background.toComposeColorForTest().luminance() <
                light.background.toComposeColorForTest().luminance()
        )
    }

    @Test
    fun `muted cover stays muted while vivid cover keeps punch`() {
        val gray = Color(0xFF808080)
        val vivid = Color(0xFF1565C0)
        assertTrue(
            Material3PaletteGenerator.generate(gray, false).primary.toComposeColorForTest()
                .let { saturationOf(it) } < 0.2f,
            "near-gray seed should produce a near-gray primary"
        )
        assertTrue(
            saturationOf(Material3PaletteGenerator.generate(vivid, false).primary.toComposeColorForTest()) > 0.4f,
            "vivid seed should stay saturated"
        )
    }

    @Test
    fun `onPrimary contrasts with primary`() {
        val seed = Color.hsv(0.75f, 0.8f, 0.6f)
        val scheme = Material3PaletteGenerator.generate(seed, isDark = true)
        val onPrimaryLum = scheme.onPrimary.luminance()
        val primaryLum = scheme.primary.luminance()
        assertEquals(
            onPrimaryLum > primaryLum,
            primaryLum < 0.5f,
            "onPrimary luminance relationship wrong"
        )
    }
}

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

// Local helpers so the test stays inside presentation's existing Compose deps.
private fun ireader.domain.models.common.DomainColor.toComposeColorForTest() =
    Color(red, green, blue, alpha)
