package ireader.presentation.core.theme

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.ripple.RippleTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.text.intl.Locale
import androidx.lifecycle.viewModelScope
import ireader.domain.data.repository.ThemeRepository
import ireader.domain.models.prefs.PreferenceValues
import ireader.domain.models.theme.ExtraColors
import ireader.domain.models.theme.Theme
import ireader.domain.preferences.prefs.UiPreferences
import ireader.presentation.ui.core.theme.*
import ireader.presentation.ui.core.viewmodel.BaseViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.android.annotation.KoinViewModel
import org.koin.core.annotation.Single

@KoinViewModel
class AppThemeViewModel(
    private val uiPreferences: UiPreferences,
    private val themeRepository: ThemeRepository,
) : ireader.presentation.ui.core.viewmodel.BaseViewModel() {
    private val themeMode by uiPreferences.themeMode().asState()
    private val colorTheme by uiPreferences.colorTheme().asState()

    private val baseThemeJob = SupervisorJob()
    private val baseThemeScope = CoroutineScope(baseThemeJob)

    init {
        themeRepository.subscribe().onEach {
            themes.removeIf { baseTheme -> baseTheme.id > 0L }
            themes.addAll(it)
        }.launchIn(viewModelScope)
    }

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

        val material = getMaterialColors(
            baseTheme.materialColors,
            colors.primary.value,
            colors.secondary.value
        )
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


    var locales by mutableStateOf(listOf<Locale>())
        private set


}

@Single
class LocaleHelper(
    val context: Context,
    val uiPreferences: UiPreferences
) {
    val language = uiPreferences.language()

    val languages = mutableListOf<String>()

    init {
        getLocales()
    }


    fun setLocaleLang(context: Context) {
        val lang = language.get()
        val locale = java.util.Locale(lang)
        java.util.Locale.setDefault(locale)
        val resources = context.resources
        val configuration = resources.configuration
        configuration.setLocale(locale)
        resources.updateConfiguration(configuration, resources.displayMetrics)
    }


    fun updateLocal() {
        context.resources.apply {
            val locale = java.util.Locale(language.get())
            val config = Configuration(configuration)

            context.createConfigurationContext(configuration)
            java.util.Locale.setDefault(locale)
            config.setLocale(locale)
            context.resources.updateConfiguration(config, displayMetrics)
        }
    }

    fun resetLocale() {
        context.resources.apply {
            val config = Configuration(configuration)
            val default = this.configuration.locale
            config.setLocale(default)
            context.resources.updateConfiguration(config, displayMetrics)
        }
    }


    private fun getLocales() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val locales = context.resources.assets.locales
            for (i in locales.indices) {
                languages.add(locales[i])
            }
        }
    }
}
