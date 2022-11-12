package ireader.domain.usecases.local.insert_usecases

import ireader.common.models.entities.Chapter
import ireader.domain.data.repository.ChapterRepository
import ireader.domain.utils.extensions.withIOContext
import org.koin.core.annotation.Factory

@Factory
class InsertChapter(private val chapterRepository: ChapterRepository) {
    suspend operator fun invoke(chapter: Chapter?) {
        if (chapter == null) return
        return withIOContext {
            chapterRepository.insertChapter(chapter)
        }
    }
}
