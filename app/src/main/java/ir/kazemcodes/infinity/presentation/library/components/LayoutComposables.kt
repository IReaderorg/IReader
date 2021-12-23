package ir.kazemcodes.infinity.presentation.library.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.runtime.Composable
import com.zhuinden.simplestackcomposeintegration.core.LocalBackstack
import ir.kazemcodes.infinity.base_feature.navigation.BookDetailKey
import ir.kazemcodes.infinity.domain.models.Book
import ir.kazemcodes.infinity.library_feature.util.mappingApiNameToAPi
import ir.kazemcodes.infinity.presentation.browse.LayoutType
import ir.kazemcodes.infinity.presentation.components.GridLayoutComposable
import ir.kazemcodes.infinity.presentation.components.LinearViewList

@ExperimentalFoundationApi
@Composable
fun LayoutComposable(books: List<Book>, layout: LayoutType) {
    val backStack = LocalBackstack.current
    when (layout) {
        is LayoutType.GridLayout -> {
            GridLayoutComposable(books = books,
                onClick = { index ->
                    backStack.goTo(
                        BookDetailKey(
                            book = books[index],
                            source = mappingApiNameToAPi(books[index].source ?: "")
                        )
                    )
                })
        }
        is LayoutType.CompactLayout -> {
            LinearViewList(books = books, onClick = { index ->
                backStack.goTo(
                    BookDetailKey(
                        books[index],
                        source = mappingApiNameToAPi(books[index].source ?: "")
                    )
                )
            })
        }
    }
}