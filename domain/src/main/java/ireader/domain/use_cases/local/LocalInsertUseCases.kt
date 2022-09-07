package ireader.domain.use_cases.local

import ireader.domain.use_cases.local.insert_usecases.InsertBook
import ireader.domain.use_cases.local.insert_usecases.InsertBookAndChapters
import ireader.domain.use_cases.local.insert_usecases.InsertBooks
import ireader.domain.use_cases.local.insert_usecases.InsertChapter
import ireader.domain.use_cases.local.insert_usecases.InsertChapters
import ireader.domain.use_cases.local.insert_usecases.UpdateBook

data class LocalInsertUseCases(
    val insertBook: InsertBook,
    val insertBooks: InsertBooks,
    val insertChapter: InsertChapter,
    val insertChapters: InsertChapters,
    val insertBookAndChapters: InsertBookAndChapters,
    val updateBook: UpdateBook
)
