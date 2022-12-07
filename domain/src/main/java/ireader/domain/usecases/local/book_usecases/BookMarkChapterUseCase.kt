package ireader.domain.usecases.local.book_usecases

import ireader.domain.models.entities.Chapter
import ireader.domain.usecases.local.LocalInsertUseCases
import org.koin.core.annotation.Factory

@Factory
class BookMarkChapterUseCase(
    private val insertUseCases: LocalInsertUseCases
) {

    suspend fun bookMarkChapter(chapter: Chapter?): Chapter? {
        chapter?.let { item ->
                insertUseCases.insertChapter(chapter.copy(bookmark = !item.bookmark))
                return chapter.copy(bookmark = !item.bookmark)
        }
        return null
    }
}