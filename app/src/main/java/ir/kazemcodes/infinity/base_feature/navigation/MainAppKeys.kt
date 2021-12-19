package ir.kazemcodes.infinity.base_feature.navigation

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import ir.kazemcodes.infinity.api_feature.HttpSource
import ir.kazemcodes.infinity.api_feature.network.ParsedHttpSource
import ir.kazemcodes.infinity.book_detail_feature.presentation.book_detail_screen.BookDetailScreen
import ir.kazemcodes.infinity.explore_feature.data.model.Book
import ir.kazemcodes.infinity.explore_feature.data.model.Chapter
import ir.kazemcodes.infinity.explore_feature.presentation.screen.browse_screen.BrowserScreen
import ir.kazemcodes.infinity.explore_feature.presentation.screen.chapters_screen.ChapterDetailScreen
import ir.kazemcodes.infinity.explore_feature.presentation.screen.reading_screen.ReadingScreen
import ir.kazemcodes.infinity.extension_feature.presentation.extension_screen.ExtensionScreen
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
data class BookDetailKey(val book: Book,val api: @RawValue HttpSource) : ComposeKey() {
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
data class ChapterDetailKey(val book: Book,val chapters: List<Chapter>,val api: @RawValue HttpSource) : ComposeKey() {

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