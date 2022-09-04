

package org.ireader.core_api.source

import androidx.annotation.Keep
import org.ireader.core_api.source.model.CommandList
import org.ireader.core_api.source.model.FilterList
import org.ireader.core_api.source.model.Listing
import org.ireader.core_api.source.model.MangasPageInfo
@Keep
interface CatalogSource : org.ireader.core_api.source.Source {

    override val lang: String

    suspend fun getMangaList(sort: Listing?, page: Int): MangasPageInfo
    suspend fun getMangaList(filters: FilterList, page: Int): MangasPageInfo

    fun getListings(): List<Listing>

    fun getFilters(): FilterList

    fun getCommands(): CommandList
}
