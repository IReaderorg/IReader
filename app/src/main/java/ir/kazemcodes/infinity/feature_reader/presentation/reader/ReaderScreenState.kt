package ir.kazemcodes.infinity.feature_reader.presentation.reader


import androidx.compose.ui.graphics.Color
import ir.kazemcodes.infinity.core.data.network.models.Source
import ir.kazemcodes.infinity.core.domain.models.Book
import ir.kazemcodes.infinity.core.domain.models.Chapter
import ir.kazemcodes.infinity.core.domain.models.FontType
import ir.kazemcodes.infinity.core.presentation.theme.BackgroundColor

data class ReaderScreenState(
    val isLoading: Boolean = false,
    val isLoaded: Boolean = false,
    val book: Book = Book.create(),
    val isBookLoaded:Boolean = false,
    val isChapterLoaded:Boolean = false,
    val chapter: Chapter = Chapter.create(),
    val chapters: List<Chapter> = emptyList(),
    val drawerChapters : List<Chapter> = emptyList(),
    val isReversed : Boolean = false,
    val error: String = "",
    val fontSize: Int = 18,
    val font: FontType = FontType.Poppins,
    val brightness: Float = 0.5f,
    val source: Source,
    val isReaderModeEnable: Boolean = true,
    val isSettingModeEnable: Boolean = false,
    val isMainBottomModeEnable: Boolean = true,
    val distanceBetweenParagraphs: Int = 2,
    val paragraphsIndent: Int = 8,
    val lineHeight: Int = 25,
    val currentChapterIndex: Int,
    val backgroundColor: Color = BackgroundColor.Black.color,
    val textColor: Color = BackgroundColor.Black.onTextColor,
    val orientation: Orientation = Orientation.Portrait,
)

sealed class Orientation(val index : Int){
    object Portrait : Orientation(0)
    object Landscape : Orientation(1)
}
