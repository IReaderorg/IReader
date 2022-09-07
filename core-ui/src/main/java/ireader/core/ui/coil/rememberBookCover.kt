

package ireader.core.ui.coil

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import ireader.common.models.entities.Book
import ireader.common.models.entities.HistoryWithRelations
import ireader.common.models.entities.UpdateWithInfo
import ireader.ui.imageloader.BookCover

@Composable
fun rememberBookCover(manga: Book): BookCover {
    return remember(manga.id) {
        BookCover.from(manga)
    }
}

@Composable
fun rememberBookCover(history: HistoryWithRelations): BookCover {
    return remember(history.bookId) {
        BookCover.from(history)
    }
}

@Composable
fun rememberBookCover(manga: UpdateWithInfo): BookCover {
    return remember(manga.bookId) {
        BookCover.from(manga)
    }
}
