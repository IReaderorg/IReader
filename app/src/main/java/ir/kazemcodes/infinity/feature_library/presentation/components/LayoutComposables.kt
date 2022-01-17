package ir.kazemcodes.infinity.feature_library.presentation.components

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.paging.compose.LazyPagingItems
import com.zhuinden.simplestack.Backstack
import ir.kazemcodes.infinity.core.data.network.models.Source
import ir.kazemcodes.infinity.core.domain.models.Book
import ir.kazemcodes.infinity.core.presentation.layouts.CompactGridLayoutComposable
import ir.kazemcodes.infinity.core.presentation.layouts.GridLayoutComposable
import ir.kazemcodes.infinity.core.presentation.layouts.LayoutType
import ir.kazemcodes.infinity.core.presentation.layouts.LinearListDisplay
import ir.kazemcodes.infinity.feature_activity.presentation.BookDetailKey


@Composable
fun LayoutComposable(
    backStack : Backstack,
    books: LazyPagingItems<Book>,
    layout: LayoutType,
    scrollState: LazyListState = rememberLazyListState(),
    source: Source? = null,
    isLocal:Boolean
) {

    when (layout) {
        is LayoutType.GridLayout -> {
            GridLayoutComposable(books = books,
                onClick = { book ->
                    backStack.goTo(
                        BookDetailKey(
                            book = book,
                            sourceName = if (source?.name != null) source.name else book.source
                                ?: "",
                            isLocal

                        )
                    )
                }, scrollState = scrollState)
        }
        is LayoutType.ListLayout -> {
            LinearListDisplay(books = books, onClick = { book ->
                backStack.goTo(
                    BookDetailKey(
                        book,
                        sourceName = if (source?.name != null) source.name else book.source
                            ?: "",
                        isLocal
                    )
                )
            }, scrollState = scrollState)
        }
        is LayoutType.CompactGrid -> {
            CompactGridLayoutComposable(
                books = books,
                onClick = { book ->
                    backStack.goTo(
                        BookDetailKey(
                            book = book,
                            sourceName = if (source?.name != null) source.name else book.source
                                ?: "",
                            isLocal
                        )
                    )
                }, scrollState = scrollState)
        }
    }
}