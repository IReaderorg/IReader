package ireader.domain.usecases.reader

import ireader.domain.models.entities.Chapter
import ireader.domain.utils.extensions.async.nextAfter
import ireader.domain.utils.extensions.async.prevBefore

/**
 * Use case for managing chapter navigation logic
 * Extracts navigation business logic from ViewModel
 */
class ManageChapterNavigationUseCase {
    
    /**
     * Get the next chapter in the list
     * @param currentChapter Current chapter being read
     * @param allChapters List of all chapters
     * @return Next chapter or null if at the end
     */
    fun getNextChapter(
        currentChapter: Chapter?,
        allChapters: List<Chapter>
    ): Chapter? {
        if (currentChapter == null || allChapters.isEmpty()) return null
        
        val index = allChapters.indexOfFirst { it.id == currentChapter.id }
        if (index == -1) return null
        
        return allChapters.nextAfter(index)
    }
    
    /**
     * Get the previous chapter in the list
     * @param currentChapter Current chapter being read
     * @param allChapters List of all chapters
     * @return Previous chapter or null if at the beginning
     */
    fun getPreviousChapter(
        currentChapter: Chapter?,
        allChapters: List<Chapter>
    ): Chapter? {
        if (currentChapter == null || allChapters.isEmpty()) return null
        
        val index = allChapters.indexOfFirst { it.id == currentChapter.id }
        if (index == -1) return null
        
        return allChapters.prevBefore(index)
    }
    
    /**
     * Get chapter index in the list
     * @param chapter Chapter to find
     * @param allChapters List of all chapters
     * @return Index of the chapter or -1 if not found
     */
    fun getChapterIndex(
        chapter: Chapter?,
        allChapters: List<Chapter>
    ): Int {
        if (chapter == null) return -1
        return allChapters.indexOfFirst { it.id == chapter.id }
    }
    
    /**
     * Check if there is a next chapter available
     */
    fun hasNextChapter(
        currentChapter: Chapter?,
        allChapters: List<Chapter>
    ): Boolean {
        return getNextChapter(currentChapter, allChapters) != null
    }
    
    /**
     * Check if there is a previous chapter available
     */
    fun hasPreviousChapter(
        currentChapter: Chapter?,
        allChapters: List<Chapter>
    ): Boolean {
        return getPreviousChapter(currentChapter, allChapters) != null
    }
}
