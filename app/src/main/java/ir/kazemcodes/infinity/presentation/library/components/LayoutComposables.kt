package ir.kazemcodes.infinity.presentation.library.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import com.zhuinden.simplestackcomposeintegration.core.LocalBackstack
import ir.kazemcodes.infinity.base_feature.navigation.BookDetailKey
import ir.kazemcodes.infinity.data.network.models.Source
import ir.kazemcodes.infinity.domain.models.Book
import ir.kazemcodes.infinity.presentation.components.GridLayoutComposable
import ir.kazemcodes.infinity.presentation.components.LinearListDisplay
import ir.kazemcodes.infinity.presentation.layouts.CompactGridLayoutComposable
import ir.kazemcodes.infinity.presentation.layouts.LayoutType

@ExperimentalFoundationApi
@Composable
fun LayoutComposable(
    books: List<Book>,
    layout: LayoutType,
    scrollState: LazyListState = rememberLazyListState(),
    source : Source? = null
) {
    val backStack = LocalBackstack.current
    when (layout) {
        is LayoutType.GridLayout -> {
            GridLayoutComposable(books = books,
                onClick = { index ->
                    backStack.goTo(
                        BookDetailKey(
                            book = books[index],
                            sourceName = if (source?.name != null) source.name else books[index].source?:""
                        )
                    )
                }, scrollState = scrollState)
        }
        is LayoutType.ListLayout -> {
            LinearListDisplay(books = books, onClick = { index ->
                backStack.goTo(
                    BookDetailKey(
                        books[index],
                        sourceName = if (source?.name != null) source.name else books[index].source?:""
                    )
                )
            }, scrollState = scrollState)
        }
        is LayoutType.CompactGrid -> {
            CompactGridLayoutComposable(
                books = books,
                onClick = { index ->
                    backStack.goTo(
                        BookDetailKey(
                            book = books[index],
                            sourceName = if (source?.name != null) source.name else books[index].source?:""
                        )
                    )
                }, scrollState = scrollState)
        }
    }
}