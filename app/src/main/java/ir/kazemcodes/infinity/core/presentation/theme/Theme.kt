package ir.kazemcodes.infinity.core.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import ir.kazemcodes.infinity.core.domain.use_cases.preferences.apperance.NightMode
import ir.kazemcodes.infinity.core.domain.use_cases.preferences.reader_preferences.PreferencesUseCase
import org.koin.androidx.compose.get

private val DarkColorPalette = darkColors(
    primary = Colour.blue_200,
    primaryVariant = Colour.blue_600,
    onPrimary = Colour.black_900,
    secondary = Colour.light_blue_a_200,
    onSecondary = Colour.black_900,
    background = Colour.black_900,
    surface = Colour.white_50,
    onBackground = Colour.white_50,
    onSurface = Colour.white_50,
    error = Colour.red_600,
    onError = Colour.black_900,
)

private val LightColorPalette = lightColors(
    primary = Colour.blue_500,
    primaryVariant = Colour.blue_700,
    onPrimary = Colour.white_50,
    secondary = Colour.light_blue_a_200,
    onSecondary = Colour.black_900,
    background = Colour.white_50,
    surface = Colour.white_50,
    onBackground = Colour.black_900,
    onSurface = Colour.black_900,
    error = Colour.red_600,
    onError = Colour.white_50,
)

@Composable
fun InfinityTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable() () -> Unit) {

    val pref = get<PreferencesUseCase>()

    val state = pref.readNightModePreferences().collectAsState(initial = NightMode.FollowSystem)

    val colors = when (state.value) {
        is NightMode.Enable -> {
            DarkColorPalette
        }
        is NightMode.Disable -> {
            LightColorPalette
        }
        is NightMode.FollowSystem -> {
            if (darkTheme) {
                DarkColorPalette
            } else {
                LightColorPalette
            }
        }
    }
    val rememberSystemUiController = rememberSystemUiController()
    SideEffect {
        rememberSystemUiController.setSystemBarsColor(
            color = colors.background
        )
        rememberSystemUiController.setStatusBarColor(color = colors.background)
    }
    CompositionLocalProvider(LocalSpacing provides Spacing()) {
        
    }

    MaterialTheme(
        colors = colors,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}