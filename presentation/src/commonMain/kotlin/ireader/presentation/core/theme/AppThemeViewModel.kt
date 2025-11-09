package ireader.presentation.core.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.text.intl.Locale
import ireader.core.prefs.Preference
import ireader.domain.data.repository.ThemeRepository
import ireader.domain.models.prefs.PreferenceValues
import ireader.domain.models.theme.ExtraColors
import ireader.domain.models.theme.Theme
import ireader.domain.preferences.prefs.UiPreferences
import ireader.presentation.ui.core.theme.AppTypography
import ireader.presentation.ui.core.theme.asState
import ireader.presentation.ui.core.theme.getAppUiFontFamily
import ireader.presentation.ui.core.theme.getDarkColors
import ireader.presentation.ui.core.theme.getLightColors
import ireader.presentation.ui.core.theme.isLight
import ireader.presentation.ui.core.theme.themes
import ireader.presentation.ui.core.ui.PreferenceMutableState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach


class AppThemeViewModel(
    private val uiPreferences: UiPreferences,
    private val themeRepository: ThemeRepository,
    private val dynamicColorScheme: DynamicColorScheme,
    val scope: CoroutineScope
) {
    fun <T> Preference<T>.asState() = PreferenceMutableState(this, scope)

    private val themeMode by uiPreferences.themeMode().asState()
    private val colorTheme by uiPreferences.colorTheme().asState()
    private val dynamicColorMode by uiPreferences.dynamicColorMode().asState()
    private val useTrueBlack by uiPreferences.useTrueBlack().asState()
    private val appUiFont by uiPreferences.appUiFont().asState()

    private val baseThemeJob = SupervisorJob()
    private val baseThemeScope = CoroutineScope(baseThemeJob)

    init {
        themeRepository.subscribe().onEach {
            themes.removeIf { baseTheme -> baseTheme.id > 0L }
            themes.addAll(it)
        }.launchIn(scope)
    }


    @Composable
    fun getColors(): Pair<ColorScheme, ExtraColors> {
        val baseTheme = getBaseTheme(themeMode, colorTheme)
        val isLight = baseTheme.materialColors.isLight()
        
        val colors = remember(baseTheme.materialColors.isLight()) {
            baseThemeJob.cancelChildren()
            if (isLight) {
                uiPreferences.getLightColors().asState(baseThemeScope)
            } else {
                uiPreferences.getDarkColors().asState(baseThemeScope)
            }
        }

        // Check if dynamic colors should be used
        val useDynamicColors = dynamicColorMode
            && dynamicColorScheme.isSupported()

        val material = if (useDynamicColors) {
            // Use dynamic color scheme from system
            try {
                val dynamicScheme = if (isLight) {
                    dynamicColorScheme.lightColorScheme()
                } else {
                    dynamicColorScheme.darkColorScheme()
                }
                dynamicScheme ?: getMaterialColors(
                    baseTheme.materialColors,
                    colors.primary.value,
                    colors.secondary.value,
                    isLight
                )
            } catch (e: Exception) {
                // Fallback to regular colors if dynamic colors fail
                getMaterialColors(
                    baseTheme.materialColors,
                    colors.primary.value,
                    colors.secondary.value,
                    isLight
                )
            }
        } else {
            getMaterialColors(
                baseTheme.materialColors,
                colors.primary.value,
                colors.secondary.value,
                isLight
            )
        }
        
        val custom = getExtraColors(baseTheme.extraColors, colors.bars.value, isLight)
        return material to custom
    }

    @Composable
    private fun getBaseTheme(
        themeMode: PreferenceValues.ThemeMode,
        colorTheme: Long,
    ): Theme {
        @Composable
        fun getTheme(fallbackIsLight: Boolean): Theme {
            return themes.firstOrNull { it.id == colorTheme }
                ?: themes.first { it.materialColors.isLight() == fallbackIsLight }
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

    private fun getMaterialColors(
        baseColors: ColorScheme,
        colorPrimary: Color,
        colorSecondary: Color,
        isLight: Boolean
    ): ColorScheme {
        val primary = colorPrimary.takeOrElse { baseColors.primary }
        val secondary = colorSecondary.takeOrElse { baseColors.secondary }
        
        // Apply true black for dark themes when enabled
        val background = if (!isLight && useTrueBlack) Color.Black else baseColors.background
        val surface = if (!isLight && useTrueBlack) Color.Black else baseColors.surface
        val surfaceVariant = if (!isLight && useTrueBlack) Color(0xFF0A0A0A) else baseColors.surfaceVariant
        
        return baseColors.copy(
            primary = primary,
            primaryContainer = primary,
            secondary = secondary,
            secondaryContainer = secondary,
            onPrimary = if (primary.luminance() > 0.5) Color.Black else Color.White,
            onSecondary = if (secondary.luminance() > 0.5) Color.Black else Color.White,
            background = background,
            surface = surface,
            surfaceVariant = surfaceVariant,
        )
    }

    private fun getExtraColors(colors: ExtraColors, colorBars: Color, isLight: Boolean): ExtraColors {
        val appbar = kotlin.runCatching {
         colorBars.takeOrElse { colors.bars }

        }.getOrNull() ?:  Color.White
        
        // Apply true black to bars for dark themes when enabled
        val barsColor = if (!isLight && useTrueBlack) Color.Black else appbar
        
        return ExtraColors(
            bars = barsColor,
            onBars = if (barsColor.isLight()) Color.Black else Color.White
        )
    }


    var locales by mutableStateOf(listOf<Locale>())
        private set

    @Composable
    fun getTypography(): Typography {
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


