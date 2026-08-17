package ireader.presentation.core.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.graphics.Color
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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
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
    private val coverBasedThemeStyleState = uiPreferences.coverBasedThemeStyle().asState()
    private val coverBasedThemeSaturationState = uiPreferences.coverBasedThemeSaturation().asState()
    private val coverBasedThemeIntensityState = uiPreferences.coverBasedThemeIntensity().asState()
    private val coverBasedThemeTextColorModeState = uiPreferences.coverBasedThemeTextColorMode().asState()
    private val coverBasedThemeForLibraryState = uiPreferences.coverBasedThemeForLibrary().asState()
    private val coverBasedThemeSourceState = uiPreferences.coverBasedThemeSource().asState()
    private val coverBasedThemeSurfaceTintingState = uiPreferences.coverBasedThemeSurfaceTinting().asState()
    private val coverBasedThemeContrastState = uiPreferences.coverBasedThemeContrast().asState()
    private val coverBasedThemePresetState = uiPreferences.coverBasedThemePreset().asState()
    private val coverBasedThemeBrightnessState = uiPreferences.coverBasedThemeBrightness().asState()
    private val coverBasedThemeBackgroundTintOpacityState = uiPreferences.coverBasedThemeBackgroundTintOpacity().asState()
    private val coverBasedThemeBackdropBlurState = uiPreferences.coverBasedThemeBackdropBlur().asState()
    private val useTrueBlackState = uiPreferences.useTrueBlack().asState()
    private val appUiFontState = uiPreferences.appUiFont().asState()
    
    // Track last applied cover URL so we can re-apply when style changes
    private var lastAppliedCoverUrl: String? = null
    private var lastAppliedIsDark: Boolean = false
    
    // Expose cover-based ColorScheme for local MaterialTheme overrides
    val coverBasedColorScheme: Flow<ColorScheme?> = coverBasedThemeManager?.coverBasedTheme?.map { scheme ->
        scheme?.toComposeColorScheme()
    } ?: kotlinx.coroutines.flow.flowOf(null)
    
    // Pre-create color states for both light and dark modes
    // This ensures we always have reactive state objects ready
    private val lightColorsState = uiPreferences.getLightColors().asState(scope)
    private val darkColorsState = uiPreferences.getDarkColors().asState(scope)

    init {
        themeRepository.subscribe().onEach {
            themes.removeIf { baseTheme -> baseTheme.id > 0L }
            themes.addAll(it)
        }.launchIn(scope)
        
        // Re-apply cover-based theme when style changes, if we have a cached cover URL
        coverBasedThemeStyleState.value
        uiPreferences.coverBasedThemeStyle().changes().onEach { newStyle ->
            val currentUrl = lastAppliedCoverUrl
            if (currentUrl != null && coverBasedThemeEnabledState.value) {
                val resolvedIsDark = when (uiPreferences.themeMode().get()) {
                    PreferenceValues.ThemeMode.Light -> false
                    PreferenceValues.ThemeMode.Dark -> true
                    else -> lastAppliedIsDark
                }
                coverBasedThemeManager?.applyCoverBasedTheme(
                    currentUrl, null, coverBasedThemeStyleState.value, resolvedIsDark,
                    coverBasedThemeSaturationState.value / 5.0f,
                    coverBasedThemeIntensityState.value / 10.0f + 0.5f,
                    coverBasedThemeBrightnessState.value / 10.0f + 0.5f,
                    coverBasedThemeTextColorModeState.value,
                    coverBasedThemeContrastState.value,
                    coverBasedThemeSurfaceTintingState.value,
                    coverBasedThemeBackgroundTintOpacityState.value / 10.0f
                )
            }
        }.launchIn(scope)
        
        // Re-apply cover-based theme when contrast changes
        coverBasedThemeContrastState.value
        uiPreferences.coverBasedThemeContrast().changes().onEach { newContrast ->
            val currentUrl = lastAppliedCoverUrl
            if (currentUrl != null && coverBasedThemeEnabledState.value) {
                val resolvedIsDark = when (uiPreferences.themeMode().get()) {
                    PreferenceValues.ThemeMode.Light -> false
                    PreferenceValues.ThemeMode.Dark -> true
                    else -> lastAppliedIsDark
                }
                coverBasedThemeManager?.applyCoverBasedTheme(
                    currentUrl, null, coverBasedThemeStyleState.value, resolvedIsDark,
                    coverBasedThemeSaturationState.value / 5.0f,
                    coverBasedThemeIntensityState.value / 10.0f + 0.5f,
                    coverBasedThemeBrightnessState.value / 10.0f + 0.5f,
                    coverBasedThemeTextColorModeState.value,
                    newContrast,
                    coverBasedThemeSurfaceTintingState.value,
                    coverBasedThemeBackgroundTintOpacityState.value / 10.0f
                )
            }
        }.launchIn(scope)
        
        // Re-apply cover-based theme when preset changes
        coverBasedThemePresetState.value
        uiPreferences.coverBasedThemePreset().changes().onEach { preset ->
            val currentUrl = lastAppliedCoverUrl
            if (currentUrl != null && coverBasedThemeEnabledState.value) {
                val resolvedIsDark = when (uiPreferences.themeMode().get()) {
                    PreferenceValues.ThemeMode.Light -> false
                    PreferenceValues.ThemeMode.Dark -> true
                    else -> lastAppliedIsDark
                }
                val presetSaturation: Float
                val presetIntensity: Float
                val presetBrightness: Float
                val presetBackgroundTintOpacity: Float
                when (preset) {
                    PreferenceValues.CoverBasedThemePreset.Off -> {
                        presetSaturation = 0f
                        presetIntensity = 0f
                        presetBrightness = 0f
                        presetBackgroundTintOpacity = 0f
                    }
                    PreferenceValues.CoverBasedThemePreset.Soft -> {
                        presetSaturation = 3f
                        presetIntensity = 3f
                        presetBrightness = 3f
                        presetBackgroundTintOpacity = 2f
                    }
                    PreferenceValues.CoverBasedThemePreset.Medium -> {
                        presetSaturation = 5f
                        presetIntensity = 5f
                        presetBrightness = 5f
                        presetBackgroundTintOpacity = 3f
                    }
                    PreferenceValues.CoverBasedThemePreset.High -> {
                        presetSaturation = 8f
                        presetIntensity = 8f
                        presetBrightness = 8f
                        presetBackgroundTintOpacity = 5f
                    }
                }
                uiPreferences.coverBasedThemeSaturation().set(presetSaturation)
                uiPreferences.coverBasedThemeIntensity().set(presetIntensity)
                uiPreferences.coverBasedThemeBrightness().set(presetBrightness)
                uiPreferences.coverBasedThemeBackgroundTintOpacity().set(presetBackgroundTintOpacity)
                coverBasedThemeManager?.applyCoverBasedTheme(
                    currentUrl, null, coverBasedThemeStyleState.value, resolvedIsDark,
                    presetSaturation / 5.0f,
                    presetIntensity / 10.0f + 0.5f,
                    presetBrightness / 10.0f + 0.5f,
                    coverBasedThemeTextColorModeState.value,
                    coverBasedThemeContrastState.value,
                    coverBasedThemeSurfaceTintingState.value,
                    presetBackgroundTintOpacity / 10.0f
                )
            }
        }.launchIn(scope)
        
        // Re-apply cover-based theme when saturation changes
        coverBasedThemeSaturationState.value
        uiPreferences.coverBasedThemeSaturation().changes().onEach { newSaturation ->
            val currentUrl = lastAppliedCoverUrl
            if (currentUrl != null && coverBasedThemeEnabledState.value) {
                val resolvedIsDark = when (uiPreferences.themeMode().get()) {
                    PreferenceValues.ThemeMode.Light -> false
                    PreferenceValues.ThemeMode.Dark -> true
                    else -> lastAppliedIsDark
                }
                coverBasedThemeManager?.applyCoverBasedTheme(
                    currentUrl, null, coverBasedThemeStyleState.value, resolvedIsDark,
                    newSaturation / 5.0f,
                    coverBasedThemeIntensityState.value / 10.0f + 0.5f,
                    coverBasedThemeBrightnessState.value / 10.0f + 0.5f,
                    coverBasedThemeTextColorModeState.value,
                    coverBasedThemeContrastState.value,
                    coverBasedThemeSurfaceTintingState.value,
                    coverBasedThemeBackgroundTintOpacityState.value / 10.0f
                )
            }
        }.launchIn(scope)
        
        // Re-apply cover-based theme when intensity changes
        coverBasedThemeIntensityState.value
        uiPreferences.coverBasedThemeIntensity().changes().onEach { newIntensity ->
            val currentUrl = lastAppliedCoverUrl
            if (currentUrl != null && coverBasedThemeEnabledState.value) {
                val resolvedIsDark = when (uiPreferences.themeMode().get()) {
                    PreferenceValues.ThemeMode.Light -> false
                    PreferenceValues.ThemeMode.Dark -> true
                    else -> lastAppliedIsDark
                }
                coverBasedThemeManager?.applyCoverBasedTheme(
                    currentUrl, null, coverBasedThemeStyleState.value, resolvedIsDark,
                    coverBasedThemeSaturationState.value / 5.0f,
                    newIntensity / 10.0f + 0.5f,
                    coverBasedThemeBrightnessState.value / 10.0f + 0.5f,
                    coverBasedThemeTextColorModeState.value,
                    coverBasedThemeContrastState.value,
                    coverBasedThemeSurfaceTintingState.value,
                    coverBasedThemeBackgroundTintOpacityState.value / 10.0f
                )
            }
        }.launchIn(scope)
        
        // Re-apply cover-based theme when text color mode changes
        coverBasedThemeTextColorModeState.value
        uiPreferences.coverBasedThemeTextColorMode().changes().onEach { newTextColorMode ->
            val currentUrl = lastAppliedCoverUrl
            if (currentUrl != null && coverBasedThemeEnabledState.value) {
                val resolvedIsDark = when (uiPreferences.themeMode().get()) {
                    PreferenceValues.ThemeMode.Light -> false
                    PreferenceValues.ThemeMode.Dark -> true
                    else -> lastAppliedIsDark
                }
            coverBasedThemeManager?.applyCoverBasedTheme(
                currentUrl, null, coverBasedThemeStyleState.value, resolvedIsDark,
                coverBasedThemeSaturationState.value / 5.0f,
                coverBasedThemeIntensityState.value / 10.0f + 0.5f,
                coverBasedThemeBrightnessState.value / 10.0f + 0.5f,
                newTextColorMode,
                coverBasedThemeContrastState.value,
                coverBasedThemeSurfaceTintingState.value,
                coverBasedThemeBackgroundTintOpacityState.value / 10.0f
            )
            }
        }.launchIn(scope)
        
        // Re-apply cover-based theme when source changes
        coverBasedThemeSourceState.value
        uiPreferences.coverBasedThemeSource().changes().onEach { newSource ->
            val currentUrl = lastAppliedCoverUrl
            if (currentUrl != null && coverBasedThemeEnabledState.value) {
                val resolvedIsDark = when (uiPreferences.themeMode().get()) {
                    PreferenceValues.ThemeMode.Light -> false
                    PreferenceValues.ThemeMode.Dark -> true
                    else -> lastAppliedIsDark
                }
                coverBasedThemeManager?.applyCoverBasedTheme(
                    currentUrl, null, coverBasedThemeStyleState.value, resolvedIsDark,
                    coverBasedThemeSaturationState.value / 5.0f,
                    coverBasedThemeIntensityState.value / 10.0f + 0.5f,
                    coverBasedThemeBrightnessState.value / 10.0f + 0.5f,
                    coverBasedThemeTextColorModeState.value,
                    coverBasedThemeContrastState.value,
                    coverBasedThemeSurfaceTintingState.value,
                    coverBasedThemeBackgroundTintOpacityState.value / 10.0f
                )
            }
        }.launchIn(scope)
        
        // Re-apply cover-based theme when contrast changes
        coverBasedThemeContrastState.value
        uiPreferences.coverBasedThemeContrast().changes().onEach { newContrast ->
            val currentUrl = lastAppliedCoverUrl
            if (currentUrl != null && coverBasedThemeEnabledState.value) {
                val resolvedIsDark = when (uiPreferences.themeMode().get()) {
                    PreferenceValues.ThemeMode.Light -> false
                    PreferenceValues.ThemeMode.Dark -> true
                    else -> lastAppliedIsDark
                }
                coverBasedThemeManager?.applyCoverBasedTheme(
                    currentUrl, null, coverBasedThemeStyleState.value, resolvedIsDark,
                    coverBasedThemeSaturationState.value / 5.0f,
                    coverBasedThemeIntensityState.value / 10.0f + 0.5f,
                    coverBasedThemeBrightnessState.value / 10.0f + 0.5f,
                    coverBasedThemeTextColorModeState.value,
                    newContrast,
                    coverBasedThemeSurfaceTintingState.value,
                    coverBasedThemeBackgroundTintOpacityState.value / 10.0f
                )
            }
        }.launchIn(scope)
        
        // Re-apply cover-based theme when brightness changes
        coverBasedThemeBrightnessState.value
        uiPreferences.coverBasedThemeBrightness().changes().onEach { newBrightness ->
            val currentUrl = lastAppliedCoverUrl
            if (currentUrl != null && coverBasedThemeEnabledState.value) {
                val resolvedIsDark = when (uiPreferences.themeMode().get()) {
                    PreferenceValues.ThemeMode.Light -> false
                    PreferenceValues.ThemeMode.Dark -> true
                    else -> lastAppliedIsDark
                }
                coverBasedThemeManager?.applyCoverBasedTheme(
                    currentUrl, null, coverBasedThemeStyleState.value, resolvedIsDark,
                    coverBasedThemeSaturationState.value / 5.0f,
                    coverBasedThemeIntensityState.value / 10.0f + 0.5f,
                    newBrightness / 10.0f + 0.5f,
                    coverBasedThemeTextColorModeState.value,
                    coverBasedThemeContrastState.value,
                    coverBasedThemeSurfaceTintingState.value,
                    coverBasedThemeBackgroundTintOpacityState.value / 10.0f
                )
            }
        }.launchIn(scope)
        
        // Re-apply cover-based theme when background tint opacity changes
        coverBasedThemeBackgroundTintOpacityState.value
        uiPreferences.coverBasedThemeBackgroundTintOpacity().changes().onEach { newOpacity ->
            val currentUrl = lastAppliedCoverUrl
            if (currentUrl != null && coverBasedThemeEnabledState.value) {
                val resolvedIsDark = when (uiPreferences.themeMode().get()) {
                    PreferenceValues.ThemeMode.Light -> false
                    PreferenceValues.ThemeMode.Dark -> true
                    else -> lastAppliedIsDark
                }
                coverBasedThemeManager?.applyCoverBasedTheme(
                    currentUrl, null, coverBasedThemeStyleState.value, resolvedIsDark,
                    coverBasedThemeSaturationState.value / 5.0f,
                    coverBasedThemeIntensityState.value / 10.0f + 0.5f,
                    coverBasedThemeBrightnessState.value / 10.0f + 0.5f,
                    coverBasedThemeTextColorModeState.value,
                    coverBasedThemeContrastState.value,
                    coverBasedThemeSurfaceTintingState.value,
                    newOpacity / 10.0f
                )
            }
        }.launchIn(scope)
        
        // Re-apply cover-based theme when backdrop blur changes
        coverBasedThemeBackdropBlurState.value
        uiPreferences.coverBasedThemeBackdropBlur().changes().onEach { newBlur ->
            val currentUrl = lastAppliedCoverUrl
            if (currentUrl != null && coverBasedThemeEnabledState.value) {
                val resolvedIsDark = when (uiPreferences.themeMode().get()) {
                    PreferenceValues.ThemeMode.Light -> false
                    PreferenceValues.ThemeMode.Dark -> true
                    else -> lastAppliedIsDark
                }
                coverBasedThemeManager?.applyCoverBasedTheme(
                    currentUrl, null, coverBasedThemeStyleState.value, resolvedIsDark,
                    coverBasedThemeSaturationState.value / 5.0f,
                    coverBasedThemeIntensityState.value / 10.0f + 0.5f,
                    coverBasedThemeBrightnessState.value / 10.0f + 0.5f,
                    coverBasedThemeTextColorModeState.value,
                    coverBasedThemeContrastState.value,
                    coverBasedThemeSurfaceTintingState.value,
                    coverBasedThemeBackgroundTintOpacityState.value / 10.0f
                )
            }
        }.launchIn(scope)
    }


    @Composable
    fun getColors(): Pair<ColorScheme, ExtraColors> {
        // Read state values inside composable to trigger recomposition when they change
        val themeMode = themeModeState.value
        val colorTheme = colorThemeState.value
        val dynamicColorMode = dynamicColorModeState.value
        val coverBasedThemeEnabled = coverBasedThemeEnabledState.value
        val coverBasedThemeStyle = coverBasedThemeStyleState.value
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
        
        // Step 1b: Apply cover-based theme if enabled
        if (coverBasedThemeEnabled && coverBasedThemeManager != null) {
            val coverScheme = coverBasedThemeManager.coverBasedTheme.collectAsState(initial = null).value
            if (coverScheme != null) {
                materialColors = coverScheme.toComposeColorScheme()
            }
        }
         
        // Step 2: Apply custom primary/secondary colors if specified
        val customPrimary = customPrimaryColor.takeIf { it != Color.Unspecified }
        val customSecondary = customSecondaryColor.takeIf { it != Color.Unspecified }
        
        if (customPrimary != null || customSecondary != null) {
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
    
    fun setCurrentCoverUrl(coverUrl: String?, sourceId: Long?, isDark: Boolean) {
        if (coverBasedThemeManager == null) return
        lastAppliedCoverUrl = coverUrl
        lastAppliedIsDark = isDark
        val coverBasedThemeEnabled = coverBasedThemeEnabledState.value
        if (!coverBasedThemeEnabled) {
            coverBasedThemeManager.clearTheme()
            return
        }
        if (coverUrl == null || coverUrl.isBlank()) {
            return
        }
        val coverBasedThemeStyle = coverBasedThemeStyleState.value
        val saturation = coverBasedThemeSaturationState.value / 5.0f
        val intensity = coverBasedThemeIntensityState.value / 10.0f + 0.5f
        val brightness = coverBasedThemeBrightnessState.value / 10.0f + 0.5f
        val textColorMode = coverBasedThemeTextColorModeState.value
        val resolvedIsDark = when (uiPreferences.themeMode().get()) {
            PreferenceValues.ThemeMode.Light -> false
            PreferenceValues.ThemeMode.Dark -> true
            else -> isDark
        }
        coverBasedThemeManager.applyCoverBasedTheme(coverUrl, sourceId, coverBasedThemeStyle, resolvedIsDark, saturation, intensity, brightness, textColorMode, coverBasedThemeContrastState.value, coverBasedThemeSurfaceTintingState.value, coverBasedThemeBackgroundTintOpacityState.value / 10.0f)
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


