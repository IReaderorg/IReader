package org.ireader.domain.use_cases.local

import org.ireader.domain.use_cases.local.chapter_usecases.GetChaptersByBookId
import org.ireader.domain.use_cases.local.chapter_usecases.GetLastReadChapter
import org.ireader.domain.use_cases.local.chapter_usecases.GetLocalChaptersByPaging
import org.ireader.domain.use_cases.local.chapter_usecases.GetOneChapterById

data class LocalGetChapterUseCase(
    val getOneChapterById: GetOneChapterById,
    val getChaptersByBookId: GetChaptersByBookId,
    val getLastReadChapter: GetLastReadChapter,
    val getLocalChaptersByPaging: GetLocalChaptersByPaging,

    )

