

package org.ireader.core_ui.coil

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import org.ireader.common_models.entities.Book
import org.ireader.common_models.entities.HistoryWithRelations
import org.ireader.common_models.entities.UpdateWithInfo
import org.ireader.image_loader.BookCover

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
