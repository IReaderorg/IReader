package ir.kazemcodes.infinity.core.domain.use_cases.local

import ir.kazemcodes.infinity.core.domain.use_cases.local.insert_usecases.InsertBook
import ir.kazemcodes.infinity.core.domain.use_cases.local.insert_usecases.InsertBooks
import ir.kazemcodes.infinity.core.domain.use_cases.local.insert_usecases.InsertChapter
import ir.kazemcodes.infinity.core.domain.use_cases.local.insert_usecases.InsertChapters

data class LocalInsertUseCases(
    val insertBook: InsertBook,
    val insertBooks: InsertBooks,
    val insertChapter: InsertChapter,
    val insertChapters: InsertChapters
)










