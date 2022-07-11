package org.ireader.domain.use_cases.local

import org.ireader.domain.use_cases.local.book_usecases.FindAllInLibraryBooks
import org.ireader.domain.use_cases.local.book_usecases.FindBookById
import org.ireader.domain.use_cases.local.book_usecases.SubscribeBookById
import org.ireader.domain.use_cases.local.book_usecases.SubscribeInLibraryBooks

data class LocalGetBookUseCases(
    val subscribeBookById: SubscribeBookById,
    val findBookById: FindBookById,
    val SubscribeInLibraryBooks: SubscribeInLibraryBooks,
    val findAllInLibraryBooks: FindAllInLibraryBooks,
    val findBookByKey: FindBookByKey,
    val findBooksByKey: FindBooksByKey,
    val subscribeBooksByKey: SubscribeBooksByKey,
)
