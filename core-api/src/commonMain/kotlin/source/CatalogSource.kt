

package ireader.core.api.source

import androidx.annotation.Keep
import ireader.core.api.source.model.CommandList
import ireader.core.api.source.model.FilterList
import ireader.core.api.source.model.Listing
import ireader.core.api.source.model.MangasPageInfo
@Keep
interface CatalogSource : ireader.core.api.source.Source {

    override val lang: String

    suspend fun getMangaList(sort: Listing?, page: Int): MangasPageInfo
    suspend fun getMangaList(filters: FilterList, page: Int): MangasPageInfo

    fun getListings(): List<Listing>

    fun getFilters(): FilterList

    fun getCommands(): CommandList
}
