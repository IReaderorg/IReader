package ir.kazemcodes.infinity.presentation.reader


import ir.kazemcodes.infinity.data.network.models.Source
import ir.kazemcodes.infinity.data.network.sources.FreeWebNovel
import ir.kazemcodes.infinity.domain.models.remote.Chapter
import ir.kazemcodes.infinity.domain.models.FontType

data class ReaderScreenState(
    val isLoading: Boolean = false,
    val isLoaded: Boolean = false,
    val chapter: Chapter = Chapter.create(),
    val error: String = "",
    val fontSize: Int = 18,
    val font: FontType = FontType.Poppins,
    val brightness: Float = 0.5f,
    val source: Source = FreeWebNovel(),
    val isReaderModeEnable: Boolean = true,
)
