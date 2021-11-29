package ir.kazemcodes.infinity.explore_feature.presentation.screen.reading_screen


import androidx.compose.ui.text.font.FontFamily
import ir.kazemcodes.infinity.base_feature.theme.poppins
import ir.kazemcodes.infinity.explore_feature.data.model.Chapter

data class ReadingScreenState(
    val isLoading : Boolean = false,
    val chapter: Chapter = Chapter.create(),
    val error: String = "",
    val readingModel : Boolean= false,
    val fontSize : Int = 18,
    val font : FontFamily = poppins,


    )
