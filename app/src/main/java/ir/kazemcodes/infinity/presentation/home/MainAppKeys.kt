package ir.kazemcodes.infinity.base_feature.navigation

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.zhuinden.simplestack.ServiceBinder
import com.zhuinden.simplestackextensions.servicesktx.add
import com.zhuinden.simplestackextensions.servicesktx.lookup
import ir.kazemcodes.infinity.domain.local_feature.domain.use_case.LocalUseCase
import ir.kazemcodes.infinity.domain.models.Book
import ir.kazemcodes.infinity.domain.models.Chapter
import ir.kazemcodes.infinity.domain.network.models.Source
import ir.kazemcodes.infinity.domain.use_cases.remote.RemoteUseCase
import ir.kazemcodes.infinity.presentation.book_detail.BookDetailScreen
import ir.kazemcodes.infinity.presentation.book_detail.BookDetailViewModel
import ir.kazemcodes.infinity.presentation.browse.BrowseViewModel
import ir.kazemcodes.infinity.presentation.browse.BrowserScreen
import ir.kazemcodes.infinity.presentation.chapter_detail.ChapterDetailScreen
import ir.kazemcodes.infinity.presentation.chapter_detail.ChapterDetailViewModel
import ir.kazemcodes.infinity.presentation.core.Constants.DatastoreServiceTAG
import ir.kazemcodes.infinity.presentation.extension.ExtensionScreen
import ir.kazemcodes.infinity.presentation.home.ComposeKey
import ir.kazemcodes.infinity.presentation.home.MainScreen
import ir.kazemcodes.infinity.presentation.library.LibraryViewModel
import ir.kazemcodes.infinity.presentation.reader.ReaderScreenViewModel
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

    override fun bindServices(serviceBinder: ServiceBinder) {
        with(serviceBinder) {
            add(LibraryViewModel(lookup<LocalUseCase>() ))
        }
    }
}
@Immutable
@Parcelize
data class BrowserScreenKey(val source: @RawValue Source) : ComposeKey() {
    @Composable
    override fun ScreenComposable(modifier: Modifier) {
        BrowserScreen()
    }

    override fun bindServices(serviceBinder: ServiceBinder) {
        with(serviceBinder) {
            add(BrowseViewModel(lookup<LocalUseCase>() , lookup<RemoteUseCase>(),source = source))
        }

    }

}

@Immutable
@Parcelize
data class BookDetailKey(val book: Book, val source: @RawValue Source) : ComposeKey() {
    @Composable
    override fun ScreenComposable(modifier: Modifier) {
        BookDetailScreen(book=book)

    }

    override fun bindServices(serviceBinder: ServiceBinder) {
        with(serviceBinder) {
            add(BookDetailViewModel(lookup<LocalUseCase>() , lookup<RemoteUseCase>(),source = source))
        }
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
data class ChapterDetailKey(val book: Book, val chapters: List<Chapter>, val source: @RawValue Source) : ComposeKey() {

    @Composable
    override fun ScreenComposable(modifier: Modifier) {
        ChapterDetailScreen(chapters = chapters , book = book)
    }
    override fun bindServices(serviceBinder: ServiceBinder) {
        with(serviceBinder) {
            add(ChapterDetailViewModel(lookup<LocalUseCase>(), source = source))
        }
    }
}
@Immutable
@Parcelize
data class ReaderScreenKey(val book: Book, val chapter: Chapter, val source: @RawValue Source) : ComposeKey() {

    @ExperimentalMaterialApi
    @Composable
    override fun ScreenComposable(modifier: Modifier) {

        ReadingScreen(book = book, chapter = chapter)
    }
    override fun bindServices(serviceBinder: ServiceBinder) {
        with(serviceBinder) {
            add(ReaderScreenViewModel(
                lookup<LocalUseCase>(),
                lookup<RemoteUseCase>(),
                lookup<DataStore<Preferences>>(DatastoreServiceTAG),
                source = source,
                book = book,
                chapter = chapter
            ))
        }
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