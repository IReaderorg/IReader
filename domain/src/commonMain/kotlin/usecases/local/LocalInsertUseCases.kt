package ireader.domain.usecases.local

import ireader.domain.usecases.local.insert_usecases.*

data class LocalInsertUseCases(
    val insertBook: InsertBook,
    val insertBooks: InsertBooks,
    val insertChapter: InsertChapter,
    val insertChapters: InsertChapters,
    val insertBookAndChapters: InsertBookAndChapters,
    val updateBook: UpdateBook
)
