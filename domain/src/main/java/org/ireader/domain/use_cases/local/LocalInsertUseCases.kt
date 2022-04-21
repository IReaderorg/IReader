package org.ireader.domain.use_cases.local

import androidx.annotation.Keep
import org.ireader.domain.use_cases.local.delete_usecases.chapter.UpdateChaptersUseCase
import org.ireader.domain.use_cases.local.insert_usecases.*
import javax.inject.Inject


data class LocalInsertUseCases @Inject constructor(
    val insertBook: InsertBook,
    val insertBooks: InsertBooks,
    val insertChapter: InsertChapter,
    val insertChapters: InsertChapters,
    val insertBookAndChapters: InsertBookAndChapters,
    val updateChaptersUseCase: UpdateChaptersUseCase,
)










