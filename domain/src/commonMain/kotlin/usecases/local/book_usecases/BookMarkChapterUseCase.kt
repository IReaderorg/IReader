package ireader.domain.usecases.local.book_usecases

import ireader.domain.models.entities.Chapter



class BookMarkChapterUseCase(
    private val insertUseCases: ireader.domain.usecases.local.LocalInsertUseCases
) {

    suspend fun bookMarkChapter(chapter: Chapter?): Chapter? {
        chapter?.let { item ->
                insertUseCases.insertChapter(chapter.copy(bookmark = !item.bookmark))
                return chapter.copy(bookmark = !item.bookmark)
        }
        return null
    }
}