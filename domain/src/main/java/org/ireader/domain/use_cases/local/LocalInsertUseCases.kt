package org.ireader.domain.use_cases.local

import org.ireader.domain.use_cases.local.chapter_usecases.SetLastReadToFalse
import org.ireader.domain.use_cases.local.insert_usecases.InsertBook
import org.ireader.domain.use_cases.local.insert_usecases.InsertBooks
import org.ireader.domain.use_cases.local.insert_usecases.InsertChapter
import org.ireader.domain.use_cases.local.insert_usecases.InsertChapters

data class LocalInsertUseCases(
    val insertBook: InsertBook,
    val insertBooks: InsertBooks,
    val insertChapter: InsertChapter,
    val insertChapters: InsertChapters,
    val setLastReadToFalse: SetLastReadToFalse,
)










