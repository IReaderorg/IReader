package ireader.domain.usecases.chapter

/**
 * Aggregate class for all chapter-related use cases
 * Provides a single point of access for chapter operations
 */
data class ChapterUseCases(
    val getChaptersByBookId: GetChaptersByBookIdUseCase,
    val getChapterById: GetChapterByIdUseCase,
    val getLastReadChapter: GetLastReadChapterUseCase,
    val updateReadStatus: UpdateChapterReadStatusUseCase,
    val updateBookmarkStatus: UpdateChapterBookmarkStatusUseCase,
    val deleteChapters: DeleteChaptersUseCase
)
