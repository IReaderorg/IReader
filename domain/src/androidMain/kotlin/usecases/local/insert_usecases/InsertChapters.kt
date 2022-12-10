package ireader.domain.usecases.local.insert_usecases

import ireader.domain.data.repository.ChapterRepository
import ireader.domain.models.entities.Chapter
import ireader.domain.utils.extensions.withIOContext
import org.koin.core.annotation.Factory

@Factory
class InsertChapters(private val chapterRepository: ChapterRepository) {
    suspend operator fun invoke(chapters: List<Chapter>): List<Long> {
        return withIOContext {
            return@withIOContext chapterRepository.insertChapters(chapters)
        }
    }
}
