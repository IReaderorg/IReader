package org.ireader.domain.use_cases.local

import org.ireader.domain.use_cases.local.chapter_usecases.*

data class LocalGetChapterUseCase(
    val getOneChapterById: GetOneChapterById,
    val getChaptersByBookId: GetChaptersByBookId,
    val findLastReadChapter: FindLastReadChapter,
    val findFirstChapter: FindFirstChapter,
    val getLocalChaptersByPaging: GetLocalChaptersByPaging,
)

