package org.ireader.presentation.ui


import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.Colors
import androidx.compose.material.ripple.RippleTheme
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
import org.ireader.core_ui.theme.*
import org.ireader.core_ui.viewmodel.BaseViewModel
import org.ireader.domain.feature_service.io.coil.CoilLoaderFactory
import javax.inject.Inject

@HiltViewModel
class AppThemeViewModel @Inject constructor(
    private val uiPreferences: UiPreferences,
    coilLoaderFactory: CoilLoaderFactory,
) : BaseViewModel() {
    private val themeMode by uiPreferences.themeMode().asState()
    private val lightTheme by uiPreferences.lightTheme().asState()
    private val darkTheme by uiPreferences.darkTheme().asState()

    private val baseThemeJob = SupervisorJob()
    private val baseThemeScope = CoroutineScope(baseThemeJob)
    val coilLoader = coilLoaderFactory.newImageLoader()


    @Composable
    fun getRippleTheme(): RippleTheme {
        return when (themeMode) {
            ThemeMode.System -> AppRippleTheme(!isSystemInDarkTheme())
            ThemeMode.Light -> AppRippleTheme(true)
            ThemeMode.Dark -> AppRippleTheme(false)
        }
    }
    @Composable
    fun getColors(): Pair<Colors, ExtraColors> {
        val baseTheme = getBaseTheme(themeMode, lightTheme, darkTheme)
        val colors = remember(baseTheme.materialColors.isLight) {
            baseThemeJob.cancelChildren()

            if (baseTheme.materialColors.isLight) {
                uiPreferences.getLightColors().asState(baseThemeScope)
            } else {
                uiPreferences.getDarkColors().asState(baseThemeScope)
            }
        }

        val material = getMaterialColors(baseTheme.materialColors, colors.primary, colors.secondary)
        val custom = getExtraColors(baseTheme.extraColors, colors.bars)
        return material to custom
    }

    @Composable
    private fun getBaseTheme(
        themeMode: ThemeMode,
        lightTheme: Int,
        darkTheme: Int,
    ): Theme {
        fun getTheme(id: Int, fallbackIsLight: Boolean): Theme {
            return themes.find { it.id == id }
                ?: themes.first { it.materialColors.isLight == fallbackIsLight }
        }

        return when (themeMode) {
            ThemeMode.System -> if (!isSystemInDarkTheme()) {
                getTheme(lightTheme, true)
            } else {
                getTheme(darkTheme, false)
            }
            ThemeMode.Light -> getTheme(lightTheme, true)
            ThemeMode.Dark -> getTheme(darkTheme, false)
        }
    }

    private fun getMaterialColors(
        baseColors: Colors,
        colorPrimary: Color,
        colorSecondary: Color,
    ): Colors {
        val primary = colorPrimary.takeOrElse { baseColors.primary }
        val secondary = colorSecondary.takeOrElse { baseColors.secondary }
        return baseColors.copy(
            primary = primary,
            primaryVariant = primary,
            secondary = secondary,
            secondaryVariant = secondary,
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
