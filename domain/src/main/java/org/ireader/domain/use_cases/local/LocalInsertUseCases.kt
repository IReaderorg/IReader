package org.ireader.domain.use_cases.local

import org.ireader.domain.use_cases.local.delete_usecases.chapter.UpdateChaptersUseCase
import org.ireader.domain.use_cases.local.insert_usecases.InsertBook
import org.ireader.domain.use_cases.local.insert_usecases.InsertBookAndChapters
import org.ireader.domain.use_cases.local.insert_usecases.InsertBooks
import org.ireader.domain.use_cases.local.insert_usecases.InsertChapter
import org.ireader.domain.use_cases.local.insert_usecases.InsertChapters
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
data class LocalInsertUseCases @Inject constructor(
    val insertBook: InsertBook,
    val insertBooks: InsertBooks,
    val insertChapter: InsertChapter,
    val insertChapters: InsertChapters,
    val insertBookAndChapters: InsertBookAndChapters,
    val updateChaptersUseCase: UpdateChaptersUseCase,
)
