package ireader.presentation.core.theme

import androidx.compose.ui.graphics.Color
import ireader.domain.models.common.DomainColor
import ireader.domain.models.theme.DomainColorScheme
import kotlin.math.roundToInt

/**
 * Builds a Material-style palette from a single cover-extracted seed color.
 * Fully automatic: muted covers stay muted, vivid covers stay vivid.
 */
object Material3PaletteGenerator {

    fun generate(seedColor: Color, isDark: Boolean): DomainColorScheme {
        val hsl = seedColor.toHSL()
        // Adaptive saturation: near-gray covers stay quiet, mid covers get a lift,
        // vivid covers keep (most of) their punch.
        val s = when {
            hsl.saturation <= 0.1f -> hsl.saturation.coerceAtLeast(0.05f)
            hsl.saturation < 0.35f -> 0.45f
            else -> (hsl.saturation * 1.25f).coerceAtMost(1f)
        }
        return scheme(
            hsl.hue, s, hsl.lightness, isDark,
            hsl.hue, s, if (isDark) 0.45f else 0.32f,
            (hsl.hue + 30f) % 360f, (s * 0.85f).coerceAtMost(1f), if (isDark) 0.58f else 0.4f,
            (hsl.hue + 180f) % 360f, (s * 0.9f).coerceAtMost(1f), if (isDark) 0.55f else 0.45f
        )
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
        // Decide by perceptual luminance, not HSL lightness — blue hues at the same
        // lightness are visibly darker than yellow ones.
        val c = hslToDomainColor(hsl.hue, hsl.saturation, hsl.lightness)
        return if (c.luminance() > 0.5f) DomainColor(0f, 0f, 0f, 0.87f) else DomainColor(1f, 1f, 1f, 0.87f)
    }

    private fun scheme(
        h: Float, s: Float, l: Float, isDark: Boolean,
        primaryHue: Float = h, primarySat: Float = s, primaryLight: Float = l,
        secondaryHue: Float = (h + 30f) % 360f, secondarySat: Float = s * 0.85f, secondaryLight: Float = if (isDark) 0.58f else 0.4f,
        tertiaryHue: Float = (h + 180f) % 360f, tertiarySat: Float = s * 0.9f, tertiaryLight: Float = if (isDark) 0.55f else 0.45f,
        bgSat: Float = s * 0.25f, bgLight: Float = if (isDark) 0.06f else 0.96f,
        surfSat: Float = s * 0.2f, surfLight: Float = if (isDark) 0.1f else 0.94f
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
}
