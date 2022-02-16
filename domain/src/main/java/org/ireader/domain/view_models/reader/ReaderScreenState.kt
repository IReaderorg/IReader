package org.ireader.domain.view_models.reader


import androidx.compose.ui.graphics.Color
import org.ireader.core.utils.UiText
import org.ireader.core_ui.theme.BackgroundColor
import org.ireader.core_ui.theme.FontType
import org.ireader.domain.R
import org.ireader.domain.models.entities.Book
import org.ireader.domain.models.entities.Chapter
import org.ireader.domain.models.source.Source

data class ReaderScreenState(
    val isLoading: Boolean = false,
    val isLocalLoaded: Boolean = false,
    val isRemoteLoaded: Boolean = false,
    val enable: Boolean = true,
    val book: Book? = null,
    val isDrawerAsc: Boolean = true,
    val isBookLoaded: Boolean = false,
    val isChapterLoaded: Boolean = false,
    val chapter: Chapter? = null,
    val chapters: List<Chapter> = emptyList(),
    val error: UiText = UiText.StringResource(R.string.no_error),
    val source: Source? = null,
    val isReaderModeEnable: Boolean = true,
    val isSettingModeEnable: Boolean = false,
    val isMainBottomModeEnable: Boolean = true,
    val currentChapterIndex: Int = 0,
    val isWebViewEnable: Boolean = false,
)

data class ReaderScreenPreferencesState(
    val isAsc: Boolean = true,
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
    val isChaptersReversed: Boolean = false,
    val isChapterReversingInProgress: Boolean = false,
    val scrollPosition: Int = 0,
)


sealed class Orientation(val index: Int) {
    object Portrait : Orientation(0)
    object Landscape : Orientation(1)
}
