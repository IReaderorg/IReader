package ireader.domain.usecases.local.chapter_usecases

import ireader.domain.models.entities.Chapter
import ireader.domain.data.repository.ChapterRepository
import ireader.domain.usecases.reader.ContentFilterUseCase
import ireader.domain.usecases.reader.TextReplacementUseCase
import ireader.i18n.LAST_CHAPTER
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.mapLatest


class FindChapterById(
    private val chapterRepository: ChapterRepository,
    private val contentFilterUseCase: ContentFilterUseCase? = null,
    private val textReplacementUseCase: TextReplacementUseCase? = null
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
        
        // Apply text replacements and content filter to chapter content
        return chapter?.let { applyContentProcessing(it) }
    }
    
    private suspend fun applyContentProcessing(chapter: Chapter): Chapter {
        if (chapter.content.isEmpty()) {
            return chapter
        }
        
        var processedContent = chapter.content
        
        // Step 1: Apply text replacements first
        if (textReplacementUseCase != null) {
            processedContent = textReplacementUseCase.applyReplacementsToPages(processedContent, chapter.bookId)
        }
        
        // Step 2: Apply content filtering
        if (contentFilterUseCase != null) {
            processedContent = contentFilterUseCase.filterPages(processedContent, chapter.bookId)
        }
        
        return if (processedContent != chapter.content) {
            chapter.copy(content = processedContent)
        } else {
            chapter
        }
    }
}

class SubscribeChapterById(
    private val chapterRepository: ChapterRepository,
    private val contentFilterUseCase: ContentFilterUseCase? = null,
    private val textReplacementUseCase: TextReplacementUseCase? = null
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
        
        // Apply text replacements and content filter to chapter content
        return flow.mapLatest { chapter -> chapter?.let { applyContentProcessing(it) } }
    }
    
    private suspend fun applyContentProcessing(chapter: Chapter): Chapter {
        if (chapter.content.isEmpty()) {
            return chapter
        }
        
        var processedContent = chapter.content
        
        // Step 1: Apply text replacements first
        if (textReplacementUseCase != null) {
            processedContent = textReplacementUseCase.applyReplacementsToPages(processedContent, chapter.bookId)
        }
        
        // Step 2: Apply content filtering
        if (contentFilterUseCase != null) {
            processedContent = contentFilterUseCase.filterPages(processedContent, chapter.bookId)
        }
        
        return if (processedContent != chapter.content) {
            chapter.copy(content = processedContent)
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
