package ireader.domain.usecases.local.chapter_usecases

import ireader.domain.models.entities.Chapter
import ireader.domain.data.repository.ChapterRepository
import ireader.domain.usecases.reader.ContentFilterUseCase
import ireader.i18n.LAST_CHAPTER
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map


class FindChapterById(
    private val chapterRepository: ChapterRepository,
    private val contentFilterUseCase: ContentFilterUseCase? = null
) {
    suspend operator fun invoke(
        chapterId: Long?,
        bookId: Long? = null,
    ): Chapter? {
        if (chapterId == null) return null
        val chapter = if (chapterId != LAST_CHAPTER) {
            chapterRepository.findChapterById(chapterId = chapterId)
        } else if (bookId != null) {
            chapterRepository.findLastReadChapter(bookId)
        } else {
            chapterRepository.findChapterById(chapterId)
        }
        
        // Apply content filter to chapter content
        return chapter?.let { applyContentFilter(it) }
    }
    
    private fun applyContentFilter(chapter: Chapter): Chapter {
        if (contentFilterUseCase == null || chapter.content.isEmpty()) {
            return chapter
        }
        val filteredContent = contentFilterUseCase.filterPages(chapter.content, chapter.bookId)
        return if (filteredContent != chapter.content) {
            chapter.copy(content = filteredContent)
        } else {
            chapter
        }
    }
}

class SubscribeChapterById(
    private val chapterRepository: ChapterRepository,
    private val contentFilterUseCase: ContentFilterUseCase? = null
) {
    suspend operator fun invoke(
            chapterId: Long?,
            bookId: Long? = null,
    ): kotlinx.coroutines.flow.Flow<Chapter?> {
        if (chapterId == null) return emptyFlow()
        val flow = if (chapterId != LAST_CHAPTER) {
            chapterRepository.subscribeChapterById(chapterId = chapterId)
        } else if (bookId != null) {
            chapterRepository.subscribeLastReadChapter(bookId)
        } else {
            chapterRepository.subscribeChapterById(chapterId)
        }
        
        // Apply content filter to chapter content
        return flow.map { chapter -> chapter?.let { applyContentFilter(it) } }
    }
    
    private fun applyContentFilter(chapter: Chapter): Chapter {
        if (contentFilterUseCase == null || chapter.content.isEmpty()) {
            return chapter
        }
        val filteredContent = contentFilterUseCase.filterPages(chapter.content, chapter.bookId)
        return if (filteredContent != chapter.content) {
            chapter.copy(content = filteredContent)
        } else {
            chapter
        }
    }
}

class FindAllInLibraryChapters(private val chapterRepository: ChapterRepository) {
    suspend operator fun invoke(): List<Chapter> {
        return chapterRepository.findAllInLibraryChapter()
    }
}
