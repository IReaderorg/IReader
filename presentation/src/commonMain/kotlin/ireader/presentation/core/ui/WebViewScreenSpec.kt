package ireader.presentation.core.ui

import androidx.compose.runtime.Composable

expect class WebViewScreenSpec(
    url: String?,
    sourceId: Long?,
    bookId: Long?,
    chapterId: Long?,
    enableBookFetch: Boolean,
    enableChapterFetch: Boolean,
    enableChaptersFetch: Boolean,
) {
    @Composable
    fun Content()
}