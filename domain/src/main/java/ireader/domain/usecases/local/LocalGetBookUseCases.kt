package ireader.domain.usecases.local

import ireader.domain.usecases.local.book_usecases.FindAllInLibraryBooks
import ireader.domain.usecases.local.book_usecases.FindBookById
import ireader.domain.usecases.local.book_usecases.SubscribeBookById
import ireader.domain.usecases.local.book_usecases.SubscribeInLibraryBooks

data class LocalGetBookUseCases(
    val subscribeBookById: SubscribeBookById,
    val findBookById: FindBookById,
    val SubscribeInLibraryBooks: SubscribeInLibraryBooks,
    val findAllInLibraryBooks: FindAllInLibraryBooks,
    val findBookByKey: FindBookByKey,
    val findBooksByKey: FindBooksByKey,
    val subscribeBooksByKey: SubscribeBooksByKey,
)
