package ir.kazemcodes.infinity.presentation.reader


import ir.kazemcodes.infinity.data.network.models.Source
import ir.kazemcodes.infinity.domain.models.FontType
import ir.kazemcodes.infinity.domain.models.remote.Chapter

data class ReaderScreenState(
    val isLoading: Boolean = false,
    val isLoaded: Boolean = false,
    val chapter: Chapter = Chapter.create(),
    val error: String = "",
    val fontSize: Int = 18,
    val font: FontType = FontType.Poppins,
    val brightness: Float = 0.5f,
    val source: Source,
    val isReaderModeEnable: Boolean = true,
    val distanceBetweenParagraphs : Int = 2
)
