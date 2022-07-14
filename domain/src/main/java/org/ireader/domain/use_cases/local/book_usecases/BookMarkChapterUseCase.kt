package org.ireader.domain.use_cases.local.book_usecases

import org.ireader.common_models.entities.Chapter
import org.ireader.domain.use_cases.local.LocalInsertUseCases
import javax.inject.Inject

class BookMarkChapterUseCase @Inject constructor(
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