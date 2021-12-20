package ir.kazemcodes.infinity.base_feature.navigation

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import ir.kazemcodes.infinity.domain.network.models.HttpSource
import ir.kazemcodes.infinity.domain.network.models.ParsedHttpSource
import ir.kazemcodes.infinity.presentation.book_detail.BookDetailScreen
import ir.kazemcodes.infinity.presentation.chapter_detail.ChapterDetailScreen
import ir.kazemcodes.infinity.domain.models.Book
import ir.kazemcodes.infinity.domain.models.Chapter
import ir.kazemcodes.infinity.extension_feature.presentation.extension_screen.ExtensionScreen
import ir.kazemcodes.infinity.presentation.browse.BrowserScreen
import ir.kazemcodes.infinity.presentation.home.ComposeKey
import ir.kazemcodes.infinity.presentation.home.MainScreen
import ir.kazemcodes.infinity.presentation.reader.ReadingScreen
import ir.kazemcodes.infinity.presentation.screen.components.WebPageScreen
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue


@Immutable
@Parcelize
data class MainScreenKey(val noArgument: String = "") : ComposeKey() {
    @ExperimentalMaterialApi
    @Composable
    override fun ScreenComposable(modifier: Modifier) {
        MainScreen()
    }

}
@Immutable
@Parcelize
data class BrowserScreenKey(val api: @RawValue ParsedHttpSource) : ComposeKey() {
    @Composable
    override fun ScreenComposable(modifier: Modifier) {
        BrowserScreen(api=api)
    }

}

@Immutable
@Parcelize
data class BookDetailKey(val book: Book, val api: @RawValue HttpSource) : ComposeKey() {
    @Composable
    override fun ScreenComposable(modifier: Modifier) {
        BookDetailScreen(book=book, api = api)

    }

}
@Immutable
@Parcelize
data class WebViewKey(val url: String) : ComposeKey() {
    @ExperimentalMaterialApi
    @Composable
    override fun ScreenComposable(modifier: Modifier) {
        WebPageScreen(url)

    }
}
@Immutable
@Parcelize
data class ChapterDetailKey(val book: Book, val chapters: List<Chapter>, val api: @RawValue HttpSource) : ComposeKey() {

    @Composable
    override fun ScreenComposable(modifier: Modifier) {
        ChapterDetailScreen(chapters = chapters , book = book ,api=api)
    }
}
@Immutable
@Parcelize
data class ReadingContentKey(val book: Book, val chapter: Chapter, val api: @RawValue HttpSource) : ComposeKey() {

    @ExperimentalMaterialApi
    @Composable
    override fun ScreenComposable(modifier: Modifier) {

        ReadingScreen(book = book, chapter = chapter,api = api)
    }
}
@Immutable
@Parcelize
data class ExtensionScreenKey(val noArgs : String = "") : ComposeKey() {

    @Composable
    override fun ScreenComposable(modifier: Modifier) {

        ExtensionScreen()
    }
}