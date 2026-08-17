package ireader.presentation.core.theme

import androidx.compose.ui.graphics.Color
import ireader.domain.models.common.DomainColor
import ireader.domain.models.theme.DomainColorScheme
import ireader.domain.models.prefs.PreferenceValues
import kotlin.math.roundToInt

object Material3PaletteGenerator {
    
    fun generate(
        seedColor: Color,
        style: PreferenceValues.CoverBasedThemeStyle,
        isDark: Boolean
    ): DomainColorScheme {
        val hsl = seedColor.toHSL()
        return when (style) {
            PreferenceValues.CoverBasedThemeStyle.TonalSpot -> generateTonalSpot(hsl, isDark)
            PreferenceValues.CoverBasedThemeStyle.Neutral -> generateNeutral(hsl, isDark)
            PreferenceValues.CoverBasedThemeStyle.Vibrant -> generateVibrant(hsl, isDark)
            PreferenceValues.CoverBasedThemeStyle.Expressive -> generateExpressive(hsl, isDark)
            PreferenceValues.CoverBasedThemeStyle.Rainbow -> generateRainbow(hsl, isDark)
            PreferenceValues.CoverBasedThemeStyle.FruitSalad -> generateFruitSalad(hsl, isDark)
            PreferenceValues.CoverBasedThemeStyle.Monochrome -> generateMonochrome(hsl, isDark)
            PreferenceValues.CoverBasedThemeStyle.Fidelity -> generateFidelity(hsl, isDark)
            PreferenceValues.CoverBasedThemeStyle.Content -> generateContent(hsl, isDark)
        }
    }
    
    private data class HSL(val hue: Float, val saturation: Float, val lightness: Float)
    
    private fun Color.toHSL(): HSL {
        val r = red
        val g = green
        val b = blue
        val max = maxOf(r, g, b)
        val min = minOf(r, g, b)
        val l = (max + min) / 2f
        val d = max - min
        val s = if (d == 0f) 0f else d / (1f - kotlin.math.abs(2 * l - 1f))
        val h = when {
            d == 0f -> 0f
            max == r -> (((g - b) / d) % 6f) * 60f
            max == g -> (((b - r) / d) + 2f) * 60f
            else -> (((r - g) / d) + 4f) * 60f
        }
        return HSL(if (h < 0f) h + 360f else h, s, l)
    }
    
    private fun hslToDomainColor(h: Float, s: Float, l: Float, alpha: Float = 1f): DomainColor {
        val c = (1f - kotlin.math.abs(2 * l - 1f)) * s
        val x = c * (1f - kotlin.math.abs(((h / 60f) % 2f) - 1f))
        val m = l - c / 2f
        val (r1, g1, b1) = when {
            h < 60f -> Triple(c, x, 0f)
            h < 120f -> Triple(x, c, 0f)
            h < 180f -> Triple(0f, c, x)
            h < 240f -> Triple(0f, x, c)
            h < 300f -> Triple(x, 0f, c)
            else -> Triple(c, 0f, x)
        }
        val r = ((r1 + m) * 255).roundToInt().coerceIn(0, 255)
        val g = ((g1 + m) * 255).roundToInt().coerceIn(0, 255)
        val b = ((b1 + m) * 255).roundToInt().coerceIn(0, 255)
        return DomainColor(r / 255f, g / 255f, b / 255f, alpha)
    }
    
    private fun hslContrastOn(hsl: HSL): DomainColor {
        return if (hsl.lightness > 0.55f) DomainColor(0f, 0f, 0f, 0.87f) else DomainColor(1f, 1f, 1f, 0.87f)
    }
    
    private fun hslContrastOn(h: Float, s: Float, l: Float): DomainColor = hslContrastOn(HSL(h, s, l))
    
    private fun baseColors(h: Float, s: Float, l: Float, isDark: Boolean): Triple<HSL, HSL, HSL> {
        val boostedS = (s * 1.5f).coerceAtMost(1f)
        return Triple(
            HSL(h, boostedS, if (isDark) 0.45f else 0.32f),
            HSL((h + 30f) % 360f, (boostedS * 0.85f).coerceAtMost(1f), if (isDark) 0.58f else 0.4f),
            HSL((h + 180f) % 360f, (boostedS * 0.9f).coerceAtMost(1f), if (isDark) 0.55f else 0.45f)
        )
    }
    
    private fun scheme(
        h: Float, s: Float, l: Float, isDark: Boolean,
        primaryHue: Float = h, primarySat: Float = s, primaryLight: Float = l,
        secondaryHue: Float = (h + 30f) % 360f, secondarySat: Float = s * 0.85f, secondaryLight: Float = if (isDark) 0.58f else 0.4f,
        tertiaryHue: Float = (h + 180f) % 360f, tertiarySat: Float = s * 0.9f, tertiaryLight: Float = if (isDark) 0.55f else 0.45f,
        bgSat: Float = s * 0.15f, bgLight: Float = if (isDark) 0.06f else 0.96f,
        surfSat: Float = s * 0.12f, surfLight: Float = if (isDark) 0.1f else 0.94f
    ): DomainColorScheme {
        val p = HSL(primaryHue, primarySat, primaryLight)
        val sec = HSL(secondaryHue, secondarySat, secondaryLight)
        val ter = HSL(tertiaryHue, tertiarySat, tertiaryLight)
        val primary = hslToDomainColor(p.hue, p.saturation, p.lightness)
        val onPrimary = hslContrastOn(p)
        val primaryContainer = hslToDomainColor(p.hue, p.saturation * 0.75f, if (isDark) 0.18f else 0.82f)
        val onPrimaryContainer = hslToDomainColor(p.hue, p.saturation * 0.9f, if (isDark) 0.85f else 0.15f)
        val secondary = hslToDomainColor(sec.hue, sec.saturation, sec.lightness)
        val onSecondary = hslContrastOn(sec)
        val secondaryContainer = hslToDomainColor(sec.hue, sec.saturation * 0.65f, if (isDark) 0.18f else 0.82f)
        val onSecondaryContainer = hslToDomainColor(sec.hue, sec.saturation * 0.8f, if (isDark) 0.82f else 0.12f)
        val tertiary = hslToDomainColor(ter.hue, ter.saturation, ter.lightness)
        val onTertiary = hslContrastOn(ter)
        val tertiaryContainer = hslToDomainColor(ter.hue, ter.saturation * 0.65f, if (isDark) 0.22f else 0.85f)
        val onTertiaryContainer = hslToDomainColor(ter.hue, ter.saturation * 0.8f, if (isDark) 0.85f else 0.12f)
        val background = hslToDomainColor(h, bgSat, bgLight)
        val onBackground = hslToDomainColor(h, bgSat * 2.5f, if (isDark) 0.92f else 0.12f)
        val surface = hslToDomainColor(h, surfSat, surfLight)
        val onSurface = hslToDomainColor(h, surfSat * 2.5f, if (isDark) 0.9f else 0.12f)
        val surfaceVariant = hslToDomainColor(h, surfSat, if (isDark) 0.18f else 0.9f)
        val onSurfaceVariant = hslToDomainColor(h, surfSat * 2f, if (isDark) 0.78f else 0.32f)
        val surfaceTint = primary
        val inverseSurface = hslToDomainColor(h, bgSat, if (isDark) bgLight else 0.1f)
        val inverseOnSurface = hslToDomainColor(h, surfSat * 2f, if (isDark) 0.12f else 0.88f)
        val error = DomainColor(0.75f, 0.18f, 0.12f, 1f)
        val onError = DomainColor(1f, 1f, 1f, 0.87f)
        val errorContainer = DomainColor(0.85f, 0.78f, 0.75f, 1f)
        val onErrorContainer = DomainColor(0.2f, 0.05f, 0.03f, 1f)
        val outline = hslToDomainColor(h, surfSat * 1.5f, if (isDark) 0.65f else 0.5f)
        val outlineVariant = hslToDomainColor(h, surfSat * 0.8f, if (isDark) 0.3f else 0.78f)
        val scrim = DomainColor(0f, 0f, 0f, 0.7f)
        return DomainColorScheme(
            primary = primary, onPrimary = onPrimary, primaryContainer = primaryContainer, onPrimaryContainer = onPrimaryContainer,
            inversePrimary = hslToDomainColor(h, s, if (isDark) 0.58f else 0.45f),
            secondary = secondary, onSecondary = onSecondary, secondaryContainer = secondaryContainer, onSecondaryContainer = onSecondaryContainer,
            tertiary = tertiary, onTertiary = onTertiary, tertiaryContainer = tertiaryContainer, onTertiaryContainer = onTertiaryContainer,
            background = background, onBackground = onBackground, surface = surface, onSurface = onSurface,
            surfaceVariant = surfaceVariant, onSurfaceVariant = onSurfaceVariant, surfaceTint = surfaceTint,
            inverseSurface = inverseSurface, inverseOnSurface = inverseOnSurface,
            error = error, onError = onError, errorContainer = errorContainer, onErrorContainer = onErrorContainer,
            outline = outline, outlineVariant = outlineVariant, scrim = scrim
        )
    }
    
    private fun generateTonalSpot(hsl: HSL, isDark: Boolean): DomainColorScheme {
        val (p, sec, ter) = baseColors(hsl.hue, hsl.saturation, hsl.lightness, isDark)
        return scheme(hsl.hue, (hsl.saturation * 1.3f).coerceAtMost(1f), hsl.lightness, isDark, p.hue, p.saturation, p.lightness, sec.hue, sec.saturation, sec.lightness, ter.hue, ter.saturation, ter.lightness)
    }
    
    private fun generateNeutral(hsl: HSL, isDark: Boolean): DomainColorScheme {
        val ns = (hsl.saturation * 0.45f).coerceAtMost(0.55f)
        val (p, sec, ter) = baseColors(hsl.hue, ns, hsl.lightness, isDark)
        return scheme(hsl.hue, ns, hsl.lightness, isDark, p.hue, p.saturation, p.lightness, sec.hue, sec.saturation, sec.lightness, ter.hue, ter.saturation, ter.lightness)
    }
    
    private fun generateVibrant(hsl: HSL, isDark: Boolean): DomainColorScheme {
        val vs = (hsl.saturation * 1.6f).coerceAtMost(1f)
        val (p, sec, ter) = baseColors(hsl.hue, vs, hsl.lightness, isDark)
        return scheme(hsl.hue, vs, hsl.lightness, isDark, p.hue, p.saturation, p.lightness, (hsl.hue + 40f) % 360f, vs * 0.95f, sec.lightness, (hsl.hue + 200f) % 360f, vs * 0.9f, ter.lightness)
    }
    
    private fun generateExpressive(hsl: HSL, isDark: Boolean): DomainColorScheme {
        val es = (hsl.saturation * 1.5f).coerceAtMost(1f)
        return scheme(hsl.hue, es, hsl.lightness, isDark, hsl.hue, es, if (isDark) 0.52f else 0.32f, (hsl.hue + 60f) % 360f, es * 0.9f, if (isDark) 0.55f else 0.35f, (hsl.hue + 150f) % 360f, es * 0.95f, if (isDark) 0.52f else 0.38f)
    }
    
    private fun generateRainbow(hsl: HSL, isDark: Boolean): DomainColorScheme {
        val s = hsl.saturation.coerceAtLeast(0.85f)
        return scheme(hsl.hue, s, hsl.lightness, isDark, hsl.hue, s, if (isDark) 0.58f else 0.35f, (hsl.hue + 90f) % 360f, s * 0.95f, if (isDark) 0.55f else 0.38f, (hsl.hue + 210f) % 360f, s * 0.9f, if (isDark) 0.52f else 0.4f)
    }
    
    private fun generateFruitSalad(hsl: HSL, isDark: Boolean): DomainColorScheme {
        val fs = (hsl.saturation * 1.55f).coerceAtMost(1f)
        return scheme(hsl.hue, fs, hsl.lightness, isDark, hsl.hue, fs, if (isDark) 0.5f else 0.35f, (hsl.hue + 45f) % 360f, fs * 0.95f, if (isDark) 0.52f else 0.37f, (hsl.hue + 160f) % 360f, fs * 0.9f, if (isDark) 0.5f else 0.4f)
    }
    
    private fun generateMonochrome(hsl: HSL, isDark: Boolean): DomainColorScheme {
        val ms = (hsl.saturation * 0.2f).coerceAtMost(0.25f)
        return scheme(hsl.hue, ms, hsl.lightness, isDark, hsl.hue, ms, if (isDark) 0.55f else 0.35f, hsl.hue, ms * 0.9f, if (isDark) 0.5f else 0.38f, hsl.hue, ms * 0.85f, if (isDark) 0.48f else 0.4f)
    }
    
    private fun generateFidelity(hsl: HSL, isDark: Boolean): DomainColorScheme {
        val s = hsl.saturation.coerceAtLeast(0.65f)
        val primaryL = if (isDark) 0.6f else 0.35f
        val secondaryL = if (isDark) 0.55f else 0.38f
        val tertiaryL = if (isDark) 0.52f else 0.4f
        return scheme(hsl.hue, s, hsl.lightness, isDark, hsl.hue, s, primaryL, (hsl.hue + 35f) % 360f, s * 0.9f, secondaryL, (hsl.hue + 170f) % 360f, s * 0.85f, tertiaryL)
    }
    
    private fun generateContent(hsl: HSL, isDark: Boolean): DomainColorScheme {
        val s = hsl.saturation.coerceAtLeast(0.55f)
        val primaryL = if (isDark) 0.62f else 0.34f
        val secondaryL = if (isDark) 0.56f else 0.37f
        val tertiaryL = if (isDark) 0.52f else 0.39f
        return scheme(hsl.hue, s, hsl.lightness, isDark, hsl.hue, s, primaryL, (hsl.hue + 25f) % 360f, s * 0.75f, secondaryL, (hsl.hue + 190f) % 360f, s * 0.8f, tertiaryL)
    }
}
