package ireader.presentation.core.theme

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.intl.Locale
import ireader.core.prefs.Preference
import ireader.domain.data.repository.ThemeRepository
import ireader.domain.models.prefs.PreferenceValues
import ireader.domain.models.theme.ExtraColors
import ireader.domain.models.theme.Theme
import ireader.domain.preferences.prefs.UiPreferences
import ireader.domain.utils.removeIf
import ireader.presentation.ui.core.theme.AppTypography
import ireader.presentation.ui.core.theme.asState
import ireader.presentation.ui.core.theme.getAppUiFontFamily
import ireader.presentation.ui.core.theme.getDarkColors
import ireader.presentation.ui.core.theme.getLightColors
import ireader.presentation.ui.core.theme.isLight
import ireader.presentation.ui.core.theme.themes
import ireader.presentation.ui.core.ui.PreferenceMutableState
import ireader.presentation.core.toComposeColor
import ireader.presentation.core.toComposeColorScheme
import ireader.presentation.core.toDomainColor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class AppThemeViewModel(
    private val uiPreferences: UiPreferences,
    private val themeRepository: ThemeRepository,
    private val dynamicColorScheme: DynamicColorScheme,
    val scope: CoroutineScope,
    private val coverBasedThemeManager: CoverBasedThemeManager? = null
) {
    fun <T> Preference<T>.asState() = PreferenceMutableState(this, scope)

    // Keep state objects for preferences - these trigger recomposition when values change
    private val themeModeState = uiPreferences.themeMode().asState()
    private val colorThemeState = uiPreferences.colorTheme().asState()
    private val dynamicColorModeState = uiPreferences.dynamicColorMode().asState()
    private val coverBasedThemeEnabledState = uiPreferences.coverBasedThemeEnabled().asState()
    private val useTrueBlackState = uiPreferences.useTrueBlack().asState()
    private val appUiFontState = uiPreferences.appUiFont().asState()

    // Pre-create color states for both light and dark modes
    // This ensures we always have reactive state objects ready
    private val lightColorsState = uiPreferences.getLightColors().asState(scope)
    private val darkColorsState = uiPreferences.getDarkColors().asState(scope)

    init {
        themeRepository.subscribe().onEach {
            themes.removeIf { baseTheme -> baseTheme.id > 0L }
            themes.addAll(it)
        }.launchIn(scope)

        // Clear the published scheme when the feature is switched off
        uiPreferences.coverBasedThemeEnabled().changes().onEach { enabled ->
            if (!enabled) coverBasedThemeManager?.clearAll()
        }.launchIn(scope)
    }


    @Composable
    fun getColors(): Pair<ColorScheme, ExtraColors> {
        // Read state values inside composable to trigger recomposition when they change
        val themeMode = themeModeState.value
        val colorTheme = colorThemeState.value
        val dynamicColorMode = dynamicColorModeState.value
        val coverBasedThemeEnabled = coverBasedThemeEnabledState.value
        val useTrueBlack = useTrueBlackState.value
        
        val baseTheme = getBaseTheme(themeMode, colorTheme)
        val isLight = baseTheme.materialColors.toComposeColorScheme().isLight()
        
        // Use the pre-created color states based on current theme mode
        // Reading .value triggers recomposition when colors change
        val colors = if (isLight) lightColorsState else darkColorsState
        val customBarsColor = colors.bars.value.toComposeColor()
        val customPrimaryColor = colors.primary.value.toComposeColor()
        val customSecondaryColor = colors.secondary.value.toComposeColor()

        // Check if dynamic colors should be used
        val useDynamicColors = dynamicColorMode && dynamicColorScheme.isSupported()

        // Step 1: Get base color scheme (either dynamic or theme-based)
        var materialColors: ColorScheme = if (useDynamicColors) {
            try {
                if (isLight) {
                    dynamicColorScheme.lightColorScheme()
                } else {
                    dynamicColorScheme.darkColorScheme()
                } ?: baseTheme.materialColors.toComposeColorScheme()
            } catch (e: Exception) {
                baseTheme.materialColors.toComposeColorScheme()
            }
        } else {
            baseTheme.materialColors.toComposeColorScheme()
        }
        
        // Step 1b: Apply cover-based theme if enabled. The cover scheme wins over
        // dynamic/base themes; user custom primary/secondary are skipped so they
        // don't fight the extracted palette.
        var usingCoverScheme = false
        if (coverBasedThemeEnabled && coverBasedThemeManager != null) {
            val coverScheme = coverBasedThemeManager.coverBasedTheme.collectAsState(initial = null).value
            // Fade toward the cover scheme on enter, fade back to base on leave —
            // no hard snap in either direction.
            val animated = blendCoverScheme(materialColors, coverScheme?.toComposeColorScheme())
            if (coverScheme != null) {
                materialColors = animated
                usingCoverScheme = true
            } else {
                materialColors = animated
            }
        }
         
        // Step 2: Apply custom primary/secondary colors if specified
        val customPrimary = customPrimaryColor.takeIf { it != Color.Unspecified }
        val customSecondary = customSecondaryColor.takeIf { it != Color.Unspecified }
        
        if (!usingCoverScheme && (customPrimary != null || customSecondary != null)) {
            materialColors = ThemeColorUtils.applyCustomColors(
                materialColors,
                customPrimary,
                customSecondary
            )
        }
         
        // Step 3: Apply true black mode if enabled for dark themes
        if (!isLight && useTrueBlack) {
            materialColors = ThemeColorUtils.applyTrueBlack(materialColors)
        }
         
        // Step 4: Ensure all "on" colors have proper contrast
        materialColors = ThemeColorUtils.ensureProperOnColors(materialColors)
        
        // Step 5: Create extra colors for bars
        // Use base theme's bar color, but allow custom override
        val extraColors = createExtraColors(
            baseExtraColors = baseTheme.extraColors,
            customBarsColor = customBarsColor,
            materialColors = materialColors,
            isLight = isLight,
            useTrueBlack = useTrueBlack
        )
        
        return materialColors to extraColors
    }
    
    /**
     * [owner] is a composition-scoped token (e.g. remember { Any() }) identifying the
     * claiming screen; its dispose must clear with the same token so stale screens
     * can't wipe a theme claimed by someone else.
     */
    fun setCurrentCoverUrl(coverUrl: String?, sourceId: Long?, isDark: Boolean, owner: Any) {
        if (coverBasedThemeManager == null) return
        if (!coverBasedThemeEnabledState.value || coverUrl == null) {
            coverBasedThemeManager.clear(owner)
            return
        }
        val resolvedIsDark = when (uiPreferences.themeMode().get()) {
            PreferenceValues.ThemeMode.Light -> false
            PreferenceValues.ThemeMode.Dark -> true
            else -> isDark
        }
        coverBasedThemeManager.applyCoverBasedTheme(coverUrl, sourceId, resolvedIsDark, owner)
    }

    /** Feature switched off — unconditional clear regardless of owner. */
    fun onCoverBasedThemeDisabled() {
        coverBasedThemeManager?.clearAll()
    }

    /**
     * Cross-fades [base] toward the cover scheme on appear, and back to [base] on
     * disappear — turning hard theme snaps into smooth fades.
     */
    @Composable
    private fun blendCoverScheme(base: ColorScheme, cover: ColorScheme?): ColorScheme {
        var lastCover by remember { mutableStateOf(cover) }
        val fraction = remember { Animatable(if (cover != null) 1f else 0f) }

        LaunchedEffect(cover) {
            if (cover != null) lastCover = cover
            fraction.animateTo(if (cover != null) 1f else 0f, tween(durationMillis = 400, easing = FastOutSlowInEasing))
        }

        if (fraction.value >= 1f) return cover ?: base
        if (fraction.value <= 0f) return base
        val from = if (cover != null) base else lastCover ?: return base
        val to = cover ?: base
        return from.lerpTo(to, fraction.value)
    }

    @Composable
    private fun getBaseTheme(
        themeMode: PreferenceValues.ThemeMode,
        colorTheme: Long,
    ): Theme {
        @Composable
        fun getTheme(fallbackIsLight: Boolean): Theme {
            return themes.firstOrNull { it.id == colorTheme }
                ?: themes.first { it.materialColors.toComposeColorScheme().isLight() == fallbackIsLight }
        }

        return when (themeMode) {
            PreferenceValues.ThemeMode.System -> if (!isSystemInDarkTheme()) {
                getTheme(true)
            } else {
                getTheme(false)
            }
            PreferenceValues.ThemeMode.Light -> getTheme(true)
            PreferenceValues.ThemeMode.Dark -> getTheme(false)
        }
    }

    /**
     * Creates ExtraColors with proper bar colors and onBar text colors.
     * Note: No remember() used here to ensure immediate updates when theme changes.
     */
    private fun createExtraColors(
        baseExtraColors: ExtraColors,
        customBarsColor: Color,
        materialColors: ColorScheme,
        isLight: Boolean,
        useTrueBlack: Boolean
    ): ExtraColors {
        val baseBarsColor = baseExtraColors.bars.toComposeColor()
        
        // Determine the bars color: custom > base > surface
        val barsColor = when {
            customBarsColor != Color.Unspecified -> customBarsColor
            baseBarsColor != Color.Unspecified -> baseBarsColor
            else -> materialColors.surface
        }
        
        // Apply true black to bars if enabled for dark themes
        val finalBarsColor = if (!isLight && useTrueBlack) {
            Color.Black
        } else {
            barsColor
        }
        
        // Calculate proper onBars color based on bars luminance
        val onBarsColor = ThemeColorUtils.getOnColor(finalBarsColor)
        
        return ExtraColors(
            bars = finalBarsColor.toDomainColor(),
            onBars = onBarsColor.toDomainColor()
        )
    }


    var locales by mutableStateOf(listOf<Locale>())
        private set

    @Composable
    private fun ColorScheme.lerpTo(other: ColorScheme, fraction: Float): ColorScheme = ColorScheme(
        primary = lerp(primary, other.primary, fraction),
        onPrimary = lerp(onPrimary, other.onPrimary, fraction),
        primaryContainer = lerp(primaryContainer, other.primaryContainer, fraction),
        onPrimaryContainer = lerp(onPrimaryContainer, other.onPrimaryContainer, fraction),
        inversePrimary = lerp(inversePrimary, other.inversePrimary, fraction),
        secondary = lerp(secondary, other.secondary, fraction),
        onSecondary = lerp(onSecondary, other.onSecondary, fraction),
        secondaryContainer = lerp(secondaryContainer, other.secondaryContainer, fraction),
        onSecondaryContainer = lerp(onSecondaryContainer, other.onSecondaryContainer, fraction),
        tertiary = lerp(tertiary, other.tertiary, fraction),
        onTertiary = lerp(onTertiary, other.onTertiary, fraction),
        tertiaryContainer = lerp(tertiaryContainer, other.tertiaryContainer, fraction),
        onTertiaryContainer = lerp(onTertiaryContainer, other.onTertiaryContainer, fraction),
        background = lerp(background, other.background, fraction),
        onBackground = lerp(onBackground, other.onBackground, fraction),
        surface = lerp(surface, other.surface, fraction),
        onSurface = lerp(onSurface, other.onSurface, fraction),
        surfaceVariant = lerp(surfaceVariant, other.surfaceVariant, fraction),
        onSurfaceVariant = lerp(onSurfaceVariant, other.onSurfaceVariant, fraction),
        surfaceTint = lerp(surfaceTint, other.surfaceTint, fraction),
        inverseSurface = lerp(inverseSurface, other.inverseSurface, fraction),
        inverseOnSurface = lerp(inverseOnSurface, other.inverseOnSurface, fraction),
        error = lerp(error, other.error, fraction),
        onError = lerp(onError, other.onError, fraction),
        errorContainer = lerp(errorContainer, other.errorContainer, fraction),
        onErrorContainer = lerp(onErrorContainer, other.onErrorContainer, fraction),
        outline = lerp(outline, other.outline, fraction),
        outlineVariant = lerp(outlineVariant, other.outlineVariant, fraction),
        scrim = lerp(scrim, other.scrim, fraction)
    )

    @Composable
    fun getTypography(): Typography {
        val appUiFont = appUiFontState.value
        val fontFamily = getAppUiFontFamily(appUiFont)
        return Typography(
            displayLarge = AppTypography.displayLarge.copy(fontFamily = fontFamily),
            displayMedium = AppTypography.displayMedium.copy(fontFamily = fontFamily),
            displaySmall = AppTypography.displaySmall.copy(fontFamily = fontFamily),
            headlineLarge = AppTypography.headlineLarge.copy(fontFamily = fontFamily),
            headlineMedium = AppTypography.headlineMedium.copy(fontFamily = fontFamily),
            headlineSmall = AppTypography.headlineSmall.copy(fontFamily = fontFamily),
            titleLarge = AppTypography.titleLarge.copy(fontFamily = fontFamily),
            titleMedium = AppTypography.titleMedium.copy(fontFamily = fontFamily),
            titleSmall = AppTypography.titleSmall.copy(fontFamily = fontFamily),
            labelLarge = AppTypography.labelLarge.copy(fontFamily = fontFamily),
            bodyLarge = AppTypography.bodyLarge.copy(fontFamily = fontFamily),
            bodyMedium = AppTypography.bodyMedium.copy(fontFamily = fontFamily),
            bodySmall = AppTypography.bodySmall.copy(fontFamily = fontFamily),
            labelMedium = AppTypography.labelMedium.copy(fontFamily = fontFamily),
            labelSmall = AppTypography.labelSmall.copy(fontFamily = fontFamily),
        )
    }

}


