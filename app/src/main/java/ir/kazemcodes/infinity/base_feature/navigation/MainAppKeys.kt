package ir.kazemcodes.infinity.base_feature.navigation

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import ir.kazemcodes.infinity.base_feature.theme.InfinityTheme
import ir.kazemcodes.infinity.explore_feature.data.model.Book
import ir.kazemcodes.infinity.explore_feature.data.model.Chapter
import ir.kazemcodes.infinity.explore_feature.presentation.screen.book_detail_screen.BookDetailScreen
import ir.kazemcodes.infinity.explore_feature.presentation.screen.chapters_screen.ChapterDetailScreen
import ir.kazemcodes.infinity.explore_feature.presentation.screen.reading_screen.ReadingScreen
import ir.kazemcodes.infinity.presentation.screen.components.WebPageScreen
import kotlinx.parcelize.Parcelize




@Immutable
@Parcelize
data class MainScreenKey(val noArgument: String = "") : ComposeKey() {
    @ExperimentalMaterialApi
    @Composable
    override fun ScreenComposable(modifier: Modifier) {
        InfinityTheme() {
        MainScreen()

        }
    }

}

@Immutable
@Parcelize
data class BookDetailKey(val book: Book) : ComposeKey() {
    @ExperimentalMaterialApi
    @Composable
    override fun ScreenComposable(modifier: Modifier) {

        InfinityTheme() {

        BookDetailScreen(book)
        }
    }
}
@Immutable
@Parcelize
data class WebViewKey(val url: String) : ComposeKey() {
    @ExperimentalMaterialApi
    @Composable
    override fun ScreenComposable(modifier: Modifier) {
        InfinityTheme() {

        WebPageScreen(url)
        }
    }
}
@Immutable
@Parcelize
data class ChapterDetailKey(val book: Book,val chapters: List<Chapter>) : ComposeKey() {
    @ExperimentalMaterialApi
    @Composable
    override fun ScreenComposable(modifier: Modifier) {
        InfinityTheme() {
        ChapterDetailScreen(chapters = chapters , book = book)

        }
    }
}
@Immutable
@Parcelize
data class ReadingContentKey(val book: Book, val chapter: Chapter) : ComposeKey() {
    @ExperimentalMaterialApi
    @Composable
    override fun ScreenComposable(modifier: Modifier) {
        InfinityTheme() {
        ReadingScreen(book = book, chapter = chapter)

        }
    }
}