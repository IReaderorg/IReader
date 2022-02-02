package ir.kazemcodes.infinity.feature_library.presentation.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.lazy.LazyGridState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyGridState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.paging.compose.LazyPagingItems
import ir.kazemcodes.infinity.core.data.network.models.Source
import ir.kazemcodes.infinity.core.domain.models.Book
import ir.kazemcodes.infinity.core.presentation.layouts.CompactGridLayoutComposable
import ir.kazemcodes.infinity.core.presentation.layouts.GridLayoutComposable
import ir.kazemcodes.infinity.core.presentation.layouts.LayoutType
import ir.kazemcodes.infinity.core.presentation.layouts.LinearListDisplay
import ir.kazemcodes.infinity.core.ui.BookDetailScreenSpec


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LayoutComposable(
    navController: NavController,
    books: LazyPagingItems<Book>,
    layout: LayoutType,
    scrollState: LazyListState = rememberLazyListState(),
    gridState: LazyGridState = rememberLazyGridState(),
    source: Source? = null,
    isLocal:Boolean
) {

    when (layout) {
        is LayoutType.GridLayout -> {
            GridLayoutComposable(books = books,
                onClick = { book ->
                    navController.navigate(
                        route = BookDetailScreenSpec.buildRoute(sourceId = if (source?.sourceId != null) source.sourceId else book.sourceId , bookId = book.id)
                    )
                }, scrollState = gridState)
        }
        is LayoutType.ListLayout -> {
            LinearListDisplay(books = books, onClick = { book ->
                navController.navigate(
                    route = BookDetailScreenSpec.buildRoute(sourceId = if (source?.sourceId != null) source.sourceId else book.sourceId , bookId = book.id)
                )
            }, scrollState = scrollState)
        }
        is LayoutType.CompactGrid -> {
            CompactGridLayoutComposable(
                books = books,
                onClick = { book ->
                    navController.navigate(
                        route = BookDetailScreenSpec.buildRoute(sourceId = if (source?.sourceId != null) source.sourceId else book.sourceId , bookId = book.id)
                    )
                }, scrollState = gridState)
        }
    }
}