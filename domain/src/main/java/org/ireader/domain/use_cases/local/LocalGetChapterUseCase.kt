package org.ireader.domain.use_cases.local

import androidx.annotation.Keep
import org.ireader.domain.use_cases.local.chapter_usecases.*
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

