package ireader.domain.use_cases.local.book_usecases

import ireader.common.models.entities.Chapter
import ireader.domain.use_cases.local.LocalInsertUseCases
import org.koin.core.annotation.Factory

@Factory
class BookMarkChapterUseCase(
    private val insertUseCases: LocalInsertUseCases
) {

    suspend fun bookMarkChapter(chapter:Chapter?):Chapter? {
        chapter?.let { item ->
                insertUseCases.insertChapter(chapter.copy(bookmark = !item.bookmark))
                return chapter.copy(bookmark = !item.bookmark)
        }
        return null
    }
}