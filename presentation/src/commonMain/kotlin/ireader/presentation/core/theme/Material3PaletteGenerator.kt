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
        isDark: Boolean,
        saturation: Float = 1.0f,
        intensity: Float = 1.0f,
        brightness: Float = 1.0f,
        textColorMode: PreferenceValues.CoverBasedTextColorMode = PreferenceValues.CoverBasedTextColorMode.Auto,
        contrast: PreferenceValues.CoverBasedThemeContrast = PreferenceValues.CoverBasedThemeContrast.Vibrant,
        surfaceTinting: Boolean = true,
        backgroundTintOpacity: Float = 0.3f
    ): DomainColorScheme {
        val hsl = seedColor.toHSL()
        return when (style) {
            PreferenceValues.CoverBasedThemeStyle.TonalSpot -> generateTonalSpot(hsl, isDark, saturation, intensity, brightness, textColorMode, contrast, surfaceTinting, backgroundTintOpacity)
            PreferenceValues.CoverBasedThemeStyle.Neutral -> generateNeutral(hsl, isDark, saturation, intensity, brightness, textColorMode, contrast, surfaceTinting, backgroundTintOpacity)
            PreferenceValues.CoverBasedThemeStyle.Vibrant -> generateVibrant(hsl, isDark, saturation, intensity, brightness, textColorMode, contrast, surfaceTinting, backgroundTintOpacity)
            PreferenceValues.CoverBasedThemeStyle.Expressive -> generateExpressive(hsl, isDark, saturation, intensity, brightness, textColorMode, contrast, surfaceTinting, backgroundTintOpacity)
            PreferenceValues.CoverBasedThemeStyle.Rainbow -> generateRainbow(hsl, isDark, saturation, intensity, brightness, textColorMode, contrast, surfaceTinting, backgroundTintOpacity)
            PreferenceValues.CoverBasedThemeStyle.FruitSalad -> generateFruitSalad(hsl, isDark, saturation, intensity, brightness, textColorMode, contrast, surfaceTinting, backgroundTintOpacity)
            PreferenceValues.CoverBasedThemeStyle.Monochrome -> generateMonochrome(hsl, isDark, saturation, intensity, brightness, textColorMode, contrast, surfaceTinting, backgroundTintOpacity)
            PreferenceValues.CoverBasedThemeStyle.Fidelity -> generateFidelity(hsl, isDark, saturation, intensity, brightness, textColorMode, contrast, surfaceTinting, backgroundTintOpacity)
            PreferenceValues.CoverBasedThemeStyle.Content -> generateContent(hsl, isDark, saturation, intensity, brightness, textColorMode, contrast, surfaceTinting, backgroundTintOpacity)
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
    
    private fun hslContrastOn(hsl: HSL, textColorMode: PreferenceValues.CoverBasedTextColorMode = PreferenceValues.CoverBasedTextColorMode.Auto): DomainColor {
        return when (textColorMode) {
            PreferenceValues.CoverBasedTextColorMode.Light -> DomainColor(1f, 1f, 1f, 0.87f)
            PreferenceValues.CoverBasedTextColorMode.Dark -> DomainColor(0f, 0f, 0f, 0.87f)
            else -> if (hsl.lightness > 0.55f) DomainColor(0f, 0f, 0f, 0.87f) else DomainColor(1f, 1f, 1f, 0.87f)
        }
    }
    
    private fun hslContrastOn(h: Float, s: Float, l: Float, textColorMode: PreferenceValues.CoverBasedTextColorMode = PreferenceValues.CoverBasedTextColorMode.Auto): DomainColor = hslContrastOn(HSL(h, s, l), textColorMode)
    
    private fun baseColors(h: Float, s: Float, l: Float, isDark: Boolean, saturation: Float = 1.0f, intensity: Float = 1.0f, brightness: Float = 1.0f, contrast: PreferenceValues.CoverBasedThemeContrast = PreferenceValues.CoverBasedThemeContrast.Vibrant): Triple<HSL, HSL, HSL> {
        val contrastS = when (contrast) {
            PreferenceValues.CoverBasedThemeContrast.Vibrant -> 1f
            PreferenceValues.CoverBasedThemeContrast.Muted -> 0.7f
            PreferenceValues.CoverBasedThemeContrast.HighContrast -> 1.2f
        }
        val contrastL = when (contrast) {
            PreferenceValues.CoverBasedThemeContrast.Vibrant -> 1f
            PreferenceValues.CoverBasedThemeContrast.Muted -> 1.05f
            PreferenceValues.CoverBasedThemeContrast.HighContrast -> 1.1f
        }
        val boostedS = (s * 1.5f * saturation * contrastS).coerceAtMost(1f)
        val adjustedL = (l * intensity * brightness * contrastL).coerceIn(0.05f, 0.95f)
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
        surfSat: Float = s * 0.12f, surfLight: Float = if (isDark) 0.1f else 0.94f,
        textColorMode: PreferenceValues.CoverBasedTextColorMode = PreferenceValues.CoverBasedTextColorMode.Auto,
        contrast: PreferenceValues.CoverBasedThemeContrast = PreferenceValues.CoverBasedThemeContrast.Vibrant,
        surfaceTinting: Boolean = true,
        backgroundTintOpacity: Float = 0.3f
    ): DomainColorScheme {
        val effectiveBgSat = if (surfaceTinting) bgSat * backgroundTintOpacity else 0f
        val effectiveSurfSat = if (surfaceTinting) surfSat * backgroundTintOpacity else 0f
        val p = HSL(primaryHue, primarySat, primaryLight)
        val sec = HSL(secondaryHue, secondarySat, secondaryLight)
        val ter = HSL(tertiaryHue, tertiarySat, tertiaryLight)
        val primary = hslToDomainColor(p.hue, p.saturation, p.lightness)
        val onPrimary = hslContrastOn(HSL(p.hue, p.saturation * 0.75f, if (isDark) 0.18f else 0.82f), textColorMode)
        val primaryContainer = hslToDomainColor(p.hue, (p.saturation * 0.6f).coerceAtMost(0.65f), if (isDark) 0.18f else 0.82f)
        val onPrimaryContainer = hslContrastOn(HSL(p.hue, (p.saturation * 0.7f).coerceAtMost(0.75f), if (isDark) 0.18f else 0.82f), textColorMode)
        val secondary = hslToDomainColor(sec.hue, sec.saturation, sec.lightness)
        val onSecondary = hslContrastOn(HSL(sec.hue, sec.saturation * 0.65f, if (isDark) 0.18f else 0.82f), textColorMode)
        val secondaryContainer = hslToDomainColor(sec.hue, (sec.saturation * 0.65f).coerceAtMost(0.6f), if (isDark) 0.18f else 0.82f)
        val onSecondaryContainer = hslContrastOn(HSL(sec.hue, (sec.saturation * 0.8f).coerceAtMost(0.7f), if (isDark) 0.18f else 0.82f), textColorMode)
        val tertiary = hslToDomainColor(ter.hue, ter.saturation, ter.lightness)
        val onTertiary = hslContrastOn(HSL(ter.hue, ter.saturation * 0.65f, if (isDark) 0.22f else 0.85f), textColorMode)
        val tertiaryContainer = hslToDomainColor(ter.hue, (ter.saturation * 0.65f).coerceAtMost(0.6f), if (isDark) 0.22f else 0.85f)
        val onTertiaryContainer = hslContrastOn(HSL(ter.hue, (ter.saturation * 0.8f).coerceAtMost(0.7f), if (isDark) 0.22f else 0.85f), textColorMode)
        val background = hslToDomainColor(h, effectiveBgSat, bgLight)
        val onBackground = hslContrastOn(HSL(h, effectiveBgSat, bgLight), textColorMode)
        val surface = hslToDomainColor(h, effectiveSurfSat, surfLight)
        val onSurface = hslContrastOn(HSL(h, effectiveSurfSat, surfLight), textColorMode)
        val surfaceVariant = hslToDomainColor(h, effectiveSurfSat, if (isDark) 0.18f else 0.9f)
        val onSurfaceVariant = hslContrastOn(HSL(h, effectiveSurfSat, if (isDark) 0.18f else 0.9f), textColorMode)
        val surfaceTint = primary
        val inverseSurface = hslToDomainColor(h, effectiveBgSat, if (isDark) bgLight else 0.1f)
        val inverseOnSurface = hslContrastOn(HSL(h, effectiveBgSat, if (isDark) bgLight else 0.1f), textColorMode)
        val error = DomainColor(0.75f, 0.18f, 0.12f, 1f)
        val onError = hslContrastOn(HSL(0f, 0f, if (isDark) 0.9f else 0.12f), textColorMode)
        val errorContainer = DomainColor(0.85f, 0.78f, 0.75f, 1f)
        val onErrorContainer = hslContrastOn(HSL(0f, 0f, if (isDark) 0.12f else 0.88f), textColorMode)
        val outline = hslToDomainColor(h, effectiveSurfSat * 1.5f, if (isDark) 0.65f else 0.5f)
        val outlineVariant = hslToDomainColor(h, effectiveSurfSat * 0.8f, if (isDark) 0.3f else 0.78f)
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
    
    private fun generateTonalSpot(hsl: HSL, isDark: Boolean, saturation: Float = 1.0f, intensity: Float = 1.0f, brightness: Float = 1.0f, textColorMode: PreferenceValues.CoverBasedTextColorMode = PreferenceValues.CoverBasedTextColorMode.Auto, contrast: PreferenceValues.CoverBasedThemeContrast = PreferenceValues.CoverBasedThemeContrast.Vibrant, surfaceTinting: Boolean = true, backgroundTintOpacity: Float = 0.3f): DomainColorScheme {
        val boostedS = (hsl.saturation * 1.3f * saturation).coerceAtMost(1f)
        val (p, sec, ter) = baseColors(hsl.hue, boostedS, hsl.lightness, isDark, saturation, intensity, brightness, contrast)
        val effectiveBgSat = if (surfaceTinting) boostedS * 0.15f else 0f
        val effectiveSurfSat = if (surfaceTinting) boostedS * 0.12f else 0f
        return scheme(hsl.hue, boostedS, hsl.lightness, isDark, p.hue, p.saturation, p.lightness, sec.hue, sec.saturation, sec.lightness, ter.hue, ter.saturation, ter.lightness, effectiveBgSat, if (isDark) 0.06f else 0.96f, effectiveSurfSat, if (isDark) 0.1f else 0.94f, textColorMode = textColorMode, contrast = contrast, surfaceTinting = surfaceTinting, backgroundTintOpacity = backgroundTintOpacity)
    }
    
    private fun generateNeutral(hsl: HSL, isDark: Boolean, saturation: Float = 1.0f, intensity: Float = 1.0f, brightness: Float = 1.0f, textColorMode: PreferenceValues.CoverBasedTextColorMode = PreferenceValues.CoverBasedTextColorMode.Auto, contrast: PreferenceValues.CoverBasedThemeContrast = PreferenceValues.CoverBasedThemeContrast.Vibrant, surfaceTinting: Boolean = true, backgroundTintOpacity: Float = 0.3f): DomainColorScheme {
        val ns = (hsl.saturation * 0.45f * saturation).coerceAtMost(0.55f)
        val (p, sec, ter) = baseColors(hsl.hue, ns, hsl.lightness, isDark, saturation, intensity, brightness, contrast)
        val effectiveBgSat = if (surfaceTinting) ns * 0.15f else 0f
        val effectiveSurfSat = if (surfaceTinting) ns * 0.12f else 0f
        return scheme(hsl.hue, ns, hsl.lightness, isDark, p.hue, p.saturation, p.lightness, sec.hue, sec.saturation, sec.lightness, ter.hue, ter.saturation, ter.lightness, effectiveBgSat, if (isDark) 0.06f else 0.96f, effectiveSurfSat, if (isDark) 0.1f else 0.94f, textColorMode = textColorMode, contrast = contrast, surfaceTinting = surfaceTinting, backgroundTintOpacity = backgroundTintOpacity)
    }
    
    private fun generateVibrant(hsl: HSL, isDark: Boolean, saturation: Float = 1.0f, intensity: Float = 1.0f, brightness: Float = 1.0f, textColorMode: PreferenceValues.CoverBasedTextColorMode = PreferenceValues.CoverBasedTextColorMode.Auto, contrast: PreferenceValues.CoverBasedThemeContrast = PreferenceValues.CoverBasedThemeContrast.Vibrant, surfaceTinting: Boolean = true, backgroundTintOpacity: Float = 0.3f): DomainColorScheme {
        val vs = (hsl.saturation * 1.6f * saturation).coerceAtMost(1f)
        val (p, sec, ter) = baseColors(hsl.hue, vs, hsl.lightness, isDark, saturation, intensity, brightness, contrast)
        val effectiveBgSat = if (surfaceTinting) vs * 0.15f else 0f
        val effectiveSurfSat = if (surfaceTinting) vs * 0.12f else 0f
        return scheme(hsl.hue, vs, hsl.lightness, isDark, p.hue, p.saturation, p.lightness, (hsl.hue + 40f) % 360f, vs * 0.95f, sec.lightness, (hsl.hue + 200f) % 360f, vs * 0.9f, ter.lightness, effectiveBgSat, if (isDark) 0.06f else 0.96f, effectiveSurfSat, if (isDark) 0.1f else 0.94f, textColorMode = textColorMode, contrast = contrast, surfaceTinting = surfaceTinting, backgroundTintOpacity = backgroundTintOpacity)
    }
    
    private fun generateExpressive(hsl: HSL, isDark: Boolean, saturation: Float = 1.0f, intensity: Float = 1.0f, brightness: Float = 1.0f, textColorMode: PreferenceValues.CoverBasedTextColorMode = PreferenceValues.CoverBasedTextColorMode.Auto, contrast: PreferenceValues.CoverBasedThemeContrast = PreferenceValues.CoverBasedThemeContrast.Vibrant, surfaceTinting: Boolean = true, backgroundTintOpacity: Float = 0.3f): DomainColorScheme {
        val es = (hsl.saturation * 1.5f * saturation).coerceAtMost(1f)
        val effectiveBgSat = if (surfaceTinting) es * 0.15f else 0f
        val effectiveSurfSat = if (surfaceTinting) es * 0.12f else 0f
        return scheme(hsl.hue, es, hsl.lightness, isDark, hsl.hue, es, if (isDark) 0.52f else 0.32f, (hsl.hue + 60f) % 360f, es * 0.9f, if (isDark) 0.55f else 0.35f, (hsl.hue + 150f) % 360f, es * 0.95f, if (isDark) 0.52f else 0.38f, effectiveBgSat, if (isDark) 0.06f else 0.96f, effectiveSurfSat, if (isDark) 0.1f else 0.94f, textColorMode = textColorMode, contrast = contrast, surfaceTinting = surfaceTinting, backgroundTintOpacity = backgroundTintOpacity)
    }
    
    private fun generateRainbow(hsl: HSL, isDark: Boolean, saturation: Float = 1.0f, intensity: Float = 1.0f, brightness: Float = 1.0f, textColorMode: PreferenceValues.CoverBasedTextColorMode = PreferenceValues.CoverBasedTextColorMode.Auto, contrast: PreferenceValues.CoverBasedThemeContrast = PreferenceValues.CoverBasedThemeContrast.Vibrant, surfaceTinting: Boolean = true, backgroundTintOpacity: Float = 0.3f): DomainColorScheme {
        val s = (hsl.saturation * saturation).coerceAtLeast(0.85f)
        val effectiveBgSat = if (surfaceTinting) s * 0.15f else 0f
        val effectiveSurfSat = if (surfaceTinting) s * 0.12f else 0f
        return scheme(hsl.hue, s, hsl.lightness, isDark, hsl.hue, s, if (isDark) 0.58f else 0.35f, (hsl.hue + 90f) % 360f, s * 0.95f, if (isDark) 0.55f else 0.38f, (hsl.hue + 210f) % 360f, s * 0.9f, if (isDark) 0.52f else 0.4f, effectiveBgSat, if (isDark) 0.06f else 0.96f, effectiveSurfSat, if (isDark) 0.1f else 0.94f, textColorMode = textColorMode, contrast = contrast, surfaceTinting = surfaceTinting, backgroundTintOpacity = backgroundTintOpacity)
    }
    
    private fun generateFruitSalad(hsl: HSL, isDark: Boolean, saturation: Float = 1.0f, intensity: Float = 1.0f, brightness: Float = 1.0f, textColorMode: PreferenceValues.CoverBasedTextColorMode = PreferenceValues.CoverBasedTextColorMode.Auto, contrast: PreferenceValues.CoverBasedThemeContrast = PreferenceValues.CoverBasedThemeContrast.Vibrant, surfaceTinting: Boolean = true, backgroundTintOpacity: Float = 0.3f): DomainColorScheme {
        val fs = (hsl.saturation * 1.55f * saturation).coerceAtMost(1f)
        val effectiveBgSat = if (surfaceTinting) fs * 0.15f else 0f
        val effectiveSurfSat = if (surfaceTinting) fs * 0.12f else 0f
        return scheme(hsl.hue, fs, hsl.lightness, isDark, hsl.hue, fs, if (isDark) 0.5f else 0.35f, (hsl.hue + 45f) % 360f, fs * 0.95f, if (isDark) 0.52f else 0.37f, (hsl.hue + 160f) % 360f, fs * 0.9f, if (isDark) 0.5f else 0.4f, effectiveBgSat, if (isDark) 0.06f else 0.96f, effectiveSurfSat, if (isDark) 0.1f else 0.94f, textColorMode = textColorMode, contrast = contrast, surfaceTinting = surfaceTinting, backgroundTintOpacity = backgroundTintOpacity)
    }
    
    private fun generateMonochrome(hsl: HSL, isDark: Boolean, saturation: Float = 1.0f, intensity: Float = 1.0f, brightness: Float = 1.0f, textColorMode: PreferenceValues.CoverBasedTextColorMode = PreferenceValues.CoverBasedTextColorMode.Auto, contrast: PreferenceValues.CoverBasedThemeContrast = PreferenceValues.CoverBasedThemeContrast.Vibrant, surfaceTinting: Boolean = true, backgroundTintOpacity: Float = 0.3f): DomainColorScheme {
        val ms = (hsl.saturation * 0.2f * saturation).coerceAtMost(0.25f)
        val effectiveBgSat = if (surfaceTinting) ms * 0.15f else 0f
        val effectiveSurfSat = if (surfaceTinting) ms * 0.12f else 0f
        return scheme(hsl.hue, ms, hsl.lightness, isDark, hsl.hue, ms, if (isDark) 0.55f else 0.35f, hsl.hue, ms * 0.9f, if (isDark) 0.5f else 0.38f, hsl.hue, ms * 0.85f, if (isDark) 0.48f else 0.4f, effectiveBgSat, if (isDark) 0.06f else 0.96f, effectiveSurfSat, if (isDark) 0.1f else 0.94f, textColorMode = textColorMode, contrast = contrast, surfaceTinting = surfaceTinting, backgroundTintOpacity = backgroundTintOpacity)
    }
    
    private fun generateFidelity(hsl: HSL, isDark: Boolean, saturation: Float = 1.0f, intensity: Float = 1.0f, brightness: Float = 1.0f, textColorMode: PreferenceValues.CoverBasedTextColorMode = PreferenceValues.CoverBasedTextColorMode.Auto, contrast: PreferenceValues.CoverBasedThemeContrast = PreferenceValues.CoverBasedThemeContrast.Vibrant, surfaceTinting: Boolean = true, backgroundTintOpacity: Float = 0.3f): DomainColorScheme {
        val s = hsl.saturation.coerceAtLeast(0.65f)
        val effectiveBgSat = if (surfaceTinting) s * 0.15f else 0f
        val effectiveSurfSat = if (surfaceTinting) s * 0.12f else 0f
        return scheme(hsl.hue, s, hsl.lightness, isDark, hsl.hue, s, if (isDark) 0.6f else 0.35f, (hsl.hue + 35f) % 360f, s * 0.9f, if (isDark) 0.55f else 0.38f, (hsl.hue + 170f) % 360f, s * 0.85f, if (isDark) 0.52f else 0.4f, effectiveBgSat, if (isDark) 0.06f else 0.96f, effectiveSurfSat, if (isDark) 0.1f else 0.94f, textColorMode = textColorMode, contrast = contrast, surfaceTinting = surfaceTinting, backgroundTintOpacity = backgroundTintOpacity)
    }
    
    private fun generateContent(hsl: HSL, isDark: Boolean, saturation: Float = 1.0f, intensity: Float = 1.0f, brightness: Float = 1.0f, textColorMode: PreferenceValues.CoverBasedTextColorMode = PreferenceValues.CoverBasedTextColorMode.Auto, contrast: PreferenceValues.CoverBasedThemeContrast = PreferenceValues.CoverBasedThemeContrast.Vibrant, surfaceTinting: Boolean = true, backgroundTintOpacity: Float = 0.3f): DomainColorScheme {
        val s = hsl.saturation.coerceAtLeast(0.55f)
        val effectiveBgSat = if (surfaceTinting) s * 0.15f else 0f
        val effectiveSurfSat = if (surfaceTinting) s * 0.12f else 0f
        return scheme(hsl.hue, s, hsl.lightness, isDark, hsl.hue, s, if (isDark) 0.62f else 0.34f, (hsl.hue + 25f) % 360f, s * 0.75f, if (isDark) 0.56f else 0.37f, (hsl.hue + 190f) % 360f, s * 0.8f, if (isDark) 0.52f else 0.39f, effectiveBgSat, if (isDark) 0.06f else 0.96f, effectiveSurfSat, if (isDark) 0.1f else 0.94f, textColorMode = textColorMode, contrast = contrast, surfaceTinting = surfaceTinting, backgroundTintOpacity = backgroundTintOpacity)
    }
}
