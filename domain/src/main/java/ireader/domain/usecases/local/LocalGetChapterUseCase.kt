package ireader.domain.usecases.local

import ireader.domain.usecases.local.chapter_usecases.*

data class LocalGetChapterUseCase(
    val findChapterById: FindChapterById,
    val findAllInLibraryChapters: FindAllInLibraryChapters,
    val subscribeChaptersByBookId: SubscribeChaptersByBookId,
    val findChaptersByBookId: FindChaptersByBookId,
    val subscribeChapterById: SubscribeChapterById,
    val updateLastReadTime: UpdateLastReadTime,
)
