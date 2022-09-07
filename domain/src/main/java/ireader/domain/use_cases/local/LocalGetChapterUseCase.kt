package ireader.domain.use_cases.local

import ireader.domain.use_cases.local.chapter_usecases.FindAllInLibraryChapters
import ireader.domain.use_cases.local.chapter_usecases.FindChapterById
import ireader.domain.use_cases.local.chapter_usecases.FindChapterByKey
import ireader.domain.use_cases.local.chapter_usecases.FindChaptersByBookId
import ireader.domain.use_cases.local.chapter_usecases.FindChaptersByKey
import ireader.domain.use_cases.local.chapter_usecases.FindFirstChapter
import ireader.domain.use_cases.local.chapter_usecases.FindLastReadChapter
import ireader.domain.use_cases.local.chapter_usecases.SubscribeChapterById
import ireader.domain.use_cases.local.chapter_usecases.SubscribeChaptersByBookId
import ireader.domain.use_cases.local.chapter_usecases.SubscribeLastReadChapter
import ireader.domain.use_cases.local.chapter_usecases.UpdateLastReadTime

data class LocalGetChapterUseCase(
    val subscribeChapterById: SubscribeChapterById,
    val findChapterById: FindChapterById,
    val findAllInLibraryChapters: FindAllInLibraryChapters,
    val subscribeChaptersByBookId: SubscribeChaptersByBookId,
    val findChaptersByBookId: FindChaptersByBookId,
    val subscribeLastReadChapter: SubscribeLastReadChapter,
    val findLastReadChapter: FindLastReadChapter,
    val findFirstChapter: FindFirstChapter,
    val findChapterByKey: FindChapterByKey,
    val findChaptersByKey: FindChaptersByKey,
    val updateLastReadTime: UpdateLastReadTime
)
