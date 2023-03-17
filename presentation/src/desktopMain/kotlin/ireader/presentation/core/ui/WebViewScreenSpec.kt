package ireader.presentation.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalUriHandler
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import ireader.presentation.core.VoyagerScreen

actual class WebViewScreenSpec actual constructor(
    private val url: String?,
    sourceId: Long?,
    bookId: Long?,
    chapterId: Long?,
    enableBookFetch: Boolean,
    enableChapterFetch: Boolean,
    enableChaptersFetch: Boolean
) : VoyagerScreen() {
    @Composable
    override fun Content() {
        val urlHandler = LocalUriHandler.current
        if (url != null) {
            urlHandler.openUri(url)
        }
        LocalNavigator.currentOrThrow.pop()
    }
}