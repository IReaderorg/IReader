package org.ireader.domain.use_cases.local

import org.ireader.domain.use_cases.local.chapter_usecases.*

data class LocalGetChapterUseCase(
    val subscribeChapterById: SubscribeChapterById,
    val findChapterById: FindChapterById,
    val subscribeChaptersByBookId: SubscribeChaptersByBookId,
    val findChaptersByBookId: FindChaptersByBookId,
    val subscribeLastReadChapter: SubscribeLastReadChapter,
    val findLastReadChapter: FindLastReadChapter,
    val findFirstChapter: FindFirstChapter,
    val getLocalChaptersByPaging: GetLocalChaptersByPaging,
    val findChapterByKey: FindChapterByKey,
    val findChaptersByKey: FindChaptersByKey,
)

