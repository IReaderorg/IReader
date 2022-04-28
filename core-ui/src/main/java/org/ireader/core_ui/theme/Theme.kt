package org.ireader.core_ui.theme

import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import org.ireader.core_ui.ui.Colour

val DarkColorPalette = darkColors(
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
val LightColorPalette = lightColors(
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

// @Composable
// fun InfinityTheme(
//    darkTheme: Boolean = isSystemInDarkTheme(),
//    content: @Composable() () -> Unit,
// ) {
//    val vm : AppThemeViewModel = hiltViewModel()
//    val colors = if (darkTheme) {
//        DarkColorPalette
//    } else {
//        LightColorPalette
//    }
//    val useDarkIcon = MaterialTheme.colors.isLight
//    val systemUiController = rememberSystemUiController()
//    val transparentStatusBar = LocalTransparentStatusBar.current.enabled
//    if (!isMiui) {
//        SideEffect {
//            if (transparentStatusBar) {
//                systemUiController.setSystemBarsColor(
//                    color = Color.Transparent,
//                    darkIcons = useDarkIcon,
//                )
//            } else {
//                systemUiController.setSystemBarsColor(
//                    color = colors.background,
//                    darkIcons = !darkTheme,
//                )
//            }
//
//            systemUiController.setStatusBarColor(
//                color = colors.background,
//                darkIcons = !darkTheme,
//            )
//            systemUiController.setNavigationBarColor(
//                color = colors.background,
//                darkIcons = !darkTheme
//            )
//        }
//
//
//    }
//    CompositionLocalProvider(LocalSpacing provides Spacing()) {
//
//    }
//
//    MaterialTheme(
//        colors = colors,
//        typography = Typography,
//        shapes = Shapes,
//        content = content
//    )
// }
