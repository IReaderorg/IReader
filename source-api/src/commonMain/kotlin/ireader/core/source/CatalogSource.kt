

package ireader.core.source


import com.google.errorprone.annotations.Keep
import ireader.core.source.model.CommandList
import ireader.core.source.model.FilterList
import ireader.core.source.model.Listing
import ireader.core.source.model.MangasPageInfo
@Keep
interface CatalogSource : ireader.core.source.Source {

    companion object {
        const val TYPE_NOVEL = 0
        const val TYPE_MANGA = 1
        const val TYPE_MOVIE = 2
        
        /**
         * Get type name from type constant
         */
        fun getTypeName(type: Int): String {
            return when (type) {
                TYPE_NOVEL -> "Novel"
                TYPE_MANGA -> "Manga"
                TYPE_MOVIE -> "Movie"
                else -> "Unknown"
            }
        }
    }

    override val lang: String

    suspend fun getMangaList(sort: Listing?, page: Int): MangasPageInfo
    suspend fun getMangaList(filters: FilterList, page: Int): MangasPageInfo

    fun getListings(): List<Listing>

    fun getFilters(): FilterList

    fun getCommands(): CommandList
    
    /**
     * Check if source supports search
     */
    fun supportsSearch(): Boolean {
        return getFilters().isNotEmpty()
    }
    
    /**
     * Check if source supports latest updates
     */
    fun supportsLatest(): Boolean {
        return getListings().isNotEmpty()
    }
    
    /**
     * Check if source has filters
     */
    fun hasFilters(): Boolean {
        return getFilters().isNotEmpty()
    }
    
    /**
     * Check if source has commands
     */
    fun hasCommands(): Boolean {
        return getCommands().isNotEmpty()
    }
}
