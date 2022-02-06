package ir.kazemcodes.infinity.feature_reader.presentation.reader


import androidx.compose.ui.graphics.Color
import ir.kazemcodes.infinity.R
import ir.kazemcodes.infinity.core.data.network.models.Source
import ir.kazemcodes.infinity.core.domain.models.Book
import ir.kazemcodes.infinity.core.domain.models.Chapter
import ir.kazemcodes.infinity.core.domain.models.FontType
import ir.kazemcodes.infinity.core.presentation.theme.BackgroundColor
import ir.kazemcodes.infinity.core.utils.UiText

data class ReaderScreenState(
    val isLocalLoading: Boolean = false,
    val isRemoteLoading: Boolean = false,
    val isLocalLoaded: Boolean = false,
    val isRemoteLoaded: Boolean = false,
    val enable :Boolean = true,
    val book: Book = Book.create(),
    val isBookLoaded:Boolean = false,
    val isChapterLoaded:Boolean = false,
    val chapter: Chapter = Chapter.create(),
    val chapters: List<Chapter> = emptyList(),
    val error: UiText = UiText.StringResource(R.string.no_error),
    val source: Source,
    val isReaderModeEnable: Boolean = true,
    val isSettingModeEnable: Boolean = false,
    val isMainBottomModeEnable: Boolean = true,
    val currentChapterIndex: Int = 0,
    val isWebViewEnable : Boolean = false,
)
data class ReaderScreenPreferencesState(
    val isAsc : Boolean = true,
    val fontSize: Int = 18,
    val font: FontType = FontType.Poppins,
    val brightness: Float = 0.3f,
    val distanceBetweenParagraphs: Int = 2,
    val paragraphsIndent: Int = 8,
    val lineHeight: Int = 25,
    val currentChapterIndex: Int = 0,
    val backgroundColor: Color = BackgroundColor.Black.color,
    val textColor: Color = BackgroundColor.Black.onTextColor,
    val orientation: Orientation = Orientation.Portrait,
    val isChaptersReversed : Boolean = false,
    val isChapterReversingInProgress: Boolean = false,
    val scrollPosition: Int = 0,
)


sealed class Orientation(val index : Int){
    object Portrait : Orientation(0)
    object Landscape : Orientation(1)
}
