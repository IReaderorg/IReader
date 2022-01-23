package ir.kazemcodes.infinity.core.domain.use_cases.local

import ir.kazemcodes.infinity.core.domain.use_cases.local.chapter_usecases.GetChaptersByBookId
import ir.kazemcodes.infinity.core.domain.use_cases.local.chapter_usecases.GetLastReadChapter
import ir.kazemcodes.infinity.core.domain.use_cases.local.chapter_usecases.GetLocalChaptersByPaging
import ir.kazemcodes.infinity.core.domain.use_cases.local.chapter_usecases.GetOneChapterById

data class LocalGetChapterUseCase(
    val getOneChapterById: GetOneChapterById,
    val getChaptersByBookId: GetChaptersByBookId,
    val getLastReadChapter: GetLastReadChapter,
    val getLocalChaptersByPaging: GetLocalChaptersByPaging,

)

