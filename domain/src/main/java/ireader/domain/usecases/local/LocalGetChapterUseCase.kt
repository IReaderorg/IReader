package ireader.domain.usecases.local

import ireader.domain.usecases.local.chapter_usecases.FindAllInLibraryChapters
import ireader.domain.usecases.local.chapter_usecases.FindChapterById
import ireader.domain.usecases.local.chapter_usecases.FindChaptersByBookId
import ireader.domain.usecases.local.chapter_usecases.SubscribeChaptersByBookId
import ireader.domain.usecases.local.chapter_usecases.UpdateLastReadTime

data class LocalGetChapterUseCase(
    val findChapterById: FindChapterById,
    val findAllInLibraryChapters: FindAllInLibraryChapters,
    val subscribeChaptersByBookId: SubscribeChaptersByBookId,
    val findChaptersByBookId: FindChaptersByBookId,
    val updateLastReadTime: UpdateLastReadTime
)
