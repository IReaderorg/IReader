package ireader.domain.services.chapter

import ireader.domain.models.entities.Chapter

/**
 * Sealed class representing chapter filter options.
 * Supports single filters and combined filters for complex filtering.
 */
sealed class ChapterFilter {
    /**
     * No filter applied - show all chapters.
     */
    object None : ChapterFilter()
    
    /**
     * Show only read chapters.
     */
    object ReadOnly : ChapterFilter()
    
    /**
     * Show only unread chapters.
     */
    object UnreadOnly : ChapterFilter()
    
    /**
     * Show only bookmarked chapters.
     */
    object BookmarkedOnly : ChapterFilter()
    
    /**
     * Show only downloaded chapters (chapters with content).
     */
    object DownloadedOnly : ChapterFilter()
    
    /**
     * Combine multiple filters (AND logic).
     */
    data class Combined(val filters: Set<ChapterFilter>) : ChapterFilter()
    
    /**
     * Returns a predicate function to filter chapters.
     */
    fun toPredicate(): (Chapter) -> Boolean = when (this) {
        is None -> { _ -> true }
        is ReadOnly -> { chapter -> chapter.read }
        is UnreadOnly -> { chapter -> !chapter.read }
        is BookmarkedOnly -> { chapter -> chapter.bookmark }
        is DownloadedOnly -> { chapter -> chapter.content.isNotEmpty() }
        is Combined -> { chapter ->
            filters.all { filter ->
                // Avoid infinite recursion by not allowing Combined within Combined
                if (filter is Combined) true
                else filter.toPredicate()(chapter)
            }
        }
    }
}

/**
 * Data class representing chapter sort options.
 */
data class ChapterSort(
    val type: Type = Type.NUMBER,
    val ascending: Boolean = true
) {
    enum class Type {
        /** Sort by chapter number */
        NUMBER,
        /** Sort by chapter name alphabetically */
        NAME,
        /** Sort by date added to database */
        DATE_ADDED,
        /** Sort by date last read */
        DATE_READ
    }
    
    companion object {
        val Default = ChapterSort(Type.NUMBER, true)
    }
    
    /**
     * Returns a comparator function to sort chapters.
     */
    fun toComparator(): Comparator<Chapter> {
        val baseComparator: Comparator<Chapter> = when (type) {
            Type.NUMBER -> compareBy { it.number }
            Type.NAME -> compareBy { it.name }
            Type.DATE_ADDED -> compareBy { it.dateFetch }
            Type.DATE_READ -> compareBy { it.lastPageRead }
        }
        
        return if (ascending) baseComparator else baseComparator.reversed()
    }
}
