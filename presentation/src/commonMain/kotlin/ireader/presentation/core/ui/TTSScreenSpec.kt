package ireader.presentation.core.ui

import androidx.compose.runtime.Composable

expect class TTSScreenSpec(
    bookId: Long,
    chapterId: Long,
    sourceId: Long,
    readingParagraph: Int,
) {
    @Composable
    fun Content()
}