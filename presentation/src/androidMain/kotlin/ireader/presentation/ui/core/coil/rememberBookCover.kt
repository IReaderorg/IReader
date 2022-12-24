

package ireader.presentation.ui.core.coil

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import ireader.domain.models.entities.Book
import ireader.domain.models.entities.HistoryWithRelations
import ireader.domain.models.entities.UpdatesWithRelations
import ireader.domain.models.BookCover

@Composable
fun rememberBookCover(manga: Book): BookCover {
    return remember(manga.id) {
        BookCover.from(manga)
    }
}

@Composable
fun rememberBookCover(history: HistoryWithRelations): BookCover {
    return remember(history.id) {
        BookCover.from(history)
    }
}

@Composable
fun rememberBookCover(manga: UpdatesWithRelations): BookCover {
    return remember(manga.bookId) {
        BookCover.from(manga)
    }
}
