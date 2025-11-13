package ireader.presentation.core.ui.util

import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import ireader.domain.models.common.AlignmentModel
import ireader.domain.models.common.ColorModel
import ireader.domain.models.common.FontFamilyModel
import ireader.domain.models.common.TextAlignmentModel

/**
 * Extension functions to convert domain models to Compose UI types.
 * This keeps the domain layer free of UI framework dependencies.
 */

// Color conversions
fun ColorModel.toComposeColor(): Color {
    return Color(red, green, blue, alpha)
}

fun Color.toColorModel(): ColorModel {
    return ColorModel(red, green, blue, alpha)
}

// Alignment conversions
fun AlignmentModel.toComposeAlignment(): Alignment {
    return when (this) {
        AlignmentModel.TOP_START -> Alignment.TopStart
        AlignmentModel.TOP_CENTER -> Alignment.TopCenter
        AlignmentModel.TOP_END -> Alignment.TopEnd
        AlignmentModel.CENTER_START -> Alignment.CenterStart
        AlignmentModel.CENTER -> Alignment.Center
        AlignmentModel.CENTER_END -> Alignment.CenterEnd
        AlignmentModel.BOTTOM_START -> Alignment.BottomStart
        AlignmentModel.BOTTOM_CENTER -> Alignment.BottomCenter
        AlignmentModel.BOTTOM_END -> Alignment.BottomEnd
    }
}

fun Alignment.toAlignmentModel(): AlignmentModel {
    return when (this) {
        Alignment.TopStart -> AlignmentModel.TOP_START
        Alignment.TopCenter -> AlignmentModel.TOP_CENTER
        Alignment.TopEnd -> AlignmentModel.TOP_END
        Alignment.CenterStart -> AlignmentModel.CENTER_START
        Alignment.Center -> AlignmentModel.CENTER
        Alignment.CenterEnd -> AlignmentModel.CENTER_END
        Alignment.BottomStart -> AlignmentModel.BOTTOM_START
        Alignment.BottomCenter -> AlignmentModel.BOTTOM_CENTER
        Alignment.BottomEnd -> AlignmentModel.BOTTOM_END
        else -> AlignmentModel.CENTER
    }
}

// Text alignment conversions
fun TextAlignmentModel.toComposeTextAlign(): TextAlign {
    return when (this) {
        TextAlignmentModel.LEFT -> TextAlign.Left
        TextAlignmentModel.CENTER -> TextAlign.Center
        TextAlignmentModel.RIGHT -> TextAlign.Right
        TextAlignmentModel.JUSTIFY -> TextAlign.Justify
        TextAlignmentModel.START -> TextAlign.Start
        TextAlignmentModel.END -> TextAlign.End
    }
}

fun TextAlign.toTextAlignmentModel(): TextAlignmentModel {
    return when (this) {
        TextAlign.Left -> TextAlignmentModel.LEFT
        TextAlign.Center -> TextAlignmentModel.CENTER
        TextAlign.Right -> TextAlignmentModel.RIGHT
        TextAlign.Justify -> TextAlignmentModel.JUSTIFY
        TextAlign.Start -> TextAlignmentModel.START
        TextAlign.End -> TextAlignmentModel.END
        else -> TextAlignmentModel.START
    }
}

// Font family conversions
fun FontFamilyModel.toComposeFontFamily(): FontFamily {
    return when (this) {
        FontFamilyModel.Default -> FontFamily.Default
        FontFamilyModel.SansSerif -> FontFamily.SansSerif
        FontFamilyModel.Serif -> FontFamily.Serif
        FontFamilyModel.Monospace -> FontFamily.Monospace
        FontFamilyModel.Cursive -> FontFamily.Cursive
        is FontFamilyModel.Custom -> {
            // For custom fonts, we'd need to load them from the path
            // For now, return default
            FontFamily.Default
        }
    }
}

fun FontFamily.toFontFamilyModel(): FontFamilyModel {
    return when (this) {
        FontFamily.Default -> FontFamilyModel.Default
        FontFamily.SansSerif -> FontFamilyModel.SansSerif
        FontFamily.Serif -> FontFamilyModel.Serif
        FontFamily.Monospace -> FontFamilyModel.Monospace
        FontFamily.Cursive -> FontFamilyModel.Cursive
        else -> FontFamilyModel.Default
    }
}
