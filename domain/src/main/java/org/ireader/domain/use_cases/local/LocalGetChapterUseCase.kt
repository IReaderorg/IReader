package org.ireader.domain.use_cases.local

import org.ireader.domain.use_cases.local.chapter_usecases.FindAllInLibraryChapters
import org.ireader.domain.use_cases.local.chapter_usecases.FindChapterById
import org.ireader.domain.use_cases.local.chapter_usecases.FindChapterByIdByBatch
import org.ireader.domain.use_cases.local.chapter_usecases.FindChapterByKey
import org.ireader.domain.use_cases.local.chapter_usecases.FindChaptersByBookId
import org.ireader.domain.use_cases.local.chapter_usecases.FindChaptersByKey
import org.ireader.domain.use_cases.local.chapter_usecases.FindFirstChapter
import org.ireader.domain.use_cases.local.chapter_usecases.FindLastReadChapter
import org.ireader.domain.use_cases.local.chapter_usecases.SubscribeChapterById
import org.ireader.domain.use_cases.local.chapter_usecases.SubscribeChaptersByBookId
import org.ireader.domain.use_cases.local.chapter_usecases.SubscribeLastReadChapter
import javax.inject.Inject

data class LocalGetChapterUseCase @Inject constructor(
    val subscribeChapterById: SubscribeChapterById,
    val findChapterById: FindChapterById,
    val findChapterByIdByBatch: FindChapterByIdByBatch,
    val findAllInLibraryChapters: FindAllInLibraryChapters,
    val subscribeChaptersByBookId: SubscribeChaptersByBookId,
    val findChaptersByBookId: FindChaptersByBookId,
    val subscribeLastReadChapter: SubscribeLastReadChapter,
    val findLastReadChapter: FindLastReadChapter,
    val findFirstChapter: FindFirstChapter,
    val findChapterByKey: FindChapterByKey,
    val findChaptersByKey: FindChaptersByKey,
)
