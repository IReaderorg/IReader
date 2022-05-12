package org.ireader.core_ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import org.ireader.core_ui.ui.Colour

data class Theme(
    val id: Int,
    val materialColors: ColorScheme,
    val extraColors: ExtraColors,
)

val themes = listOf(
    Theme(
        1, lightColorScheme(),
        ExtraColors(
            bars = androidx.compose.ui.graphics.Color.White,
            onBars = Color.Black
        )
    ),
    Theme(
        2,
        lightColorScheme(
            primary = Color(0xFF2979FF),
            primaryContainer = Color(0xFF2979FF),
            onPrimary = Color.White,
            secondary = Color(0xFF2979FF),
            secondaryContainer = Color(0xFF2979FF),
            onSecondary = Color.White
        ),
        ExtraColors(
            bars = Color(0xFF54759E),
            onBars = Color.White
        )
    ),
    Theme(
        3, darkColorScheme(),
        ExtraColors(
            bars = Color(0xFF212121),
            onBars = Color.White
        )
    ),
    Theme(
        4,
        darkColorScheme(
            primary = Color.Black,
            background = Color.Black
        ),
        ExtraColors(
            bars = Color.Black,
            onBars = Color.White
        )
    ),
    Theme(
        5,
        darkColorScheme(
            primary = Colour.blue_accent,
            primaryContainer = Colour.blue_600,
            onPrimary = Colour.black_900,
            secondary = Colour.blue_accent,
            onSecondary = Colour.black_900,
            background = Color(0xFF121212),
            surface = Color(0xFF000000),
            onBackground = Colour.white_50,
            onSurface = Colour.white_50,
            error = Colour.red_600,
            onError = Colour.black_900,
        ),
        ExtraColors(
            bars = Color(0xFF181818),
            onBars = Color.White
        )
    ),
    Theme(
        6,
        lightColorScheme(
            primary = Colour.blue_accent,
            primaryContainer = Colour.blue_700,
            onPrimary = Colour.white_50,
            secondary = Colour.blue_accent,
            onSecondary = Colour.white_50,
            background = Colour.white_50,
            surface = Colour.white_50,
            onBackground = Colour.black_900,
            onSurface = Colour.black_900,
            error = Colour.red_600,
            onError = Colour.white_50,
        ),
        ExtraColors(
            bars = Color.White,
            onBars = Color.Black
        )
    )
)
