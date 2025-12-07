package ireader.domain.services.library

import ireader.domain.models.entities.LibraryBook
import ireader.domain.models.library.LibrarySort as DomainLibrarySort

/**
 * Data class representing the sort configuration for the library.
 * 
 * Requirements: 3.3
 */
data class LibrarySort(
    val type: Type = Type.Title,
    val ascending: Boolean = true
) {
    /**
     * Enum representing the available sort types.
     */
    enum class Type {
        Title,
        DateAdded,
        LastRead,
        TotalChapters,
        UnreadCount
    }
    
    /**
     * Convert this sort to a comparator function.
     * 
     * @return A comparator for sorting LibraryBook instances
     */
    fun toComparator(): Comparator<LibraryBook> {
        val baseComparator: Comparator<LibraryBook> = when (type) {
            Type.Title -> compareBy(String.CASE_INSENSITIVE_ORDER) { it.title }
            Type.DateAdded -> compareBy { it.lastUpdate }
            Type.LastRead -> compareBy { it.lastRead }
            Type.TotalChapters -> compareBy { it.totalChapters }
            Type.UnreadCount -> compareBy { it.unreadCount }
        }
        return if (ascending) baseComparator else baseComparator.reversed()
    }
    
    /**
     * Convert to the domain LibrarySort model for repository operations.
     */
    fun toDomainSort(): DomainLibrarySort {
        val domainType = when (type) {
            Type.Title -> DomainLibrarySort.Type.Title
            Type.DateAdded -> DomainLibrarySort.Type.DateAdded
            Type.LastRead -> DomainLibrarySort.Type.LastRead
            Type.TotalChapters -> DomainLibrarySort.Type.TotalChapters
            Type.UnreadCount -> DomainLibrarySort.Type.Unread
        }
        return DomainLibrarySort(domainType, ascending)
    }
    
    companion object {
        val default = LibrarySort(Type.Title, true)
        
        /**
         * Create from domain LibrarySort model.
         */
        fun fromDomainSort(domainSort: DomainLibrarySort): LibrarySort {
            val type = when (domainSort.type) {
                DomainLibrarySort.Type.Title -> Type.Title
                DomainLibrarySort.Type.DateAdded -> Type.DateAdded
                DomainLibrarySort.Type.LastRead -> Type.LastRead
                DomainLibrarySort.Type.TotalChapters -> Type.TotalChapters
                DomainLibrarySort.Type.Unread -> Type.UnreadCount
                else -> Type.Title // Default for unsupported types
            }
            return LibrarySort(type, domainSort.isAscending)
        }
    }
}
