package ir.kazemcodes.infinity.presentation.reader


import android.content.res.Configuration
import androidx.compose.ui.graphics.Color
import ir.kazemcodes.infinity.data.network.models.Source
import ir.kazemcodes.infinity.domain.models.FontType
import ir.kazemcodes.infinity.domain.models.remote.Book
import ir.kazemcodes.infinity.domain.models.remote.Chapter

data class ReaderScreenState(
    val isLoading: Boolean = false,
    val isLoaded: Boolean = false,
    val chapter: Chapter,
    val isChaptersReversed: Boolean = false,
    val chapters: List<Chapter>,
    val error: String = "",
    val fontSize: Int = 18,
    val font: FontType = FontType.Poppins,
    val brightness: Float = 0.5f,
    val source: Source,
    val isReaderModeEnable: Boolean = true,
    val isSettingModeEnable: Boolean = false,
    val isMainBottomModeEnable: Boolean = true,
    val distanceBetweenParagraphs: Int = 2,
    val lineHeight: Int = 25,
    val currentChapterIndex: Int = 0,
    val isChapterSliderMoving: Boolean? = false,
    val isDarkThemeEnabled: Boolean = false,
    val backgroundColor: Color = Color.White,
    val textColor: Color = Color.Black,
    val book: Book,
    val orientation : Int = Configuration.ORIENTATION_PORTRAIT,
)
