package ireader.presentation.core.ui

import androidx.compose.runtime.Composable
import ireader.presentation.core.VoyagerScreen

actual class WebViewScreenSpec actual constructor(url: String?, sourceId: Long?, bookId: Long?, chapterId: Long?, enableBookFetch: Boolean, enableChapterFetch: Boolean, enableChaptersFetch: Boolean) : VoyagerScreen() {
    @Composable
    override fun Content() {
        NotImplementedScreen("WebViewScreenSpec")
    }
}