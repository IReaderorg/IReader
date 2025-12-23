package ireader.core.source.model

import kotlinx.serialization.Serializable

/**
 * Paginated list of chapter results.
 * Used by sources that support paginated chapter loading (like LNReader plugins).
 * 
 * This class is serializable for iOS JS bridge support.
 */
@Serializable
data class ChaptersPageInfo(
    val chapters: List<ChapterInfo>,
    val currentPage: Int,
    val totalPages: Int,
    val hasNextPage: Boolean = currentPage < totalPages,
    val hasPreviousPage: Boolean = currentPage > 1
) {
    companion object {
        /**
         * Create empty page info
         */
        fun empty(): ChaptersPageInfo {
            return ChaptersPageInfo(
                chapters = emptyList(),
                currentPage = 1,
                totalPages = 1,
                hasNextPage = false,
                hasPreviousPage = false
            )
        }
        
        /**
         * Create single page (non-paginated) result
         */
        fun singlePage(chapters: List<ChapterInfo>): ChaptersPageInfo {
            return ChaptersPageInfo(
                chapters = chapters,
                currentPage = 1,
                totalPages = 1,
                hasNextPage = false,
                hasPreviousPage = false
            )
        }
        
        /**
         * Create page info for a specific page
         */
        fun forPage(
            chapters: List<ChapterInfo>,
            page: Int,
            totalPages: Int
        ): ChaptersPageInfo {
            return ChaptersPageInfo(
                chapters = chapters,
                currentPage = page,
                totalPages = totalPages,
                hasNextPage = page < totalPages,
                hasPreviousPage = page > 1
            )
        }
    }
    
    /**
     * Check if page is empty
     */
    fun isEmpty(): Boolean = chapters.isEmpty()
    
    /**
     * Check if page has content
     */
    fun isNotEmpty(): Boolean = chapters.isNotEmpty()
    
    /**
     * Get chapter count on this page
     */
    fun size(): Int = chapters.size
    
    /**
     * Check if this is a paginated source (more than 1 page)
     */
    fun isPaginated(): Boolean = totalPages > 1
    
    /**
     * Filter chapters by predicate
     */
    fun filter(predicate: (ChapterInfo) -> Boolean): ChaptersPageInfo {
        return copy(chapters = chapters.filter(predicate))
    }
    
    /**
     * Map chapters with transform
     */
    fun map(transform: (ChapterInfo) -> ChapterInfo): ChaptersPageInfo {
        return copy(chapters = chapters.map(transform))
    }
}
