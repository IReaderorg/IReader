package org.ireader.domain.use_cases.local

import org.ireader.domain.use_cases.local.book_usecases.*
import javax.inject.Inject

data class LocalGetBookUseCases @Inject constructor(
    val subscribeBookById: SubscribeBookById,
    val findBookById: FindBookById,
    val findBookByIds: FindBookByIds,
    val SubscribeInLibraryBooks: SubscribeInLibraryBooks,
    val findAllInLibraryBooks: FindAllInLibraryBooks,
    val findBookByKey: FindBookByKey,
    val findBooksByKey: FindBooksByKey,
)















