package ireader.presentation.core

import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import ireader.domain.models.common.AlignmentModel
import ireader.domain.models.common.ColorModel
import ireader.domain.models.common.FontFamilyModel
import ireader.domain.models.common.TextAlignmentModel

/**
 * Extension functions to convert domain models to Compose UI types
 */

fun ColorModel.toComposeColor(): Color {
    return Color(red, green, blue, alpha)
}

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

fun FontFamilyModel.toComposeFontFamily(): FontFamily {
    return when (this) {
        is FontFamilyModel.Default -> FontFamily.Default
        is FontFamilyModel.SansSerif -> FontFamily.SansSerif
        is FontFamilyModel.Serif -> FontFamily.Serif
        is FontFamilyModel.Monospace -> FontFamily.Monospace
        is FontFamilyModel.Cursive -> FontFamily.Cursive
        is FontFamilyModel.Custom -> {
            // For custom fonts, we'll need platform-specific implementation
            // For now, return Default
            FontFamily.Default
        }
    }
}

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
