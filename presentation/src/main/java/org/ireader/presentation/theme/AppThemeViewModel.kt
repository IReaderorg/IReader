package org.ireader.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.ripple.RippleTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.takeOrElse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelChildren
import org.ireader.common_data.repository.ThemeRepository
import org.ireader.common_models.theme.ExtraColors
import org.ireader.common_models.theme.Theme
import org.ireader.core_ui.preferences.PreferenceValues
import org.ireader.core_ui.preferences.UiPreferences
import org.ireader.core_ui.theme.AppRippleTheme
import org.ireader.core_ui.theme.asState
import org.ireader.core_ui.theme.dark
import org.ireader.core_ui.theme.getDarkColors
import org.ireader.core_ui.theme.getLightColors
import org.ireader.core_ui.theme.isLight
import org.ireader.core_ui.theme.light
import org.ireader.core_ui.theme.themes
import org.ireader.core_ui.viewmodel.BaseViewModel
import javax.inject.Inject

@HiltViewModel
class AppThemeViewModel @Inject constructor(
    private val uiPreferences: UiPreferences,
    private val themeRepository:ThemeRepository,
    coilLoaderFactory: org.ireader.image_loader.coil.CoilLoaderFactory,
) : BaseViewModel() {
    private val themeMode by uiPreferences.themeMode().asState()
    private val colorTheme by uiPreferences.colorTheme().asState()


    private val baseThemeJob = SupervisorJob()
    private val baseThemeScope = CoroutineScope(baseThemeJob)

  //  val themes by themeRepository.subscribe().asState(emptyList())


    @Composable
    fun getRippleTheme(): RippleTheme {
        return when (themeMode) {
            PreferenceValues.ThemeMode.System -> AppRippleTheme(!isSystemInDarkTheme())
            PreferenceValues.ThemeMode.Light -> AppRippleTheme(true)
            PreferenceValues.ThemeMode.Dark -> AppRippleTheme(false)
        }
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

        val material = getMaterialColors(baseTheme.materialColors, colors.primary.value, colors.secondary.value)
        val custom = getExtraColors(baseTheme.extraColors, colors.bars.value)
        return material to custom
    }

    @Composable
    private fun getBaseTheme(
        themeMode: PreferenceValues.ThemeMode,
        colorTheme: Long,
    ): Theme {
        @Composable
        fun getTheme(fallbackIsLight: Boolean): Theme {

            return themes.firstOrNull { it.id == colorTheme }?.let {
                if (fallbackIsLight) {
                    it.light()
                } else {
                    it.dark()
                }
            } ?: if (fallbackIsLight) themes.first().light() else themes.first().dark()
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
    ): ColorScheme {
        val primary = colorPrimary.takeOrElse { baseColors.primary }
        val secondary = colorSecondary.takeOrElse { baseColors.secondary }
        return baseColors.copy(
            primary = primary,
            primaryContainer = primary,
            secondary = secondary,
            secondaryContainer = secondary,
            onPrimary = if (primary.luminance() > 0.5) Color.Black else Color.White,
            onSecondary = if (secondary.luminance() > 0.5) Color.Black else Color.White,
        )
    }

    private fun getExtraColors(colors: ExtraColors, colorBars: Color): ExtraColors {
        val appbar = colorBars.takeOrElse { colors.bars }
        return ExtraColors(
            bars = appbar,
            onBars = if (appbar.luminance() > 0.5) Color.Black else Color.White
        )
    }

    override fun onDestroy() {
        baseThemeScope.cancel()
    }
}
