package ireader.domain.usecases.local.insert_usecases

import ireader.domain.data.repository.ChapterRepository
import ireader.common.models.entities.Chapter
import org.koin.core.annotation.Factory

@Factory
class InsertChapter(private val chapterRepository: ChapterRepository) {
    suspend operator fun invoke(chapter: Chapter) {
        return ireader.common.extensions.withIOContext {
            chapterRepository.insertChapter(chapter)
        }
    }
}
