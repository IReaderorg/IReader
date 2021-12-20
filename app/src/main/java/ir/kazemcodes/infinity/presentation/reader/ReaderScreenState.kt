package ir.kazemcodes.infinity.presentation.reader


import androidx.compose.ui.text.font.FontFamily
import ir.kazemcodes.infinity.domain.models.Chapter
import ir.kazemcodes.infinity.presentation.theme.poppins

data class ReaderScreenState(
    val isLoading : Boolean = false,
    val chapter: Chapter = Chapter.create(),
    val error: String = "",
    val readingModel : Boolean= false,
    val fontSize : Int = 18,
    val font : FontFamily = poppins,


    )
